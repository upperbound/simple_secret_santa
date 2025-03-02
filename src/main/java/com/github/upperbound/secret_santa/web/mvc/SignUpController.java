package com.github.upperbound.secret_santa.web.mvc;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.service.ServiceException;
import com.github.upperbound.secret_santa.util.StaticContext;
import com.github.upperbound.secret_santa.web.dto.ParticipantDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@Controller
@RequestMapping("sign_up")
public class SignUpController extends CommonController {
    private final SignInController signInController;
    private final ParticipantService participantService;

    public SignUpController(StaticContext staticContext,
                            SignInController signInController,
                            ParticipantService participantService)
    {
        super(staticContext);
        this.signInController = signInController;
        this.participantService = participantService;
    }

    @ModelAttribute("participant")
    public ParticipantDTO participant() {
        return new ParticipantDTO();
    }

    @ModelAttribute("customPassword")
    public Boolean customPassword() {
        return staticContext.isCustomPassword();
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
        log.info("sign_up GET");
        if (participantService.getCurrentAuthentication() != Participant.ANONYMOUS)
            return new ModelAndView("home", httpStatus);
        return new ModelAndView("sign_up", modelMap, httpStatus);
    }

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @PostMapping
    public ModelAndView post(@ModelAttribute ParticipantDTO participantDTO,
                             @RequestParam(value = "password", required = false) String password,
                             ModelMap modelMap,
                             HttpServletRequest request,
                             TimeZone timeZone,
                             Locale locale)
    {
        HttpStatus httpStatus;
        Participant currentParticipant = participantService.getCurrentAuthentication();
        MDC.put(StaticContext.MDC_SESSION_USER, currentParticipant.equals(Participant.ANONYMOUS) ?
                staticContext.getRemoteInetAddress(request) :
                currentParticipant.getEmail()
        );
        log.info("sign_up POST with email={}, info={}", participantDTO.getEmail(), participantDTO.getInfo());
        try {
            Participant createdParticipant = participantService.findByEmail(participantDTO.getEmail());
            modelMap.addAttribute("email", participantDTO.getEmail());
            if (createdParticipant != null) {
                modelMap.addAttribute(
                        "regConfirmMessage",
                        staticContext.getTextTemplateTooManyRegistrationAttempts(locale)
                );
                return signInController.get(modelMap, request);
            }
            createdParticipant = participantService.createIfNotExist(
                    participantDTO.getEmail(),
                    password,
                    participantDTO.getInfo(),
                    participantDTO.getReceiveNotifications(),
                    timeZone,
                    locale
            );
            if (createdParticipant == null) {
                httpStatus = HttpStatus.NOT_ACCEPTABLE;
                modelMap.addAttribute(
                        "errMessage",
                        staticContext.getTextTemplateParticipantCreateError(locale)
                );
            } else {
                modelMap.addAttribute(
                        "regConfirmMessage",
                        staticContext.getTextTemplateSuccessRegistration(locale)
                );
                return signInController.get(modelMap, request);
            }
        } catch (ConstraintViolationException e) {
            httpStatus = HttpStatus.NOT_ACCEPTABLE;
            StringBuilder errMessage = new StringBuilder("[Введены неверные данные:");
            for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
                if (constraintViolation.getPropertyPath() != null) {
                    switch (constraintViolation.getPropertyPath().toString()) {
                        case "email":
                            errMessage.append(' ')
                                    .append(staticContext.getTextTemplateWrongEmail(locale))
                                    .append(',');
                            log.error("incorrect email: {}", participantDTO.getEmail());
                            break;
                    }
                }
            }
            errMessage.deleteCharAt(errMessage.length() - 1).append("]");
            log.error(e.getMessage());
            modelMap.addAttribute("errMessage", errMessage.toString());
        } catch (ServiceException e) {
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        }
        return new ModelAndView("sign_up", modelMap, httpStatus);
    }

}
