package cn.mrcode.user.web.controller;

import cn.mrcode.user.pojo.Users;
import cn.mrcode.user.pojo.vo.UsersVO;
import cn.mrcode.utils.RedisOperator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author mrcode
 * @date 2022/3/6 19:43
 */
@Service
public class BaseUserController {
    @Autowired
    private RedisOperator redisOperator;
    public static final String REDIS_USER_TOKEN = "redis_user_token";

    public UsersVO convertVo(Users user) {
        // 实现用户的 redis 会话
        String uniqueToken = UUID.randomUUID().toString();
        // 永远不过期，除非自动退出
        // redis_user_token
        redisOperator.set(REDIS_USER_TOKEN + ":" + user.getId(), uniqueToken);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUniqueToken(uniqueToken);
        return usersVO;
    }
}
