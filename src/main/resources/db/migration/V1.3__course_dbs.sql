CREATE TABLE IF NOT EXISTS "course_category"
(
    "id"    BIGSERIAL PRIMARY KEY,
    "title" VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS "course"
(
    "id"          BIGSERIAL PRIMARY KEY,
    "title"       VARCHAR(255) NOT NULL,
    "category_id" BIGINT REFERENCES "course_category" (id) ON DELETE CASCADE
);

DO
$$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = 'class_type') THEN
            CREATE TYPE "class_type" AS ENUM ('lesson', 'homework');
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = 'level_type') THEN
            CREATE TYPE "level_type" AS ENUM ('beginner', 'elementary', 'pre_intermediate', 'intermediate', 'upper_intermediate', 'advanced');
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS "class"
(
    "id"            BIGSERIAL PRIMARY KEY,
    "course_id"     BIGINT REFERENCES "course" (id) ON DELETE CASCADE,
    "type"          class_type NOT NULL,
    "lesson_id"     BIGINT REFERENCES "class" (id) ON DELETE NO ACTION, -- if it's a homework, there should be a lesson for it
    "difficulty"    level_type NOT NULL,
    "list_position" INT        NOT NULL
);

DO
$$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = 'task_type') THEN
            CREATE TYPE "task_type" AS ENUM ('vocabulary', 'grammar', 'speaking', 'writing', 'reading', 'listening');
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS "task"
(
    "id"            BIGSERIAL PRIMARY KEY,
    "class_id"      BIGINT REFERENCES "class" (id) ON DELETE CASCADE,
    "type"          task_type  NOT NULL,
    "difficulty"    level_type NOT NULL,
    "list_position" INT        NOT NULL
);

DO
$$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = 'exercise_type') THEN
            CREATE TYPE "exercise_type" AS ENUM ('true_false', 'fill_blanks', 'match');
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS "exercise"
(
    "id"      BIGSERIAL PRIMARY KEY,
    "task_id" BIGINT REFERENCES "task" (id) ON DELETE CASCADE,
    "type"    exercise_type NOT NULL,
    "content" JSON          NOT NULL
);