package com.autolavado.autolavadomvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardingController {

    @GetMapping("/")
    public String root() {
        return "forward:/index.html";
    }

    @GetMapping(value = {
            "/{path:^(?!api$)(?!components$)(?!src$)(?!assets$)(?!css$)(?!js$)(?!imgs$)[^\\.]*}",
            "/{path:^(?!api$)(?!components$)(?!src$)(?!assets$)(?!css$)(?!js$)(?!imgs$)[^\\.]*}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
