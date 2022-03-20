package cn.mrcode;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

/**
 * @author mrcode
 * @date 2022/3/19 09:50
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableTurbine
public class HystrixTurbineApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HystrixTurbineApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
