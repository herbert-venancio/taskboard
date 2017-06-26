@Library("liferay-sdlc-jenkins-lib") import static org.liferay.sdlc.SDLCPrUtilities.*

properties([
        parameters([
                booleanParam(
                        defaultValue: false
                        , description: 'Runs release step'
                        , name: 'RELEASE')
        ])
])

node ("general-purpose") {
    checkout scm
    def mvnHome = tool 'maven-3.3.3'
    def javaHome = tool '1.8.0_131'
    withEnv(["JAVA_HOME=$javaHome", "M2_HOME=$mvnHome", "PATH+MAVEN=$mvnHome/bin", "PATH+JDK=$javaHome/bin"]) {
        try {
            stage('Build') {
                wrap([$class: 'Xvnc']) {
                    sh "${mvnHome}/bin/mvn --batch-mode -V -U clean verify -P packaging-war,dev"
                }
                junit 'target/surefire-reports/*.xml'
                junit 'target/failsafe-reports/*.xml'
            }
        } catch (ex) {
            handleError('objective-solutions/taskboard', 'devops@objective.com.br', 'objective-solutions-user')
            throw ex
        }
        if (BRANCH_NAME == 'master') {
            stage('Deploy Maven') {
                sh "${mvnHome}/bin/mvn --batch-mode -V clean deploy -DskipTests -P packaging-war,dev -DaltDeploymentRepository=repo::default::http://repo:8080/archiva/repository/snapshots"
            }
            stage('Deploy Docker') {
                git clone 'https://github.com/objective-solutions/liferay-environment-bootstrap.git'
                dir('liferay-environment-bootstrap/dockers/taskboard') {
                    sh 'cp ../../../target/taskboard-*-SNAPSHOT.war taskboard.war'

                    docker.withRegistry("http://dockercb:5000") {
                        def image = docker.build "taskboard-snapshot"
                        image.push
                    }
                }
            }
            if (params.RELEASE) {
                stage('Release') {
                    echo 'Releasing...'
                    sh "${mvnHome}/bin/mvn --batch-mode release:prepare release:perform"
                }
            }
        }
    }
}
