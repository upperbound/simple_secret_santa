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
import com.github.upperbound.secret_santa.service.GroupService;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.util.ApplicationParams;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("group")
public class GroupController {
    private final ApplicationParams applicationParams;
    private final ParticipantService participantService;
    private final GroupService groupService;

    @ModelAttribute("groupList")
    public List<Group> groupList() {
        return groupService.findAll();
    }

    @GetMapping
    public ModelAndView get(@RequestParam(value = "groupUuid", required = false) String groupUuid,
                            ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            MDC.put(applicationParams.MDC_SESSION_USER, participantService.getCurrentAuthentication().getEmail());
            log.info("group GET with groupUuid={}", groupUuid);
            Group g = groupUuid != null && !groupUuid.isBlank() ?
                    groupService.findById(groupUuid) :
                    new Group();
            modelMap.addAttribute("group", g);
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("group", modelMap, httpStatus);
    }

    @PostMapping
    public ModelAndView post(@RequestParam(value = "groupUuid", required = false) String groupUuid,
                             @RequestParam("action") String action,
                             @RequestParam("description") String newDescription,
                             ModelMap modelMap) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            MDC.put(applicationParams.MDC_SESSION_USER, participantService.getCurrentAuthentication().getEmail());
            String description = newDescription.trim();
            log.info("group POST with action={}, groupUuid={}, description={}", action, groupUuid, description);
            Group g = new Group();
            if (!description.isBlank()) {
                switch (action) {
                    case "create":
                        g = groupService.createIfNotExist(description);
                        break;
                    case "update":
                        if (groupUuid == null || groupUuid.isBlank())
                            break;
                        if ((g = groupService.findById(groupUuid)) == null || groupService.findByDescription(description) != null) {
                            httpStatus = HttpStatus.NOT_ACCEPTABLE;
                            break;
                        }
                        g.setDescription(description);
                        groupService.update(g);
                        break;
                    case "delete":
                        if (g.getHasDrawn())
                            break;
                        if (!groupService.delete(groupUuid))
                            httpStatus = HttpStatus.NOT_ACCEPTABLE;
                        break;
                }
            }
            modelMap.addAttribute("group", g);
            modelMap.addAttribute("groupList", groupService.findAll());
        } catch (RuntimeException e) {
            log.error("error while performing action", e);
            throw e;
        } finally {
            MDC.remove(applicationParams.MDC_SESSION_USER);
        }
        return new ModelAndView("group", modelMap, httpStatus);
    }
}
