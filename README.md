# Valkey compatibility test

A one-shot container that reproduces the cache path used by `vto-video-profile-gateway`: Java 11, Spring Data Redis 2.2.5, Lettuce 5.2.2, its cluster/standalone selection, password authentication, TLS with STARTTLS, and Spring Data repository mapping.

## Inputs

- `SPRING_REDIS_HOST`: standalone host, matching the gateway setting; port `6379` is inherited from Spring Data
- `SPRING_REDIS_PASSWORD`: password supplied at runtime
- `SPRING_REDIS_SSL`: set to `true` to reproduce the deployed gateway's TLS with STARTTLS
- `SPRING_REDIS_START_TLS`: defaults to `true`; set to `false` only for the direct-TLS diagnostic control
- `SPRING_REDIS_CLUSTER_NODES`: optional comma-separated `host:port` list; when absent, the gateway standalone branch is used
- `KEEP_ALIVE`: defaults to `false`; set to `true` when running as an ECS service

The process runs `PING`, then saves, reads, deserializes, checks TTL, and deletes a disposable `JwtBlacklist` through the same Spring Data repository model as the gateway. It exits `0` on success and `1` on failure. It never prints credentials.

## Build

```bash
docker build -t valkey-compat-test .
```

## Image

GitHub Actions publishes `linux/amd64` images to:

```text
ghcr.io/adetomasilux/valkey-compat-test
```

Set the package visibility to public in GitHub after the first publish.
