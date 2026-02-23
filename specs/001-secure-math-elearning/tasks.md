---

description: "Task list for secure mathematics e-learning platform implementation"
---

# Tasks: Secure Mathematics E-Learning Platform

**Input**: Design documents from `/specs/001-secure-math-elearning/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included because the specification explicitly requires service unit tests, security integration tests, signed URL validation, and load testing.

**Organization**: Tasks are grouped by user story to support independent implementation and validation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Task can run in parallel (different files, no unmet dependencies)
- **[Story]**: User story label for story-phase tasks (`[US1]`, `[US2]`, `[US3]`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Bootstrap backend, frontend, and deployment scaffold.

- [X] T001 Create backend Spring Boot project skeleton in `backend/pom.xml`
- [X] T002 [P] Create frontend React project skeleton in `frontend/package.json`
- [X] T003 [P] Create Docker Compose baseline services in `infra/docker-compose.yml`
- [X] T004 [P] Configure Nginx reverse proxy and HTTPS entrypoint in `infra/nginx/default.conf`
- [X] T005 [P] Add backend environment template in `backend/.env.example`
- [X] T006 [P] Add frontend environment template in `frontend/.env.example`
- [X] T007 [P] Add backend base application configuration in `backend/src/main/resources/application.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Complete shared auth, security, persistence, and policy foundations before user stories.

**CRITICAL**: No user story work starts before this phase is complete.

- [X] T008 Initialize Flyway baseline migration in `backend/src/main/resources/db/migration/V1__baseline.sql`
- [X] T009 [P] Implement `UserAccount` JPA entity in `backend/src/main/java/com/securemath/domain/UserAccount.java`
- [X] T010 [P] Implement `Role` and `AccountStatus` enums in `backend/src/main/java/com/securemath/domain/Role.java`
- [X] T011 [P] Implement user repository in `backend/src/main/java/com/securemath/repository/UserAccountRepository.java`
- [X] T012 [P] Implement BCrypt password encoder configuration in `backend/src/main/java/com/securemath/security/PasswordConfig.java`
- [X] T013 [P] Implement JWT token service in `backend/src/main/java/com/securemath/security/JwtTokenService.java`
- [X] T014 [P] Implement JWT authentication filter in `backend/src/main/java/com/securemath/security/JwtAuthenticationFilter.java`
- [X] T015 Implement Spring Security route segregation for `/api/admin/**` and `/api/student/**` in `backend/src/main/java/com/securemath/security/SecurityConfig.java`
- [X] T016 [P] Create authentication DTOs in `backend/src/main/java/com/securemath/dto/auth/LoginRequestDto.java`
- [X] T017 Implement authentication endpoint in `backend/src/main/java/com/securemath/api/AuthController.java`
- [X] T017a Implement initial admin seeding from `APP_INITIAL_ADMIN_PASSWORD` env var in `backend/src/main/java/com/securemath/service/AdminProvisioningService.java`
- [X] T017b Implement forced password change flow for `mustChangePassword=true` users in `backend/src/main/java/com/securemath/api/PasswordChangeController.java`
- [X] T018 [P] Implement registered device entity in `backend/src/main/java/com/securemath/domain/RegisteredDevice.java`
- [X] T019 [P] Implement registered device repository in `backend/src/main/java/com/securemath/repository/RegisteredDeviceRepository.java`
- [X] T020 Implement device validation filter (max 2 devices/student) in `backend/src/main/java/com/securemath/security/DeviceValidationFilter.java`
- [X] T021 Implement global exception handling and API error model in `backend/src/main/java/com/securemath/api/ApiExceptionHandler.java`
- [X] T022 [P] Implement shared DTO mapper configuration in `backend/src/main/java/com/securemath/dto/MapperConfig.java`

**Checkpoint**: Foundation complete; user story phases can proceed.

---

## Phase 3: User Story 1 - Admin Creates and Publishes Learning Content (Priority: P1)

**Goal**: Enable Admin CRUD for course hierarchy, account status, and student enrollment.

**Independent Test**: Admin can create course/chapter/lesson, activate/deactivate account, enroll student, and student access scope changes accordingly.

### Tests for User Story 1

- [X] T023 [P] [US1] Add integration test for admin course CRUD authorization in `backend/src/test/java/com/securemath/integration/admin/AdminCourseAuthorizationIT.java`
- [X] T024 [P] [US1] Add integration test for enrollment scope enforcement in `backend/src/test/java/com/securemath/integration/admin/EnrollmentScopeIT.java`

### Implementation for User Story 1

