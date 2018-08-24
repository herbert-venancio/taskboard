-- Lane
Insert into lane (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,SHOW_LANE_TEAM,SHOW_PARENT_ICON_SINT) values (1,'Demand',1,1,'F','F','F');
Insert into lane (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,SHOW_LANE_TEAM,SHOW_PARENT_ICON_SINT) values (2,'Deployable',2,1,'F','F','F');
Insert into lane (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,SHOW_LANE_TEAM,SHOW_PARENT_ICON_SINT) values (3,'Operational',3,1,'F','F','F');

-- Stage
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (4,'Open',1,1,'T','#5d70d4',1);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (5,'To Do',2,1,'T','#5d70d4',1);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (6,'Doing',3,1,'T','#a1423c',1);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (7,'To UAT',4,1,'T','#a1423c',1);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (8,'UATing',5,1,'T','#a1423c',1);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (9,'Done',6,1,'T','#68c3a1',1);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (10,'Open',1,1,'T','#5d70d4',2);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (11,'To Do',2,1,'T','#5d70d4',2);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (12,'Doing',3,1,'T','#a1423c',2);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (13,'To Feature Review',4,1,'T','#a1423c',2);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (14,'Feature Reviewing',5,1,'T','#a1423c',2);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (15,'To QA',6,1,'T','#a1423c',2);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (16,'QAing',7,1,'T','#a1423c',2);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (17,'Done',8,1,'T','#68c3a1',2);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (18,'Open',1,1,'T','#5d70d4',3);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (19,'To Do',2,1,'T','#5d70d4',3);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (20,'Doing',3,1,'T','#a1423c',3);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (21,'To Review',4,1,'T','#a1423c',3);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (22,'Reviewing',5,1,'T','#a1423c',3);
Insert into stage (ID,NAME,ORDEM,WEIGHT,SHOW_HEADER,COLOR,LANE) values (23,'Done',6,1,'T','#68c3a1',3);

-- INSERTING into step
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (24,'Open',1,'F',1,4);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (25,'To Do',1,'F',1,5);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (26,'Doing',1,'F',1,6);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (27,'To UAT',1,'F',1,7);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (28,'UATing',1,'F',1,8);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (29,'Done',1,'F',1,9);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (30,'Open',1,'F',1,10);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (31,'To Do',1,'F',1,11);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (32,'Doing',1,'F',1,12);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (33,'To Feature Review',1,'F',1,13);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (34,'Feature Reviewing',1,'F',1,14);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (35,'To QA',1,'F',1,15);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (36,'QAing',1,'F',1,16);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (37,'Done',1,'F',1,17);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (38,'Open',1,'F',1,18);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (39,'To Do',1,'F',1,19);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (40,'Doing',1,'F',1,20);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (41,'To Review',1,'F',1,21);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (42,'Reviewing',1,'F',1,22);
Insert into step (ID,NAME,ORDEM,SHOW_HEADER,WEIGHT,STAGE) values (43,'Done',1,'F',1,23);

-- INSERTING into team_filter_configuration
Insert into team_filter_configuration (ID,TEAM_ID) values (174,521);
Insert into team_filter_configuration (ID,TEAM_ID) values (175,522);

