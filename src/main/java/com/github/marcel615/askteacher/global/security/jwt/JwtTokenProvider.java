package com.github.marcel615.askteacher.global.security.jwt;

import com.github.marcel615.askteacher.domain.user.type.UserRole;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long accessTokenExpirationMillis;
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32 || secret.codePoints().distinct().count() < 16
                || secret.matches("(?s)(.+)\\1+") || hasLongSequence(secret)) {
            throw new IllegalStateException("JWT secret is missing or too weak");
        }
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        accessTokenExpirationMillis = expiration;
    }
    public String createAccessToken(Long userId, UserRole role, String sessionId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .claim("sid", sessionId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpirationMillis))
                .signWith(secretKey)
                .compact();
    }
    public boolean validateToken(String token) {
        return verifyToken(token).isPresent();
    }
    public record Verified(
            Authentication authentication,
            String sessionId
    ) { }
    public Optional<Verified> verifyToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Long userId = Long.valueOf(claims.getSubject());
            String role = UserRole.valueOf(claims.get("role", String.class)).name();
            String sid = claims.get("sid", String.class);
            if (sid == null || sid.isBlank()) {
                return Optional.empty();
            }
            AskTeacherUserDetails principal = new AskTeacherUserDetails(userId, "", "", role);
            Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            return Optional.of(new Verified(auth, sid));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
    private static boolean hasLongSequence(String value) {
        int ascending = 1;
        int descending = 1;
        for (int i = 1; i < value.length(); i++) {
            int difference = value.charAt(i) - value.charAt(i - 1);
            ascending = difference == 1 ? ascending + 1 : 1;
            descending = difference == -1 ? descending + 1 : 1;
            if (ascending >= 12 || descending >= 12) {
                return true;
            }
        }
        return false;
    }
}
