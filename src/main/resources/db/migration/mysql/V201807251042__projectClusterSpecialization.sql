-- Create Sizing cluster table

CREATE TABLE sizing_cluster
(
    id          bigint(20)   NOT NULL AUTO_INCREMENT,
    name        varchar(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY siz_clus_name_unique(name)
);

-- Insert Sizing Cluster base registry

INSERT INTO sizing_cluster(id, name)
     VALUES (1, 'Base Sizing Cluster');

-- project_filter_configuration Adjustments

ALTER TABLE project_filter_configuration
    ADD COLUMN base_cluster_id bigint(20);

UPDATE project_filter_configuration
    SET base_cluster_id = 1;

ALTER TABLE project_filter_configuration
    ADD CONSTRAINT project_cluster_fk
    FOREIGN KEY (base_cluster_id) REFERENCES sizing_cluster(id);

ALTER TABLE project_filter_configuration
    MODIFY base_cluster_id bigint(20) NOT NULL;

-- followup_cluster_item data normalization

UPDATE followup_cluster_item
   SET parent_type_name = 'notused'
 WHERE parent_type_name = 'unused';

-- Creating Sizing cluster Item table

CREATE TABLE sizing_cluster_item
	SELECT * FROM followup_cluster_item;

ALTER TABLE sizing_cluster_item
    ADD CONSTRAINT siz_clus_item_pk
    PRIMARY KEY (id);

ALTER TABLE sizing_cluster_item
    ADD COLUMN base_cluster_id bigint(20) null;

ALTER TABLE sizing_cluster_item
    MODIFY project_key varchar(255) null;

ALTER TABLE sizing_cluster_item
    ADD CONSTRAINT siz_clus_item_clus_base_fk
    FOREIGN KEY (base_cluster_id) REFERENCES sizing_cluster(id);

ALTER TABLE sizing_cluster_item ADD
 CONSTRAINT siz_clus_item_ck
      CHECK ((project_key IS NULL AND base_cluster_id IS NOT NULL) OR (project_key IS NOT NULL AND base_cluster_id IS NULL));

ALTER TABLE sizing_cluster_item
    MODIFY id BIGINT(20) NOT NULL AUTO_INCREMENT;

-- Removing table followup_cluster_item

drop table followup_cluster_item;
