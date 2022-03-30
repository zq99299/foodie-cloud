package cn.mrcode.user.web;

import cn.mrcode.auth.service.AuthService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author mrcode
 * @date 2022/2/27 23:40
 */
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("cn.mrcode.user.mapper")
@ComponentScan(basePackages = {"cn.mrcode.user", "org.n3r.idworker", "cn.mrcode.utils"})
@EnableCircuitBreaker
@EnableFeignClients(basePackageClasses = {
        AuthService.class
})
public class UserApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(UserApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
