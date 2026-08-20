package io.github.adetomasilux;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public final class ValkeyCompatibilityTest {
    private ValkeyCompatibilityTest() {}

    public static void main(String[] args) {
        boolean keepAlive = Boolean.parseBoolean(System.getenv().getOrDefault("KEEP_ALIVE", "false"));
        String jwt = "compat-test-" + UUID.randomUUID();

        int exitCode = 0;
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(RedisConfiguration.class)) {
            LettuceConnectionFactory factory = context.getBean(LettuceConnectionFactory.class);
            JwtBlacklistRepository repository = context.getBean(JwtBlacklistRepository.class);

            RedisConnection connection = factory.getConnection();
            try {
                System.out.println("TEST: PING");
                require("PONG", connection.ping(), "PING");
            } finally {
                connection.close();
            }

            Instant expiration = Instant.now().plusSeconds(3);
            System.out.println("TEST: REPOSITORY SAVE");
            repository.save(new JwtBlacklist(jwt, expiration));

            System.out.println("TEST: REPOSITORY FIND AND DESERIALIZE");
            JwtBlacklist stored = repository.findById(jwt)
                .orElseThrow(() -> new IllegalStateException("saved JwtBlacklist was not found"));
            require(jwt, stored.getJwt(), "repository JWT round trip");
            require(expiration, stored.getExpiration(), "repository Instant round trip");

            System.out.println("TEST: TTL EXPIRY");
            Thread.sleep(4000);
            require(false, repository.findById(jwt).isPresent(), "repository TTL expiry");

            System.out.println("TEST: REPOSITORY DELETE");
            repository.save(new JwtBlacklist(jwt, Instant.now().plusSeconds(60)));
            repository.deleteById(jwt);
            require(false, repository.findById(jwt).isPresent(), "repository delete");

            System.out.println("COMPATIBLE: gateway-equivalent PING, repository mapping, TTL, and delete succeeded");
        } catch (Exception exception) {
            System.err.println("INCOMPATIBLE: " + rootCause(exception).getClass().getSimpleName()
                + ": " + rootCause(exception).getMessage());
            exception.printStackTrace(System.err);
            exitCode = 1;
        }

        if (keepAlive) {
            System.out.println("KEEP_ALIVE: waiting for ECS inspection");
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        System.exit(exitCode);
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
