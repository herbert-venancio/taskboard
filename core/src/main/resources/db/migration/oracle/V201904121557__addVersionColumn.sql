-- add column `created_dt` and `updated_dt` where it doesn't exist
ALTER TABLE change_request
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE filtro
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE followup_daily_synthesis
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE holiday
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE issue_type_visibility
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE lane
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE parent_link_config
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE project_default_team_issuetype
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE project_filter_configuration
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE project_profile_item
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE rule
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE sizing_cluster
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE sizing_cluster_item
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE stage
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE step
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE taskboard_user
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE team_filter_configuration
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE template
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE user_preferences
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE wip_config
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);

-- add column where it already exist
ALTER TABLE taskboard_issue
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE team
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);
ALTER TABLE user_team
  ADD (created_dt TIMESTAMP, updated_dt TIMESTAMP);

-- copy existing valid data to new columns
UPDATE taskboard_issue SET created_dt = created    WHERE created    IS NOT NULL;
UPDATE taskboard_issue SET updated_dt = updated    WHERE updated    IS NOT NULL;
UPDATE team            SET created_dt = created_at WHERE created_at IS NOT NULL;
UPDATE team            SET updated_dt = updated_at WHERE updated_at IS NOT NULL;
UPDATE user_team       SET created_dt = created_at WHERE created_at IS NOT NULL;
UPDATE user_team       SET updated_dt = updated_at WHERE updated_at IS NOT NULL;

-- ensure no null values exists
UPDATE change_request                 SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE change_request                 SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE filtro                         SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE filtro                         SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE followup_daily_synthesis       SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE followup_daily_synthesis       SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE holiday                        SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE holiday                        SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE issue_type_visibility          SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE issue_type_visibility          SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE lane                           SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE lane                           SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE parent_link_config             SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE parent_link_config             SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE project_default_team_issuetype SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE project_default_team_issuetype SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE project_filter_configuration   SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE project_filter_configuration   SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE project_profile_item           SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE project_profile_item           SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE rule                           SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE rule                           SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE sizing_cluster                 SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE sizing_cluster                 SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE sizing_cluster_item            SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE sizing_cluster_item            SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE stage                          SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE stage                          SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE step                           SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE step                           SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE taskboard_issue                SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE taskboard_issue                SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE taskboard_user                 SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE taskboard_user                 SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE team                           SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE team                           SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE team_filter_configuration      SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE team_filter_configuration      SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE template                       SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE template                       SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE user_preferences               SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE user_preferences               SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE user_team                      SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE user_team                      SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;
UPDATE wip_config                     SET created_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE created_dt IS NULL;
UPDATE wip_config                     SET updated_dt = TO_TIMESTAMP('2000-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS') WHERE updated_dt IS NULL;

-- drop old columns
ALTER TABLE taskboard_issue
  DROP (created, updated);
ALTER TABLE team
  DROP (created_at, updated_at);
ALTER TABLE user_team
  DROP (created_at, updated_at);

-- rename column to final name and add NOT NULL constraint
ALTER TABLE change_request                 RENAME COLUMN created_dt TO created;
ALTER TABLE change_request                 RENAME COLUMN updated_dt TO updated;
ALTER TABLE filtro                         RENAME COLUMN created_dt TO created;
ALTER TABLE filtro                         RENAME COLUMN updated_dt TO updated;
ALTER TABLE followup_daily_synthesis       RENAME COLUMN created_dt TO created;
ALTER TABLE followup_daily_synthesis       RENAME COLUMN updated_dt TO updated;
ALTER TABLE holiday                        RENAME COLUMN created_dt TO created;
ALTER TABLE holiday                        RENAME COLUMN updated_dt TO updated;
ALTER TABLE issue_type_visibility          RENAME COLUMN created_dt TO created;
ALTER TABLE issue_type_visibility          RENAME COLUMN updated_dt TO updated;
ALTER TABLE lane                           RENAME COLUMN created_dt TO created;
ALTER TABLE lane                           RENAME COLUMN updated_dt TO updated;
ALTER TABLE parent_link_config             RENAME COLUMN created_dt TO created;
ALTER TABLE parent_link_config             RENAME COLUMN updated_dt TO updated;
ALTER TABLE project_default_team_issuetype RENAME COLUMN created_dt TO created;
ALTER TABLE project_default_team_issuetype RENAME COLUMN updated_dt TO updated;
ALTER TABLE project_filter_configuration   RENAME COLUMN created_dt TO created;
ALTER TABLE project_filter_configuration   RENAME COLUMN updated_dt TO updated;
ALTER TABLE project_profile_item           RENAME COLUMN created_dt TO created;
ALTER TABLE project_profile_item           RENAME COLUMN updated_dt TO updated;
ALTER TABLE rule                           RENAME COLUMN created_dt TO created;
ALTER TABLE rule                           RENAME COLUMN updated_dt TO updated;
ALTER TABLE sizing_cluster                 RENAME COLUMN created_dt TO created;
ALTER TABLE sizing_cluster                 RENAME COLUMN updated_dt TO updated;
ALTER TABLE sizing_cluster_item            RENAME COLUMN created_dt TO created;
ALTER TABLE sizing_cluster_item            RENAME COLUMN updated_dt TO updated;
ALTER TABLE stage                          RENAME COLUMN created_dt TO created;
ALTER TABLE stage                          RENAME COLUMN updated_dt TO updated;
ALTER TABLE step                           RENAME COLUMN created_dt TO created;
ALTER TABLE step                           RENAME COLUMN updated_dt TO updated;
ALTER TABLE taskboard_issue                RENAME COLUMN created_dt TO created;
ALTER TABLE taskboard_issue                RENAME COLUMN updated_dt TO updated;
ALTER TABLE taskboard_user                 RENAME COLUMN created_dt TO created;
ALTER TABLE taskboard_user                 RENAME COLUMN updated_dt TO updated;
ALTER TABLE team                           RENAME COLUMN created_dt TO created;
ALTER TABLE team                           RENAME COLUMN updated_dt TO updated;
ALTER TABLE team_filter_configuration      RENAME COLUMN created_dt TO created;
ALTER TABLE team_filter_configuration      RENAME COLUMN updated_dt TO updated;
ALTER TABLE template                       RENAME COLUMN created_dt TO created;
ALTER TABLE template                       RENAME COLUMN updated_dt TO updated;
ALTER TABLE user_preferences               RENAME COLUMN created_dt TO created;
ALTER TABLE user_preferences               RENAME COLUMN updated_dt TO updated;
ALTER TABLE user_team                      RENAME COLUMN created_dt TO created;
ALTER TABLE user_team                      RENAME COLUMN updated_dt TO updated;
ALTER TABLE wip_config                     RENAME COLUMN created_dt TO created;
ALTER TABLE wip_config                     RENAME COLUMN updated_dt TO updated;
ALTER TABLE change_request                 MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE filtro                         MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE followup_daily_synthesis       MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE holiday                        MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE issue_type_visibility          MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE lane                           MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE parent_link_config             MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE project_default_team_issuetype MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE project_filter_configuration   MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE project_profile_item           MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE rule                           MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE sizing_cluster                 MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE sizing_cluster_item            MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE stage                          MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE step                           MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE taskboard_issue                MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE taskboard_user                 MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE team                           MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE team_filter_configuration      MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE template                       MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE user_preferences               MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE user_team                      MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
ALTER TABLE wip_config                     MODIFY (created TIMESTAMP NOT NULL, updated TIMESTAMP NOT NULL);
