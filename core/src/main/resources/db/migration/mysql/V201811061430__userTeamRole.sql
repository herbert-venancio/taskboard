ALTER TABLE user_team ADD COLUMN role VARCHAR(20);

UPDATE user_team as ut
  JOIN team as t
   SET ut.role = 'MANAGER'
 WHERE ut.user_name = t.manager
   AND ut.team = t.name;

INSERT INTO user_team(team, user_name, role)
     SELECT t.name, t.manager, 'MANAGER'
       FROM team as t
      WHERE t.manager is not null
        AND not exists(
            SELECT t.name, t.manager
              FROM user_team as ut
             WHERE ut.user_name = t.manager
               AND ut.team = t.name
      );

UPDATE user_team as ut
   SET ut.role = 'MEMBER'
 WHERE ut.role is null;

ALTER TABLE user_team MODIFY role VARCHAR(20) NOT NULL;
