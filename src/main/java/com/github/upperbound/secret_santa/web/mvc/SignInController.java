package com.github.upperbound.secret_santa.web.mvc;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.service.GroupService;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.util.StaticContext;
import com.github.upperbound.secret_santa.web.dto.ParticipantDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@Controller
@RequestMapping("sign_in")
public class SignInController extends CommonController {
    private final ParticipantService participantService;
    private final GroupService groupService;

    public SignInController(StaticContext staticContext,
                            ParticipantService participantService,
                            GroupService groupService)
    {
        super(staticContext);
        this.participantService = participantService;
        this.groupService = groupService;
    }

    @ModelAttribute("participant")
    public ParticipantDTO participant() {
        return new ParticipantDTO();
    }

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @GetMapping
    public ModelAndView get(ModelMap modelMap, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.OK;
        Participant currentParticipant = participantService.getCurrentAuthentication();
        MDC.put(StaticContext.MDC_SESSION_USER, currentParticipant.equals(Participant.ANONYMOUS) ?
                staticContext.getRemoteInetAddress(request) :
                currentParticipant.getEmail()
        );
        log.info("sign_in GET");
        if (participantService.getCurrentAuthentication() != Participant.ANONYMOUS) {
            return new ModelAndView("home", httpStatus);
        }
        return new ModelAndView("sign_in", modelMap, httpStatus);
    }
}
