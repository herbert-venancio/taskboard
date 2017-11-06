alter table filtro modify (issue_type_id not null);
alter table filtro modify (status_id not null);
alter table filtro modify (step not null);

alter table filtro add constraint filtro_issuetype_status unique (issue_type_id, status_id);