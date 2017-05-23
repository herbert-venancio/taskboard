CREATE TABLE `lane` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `ordem` int(11) DEFAULT NULL,
  `show_header` char(1) NOT NULL,
  `show_lane_team` char(1) NOT NULL,
  `show_parent_icon_sint` char(1) DEFAULT NULL,
  `weight` double NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `stage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `ordem` int(11) DEFAULT NULL,
  `show_header` char(1) NOT NULL,
  `weight` double DEFAULT NULL,
  `lane` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `stage_lane_fk` (`lane`),
  CONSTRAINT `stage_lane_fk` FOREIGN KEY (`lane`) REFERENCES `lane` (`id`)
);

CREATE TABLE `step` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `ordem` int(11) DEFAULT NULL,
  `show_header` char(1) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `stage` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `stage_fk` (`stage`),
  CONSTRAINT `stage_fk` FOREIGN KEY (`stage`) REFERENCES `stage` (`id`)
);

CREATE TABLE `filtro` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `issue_type_id` bigint(20) NOT NULL,
  `limit_in_days` varchar(255) DEFAULT NULL,
  `status_id` bigint(20) NOT NULL,
  `step` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `step_fk` (`step`),
  CONSTRAINT `step_fk` FOREIGN KEY (`step`) REFERENCES `step` (`id`)
);

CREATE TABLE `holiday` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `day` datetime NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `issue_type_visibility` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `issue_type_id` bigint(20) NOT NULL,
  `parent_issue_type_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `parent_link_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description_issue_link` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `project_filter_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `project_key_unique` (`project_key`)
);

CREATE TABLE `team_filter_configuration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `team_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `team_id_unique` (`team_id`)
);

CREATE TABLE `project_team` (
  `project_key` varchar(255) NOT NULL,
  `team_id` bigint(20) NOT NULL,
  KEY `project_fk` (`project_key`),
  KEY `team_fk` (`team_id`),
  CONSTRAINT `project_fk` FOREIGN KEY (`project_key`) REFERENCES `project_filter_configuration` (`project_key`),
  CONSTRAINT `team_fk` FOREIGN KEY (`team_id`) REFERENCES `team_filter_configuration` (`team_id`)
);

CREATE TABLE `rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `chave` varchar(255) DEFAULT NULL,
  `valor` varchar(255) DEFAULT NULL,
  `lane` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `rule_lane_fk` (`lane`),
  CONSTRAINT `rule_lane_fk` FOREIGN KEY (`lane`) REFERENCES `lane` (`id`)
);


CREATE TABLE `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coach` varchar(255) DEFAULT NULL,
  `coach_user_name` varchar(255) DEFAULT NULL,
  `created_at` date DEFAULT NULL,
  `jira_equipe` varchar(255) DEFAULT NULL,
  `jira_subequipe` varchar(255) DEFAULT NULL,
  `manager` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `nick_name` varchar(255) DEFAULT NULL,
  `updated_at` date DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `user_preferences` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `jira_user` varchar(2000) DEFAULT NULL,
  `preferences` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `user_team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `is_especificador` int(11) DEFAULT NULL,
  `team` varchar(255) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `wip_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `team` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `wip` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);
