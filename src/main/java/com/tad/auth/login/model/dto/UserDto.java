package com.tad.auth.login.model.dto;

import com.tad.auth.login.model.entity.User;

public record UserDto(    
    Long id,
    String publicId,
    String googleSub,
    String email,
    Boolean emailVerified,
    String nickname,
    String pictureUrl,
    String status
) {
    public static UserDto from(User user) {
        return new UserDto(
            user.getId(),
            user.getPublicId().toString(),
            user.getGoogleSub(),
            user.getEmail(),
            user.getEmailVerified(),
            user.getNickname(),
            user.getPictureUrl(),
            user.getStatus()
        );
    }
}
