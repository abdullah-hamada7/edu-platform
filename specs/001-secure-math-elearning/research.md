# Phase 0 Research - Secure Mathematics E-Learning Platform

## Decision 1: Authentication and Authorization Model

- Decision: Use stateless JWT authentication with strict role-based endpoint segregation
  (`/api/admin/**` and `/api/student/**`) and server-enforced authorization checks.
- Rationale: Matches constitution requirements for zero trust, no session state, and strict role
  boundaries.
- Alternatives considered:
  - Stateful server sessions: rejected due to constitution conflict and scaling friction.
  - Mixed role endpoints with in-method branching: rejected due to higher boundary risk.

## Decision 2: Device Fingerprint Enforcement

- Decision: Enforce max two registered device fingerprints per student in backend policy service
  before issuing playback access grants.
- Rationale: Satisfies anti-redistribution and device-limited access requirements while keeping
  policy auditable and testable.
- Alternatives considered:
  - Client-only fingerprint enforcement: rejected because it is bypassable.
  - IP-based limits: rejected due to shared networks and lower reliability.

## Decision 3: Video Protection Pipeline

- Decision: Store uploaded MP4 in S3, transcode to HLS (`.m3u8` + segments), apply AES-128
  encryption, and gate playback through signed URL issuance with a 2-hour TTL.
- Rationale: Provides layered protection with expiration control and aligns with required HLS
  delivery model.
- Alternatives considered:
  - Progressive MP4 delivery: rejected because direct links are easier to extract and redistribute.
  - Longer signed URL windows: rejected due to wider abuse window.

## Decision 4: Dynamic Watermark Strategy

- Decision: Overlay user-identifiable watermark content and rotate watermark position every 15-30
  seconds during active playback.
- Rationale: Improves leak attribution and raises effort for unauthorized screen recording reuse.
- Alternatives considered:
  - Static watermark: rejected due to easier cropping and masking.
  - No visible watermark: rejected due to weak forensic deterrence.

## Decision 5: Data Persistence and Integrity

- Decision: Use PostgreSQL + JPA (Hibernate) with Flyway-managed schema migrations and mandatory
  foreign key constraints for all core relationships.
- Rationale: Supports relational integrity for enrollments, attempts, and access grants while
  enabling controlled schema evolution.
- Alternatives considered:
  - NoSQL-first model: rejected due to relationship-heavy domain and consistency needs.
  - Manual SQL schema drift without migrations: rejected due to operational risk.

## Decision 6: Assessment and Rendering

- Decision: Support MCQ, true/false, and numeric input questions with LaTeX display via KaTeX in
  the minimal frontend phase.
- Rationale: Covers required assessment modes and math readability with low frontend complexity.
- Alternatives considered:
  - MathJax in phase 1: viable, but KaTeX selected for lighter runtime footprint.
  - Custom math renderer: rejected due to unnecessary complexity.

## Decision 7: Performance Verification Approach

- Decision: Validate p95 targets through repeatable load tests covering playback startup, concurrent
  student behavior (500 users), and synchronous quiz grading latency.
- Rationale: Converts constitution and spec constraints into objective release gates.
- Alternatives considered:
  - Ad-hoc manual testing: rejected as non-repeatable and non-auditable.
  - Average-only metrics: rejected because p95 better captures user-perceived reliability.

## Decision 8: Deployment Topology

- Decision: Deploy on Ubuntu 22.04 VPS using Docker Compose, with Nginx reverse proxy and HTTPS
  enforcement in front of backend/frontend containers.
- Rationale: Meets user-stated deployment constraints with operational simplicity for phase 1.
- Alternatives considered:
  - Kubernetes: rejected for unnecessary early operational overhead.
  - Direct app exposure without reverse proxy: rejected due to weaker TLS and routing control.
