-- create new column
ALTER TABLE followup_cluster_item ADD COLUMN project_key varchar(255) NULL;

-- drop not-null constraint
ALTER TABLE followup_cluster_item MODIFY followup_config bigint(20) NULL;

-- migrate from template to project
UPDATE followup_cluster_item cl
JOIN (
    SELECT tp.template_id template_id, COUNT(tp.project_key) count_project_key
    FROM template_project tp
    GROUP BY tp.template_id
) AS sub ON sub.template_id = cl.followup_config
SET cl.project_key = (SELECT project_key FROM template_project WHERE template_id = cl.followup_config),
    cl.followup_config = null
WHERE sub.count_project_key = 1;

-- remove clusters where template is for multiple projects
DELETE FROM followup_cluster_item WHERE project_key IS NULL;

-- remove old column
ALTER TABLE followup_cluster_item DROP FOREIGN KEY followup_cluster_item_config_fk;
ALTER TABLE followup_cluster_item DROP INDEX followup_cluster_item_unique;
ALTER TABLE followup_cluster_item DROP COLUMN followup_config;

-- add constraints
ALTER TABLE followup_cluster_item MODIFY project_key varchar(255) NOT NULL;
ALTER TABLE followup_cluster_item ADD CONSTRAINT followup_cluster_item_project_fk FOREIGN KEY (project_key) REFERENCES project_filter_configuration (project_key);
ALTER TABLE followup_cluster_item ADD CONSTRAINT followup_cluster_item_unique UNIQUE KEY (project_key, subtask_type_name, parent_type_name, sizing);