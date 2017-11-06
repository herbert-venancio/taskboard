alter table filtro modify step bigint(20) not null;
alter table filtro add constraint filtro_issuetype_status unique index(issue_type_id, status_id);