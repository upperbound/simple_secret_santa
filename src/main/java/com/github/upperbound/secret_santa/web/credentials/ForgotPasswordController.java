package com.github.upperbound.secret_santa.web.credentials;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.service.ServiceException;
import com.github.upperbound.secret_santa.util.ApplicationParams;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("forgot_password")
public class ForgotPasswordController {
    private final ApplicationParams applicationParams;
    private final ParticipantService participantService;

    @ModelAttribute("participant")
    public Participant participant() {
        return new Participant();
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
            log.info("forgot_password GET");
            if (participantService.getCurrentAuthentication() != Participant.ANONYMOUS) {
                return new ModelAndView("home", httpStatus);
            }
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("forgot_password", modelMap, httpStatus);
    }

    @PostMapping
    public ModelAndView post(@RequestParam("email") String email,
                             ModelMap modelMap,
                             HttpServletRequest request) {
        String viewName = "sing_in";
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Participant currentParticipant = participantService.getCurrentAuthentication();
            MDC.put(
                    applicationParams.MDC_SESSION_USER,
                    Participant.ANONYMOUS.equals(currentParticipant) ?
                            applicationParams.getRemoteInetAddress(request) :
                            currentParticipant.getEmail()
            );
            log.info("forgot_password POST with email={}", email);
            if (participantService.getCurrentAuthentication() != Participant.ANONYMOUS)
                return new ModelAndView(viewName, modelMap, httpStatus);
            participantService.sendCredentialsToUser(email);
            modelMap.addAttribute("regConfirmMessage", applicationParams.getTextTemplatePasswordResent());
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            viewName = "forgot_password";
            httpStatus = HttpStatus.NOT_ACCEPTABLE;
            modelMap.addAttribute("errMessage", applicationParams.getTextTemplateNotRegistered());
        } catch (ServiceException e) {
            viewName = "forgot_password";
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView(viewName, modelMap, httpStatus);
    }
}
