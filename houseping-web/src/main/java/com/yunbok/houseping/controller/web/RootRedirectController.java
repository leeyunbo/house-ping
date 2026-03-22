package com.yunbok.houseping.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String home() {
        return "redirect:/home";
    }

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/admin/dashboard";
    }
}
