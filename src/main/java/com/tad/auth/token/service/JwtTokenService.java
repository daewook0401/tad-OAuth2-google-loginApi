package com.tad.auth.token.service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tad.auth.login.model.entity.User;
import com.tad.auth.token.dto.TokenPair;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    @Value("${app.jwt.secret-base64}")
    private String secretBase64;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-minutes}")
    private long accessMinutes;

    @Value("${app.jwt.refresh-days}")
    private long refreshDays;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
    }

    public String createAccessToken(User user){
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessMinutes * 60);

        String subject = user.getPublicId().toString();

        Map<String, Object> claims = Map.of(
                "uid", user.getId(),
                "email", user.getEmail(),
                "nick", user.getNickname(),
                "status", user.getStatus()
        );

        return Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(claims)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshDays * 24 * 60 * 60);

        String subject = user.getPublicId().toString();

        return Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("type", "refresh")
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getSubject(String token) {
        return parse(token).getSubject();
    }

    public boolean isRefreshToken(String token) {
        Object type = parse(token).get("type");
        return "refresh".equals(type);
    }

    public TokenPair issue(User user) {
        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new TokenPair(accessToken, refreshToken);
    }
}
