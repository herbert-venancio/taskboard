CREATE TABLE project_default_team_issuetype (
  id                  NUMBER   NOT NULL,
  project_id          NUMBER   NOT NULL,
  team_id             NUMBER   NOT NULL,
  issue_type_id       NUMBER   NOT NULL,

  PRIMARY KEY (id),
  CONSTRAINT pdti_project_fk     FOREIGN KEY (project_id)     REFERENCES project_filter_configuration (id),
  CONSTRAINT pdti_team_fk        FOREIGN KEY (team_id)        REFERENCES team (id),
  CONSTRAINT pdti_project_issue_type_unique UNIQUE (project_id, issue_type_id)
);
