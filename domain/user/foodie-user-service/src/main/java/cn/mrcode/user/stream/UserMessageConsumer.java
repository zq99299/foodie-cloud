package cn.mrcode.user.stream;

import cn.mrcode.auth.service.AuthService;
import cn.mrcode.auth.service.pojo.Account;
import cn.mrcode.auth.service.pojo.AuthResponse;
import cn.mrcode.auth.service.pojo.AuthResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * @author mrcode
 * @date 2022/4/9 20:10
 */
@Slf4j
@EnableBinding(value = {
        ForceLogoutTopic.class
})
public class UserMessageConsumer {
    @Autowired
    private AuthService authService;

    @StreamListener(ForceLogoutTopic.INPUT)
    public void consumerLogoutMessage(String payload) {
        log.info("强制登出 userId = {}", payload);
        Account account = Account.builder()
                .userId(payload)
                .skipVerifiaction(true)
                .build();
        AuthResponse resp = authService.delete(account);
        if (!AuthResponseCode.SUCCESS.equals(resp.getCode())) {
            log.error("强制登出失败，userId={}", payload);
            throw new RuntimeException("Cannot delete user session");
        }
    }

    // 关联降级方法
    @ServiceActivator(inputChannel = "force-logout-topic.force-logout-group.errors")
    public void fallback(Message message){
        log.info("强制登录失败");

        // 这里要看具体的业务需求了
        // 比如新零售发布库存：失败之后，可以钉钉群通知运营
    }
}
