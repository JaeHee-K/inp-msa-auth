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

    /**
     * 임시 테스트용
     */
    @GetMapping("/authorized")
    public String oauth2Login() {
        return "authorize";
    }
}
