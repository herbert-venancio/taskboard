CREATE TABLE template (
    id NUMBER NOT NULL,
    name VARCHAR2(255) NOT NULL,
    path VARCHAR2(255) NOT NULL,
    CONSTRAINT template_pk PRIMARY KEY (id),
    CONSTRAINT template_name_unique UNIQUE (name)
);

CREATE TABLE template_project (
    TemplateId NUMBER NOT NULL,
    ProjectId VARCHAR2(255) NOT NULL,
    CONSTRAINT templateId_fk FOREIGN KEY (TemplateId) REFERENCES template (id),
    CONSTRAINT projectId_fk FOREIGN KEY (ProjectId) REFERENCES project_filter_configuration (project_key)
);