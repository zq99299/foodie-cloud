package cn.mrcode.auth.service.impl;

import cn.mrcode.auth.service.AuthService;
import cn.mrcode.auth.service.pojo.Account;
import cn.mrcode.auth.service.pojo.AuthResponse;
import cn.mrcode.auth.service.pojo.AuthResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author mrcode
 * @date 2022/3/29 21:36
 */
@Service
@Slf4j
@RestController
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String USER_TOKEN = "USER_TOKEN";

    @Override
    public AuthResponse tokenize(String userId) {
        Account account = Account.builder()
                .userId(userId)
                .build();

        String token = jwtService.token(account);
        account.setToken(token);
        account.setRefreshToken(UUID.randomUUID().toString());

        // 还需要将这个 结果 信息存放到某个地方，后续才刷新的时候才能拿到对应的账户信息
        redisTemplate.opsForValue().set(buildTokenKey(account.getUserId()), account);
        redisTemplate.opsForValue().set(account.getRefreshToken(), account);
        return AuthResponse.builder()
                .account(account)
                .code(AuthResponseCode.SUCCESS)
                .build();
    }

    private String buildTokenKey(String userId) {
        return USER_TOKEN + userId;
    }

    @Override
    public AuthResponse verify(Account account) {
        boolean success = jwtService.verify(account.getToken(), account.getUserId());
        // 验证之后，需要校验 redis 中的 token 是否还存在
        Object o = redisTemplate.opsForValue().get(buildTokenKey(account.getUserId()));
        if (success && o == null) {
            success = false;
        }
        return AuthResponse.builder()
                // todo 此处最好用 invalid token 之类的错误码
                .code(success ? AuthResponseCode.SUCCESS : AuthResponseCode.USER_NOT_FOUND)
                .build();
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        Account account = (Account) redisTemplate.opsForValue().get(refreshToken);
        if (account == null) {
            return AuthResponse.builder()
                    .code(AuthResponseCode.USER_NOT_FOUND)
                    .build();
        }

        // 删除老的 刷新 token
        redisTemplate.delete(refreshToken);
        // 删除老的 token
        redisTemplate.delete(buildTokenKey(account.getUserId()));

        String jwt = jwtService.token(account);
        account.setToken(jwt);
        account.setRefreshToken(UUID.randomUUID().toString());

        // 存入新的 token 信息
        redisTemplate.opsForValue().set(buildTokenKey(account.getUserId()), account);
        redisTemplate.opsForValue().set(account.getRefreshToken(), account);
        return AuthResponse.builder()
                .account(account)
                .code(AuthResponseCode.SUCCESS)
                .build();
    }

    @Override
    public AuthResponse delete(Account account) {
        AuthResponse resp = new AuthResponse();
        if (account.isSkipVerifiaction()) {
            redisTemplate.delete(buildTokenKey(account.getUserId()));
            resp.setCode(AuthResponseCode.SUCCESS);
        } else {
            AuthResponse verify = verify(account);
            if (verify.getCode().equals(AuthResponseCode.SUCCESS)) {
                // 删除登录时放进去的 token 信息
                Account sa = verify.getAccount();
                redisTemplate.delete(buildTokenKey(account.getUserId()));
                redisTemplate.delete(sa.getRefreshToken());
                resp.setCode(AuthResponseCode.SUCCESS);
            } else {
                resp.setCode(AuthResponseCode.USER_NOT_FOUND);
            }
        }
        return resp;
    }
}
