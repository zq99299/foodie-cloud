package cn.mrcode.item;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author mrcode
 * @date 2022/2/27 23:40
 */
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("cn.mrcode.item.mapper")
@ComponentScan(basePackages = {"cn.mrcode.item", "org.n3r.idworker"})
// TDO 后面再加上 feign
public class ItemApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ItemApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
