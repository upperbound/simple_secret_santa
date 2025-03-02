package com.github.upperbound.secret_santa.util;

import com.github.upperbound.secret_santa.web.dto.AvailableMessageBundle;
import com.github.upperbound.secret_santa.model.Participant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Provider;
import java.security.Security;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * <p> All necessary data that is used within all application levels </p>
 * @author Vladislav Tsukanov
 */
@Component
public class StaticContext {
    public static final String MDC_SESSION_USER = "session_user";
    public static final String MDC_SERVICE_ACTION = "service_action";
    public static final String MESSAGE_BUNDLE_BASE_NAME = "messages";
    private final Environment environment;
    @Getter
    private final List<AvailableMessageBundle> availableMessageBundles;
    private final ResourceBundleMessageSource messageSource;
    @Getter
    private final Duration actionTokenDuration;
    @Getter
    private final String applicationInetAddress;
    @Getter
    private final boolean customPassword;

    public StaticContext(Environment environment,
                         List<AvailableMessageBundle> availableMessageBundles,
                         ResourceBundleMessageSource messageSource,
                         @Value("${app.action-token-duration}")
                         Duration actionTokenDuration,
                         @Value("${app.server.url}")
                         String serverInetAddress,
                         @Value("${server.address}")
                         String serverAddress,
                         @Value("${server.port}")
                         Integer serverPort,
                         @Value("${server.ssl.enabled}")
                         boolean secure)
    {
        this.environment = environment;
        this.availableMessageBundles = availableMessageBundles;
        this.messageSource = messageSource;
        this.actionTokenDuration = actionTokenDuration;
        String applicationInetAddress = Objects.requireNonNullElseGet(
                serverInetAddress,
                () -> (secure ? "https://" : "http://") +
                        Objects.requireNonNullElse(serverAddress, "localhost") +
                        (serverPort > 0 && ((secure && serverPort != 443) || (!secure && serverPort != 80)) ?
                                ":" + serverPort + "/" :
                                "/")
        );
        this.applicationInetAddress = applicationInetAddress.endsWith("/") ?
                applicationInetAddress :
                applicationInetAddress + "/";
        this.customPassword = !environment.matchesProfiles("no-password | uuid-password");
    }

    public static BouncyCastleProvider getBCProvider() {
        Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null)
            Security.addProvider(provider = new BouncyCastleProvider());
        return (BouncyCastleProvider) provider;
    }

    public String getRemoteInetAddress(HttpServletRequest request) {
        if (request == null)
            return Participant.ANONYMOUS.getEmail();
        String host = request.getRemoteHost();
        String address = request.getRemoteAddr();
        return host != null ?
                (host.equals(address) ? address : host + "_" + address) :
                address;
    }

    public String getTextTemplateSuccessRegistration(Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.success-registration",
                null,
                locale
        );
    }

    public String getTextTemplatePasswordResent(Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.password-resent",
                null,
                locale
        );
    }

    public String getTextTemplateTooManyRegistrationAttempts(Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.too-many-registration-attempts",
                null,
                locale
        );
    }

    public String getTextTemplateParticipantCreateError(Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.participant-create-error",
                null,
                locale
        );
    }

    public String getTextTemplateWrongEmail(Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.wrong-email",
                null,
                locale
        );
    }

    public String getTextTemplatePasswordResetLink(String email, String token, Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.password-reset-link",
                new Object[] {
                        UriComponentsBuilder.fromUriString(getApplicationInetAddress() + "reset_password")
                                .queryParam("email", email)
                                .queryParam("action_token", token)
                                .build()
                                .toUriString()
                },
                locale
        );
    }

    public String getTextTemplateNotRegistered(Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.not-registered",
                null,
                locale
        );
    }

    public String getMailCommonSubject(Locale locale) {
        return getMailCommonSubject(null, locale);
    }

    public String getMailCommonSubject(String groupName, Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.subject",
                new Object[] {groupName == null ? "" : groupName},
                locale
        );
    }

    public String getMailDrawResultsText(String email,
                                         String info,
                                         String wishes,
                                         String drawResultsAdditionalInfo,
                                         TimeZone timeZone,
                                         Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.draw-results",
                new Object[] {
                        email,
                        info,
                        (wishes == null || wishes.isBlank() ?
                                "" :
                                messageSource.getMessage(
                                        "app.mail.text-templates.participant-2g2-info",
                                        new Object[]{wishes},
                                        locale)
                        ),
                        (drawResultsAdditionalInfo == null || drawResultsAdditionalInfo.isBlank() ?
                                messageSource.getMessage(
                                        "app.mail.text-templates.draw-results-default",
                                        null,
                                        locale) :
                                drawResultsAdditionalInfo
                        ),
                        getGoodTimeText(timeZone, locale)
                },
                locale
        );
    }

    public String getMailDrawCancelText(TimeZone timeZone, Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.draw-cancel",
                new Object[] {getGoodTimeText(timeZone, locale)},
                locale
        );
    }

    public String getMailDrawChangesText(String email,
                                         String info,
                                         String wishes,
                                         String drawResultsAdditionalInfo,
                                         TimeZone timeZone,
                                         Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.participant-draw-changes",
                new Object[] {
                        email,
                        info,
                        (wishes == null || wishes.isBlank() ?
                                "" :
                                messageSource.getMessage(
                                        "app.mail.text-templates.participant-2g2-info",
                                        new Object[]{wishes},
                                        locale)
                        ),
                        (drawResultsAdditionalInfo == null || drawResultsAdditionalInfo.isBlank() ?
                                messageSource.getMessage(
                                        "app.mail.text-templates.draw-results-default",
                                        null,
                                        locale) :
                                drawResultsAdditionalInfo
                        ),
                        getGoodTimeText(timeZone, locale)
                },
                locale
        );
    }

    public String getMailParticipantRegistrationText(TimeZone timeZone, Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.participant-credits",
                new Object[] {
                        getApplicationInetAddress(),
                        getGoodTimeText(timeZone, locale)
                },
                locale
        );

    }

    public String getMailParticipantCreditsText(String login, String password, Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.participant-registration",
                new Object[] {
                        login,
                        password
                },
                locale
        );
    }

    public String getMailParticipantDeletedText(boolean hasDrawn, TimeZone timeZone, Locale locale) {
        return messageSource.getMessage(
                "app.mail.text-templates.participant-deleted",
                new Object[] {
                        (hasDrawn ?
                                messageSource.getMessage(
                                        "app.mail.text-templates.participant-2g2-cared",
                                        null,
                                        locale) :
                                ""
                        ),
                        getGoodTimeText(timeZone, locale)
                },
                locale
        );
    }

    public String getGoodTimeText(Locale locale) {
        return getGoodTimeText(TimeZone.getDefault(), locale);
    }

    public String getGoodTimeText(TimeZone timeZone, Locale locale) {
        int hour = ZonedDateTime.now(timeZone.toZoneId()).getHour();
        return hour >= 11 && hour <= 16 ?
                messageSource.getMessage("app.mail.text-templates.good-day", null, locale) :
                (hour >= 6 && hour <= 10 ?
                        messageSource.getMessage("app.mail.text-templates.good-morning", null, locale) :
                        (hour >= 17 && hour <= 22 ?
                                messageSource.getMessage("app.mail.text-templates.good-evening", null, locale) :
                                messageSource.getMessage("app.mail.text-templates.good-night", null, locale)
                        )
                );
    }
}
