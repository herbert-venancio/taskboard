alter table user_preferences add (tmp clob);
update user_preferences set tmp = preferences;
alter table user_preferences drop column preferences;
alter table user_preferences rename column tmp to preferences;
alter table user_preferences modify (preferences not null);
