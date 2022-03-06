package cn.mrcode.user.api;

import cn.mrcode.user.pojo.Users;
import cn.mrcode.user.pojo.bo.UserBO;
import org.springframework.web.bind.annotation.*;

@RequestMapping("user-api")
public interface UserService {
    /**
     * 查找用户名是否存在
     *
     * @param username
     * @return
     */
    @GetMapping("/user/exists")
    boolean queryUsernameIsExist(@RequestParam("username") String username);

    /**
     * 创建用户
     *
     * @param userBO 接受前端传递过来的 业务对象
     * @return
     */
    @PostMapping("/user")
    Users createUser(@RequestBody UserBO userBO);

    @GetMapping("/verify")
    Users queryUserForLogin(@RequestParam("username") String username,
                            @RequestParam("passwod") String passwod);
}
