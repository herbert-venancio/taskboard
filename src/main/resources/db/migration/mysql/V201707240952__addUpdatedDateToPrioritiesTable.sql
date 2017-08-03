ALTER TABLE taskboard_issue ADD column created TIMESTAMP default CURRENT_TIMESTAMP;

ALTER TABLE taskboard_issue ADD column updated TIMESTAMP default CURRENT_TIMESTAMP;