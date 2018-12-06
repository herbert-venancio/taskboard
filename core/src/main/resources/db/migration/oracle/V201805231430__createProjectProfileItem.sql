CREATE TABLE project_profile_item (
  id                  NUMBER       NOT NULL,
  project_id          NUMBER       NOT NULL,
  role_name           VARCHAR(255) NOT NULL, 
  people_count        NUMBER       NOT NULL,
  allocation_start    DATE         NOT NULL, 
  allocation_end      DATE         NOT NULL,

  CONSTRAINT ppi_pk PRIMARY KEY (id),
  CONSTRAINT ppi_project_fk FOREIGN KEY (project_id) REFERENCES project_filter_configuration (id)
);
