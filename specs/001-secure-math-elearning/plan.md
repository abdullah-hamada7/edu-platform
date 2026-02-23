# Implementation Plan: Secure Mathematics E-Learning Platform

**Branch**: `001-secure-math-elearning` | **Date**: 2026-02-23 | **Spec**: `specs/001-secure-math-elearning/spec.md`
**Input**: Feature specification from `/specs/001-secure-math-elearning/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Build a secure e-learning platform where Admin users manage math courses, protected videos, and
quizzes, while Students can access only enrolled content, stream protected lessons, and complete
auto-graded assessments with LaTeX rendering.

Technical approach uses a Spring Boot 3 service with stateless JWT auth, strict role-segregated
APIs, PostgreSQL with Flyway migrations, and an S3-backed encrypted HLS video pipeline with signed
URLs and dynamic watermarking. A minimal React frontend provides secure playback and quiz UX.

## Technical Context

**Language/Version**: Java 21 (backend), TypeScript + React (frontend)  
**Primary Dependencies**: Spring Boot, Spring Security, Spring Data JPA (Hibernate), Flyway,
AWS S3 SDK, FFmpeg/transcoding worker, React, HLS.js, KaTeX  
**Storage**: PostgreSQL (relational data), AWS S3 (raw/transcoded video assets and keys)  
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers, React Testing Library  
**Target Platform**: VPS Ubuntu 22.04 with Docker Compose and Nginx TLS termination  
**Project Type**: Web application (backend API + frontend SPA)  
**Performance Goals**: <2s video startup, <300ms synchronous quiz grading p95, 500 concurrent students  
**Constraints**: Stateless JWT auth only, max 2 devices per student, signed HLS URLs (2-hour TTL),
dynamic watermark position refresh every 15-30 seconds, HTTPS enforced  
**Scale/Scope**: Initial release for secure lesson delivery, enrollment control, and quiz analytics

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Security-First Architecture**: Confirm default-authenticated API design, explicit public endpoint
  exceptions, and strict `Admin` vs `Student` RBAC mapping.
  - PASS: All API routes require JWT by default; only auth bootstrap endpoints are explicit public
    exceptions. Admin and Student APIs are segregated by route namespace and role policy.
- **Content Protection Enforcement**: Confirm encrypted HLS (AES-128), signed URL expiry strategy,
  server-side device fingerprint validation, and dynamic watermarking approach where media is in scope.
  - PASS: MP4 is transcoded to AES-128 HLS in S3, playback uses signed URLs with 2-hour expiration,
    device validation is server-side, and watermark position rotates every 15-30 seconds.
- **Data Integrity and Consistency**: Confirm PostgreSQL foreign key strategy, hard-delete default
  policy (unless explicitly exempted), and BCrypt password hashing requirements.
  - PASS: PostgreSQL schema uses foreign keys for all ownership/enrollment/attempt relations,
    hard delete is default policy, and BCrypt password hashing is retained.
- **Stateless Backend Design**: Confirm JWT-only auth approach and no server-side session dependency
  for identity/authz decisions.
  - PASS: JWT access token flow only; no server-managed session state in auth or media access.
- **Performance Targets**: Confirm design and verification plan for video start time <2s, support for
  500 concurrent students, and synchronous quiz grading <300ms per submission.
  - PASS: Targets are explicit in contracts and quickstart validation plan with load/perf checks.
- **Code Quality Standards**: Confirm layered architecture (`Controller -> Service -> Repository`),
  explicit DTO mapping, service-layer unit tests, and integration tests for security boundaries.
  - PASS: Architecture and testing strategy enforce controller/service/repository layering,
    DTO boundaries, service unit tests, and security integration tests.

Gate Result (Pre-Design): PASS

## Project Structure

### Documentation (this feature)

```text
specs/001-secure-math-elearning/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md
```

### Source Code (repository root)
```text
backend/
├── src/main/java/com/securemath/
│   ├── api/
│   │   ├── admin/
│   │   └── student/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   ├── dto/
│   ├── security/
│   └── video/
├── src/main/resources/
│   ├── db/migration/
│   └── application.yml
└── src/test/java/com/securemath/
    ├── unit/
    └── integration/

frontend/
├── src/
│   ├── pages/
│   ├── components/
│   ├── features/
│   │   ├── player/
│   │   └── quiz/
│   └── services/
└── tests/

infra/
├── docker-compose.yml
└── nginx/
    └── default.conf
```

**Structure Decision**: Web application split into `backend/` and `frontend/` with separate test
trees and shared deployment assets in `infra/`. This directly supports strict route separation,
secure media flow orchestration, and minimal Phase 1 frontend delivery.

## Complexity Tracking

No constitution violations identified; no complexity exemptions required.

## Phase 0: Research Output

Research findings are documented in `specs/001-secure-math-elearning/research.md` and resolve
delivery decisions for JWT posture, secure HLS pipeline, device validation, watermark cadence,
schema evolution, and VPS deployment operations.

## Phase 1: Design Output

- Data model: `specs/001-secure-math-elearning/data-model.md`
- API contracts: `specs/001-secure-math-elearning/contracts/openapi.yaml`
- Validation and run workflow: `specs/001-secure-math-elearning/quickstart.md`

Gate Result (Post-Design): PASS
