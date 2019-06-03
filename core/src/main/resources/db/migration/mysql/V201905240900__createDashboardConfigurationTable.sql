CREATE TABLE `dashboard_configuration` (
    `id` bigint(20) not null auto_increment,
    `project_id` bigint(20) not null,
    `timeline_days_to_display` int not null,
    `created` datetime not null,
    `updated` datetime not null, 
    PRIMARY KEY (`id`),
    CONSTRAINT `dc_project_fk` FOREIGN KEY (`project_id`) REFERENCES `project_filter_configuration` (`id`)
);