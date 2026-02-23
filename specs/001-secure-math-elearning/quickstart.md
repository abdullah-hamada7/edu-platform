# Quickstart - Secure Mathematics E-Learning Platform

## Prerequisites

- Docker and Docker Compose installed
- Java 21 and Node.js LTS available for local development
- AWS S3 bucket and credentials for media storage/transcode output
- TLS certificate material for HTTPS (or local self-signed certs for development)

## 1) Configure environment

Create environment files for backend, frontend, and infrastructure:

- Backend config includes:
  - PostgreSQL connection
  - JWT secret and token expiry
  - `APP_INITIAL_ADMIN_PASSWORD` (required for first startup)
  - S3 bucket names and credentials
  - Signed URL expiry (2 hours)
  - Device limit (2)
- Frontend config includes API base URL and player settings.

## 2) Start platform locally

```bash
docker compose up -d
```

Verify services:

- Nginx reverse proxy is reachable over HTTPS
- Backend health endpoint responds
- Frontend is reachable through proxy

## 3) Run schema migrations

Flyway migrations run on backend startup. Confirm migration table is populated and foreign keys are
present for enrollments, lessons, attempts, answers, and playback grants.

## 4) Seed minimum data

- Set `APP_INITIAL_ADMIN_PASSWORD` environment variable before first backend startup
- Backend auto-seeds initial Admin if no Admin exists
- Initial Admin must change password on first login
- Create one Student account (via Admin enrollment flow)
- Create one course/chapter/lesson
- Upload one sample MP4 and ensure transcode pipeline produces encrypted HLS assets
- Create one quiz with MCQ, true/false, and numeric items including LaTeX content
- Enroll the Student in the course

## 5) Validate core flows

- Admin login succeeds and can perform course/lesson/quiz CRUD.
- Student login succeeds and only enrolled courses are listed.
- Student receives signed playback grant and can stream encrypted HLS.
- Third distinct device attempt is denied after two device registrations.
- Watermark position changes at 15-30 second intervals during playback.
- Quiz submission returns synchronous score and persists attempt records.

## 6) Run Tests

### Unit Tests (via Docker)

```bash
cd secure-math-platform/backend

# Run all unit tests
docker run --rm -v "$(pwd):/app" -w /app maven:3.9.6-eclipse-temurin-21 mvn test -Dtest="QuizGradingServiceTest,EnrollmentPlaybackPolicyTest"

# On Windows (PowerShell):
docker run --rm -v "${PWD}:/app" -w /app maven:3.9.6-eclipse-temurin-21 mvn test -Dtest="QuizGradingServiceTest,EnrollmentPlaybackPolicyTest"
```

### Integration Tests (requires Docker Desktop running)

```bash
# Run integration tests with Testcontainers
docker run --rm -v "$(pwd):/app" -w /app --network host maven:3.9.6-eclipse-temurin-21 mvn verify -Dspring.profiles.active=test

# Or run all tests:
docker run --rm -v "$(pwd):/app" -w /app maven:3.9.6-eclipse-temurin-21 mvn test
```

### Quick Test Commands Reference

```bash
# Unit tests only (fast, no database needed)
mvn test -Dtest="QuizGradingServiceTest,EnrollmentPlaybackPolicyTest"

# Integration tests (requires Testcontainers/Docker)
mvn test -Dtest="*IT" -Dspring.profiles.active=test

# Run specific test class
mvn test -Dtest="PlaybackGrantAuthorizationIT"

# Run with coverage report
mvn test jacoco:report
```

### Test Categories

| Test File | Coverage |
|-----------|----------|
| `AdminCourseAuthorizationIT.java` | Admin course CRUD authorization |
| `EnrollmentScopeIT.java` | Enrollment scope validation |
| `QuizSubmissionIT.java` | Quiz submission flow |
| `QuizGradingServiceTest.java` | Grading logic (MCQ, TRUE_FALSE, NUMERIC) |
| `EnrollmentPlaybackPolicyTest.java` | Enrollment-based playback policy |
| `RoleBoundaryIT.java` | Role-based access control |
| `PlaybackGrantAuthorizationIT.java` | Playback grant authorization |
| `DeviceLimitIT.java` | 2-device limit enforcement |
| `SignedUrlExpiryIT.java` | Signed URL expiry behavior |
| `SignedUrlReplayIT.java` | Signed URL replay protection |
| `PerformanceIT.java` | Latency and concurrent load tests |

### Test Performance Thresholds

| Metric | Target |
|--------|--------|
| Quiz grading P95 latency | < 300ms |
| Playback grant latency | < 500ms average |
| Course listing latency | < 1000ms |
| Concurrent success rate | > 90% |

## 7) Validate constitution gates

- Security boundary integration tests pass for role separation and unauthorized access denial.
- Service-layer unit tests pass for grading, enrollment authorization, and playback grant policy.
- Performance checks confirm:
  - HLS manifest response < 200ms (P95)
  - Video startup under 2 seconds (P95)
  - Synchronous grading under 300ms (P95)
  - 500 concurrent student load scenario without policy regression

### Performance Validation Commands

**Load Test (k6):**
```bash
k6 run --vus 500 --duration 10m --ramp-up 2m scripts/load-test.js
```

**Load Test Parameters:**
- Tool: k6 or JMeter
- Duration: 10 minutes sustained load
- Ramp-up: 0 → 500 users in 2 minutes
- Traffic mix: 80% video playback requests, 20% quiz submission requests
- Failure thresholds:
  - Error rate < 1%
  - No memory leak > 5% heap growth over 10 minutes

## 8) Deployment baseline (VPS)

- Provision Ubuntu 22.04 VPS
- Deploy container stack with Docker Compose
- Configure Nginx reverse proxy and enforce HTTPS
- Restrict direct backend access to internal network only
- Monitor application logs and latency metrics for playback and grading

## API Endpoints Summary

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/change-password` - Password change

### Admin Endpoints (requires ADMIN role)
- `GET /api/admin/courses` - List all courses
- `POST /api/admin/courses` - Create course
- `GET /api/admin/courses/{id}` - Get course details
- `PUT /api/admin/courses/{id}` - Update course
- `DELETE /api/admin/courses/{id}` - Delete course
- `POST /api/admin/courses/{id}/enrollments` - Enroll student
- `GET /api/admin/users` - List users

### Student Endpoints (requires STUDENT role)
- `GET /api/student/courses` - List enrolled courses
- `GET /api/student/courses/{id}` - Get course details
- `POST /api/student/playback/grant` - Request playback grant
- `POST /api/student/quiz/submit` - Submit quiz answers
- `GET /api/student/grades` - Get grade history

### Security Features
- JWT-based stateless authentication
- Device fingerprint validation (max 2 devices per student)
- Signed URL with 2-hour expiry for video access
- AES-128 encrypted HLS streaming
- Dynamic watermarking with position refresh
- Role-based access control (ADMIN, STUDENT)
