package com.github.upperbound.secret_santa.web.mvc;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.service.ServiceException;
import com.github.upperbound.secret_santa.util.StaticContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@Controller
@RequestMapping("reset_password")
public class ResetPasswordController extends CommonController {
    private final SignInController signInController;
    private final ParticipantService participantService;

    public ResetPasswordController(StaticContext staticContext,
                                   SignInController signInController,
                                   ParticipantService participantService)
    {
        super(staticContext);
        this.signInController = signInController;
        this.participantService = participantService;
    }

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @GetMapping
    public ModelAndView get(@RequestParam(value = "email", required = false) String email,
                            @RequestParam(value = "actionToken", required = false) String actionToken,
                            ModelMap modelMap)
    {
        HttpStatus httpStatus = HttpStatus.OK;
        Participant participant = participantService.findByEmail(email);
        if (participant == null) {
            httpStatus = HttpStatus.NOT_FOUND;
            return new ModelAndView("home", httpStatus);
        }
        MDC.put(StaticContext.MDC_SESSION_USER, participant.getEmail());
        log.info("reset_password GET");
        modelMap.addAttribute("email", email);
        modelMap.addAttribute("actionToken", actionToken);
        return new ModelAndView("reset_password", modelMap, httpStatus);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @PostMapping
    public ModelAndView post(@RequestParam("email") String email,
                             @RequestParam("actionToken") String actionToken,
                             @RequestParam("password") String password,
                             ModelMap modelMap,
                             HttpServletRequest request,
                             TimeZone timeZone,
                             Locale locale)
    {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Participant participant = participantService.findByEmail(email);
            if (participant == null) {
                httpStatus = HttpStatus.NOT_FOUND;
                return new ModelAndView("home", httpStatus);
            }
            MDC.put(StaticContext.MDC_SESSION_USER, participant.getEmail());
            participantService.resetPassword(email, actionToken, password, timeZone, locale);
            modelMap.addAttribute("email", email);
        } catch (ServiceException e) {
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        }
        ModelAndView result = signInController.get(modelMap, request);
        result.setStatus(httpStatus);
        return result;
    }
}
