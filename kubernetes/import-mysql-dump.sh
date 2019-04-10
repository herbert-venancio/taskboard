#!/bin/bash

kubectl port-forward --namespace taskboard-liferay-dev taskboard-db-mariadb-master-0 3306:3306 &
while ! nc -z localhost 3306; do
  sleep 1
done

mysql --protocol=tcp --default-character-set=utf8 --host=localhost --user=taskboard --port=3306 -p <<EOF
drop database taskboardlf_uat_dev;
create database taskboard CHARACTER SET utf8 COLLATE utf8_general_ci;
use taskboard;
source taskboard_dev.dmp;
EOF