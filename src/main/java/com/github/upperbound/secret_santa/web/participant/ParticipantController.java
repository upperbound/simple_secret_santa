package com.github.upperbound.secret_santa.web.participant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
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
@RequestMapping("participant")
public class ParticipantController {
    private final ApplicationParams applicationParams;
    private final ParticipantService participantService;

    @GetMapping
    public ModelAndView get(ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            MDC.put(applicationParams.MDC_SESSION_USER, participantService.getCurrentAuthentication().getEmail());
            log.info("participant GET");
            Participant currentParticipant = participantService.getCurrentAuthentication();
            if (currentParticipant == Participant.ANONYMOUS)
                return new ModelAndView("home", httpStatus);
            modelMap.put("participant", currentParticipant);
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("participant", modelMap, httpStatus);
    }

    @PostMapping
    public ModelAndView post(@RequestParam("action") String action,
                             @RequestParam("info") String info,
                             @RequestParam("wishes") String wishes,
                             @RequestParam(value = "receiveNotifications", required = false) String receiveNotifications,
                             ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Participant currentParticipant = participantService.getCurrentAuthentication();
            MDC.put(applicationParams.MDC_SESSION_USER, currentParticipant.getEmail());
            log.info(
                    "participant POST with action={}, info={}",
                    action,
                    info
            );
            if (participantService.getCurrentAuthentication() == Participant.ANONYMOUS)
                return new ModelAndView("home", httpStatus);
            switch (action) {
                case "delete":
                    participantService.delete(currentParticipant.getUuid());
                    return new ModelAndView("forward:logout", modelMap, httpStatus);
                case "update":
                    currentParticipant.setInfo(info);
                    currentParticipant.setWishes(wishes);
                    currentParticipant.setReceiveNotifications(Boolean.parseBoolean(receiveNotifications));
                    participantService.update(currentParticipant);
                    break;
            }
            modelMap.put("participant", currentParticipant);
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } catch (ServiceException e) {
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("participant", modelMap, httpStatus);
    }
}