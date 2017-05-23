CREATE TABLE TASKBOARD_ISSUE (
	issue_key VARCHAR(255) NOT NULL,
	priority INT NOT NULL,
	CONSTRAINT issueprioritorder_pk PRIMARY KEY (issue_key)
)