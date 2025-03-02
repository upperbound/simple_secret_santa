package com.github.upperbound.secret_santa.config;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.service.ParticipantService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.util.StaticContext;

import java.util.*;

/**
 * <p> Used to set/revoke {@code superadmin} role on application startup </p>
 * @author Vladislav Tsukanov
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PostInitializer {
    private final ParticipantService participantService;
    @Value("#{${app.users:{}}}")
    private List<String> users;
    @Value("#{${app.superadmins:{}}}")
    private List<String> superAdmins;

    @MDCLog(mdcKey = StaticContext.MDC_SERVICE_ACTION)
    @PostConstruct
    public void setRoles() {
        setRoleForUsers(users, false);
        setRoleForUsers(superAdmins, true);
    }

    private void setRoleForUsers(List<String> users, boolean superadmin) {
        if (users != null)
            users.forEach(value -> {
                Participant participant = participantService.findByEmail(value);
                if (participant != null) {
                    participant.setIsSuperadmin(superadmin);
                    participantService.update(participant);
                }
            });
    }
}
