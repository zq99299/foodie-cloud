package cn.mrcode;

import cn.mrcode.auth.service.AuthService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author mrcode
 * @date 2022/3/29 20:21
 */
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackageClasses = {
        AuthService.class
})
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
    }
}
