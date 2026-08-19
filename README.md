# Valkey compatibility test

A one-shot container that tests a legacy Redis client using Spring Data Redis 2.2.5, Lettuce 5.2.2, Redis cluster configuration, and password authentication.

## Inputs

- `REDIS_NODE`: `host:port`
- `REDIS_PASSWORD`: password supplied at runtime
- `REDIS_SSL`: defaults to `true`
- `REDIS_START_TLS`: defaults to `true` to reproduce the gateway; set to `false` for ElastiCache direct TLS

The process runs `PING`, `SET`, `GET`, `EXPIRE`, and `DEL` against a disposable key. It exits `0` on success and `1` on failure. It never prints credentials.

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