- [X] T025 [P] [US1] Implement `Course` entity in `backend/src/main/java/com/securemath/domain/Course.java`
- [X] T026 [P] [US1] Implement `Chapter` entity in `backend/src/main/java/com/securemath/domain/Chapter.java`
- [X] T027 [P] [US1] Implement `Lesson` entity in `backend/src/main/java/com/securemath/domain/Lesson.java`
- [X] T028 [P] [US1] Implement `Enrollment` entity in `backend/src/main/java/com/securemath/domain/Enrollment.java`
- [X] T029 [P] [US1] Add migration for course/chapter/lesson/enrollment tables in `backend/src/main/resources/db/migration/V2__course_structure.sql`
- [X] T030 [P] [US1] Implement course repository in `backend/src/main/java/com/securemath/repository/CourseRepository.java`
- [X] T031 [P] [US1] Implement chapter repository in `backend/src/main/java/com/securemath/repository/ChapterRepository.java`
- [X] T032 [P] [US1] Implement lesson repository in `backend/src/main/java/com/securemath/repository/LessonRepository.java`
- [X] T033 [P] [US1] Implement enrollment repository in `backend/src/main/java/com/securemath/repository/EnrollmentRepository.java`
- [X] T034 [P] [US1] Create admin course DTOs in `backend/src/main/java/com/securemath/dto/admin/CourseDtos.java`
- [X] T035 [P] [US1] Create admin enrollment DTOs in `backend/src/main/java/com/securemath/dto/admin/EnrollmentDtos.java`
- [X] T036 [US1] Implement admin course service in `backend/src/main/java/com/securemath/service/AdminCourseService.java`
- [X] T037 [US1] Implement admin student-management service in `backend/src/main/java/com/securemath/service/AdminStudentService.java`
- [X] T038 [US1] Implement admin course endpoints in `backend/src/main/java/com/securemath/api/admin/AdminCourseController.java`
- [X] T039 [US1] Implement admin account status endpoint in `backend/src/main/java/com/securemath/api/admin/AdminUserController.java`
- [X] T040 [US1] Implement admin enrollment endpoint in `backend/src/main/java/com/securemath/api/admin/AdminEnrollmentController.java`

**Checkpoint**: Admin content and enrollment management works independently.

---

## Phase 4: User Story 2 - Student Securely Streams Enrolled Video Lessons (Priority: P1)

**Goal**: Deliver secure, enrolled-only video playback with HLS encryption, signed URLs, device limits, and dynamic watermarking.

**Independent Test**: Enrolled student receives signed playback grant and streams lesson; third device is denied; watermark position updates every 15-30 seconds.

### Tests for User Story 2

- [X] T041 [P] [US2] Add integration test for playback grant authorization in `backend/src/test/java/com/securemath/integration/student/PlaybackGrantAuthorizationIT.java`
- [X] T042 [P] [US2] Add integration test for device limit enforcement in `backend/src/test/java/com/securemath/integration/student/DeviceLimitIT.java`
- [X] T043 [P] [US2] Add integration test for signed URL expiry rejection in `backend/src/test/java/com/securemath/integration/student/SignedUrlExpiryIT.java`

### Implementation for User Story 2

- [X] T044 [P] [US2] Implement `VideoAsset` entity in `backend/src/main/java/com/securemath/domain/VideoAsset.java`
- [X] T045 [P] [US2] Implement `PlaybackAccessGrant` entity in `backend/src/main/java/com/securemath/domain/PlaybackAccessGrant.java`
- [X] T046 [P] [US2] Add migration for video assets and playback grants in `backend/src/main/resources/db/migration/V3__video_assets.sql`
- [X] T047 [P] [US2] Implement video asset repository in `backend/src/main/java/com/securemath/repository/VideoAssetRepository.java`
- [X] T048 [P] [US2] Implement playback grant repository in `backend/src/main/java/com/securemath/repository/PlaybackAccessGrantRepository.java`
- [X] T049 [P] [US2] Implement S3 upload service abstraction in `backend/src/main/java/com/securemath/video/S3StorageService.java`
- [X] T050 [P] [US2] Implement HLS transcoding job orchestration service in `backend/src/main/java/com/securemath/video/HlsTranscodingService.java`
- [X] T051 [P] [US2] Implement AES-128 key and playlist encryption configuration in `backend/src/main/java/com/securemath/video/HlsEncryptionService.java`
- [X] T052 [P] [US2] Implement signed URL issuance service (2-hour TTL) in `backend/src/main/java/com/securemath/video/SignedUrlService.java`
- [X] T053 [P] [US2] Implement watermark seed and shifting policy service in `backend/src/main/java/com/securemath/video/WatermarkPolicyService.java`
- [X] T054 [US2] Implement student playback grant service in `backend/src/main/java/com/securemath/service/StudentPlaybackService.java`
- [X] T055 [US2] Implement student playback endpoint in `backend/src/main/java/com/securemath/api/student/StudentPlaybackController.java`
- [X] T056 [P] [US2] Implement secure HLS player component in `frontend/src/features/player/SecureHlsPlayer.tsx`
- [X] T057 [P] [US2] Implement dynamic watermark overlay component in `frontend/src/features/player/WatermarkOverlay.tsx`
- [X] T058 [US2] Integrate playback grant API in frontend service layer in `frontend/src/services/api.ts`
- [X] T059 [US2] Wire enrolled lesson playback page in `frontend/src/pages/StudentLessonPage.tsx`

