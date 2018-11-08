ALTER TABLE user_team ADD role VARCHAR(20);

UPDATE user_team
   SET role = 'MANAGER'
 WHERE id in (
    SELECT ut.id
      FROM user_team ut
      JOIN team t ON ut.user_name = t.manager
     WHERE ut.team = t.name
);

INSERT INTO user_team(id, team, user_name, role)
     SELECT HIBERNATE_SEQUENCE.NEXTVAL, t.name, t.manager, 'MANAGER'
       FROM team t
      WHERE t.manager is not null
        AND not exists(
            SELECT t.name, t.manager
              FROM user_team ut
             WHERE ut.user_name = t.manager
               AND ut.team = t.name
      );

UPDATE user_team
   SET role = 'MEMBER'
 WHERE role is null;

ALTER TABLE user_team MODIFY role VARCHAR(20) NOT NULL;
