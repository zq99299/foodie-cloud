package cn.mrcode.auth.service.pojo;

/**
 * @author mrcode
 * @date 2022/3/27 19:04
 */
public class AuthResponseCode {
    public static final Long SUCCESS = 1L;
    /**
     * 密码不正确
     */
    public static final Long INCORRECT_PWD = 1000L;
    /**
     * 用户不存在
     */
    public static final Long USER_NOT_FOUND = 1001L;
}
