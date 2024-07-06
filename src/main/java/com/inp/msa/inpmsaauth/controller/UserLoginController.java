package com.inp.msa.inpmsaauth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UserLoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
