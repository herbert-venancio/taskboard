CREATE TABLE taskboard_user (
  id                  NUMBER        NOT NULL,
  username            VARCHAR(255)  NOT NULL,
  is_admin            NUMBER(1,0)   NOT NULL,
  last_login          TIMESTAMP     DEFAULT NULL,

  CONSTRAINT tu_pk PRIMARY KEY (id),
  CONSTRAINT tu_username_uq UNIQUE (username)
);
