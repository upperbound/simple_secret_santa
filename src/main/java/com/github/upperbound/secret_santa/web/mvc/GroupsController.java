package com.github.upperbound.secret_santa.web.mvc;

import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
import com.github.upperbound.secret_santa.model.ParticipantGroupLink;
import com.github.upperbound.secret_santa.model.ParticipantRole;
import com.github.upperbound.secret_santa.service.GroupService;
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

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@Controller
@RequestMapping("groups")
public class GroupsController extends CommonController {
    private final ParticipantService participantService;
    private final GroupService groupService;

    public GroupsController(StaticContext staticContext,
                            ParticipantService participantService,
                            GroupService groupService)
    {
        super(staticContext);
        this.participantService = participantService;
        this.groupService = groupService;
    }

    @GetMapping
    public ModelAndView get(@RequestParam(value = "groupUuid", required = false) String groupUuid, ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            MDC.put(StaticContext.MDC_SESSION_USER, participantService.getCurrentAuthentication().getEmail());
            log.info("groups GET with groupUuid={}", groupUuid);
            Participant currentParticipant = participantService.getCurrentAuthentication();
            AtomicReference<ParticipantGroupLink> currentGroupLink = new AtomicReference<>();
            AtomicReference<String> gifteeWishes = new AtomicReference<>();
            List<Group> groupList = groupService.findAllByParticipant(currentParticipant);
            Group currentGroup = groupUuid == null || groupUuid.isBlank() ?
                    (groupList.isEmpty() ? null : groupList.getFirst()) :
                    groupService.findById(groupUuid);
            List<Participant> participantList = currentGroup == null ?
                    List.of() :
                    participantService.findAllByGroup(currentGroup.getUuid());
            participantList.forEach(participant -> {
                if (currentGroup == null)
                    return;
                groupService.findLink(participant.getUuid(), currentGroup.getUuid())
                        .ifPresent(link -> {
                            if (currentParticipant.equals(link.getParticipant())) {
                                currentGroupLink.set(link);
                                if (currentGroup.hasDrawn() && link.getGiftee() != null) {
                                    groupService.findLink(link.getGiftee().getUuid(), currentGroup.getUuid())
                                            .ifPresent( gifteeLink -> gifteeWishes.set(gifteeLink.getWishes()));
                                }
                            }
                            participant.setCurrentRole(
                                    ParticipantRole.ADMIN.equals(link.getRole()) ?
                                            ParticipantRole.ADMIN :
                                            ParticipantRole.USER
                            );
                        });
            });
            modelMap.addAttribute("participant", currentParticipant);
            modelMap.addAttribute("participantList", participantList);
            modelMap.addAttribute("group", currentGroup);
            modelMap.addAttribute("groupList", groupList);
            modelMap.addAttribute("currentGroupLink", currentGroupLink.get());
            modelMap.addAttribute("gifteeWishes", gifteeWishes.get());
        } catch (ServiceException e) {
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            modelMap.addAttribute("errMessage", e.getMessage());
        } finally {
            MDC.remove(StaticContext.MDC_SESSION_USER);
        }
        return new ModelAndView("groups", modelMap, httpStatus);
    }

    @PostMapping
    public ModelAndView post(@RequestParam("action") String action,
                             @RequestParam("groupUuid") String groupUuid,
                             @RequestParam(value = "participantUuid", required = false) String participantUuid,
                             @RequestParam(value = "wishes", required = false) String wishes,
                             ModelMap modelMap,
                             TimeZone timeZone,
                             Locale locale) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            MDC.put(StaticContext.MDC_SESSION_USER, participantService.getCurrentAuthentication().getEmail());
            log.info(
                    "groups POST with action={}, currentGroupUuid={}, participantUuid={}",
                    action,
                    groupUuid,
                    participantUuid
            );
            Participant currentParticipant = participantService.getCurrentAuthentication();
            Group group = groupService.findById(groupUuid);
            if (group == null) {
                httpStatus = HttpStatus.NOT_ACCEPTABLE;
                return new ModelAndView("groups", modelMap, httpStatus);
            }
            modelMap.addAttribute("group", group);
            switch (action) {
                case "do_draw" -> {
                    if (group.hasDrawn())
                        break;
                    if (!groupService.makeADraw(group.getUuid(), timeZone, locale)) {
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    }
                }
                case "cancel_draw" -> {
                    if (!group.hasDrawn())
                        break;
                    if (!groupService.resetDraw(group.getUuid(), timeZone, locale))
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                }
                case "delete_participant" -> {
                    Participant participant = participantService.findById(participantUuid);
                    if (participant == null ||
                            !groupService.unlinkParticipant(group.getUuid(), participant.getEmail(), timeZone, locale))
                    {
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    } else if (currentParticipant.equals(participant))
                        participantService.updateCurrentAuthentication();
                }
                case "swap_role" -> {
                    Participant participant = participantService.findById(participantUuid);
                    if (participant == null) {
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    } else {
                        ParticipantRole currentRole = participant.getParticipantGroupLinks().stream()
                                .filter(link -> group.equals(link.getGroup()))
                                .map(ParticipantGroupLink::getRole)
                                .findFirst().orElse(null);
                        if (currentRole == null ||
                                participantService.setRole(
                                        participant.getEmail(),
                                        group.getUuid(),
                                        currentRole.equals(ParticipantRole.USER) ? ParticipantRole.ADMIN : ParticipantRole.USER))
                        {
                            httpStatus = HttpStatus.NOT_ACCEPTABLE;
                        }
                    }
                }
                case "update_wishes" -> {
                    if (!groupService.updateWishes(group, currentParticipant, wishes)) {
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    }
                }
            }
            get(groupUuid, modelMap);
        } catch (ServiceException e) {
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            modelMap.addAttribute("errMessage", e.getMessage());
        } finally {
            MDC.remove(StaticContext.MDC_SESSION_USER);
        }
        return new ModelAndView("groups", modelMap, httpStatus);
    }
}
