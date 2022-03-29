package cn.mrcode;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author mrcode
 * @date 2022/3/27 10:23
 */
@Configuration
public class RoutesConfiguration {

    /**
     * 定义路由定位器
     *
     * @param builder
     * @return
     */
    @Bean
    @Order
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(p -> p.path("/address/**", "/passport/**", "/center/**", "/userInfo/**")
                        .uri("lb://FOODIE-USER-SERVICE")
                )
                .route(p -> p.path("/items/**")
                        .uri("lb://FOODIE-ITEM-SERVICE")
                )
                .route(p -> p.path("/orders/**", "/mycomments/**", "/myorders/**")
                        .uri("lb://FOODIE-ORDER-SERVICE")
                )
                .build();
    }
}
