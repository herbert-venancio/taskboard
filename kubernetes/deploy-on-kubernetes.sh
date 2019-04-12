#! /bin/sh

helm install stable/mariadb --values=db-values.yaml --name=taskboard-db --namespace taskboard-liferay-dev
helm install --name taskboard --namespace taskboard-liferay-dev taskboard/ 
