package cn.mrcode.order.web;

import cn.mrcode.order.service.impl.fallback.itemservice.ItemCommentsFeignClient;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author mrcode
 * @date 2022/2/27 23:40
 */
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("cn.mrcode.order.mapper")
@ComponentScan(basePackages = {"cn.mrcode.order", "org.n3r.idworker", "cn.mrcode.utils"})
// TDO 后面再加上 feign
@EnableScheduling
@EnableFeignClients(
        basePackages = {
                "cn.mrcode.user.api",
                "cn.mrcode.item.service",  // 这里是 item.api 的包，写项目的时候，定义错了，所以这里是 service
                "cn.mrcode.order.service.impl.fallback.itemservice"
        }
)
@EnableHystrix
public class OrderApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
