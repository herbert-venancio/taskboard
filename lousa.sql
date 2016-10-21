CREATE OR REPLACE FORCE VIEW TASKBOARD.LOUSA_ISSUES AS
  select issue.id,
      project.pkey || '-' || issue.issuenum issueKey,
      project.pkey  projectKey,
      nvl((select project.pkey || '-' || source.issuenum from jirauser.issuelink il
			                join jirauser.issuelinktype lt on lt.id = il.linktype
			                join jirauser.jiraissue source on il.source = source.id
			                join jirauser.project project on project.id = source.project
			            where lt.linkname = 'jira_subtask_link' and il.destination = issue.id),  
			           (select project.pkey || '-' || source.issuenum from jirauser.issuelink il 
			                join jirauser.issuelinktype lt on lt.id = il.linktype
			                join jirauser.jiraissue source on il.source = source.id
			                join jirauser.project project on project.id = source.project
			            where lt.linkname = 'Demanda' and il.destination = issue.id))
			         parent, 
      nvl((select type.id from jirauser.issuelink il
					        join jirauser.issuelinktype lt on lt.id = il.linktype
			                join jirauser.jiraissue source on il.source = source.id
			                join jirauser.project project on project.id = source.project
                            join jirauser.issuetype type on type.id = source.issuetype
			            where lt.linkname = 'jira_subtask_link' and il.destination = issue.id),  
			           (select  type.id from jirauser.issuelink il
			                join jirauser.issuelinktype lt on lt.id = il.linktype
			                join jirauser.jiraissue source on il.source = source.id
			                join jirauser.project project on project.id = source.project
                            join jirauser.issuetype type on type.id = source.issuetype
			             where lt.linkname = 'Demanda' and il.destination = issue.id))
			         parentType,
      project.pname project,
      type.id issuetype,
      issue.summary, 
      (select min(usuario.email_address)
                from jirauser.cwd_user usuario 
                where usuario.user_name = issue.assignee)  subresponsavel1
             , (select min(usuario.email_address)
					   from jirauser.customfieldvalue cfv
                       join jirauser.customfield cf on cf.id = cfv.customfield                           
                       join jirauser.cwd_user usuario on usuario.user_name = cfv.stringvalue
                       where cfv.issue = issue.id
                       and cf.cfname = 'Sub-Responsáveis'
                       and cfv.stringvalue != issue.assignee) subresponsavel2,
      status.id status,
      issue.assignee,
      muser.team_id team,
      (select opt.customvalue
               from jirauser.customfieldvalue cfv 
               join jirauser.customfield cf on cf.id=cfv.customfield
               join jirauser.customfieldoption opt on opt.customfield = cf.id and cfv.stringValue = opt.id
               where cfv.issue=issue.id and cf.cfname='Tamanho'
               ) tamanho,                
      (select jirautils.stragg(nvl(usuario.user_name, ''))
                from jirauser.customfieldvalue cfv
                join jirauser.customfield cf on cf.id = cfv.customfield                           
                join jirauser.cwd_user usuario on usuario.user_name = cfv.stringvalue
                where cfv.issue = issue.id
                and cf.cfname = 'Sub-Responsáveis'
                ) subresponsaveis,
                (select opt.customvalue
               from jirauser.customfieldvalue cfv 
               join jirauser.customfield cf on cf.id=cfv.customfield
               join jirauser.customfieldoption opt on opt.customfield = cf.id and cfv.stringValue = opt.id
               where cfv.issue=issue.id and cf.id=10740
               )  classedeservico,
			      issue.priority, 
			      issue.duedate,
			      issue.description,
    
      (select opt.customvalue
         from jirauser.customfieldvalue cfv 
         join jirauser.customfield cf on cf.id=cfv.customfield
         join jirauser.customfieldoption opt on opt.customfield = cf.id and cfv.stringValue = opt.id
        where cfv.issue=issue.id and cf.id=10170) estimativa
        
      from jirauser.jiraissue issue
	    join jirauser.project project on project.id = issue.project
	    join jirauser.issuetype type on type.id = issue.issuetype
	    join jirauser.issuestatus status on status.id = issue.issuestatus
      left join mad.user_team muser on muser.user_name = issue.assignee and muser.end_date is null
     where status.pname not in ('Fechado', 'Done', 'Cancelado', 'Resolvido');

CREATE TABLE lane ( -- OK
	id NUMBER,
	name VARCHAR(255) NOT NULL,
	ordem NUMBER(2,0),
	weight NUMBER(4,2),
	show_header CHAR(1) DEFAULT ('T'),
	CONSTRAINT lanepk PRIMARY KEY (id)
);

CREATE TABLE rule ( -- OK
	id NUMBER,
	chave VARCHAR(255) NOT NULL,
	valor VARCHAR(255) NOT NULL,
	lane NUMBER,
	CONSTRAINT rulepk PRIMARY KEY (id)
);

