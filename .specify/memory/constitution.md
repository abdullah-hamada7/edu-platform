<!--
Sync Impact Report
- Version change: 1.0.0 -> 1.1.0
- Modified principles:
  - I. Security-First Architecture -> I. Security-First Architecture (unchanged)
  - II. Content Protection Enforcement -> II. Content Protection Enforcement (unchanged)
  - III. Data Integrity and Consistency -> III. Data Integrity and Consistency (unchanged)
  - IV. Stateless Backend Design -> IV. Stateless Backend Design (unchanged)
  - Added: V. Performance Targets
  - Added: VI. Code Quality Standards
- Added sections:
  - None
- Removed sections:
  - None
- Templates requiring updates:
  - .specify/templates/plan-template.md: updated
  - .specify/templates/spec-template.md: updated
  - .specify/templates/tasks-template.md: updated
  - .specify/templates/commands/*.md: pending (directory not present)
  - README.md: pending (file not present)
  - docs/quickstart.md: pending (file not present)
- Deferred TODOs:
  - TODO(RATIFICATION_DATE): Original ratification date is unknown and must be set by project owners.
-->

# Secure Math Platform Constitution

## Core Principles

### I. Security-First Architecture
All APIs MUST require authentication by default. Endpoints are public only when explicitly
declared and justified in specification and review artifacts. Authorization MUST be enforced with
strict RBAC separation between `Admin` and `Student` roles, and backend services MUST treat all
client assertions as untrusted. Every security-relevant action MUST be validated server-side,
including role checks, entitlement checks, and resource access controls.

Rationale: Client-side controls are bypassable by design. Security guarantees are valid only when
enforced by trusted backend boundaries.

### II. Content Protection Enforcement
Video delivery MUST use encrypted HLS with AES-128. Access to media assets MUST use signed URLs
with explicit expiration timestamps and signature verification. Device fingerprint validation MUST
be enforced server-side before playback is authorized. Dynamic watermarking MUST be applied to all
protected video sessions in a user-identifiable way.

Rationale: Layered content protection reduces unauthorized redistribution and enables forensic
traceability for leaked assets.

### III. Data Integrity and Consistency
Persistent relational data MUST use PostgreSQL with enforced foreign key constraints for every
referential relationship. Soft deletes MUST NOT be used unless an explicit requirement documents
retention, recovery, and query behavior impacts. Passwords MUST be hashed using BCrypt with a
work factor configured for current production security standards.

Rationale: Hard integrity constraints and robust password hashing reduce silent data corruption and
credential compromise risk.

### IV. Stateless Backend Design
Authentication for platform APIs MUST use JWT-based mechanisms only. Backend services MUST remain
stateless with respect to session identity, and authorization decisions MUST be derived from
validated tokens plus server-side policy and data checks.

Rationale: Stateless identity handling improves horizontal scalability and predictable operational
behavior across distributed services.

### V. Performance Targets
Protected video playback MUST start in under 2 seconds under expected production network and load
conditions. Platform services MUST support at least 500 concurrent student sessions without
violating core API reliability and security guarantees. Quiz grading operations MUST be synchronous
and complete in under 300ms per submission at the service boundary.

Rationale: The learning experience depends on low-latency content access and immediate assessment
feedback under realistic classroom concurrency.

### VI. Code Quality Standards
Backend implementation MUST follow a layered architecture of `Controller -> Service -> Repository`.
DTO mapping MUST be explicit across layer boundaries to prevent domain leakage and implicit
coupling. Service-layer business logic MUST be covered by unit tests. Security boundaries,
especially authentication, authorization, and media access controls, MUST be covered by
integration tests.

Rationale: Layered boundaries and test discipline reduce regression risk in high-risk security and
content-delivery paths.

## Security Control Baselines

- All feature specs MUST document endpoint authentication type (`required` or explicitly `public`).
- All protected routes MUST map to explicit RBAC policy rules for `Admin` and `Student` roles.
- Media services MUST reject playback when encryption, signature validation, fingerprint checks, or
  watermark injection preconditions fail.
- Database schema migrations MUST preserve referential integrity and fail when introducing invalid
  foreign key states.
- Password handling code MUST prohibit plaintext persistence and MUST only compare BCrypt hashes.
- Performance verification MUST include objective measurements for video start latency, concurrent
  student load, and quiz grading latency.
- Code structure validation MUST enforce `Controller -> Service -> Repository` layering and explicit
  DTO mapping at boundary transitions.

## Delivery and Compliance Workflow

- Implementation plans MUST include a Constitution Check section covering all six principles.
- Pull requests MUST include evidence of server-side authz enforcement, media protection behavior,
  integrity-aware migration/testing updates, and performance/code-quality compliance where
  applicable.
- Test strategy MUST include integration tests for role boundaries and content access denial paths,
  migration validation tests for foreign key integrity, and service-layer unit tests.
- Release readiness review MUST verify no stateful server session dependencies were introduced in
  JWT-authenticated backend flows, and MUST verify key latency and concurrency targets.

## Governance

This constitution is the highest-priority engineering policy for Secure Math Platform.

- Amendment process: Changes MUST be proposed through a documented update to this file, including
  rationale, impact analysis, and template sync updates.
- Approval policy: At least one product owner and one engineering owner MUST approve amendments.
- Versioning policy: Semantic versioning applies to this constitution.
  - MAJOR: Backward-incompatible governance changes or principle removals/redefinitions.
  - MINOR: New principle/section or materially expanded requirements.
  - PATCH: Clarifications and non-semantic wording improvements.
- Compliance review: Every implementation plan and pull request MUST pass constitution checks
  before merge.

**Version**: 1.1.0 | **Ratified**: TODO(RATIFICATION_DATE): Original adoption date unknown. | **Last Amended**: 2026-02-23
