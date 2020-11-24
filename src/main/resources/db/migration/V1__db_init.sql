CREATE TABLE "users"
(
    "id"         serial4 PRIMARY KEY,
    "user_name"  VARCHAR not null,
    "first_name" VARCHAR ,
    "last_name"  VARCHAR ,
    "email"      VARCHAR ,
    "hash"       VARCHAR not null,
    "phone"      varchar,
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

