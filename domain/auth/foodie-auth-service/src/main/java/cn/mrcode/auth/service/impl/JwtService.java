package cn.mrcode.auth.service.impl;

import cn.mrcode.auth.service.pojo.Account;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author mrcode
 * @date 2022/3/27 19:19
 */
@Slf4j
@Service
public class JwtService {
    /**
     * 密钥：生产环境下是外部传进来的
     * 可以采用配置中心加密方式传递进来
     */
    public final static String KEY = "changeIt";
    /**
     * 颁发者
     */
    public final static String ISSUER = "mrcode";
    /**
     * 过期时间：毫秒
     */
    public final static Long TOKEN_EXP_TIME = 600000L;
    /**
     * 用户名
     */
    public final static String USER_NAME = "username";

    /**
     * 生成 token ， 登录完成后，调用该方法
     *
     * @param acct
     * @return
     */
    public String token(Account acct) {
        Date now = new Date();
        Algorithm algorithm = Algorithm.HMAC256(KEY);
        String token = JWT.create()
                // 颁发者
                .withIssuer(ISSUER)
                // 颁发时间
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + TOKEN_EXP_TIME))
                // 追加自定义声明
                .withClaim(USER_NAME, acct.getUserId())
                .sign(algorithm);
        log.info("JWT 生成 user={}", acct.getUserId());
        return token;
    }

    /**
     * 验证 token
     *
     * @param token
     * @param username
     * @return
     */
    public boolean verify(String token, String username) {
        log.info("验证 jwt - username={}", username);

        try {
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            // 构建验证器，使用颁发的时候同一个算法
            JWTVerifier verifier = JWT.require(algorithm)
                    // 这里可以验证颁发的时候放进去的所有信息
                    .withIssuer(ISSUER)
                    .withClaim(USER_NAME, username)
                    .build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            log.error("验证 jwt 失败", e);
            return false;
        }
    }
}
