package com.github.upperbound.secret_santa;

import com.github.upperbound.secret_santa.service.NotificationEventFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing
@EnableScheduling
public class SecretSantaApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SecretSantaApplication.class, args);
        try {
            Environment environment = context.getBean(Environment.class);
            if (environment.matchesProfiles("send-mail-notifications")) {
                JavaMailSenderImpl mailSender = (JavaMailSenderImpl) context.getBean(JavaMailSender.class);
                try {
                    mailSender.testConnection();
                } catch (Exception ex) {
                    throw new RuntimeException("unable to connect to mail server", ex);
                }
            }
            NotificationEventFactory notificationEventFactory = context.getBean(NotificationEventFactory.class);
//            NotificationEvent mailEvent = notificationEventFactory.fireMailEvent(
//                    "no-reply@upperbound.msk.ru",
//                    "v.tsukanoff@upperbound.msk.ru",
//                    "test message",
//                    "hello there",
//                    0
//            );
//            try {
//                if (NotificationEvent.Status.SENT == mailEvent.waitForSend())
//                    System.out.println("fuck yeah");
//            } catch (ServiceException e) {
//                throw new RuntimeException(e);
//            }
        } catch (Exception ex) {
            context.close();
            log.error(ex.getMessage(), ex);
            System.exit(1);
        }
    }
}
