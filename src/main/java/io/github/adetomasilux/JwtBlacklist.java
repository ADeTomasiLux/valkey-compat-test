package io.github.adetomasilux;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("JwtBlacklist")
public class JwtBlacklist {
    @Id
    private String jwt;

    private Instant expiration;

    public JwtBlacklist(String jwt, Instant expiration) {
        this.jwt = jwt;
        this.expiration = expiration;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    @TimeToLive
    public long getTimeToLive() {
        long difference = expiration.getEpochSecond() - Instant.now().getEpochSecond();
        return difference > 0 ? difference : 1;
    }
}