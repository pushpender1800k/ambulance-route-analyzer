package com.aris.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/units")
    public String units() {
        return "units";
    }

    @GetMapping("/hospitals")
    public String hospitals() {
        return "hospitals";
    }

    @GetMapping("/incidents")
    public String incidents() {
        return "incidents";
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "analytics";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    @GetMapping("/patient")
    public String patient() {
        return "patient";
    }

    @GetMapping("/driver")
    public String driver() {
        return "driver";
    }
}
