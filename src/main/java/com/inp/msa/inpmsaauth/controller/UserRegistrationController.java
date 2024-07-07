package com.inp.msa.inpmsaauth.controller;

import com.inp.msa.inpmsaauth.dto.UserRegisterRequestDto;
import com.inp.msa.inpmsaauth.service.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class UserRegistrationController {

    private final UserRegisterService userRegisterService;

    @GetMapping
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping
    public String registeredUserAccount(@RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        userRegisterService.save(userRegisterRequestDto);
        return "redirect:/";
    }
}
