CREATE TABLE taskboard_user (
  id                  BIGINT(20)    NOT NULL AUTO_INCREMENT,
  username            VARCHAR(255)  NOT NULL,
  is_admin            BOOLEAN       NOT NULL,
  last_login          TIMESTAMP     NULL DEFAULT NULL,

  PRIMARY KEY (id),
  CONSTRAINT tu_username_uq UNIQUE (username)
);