-- INSERTING into team
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (260,'rafael.zulin@objective.com.br','rafael.zulin','ptrack@objective.com.br','DEVOPS',PARSEDATETIME('27-08-15','DD-MM-yy'),'Maringá','DevOps','Zulin',PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (1,'heron.oyama@objective.com.br','adolpho','ptrack@objective.com.br','CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (2,'anderson.rodrigues@objective.com.br','adolpho','ptrack@objective.com.br','CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (3,'supervisores.ffc@objective.com.br','adolpho','ptrack@objective.com.br','FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (4,'ptrack@objective.com.br','adolpho','ptrack@objective.com.br','ESP_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (5,'glayce.mello@objective.com.br','glayce.mello','glayce.mello@objective.com.br','CST_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('25-08-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (6,'ptrack@objective.com.br','adolpho','ptrack@objective.com.br','ARQ',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('17-03-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (7,'nazar@objective.com.br','adolpho','ptrack@objective.com.br','BRUNDLE',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('17-03-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (8,'marcelo.rezende@objective.com.br','danilo','marcelo.rezende@objective.com.br','CST',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('12-08-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (20,'guilherme@objective.com.br','adolpho','guilherme@objective.com.br','PRODUCAO-ESTAGIARIOS',PARSEDATETIME('07-04-14','DD-MM-yy'),null,null,null,PARSEDATETIME('12-08-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (9,'rcamargo@objective.com.br','adolpho','rcamargo@objective.com.br','INFRA-MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('01-04-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (10,'rcamargo@objective.com.br','adolpho','rcamargo@objective.com.br','INFRA',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('01-04-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (11,'guilherme@objective.com.br','adolpho','guilherme@objective.com.br','PRODUCAO',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('12-08-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (12,'guilherme@objective.com.br','adolpho','guilherme@objective.com.br','PRODUCAO-MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('12-08-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (13,'adolpho@objective.com.br','adolpho','elizabeth@objective.com.br','SP',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,'Adolpho',PARSEDATETIME('25-11-15','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (14,'jclaudio@objective.com.br','gabriel.takeuchi','flm@objective.com.br','OITV-RJ',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('11-04-14','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (15,'elizabeth@objective.com.br','herminio.regilio','jonas@objective.com.br','TORRE-TEL',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('31-03-14','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (140,'herminio.regilio@objective.com.br','herminio.regilio','ptrack@objective.com.br','HERMES',PARSEDATETIME('01-08-14','DD-MM-yy'),'Maringá','Hermes',null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (16,'rafael.zulin@objective.com.br','adolpho','ptrack@objective.com.br','TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (141,'luiz.augusto@objective.com.br','luiz.augusto','ptrack@objective.com.br','GUTO',PARSEDATETIME('01-08-14','DD-MM-yy'),'Maringá','Guto',null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (142,'nathan.ferracini@objective.com.br','nathan.ferracini','ptrack@objective.com.br','NATHAN',PARSEDATETIME('01-08-14','DD-MM-yy'),'Maringá','Nathan',null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (143,'heron.oyama@objective.com.br','heron.oyama','ptrack@objective.com.br','HERON',PARSEDATETIME('01-08-14','DD-MM-yy'),'Maringá','Heron',null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (144,'anderson.rodrigues@objective.com.br','anderson.rodrigues','ptrack@objective.com.br','ANDERSON',PARSEDATETIME('01-08-14','DD-MM-yy'),'Maringá','Anderson',null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (145,'adolpho@objective.com.br','adolpho','ptrack@objective.com.br','HUGHES',PARSEDATETIME('01-08-14','DD-MM-yy'),'Maringá','Peter',null,PARSEDATETIME('14-10-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (160,'egaranhani@objective.com.br','cesar','egaranhani@objective.com.br','CB_PTL',PARSEDATETIME('30-10-14','DD-MM-yy'),null,null,null,PARSEDATETIME('01-09-15','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (200,'egaranhani@objective.com.br','cesar','ramon@objective.com.br','GARANHANI',PARSEDATETIME('10-02-15','DD-MM-yy'),'Curitiba','Garanhani',null,PARSEDATETIME('05-05-15','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (17,'evelyn.silveira@objective.com.br','gabriel.takeuchi','evelyn.silveira@objective.com.br','RH',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('31-03-14','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (18,'caneppele@objective.com.br','adriano','caneppele@objective.com.br','CB',PARSEDATETIME('31-03-14','DD-MM-yy'),'Curitiba','CB',null,PARSEDATETIME('11-02-15','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (19,'humberto@objective.com.br','gabriel.takeuchi','humberto@objective.com.br','ARQ CB',PARSEDATETIME('31-03-14','DD-MM-yy'),null,null,null,PARSEDATETIME('31-03-14','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (180,'felipe.dextro@objective.com.br','felipe.dextro','felipe.dextro@objective.com.br','Dental',PARSEDATETIME('22-01-15','DD-MM-yy'),null,null,null,PARSEDATETIME('22-01-15','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (120,'nodari@objective.com.br','gabriel.takeuchi','nodari@objective.com.br','Agil',PARSEDATETIME('24-06-14','DD-MM-yy'),null,null,null,PARSEDATETIME('24-06-14','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (100,'ptrack@objective.com.br','danilo','ptrack@objective.com.br','CST_DESENVOLVIMENTO',PARSEDATETIME('03-06-14','DD-MM-yy'),'São Paulo',null,null,PARSEDATETIME('08-06-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (220,'marcelo.rezende@objective.com.br','marcelo.rezende','ptrack@objective.com.br','MARCELO REZENDE',PARSEDATETIME('05-05-15','DD-MM-yy'),'Maringá','Marcelo Rezende','MAROMBA',PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (280,'alesandro.balani@objective.com.br','alesandro.balani','ptrack@objective.com.br','OBJECTIVE_SOLUTIONS',PARSEDATETIME('04-09-15','DD-MM-yy'),null,null,null,PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (480,'jorel.cantero@objective.com.br','jorel.cantero','ptrack@objective.com.br','Kangoo Shop',PARSEDATETIME('03-11-16','DD-MM-yy'),null,null,'Kangoo',PARSEDATETIME('03-11-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (500,'leandro.soto@objective.com.br','leandro.soto','leandro.soto@objective.com.br','Up Essencia',PARSEDATETIME('28-11-16','DD-MM-yy'),'Up Essencia',null,null,PARSEDATETIME('28-11-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (460,'davy.duran@objective.com.br','davy.duran','davy.duran@objective.com.br','SODEXO_PORTAL',PARSEDATETIME('29-08-16','DD-MM-yy'),null,null,null,PARSEDATETIME('05-09-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (320,'rcamargo@objective.com.br','adolpho','rcamargo@objective.com.br','INFRA CB',PARSEDATETIME('09-11-15','DD-MM-yy'),null,null,null,PARSEDATETIME('01-04-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (360,'Jorel.cantero@objective.com.br','jorel.cantero','ptrack@objective.com.br','NGNet',PARSEDATETIME('18-04-16','DD-MM-yy'),null,null,'NGNet',PARSEDATETIME('14-10-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (380,'davy.duran@objective.com.br','davy.duran','davy.duran@objective.com.br','LUMIS',PARSEDATETIME('06-05-16','DD-MM-yy'),null,null,'LUMIS',PARSEDATETIME('11-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (440,'ptrack@objective.com.br','peter','ptrack@objective.com.br','POOL',PARSEDATETIME('23-08-16','DD-MM-yy'),null,null,'POOL',PARSEDATETIME('23-08-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (441,'davy.duran@objective.com.br','davy.duran','davy.duran@objective.com.br','DAVY',PARSEDATETIME('23-08-16','DD-MM-yy'),null,null,null,PARSEDATETIME('23-08-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (442,'joel.koppen@objective.com.br','joel.koppen','ptrack@objective.com.br','HUGGY BABY',PARSEDATETIME('23-08-16','DD-MM-yy'),null,null,null,PARSEDATETIME('14-10-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (340,'renato@objective.com.br','renato','olavo@objective.com.br','INFRA ESTAGIARIOS',PARSEDATETIME('23-11-15','DD-MM-yy'),null,null,null,PARSEDATETIME('09-12-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (420,'mlwalter@objective.com.br','mlwalter','ramon@objective.com.br','GOVERNANCA',PARSEDATETIME('11-07-16','DD-MM-yy'),'Governança',null,'GOV',PARSEDATETIME('12-07-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (300,'glayce.mello@objective.com.br','glayce.mello','glayce.mello@objective.com.br','CST_MGA_ESTAGIARIOS',PARSEDATETIME('06-10-15','DD-MM-yy'),null,null,null,PARSEDATETIME('25-08-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (381,'davy.duran@objective.com.br','davy.duran','davy.duran@objective.com.br','INMETRICS',PARSEDATETIME('06-05-16','DD-MM-yy'),null,null,'INMETRICS',PARSEDATETIME('29-06-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (382,'davy.duran@objective.com.br','davy.duran','davy.duran@objective.com.br','SODEXO_LEGADOS',PARSEDATETIME('06-05-16','DD-MM-yy'),null,null,'SODEXO_LEGADOS',PARSEDATETIME('29-06-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (383,'davy.duran@objective.com.br','davy.duran','davy.duran@objective.com.br','SODEXO_BARRAMENTO',PARSEDATETIME('06-05-16','DD-MM-yy'),null,null,'SODEXO_BARRAMENTO',PARSEDATETIME('29-06-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (400,'davy.duran@objective.com.br','davy.duran','davy.duran@objective.com.br','SODEXO_GESTAO',PARSEDATETIME('26-05-16','DD-MM-yy'),null,null,null,PARSEDATETIME('29-06-16','DD-MM-yy'));
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (521,'jean.takano@objective.com.br','gabriel.takeuchi','jean.takano@objective.com.br','TASKBOARD 2',null,null,null,null,null);
Insert into team (ID,COACH,COACH_USER_NAME,MANAGER,NAME,CREATED_AT,JIRA_EQUIPE,JIRA_SUBEQUIPE,NICK_NAME,UPDATED_AT) values (522,'taskboard@objective.com.br','gabriel.takeuchi','taskboard@objective.com.br','TASKBOARD 1',null,null,null,null,null);

-- Insertinto base cluster
INSERT INTO sizing_cluster(id, name) VALUES (1, 'Base Sizing Cluster');
INSERT INTO sizing_cluster(id, name) VALUES (2, 'Second Base Sizing Cluster');
INSERT INTO sizing_cluster(id, name) VALUES (3, 'Third Base Sizing Cluster');

Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (158,'TASKB',522,0.0,0,PARSEDATETIME('29-12-2017','DD-MM-yy'),PARSEDATETIME('28-06-2018','DD-MM-yy'), 1);
Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (159,'PROJ1',521,0.0,0,PARSEDATETIME('01-01-2014','DD-MM-yy'),PARSEDATETIME('01-01-2020','DD-MM-yy'), 1);
Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (160,'PROJ2',521,0.0,0,PARSEDATETIME('01-01-2014','DD-MM-yy'),PARSEDATETIME('01-01-2020','DD-MM-yy'), 1);
Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (161,'PROJ3',521,0.0,0,PARSEDATETIME('01-01-2014','DD-MM-yy'),PARSEDATETIME('01-01-2020','DD-MM-yy'), 1);
Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (162,'PROJ4',521,0.0,0,PARSEDATETIME('01-01-2014','DD-MM-yy'),PARSEDATETIME('01-01-2020','DD-MM-yy'), 1);
Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (163,'PROJ5',521,0.0,0,PARSEDATETIME('01-01-2014','DD-MM-yy'),PARSEDATETIME('01-01-2020','DD-MM-yy'), 1);
Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (164,'PROJ6',521,0.0,0,PARSEDATETIME('01-01-2014','DD-MM-yy'),PARSEDATETIME('01-01-2020','DD-MM-yy'), 1);
Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (165,'PROJ7',521,0.0,0,PARSEDATETIME('01-01-2014','DD-MM-yy'),PARSEDATETIME('01-01-2020','DD-MM-yy'), 1);
Insert into project_filter_configuration (ID,PROJECT_KEY,DEFAULT_TEAM,RISK_PERCENTAGE,IS_ARCHIVED,START_DATE,DELIVERY_DATE, base_cluster_id) values (166,'PROJ8',521,0.0,0,PARSEDATETIME('01-01-2014','DD-MM-yy'),PARSEDATETIME('01-01-2020','DD-MM-yy'), 1);

-- INSERTING into user_team
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1342,PARSEDATETIME('01-08-14','DD-MM-yy'),null,0,'MARCELO REZENDE',PARSEDATETIME('24-08-15','DD-MM-yy'),'evaldo.bratti');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1344,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('13-01-17','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('18-01-17','DD-MM-yy'),'gabriel.hps');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1345,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('15-03-15','DD-MM-yy'),1,'HUGHES',PARSEDATETIME('01-08-14','DD-MM-yy'),'sergio.lacerda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1346,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('05-05-15','DD-MM-yy'),1,'HUGHES',PARSEDATETIME('05-05-15','DD-MM-yy'),'marcelo.rezende');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1347,PARSEDATETIME('01-08-14','DD-MM-yy'),null,1,'HUGHES',PARSEDATETIME('01-08-14','DD-MM-yy'),'rafael.zarpellon');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1348,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('05-05-15','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('05-05-15','DD-MM-yy'),'vinicius.tona');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1349,PARSEDATETIME('01-08-14','DD-MM-yy'),null,1,'HUGHES',PARSEDATETIME('26-08-16','DD-MM-yy'),'thiago.marques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1350,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('01-09-15','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('01-08-14','DD-MM-yy'),'kenneth.becker');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1351,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('14-07-16','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('20-07-16','DD-MM-yy'),'matheus.moraes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1352,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('08-07-16','DD-MM-yy'),0,'GUTO',PARSEDATETIME('12-07-16','DD-MM-yy'),'oswaldo.junior');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1353,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('29-09-15','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('30-09-15','DD-MM-yy'),'carlos.ubialli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (146,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('07-04-14','DD-MM-yy'),1,'CRM1',PARSEDATETIME('07-04-14','DD-MM-yy'),'anderson.rodrigues');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (147,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'bruno.henrique');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (149,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('23-03-13','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'diego.bandoch');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (150,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'diego.prandini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (151,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'eduardo.ghizoni');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (152,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'evaldo.bratti');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (153,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('31-03-13','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'evandro.jose');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (154,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'fernanda.caroline');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (155,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-04-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'fernando.jaques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (156,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('09-11-13','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'fernando.yukio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (157,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-04-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'gabriel.hps');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (158,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('10-04-13','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'geraldo.castro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (159,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('29-07-13','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'guilherme.ferreira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (160,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'herminio.regilio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (161,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('20-01-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'heron.oyama');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (162,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-04-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'joel.koppen');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (163,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'CRM FORCE',PARSEDATETIME('25-07-14','DD-MM-yy'),'jorel.cantero');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (164,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('06-04-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'jorge.filho');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (165,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'leandro.nishijima');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (166,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('19-04-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'lohandus.ribeiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (167,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('05-11-13','DD-MM-yy'),1,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'luiz.augusto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (168,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('15-07-13','DD-MM-yy'),0,'ESP_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'guilherme.anjos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (169,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'luiz.paulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (170,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'luiz.taborda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (171,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'marcelo.rezende');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (172,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('20-12-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'marcelo.tomazini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (173,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'marcelo.zambrana');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (174,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'marcos.ramos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (175,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'marcus.vinicius');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (176,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('07-08-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'marrony.neris');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (177,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('15-07-13','DD-MM-yy'),0,'ESP_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'michel.menegazzo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (178,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'nathan.ferracini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (179,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'otavio.junior');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (180,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('27-11-13','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'luiz.marins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (181,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'ednardo.nobre');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (182,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'rafael.zarpellon');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (183,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('23-02-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'rafael.contessotto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (184,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-06-14','DD-MM-yy'),0,'CST_MGA',PARSEDATETIME('13-10-16','DD-MM-yy'),'huelson.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (185,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('20-12-13','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'renato.fukui');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (186,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('16-02-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'roberson.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (187,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('25-06-13','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'robson');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (188,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('15-04-13','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'rodolfo.mendes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (189,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('10-01-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'rodrigo.daniel');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (190,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('20-12-13','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'rodrigo.nonose');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (191,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-11-13','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'tiago.couto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (192,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('07-08-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'vinicius.tona');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (193,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'wilian.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (194,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('18-03-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'william.vieira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (195,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('08-02-17','DD-MM-yy'),'leticia.fetz');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (196,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('01-02-17','DD-MM-yy'),'lucas.carli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (197,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('19-02-16','DD-MM-yy'),0,'CST',PARSEDATETIME('19-02-16','DD-MM-yy'),'eudazio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (198,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-10-14','DD-MM-yy'),0,'CST',PARSEDATETIME('13-10-14','DD-MM-yy'),'fabio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (199,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('03-06-14','DD-MM-yy'),0,'CST',PARSEDATETIME('31-03-14','DD-MM-yy'),'mbagik');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (200,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'SP',PARSEDATETIME('13-03-15','DD-MM-yy'),'adolpho');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (201,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('14-05-14','DD-MM-yy'),0,'ARQ CB',PARSEDATETIME('14-05-14','DD-MM-yy'),'klaus');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (202,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('11-01-16','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('22-01-16','DD-MM-yy'),'kalecser');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (203,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('27-03-15','DD-MM-yy'),0,'INFRA-MGA',PARSEDATETIME('09-11-15','DD-MM-yy'),'rodrigo.souza');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (204,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('07-04-14','DD-MM-yy'),0,'PRODUCAO',PARSEDATETIME('31-03-14','DD-MM-yy'),'liana.pinheiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (205,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('07-04-14','DD-MM-yy'),0,'PRODUCAO',PARSEDATETIME('31-03-14','DD-MM-yy'),'thiago.santos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (206,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('20-05-16','DD-MM-yy'),0,'CB',PARSEDATETIME('23-05-16','DD-MM-yy'),'grazieli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (207,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('23-03-14','DD-MM-yy'),0,'GARANHANI',PARSEDATETIME('13-04-15','DD-MM-yy'),'caimi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (208,PARSEDATETIME('31-03-14','DD-MM-yy'),null,1,'GARANHANI',PARSEDATETIME('13-02-17','DD-MM-yy'),'cesar');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (209,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('26-12-16','DD-MM-yy'),'laercio.pereira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (210,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-10-14','DD-MM-yy'),0,'CB',PARSEDATETIME('28-10-14','DD-MM-yy'),'lau');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1980,PARSEDATETIME('23-04-15','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('14-02-17','DD-MM-yy'),'ana.vieira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (212,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('30-12-15','DD-MM-yy'),1,'GARANHANI',PARSEDATETIME('20-07-16','DD-MM-yy'),'matias');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (213,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('10-02-15','DD-MM-yy'),'dmachado');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (214,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'OITV-RJ',PARSEDATETIME('31-03-14','DD-MM-yy'),'glaucia.barboza');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (215,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('17-03-16','DD-MM-yy'),0,'OITV-RJ',PARSEDATETIME('17-03-16','DD-MM-yy'),'izabel.santos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (216,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('24-06-14','DD-MM-yy'),0,'RH',PARSEDATETIME('24-06-14','DD-MM-yy'),'ana.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (217,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('24-06-14','DD-MM-yy'),0,'RH',PARSEDATETIME('24-06-14','DD-MM-yy'),'evelyn.silveira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (218,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('24-06-14','DD-MM-yy'),0,'RH',PARSEDATETIME('24-06-14','DD-MM-yy'),'luciana.domenech');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (219,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('24-06-14','DD-MM-yy'),0,'RH',PARSEDATETIME('24-06-14','DD-MM-yy'),'marcela.meleiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1000,PARSEDATETIME('07-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'CRM FORCE',PARSEDATETIME('07-04-14','DD-MM-yy'),'anderson.rodrigues');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1001,PARSEDATETIME('07-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'CRM FORCE',PARSEDATETIME('07-04-14','DD-MM-yy'),'luiz.marins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1002,PARSEDATETIME('07-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('07-04-14','DD-MM-yy'),'rafael.borges');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1020,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('21-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('08-04-16','DD-MM-yy'),'leonam.anjos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1021,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('21-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('23-04-14','DD-MM-yy'),'renato.marteli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1022,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('21-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('19-12-16','DD-MM-yy'),'jean.takano');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1023,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('21-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('28-11-16','DD-MM-yy'),'guilherme.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1024,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('29-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('20-01-15','DD-MM-yy'),'rodrigo.pereira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1026,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('29-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('29-04-14','DD-MM-yy'),'michel.souza');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1027,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('29-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('09-02-17','DD-MM-yy'),'daniel.quesada');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1028,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('29-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('13-10-16','DD-MM-yy'),'caio.martins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1029,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('23-04-14','DD-MM-yy'),'leonam.anjos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1030,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('23-04-14','DD-MM-yy'),'renato.marteli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1031,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('23-04-14','DD-MM-yy'),'jean.takano');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1032,PARSEDATETIME('23-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('23-04-14','DD-MM-yy'),'guilherme.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1080,PARSEDATETIME('29-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('29-04-14','DD-MM-yy'),'rodrigo.pereira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1081,PARSEDATETIME('29-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('29-04-14','DD-MM-yy'),'michel.souza');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1082,PARSEDATETIME('29-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('29-04-14','DD-MM-yy'),'daniel.quesada');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1083,PARSEDATETIME('29-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('29-04-14','DD-MM-yy'),'caio.martins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1354,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('04-03-16','DD-MM-yy'),0,'FFC',PARSEDATETIME('07-03-16','DD-MM-yy'),'gabriel.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1420,PARSEDATETIME('19-08-14','DD-MM-yy'),PARSEDATETIME('01-07-16','DD-MM-yy'),0,'HERON',PARSEDATETIME('12-07-16','DD-MM-yy'),'thiago.lourin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1421,PARSEDATETIME('19-08-14','DD-MM-yy'),PARSEDATETIME('09-11-16','DD-MM-yy'),0,'GUTO',PARSEDATETIME('10-11-16','DD-MM-yy'),'caio.martins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1521,PARSEDATETIME('02-09-14','DD-MM-yy'),null,0,'PRODUCAO',PARSEDATETIME('02-09-14','DD-MM-yy'),'liana.pinheiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1540,PARSEDATETIME('18-09-14','DD-MM-yy'),PARSEDATETIME('01-09-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('23-12-16','DD-MM-yy'),'marcelo.lessa');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1542,PARSEDATETIME('18-09-14','DD-MM-yy'),PARSEDATETIME('24-03-16','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('28-03-16','DD-MM-yy'),'renato.koyama');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1546,PARSEDATETIME('18-09-14','DD-MM-yy'),PARSEDATETIME('30-10-15','DD-MM-yy'),0,'HERON',PARSEDATETIME('22-06-16','DD-MM-yy'),'thiago.cardoso');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1547,PARSEDATETIME('18-09-14','DD-MM-yy'),PARSEDATETIME('06-06-16','DD-MM-yy'),0,'HERON',PARSEDATETIME('12-01-17','DD-MM-yy'),'eduardo.sutil');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1581,PARSEDATETIME('25-09-14','DD-MM-yy'),null,1,'GUTO',PARSEDATETIME('25-09-14','DD-MM-yy'),'luiz.augusto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1582,PARSEDATETIME('25-09-14','DD-MM-yy'),null,1,'ANDERSON',PARSEDATETIME('25-09-14','DD-MM-yy'),'anderson.rodrigues');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1584,PARSEDATETIME('25-09-14','DD-MM-yy'),null,1,'ANDERSON',PARSEDATETIME('02-09-16','DD-MM-yy'),'herminio.regilio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1585,PARSEDATETIME('25-09-14','DD-MM-yy'),null,1,'HUGHES',PARSEDATETIME('30-01-17','DD-MM-yy'),'peter');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1760,PARSEDATETIME('22-01-15','DD-MM-yy'),PARSEDATETIME('13-02-15','DD-MM-yy'),0,'Dental',PARSEDATETIME('13-02-15','DD-MM-yy'),'roberson.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1761,PARSEDATETIME('22-01-15','DD-MM-yy'),PARSEDATETIME('13-02-15','DD-MM-yy'),0,'Dental',PARSEDATETIME('13-02-15','DD-MM-yy'),'leonam.anjos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1840,PARSEDATETIME('03-03-15','DD-MM-yy'),PARSEDATETIME('01-09-15','DD-MM-yy'),0,'HERMES',PARSEDATETIME('04-09-15','DD-MM-yy'),'luiz.paulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1860,PARSEDATETIME('09-03-15','DD-MM-yy'),PARSEDATETIME('15-09-15','DD-MM-yy'),0,'OITV-RJ',PARSEDATETIME('22-09-15','DD-MM-yy'),'cristiano.barroso');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1861,PARSEDATETIME('09-03-15','DD-MM-yy'),PARSEDATETIME('11-05-16','DD-MM-yy'),0,'GUTO',PARSEDATETIME('16-05-16','DD-MM-yy'),'monica.roco');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1880,PARSEDATETIME('12-03-15','DD-MM-yy'),PARSEDATETIME('01-09-15','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('25-01-17','DD-MM-yy'),'alesandro.balani');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1920,PARSEDATETIME('16-03-15','DD-MM-yy'),PARSEDATETIME('18-03-16','DD-MM-yy'),0,'NATHAN',PARSEDATETIME('01-08-16','DD-MM-yy'),'gustavo.vieira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1940,PARSEDATETIME('18-03-15','DD-MM-yy'),PARSEDATETIME('14-10-16','DD-MM-yy'),0,'HERMES',PARSEDATETIME('14-10-16','DD-MM-yy'),'tiago.moreno');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1960,PARSEDATETIME('20-03-15','DD-MM-yy'),PARSEDATETIME('11-07-16','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('23-08-16','DD-MM-yy'),'leandro.corbelo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2000,PARSEDATETIME('05-05-15','DD-MM-yy'),PARSEDATETIME('20-06-16','DD-MM-yy'),0,'MARCELO REZENDE',PARSEDATETIME('23-08-16','DD-MM-yy'),'eduardo.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2002,PARSEDATETIME('05-05-15','DD-MM-yy'),PARSEDATETIME('03-06-15','DD-MM-yy'),0,'MARCELO REZENDE',PARSEDATETIME('08-06-15','DD-MM-yy'),'vinicius.tona');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2003,PARSEDATETIME('05-05-15','DD-MM-yy'),null,1,'MARCELO REZENDE',PARSEDATETIME('05-05-15','DD-MM-yy'),'marcelo.rezende');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (85,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('25-01-17','DD-MM-yy'),'diego.prandini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2100,PARSEDATETIME('25-06-15','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('20-12-16','DD-MM-yy'),'henrique.vignando');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2142,PARSEDATETIME('16-07-15','DD-MM-yy'),PARSEDATETIME('31-12-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('07-03-16','DD-MM-yy'),'fernando.higashi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1100,PARSEDATETIME('02-05-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'FFC',PARSEDATETIME('02-05-14','DD-MM-yy'),'joaocesar');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (220,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-05-13','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'joel.koppen');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (221,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'matheus.moraes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1140,PARSEDATETIME('02-06-14','DD-MM-yy'),PARSEDATETIME('21-11-15','DD-MM-yy'),0,'PRODUCAO',PARSEDATETIME('07-12-15','DD-MM-yy'),'thiago.santos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1160,PARSEDATETIME('03-06-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'CST_DESENVOLVIMENTO',PARSEDATETIME('04-03-15','DD-MM-yy'),'alex');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1161,PARSEDATETIME('03-06-14','DD-MM-yy'),null,0,'MARCELO REZENDE',PARSEDATETIME('13-01-17','DD-MM-yy'),'daniel');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1162,PARSEDATETIME('03-06-14','DD-MM-yy'),PARSEDATETIME('13-03-15','DD-MM-yy'),0,'CST_DESENVOLVIMENTO',PARSEDATETIME('13-03-15','DD-MM-yy'),'felipe.dextro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1163,PARSEDATETIME('03-06-14','DD-MM-yy'),PARSEDATETIME('21-11-14','DD-MM-yy'),0,'CST_DESENVOLVIMENTO',PARSEDATETIME('21-11-14','DD-MM-yy'),'mbagik');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1164,PARSEDATETIME('03-06-14','DD-MM-yy'),null,0,'SP',PARSEDATETIME('23-12-16','DD-MM-yy'),'reginaldo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1165,PARSEDATETIME('03-06-14','DD-MM-yy'),PARSEDATETIME('14-01-15','DD-MM-yy'),0,'CST_DESENVOLVIMENTO',PARSEDATETIME('14-01-15','DD-MM-yy'),'renanoliveira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1166,PARSEDATETIME('03-06-14','DD-MM-yy'),PARSEDATETIME('02-03-16','DD-MM-yy'),0,'SP',PARSEDATETIME('07-03-16','DD-MM-yy'),'rodrigo.medeiros');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1167,PARSEDATETIME('03-06-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'CST_DESENVOLVIMENTO',PARSEDATETIME('04-03-15','DD-MM-yy'),'tanaka');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1281,PARSEDATETIME('30-07-14','DD-MM-yy'),PARSEDATETIME('27-10-14','DD-MM-yy'),0,'CB',PARSEDATETIME('27-10-14','DD-MM-yy'),'gustavo.camargo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1220,PARSEDATETIME('24-06-14','DD-MM-yy'),PARSEDATETIME('18-02-15','DD-MM-yy'),0,'Agil',PARSEDATETIME('27-12-16','DD-MM-yy'),'juliano.ribeiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1280,PARSEDATETIME('30-07-14','DD-MM-yy'),PARSEDATETIME('27-10-14','DD-MM-yy'),0,'CB',PARSEDATETIME('27-10-14','DD-MM-yy'),'felipe');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1282,PARSEDATETIME('30-07-14','DD-MM-yy'),PARSEDATETIME('30-10-14','DD-MM-yy'),0,'CB',PARSEDATETIME('30-10-14','DD-MM-yy'),'luiz.ferreira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1284,PARSEDATETIME('30-07-14','DD-MM-yy'),PARSEDATETIME('06-04-15','DD-MM-yy'),0,'SP',PARSEDATETIME('08-04-15','DD-MM-yy'),'bruno.castagnino');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1285,PARSEDATETIME('30-07-14','DD-MM-yy'),PARSEDATETIME('20-05-15','DD-MM-yy'),0,'CST_DESENVOLVIMENTO',PARSEDATETIME('14-02-17','DD-MM-yy'),'julia.nakashima');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1380,PARSEDATETIME('11-08-14','DD-MM-yy'),PARSEDATETIME('09-06-16','DD-MM-yy'),0,'GUTO',PARSEDATETIME('10-06-16','DD-MM-yy'),'pablo.fassina');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1326,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('16-07-15','DD-MM-yy'),1,'ANDERSON',PARSEDATETIME('16-07-15','DD-MM-yy'),'erison.villegas');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1441,PARSEDATETIME('25-08-14','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('13-01-17','DD-MM-yy'),'adrieli.apolinario');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1442,PARSEDATETIME('25-08-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'ARQ',PARSEDATETIME('03-03-15','DD-MM-yy'),'mario.marques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1443,PARSEDATETIME('25-08-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'ARQ',PARSEDATETIME('03-03-15','DD-MM-yy'),'carolene.bertoldi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1462,PARSEDATETIME('29-08-14','DD-MM-yy'),PARSEDATETIME('18-02-16','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('18-02-16','DD-MM-yy'),'evandro.pepinelli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1480,PARSEDATETIME('01-09-14','DD-MM-yy'),PARSEDATETIME('22-01-15','DD-MM-yy'),0,'HERON',PARSEDATETIME('22-01-15','DD-MM-yy'),'roberson.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1543,PARSEDATETIME('18-09-14','DD-MM-yy'),PARSEDATETIME('19-08-16','DD-MM-yy'),0,'GUTO',PARSEDATETIME('23-08-16','DD-MM-yy'),'jonathan.oliveira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1544,PARSEDATETIME('18-09-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'GUTO',PARSEDATETIME('03-03-15','DD-MM-yy'),'fabricio.noda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1545,PARSEDATETIME('18-09-14','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('21-12-16','DD-MM-yy'),'marcelo.zambrana');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1548,PARSEDATETIME('18-09-14','DD-MM-yy'),PARSEDATETIME('09-03-15','DD-MM-yy'),0,'NATHAN',PARSEDATETIME('22-04-16','DD-MM-yy'),'monica.roco');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1549,PARSEDATETIME('25-09-14','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('09-01-17','DD-MM-yy'),'alan.clappis');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1561,PARSEDATETIME('25-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'douglas.felipe');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1580,PARSEDATETIME('25-09-14','DD-MM-yy'),PARSEDATETIME('17-06-16','DD-MM-yy'),1,'HERON',PARSEDATETIME('23-08-16','DD-MM-yy'),'heron.oyama');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1565,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'fabricio.noda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1566,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'heidi.kussakawa');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1568,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'marcelo.lessa');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1569,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'monica.roco');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1571,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'thiago.cardoso');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1583,PARSEDATETIME('25-09-14','DD-MM-yy'),null,1,'ANDERSON',PARSEDATETIME('02-09-16','DD-MM-yy'),'nathan.ferracini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1620,PARSEDATETIME('07-10-14','DD-MM-yy'),PARSEDATETIME('31-03-16','DD-MM-yy'),0,'ARQ',PARSEDATETIME('25-05-16','DD-MM-yy'),'miyagi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1720,PARSEDATETIME('19-12-14','DD-MM-yy'),PARSEDATETIME('11-08-16','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('12-08-16','DD-MM-yy'),'danilo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1740,PARSEDATETIME('13-01-15','DD-MM-yy'),null,0,'SP',PARSEDATETIME('02-02-17','DD-MM-yy'),'sergio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1941,PARSEDATETIME('18-03-15','DD-MM-yy'),PARSEDATETIME('11-05-16','DD-MM-yy'),0,'NATHAN',PARSEDATETIME('16-05-16','DD-MM-yy'),'renan.bitencourt');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2001,PARSEDATETIME('05-05-15','DD-MM-yy'),PARSEDATETIME('26-08-16','DD-MM-yy'),0,'DAVY',PARSEDATETIME('29-08-16','DD-MM-yy'),'wilian.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2022,PARSEDATETIME('20-05-15','DD-MM-yy'),PARSEDATETIME('11-08-16','DD-MM-yy'),0,'CB',PARSEDATETIME('12-08-16','DD-MM-yy'),'lucas');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2200,PARSEDATETIME('31-07-15','DD-MM-yy'),PARSEDATETIME('01-05-16','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('26-01-17','DD-MM-yy'),'nodari');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2220,PARSEDATETIME('04-08-15','DD-MM-yy'),null,0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('26-09-16','DD-MM-yy'),'anderson.zanichelli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2221,PARSEDATETIME('04-08-15','DD-MM-yy'),PARSEDATETIME('11-05-16','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('16-05-16','DD-MM-yy'),'murillo.massarotto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2240,PARSEDATETIME('11-08-15','DD-MM-yy'),PARSEDATETIME('31-12-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('07-03-16','DD-MM-yy'),'welington.cruz');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2241,PARSEDATETIME('12-08-15','DD-MM-yy'),null,0,'HUGHES',PARSEDATETIME('22-12-16','DD-MM-yy'),'tanaka');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2260,PARSEDATETIME('17-08-15','DD-MM-yy'),null,0,null,PARSEDATETIME('17-08-15','DD-MM-yy'),'ramon');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2280,PARSEDATETIME('26-08-15','DD-MM-yy'),PARSEDATETIME('31-07-16','DD-MM-yy'),0,'HERON',PARSEDATETIME('23-08-16','DD-MM-yy'),'fernando.oliveira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2281,PARSEDATETIME('27-08-15','DD-MM-yy'),PARSEDATETIME('22-11-15','DD-MM-yy'),0,'DEVOPS',PARSEDATETIME('26-11-15','DD-MM-yy'),'gtakeuchi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2320,PARSEDATETIME('03-09-15','DD-MM-yy'),PARSEDATETIME('09-01-16','DD-MM-yy'),0,'DEVOPS',PARSEDATETIME('10-06-16','DD-MM-yy'),'roberson.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2322,PARSEDATETIME('04-09-15','DD-MM-yy'),PARSEDATETIME('15-04-16','DD-MM-yy'),1,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('18-04-16','DD-MM-yy'),'jorel.cantero');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2323,PARSEDATETIME('04-09-15','DD-MM-yy'),PARSEDATETIME('26-08-16','DD-MM-yy'),0,'DAVY',PARSEDATETIME('29-08-16','DD-MM-yy'),'marcus.vinicius');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2324,PARSEDATETIME('04-09-15','DD-MM-yy'),null,0,'HUGHES',PARSEDATETIME('10-11-16','DD-MM-yy'),'marcelo.lessa');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2325,PARSEDATETIME('04-09-15','DD-MM-yy'),PARSEDATETIME('15-04-16','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('18-04-16','DD-MM-yy'),'luiz.paulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2340,PARSEDATETIME('10-09-15','DD-MM-yy'),PARSEDATETIME('22-11-15','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('25-11-15','DD-MM-yy'),'rodrigo.ragioto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2341,PARSEDATETIME('11-09-15','DD-MM-yy'),null,0,'DEVOPS',PARSEDATETIME('13-02-17','DD-MM-yy'),'lucas.kuhlemann');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2300,PARSEDATETIME('31-08-15','DD-MM-yy'),PARSEDATETIME('30-04-16','DD-MM-yy'),0,'HERMES',PARSEDATETIME('09-12-16','DD-MM-yy'),'rodolfo.roza');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2301,PARSEDATETIME('01-09-15','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('01-09-15','DD-MM-yy'),'eric.freitas');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2321,PARSEDATETIME('04-09-15','DD-MM-yy'),PARSEDATETIME('24-03-16','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('28-03-16','DD-MM-yy'),'kenneth.becker');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('21-03-16','DD-MM-yy'),'gabriel.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'fernando.jaques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'vinicius.tona');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (4,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('16-08-16','DD-MM-yy'),'pablo.fassina');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (5,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('07-12-16','DD-MM-yy'),'alexandre.betioli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (6,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('17-10-16','DD-MM-yy'),'deisner.castravechi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (7,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('16-08-16','DD-MM-yy'),'thiago.lourin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (8,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('30-01-17','DD-MM-yy'),'thiago.marques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (9,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('16-05-16','DD-MM-yy'),'oswaldo.junior');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1041,PARSEDATETIME('24-04-14','DD-MM-yy'),PARSEDATETIME('25-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('14-10-16','DD-MM-yy'),'daniel.petrico');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1042,PARSEDATETIME('24-04-14','DD-MM-yy'),PARSEDATETIME('25-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('13-02-17','DD-MM-yy'),'carlos.ubialli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1060,PARSEDATETIME('28-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('28-04-14','DD-MM-yy'),'raphael.vicente');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1061,PARSEDATETIME('28-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('28-04-14','DD-MM-yy'),'daniel.petrico');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1062,PARSEDATETIME('28-04-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('28-04-14','DD-MM-yy'),'carlos.ubialli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1120,PARSEDATETIME('13-05-14','DD-MM-yy'),PARSEDATETIME('03-06-14','DD-MM-yy'),0,'CST',PARSEDATETIME('13-05-14','DD-MM-yy'),'renanoliveira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1121,PARSEDATETIME('13-05-14','DD-MM-yy'),PARSEDATETIME('03-06-14','DD-MM-yy'),0,'CST',PARSEDATETIME('13-05-14','DD-MM-yy'),'felipe.dextro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1122,PARSEDATETIME('13-05-14','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('04-08-15','DD-MM-yy'),'jose.otavio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1123,PARSEDATETIME('13-05-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'GARANHANI',PARSEDATETIME('04-03-15','DD-MM-yy'),'eduardo.ando');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2020,PARSEDATETIME('20-05-15','DD-MM-yy'),null,0,'SP',PARSEDATETIME('10-11-15','DD-MM-yy'),'julia.nakashima');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1360,PARSEDATETIME('04-08-14','DD-MM-yy'),PARSEDATETIME('19-08-14','DD-MM-yy'),0,'HERMES',PARSEDATETIME('04-08-14','DD-MM-yy'),'caio.martins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1362,PARSEDATETIME('04-08-14','DD-MM-yy'),PARSEDATETIME('01-07-15','DD-MM-yy'),0,'HERMES',PARSEDATETIME('01-07-15','DD-MM-yy'),'marcos.ramos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1481,PARSEDATETIME('01-09-14','DD-MM-yy'),PARSEDATETIME('05-05-15','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('05-05-15','DD-MM-yy'),'eduardo.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1520,PARSEDATETIME('02-09-14','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('28-07-15','DD-MM-yy'),'karina.baraldi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1600,PARSEDATETIME('02-10-14','DD-MM-yy'),PARSEDATETIME('20-05-15','DD-MM-yy'),0,'ARQ',PARSEDATETIME('03-08-15','DD-MM-yy'),'celio.hira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1707,PARSEDATETIME('24-11-14','DD-MM-yy'),null,0,null,PARSEDATETIME('13-02-17','DD-MM-yy'),'elizabeth');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1706,PARSEDATETIME('24-11-14','DD-MM-yy'),PARSEDATETIME('18-12-15','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('18-12-15','DD-MM-yy'),'lousa');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1780,PARSEDATETIME('11-02-15','DD-MM-yy'),null,0,null,PARSEDATETIME('14-12-15','DD-MM-yy'),'adriano');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1900,PARSEDATETIME('13-03-15','DD-MM-yy'),null,0,'SP',PARSEDATETIME('13-03-15','DD-MM-yy'),'alexandre');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2021,PARSEDATETIME('20-05-15','DD-MM-yy'),PARSEDATETIME('25-02-16','DD-MM-yy'),0,'CST_DESENVOLVIMENTO',PARSEDATETIME('29-02-16','DD-MM-yy'),'celio.hira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2080,PARSEDATETIME('18-06-15','DD-MM-yy'),PARSEDATETIME('14-06-16','DD-MM-yy'),0,'HERON',PARSEDATETIME('20-07-16','DD-MM-yy'),'ricardo.andrade');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2160,PARSEDATETIME('20-07-15','DD-MM-yy'),PARSEDATETIME('18-03-16','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('21-03-16','DD-MM-yy'),'gabriel.capoia');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (10,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('18-12-13','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'guilherme.pasqualino');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (11,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('25-06-13','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'rodolfo.mendes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (12,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'william.vieira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (13,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'FFC',PARSEDATETIME('25-06-14','DD-MM-yy'),'sergio.lacerda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (14,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('06-01-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('26-10-16','DD-MM-yy'),'joel.koppen');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (15,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'guilherme.anjos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (16,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('13-04-14','DD-MM-yy'),1,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'michel.menegazzo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (17,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('08-09-14','DD-MM-yy'),0,'ARQ',PARSEDATETIME('08-09-14','DD-MM-yy'),'jcn');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (18,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'ARQ',PARSEDATETIME('31-03-14','DD-MM-yy'),'joaopaulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (19,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('10-02-15','DD-MM-yy'),0,'ARQ',PARSEDATETIME('10-02-15','DD-MM-yy'),'peccin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (20,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'ARQ',PARSEDATETIME('31-03-14','DD-MM-yy'),'ziba');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (21,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('31-03-14','DD-MM-yy'),'andre');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (22,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('30-10-14','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('30-10-14','DD-MM-yy'),'gtakeuchi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2040,PARSEDATETIME('27-05-15','DD-MM-yy'),PARSEDATETIME('06-09-16','DD-MM-yy'),0,'GARANHANI',PARSEDATETIME('06-09-16','DD-MM-yy'),'wilson.piasecki');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (24,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'BRUNDLE',PARSEDATETIME('03-08-15','DD-MM-yy'),'lohandus.ribeiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (25,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'BRUNDLE',PARSEDATETIME('10-02-16','DD-MM-yy'),'nazar');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (26,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('03-06-14','DD-MM-yy'),0,'CST',PARSEDATETIME('31-03-14','DD-MM-yy'),'alex');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (27,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('04-08-14','DD-MM-yy'),0,'CST_MGA',PARSEDATETIME('08-09-14','DD-MM-yy'),'ananda.possar');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (28,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('23-06-16','DD-MM-yy'),0,'CST',PARSEDATETIME('27-06-16','DD-MM-yy'),'cristina.fagundes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (29,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('03-02-16','DD-MM-yy'),0,'CST',PARSEDATETIME('16-02-16','DD-MM-yy'),'cyntia');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (30,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('03-06-14','DD-MM-yy'),0,'CST',PARSEDATETIME('19-12-16','DD-MM-yy'),'daniel');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (31,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('15-07-14','DD-MM-yy'),0,'CST_MGA',PARSEDATETIME('08-09-14','DD-MM-yy'),'decio.dianin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (32,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'CST',PARSEDATETIME('04-03-15','DD-MM-yy'),'eduardo.finardi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (33,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('26-01-14','DD-MM-yy'),0,'CST_MGA',PARSEDATETIME('26-05-15','DD-MM-yy'),'erison.villegas');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (34,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('06-11-13','DD-MM-yy'),0,'CST_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'giancarlo.pessatto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (35,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'CST',PARSEDATETIME('13-02-17','DD-MM-yy'),'glayce.mello');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (36,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('19-02-16','DD-MM-yy'),0,'CST',PARSEDATETIME('19-02-16','DD-MM-yy'),'lucilayne');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (37,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('27-03-14','DD-MM-yy'),0,'CST_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'marcelo.barbieri');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (38,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('19-02-16','DD-MM-yy'),0,'CST',PARSEDATETIME('19-02-16','DD-MM-yy'),'patricia.pereira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (39,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-09-16','DD-MM-yy'),0,'CST_MGA',PARSEDATETIME('22-09-16','DD-MM-yy'),'rebeca.claus');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (40,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('03-06-14','DD-MM-yy'),0,'CST',PARSEDATETIME('27-01-17','DD-MM-yy'),'reginaldo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (41,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('03-06-14','DD-MM-yy'),0,'CST',PARSEDATETIME('13-01-16','DD-MM-yy'),'rodrigo.medeiros');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (42,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'CST',PARSEDATETIME('04-03-15','DD-MM-yy'),'rosemari.oliveira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (43,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('03-06-14','DD-MM-yy'),0,'CST',PARSEDATETIME('11-07-16','DD-MM-yy'),'tanaka');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (44,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'INFRA-MGA',PARSEDATETIME('17-12-15','DD-MM-yy'),'dvasconcelos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (45,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'INFRA',PARSEDATETIME('08-01-17','DD-MM-yy'),'renato');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (46,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'INFRA',PARSEDATETIME('04-03-15','DD-MM-yy'),'simone');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (47,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'PRODUCAO',PARSEDATETIME('31-03-14','DD-MM-yy'),'andre.sturiao');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (48,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('10-04-14','DD-MM-yy'),0,'PRODUCAO',PARSEDATETIME('31-03-14','DD-MM-yy'),'grazieli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (49,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'PRODUCAO',PARSEDATETIME('28-09-15','DD-MM-yy'),'guilherme');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (50,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'PRODUCAO-MGA',PARSEDATETIME('04-03-15','DD-MM-yy'),'paulo.rafael');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (51,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('31-03-13','DD-MM-yy'),0,'SP',PARSEDATETIME('15-07-16','DD-MM-yy'),'adolpho');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (52,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'SP',PARSEDATETIME('31-03-14','DD-MM-yy'),'alexandre');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (53,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'SP',PARSEDATETIME('06-12-16','DD-MM-yy'),'camila');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (54,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('24-05-16','DD-MM-yy'),0,'SP',PARSEDATETIME('25-05-16','DD-MM-yy'),'davy.duran');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (55,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('06-10-14','DD-MM-yy'),0,'SP',PARSEDATETIME('07-10-14','DD-MM-yy'),'miyagi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (56,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'SP',PARSEDATETIME('03-05-16','DD-MM-yy'),'mpb');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (57,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'SP',PARSEDATETIME('31-03-14','DD-MM-yy'),'rafael.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (58,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'OITV-RJ',PARSEDATETIME('02-01-17','DD-MM-yy'),'claudio.martins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (59,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'OITV-RJ',PARSEDATETIME('31-03-14','DD-MM-yy'),'daniel.maldonado');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (60,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'OITV-RJ',PARSEDATETIME('31-03-14','DD-MM-yy'),'priscila.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (61,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'OITV-RJ',PARSEDATETIME('31-03-14','DD-MM-yy'),'simone.farias');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (62,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'TORRE-TEL',PARSEDATETIME('13-10-15','DD-MM-yy'),'eudazio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (63,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'TORRE-TEL',PARSEDATETIME('31-03-14','DD-MM-yy'),'fabio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (65,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'TORRE-TEL',PARSEDATETIME('31-03-14','DD-MM-yy'),'sandra');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (66,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('31-03-14','DD-MM-yy'),'felipe.lupepic');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (67,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'renato.fukui');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (68,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('22-04-16','DD-MM-yy'),'willian.hayato');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (69,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('11-02-16','DD-MM-yy'),'kristian.fantin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (71,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'danilo.torres');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (72,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'CRM FORCE',PARSEDATETIME('31-03-14','DD-MM-yy'),'erison.villegas');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (73,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),1,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'joel.koppen');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (222,PARSEDATETIME('07-04-14','DD-MM-yy'),PARSEDATETIME('02-09-14','DD-MM-yy'),0,'PRODUCAO-ESTAGIARIOS',PARSEDATETIME('02-09-14','DD-MM-yy'),'liana.pinheiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (223,PARSEDATETIME('07-04-14','DD-MM-yy'),PARSEDATETIME('02-06-14','DD-MM-yy'),0,'PRODUCAO-ESTAGIARIOS',PARSEDATETIME('07-04-14','DD-MM-yy'),'thiago.santos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1240,PARSEDATETIME('26-06-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('26-06-14','DD-MM-yy'),'huelson.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1400,PARSEDATETIME('13-08-14','DD-MM-yy'),PARSEDATETIME('03-03-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('03-03-15','DD-MM-yy'),'luiz.paulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1500,PARSEDATETIME('01-09-14','DD-MM-yy'),PARSEDATETIME('23-09-14','DD-MM-yy'),0,'CST_DESENVOLVIMENTO',PARSEDATETIME('01-09-14','DD-MM-yy'),'julia.nakashima');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1550,PARSEDATETIME('25-09-14','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('12-12-16','DD-MM-yy'),'douglas.felipe');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1562,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'eduardo.sutil');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1564,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'evandro.pepinelli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1567,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'jonathan.oliveira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1570,PARSEDATETIME('24-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'renato.koyama');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1621,PARSEDATETIME('30-10-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('04-03-15','DD-MM-yy'),'lau');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1623,PARSEDATETIME('30-10-14','DD-MM-yy'),PARSEDATETIME('31-07-15','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('27-08-15','DD-MM-yy'),'gtakeuchi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1800,PARSEDATETIME('13-02-15','DD-MM-yy'),PARSEDATETIME('11-05-16','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('16-05-16','DD-MM-yy'),'leonam.anjos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1801,PARSEDATETIME('13-02-15','DD-MM-yy'),PARSEDATETIME('01-09-15','DD-MM-yy'),0,'HERON',PARSEDATETIME('03-09-15','DD-MM-yy'),'roberson.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1820,PARSEDATETIME('18-02-15','DD-MM-yy'),PARSEDATETIME('01-01-16','DD-MM-yy'),1,'HERON',PARSEDATETIME('11-07-16','DD-MM-yy'),'juliano.ribeiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2060,PARSEDATETIME('01-06-15','DD-MM-yy'),PARSEDATETIME('06-06-16','DD-MM-yy'),0,'GARANHANI',PARSEDATETIME('20-07-16','DD-MM-yy'),'angelo.franco');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (74,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('08-12-16','DD-MM-yy'),'kenneth.becker');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (75,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('09-02-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('22-02-17','DD-MM-yy'),'eduardo.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (76,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('07-04-14','DD-MM-yy'),1,'CRM1',PARSEDATETIME('24-01-17','DD-MM-yy'),'luiz.marins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (77,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('16-11-16','DD-MM-yy'),'gabriel.hps');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (78,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-06-14','DD-MM-yy'),0,'CRM FORCE',PARSEDATETIME('22-05-15','DD-MM-yy'),'joao.bortolozzo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (79,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-12-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'alexandre.betioli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (80,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-01-17','DD-MM-yy'),'anderson.rodrigues');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (81,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('29-09-16','DD-MM-yy'),'bruno.henrique');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (82,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('05-11-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'danilo.torres');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (84,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'diego.bandoch');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (86,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('10-01-17','DD-MM-yy'),'ednardo.nobre');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (87,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('19-04-16','DD-MM-yy'),'eduardo.ghizoni');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (88,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'eduardo.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (89,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('08-02-17','DD-MM-yy'),'evaldo.bratti');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (90,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'evandro.jose');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (91,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('05-11-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'felipe.lupepic');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (92,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'fernanda.caroline');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (93,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'fernando.jaques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (94,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'fernando.yukio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (95,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'gabriel.hps');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (96,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'gabriel.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (97,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'geraldo.castro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (98,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'guilherme.anjos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (99,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'guilherme.ferreira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (100,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('10-03-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'guilherme.pasqualino');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (101,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('26-01-17','DD-MM-yy'),'herminio.regilio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (102,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-06-16','DD-MM-yy'),'heron.oyama');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (103,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('08-12-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'joao.bortolozzo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (104,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'joel.koppen');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (105,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('01-02-17','DD-MM-yy'),'jorel.cantero');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (106,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'jorge.filho');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (107,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'kenneth.becker');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (108,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('05-11-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'kristian.fantin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (109,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('23-12-16','DD-MM-yy'),'leandro.nishijima');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (110,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'lohandus.ribeiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (111,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('27-01-17','DD-MM-yy'),'luiz.augusto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (112,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'luiz.marins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (113,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('14-11-16','DD-MM-yy'),'luiz.paulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (114,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),1,'TREINAMENTO_MGA',PARSEDATETIME('31-10-14','DD-MM-yy'),'luiz.taborda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (115,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('13-01-17','DD-MM-yy'),'marcelo.rezende');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (116,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('06-10-15','DD-MM-yy'),'marcelo.tomazini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (117,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('03-02-17','DD-MM-yy'),'marcelo.zambrana');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (118,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'marcos.ramos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (119,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('25-01-17','DD-MM-yy'),'marcus.vinicius');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (120,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'marrony.neris');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (121,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('13-10-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('25-04-16','DD-MM-yy'),'matheus.moraes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (122,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'michel.menegazzo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (123,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('26-12-16','DD-MM-yy'),'nathan.ferracini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (124,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-12-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'oswaldo.junior');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (125,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('06-02-17','DD-MM-yy'),'otavio.junior');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (126,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-12-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'pablo.fassina');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (127,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('05-11-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'rafael.borges');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (128,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'rafael.contessotto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (129,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('30-08-16','DD-MM-yy'),'rafael.zarpellon');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (130,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'renato.fukui');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (131,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('12-12-16','DD-MM-yy'),'roberson.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (132,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'robson');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (133,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'rodolfo.mendes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (134,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'rodrigo.daniel');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (135,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'rodrigo.nonose');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (136,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'sergio.lacerda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (137,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-12-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'thiago.lourin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (138,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-12-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'thiago.marques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (139,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'tiago.couto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (140,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'vinicius.tona');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (141,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('21-12-16','DD-MM-yy'),'wilian.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (142,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-01-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'william.vieira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (143,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('05-11-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'willian.hayato');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (144,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('01-08-14','DD-MM-yy'),0,'FFC',PARSEDATETIME('31-03-14','DD-MM-yy'),'marcelo.tomazini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (145,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('31-08-14','DD-MM-yy'),0,'ARQ',PARSEDATETIME('01-09-14','DD-MM-yy'),'eduardo.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1260,PARSEDATETIME('24-07-14','DD-MM-yy'),PARSEDATETIME('01-01-15','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('19-03-15','DD-MM-yy'),'lucasmogari');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1302,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('30-05-16','DD-MM-yy'),0,'HERON',PARSEDATETIME('23-08-16','DD-MM-yy'),'jean.takano');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1303,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('30-10-15','DD-MM-yy'),0,'HERON',PARSEDATETIME('30-10-15','DD-MM-yy'),'diego.prandini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1304,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('18-09-14','DD-MM-yy'),0,'HERON',PARSEDATETIME('01-08-14','DD-MM-yy'),'marcelo.zambrana');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1305,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('04-08-14','DD-MM-yy'),0,'HERON',PARSEDATETIME('01-08-14','DD-MM-yy'),'marcos.ramos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1306,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('01-09-15','DD-MM-yy'),0,'HERON',PARSEDATETIME('04-09-15','DD-MM-yy'),'marcus.vinicius');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1307,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('14-07-16','DD-MM-yy'),1,'HERON',PARSEDATETIME('20-07-16','DD-MM-yy'),'eduardo.ghizoni');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1308,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'HERON',PARSEDATETIME('03-03-15','DD-MM-yy'),'rodrigo.pereira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1309,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('04-08-14','DD-MM-yy'),0,'GUTO',PARSEDATETIME('01-08-14','DD-MM-yy'),'michel.souza');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1310,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('12-12-16','DD-MM-yy'),0,'GUTO',PARSEDATETIME('12-12-16','DD-MM-yy'),'daniel.quesada');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1311,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('13-08-14','DD-MM-yy'),0,'GUTO',PARSEDATETIME('01-08-14','DD-MM-yy'),'luiz.paulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1312,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('01-09-15','DD-MM-yy'),1,'GUTO',PARSEDATETIME('04-09-15','DD-MM-yy'),'jorel.cantero');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1313,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('05-05-15','DD-MM-yy'),0,'GUTO',PARSEDATETIME('05-05-15','DD-MM-yy'),'wilian.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1314,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('04-08-14','DD-MM-yy'),0,'GUTO',PARSEDATETIME('01-08-14','DD-MM-yy'),'caio.martins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1315,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('07-06-16','DD-MM-yy'),0,'GUTO',PARSEDATETIME('10-06-16','DD-MM-yy'),'alexandre.betioli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1316,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('22-08-16','DD-MM-yy'),0,'GUTO',PARSEDATETIME('23-08-16','DD-MM-yy'),'huelson.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1317,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('04-07-16','DD-MM-yy'),1,'ANDERSON',PARSEDATETIME('23-08-16','DD-MM-yy'),'luiz.marins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1318,PARSEDATETIME('01-08-14','DD-MM-yy'),null,1,'ANDERSON',PARSEDATETIME('01-08-14','DD-MM-yy'),'rafael.borges');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1319,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('03-03-15','DD-MM-yy'),'renato.marteli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1320,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('18-05-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('20-05-15','DD-MM-yy'),'danilo.torres');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1321,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('03-03-15','DD-MM-yy'),'fernanda.caroline');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1322,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('22-01-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('22-01-15','DD-MM-yy'),'leonam.anjos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1323,PARSEDATETIME('01-08-14','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('01-08-14','DD-MM-yy'),'guilherme.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1324,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('11-08-14','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('01-08-14','DD-MM-yy'),'pablo.fassina');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1325,PARSEDATETIME('01-08-14','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('01-08-14','DD-MM-yy'),'otavio.junior');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1327,PARSEDATETIME('01-08-14','DD-MM-yy'),null,1,'ANDERSON',PARSEDATETIME('02-09-16','DD-MM-yy'),'bruno.henrique');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1328,PARSEDATETIME('01-08-14','DD-MM-yy'),null,1,'ANDERSON',PARSEDATETIME('02-09-16','DD-MM-yy'),'ednardo.nobre');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1329,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('17-10-16','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('17-10-16','DD-MM-yy'),'daniel.petrico');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1330,PARSEDATETIME('01-08-14','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('02-09-16','DD-MM-yy'),'marcelo.tomazini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1331,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('18-05-15','DD-MM-yy'),0,'NATHAN',PARSEDATETIME('20-05-15','DD-MM-yy'),'william.vieira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1332,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('19-08-14','DD-MM-yy'),0,'NATHAN',PARSEDATETIME('01-08-14','DD-MM-yy'),'thiago.lourin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1333,PARSEDATETIME('01-08-14','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('02-09-16','DD-MM-yy'),'leandro.nishijima');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1334,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),1,'NATHAN',PARSEDATETIME('03-03-15','DD-MM-yy'),'luiz.taborda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1335,PARSEDATETIME('01-08-14','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('02-09-16','DD-MM-yy'),'deisner.castravechi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1336,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('13-02-15','DD-MM-yy'),1,'NATHAN',PARSEDATETIME('20-02-15','DD-MM-yy'),'felipe.lupepic');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1337,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('10-12-14','DD-MM-yy'),1,'HERMES',PARSEDATETIME('10-12-14','DD-MM-yy'),'joaocesar');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1338,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('30-04-16','DD-MM-yy'),1,'HERMES',PARSEDATETIME('14-10-16','DD-MM-yy'),'joel.koppen');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1339,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('12-05-16','DD-MM-yy'),0,'HERMES',PARSEDATETIME('16-05-16','DD-MM-yy'),'willian.hayato');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1340,PARSEDATETIME('01-08-14','DD-MM-yy'),PARSEDATETIME('13-04-15','DD-MM-yy'),0,'HERMES',PARSEDATETIME('13-04-15','DD-MM-yy'),'renato.fukui');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2360,PARSEDATETIME('15-09-15','DD-MM-yy'),PARSEDATETIME('13-11-15','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('17-11-15','DD-MM-yy'),'leonardo.terrao');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2361,PARSEDATETIME('16-09-15','DD-MM-yy'),PARSEDATETIME('21-10-16','DD-MM-yy'),1,'POOL',PARSEDATETIME('27-10-16','DD-MM-yy'),'alesandro.balani');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2362,PARSEDATETIME('16-09-15','DD-MM-yy'),PARSEDATETIME('14-07-16','DD-MM-yy'),0,'HERON',PARSEDATETIME('20-07-16','DD-MM-yy'),'eduardo.neto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2363,PARSEDATETIME('17-09-15','DD-MM-yy'),PARSEDATETIME('27-10-15','DD-MM-yy'),0,'MARCELO REZENDE',PARSEDATETIME('29-10-15','DD-MM-yy'),'mateus.loureiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2364,PARSEDATETIME('17-09-15','DD-MM-yy'),PARSEDATETIME('03-11-15','DD-MM-yy'),0,'MARCELO REZENDE',PARSEDATETIME('03-11-15','DD-MM-yy'),'emmanuel.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2380,PARSEDATETIME('21-09-15','DD-MM-yy'),PARSEDATETIME('20-11-15','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('23-11-15','DD-MM-yy'),'andre.fonseca');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2381,PARSEDATETIME('22-09-15','DD-MM-yy'),PARSEDATETIME('14-07-16','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('20-07-16','DD-MM-yy'),'ed.junior');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2400,PARSEDATETIME('29-09-15','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('09-02-17','DD-MM-yy'),'oreles.santiago');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2401,PARSEDATETIME('29-09-15','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('10-02-17','DD-MM-yy'),'joao.lopes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2402,PARSEDATETIME('29-09-15','DD-MM-yy'),PARSEDATETIME('22-09-16','DD-MM-yy'),0,'CST_MGA',PARSEDATETIME('22-09-16','DD-MM-yy'),'angelica.ceole');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2403,PARSEDATETIME('30-09-15','DD-MM-yy'),PARSEDATETIME('04-03-16','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('07-03-16','DD-MM-yy'),'carlos.ubialli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2420,PARSEDATETIME('16-10-15','DD-MM-yy'),PARSEDATETIME('15-04-16','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('13-01-17','DD-MM-yy'),'pedro.iwamoto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2440,PARSEDATETIME('20-10-15','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('20-10-15','DD-MM-yy'),'glayce.mello');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2460,PARSEDATETIME('30-10-15','DD-MM-yy'),PARSEDATETIME('14-07-16','DD-MM-yy'),0,'DEVOPS',PARSEDATETIME('20-07-16','DD-MM-yy'),'thiago.cardoso');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2461,PARSEDATETIME('30-10-15','DD-MM-yy'),PARSEDATETIME('01-07-16','DD-MM-yy'),0,'HERON',PARSEDATETIME('12-07-16','DD-MM-yy'),'diego.prandini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2480,PARSEDATETIME('09-11-15','DD-MM-yy'),PARSEDATETIME('14-07-16','DD-MM-yy'),0,'INFRA CB',PARSEDATETIME('12-08-16','DD-MM-yy'),'francisco.wolf');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2481,PARSEDATETIME('09-11-15','DD-MM-yy'),PARSEDATETIME('02-03-16','DD-MM-yy'),0,'SP',PARSEDATETIME('07-03-16','DD-MM-yy'),'jadson.santos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2482,PARSEDATETIME('09-11-15','DD-MM-yy'),PARSEDATETIME('30-11-16','DD-MM-yy'),0,'SP',PARSEDATETIME('12-12-16','DD-MM-yy'),'guilherme.pereira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2483,PARSEDATETIME('09-11-15','DD-MM-yy'),PARSEDATETIME('18-02-16','DD-MM-yy'),0,'INFRA-MGA',PARSEDATETIME('10-03-16','DD-MM-yy'),'michele.miquelon');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2484,PARSEDATETIME('09-11-15','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('23-01-17','DD-MM-yy'),'jonathan.nascimento');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2500,PARSEDATETIME('23-11-15','DD-MM-yy'),null,0,'INFRA ESTAGIARIOS',PARSEDATETIME('23-11-15','DD-MM-yy'),'joao.molina');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2540,PARSEDATETIME('11-01-16','DD-MM-yy'),null,0,'PRODUCAO',PARSEDATETIME('11-01-16','DD-MM-yy'),'guilherme.mendes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2560,PARSEDATETIME('07-03-16','DD-MM-yy'),null,0,'HUGHES',PARSEDATETIME('13-01-17','DD-MM-yy'),'gabriel.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2561,PARSEDATETIME('07-03-16','DD-MM-yy'),null,0,'MARCELO REZENDE',PARSEDATETIME('07-03-16','DD-MM-yy'),'raphael.vicente');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2562,PARSEDATETIME('07-03-16','DD-MM-yy'),null,0,'MARCELO REZENDE',PARSEDATETIME('07-03-16','DD-MM-yy'),'carlos.ubialli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1541,PARSEDATETIME('18-09-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'HUGHES',PARSEDATETIME('18-09-14','DD-MM-yy'),'heidi.kussakawa');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1040,PARSEDATETIME('24-04-14','DD-MM-yy'),PARSEDATETIME('25-04-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('28-09-15','DD-MM-yy'),'raphael.vicente');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1361,PARSEDATETIME('04-08-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'HERMES',PARSEDATETIME('03-03-15','DD-MM-yy'),'michel.souza');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (64,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('28-03-14','DD-MM-yy'),0,'TORRE-TEL',PARSEDATETIME('31-03-14','DD-MM-yy'),'mbagik');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (70,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('07-04-14','DD-MM-yy'),0,'CRM1',PARSEDATETIME('08-10-15','DD-MM-yy'),'rafael.borges');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1560,PARSEDATETIME('25-09-14','DD-MM-yy'),PARSEDATETIME('21-09-14','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('24-09-14','DD-MM-yy'),'alan.clappis');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (1622,PARSEDATETIME('30-10-14','DD-MM-yy'),PARSEDATETIME('02-03-15','DD-MM-yy'),0,'BRUNDLE',PARSEDATETIME('04-03-15','DD-MM-yy'),'luiz.ferreira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2140,PARSEDATETIME('16-07-15','DD-MM-yy'),PARSEDATETIME('22-09-15','DD-MM-yy'),0,'NATHAN',PARSEDATETIME('22-09-15','DD-MM-yy'),'erison.villegas');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (83,PARSEDATETIME('31-03-14','DD-MM-yy'),PARSEDATETIME('22-12-13','DD-MM-yy'),0,'TREINAMENTO_MGA',PARSEDATETIME('31-03-14','DD-MM-yy'),'deisner.castravechi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2580,PARSEDATETIME('21-03-16','DD-MM-yy'),PARSEDATETIME('11-05-16','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('12-05-16','DD-MM-yy'),'gabriel.capoia');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2581,PARSEDATETIME('21-03-16','DD-MM-yy'),PARSEDATETIME('05-05-16','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('06-05-16','DD-MM-yy'),'gustavo.vieira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2600,PARSEDATETIME('28-03-16','DD-MM-yy'),null,0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('28-03-16','DD-MM-yy'),'renato.koyama');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2601,PARSEDATETIME('28-03-16','DD-MM-yy'),null,0,'HUGHES',PARSEDATETIME('28-03-16','DD-MM-yy'),'kenneth.becker');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2620,PARSEDATETIME('18-04-16','DD-MM-yy'),null,0,'NGNet',PARSEDATETIME('18-04-16','DD-MM-yy'),'luiz.paulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2621,PARSEDATETIME('18-04-16','DD-MM-yy'),null,0,'NGNet',PARSEDATETIME('18-04-16','DD-MM-yy'),'pedro.iwamoto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2622,PARSEDATETIME('18-04-16','DD-MM-yy'),null,1,'NGNet',PARSEDATETIME('18-04-16','DD-MM-yy'),'jorel.cantero');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2640,PARSEDATETIME('06-05-16','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('21-12-16','DD-MM-yy'),'gustavo.vieira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2641,PARSEDATETIME('06-05-16','DD-MM-yy'),null,0,'LUMIS',PARSEDATETIME('06-05-16','DD-MM-yy'),'andre.andrade');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2642,PARSEDATETIME('06-05-16','DD-MM-yy'),null,0,'LUMIS',PARSEDATETIME('06-05-16','DD-MM-yy'),'eder.lumis');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2643,PARSEDATETIME('06-05-16','DD-MM-yy'),PARSEDATETIME('20-10-16','DD-MM-yy'),0,'INMETRICS',PARSEDATETIME('20-10-16','DD-MM-yy'),'douglas.alencar');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2644,PARSEDATETIME('06-05-16','DD-MM-yy'),null,0,'INMETRICS',PARSEDATETIME('06-05-16','DD-MM-yy'),'felipe.marchi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2660,PARSEDATETIME('10-05-16','DD-MM-yy'),PARSEDATETIME('19-10-16','DD-MM-yy'),0,'SODEXO_BARRAMENTO',PARSEDATETIME('19-10-16','DD-MM-yy'),'wagner.ribeiro.ext');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2680,PARSEDATETIME('20-05-16','DD-MM-yy'),null,0,null,PARSEDATETIME('25-08-16','DD-MM-yy'),'maria.fernanda');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2720,PARSEDATETIME('08-06-16','DD-MM-yy'),PARSEDATETIME('22-09-16','DD-MM-yy'),0,'INMETRICS',PARSEDATETIME('22-09-16','DD-MM-yy'),'arthur.glaser');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2701,PARSEDATETIME('25-05-16','DD-MM-yy'),null,0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('25-05-16','DD-MM-yy'),'davy.duran');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2721,PARSEDATETIME('01-04-16','DD-MM-yy'),null,0,'POOL',PARSEDATETIME('23-08-16','DD-MM-yy'),'roberson.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2703,PARSEDATETIME('01-04-16','DD-MM-yy'),null,0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('25-05-16','DD-MM-yy'),'miyagi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2704,PARSEDATETIME('25-05-16','DD-MM-yy'),null,0,'LUMIS',PARSEDATETIME('25-05-16','DD-MM-yy'),'desenv_lumis');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2705,PARSEDATETIME('25-05-16','DD-MM-yy'),null,0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('25-05-16','DD-MM-yy'),'ziba');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2706,PARSEDATETIME('26-05-16','DD-MM-yy'),null,0,'SODEXO_GESTAO',PARSEDATETIME('26-05-16','DD-MM-yy'),'jorge.alexiou');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2707,PARSEDATETIME('26-05-16','DD-MM-yy'),null,0,'SODEXO_GESTAO',PARSEDATETIME('26-05-16','DD-MM-yy'),'arezende');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2708,PARSEDATETIME('26-05-16','DD-MM-yy'),null,0,'SODEXO_GESTAO',PARSEDATETIME('26-05-16','DD-MM-yy'),'sillopes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2709,PARSEDATETIME('26-05-16','DD-MM-yy'),null,0,'SODEXO_LEGADOS',PARSEDATETIME('26-05-16','DD-MM-yy'),'marcel.simonette.ext');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2722,PARSEDATETIME('08-06-16','DD-MM-yy'),PARSEDATETIME('17-06-16','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('24-06-16','DD-MM-yy'),'alexandre.betioli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2723,PARSEDATETIME('10-06-16','DD-MM-yy'),PARSEDATETIME('26-08-16','DD-MM-yy'),0,'DAVY',PARSEDATETIME('29-08-16','DD-MM-yy'),'pablo.fassina');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2740,PARSEDATETIME('24-06-16','DD-MM-yy'),null,0,'Kangoo Shop',PARSEDATETIME('03-11-16','DD-MM-yy'),'alexandre.betioli');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2760,PARSEDATETIME('11-07-16','DD-MM-yy'),null,0,'GOVERNANCA',PARSEDATETIME('11-07-16','DD-MM-yy'),'nodari');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2761,PARSEDATETIME('11-07-16','DD-MM-yy'),null,0,'GOVERNANCA',PARSEDATETIME('11-07-16','DD-MM-yy'),'juliano.ribeiro');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2222,PARSEDATETIME('07-08-15','DD-MM-yy'),null,0,'GOVERNANCA',PARSEDATETIME('11-07-16','DD-MM-yy'),'mlwalter');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2762,PARSEDATETIME('12-07-16','DD-MM-yy'),PARSEDATETIME('26-08-16','DD-MM-yy'),0,'DAVY',PARSEDATETIME('29-08-16','DD-MM-yy'),'diego.prandini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2763,PARSEDATETIME('12-07-16','DD-MM-yy'),PARSEDATETIME('09-09-16','DD-MM-yy'),0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('09-09-16','DD-MM-yy'),'thiago.lourin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2120,PARSEDATETIME('14-07-15','DD-MM-yy'),PARSEDATETIME('16-03-16','DD-MM-yy'),0,'ANDERSON',PARSEDATETIME('20-07-16','DD-MM-yy'),'rafael.zulin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2780,PARSEDATETIME('21-07-16','DD-MM-yy'),PARSEDATETIME('22-09-16','DD-MM-yy'),0,'INMETRICS',PARSEDATETIME('22-09-16','DD-MM-yy'),'rodrigo.versollato');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2800,PARSEDATETIME('26-07-16','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('26-07-16','DD-MM-yy'),'virginia.santos');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2820,PARSEDATETIME('03-08-16','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('24-10-16','DD-MM-yy'),'eduardo.sutil');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2840,PARSEDATETIME('23-08-16','DD-MM-yy'),null,0,'POOL',PARSEDATETIME('23-08-16','DD-MM-yy'),'jean.takano');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2841,PARSEDATETIME('23-08-16','DD-MM-yy'),null,0,'POOL',PARSEDATETIME('23-08-16','DD-MM-yy'),'leandro.corbelo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2842,PARSEDATETIME('23-08-16','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('23-08-16','DD-MM-yy'),'fernando.oliveira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2843,PARSEDATETIME('23-08-16','DD-MM-yy'),PARSEDATETIME('26-08-16','DD-MM-yy'),0,'DAVY',PARSEDATETIME('29-08-16','DD-MM-yy'),'huelson.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2844,PARSEDATETIME('23-08-16','DD-MM-yy'),null,0,'Kangoo Shop',PARSEDATETIME('03-11-16','DD-MM-yy'),'rodolfo.roza');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2845,PARSEDATETIME('23-08-16','DD-MM-yy'),PARSEDATETIME('27-10-16','DD-MM-yy'),0,'HUGGY BABY',PARSEDATETIME('07-11-16','DD-MM-yy'),'eduardo.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2846,PARSEDATETIME('23-08-16','DD-MM-yy'),PARSEDATETIME('26-08-16','DD-MM-yy'),0,'POOL',PARSEDATETIME('29-08-16','DD-MM-yy'),'luiz.marins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2101,PARSEDATETIME('25-06-15','DD-MM-yy'),null,0,'HUGHES',PARSEDATETIME('14-02-17','DD-MM-yy'),'luiz.alves');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2860,PARSEDATETIME('29-08-16','DD-MM-yy'),null,0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('29-08-16','DD-MM-yy'),'diego.prandini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2861,PARSEDATETIME('29-08-16','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('21-12-16','DD-MM-yy'),'huelson.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2862,PARSEDATETIME('29-08-16','DD-MM-yy'),null,0,'OBJECTIVE_SOLUTIONS',PARSEDATETIME('29-08-16','DD-MM-yy'),'marcus.vinicius');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2863,PARSEDATETIME('29-08-16','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('21-12-16','DD-MM-yy'),'pablo.fassina');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2864,PARSEDATETIME('29-08-16','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('21-11-16','DD-MM-yy'),'wilian.ceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2865,PARSEDATETIME('29-08-16','DD-MM-yy'),null,0,'ANDERSON',PARSEDATETIME('24-10-16','DD-MM-yy'),'luiz.marins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2866,PARSEDATETIME('29-08-16','DD-MM-yy'),null,0,'SODEXO_PORTAL',PARSEDATETIME('05-09-16','DD-MM-yy'),'knassif');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2880,PARSEDATETIME('22-09-16','DD-MM-yy'),null,0,'INMETRICS',PARSEDATETIME('22-09-16','DD-MM-yy'),'victor.galceron');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2881,PARSEDATETIME('22-09-16','DD-MM-yy'),null,0,'INMETRICS',PARSEDATETIME('22-09-16','DD-MM-yy'),'gabriel.pereira');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2882,PARSEDATETIME('23-09-16','DD-MM-yy'),null,0,'SODEXO_BARRAMENTO',PARSEDATETIME('23-09-16','DD-MM-yy'),'wilian.azevedo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2900,PARSEDATETIME('26-09-16','DD-MM-yy'),null,0,'SODEXO_BARRAMENTO',PARSEDATETIME('26-09-16','DD-MM-yy'),'sergio.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2901,PARSEDATETIME('26-09-16','DD-MM-yy'),null,0,'SODEXO_GESTAO',PARSEDATETIME('26-09-16','DD-MM-yy'),'andre.bueno');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2902,PARSEDATETIME('26-09-16','DD-MM-yy'),null,0,'SODEXO_GESTAO',PARSEDATETIME('26-09-16','DD-MM-yy'),'vanessa.almeida.ext');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2920,PARSEDATETIME('04-10-16','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('13-02-17','DD-MM-yy'),'guilherme.cardoso');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2921,PARSEDATETIME('05-10-16','DD-MM-yy'),null,0,'INMETRICS',PARSEDATETIME('05-10-16','DD-MM-yy'),'tatiana.almeida');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2922,PARSEDATETIME('05-10-16','DD-MM-yy'),null,0,'INMETRICS',PARSEDATETIME('05-10-16','DD-MM-yy'),'luiz.feliciano');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2923,PARSEDATETIME('06-10-16','DD-MM-yy'),null,1,'HUGHES',PARSEDATETIME('06-10-16','DD-MM-yy'),'rafael.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2980,PARSEDATETIME('24-10-16','DD-MM-yy'),null,0,'SODEXO_GESTAO',PARSEDATETIME('24-10-16','DD-MM-yy'),'marcel.lino');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2981,PARSEDATETIME('27-10-16','DD-MM-yy'),null,0,'HUGHES',PARSEDATETIME('27-10-16','DD-MM-yy'),'alesandro.balani');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3040,PARSEDATETIME('23-11-16','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('26-12-16','DD-MM-yy'),'douglas.borba');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3060,PARSEDATETIME('28-11-16','DD-MM-yy'),null,0,'SP',PARSEDATETIME('28-11-16','DD-MM-yy'),'leandro.soto');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3101,PARSEDATETIME('20-12-16','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('20-12-16','DD-MM-yy'),'fernando.leite');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3103,PARSEDATETIME('21-12-16','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('23-12-16','DD-MM-yy'),'luiz.davantel');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3120,PARSEDATETIME('19-01-17','DD-MM-yy'),null,0,'CST_MGA_ESTAGIARIOS',PARSEDATETIME('13-02-17','DD-MM-yy'),'maria.dalle');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3180,PARSEDATETIME('13-02-17','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('13-02-17','DD-MM-yy'),'herbert.venancio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2960,PARSEDATETIME('17-10-16','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('14-02-17','DD-MM-yy'),'gustavo.silva');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3020,PARSEDATETIME('10-11-16','DD-MM-yy'),null,0,'POOL',PARSEDATETIME('10-11-16','DD-MM-yy'),'caio.martins');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3102,PARSEDATETIME('20-12-16','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('21-12-16','DD-MM-yy'),'giovane.bonifacio');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (2940,PARSEDATETIME('14-10-16','DD-MM-yy'),PARSEDATETIME('28-10-16','DD-MM-yy'),1,'HUGGY BABY',PARSEDATETIME('08-11-16','DD-MM-yy'),'joel.koppen');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3001,PARSEDATETIME('08-11-16','DD-MM-yy'),null,0,'SODEXO_GESTAO',PARSEDATETIME('08-11-16','DD-MM-yy'),'danielle.pessanha');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3140,PARSEDATETIME('23-01-17','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('23-01-17','DD-MM-yy'),'lucas.gemin');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3220,PARSEDATETIME('20-02-17','DD-MM-yy'),null,0,'NGNet',PARSEDATETIME('21-02-17','DD-MM-yy'),'luiz.paulo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3000,PARSEDATETIME('07-11-16','DD-MM-yy'),null,0,'POOL',PARSEDATETIME('07-11-16','DD-MM-yy'),'eduardo.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3080,PARSEDATETIME('12-12-16','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('02-01-17','DD-MM-yy'),'carlos.dugonski');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3100,PARSEDATETIME('20-12-16','DD-MM-yy'),null,0,'GUTO',PARSEDATETIME('21-12-16','DD-MM-yy'),'leonardo.scalabrini');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3160,PARSEDATETIME('10-02-17','DD-MM-yy'),null,0,'CST_MGA',PARSEDATETIME('10-02-17','DD-MM-yy'),'claudio.sakae');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3181,PARSEDATETIME('13-02-17','DD-MM-yy'),null,0,'GARANHANI',PARSEDATETIME('13-02-17','DD-MM-yy'),'hanor.cintra');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3200,PARSEDATETIME('17-02-17','DD-MM-yy'),null,0,'ARQ',PARSEDATETIME('20-02-17','DD-MM-yy'),'eduardo.vasques');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3244,PARSEDATETIME('16-09-15','DD-MM-yy'),null,0,'TASKBOARD 2',PARSEDATETIME('27-10-16','DD-MM-yy'),'jean.takano');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3245,PARSEDATETIME('16-09-15','DD-MM-yy'),null,0,'TASKBOARD 1',PARSEDATETIME('27-10-16','DD-MM-yy'),'taskboard');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3246,PARSEDATETIME('16-09-15','DD-MM-yy'),null,0,'TASKBOARD 2',PARSEDATETIME('27-10-16','DD-MM-yy'),'gtakeuchi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3247,PARSEDATETIME('16-09-15','DD-MM-yy'),null,0,'TASKBOARD 2',PARSEDATETIME('27-10-16','DD-MM-yy'),'jhony.gomes');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3248,PARSEDATETIME('16-09-15','DD-MM-yy'),null,0,'TASKBOARD 2',PARSEDATETIME('27-10-16','DD-MM-yy'),'nazar');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3249,PARSEDATETIME('16-09-15','DD-MM-yy'),null,0,'TASKBOARD 1',PARSEDATETIME('27-10-16','DD-MM-yy'),'foo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3250,PARSEDATETIME('16-09-15','DD-MM-yy'),null,0,'FFC',PARSEDATETIME('27-10-16','DD-MM-yy'),'foo');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3251,PARSEDATETIME('31-03-14','DD-MM-yy'),null,0,'OITV-RJ',PARSEDATETIME('31-03-14','DD-MM-yy'),'gabriel.takeuchi');
Insert into user_team (ID,CREATED_AT,END_DATE,IS_ESPECIFICADOR,TEAM,UPDATED_AT,USER_NAME) values (3252,PARSEDATETIME('16-09-15','DD-MM-yy'),null,0,'TASKBOARD 2',PARSEDATETIME('27-10-16','DD-MM-yy'),'lohandus.ribeiro');

-- INSERTING into holiday
-- delete from holiday;
Insert into holiday (ID,NAME,DAY) values (1,'Natal',PARSEDATETIME('25-12-12','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (180,'Quarta-feira de cinzas (meio periodo)',PARSEDATETIME('18-02-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (220,'Tiradentes',PARSEDATETIME('21-04-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (2,'CARNAVAL',PARSEDATETIME('03-03-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (3,'CARNAVAL',PARSEDATETIME('04-03-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (4,'PONTE CORPUS CHRISTI',PARSEDATETIME('31-05-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (5,'VÉSPERA NATAL',PARSEDATETIME('24-12-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (6,'VÉSPERA ANO NOVO',PARSEDATETIME('31-12-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (7,'ANO NOVO',PARSEDATETIME('01-01-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (100,'Sexta-feira Santa',PARSEDATETIME('18-04-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (101,'Tiradentes',PARSEDATETIME('21-04-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (102,'Dia do trabalho',PARSEDATETIME('01-05-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (140,'Véspera Natal',PARSEDATETIME('24-12-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (143,'Ano novo',PARSEDATETIME('01-01-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (160,'Carnaval-2015',PARSEDATETIME('16-02-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (260,'Corpus Christi',PARSEDATETIME('04-06-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (8,'PONTE CARNAVAL',PARSEDATETIME('11-02-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (9,'SEXTA FEIRA SANTA',PARSEDATETIME('29-03-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (10,'DIA DO TRABALHO',PARSEDATETIME('01-05-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (11,'CORPUS CHRISTI',PARSEDATETIME('30-05-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (12,'PROCLAMAÇÃO DA REPÚBLICA',PARSEDATETIME('15-11-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (13,'NATAL',PARSEDATETIME('25-12-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (14,'ENCONTRO OBJECTIVE',PARSEDATETIME('29-11-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (120,'Corpus Christi',PARSEDATETIME('19-06-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (142,'Véspera Ano Novo',PARSEDATETIME('31-12-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (161,'Carnaval-2015',PARSEDATETIME('17-02-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (240,'Dia do Trabalho',PARSEDATETIME('01-05-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (15,'CARNAVAL',PARSEDATETIME('12-02-13','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (141,'Natal',PARSEDATETIME('25-12-14','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (200,'Sexta-feira santa',PARSEDATETIME('03-04-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (280,'Independência',PARSEDATETIME('07-09-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (360,'Carnaval 2016',PARSEDATETIME('08-02-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (361,'Carnaval 2016',PARSEDATETIME('09-02-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (341,'Ano Novo 2015',PARSEDATETIME('31-12-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (342,'Natal 2015',PARSEDATETIME('25-12-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (343,'Ano Novo 2016',PARSEDATETIME('01-01-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (380,'Sexta-feira santa',PARSEDATETIME('25-03-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (404,'Finados',PARSEDATETIME('02-11-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (405,'Proclamação da República',PARSEDATETIME('15-11-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (320,' Dia de Finados',PARSEDATETIME('02-11-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (340,'Natal 2015',PARSEDATETIME('24-12-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (300,'Padroeira do Brasil',PARSEDATETIME('12-10-15','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (402,'Independência do Brasil',PARSEDATETIME('07-09-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (400,'Tiradentes',PARSEDATETIME('21-04-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (401,'Corpus Christi',PARSEDATETIME('26-05-16','DD-MM-yy'));
Insert into holiday (ID,NAME,DAY) values (403,'Padroeira do Brasil',PARSEDATETIME('12-10-16','DD-MM-yy'));

-- INSERTING into filtro
-- delete from filtro;
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (44,10600,10651,24,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (45,10600,10052,25,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (46,10600,10652,26,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (47,10600,10653,27,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (48,10600,10654,28,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (49,10600,10053,29,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (50,10600,10656,29,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (51,4,10651,30,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (52,4,10052,31,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (53,4,10652,32,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (54,4,10659,32,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (55,4,10660,32,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (56,4,10657,33,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (57,4,10658,34,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (58,4,10661,35,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (59,4,10662,36,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (60,4,10053,37,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (61,4,10656,37,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (62,10601,10651,30,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (63,10601,10052,31,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (64,10601,10652,32,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (65,10601,10659,32,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (66,10601,10660,32,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (67,10601,10657,33,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (68,10601,10658,34,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (69,10601,10661,35,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (70,10601,10662,36,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (71,10601,10053,37,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (72,10601,10656,37,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (73,11,10651,30,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (74,11,10052,31,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (75,11,10652,32,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (76,11,10657,33,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (77,11,10658,34,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (78,11,10053,37,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (79,11,10656,37,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (80,10602,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (81,10602,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (82,10602,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (83,10602,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (84,10602,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (85,10602,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (86,10602,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (87,10603,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (88,10603,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (89,10603,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (90,10603,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (91,10603,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (92,10603,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (93,10603,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (94,10610,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (95,10610,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (96,10610,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (97,10610,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (98,10610,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (99,10610,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (100,10610,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (101,10604,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (102,10604,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (103,10604,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (104,10604,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (105,10604,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (106,10604,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (107,10604,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (108,10606,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (109,10606,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (110,10606,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (111,10606,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (112,10606,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (113,10606,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (114,10606,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (115,10605,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (116,10605,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (117,10605,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (118,10605,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (119,10605,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (120,10605,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (121,10605,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (122,10611,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (123,10611,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (124,10611,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (125,10611,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (126,10611,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (127,10611,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (128,10611,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (129,12,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (130,12,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (131,12,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (132,12,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (133,12,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (134,12,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (135,12,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (136,10607,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (137,10607,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (138,10607,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (139,10607,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (140,10607,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (141,10607,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (142,10607,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (143,10608,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (144,10608,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (145,10608,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (146,10608,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (147,10608,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (148,10608,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (149,10608,10656,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (150,10609,10651,38,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (151,10609,10052,39,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (152,10609,10652,40,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (153,10609,10465,41,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (154,10609,10663,42,null);
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (155,10609,10053,43,'-14d');
Insert into filtro (ID,ISSUE_TYPE_ID,STATUS_ID,STEP,LIMIT_IN_DAYS) values (156,10609,10656,43,'-14d');

-- INSERTING into issue_type_visibility
-- delete from issue_type_visibility;
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (159,10600,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (160,10601,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (161,10604,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (162,10605,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (163,10607,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (164,10609,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (165,10610,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (166,11,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (167,12,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (168,4,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (169,10603,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (170,10602,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (171,10606,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (172,10608,0);
Insert into issue_type_visibility (ID,ISSUE_TYPE_ID,PARENT_ISSUE_TYPE_ID) values (173,10611,0);

-- INSERTING into parent_link_config
-- delete from parent_link_config;
Insert into parent_link_config (ID,DESCRIPTION_ISSUE_LINK) values (157,'is demanded by');

-- INSERTING into user_preferences
-- delete from user_preferences;
Insert into user_preferences (JIRA_USER,PREFERENCES) values ('diego.prandini','{"levelPreferences":[],"filterPreferences":{"4":true,"11":true,"12":true,"10600":true,"10601":true,"10602":true,"10603":true,"10604":true,"10605":true,"10606":true,"10607":true,"10608":true,"10609":true,"10610":true,"10611":true,"TASKB":true,"TASKBOARD 1":true,"TASKBOARD 2":true},"visibilityConfiguration":{"showSynthetic":false},"laneConfiguration":[{"showCount":false}]}');
Insert into user_preferences (JIRA_USER,PREFERENCES) values ('taskboard','{"levelPreferences":[{"level":"Demand","showLevel":true,"showLaneTeam":false,"showHeader":false,"weightLevel":1.2592900752850273},{"level":"Deployable","showLevel":true,"showLaneTeam":false,"showHeader":false,"weightLevel":0.8025279575128927},{"level":"Operational","showLevel":true,"showLaneTeam":false,"showHeader":false,"weightLevel":0.93818196720208}],"filterPreferences":{"4":true,"11":true,"12":true,"10600":true,"10601":true,"10602":true,"10603":true,"10604":true,"10605":true,"10606":true,"10607":true,"10608":true,"10609":true,"10610":true,"10611":true,"TASKB":true,"TASKBOARD 1":true,"TASKBOARD 2":true},"visibilityConfiguration":{"showSynthetic":false},"laneConfiguration":[{"showCount":true}]}');
Insert into user_preferences (JIRA_USER,PREFERENCES) values ('jean.takano','{"levelPreferences":[],"filterPreferences":{"4":true,"11":true,"12":true,"10600":true,"10601":true,"10602":true,"10603":true,"10604":true,"10605":true,"10606":true,"10607":true,"10608":true,"10609":true,"10610":true,"10611":true,"TASKB":true,"TASKBOARD 1":true,"TASKBOARD 2":true},"visibilityConfiguration":{"showSynthetic":false},"laneConfiguration":[{"showCount":false}]}');

-- INSERTING INTO project_team_by_issue_type
INSERT INTO project_default_team_issuetype (id, project_id, team_id, issue_type_id) VALUES (1, 158, 522, 4);
INSERT INTO project_default_team_issuetype (id, project_id, team_id, issue_type_id) VALUES (2, 158, 522, 11);
INSERT INTO project_default_team_issuetype (id, project_id, team_id, issue_type_id) VALUES (3, 158, 521, 12);
INSERT INTO project_default_team_issuetype (id, project_id, team_id, issue_type_id) VALUES (4, 160, 260, 10611);

-- INSERTING INTO sizing_cluster_item for dashboards
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (211,'Alpha Test','notused','XS',1.060,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (212,'Alpha Test','notused','S',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (213,'Alpha Test','notused','M',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (214,'Alpha Test','notused','L',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (215,'Alpha Test','notused','XL',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (216,'Alpha Bug','notused','XS',1.060,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (217,'Alpha Bug','notused','S',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (218,'Alpha Bug','notused','M',3.190,3.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (219,'Alpha Bug','notused','L',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (220,'Alpha Bug','notused','XL',5.320,6.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (221,'Backend Development','notused','XS',4.000,4.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (222,'Backend Development','notused','S',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (223,'Backend Development','notused','M',24.000,28.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (224,'Backend Development','notused','L',40.000,48.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (225,'Backend Development','notused','XL',48.000,57.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (226,'Feature Planning','notused','XS',1.060,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (227,'Feature Planning','notused','S',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (228,'Feature Planning','notused','M',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (229,'Feature Planning','notused','L',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (230,'Feature Planning','notused','XL',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (231,'Feature Review','notused','XS',1.060,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (232,'Feature Review','notused','S',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (233,'Feature Review','notused','M',3.190,3.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (234,'Feature Review','notused','L',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (235,'Feature Review','notused','XL',8.000,9.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (236,'Frontend Development','notused','XS',4.000,4.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (237,'Frontend Development','notused','S',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (238,'Frontend Development','notused','M',24.000,28.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (239,'Frontend Development','notused','L',40.000,48.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (240,'Frontend Development','notused','XL',48.000,57.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (241,'Sub-Task','notused','XS',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (242,'Sub-Task','notused','S',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (243,'Sub-Task','notused','M',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (244,'Sub-Task','notused','L',17.020,20.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (245,'Sub-Task','notused','XL',35.000,42.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (246,'Tech Planning','notused','XS',1.060,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (247,'Tech Planning','notused','S',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (248,'Tech Planning','notused','M',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (249,'Tech Planning','notused','L',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (250,'Tech Planning','notused','XL',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (251,'UAT','notused','XS',2.000,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (252,'UAT','notused','S',4.000,4.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (253,'UAT','notused','M',6.000,7.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (254,'UAT','notused','L',10.000,12.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (255,'UAT','notused','XL',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (256,'UX','notused','XS',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (257,'UX','notused','S',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (258,'UX','notused','M',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (259,'UX','notused','L',17.020,20.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (260,'UX','notused','XL',35.000,42.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (261,'QA','notused','XS',2.000,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (262,'QA','notused','S',4.000,4.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (263,'QA','notused','M',6.000,7.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (264,'QA','notused','L',10.000,12.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (265,'QA','notused','XL',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (266,'BALLPARK - Planning','notused','XS',1.060,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (267,'BALLPARK - Planning','notused','S',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (268,'BALLPARK - Planning','notused','M',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (269,'BALLPARK - Planning','notused','L',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (270,'BALLPARK - Planning','notused','XL',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (271,'BALLPARK - Feature Review','notused','XS',1.000,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (272,'BALLPARK - Feature Review','notused','S',2.000,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (273,'BALLPARK - Feature Review','notused','M',3.000,3.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (274,'BALLPARK - Feature Review','notused','L',5.000,6.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (275,'BALLPARK - Feature Review','notused','XL',8.000,9.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (276,'BALLPARK - UX','notused','XS',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (277,'BALLPARK - UX','notused','S',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (278,'BALLPARK - UX','notused','M',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (279,'BALLPARK - UX','notused','L',17.020,20.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (280,'BALLPARK - UX','notused','XL',35.000,42.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (281,'BALLPARK - Tech Planning','notused','XS',1.060,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (282,'BALLPARK - Tech Planning','notused','S',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (283,'BALLPARK - Tech Planning','notused','M',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (284,'BALLPARK - Tech Planning','notused','L',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (285,'BALLPARK - Tech Planning','notused','XL',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (286,'BALLPARK - Development','notused','XS',4.000,4.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (287,'BALLPARK - Development','notused','S',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (288,'BALLPARK - Development','notused','M',24.000,28.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (289,'BALLPARK - Development','notused','L',40.000,48.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (290,'BALLPARK - Development','notused','XL',48.000,57.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (291,'BALLPARK - Alpha Test','notused','XS',1.060,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (292,'BALLPARK - Alpha Test','notused','S',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (293,'BALLPARK - Alpha Test','notused','M',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (294,'BALLPARK - Alpha Test','notused','L',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (295,'BALLPARK - Alpha Test','notused','XL',16.000,19.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (296,'BALLPARK - Developer Support','notused','XS',1.000,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (297,'BALLPARK - Developer Support','notused','S',2.000,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (298,'BALLPARK - Developer Support','notused','M',3.000,3.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (299,'BALLPARK - Developer Support','notused','L',5.000,6.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (300,'BALLPARK - Developer Support','notused','XL',8.000,9.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (301,'BALLPARK - QA/UAT Support','notused','XS',1.000,1.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (302,'BALLPARK - QA/UAT Support','notused','S',2.000,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (303,'BALLPARK - QA/UAT Support','notused','M',3.000,3.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (304,'BALLPARK - QA/UAT Support','notused','L',5.000,6.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (305,'BALLPARK - QA/UAT Support','notused','XL',8.000,9.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (306,'BALLPARK - Subtask','notused','XS',2.130,2.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (307,'BALLPARK - Subtask','notused','S',4.260,5.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (308,'BALLPARK - Subtask','notused','M',8.510,10.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (309,'BALLPARK - Subtask','notused','L',17.020,20.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (310,'BALLPARK - Subtask','notused','XL',35.000,42.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (311,'BALLPARK - Demand','notused','XS',30.000,45.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (312,'BALLPARK - Demand','notused','S',80.000,120.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (313,'BALLPARK - Demand','notused','M',120.000,180.000,'TASKB', null);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (314,'BALLPARK - Demand','notused','L',240.000,360.000,'TASKB', null);

INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (315,'BALLPARK - Subtask','notused','XL',35.000,42.000, null, 1);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (316,'BALLPARK - Demand','notused','XS',30.000,45.000, null, 1);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (317,'BALLPARK - Demand','notused','S',80.000,120.000, null, 1);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (318,'BALLPARK - Demand','notused','M',120.000,180.000, null, 1);
INSERT INTO sizing_cluster_item (id, subtask_type_name, parent_type_name, sizing, effort, cycle, project_key, base_cluster_id)
     VALUES (319,'BALLPARK - Demand','notused','L',240.000,360.000, null, 1);
