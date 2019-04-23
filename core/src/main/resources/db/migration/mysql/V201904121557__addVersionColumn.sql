-- add column `created_dt` and `updated_dt` where it doesn't exist
ALTER TABLE change_request
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE filtro
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE followup_daily_synthesis
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE holiday
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE issue_type_visibility
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE lane
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE parent_link_config
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE project_default_team_issuetype
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE project_filter_configuration
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE project_profile_item
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE rule
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE sizing_cluster
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE sizing_cluster_item
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE stage
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE step
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE taskboard_user
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE team_filter_configuration
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE template
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE user_preferences
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE wip_config
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;

-- add column where it already exist
ALTER TABLE taskboard_issue
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE team
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;
ALTER TABLE user_team
  ADD COLUMN created_dt DATETIME NULL DEFAULT NULL, ADD COLUMN updated_dt DATETIME NULL DEFAULT NULL;

-- copy existing valid data to new columns
UPDATE taskboard_issue SET created_dt = created    WHERE created    <> 0;
UPDATE taskboard_issue SET updated_dt = updated    WHERE updated    <> 0;
UPDATE team            SET created_dt = created_at WHERE created_at <> 0;
UPDATE team            SET updated_dt = updated_at WHERE updated_at <> 0;
UPDATE user_team       SET created_dt = created_at WHERE created_at <> 0;
UPDATE user_team       SET updated_dt = updated_at WHERE updated_at <> 0;

-- ensure no null values exists
UPDATE change_request                 SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE change_request                 SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE filtro                         SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE filtro                         SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE followup_daily_synthesis       SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE followup_daily_synthesis       SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE holiday                        SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE holiday                        SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE issue_type_visibility          SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE issue_type_visibility          SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE lane                           SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE lane                           SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE parent_link_config             SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE parent_link_config             SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE project_default_team_issuetype SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE project_default_team_issuetype SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE project_filter_configuration   SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE project_filter_configuration   SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE project_profile_item           SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE project_profile_item           SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE rule                           SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE rule                           SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE sizing_cluster                 SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE sizing_cluster                 SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE sizing_cluster_item            SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE sizing_cluster_item            SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE stage                          SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE stage                          SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE step                           SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE step                           SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE taskboard_issue                SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE taskboard_issue                SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE taskboard_user                 SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE taskboard_user                 SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE team                           SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE team                           SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE team_filter_configuration      SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE team_filter_configuration      SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE template                       SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE template                       SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE user_preferences               SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE user_preferences               SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE user_team                      SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE user_team                      SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;
UPDATE wip_config                     SET created_dt = '2000-01-01 00:00:00' WHERE created_dt IS NULL;
UPDATE wip_config                     SET updated_dt = '2000-01-01 00:00:00' WHERE updated_dt IS NULL;

-- drop old columns
ALTER TABLE taskboard_issue
  DROP COLUMN created, DROP COLUMN updated;
ALTER TABLE team
  DROP COLUMN created_at, DROP COLUMN updated_at;
ALTER TABLE user_team
  DROP COLUMN created_at, DROP COLUMN updated_at;

-- rename column to final name and add NOT NULL constraint
ALTER TABLE change_request
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE filtro
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE followup_daily_synthesis
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE holiday
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE issue_type_visibility
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE lane
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE parent_link_config
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE project_default_team_issuetype
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE project_filter_configuration
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE project_profile_item
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE rule
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE sizing_cluster
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE sizing_cluster_item
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE stage
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE step
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE taskboard_issue
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE taskboard_user
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE team
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE team_filter_configuration
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE template
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE user_preferences
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE user_team
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
ALTER TABLE wip_config
  CHANGE created_dt created DATETIME NOT NULL,
  CHANGE updated_dt updated DATETIME NOT NULL;
