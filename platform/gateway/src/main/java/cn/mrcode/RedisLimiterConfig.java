package cn.mrcode;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * @author mrcode
 * @date 2022/3/29 20:56
 */
@Configuration
public class RedisLimiterConfig {

    /**
     * 创建限流的 key
     *
     * @return
     */
    @Bean
    // 真实环境中，不同的模块会声明不同的 key
    // 使用 @Primary 是为了避免在自动装配的时候 注入一个 KeyResolver 导致不知道注入哪一个而报错
    @Primary
    public KeyResolver remoteAddrKeyResolver() {
        return exchange -> Mono.just(
                // 以访问的 IP 地址作为 key
                exchange.getRequest()
                        .getRemoteAddress()
                        .getHostName());
    }

    @Bean("redisRateLimiterUser")
    @Primary
    public RedisRateLimiter redisRateLimiterUser() {
        // RedisRateLimiter 构造函数有很多个，支持传入 redisTemplete、redis 脚本之类的
        // 这里就用很简单的
        return new RedisRateLimiter(
                // 每秒发几个令牌
                1,
                // 令牌桶的容量
                2
        );
    }

    @Bean("redisRateLimiterItem")
    public RedisRateLimiter redisRateLimiterItem() {
        // 商品服务访问量大，就多个令牌
        return new RedisRateLimiter(
                // 每秒发几个令牌
                20,
                // 令牌桶的容量
                20
        );
    }
}
