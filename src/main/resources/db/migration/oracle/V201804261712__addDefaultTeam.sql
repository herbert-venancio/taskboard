ALTER TABLE project_filter_configuration ADD default_team NUMBER;
ALTER TABLE project_filter_configuration ADD CONSTRAINT project_default_team_fk FOREIGN KEY (default_team) REFERENCES team (id); 