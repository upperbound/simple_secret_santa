package com.github.upperbound.secret_santa.web.group;

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
import com.github.upperbound.secret_santa.model.ParticipantRole;
import com.github.upperbound.secret_santa.service.GroupService;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.service.ServiceException;
import com.github.upperbound.secret_santa.util.ApplicationParams;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("groups")
public class GroupsController {
    private final ApplicationParams applicationParams;
    private final ParticipantService participantService;
    private final GroupService groupService;

    @ModelAttribute("groupList")
    public List<Group> groupList() {
        return groupService.findAll();
    }

    @GetMapping
    public ModelAndView get(@RequestParam(value = "groupUuid", required = false) String groupUuid, ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            MDC.put(applicationParams.MDC_SESSION_USER, participantService.getCurrentAuthentication().getEmail());
            log.info("groups GET with groupUuid={}", groupUuid);
            Participant currentParticipant = participantService.getCurrentAuthentication();
            if (currentParticipant == Participant.ANONYMOUS || ParticipantRole.USER.equals(currentParticipant.getRole()))
                return new ModelAndView("home", httpStatus);
            Group g = groupUuid != null && !groupUuid.isBlank() && ParticipantRole.SUPERADMIN.equals(currentParticipant.getRole()) ?
                    groupService.findById(groupUuid) :
                    currentParticipant.getGroup();
            g = g == null ? currentParticipant.getGroup() : g;
            List<Participant> participantList = participantService.findAllByGroup(g);
            modelMap.addAttribute("group", g);
            modelMap.addAttribute("participantList", participantList);
            modelMap.addAttribute("participantsCount", participantList.size());
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("groups", modelMap, httpStatus);
    }

    @PostMapping
    public ModelAndView post(@RequestParam("action") String action,
                             @RequestParam("groupUuid") String currentGroupUuid,
                             @RequestParam(value = "targetGroupUuid", required = false) String targetGroupUuid,
                             @RequestParam(value = "participantUuid", required = false) String participantUuid,
                             ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            MDC.put(applicationParams.MDC_SESSION_USER, participantService.getCurrentAuthentication().getEmail());
            log.info(
                    "groups POST with action={}, currentGroupUuid={}, targetGroupUuid={}, participantUuid={}",
                    action,
                    currentGroupUuid,
                    targetGroupUuid,
                    participantUuid
            );
            Participant currentParticipant = participantService.getCurrentAuthentication();
            if (currentParticipant == Participant.ANONYMOUS || ParticipantRole.USER.equals(currentParticipant.getRole()))
                return new ModelAndView("home", httpStatus);
            Group group = groupService.findById(currentGroupUuid);
            if (group == null) {
                httpStatus = HttpStatus.NOT_ACCEPTABLE;
                return new ModelAndView("groups", modelMap, httpStatus);
            }
            modelMap.addAttribute("group", group);
            switch (action) {
                case "do_draw":
                    if (group.getHasDrawn())
                        break;
                    if (!groupService.makeADraw(group))
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    break;
                case "cancel_draw":
                    if (!group.getHasDrawn())
                        break;
                    if (!groupService.resetDraw(group))
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    break;
                case "delete_participant":
                    boolean isCurrent = currentParticipant.equals(participantService.findById(participantUuid));
                    if (!participantService.delete(participantUuid))
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    else if (isCurrent)
                        return new ModelAndView("forward:logout", modelMap, HttpStatus.OK);
                    break;
                case "move_participant":
                    if (!participantService.moveParticipant(participantUuid, groupService.findById(targetGroupUuid)))
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    break;
                case "swap_role":
                    if (!participantService.swapRole(participantUuid))
                        httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    break;
            }
            List<Participant> participantList = participantService.findAllByGroup(group);
            modelMap.addAttribute("participantList", participantList);
            modelMap.addAttribute("participantsCount", participantList.size());
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } catch (ServiceException e) {
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("groups", modelMap, httpStatus);
    }
}
