package io.github.adetomasilux;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public final class ValkeyCompatibilityTest {
    private ValkeyCompatibilityTest() {}

    public static void main(String[] args) {
        String node = required("REDIS_NODE");
        String password = required("REDIS_PASSWORD");
        boolean ssl = Boolean.parseBoolean(System.getenv().getOrDefault("REDIS_SSL", "true"));
        String key = "compat-test:" + UUID.randomUUID();

        RedisClusterConfiguration redis = new RedisClusterConfiguration(Collections.singletonList(node));
        redis.setPassword(password);

        LettuceClientConfiguration.LettuceClientConfigurationBuilder client =
            LettuceClientConfiguration.builder().commandTimeout(Duration.ofSeconds(10));
        if (ssl) {
            client.useSsl().startTls();
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redis, client.build());
        factory.afterPropertiesSet();

        try {
            byte[] encodedKey = key.getBytes(StandardCharsets.UTF_8);
            byte[] value = "ok".getBytes(StandardCharsets.UTF_8);
            try (var connection = factory.getConnection()) {
                require("PONG", connection.ping(), "PING");
                require(true, connection.stringCommands().set(encodedKey, value), "SET");
                require("ok", new String(connection.stringCommands().get(encodedKey), StandardCharsets.UTF_8), "GET");
                require(true, connection.keyCommands().expire(encodedKey, 60), "EXPIRE");
                require(1L, connection.keyCommands().del(encodedKey), "DEL");
            }
            System.out.println("COMPATIBLE: PING, SET, GET, EXPIRE, and DEL succeeded");
        } catch (Exception exception) {
            System.err.println("INCOMPATIBLE: " + rootCause(exception).getClass().getSimpleName()
                + ": " + rootCause(exception).getMessage());
            System.exit(1);
        } finally {
            factory.destroy();
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    private static void require(Object expected, Object actual, String operation) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException(operation + " returned " + actual + ", expected " + expected);
        }
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
