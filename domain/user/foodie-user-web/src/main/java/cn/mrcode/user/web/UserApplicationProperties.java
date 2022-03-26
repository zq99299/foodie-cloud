package cn.mrcode.user.web;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 管理外部属性
 *
 * @author mrcode
 * @date 2022/3/26 09:38
 */
@Configuration
@RefreshScope  // 支持动态刷新
@Data
public class UserApplicationProperties {
    @Value("${userservice.registration.disabled}")
    private boolean disabledRegistration;
}
