package cn.mrcode;

import cn.mrcode.auth.service.AuthService;
import cn.mrcode.auth.service.pojo.Account;
import cn.mrcode.auth.service.pojo.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author mrcode
 * @date 2022/3/27 20:21
 */
@Slf4j
@Component
public class AuthFilter implements GatewayFilter, Ordered {
    /**
     * token 使用的头
     */
    private static final String AUTH = "Authorization";
    /**
     * 存放用户的头
     */
    private static final String USERNAME = "mrcode-username";

    @Autowired
    private AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Auth 开始");
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String token = headers.getFirst(AUTH);
        String username = headers.getFirst(USERNAME);

        ServerHttpResponse response = exchange.getResponse();
        if (StringUtils.isBlank(token)) {
            log.error("token 未找到");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        Account account = new Account();
        account.setToken(token);
        account.setUserId(username);
        AuthResponse resp = authService.verify(account);
        if (resp.getCode() != 1L) {
            log.error("token 无效");
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        // TODO 将用户信息存放在请求 header 中传递给下游业务
        ServerHttpRequest buildRequest = request.mutate()
                .header(USERNAME, new String[]{username})
                .build();

        // todo 如果响应中需要放数据，也可以放在 response 的 header 中
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(USERNAME, username);
        return chain.filter(exchange.mutate()
                .request(buildRequest)
                .response(response)
                .build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
