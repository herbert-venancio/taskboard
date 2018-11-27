CREATE TABLE change_request (
    id NUMBER NOT NULL,
    project_id NUMBER NOT NULL,
    "DATE" date NOT NULL,
    name varchar(255 char) NOT NULL,
    budget_increase NUMBER(10,0) NOT NULL,
    is_baseline NUMBER(1) NOT NULL,
    CONSTRAINT project_fk FOREIGN KEY (project_id) REFERENCES project_filter_configuration (id),
    PRIMARY KEY (id)
);

INSERT INTO change_request(id, project_id, "DATE", name, budget_increase, is_baseline)
     (SELECT HIBERNATE_SEQUENCE.NEXTVAL, p.id, p.start_date, 'Baseline', 0, 1
       FROM project_filter_configuration p
            WHERE p.start_date IS NOT NULL) ;