**Checkpoint**: Secure streaming flow works independently.

---

## Phase 5: User Story 3 - Student Completes Math Assessments and Views Grades (Priority: P2)

**Goal**: Provide quiz delivery, LaTeX rendering, synchronous grading, attempt persistence, and grade visibility.

**Independent Test**: Student completes quiz (MCQ/True-False/Numeric with LaTeX), receives synchronous score, and can view grade history; admin can view performance analytics.

### Tests for User Story 3

- [X] T060 [P] [US3] Add unit tests for grading service in `backend/src/test/java/com/securemath/unit/service/QuizGradingServiceTest.java`
- [X] T061 [P] [US3] Add integration test for quiz submission and persistence in `backend/src/test/java/com/securemath/integration/student/QuizSubmissionIT.java`

### Implementation for User Story 3

- [X] T062 [P] [US3] Implement `Quiz` entity in `backend/src/main/java/com/securemath/domain/Quiz.java`
- [X] T063 [P] [US3] Implement `Question` entity in `backend/src/main/java/com/securemath/domain/Question.java`
- [X] T064 [P] [US3] Implement `QuizAttempt` entity in `backend/src/main/java/com/securemath/domain/QuizAttempt.java`
- [X] T065 [P] [US3] Implement `Answer` entity in `backend/src/main/java/com/securemath/domain/Answer.java`
- [X] T066 [P] [US3] Add migration for quiz domain tables in `backend/src/main/resources/db/migration/V5__quiz_engine.sql`
- [X] T067 [P] [US3] Implement quiz repository in `backend/src/main/java/com/securemath/repository/QuizRepository.java`
- [X] T068 [P] [US3] Implement question repository in `backend/src/main/java/com/securemath/repository/QuestionRepository.java`
- [X] T069 [P] [US3] Implement quiz attempt repository in `backend/src/main/java/com/securemath/repository/QuizAttemptRepository.java`
- [X] T070 [P] [US3] Implement answer repository in `backend/src/main/java/com/securemath/repository/AnswerRepository.java`
- [X] T071 [P] [US3] Implement quiz DTOs and explicit mapper in `backend/src/main/java/com/securemath/dto/quiz/QuizDtos.java`
- [X] T072 [US3] Implement synchronous quiz grading service in `backend/src/main/java/com/securemath/service/QuizGradingService.java`
- [X] T073 [US3] Implement student quiz service in `backend/src/main/java/com/securemath/service/StudentQuizService.java`
- [X] T074 [US3] Implement quiz submission endpoint in `backend/src/main/java/com/securemath/api/student/StudentQuizController.java`
- [X] T075 [US3] Implement student grades endpoint in `backend/src/main/java/com/securemath/api/student/StudentGradeController.java`
- [X] T076 [US3] Implement admin quiz analytics endpoint in `backend/src/main/java/com/securemath/api/admin/AdminAnalyticsController.java`
- [X] T077 [P] [US3] Implement quiz API service for frontend in `frontend/src/services/api.ts`
- [X] T078 [P] [US3] Implement KaTeX question renderer component in `frontend/src/features/quiz/KatexQuestionRenderer.tsx`
- [X] T079 [US3] Implement student quiz page flow in `frontend/src/pages/StudentQuizPage.tsx`
- [X] T080 [US3] Implement student grades page in `frontend/src/pages/StudentGradesPage.tsx`

