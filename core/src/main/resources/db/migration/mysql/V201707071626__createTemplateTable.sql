CREATE TABLE `template` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `path` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `template_name_unique` (`name`)
);

CREATE TABLE `template_project` (
    `template_id` bigint(20) NOT NULL,
    `project_key` varchar(255) NOT NULL,
    KEY `tp_template_fk` (`template_id`),
    KEY `tp_project_fk` (`project_key`),
    CONSTRAINT `tp_template_fk` FOREIGN KEY (`template_id`) REFERENCES `template` (`id`),
    CONSTRAINT `tp_project_fk` FOREIGN KEY (`project_key`) REFERENCES `project_filter_configuration` (`project_key`)
);