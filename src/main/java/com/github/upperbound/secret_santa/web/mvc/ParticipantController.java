package com.github.upperbound.secret_santa.web.mvc;

import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.service.ServiceException;
import com.github.upperbound.secret_santa.util.StaticContext;
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
@RequestMapping("participant")
public class ParticipantController extends CommonController {
    private final ParticipantService participantService;

    public ParticipantController(StaticContext staticContext,
                                 ParticipantService participantService)
    {
        super(staticContext);
        this.participantService = participantService;
    }

    @GetMapping
    public ModelAndView get(ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Participant currentParticipant = participantService.getCurrentAuthentication();
            MDC.put(StaticContext.MDC_SESSION_USER, currentParticipant.getEmail());
            log.info("participant GET");
            modelMap.put("participant", currentParticipant);
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            modelMap.addAttribute("errMessage", e.getMessage());
        } finally {
            MDC.remove(StaticContext.MDC_SESSION_USER);
        }
        return new ModelAndView("participant", modelMap, httpStatus);
    }

    @PostMapping
    public ModelAndView post(@RequestParam("action")
                             String action,
                             @RequestParam("info")
                             String info,
                             @RequestParam(value = "receiveNotifications", required = false)
                             String receiveNotifications,
                             ModelMap modelMap,
                             TimeZone timeZone,
                             Locale locale) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Participant currentParticipant = participantService.getCurrentAuthentication();
            MDC.put(StaticContext.MDC_SESSION_USER, currentParticipant.getEmail());
            log.info(
                    "participant POST with action={}, info={}",
                    action,
                    info
            );
            switch (action) {
                case "delete" -> {
                    if (!participantService.delete(currentParticipant.getEmail(), timeZone, locale)) {
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    } else {
                        return new ModelAndView("forward:logout", modelMap, httpStatus);
                    }
                }
                case "update" -> {
                    currentParticipant.setInfo(info);
                    currentParticipant.setReceiveNotifications(Boolean.parseBoolean(receiveNotifications));
                    participantService.update(currentParticipant);
                }
            }
            modelMap.put("participant", currentParticipant);
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            modelMap.addAttribute("errMessage", e.getMessage());
        } catch (ServiceException e) {
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        } finally {
            MDC.remove(StaticContext.MDC_SESSION_USER);
        }
        return new ModelAndView("participant", modelMap, httpStatus);
    }
}