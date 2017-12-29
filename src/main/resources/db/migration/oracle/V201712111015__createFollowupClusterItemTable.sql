CREATE TABLE followup_cluster_item (
    id NUMBER NOT NULL,
    followup_config NUMBER NOT NULL,
    subtask_type_name varchar(255) NOT NULL,
    parent_type_name varchar(255) NOT NULL,
    sizing varchar(10) NOT NULL,
    effort NUMBER(5,2) NOT NULL,
    cycle NUMBER(5,2) NOT NULL,

    CONSTRAINT fu_cluster_item_pk PRIMARY KEY (id),
    CONSTRAINT fu_cluster_item_unique UNIQUE (followup_config, subtask_type_name, parent_type_name, sizing),
    CONSTRAINT fu_cluster_item_config_fk FOREIGN KEY (followup_config) REFERENCES template (id)
)