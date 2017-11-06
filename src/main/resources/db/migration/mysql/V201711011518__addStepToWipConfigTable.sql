alter table wip_config add column step bigint(20);
alter table wip_config add constraint `wip_config_step_fk` foreign key (`step`) references step (id);

alter table wip_config modify status varchar(255);