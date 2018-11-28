CREATE TABLE change_request (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    project_id bigint(20) NOT NULL,
    `date` datetime NOT NULL,
    name VARCHAR(200) NOT NULL,
    budget_increase int NOT NULL,
    is_baseline BOOLEAN NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `project_fk` FOREIGN KEY (`project_id`) REFERENCES `project_filter_configuration` (`id`)
);

INSERT INTO change_request(project_id, `date`, name, budget_increase, is_baseline)
     SELECT p.id, p.start_date, 'Baseline', 0, TRUE
       FROM project_filter_configuration as p
            WHERE p.start_date IS NOT NULL;
