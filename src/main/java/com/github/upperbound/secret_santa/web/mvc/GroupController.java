package com.github.upperbound.secret_santa.web.mvc;

import com.github.upperbound.secret_santa.model.Group;
import com.github.upperbound.secret_santa.model.Participant;
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

/**
 * @author Vladislav Tsukanov
 */
@Slf4j
@Controller
@RequestMapping("group")
public class GroupController extends CommonController {
    private final GroupsController groupsController;
    private final ParticipantService participantService;
    private final GroupService groupService;

    public GroupController(StaticContext staticContext,
                           GroupsController groupsController,
                           ParticipantService participantService,
                           GroupService groupService)
    {
        super(staticContext);
        this.groupsController = groupsController;
        this.participantService = participantService;
        this.groupService = groupService;
    }

    @ModelAttribute("groupList")
    public List<Group> groupList() {
        return groupService.findAll();
    }

    private boolean canJoin(Group g, Participant participant) {
        return g != null && !g.hasDrawn() && participant.getParticipantGroupLinks().stream()
                .noneMatch(link -> link.getGroup().getUuid().equals(g.getUuid()));
    }

    @GetMapping
    public ModelAndView get(@RequestParam(value = "groupUuid", required = false) String groupUuid,
                            ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        Participant currentParticipant = participantService.getCurrentAuthentication();
        Group g = new Group();
        try {
            MDC.put(StaticContext.MDC_SESSION_USER, currentParticipant.getEmail());
            log.info("group GET with groupUuid={}", groupUuid);
            g = groupUuid != null && !groupUuid.isBlank() ?
                    groupService.findById(groupUuid) :
                    new Group();
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            modelMap.addAttribute("errMessage", e.getMessage());
        } finally {
            modelMap.addAttribute("canJoin", canJoin(g, currentParticipant));
            modelMap.addAttribute("group", g);
            MDC.remove(StaticContext.MDC_SESSION_USER);
        }
        return new ModelAndView("group", modelMap, httpStatus);
    }

    @PostMapping
    public ModelAndView post(@RequestParam(value = "groupUuid", required = false) String groupUuid,
                             @RequestParam("action") String action,
                             @RequestParam("description") String newDescription,
                             ModelMap modelMap,
                             TimeZone timeZone,
                             Locale locale) {
        HttpStatus httpStatus = HttpStatus.OK;
        Participant currentParticipant = participantService.getCurrentAuthentication();
        Group g = new Group();
        try {
            MDC.put(StaticContext.MDC_SESSION_USER, currentParticipant.getEmail());
            String description = newDescription.trim();
            log.info("group POST with action={}, groupUuid={}, description={}", action, groupUuid, description);
            if (!description.isBlank()) {
                switch (action) {
                    case "create" -> {
                        g = groupService.create(description, currentParticipant);
                        participantService.updateCurrentAuthentication();
                        return groupsController.get(g.getUuid(), modelMap);
                    }
                    case "join" -> {
                        if (groupUuid == null) {
                            httpStatus = HttpStatus.BAD_REQUEST;
                            break;
                        }
                        groupService.linkParticipant(groupUuid, currentParticipant.getEmail(), timeZone, locale);
                        participantService.updateCurrentAuthentication();
                        return groupsController.get(groupUuid, modelMap);
                    }
                    case "update" -> {
                        if (groupUuid == null || groupUuid.isBlank())
                            break;
                        if ((g = groupService.findById(groupUuid)) == null || groupService.findByDescription(description) != null) {
                            httpStatus = HttpStatus.NOT_ACCEPTABLE;
                            break;
                        }
                        g.setDescription(description);
                        groupService.update(g);
                        return groupsController.get(groupUuid, modelMap);
                    }
                    case "delete" -> {
                        if (!groupService.delete(groupUuid)) {
                            httpStatus = HttpStatus.NOT_ACCEPTABLE;
                            g = groupService.findById(groupUuid);
                            break;
                        }
                        participantService.updateCurrentAuthentication();
                        return groupsController.get(null, modelMap);
                    }
                }
            }
        } catch (ServiceException e) {
            httpStatus = e.getExceptionCode().getHttpStatus();
            modelMap.addAttribute("errMessage", e.getErrorDescription());
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            modelMap.addAttribute("errMessage", e.getMessage());
        } finally {
            modelMap.addAttribute("canJoin", canJoin(g, currentParticipant));
            modelMap.addAttribute("group", g);
            MDC.remove(StaticContext.MDC_SESSION_USER);
        }
        return new ModelAndView("group", modelMap, httpStatus);
    }
}
