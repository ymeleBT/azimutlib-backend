package cm.univ.library.notification;

import cm.univ.library.common.enums.NotificationChannel;
import cm.univ.library.common.enums.NotificationStatus;
import cm.univ.library.common.enums.NotificationType;
import cm.univ.library.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final SmsGateway smsGateway;

    @Transactional
    public void notifyUser(User user, NotificationType type, String message) {
        if (user.getEmail() != null) {
            send(user, type, NotificationChannel.EMAIL, message);
        }
        if (user.getPhone() != null) {
            send(user, type, NotificationChannel.SMS, message);
        }
    }

    private void send(User user, NotificationType type, NotificationChannel channel, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .channel(channel)
                .message(message)
                .build();

        try {
            switch (channel) {
                case EMAIL -> sendEmail(user.getEmail(), message);
                case SMS -> smsGateway.send(user.getPhone(), message);
                case IN_APP -> { /* surfaced by NotificationRepository queries only */ }
            }
            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception ex) {
            log.error("Failed to send {} notification to user {}", channel, user.getId(), ex);
            notification.setStatus(NotificationStatus.FAILED);
        }

        notificationRepository.save(notification);
    }

    private void sendEmail(String to, String body) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject("University Library");
        mailMessage.setText(body);
        mailSender.send(mailMessage);
    }
}
