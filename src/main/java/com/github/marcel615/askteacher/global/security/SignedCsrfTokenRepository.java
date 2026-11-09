package com.github.marcel615.askteacher.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Stateless signed double-submit token; the cookie cannot be replaced with an attacker-chosen value. */
public class SignedCsrfTokenRepository implements CsrfTokenRepository {
    private final CookieCsrfTokenRepository cookies = new CookieCsrfTokenRepository();
    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public SignedCsrfTokenRepository(String secret, boolean secure, String sameSite) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("CSRF secret must contain at least 32 bytes");
        }
        key = secret.getBytes(StandardCharsets.UTF_8);
        if (secure) {
            cookies.setCookieName("__Host-XSRF-TOKEN");
        }
        cookies.setCookieCustomizer(cookie -> cookie
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/"));
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        byte[] nonce = new byte[32];
        random.nextBytes(nonce);
        String nonceValue = Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
        String signature = sign(nonceValue, request);
        String signedToken = nonceValue + "." + signature;
        return new DefaultCsrfToken(
                "X-XSRF-TOKEN",
                "_csrf",
                signedToken
        );
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        cookies.saveToken(token, request, response);
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        CsrfToken token = cookies.loadToken(request);
        if (token == null) {
            return null;
        }
        String[] parts = token.getToken().split("\\.", -1);
        if (parts.length != 2
                || !parts[0].matches("[A-Za-z0-9_-]{43}")
                || !parts[1].matches("[A-Za-z0-9_-]{43}")) {
            return null;
        }
        byte[] expectedSignature = sign(parts[0], request).getBytes(StandardCharsets.US_ASCII);
        byte[] submittedSignature = parts[1].getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(expectedSignature, submittedSignature)) {
            return null;
        }
        return token;
    }

    private String sign(String value, HttpServletRequest request) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            String refreshTokenValue = refreshCookieValue(request);
            byte[] refreshTokenBytes = refreshTokenValue.getBytes(StandardCharsets.UTF_8);
            byte[] refreshTokenHash = MessageDigest.getInstance("SHA-256").digest(refreshTokenBytes);

            mac.update(value.getBytes(StandardCharsets.US_ASCII));
            byte[] signatureBytes = mac.doFinal(refreshTokenHash);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String refreshCookieValue(HttpServletRequest request) {
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies == null) {
            return "";
        }
        for (Cookie cookie : requestCookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }
}
