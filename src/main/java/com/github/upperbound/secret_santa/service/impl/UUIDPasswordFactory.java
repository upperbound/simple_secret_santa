package com.github.upperbound.secret_santa.service.impl;

import com.github.upperbound.secret_santa.service.*;
import com.github.upperbound.secret_santa.util.StaticContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

/**
 * <p> Used to generate password based on {@link UUID} with the maximum length of 32 </p>
 * @author Vladislav Tsukanov
 */
@Profile("uuid-password")
@Service
public class UUIDPasswordFactory implements PasswordFactory {
    private final StaticContext staticContext;
    private final PasswordEncoder passwordEncoder;
    private final NotificationEventFactory notificationEventFactory;
    private final int passwordLength;

    public UUIDPasswordFactory(StaticContext staticContext,
                               PasswordEncoder passwordEncoder,
                               NotificationEventFactory notificationEventFactory,
                               @Value("${app.uuid-pass-length:8}")
                               int passwordLength)
    {
        this.staticContext = staticContext;
        this.passwordEncoder = passwordEncoder;
        this.notificationEventFactory = notificationEventFactory;
        this.passwordLength = passwordLength;
    }

    /**
     * @param pwd not used
     * @param email to send the generated password
     * @param locale for email content
     * @return generated {@link UUID} password, encoded by {@link PasswordEncoder}
     */
    @Override
    public String createPassword(String pwd, String email, Locale locale) {
        String password = newPassword();
        notificationEventFactory.fireNotification(
                email,
                staticContext.getMailCommonSubject(locale),
                staticContext.getMailParticipantCreditsText(email, password, locale)
        );
        return passwordEncoder.encode(password);
    }

    private String newPassword() {
        return passwordLength >= 32 ?
                UUID.randomUUID().toString().replace("-", ""):
                UUID.randomUUID().toString().replace("-", "").substring(0, passwordLength);
    }
}
