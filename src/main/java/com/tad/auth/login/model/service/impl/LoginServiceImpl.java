package com.tad.auth.login.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tad.auth.login.model.dto.LoginResponseDto;
import com.tad.auth.login.model.dto.UserDto;
import com.tad.auth.login.model.entity.User;
import com.tad.auth.login.model.repository.UserRepository;
import com.tad.auth.login.model.service.LoginService;
import com.tad.auth.token.dto.TokenPair;
import com.tad.auth.token.service.JwtTokenService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService{

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    @Value("${app.oauth2.google.client-id}")
    private String googleClientId;

    @Override
    public LoginResponseDto loginWithIdToken(String idToken) {
        Jwt jwt = jwtDecoder.decode(idToken);

        // aud(client_id) 검증
        List<String> aud = jwt.getAudience();
        if (aud == null || aud.stream().noneMatch(googleClientId::equals)) {
            throw new IllegalArgumentException("Invalid Google token audience");
        }

        String sub = jwt.getSubject(); // google_sub
        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        String name = jwt.getClaimAsString("name");
        String picture = jwt.getClaimAsString("picture");

        // 1) sub로 조회(있으면 로그인)
        User user = userRepository.findByGoogleSub(sub)
                .orElseGet(() -> registerNewGoogleUser(sub, email, emailVerified, name, picture));

        // 로그인 시간 기록
        user.recordLogin();

        // 토큰 발급
        TokenPair tokens = jwtTokenService.issue(user);

        // 응답에 유저 정보도 포함하고 싶으면
        UserDto userDto = UserDto.from(user);

        return new LoginResponseDto(tokens.accessToken(), tokens.refreshToken(), userDto);
    }

    private User registerNewGoogleUser(
            String sub,
            String email,
            Boolean emailVerified,
            String name,
            String picture
    ) {
        // 이메일 중복 방지 (email UNIQUE라서 선체크하면 메시지 깔끔)
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        });

        User user = User.builder()
                .googleSub(sub)
                .email(email)
                .emailVerified(Boolean.TRUE.equals(emailVerified))
                .nickname(name != null ? name : "user")
                .pictureUrl(picture)
                .build();

        return userRepository.save(user);
    }
}
