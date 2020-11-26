CREATE TABLE IF NOT EXISTS "word"
(
    "id"                  BIGSERIAL PRIMARY KEY,
    "phase"               VARCHAR(255) NOT NULL,
    "image_url"           VARCHAR(512),
    "transcript"          VARCHAR(512),
    "english_meaning"     VARCHAR(2048),
    "russian_translation" VARCHAR(2048)
);

CREATE TABLE IF NOT EXISTS "class_vocabulary"
(
    "id"       BIGSERIAL PRIMARY KEY,
    "word_id"  BIGINT NOT NULL REFERENCES word (id) ON DELETE CASCADE,
    "class_id" BIGINT NOT NULL REFERENCES class (id) ON DELETE CASCADE
);