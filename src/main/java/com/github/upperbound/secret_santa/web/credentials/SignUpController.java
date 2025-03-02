package com.github.upperbound.secret_santa.web.credentials;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
import com.github.upperbound.secret_santa.service.ServiceException;
import com.github.upperbound.secret_santa.util.ApplicationParams;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("sign_up")
public class SignUpController {
    private final ApplicationParams applicationParams;
    protected final ParticipantService participantService;
    protected final GroupService groupService;

    @ModelAttribute("participant")
    public Participant participant() {
        return new Participant();
    }

    @ModelAttribute("groupList")
    public List<Group> groupList() {
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
            log.info("sign_up GET");
            if (participantService.getCurrentAuthentication() != Participant.ANONYMOUS) {
                return new ModelAndView("home", httpStatus);
            }
            if (groupService.findAllByHasDrawnFalse().isEmpty())
                return new ModelAndView("home", httpStatus);
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("sign_up", modelMap, httpStatus);
    }

    @PostMapping
    public ModelAndView post(@RequestParam("groupUuid") String groupUuid,
                             @RequestParam("email") String email,
                             @RequestParam(value = "password", required = false) String password,
                             @RequestParam("info") String info,
                             @RequestParam(value = "wishes", required = false) String wishes,
                             @RequestParam(value = "receiveNotifications", required = false) String receiveNotifications,
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
            log.info("sign_up POST with groupUuid={}, email={}, info={}", groupUuid, email, info);
            if (participantService.getCurrentAuthentication() != Participant.ANONYMOUS)
                return new ModelAndView("home", httpStatus);
            if (groupService.findAllByHasDrawnFalse().isEmpty())
                return new ModelAndView("home", httpStatus);
            try {
                Participant createdParticipant = participantService.findByEmail(email);
                if (createdParticipant != null) {
                    modelMap.addAttribute("regConfirmMessage", applicationParams.getTextTemplateTooManyRegistrationAttempts());
                    return new ModelAndView(viewName, modelMap, httpStatus);
                }
                createdParticipant = participantService.createIfNotExists(
                        groupUuid,
                        email,
                        password,
                        info,
                        wishes,
                        Boolean.parseBoolean(receiveNotifications)
                );
                if (createdParticipant == null) {
                    viewName = "sign_up";
                    httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    modelMap.addAttribute("errMessage", applicationParams.getTextTemplateWrongGroup());
                }
//                modelMap.addAttribute("email", createdParticipant.getEmail());
//                modelMap.addAttribute("password", createdParticipant.getPassword());
            } catch (ConstraintViolationException e) {
                viewName = "sign_up";
                httpStatus = HttpStatus.NOT_ACCEPTABLE;
                StringBuilder errMessage = new StringBuilder("[Введены неверные данные:");
                for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
                    if (constraintViolation.getPropertyPath() != null) {
                        switch (constraintViolation.getPropertyPath().toString()) {
                            case "email":
                                errMessage.append(' ')
                                        .append(applicationParams.getTextTemplateUseSpecificEmail())
                                        .append(',');
                                log.error("incorrect email: {}", email);
                                break;
                        }
                    }
                }
                errMessage.deleteCharAt(errMessage.length() - 1).append("]");
                log.error(e.getMessage());
                modelMap.addAttribute("errMessage", errMessage.toString());
            }
            modelMap.addAttribute("regConfirmMessage", applicationParams.getTextTemplateSuccessRegistration());
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } catch (ServiceException e) {
            viewName = "sign_up";
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView(viewName, modelMap, httpStatus);
    }

}
