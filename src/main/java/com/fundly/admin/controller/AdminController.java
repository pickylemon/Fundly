package com.fundly.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

public class AdminController {
    @GetMapping("/")
    public String admin(){
            return "admin/index";
    }
}
