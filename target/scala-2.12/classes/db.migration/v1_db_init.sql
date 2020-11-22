CREATE TABLE "users"
(
    "id"         serial4 PRIMARY KEY,
    "username"   VARCHAR not null ,
    "email"      VARCHAR NOT NULL,
    "first_name" VARCHAR NOT NULL,
    "last_name"  VARCHAR NOT NULL,

    UNIQUE (id)
);