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
    try {
        stage('Build') {
            withEnv(["JAVA_HOME=${ tool '1.8.0_131' }", "PATH+MAVEN=${tool 'maven-3.3.3'}/bin:${env.JAVA_HOME}/bin"]) {
                sh "mvn --batch-mode -V -U clean deploy -P packaging-war,dev -DaltDeploymentRepository=repo::default::http://repo:8080/archiva/repository/snapshots"
            }
            junit 'target/surefire-reports/*.xml'
            junit 'target/failsafe-reports/*.xml'
            stash 'working-copy'
        }
        if(BRANCH_NAME == 'master') {
            stage('Deploy Docker') {
                unstash 'working-copy'

                git clone "https://github.com/objective-solutions/liferay-environment-bootstrap.git"
                dir('liferay-environment-bootstrap/dockers/taskboard') {
                    sh "cp ../../../target/taskboard-*-SNAPSHOT.war taskboard.war"

                    docker.withRegistry("http://dockercb:5000") {
                        def image = docker.build "taskboard-snapshot"
                        image.push
                    }
                }
            }
        }
    } catch (ex) {
        handleError("objective-solutions/taskboard", "devops@objective.com.br", "objective-solutions-user")
        throw ex
    }
}
