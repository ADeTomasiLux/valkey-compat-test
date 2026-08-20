# Valkey compatibility decision

## Goal

Select the simplest cache service compatible with the immutable `vto-video-profile-gateway`. The gateway cannot currently start without Elasticsearch, so `valkey-compat-test` reproduces only its cache-relevant runtime and behavior.

## Acceptance criteria

The candidate must work with Java 11, Spring Data Redis 2.2.5, Lettuce 5.2.2, the gateway's cluster/standalone selection, password handling, TLS with STARTTLS, Spring Data repository serialization, TTL, and delete behavior.

## Candidate order

1. Valkey provisioned or Valkey Serverless
2. Redis OSS on ElastiCache
3. Redis-compatible service on ECS

## Registry

| Candidate | Status | Evidence | Decision |
| --- | --- | --- | --- |
| Valkey provisioned | Testing | Already deployed in `vto-backend-dev`; gateway-equivalent probe pending | Pending |
| Valkey Serverless | Not tested | None | Pending |
| Redis OSS | Not tested | None | Pending |
| Redis-compatible ECS | Not tested | Fallback only | Pending |

## Current hypothesis

The previous raw-command probe was insufficient because it bypassed Spring Data repository mapping and allowed direct TLS without STARTTLS. Provisioned Valkey is compatible only if the gateway-equivalent probe passes without changing the immutable gateway configuration.