-- Quiz engine tables

CREATE TYPE quiz_status AS ENUM ('DRAFT', 'PUBLISHED');
CREATE TYPE question_type AS ENUM ('MCQ', 'TRUE_FALSE', 'NUMERIC');

CREATE TABLE quiz (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    status quiz_status NOT NULL DEFAULT 'DRAFT',
    time_limit_seconds INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE question (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quiz(id) ON DELETE CASCADE,
    type question_type NOT NULL,
    prompt_text TEXT NOT NULL,
    latex_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    answer_key JSONB NOT NULL,
    points DECIMAL(10,2) NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_question_position UNIQUE (quiz_id, position)
);

CREATE TABLE quiz_attempt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quiz(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    score DECIMAL(10,2) NOT NULL,
    max_score DECIMAL(10,2) NOT NULL,
    grading_latency_ms INTEGER NOT NULL,
    CONSTRAINT uk_quiz_attempt UNIQUE (quiz_id, student_id)
);

CREATE TABLE answer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES quiz_attempt(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    response_value TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    awarded_points DECIMAL(10,2) NOT NULL,
    CONSTRAINT uk_answer UNIQUE (attempt_id, question_id)
);

CREATE INDEX idx_quiz_course ON quiz(course_id);
CREATE INDEX idx_quiz_status ON quiz(status);
CREATE INDEX idx_question_quiz ON question(quiz_id);
CREATE INDEX idx_attempt_student ON quiz_attempt(student_id);
CREATE INDEX idx_attempt_quiz ON quiz_attempt(quiz_id);
CREATE INDEX idx_answer_attempt ON answer(attempt_id);