CREATE TABLE stage ( -- OK
	id NUMBER,
	name VARCHAR(255)  NOT NULL,
	ordem NUMBER(2,0),
	weight NUMBER(4,2),
	show_header CHAR(1) DEFAULT ('T'),
	color VARCHAR2(7 BYTE) DEFAULT '#CCCCCC',
	lane NUMBER,
	CONSTRAINT stagepk PRIMARY KEY (id),
	CONSTRAINT lanefk FOREIGN KEY (lane) REFERENCES lane (id)
);

CREATE TABLE step ( -- OK
	id NUMBER,
	name VARCHAR(255) NOT NULL,
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
	CONSTRAINT filtropk PRIMARY KEY (id),
	CONSTRAINT filtro_stepfk FOREIGN KEY (step) REFERENCES step (id)
);

CREATE SEQUENCE taskboard_seq 
	INCREMENT BY 1 
	START WITH 1
	NOMAXVALUE
	NOCYCLE;

-- ############## CADASTRO DAS LANES ##############
insert into lane (id, name, ordem, weight, show_header) values (taskboard_seq.nextval, 'Estratégico', 1, 1, 'F');
insert into lane (id, name, ordem, weight, show_header) values (taskboard_seq.nextval, 'Tático', 2, 1, 'F');
insert into lane (id, name, ordem, weight, show_header) values (taskboard_seq.nextval, 'Expedite',	3, 1, 'F');
insert into lane (id, name, ordem, weight, show_header) values (taskboard_seq.nextval, 'Operacional', 4, 5, 'F');
insert into lane (id, name, ordem, weight, show_header) values (taskboard_seq.nextval, 'Continuo',	5, 1, 'F');

-- ############## CADASTRO DAS RULES ##############
insert into rule (id, chave, valor, lane) values(taskboard_seq.nextval, 'priority', '6', 3);
insert into rule (id, chave, valor, lane) values(taskboard_seq.nextval, 'classedeservico', 'Expedite', 3);

-- ############## MIGRAÇÃO DOS STAGES ##############
declare
  cursor c1 is 
  select distinct(tb.stage), tb.level_order, tb.stage_order, tb.stage_weight, tb.color, tb.stage_show_header  from taskboard_config tb order by tb.level_order, tb.stage_order, tb.stage;
  cmd varchar2(320);
begin
    for x in c1 
    loop
		insert into stage (id, name, ordem, weight, color, show_header, lane) 
		values (taskboard_seq.nextval, x.stage, x.stage_order, x.stage_weight, x.color, x.stage_show_header, x.level_order);
    end loop;
end;

-- ############## MIGRAÇÃO DOS STEPS ##############

declare
  cursor c1 is 
  select t.stage, t.level_order lane, t.step, t.step_order, t.step_show_header, t.weight from taskboard_config t;
  cmd varchar2(512);
  
  id_Stage number;
begin
    for x in c1 
    loop
    	id_Stage := null;
      
      begin
        select s.id 
          into id_Stage
             from stage s 
          where s.name = x.stage 
            and s.lane = x.lane;
        exception
            when no_data_found then
                null;
        end;

		insert into step (id, name, ordem, show_header, weight, stage) 
		values (taskboard_seq.nextval, x.step, x.step_order, x.step_show_header, x.weight, id_Stage);
    end loop;
end;

-- ############## MIGRAÇÃO DOS FILTROS ##############
declare
  cursor c1 is 
  select stage, step, issue_type, status, level_desc lane FROM issue_config;
  cmd varchar2(500);
  
  id_IssueType number;
  id_Status number;
  id_Step number;
begin
    for x in c1 
    loop
        id_IssueType := null;
        id_Status := null;
        id_Step := null;
        
        begin
              select it.id 
                 into id_IssueType
               from issue_type it 
             where it.name = x.issue_type;
        exception
            when no_data_found then
                null;
        end;
        
        begin
              select st.id 
                into id_Status
                from status st 
              where st.name = x.status;
        exception
            when no_data_found then
                null;
        end;
        
        begin
              select p.id 
                into id_Step
               from step p 
                 join stage sta on sta.id = p.stage 
                 join lane l on l.id = sta.lane 
             where l.name = x.lane
                and p.name = x.step
                and sta.name = x.stage;
        exception
            when no_data_found then
                null;
        end;

        insert into filtro (id, issue_type, status, step) 
        values (taskboard_seq.nextval, id_IssueType, id_Status, id_Step);
    end loop;
end;


-- CONFIGURAÇÃO DE HIERARQUIAS DE ISSUES

CREATE TABLE issue_type_visibility (
	id INT PRIMARY KEY,
	issue_type_id INT NOT NULL,
	parent_issue_type_id INT
);


