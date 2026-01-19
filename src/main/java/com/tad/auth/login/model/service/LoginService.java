package com.tad.auth.login.model.service;

import com.tad.auth.login.model.dto.LoginResponseDto;

public interface LoginService {
    LoginResponseDto loginWithIdToken(String idToken);
}
