package cn.mrcode.user.web.controller;

import cn.mrcode.auth.service.AuthService;
import cn.mrcode.auth.service.pojo.Account;
import cn.mrcode.auth.service.pojo.AuthResponse;
import cn.mrcode.auth.service.pojo.AuthResponseCode;
import cn.mrcode.controller.BaseController;
import cn.mrcode.pojo.JSONResult;
import cn.mrcode.user.api.UserService;
import cn.mrcode.user.pojo.Users;
import cn.mrcode.user.pojo.bo.ShopcartBO;
import cn.mrcode.user.pojo.bo.UserBO;
import cn.mrcode.user.pojo.vo.UsersVO;
import cn.mrcode.user.web.UserApplicationProperties;
import cn.mrcode.utils.CookieUtils;
import cn.mrcode.utils.JsonUtils;
import cn.mrcode.utils.MD5Utils;
import cn.mrcode.utils.RedisOperator;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Api(value = "注册登录", tags = {"用户注册登录的相关接口"}) // API 分组
@RestController
@RequestMapping("/passport")
@Slf4j
public class PassportController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisOperator redisOperator;
    @Autowired
    private BaseUserController baseUserController;
    @Autowired
    private UserApplicationProperties userApplicationProperties;
    @Autowired
    private AuthService authService;
    /**
     * token 使用的头
     */
    private static final String AUTH = "Authorization";
    private static final String REFRESH_TOKEN_HEADER = "refresh-token";
    /**
     * 存放用户的头
     */
    private static final String USERNAME = "mrcode-username";

    @ApiOperation(value = "用户名是否存在", notes = "判断用户名是否存在", httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名")
    })
    public JSONResult usernameIsExist(@RequestParam String username) {
        // 1. 判断用户名不能为空
        if (StringUtils.isBlank(username)) {
            return JSONResult.errorMsg("用户名不能为空");
        }

        // 2. 查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return JSONResult.errorMsg("用户名已存在");
        }
        // 3. 用户名没有重复
        return JSONResult.ok();
    }


    @ApiOperation(value = "用户注册", notes = "用户用户注册", httpMethod = "POST")
    @PostMapping("/regist")
    public JSONResult regist(@RequestBody UserBO userBO,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        if (userApplicationProperties.isDisabledRegistration()) {
            log.info("{} 该用户被系统拦截注册", userBO.getUsername());
            return JSONResult.errorMsg("当前注册用户过多，请稍后再试");
        }
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassword = userBO.getConfirmPassword();
        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password) ||
                StringUtils.isBlank(confirmPassword)) {
            return JSONResult.errorMsg("用户名或密码不能为空");
        }
        // 1. 查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return JSONResult.errorMsg("用户名已经存在");
        }
        // 2. 密码长度不能少于 6 位
        if (password.length() < 6) {
            return JSONResult.errorMsg("密码长度不能少于 6");
        }
        // 3. 判断两次密码是否一致
        if (!password.equals(confirmPassword)) {
            return JSONResult.errorMsg("两次密码输入不一致");
        }
        // 4. 实现注册
        Users user = userService.createUser(userBO);

        // 脱敏信息
        // setNullProperty(user);
        // 下面使用 userVo 之后，这个 脱敏信息的就不再需要了

        UsersVO usersVO = baseUserController.convertVo(user);

        // 设置 cookie,使用 userVO 返回
        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(usersVO), true);

        // 同步购物车数据
        synchShopcartData(user.getId(), request, response);
        return JSONResult.ok(user);
    }

    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    @HystrixCommand(
            // 全局唯一的标识服务，默认是方法签名
            commandKey = "loginFail",
            // 全局服务分组，用于组织仪表盘，统计信息分组
            // 如果不指定的话，会默认指定一个值，以类名作为默认值
            groupKey = "password",
            // 指向当前类的 loginFail 方法，签名需要是 public 或则 private
            fallbackMethod = "loginFail",
            // 在列表中声明的异常类型不会触发降级
            ignoreExceptions = {IllegalArgumentException.class},
            // 线程有关属性配置
            // 线程组：多个服务可以共用一个线程组
            threadPoolKey = "threadPoolA",
            threadPoolProperties = {
                    // 有很多属性可配置，配置线程池属性，具体有哪些可以参考 Hystrix 的官方文档
                    // 这里挑几个有代表性的
                    // 核心线程数量
                    @HystrixProperty(name = "coreSize", value = "20"),
                    /*
                      队列最大值
                      size > 0: 使用 LinkedBlockingQueue 来实现请求等待队列
                      默认 -1：SynchronousQueue 阻塞队列，不存储元素；简单说就是一个生产者消费者的例子，但是只有一个位置，给一个，就必须有一个消费完成后，才会有下一个位置
                        对于这种 JUC 的功能，最好还是自己去 debug 源码
                     */
                    @HystrixProperty(name = "maxQueueSize", value = "40"),
                    /*
                      队列大小拒绝阈值：在队列没有达到 maxQueueSize 值时，但是达到了这里的阀值则拒绝
                      在 maxQueueSize = -1 时无效
                     */
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "15"),
                    // 统计相关属性
                    // （线程池）统计窗口持续时间
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1024"),
                    // （线程池）窗口内桶的数量
                    // 这两个加起来的含义就是：在持续时间内，均分多少个桶，这个是什么含义笔者有点忘记了，和有一个好像叫时间窗口的实现算法有关系
                    // 大概好像是：比如 10 分钟，10 个桶，那么随着时间的推移，到了 11 分钟，那么第 1 个桶就被丢弃，然后成为了第 10 个桶
                    // 在这 1 分钟内的数据都被存储在这个桶里面；总共就 10 个桶来回倒腾
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "18")
            },
            commandProperties = {
                    // 熔断降级相关属性也可以放到这里
            }
    )
    public JSONResult login(@RequestBody UserBO userBO,
                            HttpServletRequest request,
                            HttpServletResponse response) throws Exception {
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        // 0. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password)) {
            return JSONResult.errorMsg("用户名或密码不能为空");
        }
        password = MD5Utils.getMD5Str(password);
        Users user = userService.queryUserForLogin(username, password);
        if (user == null) {
            return JSONResult.errorMsg("用户名或密码不正确");
        }

        // 生成
        AuthResponse token = authService.tokenize(user.getId());
        if (!token.getCode().equals(AuthResponseCode.SUCCESS)) {
            log.error("token error uid ={}", user.getId());
            return JSONResult.errorMsg("token error");
        }
        // 将 token 信息添加到 响应的 header 中
        addAuth2Header(response, token.getAccount());

        // 脱敏信息
        // setNullProperty(user);
        UsersVO usersVO = baseUserController.convertVo(user);

        // 设置 cookie
        CookieUtils.setCookie(request, response, "user", JsonUtils.objectToJson(usersVO), true);

        // 生成用户 token 存入 redis 会话
        // 同步购物车数据
        synchShopcartData(user.getId(), request, response);


        return JSONResult.ok(user);
    }

    /**
     * 将 token 信息添加到响应的 header 中，前端处理登录流程的地方，就要额外对照换个 header 进行处理，
     * 并按规范添加到 请求头中，在通过网关鉴权的时候才会通过
     *
     * @param response
     * @param token
     */
    private void addAuth2Header(HttpServletResponse response, Account token) {
        response.addHeader(AUTH, token.getToken());
        response.addHeader(REFRESH_TOKEN_HEADER, token.getRefreshToken());
        response.addHeader(USERNAME, token.getUserId());

        // 告诉前端 token 过期的时间，在过期前如果检测到用户当前还有操作的话，就刷新 token
        Calendar expTime = Calendar.getInstance();
        expTime.add(Calendar.DAY_OF_MONTH, 1);
        response.addHeader("token-exp-time", expTime.getTimeInMillis() + "");
    }

    /**
     * 降级方法，原始方法有什么参数就必须有什么参数，但是可以多一个 Throwable 参数
     *
     * @param userBO
     * @param request
     * @param response
     * @param throwable
     * @return
     * @throws Exception 当异常降级的时候，就会把那个异常注入给你
     */
    private JSONResult loginFail(@RequestBody UserBO userBO,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Throwable throwable) throws Exception {
        return JSONResult.errorMsg("验证码输错了（模仿 12306）");
    }

    private void synchShopcartData(String userId,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {

        // 获取 redis 中的购物车数据
        String key = BaseController.FOODIE_SHOPCART + ":" + userId;
        String redisShopcartJson = redisOperator.get(key);
        // 获取 cookie 中的数据
        String cookieShopcartJson = CookieUtils.getCookieValue(request, BaseController.FOODIE_SHOPCART, true);

        if (StringUtils.isBlank(redisShopcartJson)) {
            // redis 为空，cookie 不为空，直接吧 cookie 放入 redis 中
            if (StringUtils.isNotBlank(cookieShopcartJson)) {
                redisOperator.set(key, cookieShopcartJson);
            }
        } else {
            // redis 不为空，cookie 不为空，合并 cookie 和 redis 中购物商品
            if (StringUtils.isNotBlank(cookieShopcartJson)) {
                /*
                   1. 已经存在的，用 cookie 中对应的数量，覆盖 redis（参考京东）
                   2. 该商品标记诶待删除，统一放入一个待删除的 list
                   3. 从 cookie 中清理所有的待删除 list
                   4. 合并 redis 和 cookie 中的数据
                   5. 更新到 redis 和 cookie 中
                 */
                List<ShopcartBO> redisShopcarts = JsonUtils.jsonToList(redisShopcartJson, ShopcartBO.class);
                List<ShopcartBO> cookieShopcarts = JsonUtils.jsonToList(cookieShopcartJson, ShopcartBO.class);

                List<ShopcartBO> pendingDeleteList = new ArrayList<>();
                for (ShopcartBO redisShopcart : redisShopcarts) {
                    String redisSpecId = redisShopcart.getSpecId();
                    for (ShopcartBO cookieShopcart : cookieShopcarts) {
                        String cookieSpecId = cookieShopcart.getSpecId();

                        // 如果都存在相同的商品：以 cookie 中的数量为准
                        if (redisSpecId.equals(cookieSpecId)) {
                            redisShopcart.setBuyCounts(cookieShopcart.getBuyCounts());
                            pendingDeleteList.add(cookieShopcart);
                        }
                    }
                }
                // 将 cookie 中删除对应的覆盖过的商品数据
                cookieShopcarts.removeAll(pendingDeleteList);
                // 合并两个 list
                redisShopcarts.addAll(cookieShopcarts);
                // 更新 redis 和 cookie 中的数据
                String meragedShopcartJson = JsonUtils.objectToJson(redisShopcarts);
                redisOperator.set(key, meragedShopcartJson);
                CookieUtils.setCookie(request, response,
                        BaseController.FOODIE_SHOPCART,
                        meragedShopcartJson, true);
            } else {
                //  redis 不为空，cookie 为空，直接把 redis 覆盖
                CookieUtils.setCookie(request, response,
                        BaseController.FOODIE_SHOPCART,
                        redisShopcartJson, true);
            }
        }
    }

    /**
     * <pre>
     * 由于目前响应对象是 数据库实体对象，不适用适用 @JsonIgone 直接抹去不显示该字段信息
     * 直接重置为空
     * </pre>
     *
     * @param user
     */
    private void setNullProperty(Users user) {
        user.setPassword(null);
        user.setMobile(null);
        user.setEmail(null);
        user.setCreatedTime(null);
        user.setUpdatedTime(null);
        user.setBirthday(null);
    }

    @ApiOperation(value = "用户退出登录")
    @PostMapping("/logout")
    public JSONResult logout(@RequestParam String userId,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        Account account = Account.builder()
                .token(request.getHeader(AUTH))
                .userId(userId)
                .refreshToken(request.getHeader(REFRESH_TOKEN_HEADER))
                .build();
        AuthResponse resp = authService.delete(account);
        if (!resp.getCode().equals(AuthResponseCode.SUCCESS)) {
            return JSONResult.errorMsg("token error");
        }

        // 这里暂时没有使用到 session 相关信息，不用清理，同样后续还会清空购物车
        // 但是需要清空 cookie 里面的信息
        CookieUtils.deleteCookie(request, response, "user");

        // 用户退出，清理 redis 中的用户会话信息
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);
        // 清理 cookie 中的购物车，但是 redis 中不要清理，相当于购物车数据已经保存在服务端了
        CookieUtils.deleteCookie(request, response, BaseController.FOODIE_SHOPCART);

        return JSONResult.ok();
    }
}
