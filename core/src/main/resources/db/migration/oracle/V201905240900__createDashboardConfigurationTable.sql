CREATE TABLE dashboard_configuration (
    id number not null,
    project_id number not null,
    timeline_days_to_display int not null,
    created timestamp not null,
    updated timestamp not null, 
    PRIMARY KEY (id),
    CONSTRAINT dc_project_fk FOREIGN KEY (project_id) REFERENCES project_filter_configuration (id)
);