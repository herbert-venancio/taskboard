#!/usr/bin/env groovy

import java.util.concurrent.TimeUnit
import groovy.transform.Field;

@Library("liferay-sdlc-jenkins-lib")
import static org.liferay.sdlc.SDLCPrUtilities.*

properties([
        disableConcurrentBuilds(),
        parameters([
            booleanParam(
                defaultValue: false
                , description: 'Runs release step'
                , name: 'RELEASE')
        ])
])

@Field final String RELEASE_URL = "http://repo:8080/archiva/repository/internal"
@Field final String SNAPSHOT_URL = "http://repo:8080/archiva/repository/snapshots"

node("single-executor") {
    // start with a clean workspace
    stage('Checkout') {
        deleteDir()
        checkout scm
    }
    def mvnHome = tool 'maven-3.3.3'
    def javaHome = tool '1.8.0_131'
    def nodeHome = tool 'NodeJS 9.11'
    withEnv(["JAVA_HOME=$javaHome", "M2_HOME=$mvnHome", "PATH+MAVEN=$mvnHome/bin", "PATH+JDK=$javaHome/bin", "PATH+NODE=$nodeHome/bin"]) {
        try {
            stage('Build') {
                try {
                    timeout(time: 60, unit: TimeUnit.MINUTES) {
                        wrap([$class: 'Xvnc']) {
                            sh "${mvnHome}/bin/mvn --batch-mode -V -U -Dmaven.test.failure.ignore=true clean install"
                        }
                    }
                } finally {
                    archiveArtifacts artifacts: '**/target/test-attachments/**', fingerprint: true, allowEmptyArchive: true
                    junit testResults: '**/target/surefire-reports/*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']], allowEmptyResults: true
                    junit testResults: '**/target/failsafe-reports/*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']], allowEmptyResults: true
                    killLeakedProcesses()
                }
            }

            if (currentBuild.result == 'UNSTABLE')
                return;

            stage('Sonar') {
                def SONAR_URL = env.SONARQUBE_URL
                sh "${mvnHome}/bin/mvn --batch-mode jacoco:merge -DmultimoduleRootDirectory=\$(pwd)"
                if (isMasterBranch()) {
                    sh "${mvnHome}/bin/mvn --batch-mode -V sonar:sonar -Dsonar.host.url=${SONAR_URL} -DmultimoduleRootDirectory=\$(pwd) -Dsonar.buildbreaker.skip=true"
                } else if (isPullRequest()) {
                    withCredentials([string(credentialsId: 'TASKBOARD_SDLC_SONAR', variable: 'GITHUB_OAUTH')]) {
                        def PR_ID = env.CHANGE_ID
                        def GIT_REPO = 'objective-solutions/taskboard'
                        sh "${mvnHome}/bin/mvn --batch-mode -V sonar:sonar -Dsonar.host.url=${SONAR_URL} \
                        -Dsonar.analysis.mode=preview \
                        -Dsonar.github.pullRequest=${PR_ID} \
                        -Dsonar.github.oauth=${GITHUB_OAUTH} \
                        -Dsonar.github.repository=${GIT_REPO} \
                        -DmultimoduleRootDirectory=\$(pwd)"
                    }
                }
            }

            stage('Flyway') {
                def flyway = load 'core/src/main/jenkins/flyway.groovy'
                parallel mariadb: {
                    flyway.testMariaDBMigration()
                }, oracle: {
                    flyway.testOracleMigration()
                }
                clearDocker()
            }
        } catch (ex) {
            handleError('objective-solutions/taskboard', 'devops@objective.com.br', 'objective-solutions-user')
            throw ex
        }

        def isOfficialBranch = isMasterBranch() || isPostBuildBranch()
        if (!isOfficialBranch)
            return;

        stage('Deploy Maven') {
            sh "${mvnHome}/bin/mvn --batch-mode -V clean deploy -DskipTests -P packaging-war,dev -DaltDeploymentRepository=repo::default::$SNAPSHOT_URL"
        }

        def project = readMavenPom file: ''
        stage('Release') {
            if (params.RELEASE) {
                echo 'Releasing...'
                sh "git checkout ${env.BRANCH_NAME}"
                sh "${mvnHome}/bin/mvn --batch-mode -Dresume=false release:prepare release:perform -DaltReleaseDeploymentRepository=repo::default::$RELEASE_URL -Darguments=\"-DaltDeploymentRepository=internal::default::$RELEASE_URL -P packaging-war,dev -DskipTests=true -Dmaven.test.skip=true -Dmaven.javadoc.skip=true\""

                def applicationProject = readApplicationPom()
                def downloadUrl = extractDownloadUrl(applicationProject)
                addDownloadBadge(downloadUrl)
                updateJobDescription(downloadUrl)
                if(isMasterBranch())
                    createPostBuildBranch(project)
            }
        }

        stage('Deploy Docker') {
            def projectVersion = project.version.replace("-SNAPSHOT", "");
            def tag = "";
            if (params.RELEASE)
                deployDocker("devopsobj/objective-taskboard:$projectVersion", "devopsobj", "")
            else {
                tag = isMasterBranch() ? 'snapshot-master' : 'snapshot-'+projectVersion;
                deployDocker("repo:5000/objective-taskboard:$tag", "repo-admin", "repo:5000")
            }
        }
    }
}

