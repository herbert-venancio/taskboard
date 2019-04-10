In order to install the taskboard in the kubernetes cluster, you'll need to use the following scripts in the order they're listed.
Notice that you might not need to run all of them after having installed it once.

IMPORTANT: The file dbvalues.yaml should contain the credentails to your mariadb instance. You HAVE to insert the passwords for the root and the taskboard user.

-create-config-taskboard.sh: Creates a ConfigMap with with the config files present on the config folder.
-deploy-on-kubernetes.sh: Deploys the taskboard and mariadb together with all the structure.
-import-mysql-dump.sh: Imports the content of a mysql dump named taskboard_dev.dmp in the same folder. (Requires you to type credentials)