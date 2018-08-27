CREATE TABLE project_default_team_issuetype (
  id                  BIGINT(20)   NOT NULL AUTO_INCREMENT,
  project_id          BIGINT(20)   NOT NULL,
  team_id             BIGINT(20)   NOT NULL,
  issue_type_id       BIGINT(20)   NOT NULL,

  PRIMARY KEY (id),
  CONSTRAINT pdti_project_fk     FOREIGN KEY (project_id)     REFERENCES project_filter_configuration (id),
  CONSTRAINT pdti_team_fk        FOREIGN KEY (team_id)        REFERENCES team (id),
  UNIQUE KEY pdti_project_issue_type_unique (project_id, issue_type_id)
);
