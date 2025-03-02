package com.github.upperbound.secret_santa.web.credentials;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.service.GroupService;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.util.ApplicationParams;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("sign_in")
public class SignInController {
    private final ApplicationParams applicationParams;
    private final ParticipantService participantService;
    private final GroupService groupService;

    @ModelAttribute("participant")
    public Participant participant() {
        return new Participant();
    }

    @ModelAttribute("availableGroups")
    public List<Group> availableGroups() {
        return groupService.findAllByHasDrawnFalse();
    }

    @GetMapping
    public ModelAndView get(ModelMap modelMap, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Participant currentParticipant = participantService.getCurrentAuthentication();
            MDC.put(
                    applicationParams.MDC_SESSION_USER,
                    Participant.ANONYMOUS.equals(currentParticipant) ?
                            applicationParams.getRemoteInetAddress(request) :
                            currentParticipant.getEmail()
            );
            log.info("sign_in GET");
            if (participantService.getCurrentAuthentication() != Participant.ANONYMOUS) {
                return new ModelAndView("home", httpStatus);
            }
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("sign_in", modelMap, httpStatus);
    }
}
