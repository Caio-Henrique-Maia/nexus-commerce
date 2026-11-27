# ADR 0001: Start with a modular monolith before microservices

- Status: Accepted
- Date: 2026-11-27

## Context

Nexus Commerce is a learning and portfolio project. The goal is to understand the engineering trade-offs behind distributed systems, not only to deploy multiple Spring Boot applications.

Starting with separate services would introduce network communication, distributed data consistency, deployment coordination, and failure handling before the core commerce flow is stable.

## Decision

Keep the application as one deployable Spring Boot application for now, while establishing clear business boundaries around:

- Catalog: products and stock.
- Orders: order lifecycle and order items.
- Payments: payment creation, gateway processing, attempts, and idempotency.

Each area should own its business rules and expose use-case-oriented interfaces. Avoid making one area depend on another area's repositories or persistence details.

The first extraction candidate will be Payments because it already has a distinct integration boundary with an external gateway and important reliability concerns.

## Learning path

1. Stabilize the order and payment lifecycle inside the monolith.
2. Strengthen boundaries between catalog, orders, and payments.
3. Define explicit contracts for communication between those areas.
4. Extract Payments into a separate Spring Boot application with its own database.
5. Replace in-process calls with service-to-service communication.
6. Study distributed consistency, idempotent message handling, the outbox pattern, sagas, retries, observability, and operational failure modes as the extracted service requires them.

## Consequences

- The project remains simpler to run and debug while the domain is evolving.
- Business boundaries can be tested before adding network and infrastructure complexity.
- Extracting Payments later will require explicit API/event contracts and a decision about ownership of payment data; sharing the monolith's database is not the target architecture.
- The extraction is a future learning milestone, not part of the current implementation.
