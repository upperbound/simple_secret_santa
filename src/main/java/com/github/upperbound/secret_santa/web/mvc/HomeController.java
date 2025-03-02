package com.github.upperbound.secret_santa.web.mvc;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.util.StaticContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@Controller
@RequestMapping("home")
public class HomeController extends CommonController {
    private final ParticipantService participantService;

    public HomeController(StaticContext staticContext,
                          ParticipantService participantService)
    {
        super(staticContext);
        this.participantService = participantService;
    }

    @ModelAttribute("participantsWithinGroups")
    public Long participantsWithinGroups() {
        return participantService.participantsWithinGroups();
    }

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @GetMapping
    @PostMapping
    public ModelAndView homePage(ModelMap modelMap, HttpServletRequest request) {
        Participant currentParticipant = participantService.getCurrentAuthentication();
        MDC.put(StaticContext.MDC_SESSION_USER, currentParticipant.equals(Participant.ANONYMOUS) ?
                staticContext.getRemoteInetAddress(request) :
                currentParticipant.getEmail()
        );
        log.info("home GET");
        return new ModelAndView("home", modelMap);
    }
}
