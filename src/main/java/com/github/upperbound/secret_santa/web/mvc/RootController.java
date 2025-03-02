package com.github.upperbound.secret_santa.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Vladislav Tsukanov
 */
@Controller
public class RootController {
    @GetMapping
    @PostMapping
    public ModelAndView toHome(ModelMap model) {
        return new ModelAndView("redirect:home", model);
    }
}
