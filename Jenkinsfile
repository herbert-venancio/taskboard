@Library("liferay-sdlc-jenkins-lib")
import static org.liferay.sdlc.SDLCPrUtilities.*

properties([
        parameters([
                booleanParam(
                        defaultValue: false
                        , description: 'Runs release step'
                        , name: 'RELEASE')
        ])
])

node("general-purpose") {
    // start with a clean workspace
    stage('Checkout') {
        deleteDir()
        checkout scm
    }
    def mvnHome = tool 'maven-3.3.3'
    def javaHome = tool '1.8.0_131'
    withEnv(["JAVA_HOME=$javaHome", "M2_HOME=$mvnHome", "PATH+MAVEN=$mvnHome/bin", "PATH+JDK=$javaHome/bin"]) {
        try {
            stage('Build') {
                try {
                    wrap([$class: 'Xvnc']) {
                        sh "${mvnHome}/bin/mvn --batch-mode -V -U clean verify -P packaging-war,dev"
                    }
                } finally {
                    archiveArtifacts artifacts: 'target/test-attachments/**', fingerprint: true, allowEmptyArchive: true
                    junit testResults: 'target/surefire-reports/*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']], allowEmptyResults: true
                    junit testResults: 'target/failsafe-reports/*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']], allowEmptyResults: true
                }
            }
        } catch (ex) {
            handleError('objective-solutions/taskboard', 'devops@objective.com.br', 'objective-solutions-user')
            throw ex
        }
        if (BRANCH_NAME == 'master') {
            stage('Deploy Maven') {
                sh "${mvnHome}/bin/mvn --batch-mode -V clean deploy -DskipTests -P packaging-war,dev -DaltDeploymentRepository=repo::default::http://repo:8080/archiva/repository/snapshots"
                if (!params.RELEASE) {
                    def downloadUrl = extractDownloadUrlFromLogs()
                    addDownloadBadge(downloadUrl)
                }
            }
            stage('Deploy Docker') {
                sh 'git clone https://github.com/objective-solutions/liferay-environment-bootstrap.git'
                dir('liferay-environment-bootstrap/dockers/taskboard') {
                    sh 'cp ../../../target/taskboard-*-SNAPSHOT.war ./taskboard.war'
                    sh 'sudo docker build -t dockercb:5000/taskboard-snapshot .'
                    sh 'sudo docker push dockercb:5000/taskboard-snapshot'
                }
            }
            if (params.RELEASE) {
                stage('Release') {
                    echo 'Releasing...'
                    sh 'git checkout master'
                    sh "${mvnHome}/bin/mvn --batch-mode -Dresume=false release:prepare release:perform -DaltReleaseDeploymentRepository=repo::default::http://repo:8080/archiva/repository/internal -Darguments=\"-DaltDeploymentRepository=internal::default::http://repo:8080/archiva/repository/internal -P packaging-war,dev -DskipTests=true -Dmaven.test.skip=true -Dmaven.javadoc.skip=true\""
                    def downloadUrl = extractDownloadUrlFromLogs()
                    addDownloadBadge(downloadUrl)
                    updateReleaseLinkInDescription(downloadUrl)
                }
            }
        }
    }
}

def extractDownloadUrlFromLogs() {
    def artifactType = "war"
    def pattern
    if(params.RELEASE) {
        pattern = /.*Uploaded: (http:.*internal.*${artifactType}).*/
    } else {
        pattern = /.*Uploaded: (http:.*.${artifactType}).*/
    }
    def matcher = manager.getLogMatcher(pattern)
    return matcher.group(1)
}

def addDownloadBadge(downloadUrl) {
    def artifactName = downloadUrl.replaceAll(".*/(.*)", '$1')
    def summary = manager.createSummary("info.gif")
    summary.appendText("<a href='${downloadUrl}>Link to deployed war: ${artifactName}</a>", false)
    manager.addBadge("save.gif", "Click here to download ${artifactName}", downloadUrl)
}

def updateReleaseLinkInDescription(downloadUrl) {
    manager.build.project.description = "<a href='${downloadUrl}'>Latest released artifact</a>"
}
