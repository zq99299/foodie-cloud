package cn.mrcode.user.web;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
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
// TDO 后面再加上 feign
public class UserApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(UserApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
