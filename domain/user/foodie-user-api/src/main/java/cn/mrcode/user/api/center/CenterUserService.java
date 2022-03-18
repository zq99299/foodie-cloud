package cn.mrcode.user.api.center;

import cn.mrcode.user.pojo.Users;
import cn.mrcode.user.pojo.bo.center.CenterUserBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author mrcode
 * @date 2021/2/18 20:59
 */
@FeignClient(value = "foode-user-service",path = "center-user-api")
//@RequestMapping("center-user-api")
public interface CenterUserService {
    /**
     * 根据用户 id 查询用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/profile")
    Users queryUserInfo(@RequestParam("userId") String userId);

    /**
     * 修改用户信息
     *
     * @param userId
     * @param centerUserBO
     */
    @PutMapping("/profile/{userId}")
    Users updateUserInfo(@PathVariable("userId") String userId,
                         @RequestBody CenterUserBO centerUserBO);

    /**
     * 用户头像更新
     *
     * @param userId
     * @param faceUrl
     * @return
     */
    @PutMapping("/updatePhoto")
    Users updateUserFace(@RequestParam("userId") String userId,
                         @RequestParam("faceUrl") String faceUrl);

}
