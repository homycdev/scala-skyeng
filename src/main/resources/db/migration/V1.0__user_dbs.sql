DO
$$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = 'role_type') THEN
            CREATE TYPE "role_type" AS ENUM ('student', 'teacher', 'admin');
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = 'gender_type') THEN
            CREATE TYPE "gender_type" AS ENUM ('male', 'female', 'other');
        END IF;
    END
$$;


CREATE TABLE IF NOT EXISTS "user"
(
    "id"           BIGSERIAL PRIMARY KEY,
    "first_name"   VARCHAR(255),
    "last_name"    VARCHAR(255),
    "birth_date"   DATE,
    "gender"       gender_type,
    "email"        VARCHAR(255),
    "hash"         VARCHAR   NOT NULL,
    "phone_number" VARCHAR(12),
    "role"         role_type NOT NULL,
    "created"      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "jwt"
(
    "id"           BIGSERIAL PRIMARY KEY,
    "jwt"          VARCHAR   NOT NULL,
    "identity"     BIGINT    NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    "expiry"       TIMESTAMP NOT NULL,
    "last_touched" TIMESTAMP
);

DO
$$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM pg_type WHERE typname = 'qualification_type') THEN
            CREATE TYPE "qualification_type" AS ENUM ('russian_speaking', 'native_speaker');
        END IF;
    END
$$;


CREATE TABLE IF NOT EXISTS "teacher_profile"
(
    "user_id"       BIGINT PRIMARY KEY REFERENCES "user" (id) ON DELETE CASCADE,
    "bio"           VARCHAR(2048),
    "greeting"      VARCHAR(2048),
    "qualification" qualification_type NOT NULL
);

CREATE TABLE IF NOT EXISTS "student_profile"
(
    "user_id"    BIGINT PRIMARY KEY REFERENCES "user" (id) ON DELETE CASCADE,
    "teacher_id" BIGINT REFERENCES "user" (id) ON DELETE NO ACTION,
    "balance"    INT NOT NULL DEFAULT 0
);

-- skipping admin_profile table for now
