#!/bin/bash

if ! ls application-dev.properties application-dev.yaml googleapps-credentials.json kpi.properties StoredCredential >/dev/null; then
    echo "Please insert all the configuration files inside of the config folder."
    echo "Files list: "
    echo
    echo "-application-dev.properties"
    echo "-kpi.properties"
    echo "-application-dev.yaml"
    echo "-googleapps-credentials.json"
    echo "-google-credentials/StoredCredential"
    exit 1
fi

echo "server.tomcat.internal-proxies=.*" >> application-dev.properties

kubectl delete configmap config-files --namespace taskboard-liferay-dev
kubectl create configmap config-files --namespace taskboard-liferay-dev --from-file config/
