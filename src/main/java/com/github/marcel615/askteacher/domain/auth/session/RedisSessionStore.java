package com.github.marcel615.askteacher.domain.auth.session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/** Atomic, single-instance Redis session state. Refresh token secrets are never persisted. */
@Repository
public class RedisSessionStore {
    private static final long IDLE_SECONDS = 7 * 24 * 60 * 60;
    private static final long MAX_SECONDS = 30 * 24 * 60 * 60;
    private static final int MAX_ROTATIONS = 10000;
    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate redis;
    private final String keyVersion;

    private static final DefaultRedisScript<Long> LOGIN = script("""
            redis.call('DEL', KEYS[1])
            redis.call('HSET', KEYS[1], 'sid', ARGV[1], 'current', ARGV[2], 'until', ARGV[3], 'count', '0')
            redis.call('EXPIRE', KEYS[1], ARGV[4])
            return 1
            """);
    private static final DefaultRedisScript<Long> ROTATE = script("""
            if redis.call('HGET', KEYS[1], 'sid') ~= ARGV[1] then return 0 end
            local current = redis.call('HGET', KEYS[1], 'current')
            if current ~= ARGV[2] then
              if redis.call('HEXISTS', KEYS[1], 'used:' .. ARGV[2]) == 1 then
                redis.call('DEL', KEYS[1]); return -1
              end
              return 0
            end
            local ttl = tonumber(redis.call('HGET', KEYS[1], 'until')) - tonumber(ARGV[4])
            local count = tonumber(redis.call('HGET', KEYS[1], 'count'))
            if ttl <= 0 or count >= tonumber(ARGV[6]) then redis.call('DEL', KEYS[1]); return 0 end
            redis.call('HSET', KEYS[1], 'used:' .. current, '1', 'current', ARGV[3], 'count', count + 1)
            redis.call('EXPIRE', KEYS[1], math.min(ttl, tonumber(ARGV[5])))
            return 1
            """);
    private static final DefaultRedisScript<Long> LOGOUT = script("""
            if redis.call('HGET', KEYS[1], 'sid') == ARGV[1] then
              if ARGV[2] == '' or redis.call('HGET', KEYS[1], 'current') == ARGV[2] then
                redis.call('DEL', KEYS[1]); return 1
              end
            end
            return 0
            """);

    public RedisSessionStore(StringRedisTemplate redis, @Value("${jwt.secret}") String jwtSecret) {
        this.redis = redis;
        this.keyVersion = hash(jwtSecret).substring(0, 16);
    }
    public record Token(long userId, String sessionId, String value) {
        @Override public String toString() { return "Token[userId=" + userId + ", sessionId=" + sessionId + ", value=REDACTED]"; }
    }
    public Token newToken(long userId, String sessionId) {
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String value = userId + "." + sessionId + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new Token(userId, sessionId, value);
    }
    public Token login(long userId) {
        Token token = newToken(userId, UUID.randomUUID().toString());
        long now = System.currentTimeMillis() / 1000;
        redis.execute(LOGIN, List.of(keyForUser(userId)), token.sessionId(), hash(token.value()), Long.toString(now + MAX_SECONDS), Long.toString(IDLE_SECONDS));
        return token;
    }
    public Token rotate(String raw) {
        Token old = parse(raw);
        if (old == null) return null;
        Token next = newToken(old.userId(), old.sessionId());
        Long result = redis.execute(ROTATE, List.of(keyForUser(old.userId())), old.sessionId(), hash(raw), hash(next.value()),
                Long.toString(System.currentTimeMillis() / 1000), Long.toString(IDLE_SECONDS), Integer.toString(MAX_ROTATIONS));
        return Long.valueOf(1).equals(result) ? next : null;
    }
    public boolean active(long userId, String sid) {
        return sid != null && sid.equals(redis.opsForHash().get(keyForUser(userId), "sid"));
    }
    public void ensureAvailable() { redis.hasKey("auth:availability"); }
    public void logoutAccess(long userId, String sid) {
        redis.execute(LOGOUT, List.of(keyForUser(userId)), sid, "");
    }
    public void logoutRefresh(String raw) {
        Token token = parse(raw);
        if (token != null) redis.execute(LOGOUT, List.of(keyForUser(token.userId())), token.sessionId(), hash(raw));
    }
    private static Token parse(String raw) {
        if (raw == null || raw.length() > 200) return null;
        String[] parts = raw.split("\\.", -1);
        if (parts.length != 3 || !parts[1].matches("[0-9a-f-]{36}") || !parts[2].matches("[A-Za-z0-9_-]{43}")) return null;
        try { long user = Long.parseLong(parts[0]); return user > 0 ? new Token(user, parts[1], raw) : null; }
        catch (NumberFormatException e) { return null; }
    }
    public String keyForUser(long userId) { return "auth:session:" + keyVersion + ":" + userId; }
    private static String hash(String raw) {
        try { return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static DefaultRedisScript<Long> script(String text) { return new DefaultRedisScript<>(text, Long.class); }
}