-- ALTERAÇÕES PARA USO DE ID AO INVÉS DE NOME
-- REMOÇÃO DE TABELAS REDUNDANTES

ALTER TABLE ISSUE_CONFIG RENAME COLUMN STATUS TO STATUS_NAME;
ALTER TABLE ISSUE_CONFIG ADD STATUS NUMBER;
UPDATE ISSUE_CONFIG IC SET IC.STATUS = (SELECT ID FROM JIRAUSER.ISSUESTATUS WHERE PNAME = IC.STATUS_NAME);
DELETE FROM ISSUE_CONFIG WHERE STATUS IS NULL;
ALTER TABLE ISSUE_CONFIG MODIFY STATUS NUMBER NOT NULL;
ALTER TABLE ISSUE_CONFIG DROP COLUMN STATUS_NAME;

ALTER TABLE ISSUE_CONFIG RENAME COLUMN ISSUE_TYPE TO ISSUE_TYPE_NAME;
ALTER TABLE ISSUE_CONFIG ADD ISSUE_TYPE NUMBER;
UPDATE ISSUE_CONFIG IC SET IC.ISSUE_TYPE = (SELECT ID FROM JIRAUSER.ISSUETYPE WHERE PNAME = IC.ISSUE_TYPE_NAME);
DELETE FROM ISSUE_CONFIG WHERE ISSUE_TYPE IS NULL;
ALTER TABLE ISSUE_CONFIG MODIFY ISSUE_TYPE NUMBER NOT NULL;
ALTER TABLE ISSUE_CONFIG DROP COLUMN ISSUE_TYPE_NAME;

-- CONFIGURACAO DE FILTROS

CREATE TABLE project_filter_configuration (
	id NUMBER NOT NULL,
	project_key VARCHAR2(12) NOT NULL
);

CREATE TABLE team_filter_configuration (
	id NUMBER NOT NULL,
	team_id INT NOT NULL
);

INSERT INTO project_filter_configuration VALUES(taskboard_seq.nextval, 'NET');
INSERT INTO project_filter_configuration VALUES(taskboard_seq.nextval, 'CTE');
INSERT INTO project_filter_configuration VALUES(taskboard_seq.nextval, 'SOD');
INSERT INTO project_filter_configuration VALUES(taskboard_seq.nextval, 'HOR');
INSERT INTO project_filter_configuration VALUES(taskboard_seq.nextval, 'HUGBPO');
INSERT INTO project_filter_configuration VALUES(taskboard_seq.nextval, 'ETRUST');

INSERT INTO team_filter_configuration VALUES(taskboard_seq.nextval, 0);  --Sem time
INSERT INTO team_filter_configuration VALUES(taskboard_seq.nextval, 280);--Boa Sorte
INSERT INTO team_filter_configuration VALUES(taskboard_seq.nextval, 8);  --CST
INSERT INTO team_filter_configuration VALUES(taskboard_seq.nextval, 200);--Garanhani
INSERT INTO team_filter_configuration VALUES(taskboard_seq.nextval, 141);--Guto
INSERT INTO team_filter_configuration VALUES(taskboard_seq.nextval, 13); --SP
INSERT INTO team_filter_configuration VALUES(taskboard_seq.nextval, 145);--Peter
INSERT INTO team_filter_configuration VALUES(taskboard_seq.nextval, 143);--Heron

ALTER TABLE MAD.USER_TEAM ADD START_DATE DATE;-- A pedido do Zulin
ALTER TABLE MAD.USER_TEAM ADD TEAM_ID NUMBER;
UPDATE MAD.USER_TEAM UT SET UT.TEAM_ID = (SELECT ID FROM MAD.TEAM T WHERE T.NAME = UT.TEAM);

-- CRIA TABELA PARA SALVAR CONFIGURAÇÕES PERSONALIZADAS DE FILTRO DO USUARIO

CREATE TABLE USER_PREFERENCES (
	jira_user VARCHAR2(100) NOT NULL,
	preferences VARCHAR2(2000) NOT NULL,
	CONSTRAINT unique_fields UNIQUE (jira_user)
);

--Alterar a tabela de filtro uma coluna de numero limite de dias que irao aparecer ----- OK
ALTER TABLE filtro
  ADD limit_in_days varchar2(45);

--Alterar tamanho das colunas de To Do e Doing no Stage Desenvolvimento na Lane Operacional (Verificar se eh o mesmo id)---- OK
UPDATE step set weight= 1 where id=80;
UPDATE step set weight= 1 where id=81;

UPDATE stage set weight= 2 where id=48;
UPDATE stage set weight= 2 where id=50;

-- Lane Team
ALTER TABLE lane add (show_lane_team char(1) default 'F' not null);
