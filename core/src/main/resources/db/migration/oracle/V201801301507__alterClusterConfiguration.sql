-- create new column
ALTER TABLE followup_cluster_item ADD project_key VARCHAR2(255) NULL;

-- drop not-null constraint
ALTER TABLE followup_cluster_item MODIFY followup_config NUMBER NULL;

-- migrate from template to project
UPDATE followup_cluster_item cl
SET cl.project_key = (SELECT tmp.project_key FROM template_project tmp WHERE tmp.template_id = cl.followup_config),
    cl.followup_config = null
WHERE (
    SELECT COUNT(tp.project_key)
    FROM template_project tp
    WHERE tp.template_id = cl.followup_config
    GROUP BY tp.template_id
) = 1;

-- remove clusters where template is for multiple projects
DELETE FROM followup_cluster_item WHERE project_key IS NULL;

-- remove old column
ALTER TABLE followup_cluster_item DROP CONSTRAINT fu_cluster_item_config_fk;
ALTER TABLE followup_cluster_item DROP CONSTRAINT fu_cluster_item_unique;
ALTER TABLE followup_cluster_item DROP COLUMN followup_config;

-- add constraints
ALTER TABLE followup_cluster_item MODIFY project_key VARCHAR2(255) NOT NULL;
ALTER TABLE followup_cluster_item ADD CONSTRAINT fu_cluster_item_project_fk FOREIGN KEY (project_key) REFERENCES project_filter_configuration (project_key);
ALTER TABLE followup_cluster_item ADD CONSTRAINT fu_cluster_item_unique UNIQUE (project_key, subtask_type_name, parent_type_name, sizing);