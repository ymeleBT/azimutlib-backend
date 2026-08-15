package cm.univ.library.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default SMS gateway used until a real provider is configured (app.notifications.sms-enabled=true
 *  plus a real {@link SmsGateway} bean). Logs instead of sending so local dev never fails on missing
 *  SMS credentials. */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.notifications.sms-enabled", havingValue = "false", matchIfMissing = true)
public class NoOpSmsGateway implements SmsGateway {

    @Override
    public void send(String phoneNumber, String message) {
        log.info("[SMS disabled] would send to {}: {}", phoneNumber, message);
    }
}
