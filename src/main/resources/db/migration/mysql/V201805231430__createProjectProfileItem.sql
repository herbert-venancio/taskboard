CREATE TABLE project_profile_item (
  id                  BIGINT(20)   NOT NULL AUTO_INCREMENT,
  project_id          BIGINT(20)   NOT NULL,
  role_name           VARCHAR(255) NOT NULL, 
  people_count        INT(10)      NOT NULL,
  allocation_start    DATE         NOT NULL, 
  allocation_end      DATE         NOT NULL,

  PRIMARY KEY (id),
  CONSTRAINT ppi_project_fk FOREIGN KEY (project_id) REFERENCES project_filter_configuration (id)
);
