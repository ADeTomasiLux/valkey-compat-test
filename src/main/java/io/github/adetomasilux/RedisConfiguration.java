package io.github.adetomasilux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        String host = required("SPRING_REDIS_HOST");
        String password = System.getenv("SPRING_REDIS_PASSWORD");
        String clusterNodes = System.getenv("SPRING_REDIS_CLUSTER_NODES");
        List<String> nodes = clusterNodes == null || clusterNodes.isBlank()
            ? Collections.emptyList()
            : Arrays.asList(clusterNodes.split(","));

        Object config = nodes.isEmpty()
            ? new RedisStandaloneConfiguration(host)
            : new RedisClusterConfiguration(nodes);
        if (config instanceof RedisStandaloneConfiguration) {
            if (password != null && !password.isEmpty()) {
                ((RedisStandaloneConfiguration) config).setPassword(password);
            }
            return new LettuceConnectionFactory(
                (RedisStandaloneConfiguration) config, clientConfiguration());
        }
        if (password != null && !password.isEmpty()) {
            ((RedisClusterConfiguration) config).setPassword(password);
        }
        return new LettuceConnectionFactory((RedisClusterConfiguration) config, clientConfiguration());
    }

    private LettuceClientConfiguration clientConfiguration() {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
            LettuceClientConfiguration.builder();
        if (Boolean.parseBoolean(required("SPRING_REDIS_SSL"))) {
            builder.useSsl().startTls();
        }
        return builder.build();
    }

    @Bean
    public RedisTemplate<String, JwtBlacklist> redisTemplate() {
        RedisTemplate<String, JwtBlacklist> template = new RedisTemplate<>();
        template.setEnableTransactionSupport(false);
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}