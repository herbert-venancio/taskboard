CREATE TABLE taskboard_issue (
	issue_key VARCHAR(255) NOT NULL,
	priority BIGINT NOT NULL,
	CONSTRAINT issueprioritorder_pk PRIMARY KEY (issue_key)
)