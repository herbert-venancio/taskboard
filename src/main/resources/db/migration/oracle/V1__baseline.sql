CREATE TABLE LANE (
    ID                    NUMBER,
    NAME                  VARCHAR2(255 CHAR) NOT NULL ENABLE,
    ORDEM                 NUMBER(2,0),
    WEIGHT                NUMBER(4,2),
    SHOW_HEADER           CHAR(1 CHAR) DEFAULT ('T'),
    SHOW_LANE_TEAM        CHAR(1 CHAR) DEFAULT 'F' NOT NULL ENABLE,
    SHOW_PARENT_ICON_SINT CHAR(1 CHAR),
    CONSTRAINT LANEPK PRIMARY KEY (ID) 
);

CREATE TABLE PARENT_LINK_CONFIG (
    ID                     NUMBER NOT NULL ENABLE,
    DESCRIPTION_ISSUE_LINK VARCHAR2(45 BYTE) NOT NULL ENABLE
);


  
CREATE TABLE rule ( -- OK
	id NUMBER,
	chave VARCHAR(255 CHAR) NOT NULL,
	valor VARCHAR(255 CHAR) NOT NULL,
	lane NUMBER,
	CONSTRAINT rulepk PRIMARY KEY (id)
);

CREATE TABLE stage ( -- OK
	id NUMBER,
	name VARCHAR(255 CHAR)  NOT NULL,
	ordem NUMBER(2,0),
	weight NUMBER(4,2),
	show_header CHAR(1  CHAR) DEFAULT ('T'),
	color VARCHAR2(7 CHAR) DEFAULT '#CCCCCC',
	lane NUMBER,
	CONSTRAINT stagepk PRIMARY KEY (id),
	CONSTRAINT lanefk FOREIGN KEY (lane) REFERENCES lane (id)
);

CREATE TABLE step ( -- OK
	id NUMBER,
	name VARCHAR(255 CHAR) NOT NULL,
	ordem NUMBER(2,0),
	show_header CHAR(1 BYTE) DEFAULT ('T'),
	weight NUMBER(4,2),
	stage NUMBER,
	CONSTRAINT steppk PRIMARY KEY (id),
	CONSTRAINT stagefk FOREIGN KEY (stage) REFERENCES stage (id)
);

CREATE TABLE filtro ( -- OK
	id NUMBER NOT NULL,
	issue_type_id NUMBER,
	status_id NUMBER,
	step NUMBER,
	LIMIT_IN_DAYS VARCHAR2(45 CHAR),	
	CONSTRAINT filtropk PRIMARY KEY (id),
	CONSTRAINT filtro_stepfk FOREIGN KEY (step) REFERENCES step (id)
);

CREATE SEQUENCE taskboard_seq 
	INCREMENT BY 1 
	START WITH 1
	NOMAXVALUE
	NOCYCLE;

-- CONFIGURAÇÃO DE HIERARQUIAS DE ISSUES

CREATE TABLE issue_type_visibility (
	id NUMBER PRIMARY KEY,
	issue_type_id NUMBER NOT NULL,
	parent_issue_type_id INT
);


-- CONFIGURACAO DE FILTROS
CREATE TABLE PROJECT_FILTER_CONFIGURATION (
    ID          NUMBER NOT NULL ENABLE,
    PROJECT_KEY VARCHAR2(12 CHAR) NOT NULL ENABLE,
    CONSTRAINT "PROJECT_KEY_UNIQUE" UNIQUE ("PROJECT_KEY")
);

CREATE TABLE team_filter_configuration (
	id NUMBER NOT NULL,
	team_id NUMBER NOT NULL,
	CONSTRAINT "TEAM_ID_UNIQUE" UNIQUE ("TEAM_ID")
);

CREATE TABLE PROJECT_TEAM (
    PROJECT_KEY VARCHAR2(255 CHAR) NOT NULL ENABLE,
    TEAM_ID     NUMBER(38,0) NOT NULL ENABLE,
    CONSTRAINT "PROJECT_FK" FOREIGN KEY ("PROJECT_KEY") REFERENCES "PROJECT_FILTER_CONFIGURATION" ("PROJECT_KEY") ENABLE,
    CONSTRAINT "TEAM_FK" FOREIGN KEY ("TEAM_ID") REFERENCES "TEAM_FILTER_CONFIGURATION" ("TEAM_ID") ENABLE
);

CREATE TABLE USER_PREFERENCES (
    JIRA_USER   VARCHAR2(100 CHAR) NOT NULL ENABLE,
	PREFERENCES VARCHAR2(2000 CHAR) NOT NULL ENABLE,
	CONSTRAINT "UNIQUE_FIELDS" UNIQUE ("JIRA_USER") 
);
  
CREATE TABLE WIP_CONFIG ( 
    "ID"     NUMBER NOT NULL ENABLE,
    "TEAM"   VARCHAR2(255 CHAR) NOT NULL ENABLE,
    "STATUS" VARCHAR2(255 CHAR) NOT NULL ENABLE,
    "WIP"    NUMBER NOT NULL ENABLE,
    PRIMARY KEY ("ID")
);

CREATE TABLE USER_TEAM (
  id NUMBER NOT NULL,
  created_at date,
  end_date date,
  is_especificador NUMBER DEFAULT 0,
  team varchar(255 CHAR),
  updated_at date,
  user_name varchar(255 CHAR),
  PRIMARY KEY (id)
);

CREATE TABLE HOLIDAY (
    ID NUMBER NOT NULL,
    DAY DATE NOT NULL ENABLE,
    NAME VARCHAR2(50 CHAR) NOT NULL ENABLE,
    PRIMARY KEY ("ID")
);

CREATE TABLE TEAM (
    ID                 NUMBER,
    COACH              VARCHAR2(255 CHAR) NOT NULL ENABLE,
    COACH_USER_NAME    VARCHAR2(255 CHAR) NOT NULL ENABLE,
    MANAGER            VARCHAR2(255 CHAR) NOT NULL ENABLE,
    NAME               VARCHAR2(255 CHAR),
    CREATED_AT         DATE,
    JIRA_EQUIPE        VARCHAR2(255 CHAR),
    JIRA_SUBEQUIPE     VARCHAR2(255 CHAR),
    NICK_NAME          VARCHAR2(50 CHAR),
    UPDATED_AT         DATE,
    CONSTRAINT "TEAM_NAME_UNIQUE" UNIQUE ("NAME"),
    PRIMARY KEY ("ID")
);
