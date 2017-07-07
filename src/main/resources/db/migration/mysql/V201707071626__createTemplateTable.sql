CREATE TABLE `template` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL UNIQUE,
    `path` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `template_name_unique` (`name`)
)

CREATE TABLE `template_project` (
    `TemplateId` bigint(20) NOT NULL,
    `ProjectId` varchar(255) NOT NULL,
    KEY `templateId_fk` (`TemplateId`),
    KEY `projectId_fk` (`ProjectId`),
    CONSTRAINT `templateId_fk` FOREIGN KEY (`TemplateId`) REFERENCES `template` (`id`),
    CONSTRAINT `projectId_fk` FOREIGN KEY (`ProjectId`) REFERENCES `project_filter_configuration` (`project_key`)
)