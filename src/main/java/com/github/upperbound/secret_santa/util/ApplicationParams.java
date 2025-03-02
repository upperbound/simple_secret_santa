package com.github.upperbound.secret_santa.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.github.upperbound.secret_santa.model.Participant;

import java.time.LocalDateTime;

@Component
public class ApplicationParams {
    public final String MDC_SESSION_USER = "session_user";
    public final String MDC_SERVICE_ACTION = "service_action";
    @Value("${app.server.hostname:192.168.170.81}")
    private String serverHostName;
    @Setter
    private Integer serverPort;
    @Value("${app.mail.text-templates.subject}")
    private String textTemplateSubject;
    @Value("${app.mail.text-templates.draw-results}")
    private String textTemplateDrawResults;
    @Value("${app.mail.text-templates.draw-cancel}")
    private String textTemplateDrawCancel;
    @Value("${app.mail.text-templates.participant-credits}")
    private String textTemplateParticipantCredits;
    @Value("${app.mail.text-templates.participant-draw-changes}")
    private String textTemplateParticipantDrawChanges;
    @Value("${app.mail.text-templates.participant-deleted}")
    private String textTemplateParticipantDeleted;
    @Value("${app.mail.text-templates.participant-2g2-cared}")
    private String textTemplateParticipant2g2Cared;
    @Value("${app.mail.text-templates.participant-2g2-info}")
    private String textTemplateParticipant2g2Info;
    @Value("${app.mail.text-templates.good-morning}")
    private String textTemplateGoodMorning;
    @Value("${app.mail.text-templates.good-day}")
    private String textTemplateGoodDay;
    @Value("${app.mail.text-templates.good-evening}")
    private String textTemplateGoodEvening;
    @Value("${app.mail.text-templates.good-night}")
    private String textTemplateGoodNight;
    @Getter
    @Value("${app.mail.text-templates.success-registration}")
    private String textTemplateSuccessRegistration;
    @Getter
    @Value("${app.mail.text-templates.password-resent}")
    private String textTemplatePasswordResent;
    @Getter
    @Value("${app.mail.text-templates.not-registered}")
    private String textTemplateNotRegistered;
    @Getter
    @Value("${app.mail.text-templates.too-many-registration-attempts}")
    private String textTemplateTooManyRegistrationAttempts;
    @Getter
    @Value("${app.mail.text-templates.wrong-group}")
    private String textTemplateWrongGroup;
    @Getter
    @Value("${app.mail.text-templates.use-specific-email}")
    private String textTemplateUseSpecificEmail;

    public String getApplicationInetAddress() {
        return serverHostName.startsWith("https") ? serverHostName : serverHostName + (serverPort > 0 ? ":" + serverPort : "");
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

    public String getMailCommonSubject(String groupName) {
        return String.format(textTemplateSubject, groupName);
    }

    public String getMailDrawResultsText(String email, String info, String wishes) {
        return String.format(
                textTemplateDrawResults,
                email,
                info,
                wishes == null || wishes.isBlank() ?
                        "" :
                        String.format(textTemplateParticipant2g2Info, wishes),
                getGoodTimeText()
        );
    }

    public String getMailDrawCancelText() {
        return String.format(textTemplateDrawCancel, getGoodTimeText());
    }

    public String getMailDrawChangesText(String email, String info, String wishes) {
        return String.format(
                textTemplateParticipantDrawChanges,
                email,
                info,
                wishes == null || wishes.isBlank() ?
                        "" :
                        String.format(textTemplateParticipant2g2Info, wishes),
                getGoodTimeText()
        );
    }

    public String getMailParticipantCreditsText(String login, String password) {
        return String.format(
                textTemplateParticipantCredits,
                getApplicationInetAddress(),
                login,
                password,
                getGoodTimeText()
        );
    }

    public String getMailParticipantDeletedText(boolean hasDrawn) {
        return String.format(
                textTemplateParticipantDeleted,
                (hasDrawn ? textTemplateParticipant2g2Cared : ""),
                getGoodTimeText()
        );
    }

    public String getGoodTimeText() {
        int hour = LocalDateTime.now().getHour();

        return hour >= 11 && hour <= 16 ?
                textTemplateGoodDay :
                (hour >= 6 && hour <= 10 ?
                        textTemplateGoodMorning :
                        (hour >= 17 && hour <= 22 ?
                                textTemplateGoodEvening :
                                textTemplateGoodNight)
                );
    }
}
