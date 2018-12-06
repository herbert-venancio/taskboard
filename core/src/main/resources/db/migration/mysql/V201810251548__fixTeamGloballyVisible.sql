ALTER TABLE team
  MODIFY globally_visible CHAR default '0';
update team set globally_visible = '0' where globally_visible = 'F';
update team set globally_visible = '1' where globally_visible = 'T';