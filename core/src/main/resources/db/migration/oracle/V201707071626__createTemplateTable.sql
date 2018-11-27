CREATE TABLE template (
    id NUMBER NOT NULL,
    name VARCHAR2(255) NOT NULL,
    path VARCHAR2(255) NOT NULL,
    CONSTRAINT template_pk PRIMARY KEY (id),
    CONSTRAINT template_name_unique UNIQUE (name)
);

CREATE TABLE template_project (
    template_id NUMBER NOT NULL,
    project_key VARCHAR2(255) NOT NULL,
    CONSTRAINT tp_template_fk FOREIGN KEY (template_id) REFERENCES template (id),
    CONSTRAINT tp_project_fk FOREIGN KEY (project_key) REFERENCES project_filter_configuration (project_key)
);