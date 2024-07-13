package com.inp.msa.inpmsaauth.controller;

import com.inp.msa.inpmsaauth.dto.ClientRegisterRequestDto;
import com.inp.msa.inpmsaauth.service.ClientRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientRegisterController {

    private final ClientRegisterService clientRegisterService;

    @PostMapping("/register")
    public ResponseEntity<String> registerClient(@RequestBody ClientRegisterRequestDto request) {
        clientRegisterService.registerClient(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
