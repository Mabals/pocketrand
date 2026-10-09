package io.github.mabals.pocketrand.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    // React Router handles pages like /budgets inside index.html,
    // so any single-segment path without a dot (not a file) gets index.html.
    @GetMapping("/{path:[^\\.]*}")
    public String forwardToReactApp() {
        return "forward:/index.html";
    }
}