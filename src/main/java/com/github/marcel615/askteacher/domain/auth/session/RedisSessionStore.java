package com.github.marcel615.askteacher.domain.auth.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/** Atomic, single-instance Redis session state. Refresh token secrets are never persisted. */
@Repository
public class RedisSessionStore {
    private static final long REFRESH_TOKEN_LIFETIME_SECONDS = Duration.ofDays(7).toSeconds();
    private static final long SESSION_LIFETIME_SECONDS = Duration.ofDays(30).toSeconds();
    private static final int MAX_ROTATIONS = 10000;
    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate redisTemplate;
    private final String keyVersion;

    private static final DefaultRedisScript<Long> LOGIN = script("""
            redis.call('DEL', KEYS[1])
            redis.call('HSET', KEYS[1], 'sid', ARGV[1], 'current', ARGV[2], 'until', ARGV[3], 'count', '0')
            redis.call('EXPIRE', KEYS[1], ARGV[4])
            return 1
            """);
    private static final DefaultRedisScript<Long> ROTATE = script("""
            local sessionId = ARGV[1]
            local submittedHash = ARGV[2]
            local nextHash = ARGV[3]
            local now = ARGV[4]
            local idleSeconds = ARGV[5]
            local maxRotations = ARGV[6]

            if redis.call('HGET', KEYS[1], 'sid') ~= sessionId then
              return 0
            end

            local current = redis.call('HGET', KEYS[1], 'current')
            if current ~= submittedHash then
              if redis.call('HEXISTS', KEYS[1], 'used:' .. submittedHash) == 1 then
                redis.call('DEL', KEYS[1])
                return -1
              end
              return 0
            end

            local ttl = tonumber(redis.call('HGET', KEYS[1], 'until')) - tonumber(now)
            local count = tonumber(redis.call('HGET', KEYS[1], 'count'))
            if ttl <= 0 or count >= tonumber(maxRotations) then
              redis.call('DEL', KEYS[1])
              return 0
            end

            redis.call('HSET', KEYS[1], 'used:' .. current, '1', 'current', nextHash, 'count', count + 1)
            redis.call('EXPIRE', KEYS[1], math.min(ttl, tonumber(idleSeconds)))
            return 1
            """);
    private static final DefaultRedisScript<Long> LOGOUT = script("""
            if redis.call('HGET', KEYS[1], 'sid') == ARGV[1] then
              if ARGV[2] == '' or redis.call('HGET', KEYS[1], 'current') == ARGV[2] then
                redis.call('DEL', KEYS[1])
                return 1
              end
            end
            return 0
            """);

    public RedisSessionStore(
            StringRedisTemplate redisTemplate,
            @Value("${jwt.secret}") String jwtSecret
    ) {
        this.redisTemplate = redisTemplate;
        this.keyVersion = hash(jwtSecret).substring(0, 16);
    }

    public RefreshToken newToken(long userId, String sessionId) {
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        String encodedRandomValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String tokenValue = userId + "." + sessionId + "." + encodedRandomValue;
        return new RefreshToken(userId, sessionId, tokenValue);
    }

    public RefreshToken login(long userId) {
        RefreshToken token = newToken(userId, UUID.randomUUID().toString());
        long nowSeconds = System.currentTimeMillis() / 1000;
        redisTemplate.execute(
                LOGIN,
                List.of(keyForUser(userId)),
                token.sessionId(),
                hash(token.value()),
                Long.toString(nowSeconds + SESSION_LIFETIME_SECONDS),
                Long.toString(REFRESH_TOKEN_LIFETIME_SECONDS)
        );
        return token;
    }

    public RefreshToken rotate(String refreshTokenValue) {
        RefreshToken submittedToken = parse(refreshTokenValue);
        if (submittedToken == null) {
            return null;
        }
        RefreshToken nextToken = newToken(submittedToken.userId(), submittedToken.sessionId());
        Long result = redisTemplate.execute(
                ROTATE,
                List.of(keyForUser(submittedToken.userId())),
                submittedToken.sessionId(),
                hash(refreshTokenValue),
                hash(nextToken.value()),
                Long.toString(System.currentTimeMillis() / 1000),
                Long.toString(REFRESH_TOKEN_LIFETIME_SECONDS),
                Integer.toString(MAX_ROTATIONS)
        );
        return Long.valueOf(1).equals(result) ? nextToken : null;
    }

    public boolean active(long userId, String sessionId) {
        return sessionId != null && sessionId.equals(redisTemplate.opsForHash().get(keyForUser(userId), "sid"));
    }

    public void ensureAvailable() {
        redisTemplate.hasKey("auth:availability");
    }

    public void logoutAccess(long userId, String sessionId) {
        redisTemplate.execute(
                LOGOUT,
                List.of(keyForUser(userId)),
                sessionId,
                ""
        );
    }

    public void logoutRefresh(String refreshTokenValue) {
        RefreshToken token = parse(refreshTokenValue);
        if (token != null) {
            redisTemplate.execute(
                    LOGOUT,
                    List.of(keyForUser(token.userId())),
                    token.sessionId(),
                    hash(refreshTokenValue)
            );
        }
    }

    private static RefreshToken parse(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.length() > 200) {
            return null;
        }
        String[] parts = refreshTokenValue.split("\\.", -1);
        if (parts.length != 3
                || !parts[1].matches("[0-9a-f-]{36}")
                || !parts[2].matches("[A-Za-z0-9_-]{43}")) {
            return null;
        }
        try {
            long userId = Long.parseLong(parts[0]);
            if (userId <= 0) {
                return null;
            }
            return new RefreshToken(userId, parts[1], refreshTokenValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String keyForUser(long userId) {
        return "auth:session:" + keyVersion + ":" + userId;
    }

    private static String hash(String sourceValue) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(sourceValue.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static DefaultRedisScript<Long> script(String text) {
        return new DefaultRedisScript<>(text, Long.class);
    }
}