**Checkpoint**: Assessment and grading flow works independently.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T081 [P] Add service-level unit tests for enrollment and playback policies in `backend/src/test/java/com/securemath/unit/service/EnrollmentPlaybackPolicyTest.java`
- [X] T082 [P] Add security integration suite for role boundary protection in `backend/src/test/java/com/securemath/integration/security/RoleBoundaryIT.java`
- [X] T083 [P] Add load test scenario for 500 concurrent users in `backend/src/test/java/com/securemath/integration/performance/PerformanceIT.java`
- [X] T084 [P] Add performance test for video startup latency in `backend/src/test/java/com/securemath/integration/performance/PerformanceIT.java`
- [X] T085 [P] Add performance test for synchronous grading latency in `backend/src/test/java/com/securemath/integration/performance/PerformanceIT.java`
- [X] T086 Validate signed URL expiration behavior and replay denial in `backend/src/test/java/com/securemath/integration/student/SignedUrlReplayIT.java`
- [X] T087 [P] Add deployment hardening defaults in `infra/docker-compose.yml`
- [X] T088 [P] Enforce HTTPS and secure headers in `infra/nginx/default.conf`
- [X] T089 Update implementation and validation commands in `specs/001-secure-math-elearning/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Starts immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 completion and blocks all story work
- **Phase 3 (US1)**: Depends on Phase 2
- **Phase 4 (US2)**: Depends on Phase 2; can run in parallel with Phase 3 after foundation
- **Phase 5 (US3)**: Depends on Phase 2; can run in parallel with Phases 3-4 after foundation
- **Phase 6 (Polish)**: Depends on completion of all targeted user stories

### User Story Dependencies

- **US1**: Independent once foundation exists
- **US2**: Independent once foundation exists
- **US3**: Independent once foundation exists

### Within Each User Story

- Tests first when feasible, then entities/migrations, then services, then endpoints, then frontend integration
- DTO mapping and authorization checks must be completed before endpoint finalization

### Parallel Opportunities

- Setup: T002-T007 can run in parallel after T001 starts structure
- Foundational: T009-T014 and T018-T019 can run in parallel
- US1: T025-T035 can run in parallel by layer boundaries
- US2: T044-T053 and T056-T057 can run in parallel
- US3: T062-T071 and T077-T078 can run in parallel
- Polish: T081-T085 and T087-T088 can run in parallel

---

## Parallel Example: User Story 2

```bash
# Backend video/security services in parallel:
Task: "T049 Implement S3 storage service in backend/src/main/java/com/securemath/video/S3StorageService.java"
Task: "T050 Implement HLS transcoding service in backend/src/main/java/com/securemath/video/HlsTranscodingService.java"
Task: "T052 Implement signed URL service in backend/src/main/java/com/securemath/video/SignedUrlService.java"

# Frontend playback components in parallel:
Task: "T056 Implement secure player in frontend/src/features/player/SecureHlsPlayer.tsx"
Task: "T057 Implement watermark overlay in frontend/src/features/player/WatermarkOverlay.tsx"
```

## Parallel Example: User Story 3

```bash
# Domain model and persistence in parallel:
Task: "T062 Implement Quiz entity in backend/src/main/java/com/securemath/domain/Quiz.java"
Task: "T063 Implement Question entity in backend/src/main/java/com/securemath/domain/Question.java"
Task: "T064 Implement QuizAttempt entity in backend/src/main/java/com/securemath/domain/QuizAttempt.java"

# Frontend quiz UI tasks in parallel:
Task: "T078 Implement KaTeX renderer in frontend/src/features/quiz/KatexQuestionRenderer.tsx"
Task: "T079 Implement quiz page flow in frontend/src/pages/StudentQuizPage.tsx"
```

---

## Implementation Strategy

### MVP First (Recommended)

1. Complete Phase 1 and Phase 2
2. Complete Phase 3 (US1) and Phase 4 (US2)
3. Validate secure enrolled playback flow end-to-end
4. Demo admin operations and protected video delivery

### Incremental Delivery

1. Foundation (Phases 1-2)
2. Deliver US1 (admin content and enrollment)
3. Deliver US2 (secure streaming, device policy, watermark)
4. Deliver US3 (quiz engine and grading)
5. Finish with Phase 6 hardening and performance validation

### Parallel Team Strategy

1. Team A: foundation security and auth tasks
2. Team B: US1 admin domain/services/endpoints
3. Team C: US2 media pipeline and frontend player
4. Team D: US3 quiz engine and assessment UI
5. Shared QA/SRE pass for Phase 6

---

## Notes

- `[P]` tasks are safe to parallelize when dependencies are satisfied
- All story tasks include `[USx]` label and file paths
- Service unit tests and security integration tests are required by constitution
- Performance validation tasks are mandatory release gates
