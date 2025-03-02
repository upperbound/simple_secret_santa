package com.github.upperbound.secret_santa.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("home")
public class HomeController {
    private final ApplicationParams applicationParams;
    private final ParticipantService participantService;
    private final GroupService groupService;

    @ModelAttribute("participant")
    public Participant participant() {
        return participantService.getCurrentAuthentication();
    }

    @ModelAttribute("availableGroups")
    public List<Group> availableGroups() {
        return groupService.findAllByHasDrawnFalse();
    }

    @ModelAttribute("participantsCount")
    public Integer participantsCount() {
        return participantService.findAll().size();
    }

    @GetMapping
    @PostMapping
    public ModelAndView homePage(ModelMap modelMap, HttpServletRequest request) {
        try {
            Participant currentParticipant = participantService.getCurrentAuthentication();
            MDC.put(
                    applicationParams.MDC_SESSION_USER,
                    currentParticipant.equals(Participant.ANONYMOUS) ?
                            applicationParams.getRemoteInetAddress(request) :
                            currentParticipant.getEmail()
            );
            log.info("home GET");
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("home", modelMap);
    }
}
