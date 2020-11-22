CREATE TABLE "users"
(
    "id"         serial4 PRIMARY KEY,
    "user_name"  VARCHAR not null,
    "first_name" VARCHAR NOT NULL,
    "last_name"  VARCHAR NOT NULL,
    "email"      VARCHAR NOT NULL,
    "hash"       VARCHAR not null,
    "role"       varchar not null default 'Student',

    UNIQUE (id)
);

create table JWT
(
    "id"           serial4 PRIMARY KEY,
    "JWT"          varchar   not null,
    "identity"     bigint    not null references users (id) on delete cascade,
    "EXPIRY"       timestamp not null,
    "LAST_TOUCHED" timestamp
);