def deployDocker(imageFullName, dockerCredentialId, dockerServer) {
    dir('liferay-environment-bootstrap') {
        git branch: 'master', credentialsId: 'objective-solutions-user-rsa', url: 'git@gitlab:sdlc/liferay-environment-bootstrap.git'

        dir ('dockers/taskboard') {
            sh 'cp ../../../application/target/application*.war ./taskboard.war'
            sh "sudo docker build -t $imageFullName ."

            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: dockerCredentialId,
                usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD']]) {
                sh "sudo docker login -u='${DOCKER_USER}' -p='$DOCKER_PASSWORD' $dockerServer"
                sh "sudo docker push $imageFullName"
            }
        }
    }    
}

def isMasterBranch() {
    return env.BRANCH_NAME == 'master'
}

def isPostBuildBranch() {
    return env.BRANCH_NAME ==~ /^\d+(\.[0-9]+)*\.X$/
}

def readApplicationPom() {
    dir('application') {
        sh "${env.M2_HOME}/bin/mvn --batch-mode -V -U -N help:effective-pom -Doutput=effectivePom.xml"
        return readMavenPom(file: 'effectivePom.xml')
    }
}

def extractDownloadUrl(project) {
    def artifactType = "war"
    if(params.RELEASE) {
        def version = project.version.replace('-SNAPSHOT', '')
        return "$RELEASE_URL/${project.groupId.replace(".", "/")}/$project.artifactId/$version/$project.artifactId-$version.$artifactType"
    } else {
        def pattern = /.*Uploaded: (http:.*.${artifactType}).*/
        def matcher = manager.getLogMatcher(pattern)
        return matcher != null ? matcher.group(1) : null
    }
}

def addDownloadBadge(downloadUrl) {
    if(downloadUrl == null)
        return

    def artifactName = downloadUrl.replaceAll(".*/(.*)", '$1')
    def summary = manager.createSummary("info.gif")
    summary.appendText("<a href='${downloadUrl}>Link to deployed war: ${artifactName}</a>", false)
    manager.addBadge("save.gif", "Click here to download ${artifactName}", downloadUrl)
}

def updateJobDescription(downloadUrl) {
    if(downloadUrl == null)
        return

    def latestReleaseLink = "<a href='${downloadUrl}'>Latest released artifact</a>"
    def makePostReleaseBranch = """
        <form action="/job/sdlc/job/taskboard/job/${env.BRANCH_NAME}/buildWithParameters" method="POST">
          <input type="hidden" name="RELEASE" value="true">
          <input type="submit" value="New Release">
        </form>
    """
    manager.build.project.description = latestReleaseLink + "<br/>" + makePostReleaseBranch
}

def createPostBuildBranch(project) {
    def tagVersion = project.version.replace('-SNAPSHOT', '')
    def tagName = "${project.artifactId}-${tagVersion}"
    def pbVersion = project.version.replace('-SNAPSHOT', '.1-SNAPSHOT')
    def pbBranch = tagVersion + ".X"
    sh """
        git checkout -b $pbBranch $tagName
        mvn release:update-versions -DdevelopmentVersion=$pbVersion
        mvn scm:checkin -Dmessage="[jenkins-pipeline] prepare post-build branch $pbBranch"
    """
}

def killLeakedProcesses() {
    def BUILD_URL = env.BUILD_URL.trim()
    def TestMainPID = sh(script: "ps eaux | grep objective.taskboard.TestMain | grep 'BUILD_URL=$BUILD_URL' | awk '{print \$2}'", returnStdout: true)
    if (TestMainPID)
        sh(script: "kill -9 ${TestMainPID}", returnStatus: true)

    def WORKSPACE = env.WORKSPACE.trim()
    def FirefoxPID = sh(script: "ps aux | grep firefox | grep '$WORKSPACE' | awk '{print \$2}'", returnStdout: true)
    if (FirefoxPID)
        sh(script: "kill -9 ${FirefoxPID}", returnStatus: true)
}

void clearDocker() {
    def images = sh(script: 'sudo docker images -qf dangling=true', returnStdout: true).trim()
    if(images)
        sh(script: "sudo docker rmi $images", returnStatus: true)

    def volumes = sh(script: 'sudo docker volume ls -qf dangling=true', returnStdout: true).trim()
    if(volumes)
        sh(script: "sudo docker volume rm $volumes", returnStatus: true)
}
