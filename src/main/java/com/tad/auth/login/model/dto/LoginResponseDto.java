package com.tad.auth.login.model.dto;

public record LoginResponseDto(
    String accessToken,
    String refreshToken,
    UserDto user
) {

}
