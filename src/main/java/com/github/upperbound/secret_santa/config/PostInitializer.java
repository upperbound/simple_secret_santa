package com.github.upperbound.secret_santa.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantRole;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.util.ApplicationParams;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class PostInitializer {
    private final ApplicationParams applicationParams;
    private final ParticipantService participantService;
    @Value("#{${app.admins:{}}}")
    private List<String> admins;
    @Value("#{${app.superadmins:{}}}")
    private List<String> superAdmins;

    public void init() {
        try {
            MDC.put(applicationParams.MDC_SESSION_USER, "system");
            setRoleForUsers(ParticipantRole.ADMIN, admins);
            setRoleForUsers(ParticipantRole.SUPERADMIN, superAdmins);
        } catch (Exception e) {
            log.error("unable to init application");
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
    }

    private void setRoleForUsers(ParticipantRole role, List<String> users) {
        if (users != null)
            users.forEach(value -> {
                Participant participant = participantService.findByEmail(value);
                if (participant != null) {
                    participant.setRole(role);
                    participantService.update(participant);
                }
            });
    }
}
