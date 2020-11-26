CREATE TABLE IF NOT EXISTS "room"
(
    "id"         BIGSERIAL PRIMARY KEY,
    "student_id" BIGINT       NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "teacher_id" BIGINT REFERENCES "user" (id) ON DELETE NO ACTION,
    "url"        VARCHAR(512) NOT NULL,
    "is_open"    BOOLEAN DEFAULT False
);

CREATE TABLE IF NOT EXISTS "schedule"
(
    "id"           BIGSERIAL PRIMARY KEY,
    "student_id"   BIGINT    NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "teacher_id"   BIGINT    NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "start_time"   TIMESTAMP NOT NULL,
    "duration_sec" INT       NOT NULL
);
