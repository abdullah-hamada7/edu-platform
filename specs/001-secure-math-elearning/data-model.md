# Data Model - Secure Mathematics E-Learning Platform

## UserAccount

- Fields:
  - `id` (UUID, primary key)
  - `email` (unique, required)
  - `passwordHash` (required, BCrypt)
  - `role` (enum: ADMIN, STUDENT)
  - `status` (enum: ACTIVE, INACTIVE)
  - `mustChangePassword` (boolean, default false for seeded admin)
  - `createdAt`, `updatedAt`
- Relationships:
  - One-to-many with `Enrollment` (as student)
  - One-to-many with `RegisteredDevice` (as student)
  - One-to-many with `QuizAttempt` (as student)
- Validation rules:
  - Email uniqueness required
  - Password hash must never be plaintext
  - Inactive users cannot authenticate or access protected resources
  - Users with `mustChangePassword=true` cannot access non-auth endpoints until password changed
- State transitions:
  - `ACTIVE -> INACTIVE` (admin deactivation)
  - `INACTIVE -> ACTIVE` (admin reactivation)
  - `mustChangePassword: true -> false` (after successful password change)

## Course

- Fields:
  - `id` (UUID)
  - `title` (required)
  - `description`
  - `status` (enum: DRAFT, PUBLISHED, ARCHIVED)
  - `createdAt`, `updatedAt`
- Relationships:
  - One-to-many with `Chapter`
  - One-to-many with `Enrollment`
  - One-to-many with `Quiz`
- Validation rules:
  - Title required
  - Archiving constraints checked when active enrollments exist

## Chapter

- Fields:
  - `id` (UUID)
  - `courseId` (FK -> Course)
  - `title` (required)
  - `position` (required)
- Relationships:
  - Many-to-one with `Course`
  - One-to-many with `Lesson`
- Validation rules:
  - `position` unique within a course

## Lesson

- Fields:
  - `id` (UUID)
  - `chapterId` (FK -> Chapter)
  - `title` (required)
  - `videoAssetId` (FK -> VideoAsset, required once published)
  - `position` (required)
- Relationships:
  - Many-to-one with `Chapter`
  - Many-to-one with `VideoAsset`
- Validation rules:
  - Published lesson requires associated video asset

## VideoAsset

- Fields:
  - `id` (UUID)
  - `rawObjectKey` (required)
  - `hlsManifestKey` (required after transcode)
  - `encryptionKeyRef` (required for encrypted HLS)
  - `transcodeStatus` (enum: PENDING, PROCESSING, READY, FAILED)
  - `createdAt`, `updatedAt`
- Relationships:
  - One-to-many with `Lesson`
  - One-to-many with `PlaybackAccessGrant`
- Validation rules:
  - Playback allowed only when `transcodeStatus = READY`
- State transitions:
  - `PENDING -> PROCESSING -> READY`
  - `PROCESSING -> FAILED` (retry policy controlled by worker)

## Enrollment

- Fields:
  - `id` (UUID)
  - `studentId` (FK -> UserAccount)
  - `courseId` (FK -> Course)
  - `status` (enum: ACTIVE, REMOVED)
  - `enrolledAt`, `updatedAt`
- Relationships:
  - Many-to-one with `UserAccount`
  - Many-to-one with `Course`
- Validation rules:
  - Unique active enrollment per (`studentId`, `courseId`)
  - Access checks require active enrollment

## RegisteredDevice

- Fields:
  - `id` (UUID)
  - `studentId` (FK -> UserAccount)
  - `fingerprintHash` (required)
  - `lastSeenAt`
  - `createdAt`
- Relationships:
  - Many-to-one with `UserAccount`
- Validation rules:
  - Maximum 2 active device records per student
  - Fingerprint must be normalized and hashed before persistence

## Quiz

- Fields:
  - `id` (UUID)
  - `courseId` (FK -> Course)
  - `title` (required)
  - `status` (enum: DRAFT, PUBLISHED)
  - `timeLimitSeconds` (optional)
- Relationships:
  - Many-to-one with `Course`
  - One-to-many with `Question`
  - One-to-many with `QuizAttempt`

## Question

- Fields:
  - `id` (UUID)
  - `quizId` (FK -> Quiz)
  - `type` (enum: MCQ, TRUE_FALSE, NUMERIC)
  - `promptText` (required)
  - `latexEnabled` (boolean, default false)
  - `answerKey` (required, JSON for MCQ options, string for others)
  - `points` (required, decimal)
  - `position` (required)
- Relationships:
  - Many-to-one with `Quiz` (each Question belongs to exactly one Quiz)
- Validation rules:
  - Answer key format must match question type
  - MCQ: answerKey contains correct option index and option list
  - TRUE_FALSE: answerKey is boolean
  - NUMERIC: answerKey contains expected numeric value and tolerance

## QuizAttempt

- Fields:
  - `id` (UUID)
  - `quizId` (FK -> Quiz)
  - `studentId` (FK -> UserAccount)
  - `submittedAt`
  - `score` (decimal)
  - `maxScore` (decimal)
  - `gradingLatencyMs` (integer)
- Relationships:
  - Many-to-one with `Quiz`
  - Many-to-one with `UserAccount`
  - One-to-many with `Answer`
- Validation rules:
  - One attempt belongs to one student and one quiz
  - Grading latency captured for performance verification

## Answer

- Fields:
  - `id` (UUID)
  - `attemptId` (FK -> QuizAttempt)
  - `questionId` (FK -> Question)
  - `responseValue` (required)
  - `isCorrect` (boolean)
  - `awardedPoints` (decimal)
- Relationships:
  - Many-to-one with `QuizAttempt`
  - Many-to-one with `Question`

## PlaybackAccessGrant

- Fields:
  - `id` (UUID)
  - `studentId` (FK -> UserAccount)
  - `lessonId` (FK -> Lesson)
  - `deviceId` (FK -> RegisteredDevice)
  - `signedUrlHash` (required)
  - `expiresAt` (required)
  - `issuedAt` (required)
- Relationships:
  - Many-to-one with `UserAccount`
  - Many-to-one with `Lesson`
  - Many-to-one with `RegisteredDevice`
- Validation rules:
  - Grant denied when enrollment or device policy fails
  - Expired grants cannot be reused

## Referential Integrity Summary

- All FK relationships are enforced at database level.
- Hard deletes are default; dependent data deletion follows FK policy.
- Migration scripts must preserve FK integrity and reject orphan-producing changes.
