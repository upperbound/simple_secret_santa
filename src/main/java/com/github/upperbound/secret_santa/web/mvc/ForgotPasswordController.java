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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@Controller
@RequestMapping("forgot_password")
public class ForgotPasswordController extends CommonController {
    private final ParticipantService participantService;
    private final LocaleResolver localeResolver;

    public ForgotPasswordController(StaticContext staticContext,
                                    ParticipantService participantService,
                                    LocaleResolver localeResolver)
    {
        super(staticContext);
        this.participantService = participantService;
        this.localeResolver = localeResolver;
    }

    @ModelAttribute("participant")
    public Participant participant() {
        return new Participant();
    }

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @GetMapping
    public ModelAndView get(ModelMap modelMap, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.OK;
        Participant currentParticipant = participantService.getCurrentAuthentication();
        MDC.put(StaticContext.MDC_SESSION_USER, Participant.ANONYMOUS.equals(currentParticipant) ?
                staticContext.getRemoteInetAddress(request) :
                currentParticipant.getEmail()
        );
        log.info("forgot_password GET");
        return new ModelAndView("forgot_password", modelMap, httpStatus);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @PostMapping
    public ModelAndView post(@RequestParam("email") String email,
                             ModelMap modelMap,
                             HttpServletRequest request,
                             TimeZone timeZone,
                             Locale locale)
    {
        String viewName = "sign_in";
        Participant currentParticipant = participantService.getCurrentAuthentication();
        MDC.put(StaticContext.MDC_SESSION_USER, Participant.ANONYMOUS.equals(currentParticipant) ?
                staticContext.getRemoteInetAddress(request) :
                currentParticipant.getEmail()
        );
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            log.info("forgot_password POST with email={}", email);
            participantService.sendResetPasswordLink(email, timeZone, locale);
            modelMap.addAttribute(
                    "regConfirmMessage",
                    staticContext.getTextTemplatePasswordResent(localeResolver.resolveLocale(request))
            );
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            viewName = "forgot_password";
            httpStatus = HttpStatus.NOT_ACCEPTABLE;
            modelMap.addAttribute(
                    "errMessage",
                    staticContext.getTextTemplateNotRegistered(localeResolver.resolveLocale(request))
            );
        } catch (ServiceException e) {
            viewName = "forgot_password";
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        }
        return new ModelAndView(viewName, modelMap, httpStatus);
    }
}
