In order to install the taskboard in the kubernetes cluster, you'll need to use the following scripts in the order they're listed.
Notice that you might not need to run all of them after having installed it once.

IMPORTANT: The file dbvalues.yaml should contain the credentails to your mariadb instance. You HAVE to insert the passwords for the root and the taskboard user.

-create-config-taskboard.sh: Creates a ConfigMap with with the config files present on the config folder.
-deploy-on-kubernetes.sh: Deploys the taskboard and mariadb together with all the structure.
-import-mysql-dump.sh: Imports the content of a mysql dump named taskboard_dev.dmp in the same folder. (Requires you to type credentials)

In order to update the Taskboard DEV, it is assumed that you have already installed into your local machine the following tools:

- awscli
- kubectl 
- kops
- helm

And those tools are correctly configured.

The awscli must have access to the cluster. You can achieve this goal by using the following command "aws configure" and setting the credentials.
You must set as environment variable the KOPS_STATE_STORE with the value "s3://devops-objective.kops". Ps: remove the quotes.
You must export the kubecfg using the following command: "kops export kubecfg devops-objective.k8s.local". Ps: remove the quotes either.
Make sure that your current cluster context is "objective.k8s.local".
Make sure either that the helm is initialized pointing to the existent Tiller just by using this command: "helm init --client-only"

So then you can easily update the taskboard by executing the command: "helm upgrade taskboard .".
Remember the command above, must be executed inside the path ~/taskboard/kubernetes/taskboard/

Or you can update by executing the follow script: 
