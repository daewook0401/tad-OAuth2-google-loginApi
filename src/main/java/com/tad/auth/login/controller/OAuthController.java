package com.tad.auth.login.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class OAuthController {

    @PostMapping
    public String postMethodName(@RequestBody String entity) {
        
        
        return entity;
    }
    
}
