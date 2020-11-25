CREATE TABLE IF NOT EXISTS "enrollment"
(
    "id"         BIGSERIAL PRIMARY KEY,
    "student_id" BIGINT NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "course_id"  BIGINT NOT NULL REFERENCES "course" (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "class_result"
(
    "id"         BIGSERIAL PRIMARY KEY,
    "student_id" BIGINT NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "class_id"   BIGINT NOT NULL REFERENCES "class" (id) ON DELETE CASCADE,
    "score"      INT    NOT NULL
);

CREATE TABLE IF NOT EXISTS "task_result"
(
    "id"         BIGSERIAL PRIMARY KEY,
    "student_id" BIGINT NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "task_id"    BIGINT NOT NULL REFERENCES "task" (id) ON DELETE CASCADE,
    "score"      INT    NOT NULL
);

CREATE TABLE IF NOT EXISTS "exercise_result"
(
    "id"          BIGSERIAL PRIMARY KEY,
    "student_id"  BIGINT NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "exercise_id" BIGINT NOT NULL REFERENCES "exercise" (id) ON DELETE CASCADE,
    "score"       INT    NOT NULL,
    "content"     JSON   NOT NULL
);
