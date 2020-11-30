-- DO
-- $$
--     BEGIN
--         IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = 'transaction_type') THEN
--             CREATE TYPE "transaction_type" AS ENUM ('replenishment', 'lesson_completed', 'rescheduled', 'absent', 'corporal_accrual', 'other');
--         END IF;
--     END
-- $$;

CREATE TABLE IF NOT EXISTS "transaction"
(
    "id"         BIGSERIAL PRIMARY KEY,
    "student_id" BIGINT      NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "teacher_id" BIGINT REFERENCES "user" (id) ON DELETE NO ACTION,
    "status"     VARCHAR(16) NOT NULL,
    "change"     INT         NOT NULL,
    "reminder"   INT         NOT NULL,
    "created"    TIMESTAMP   NOT NULL
);
