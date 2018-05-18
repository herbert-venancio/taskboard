import java.util.concurrent.TimeUnit

void testMariaDBMigration() {
    def flyway = new Flyway(this)
    try {
        flyway.startMariaDBContainer()
        timeout(1) {
            flyway.testMysqlFlywayInstall()
        }
    } finally {
        flyway.destroyContainer()
    }
}

void testOracleMigration() {
    def flyway = new Flyway(this)
    try {
        flyway.startOracleContainer()
        timeout(1) {
            flyway.testOracleFlywayInstall()
        }
    } finally {
        flyway.destroyContainer()
    }
}

class Flyway implements Serializable {

    private script
    private CONTAINER_ID
    private DATABASE_IP

    Flyway(def script) {
        this.script = script
    }

    void startMariaDBContainer() {
        CONTAINER_ID = script.sh(script: 'sudo docker run \
            -e MYSQL_ROOT_PASSWORD=my-secret-pw \
            -e MYSQL_DATABASE=taskboard \
            -e MYSQL_USER=taskboard \
            -e MYSQL_PASSWORD=taskboard \
            -d mariadb:5.5'
        , returnStdout: true).trim()
        DATABASE_IP = extractIP()
        waitMysqlAcceptConnections()
    }

    void startOracleContainer() {
        CONTAINER_ID = script.sh(script: 'sudo docker run \
            -e ORACLE_HOME=/opt/oracle/product/11.2.0/dbhome_1 \
            -h oracle11g \
            --privileged \
            -d dockercb:5000/ng-oracle'
        , returnStdout: true).trim()
        DATABASE_IP = extractIP()
        waitOracleAcceptConnections()
        initOracle()
    }

    void destroyContainer() {
        def dockerStop = script.sh(script: "sudo docker stop $CONTAINER_ID", returnStatus: true)
        def dockerRm = script.sh(script: "sudo docker rm -f $CONTAINER_ID", returnStatus: true)
        script.echo "'docker stop' exit code: $dockerStop, 'docker rm' exit code: $dockerRm"
    }

    void testMysqlFlywayInstall() {
        // plugin acts strange in parallel
        script.lock(resource: 'flyway') {
            script.sh """
                mvn org.flywaydb:flyway-maven-plugin:4.2.0:migrate \
                -Dflyway.url="jdbc:mysql://$DATABASE_IP:3306/taskboard?useUnicode=true&characterEncoding=UTF8" \
                -Dflyway.user=taskboard \
                -Dflyway.password=taskboard \
                -Dflyway.locations=filesystem:src/main/resources/db/migration/mysql
            """
        }
    }

    void testOracleFlywayInstall() {
        // plugin acts strange in parallel
        script.lock(resource: 'flyway') {
            script.sh """
                mvn org.flywaydb:flyway-maven-plugin:4.2.0:migrate \
                -Dflyway.url="jdbc:oracle:thin:@$DATABASE_IP:1521/orcl" \
                -Dflyway.user=system \
                -Dflyway.password=oracle \
                -Dflyway.schemas=taskboard \
                -Dflyway.locations=filesystem:src/main/resources/db/migration/oracle
            """
        }
    }

    private extractIP() {
        return script.sh(script: """
            sudo docker inspect --format '{{ .NetworkSettings.IPAddress }}' $CONTAINER_ID
        """, returnStdout: true).trim()
    }

    private waitMysqlAcceptConnections() {
        script.echo "Waiting for database ready for connections..."
        try {
            script.timeout(1) {
                script.sh(script: "bash -c 'until ( echo \"\" > /dev/tcp/$DATABASE_IP/3306 ) 2> /dev/null; do sleep 1; done'"
                        , returnStatus: true)
            }
        } catch (ex) {
            // ignore
        }
    }

    private waitOracleAcceptConnections() {
        script.echo "Waiting for database ready for connections..."
        try {
            script.timeout(5) {
                script.sh(script: $/sudo docker exec $CONTAINER_ID bash -c "until ( echo \"select 'oracle is ready' from dual;\" | /opt/oracle/product/11.2.0/dbhome_1/bin/sqlplus system/oracle@localhost:1521/orcl | grep 'oracle is ready' ); do sleep 1; done"/$
                        , returnStatus: true)
            }
        } catch (ex) {
            // ignore
        }
    }

    private initOracle() {
        try {
            script.timeout(10, TimeUnit.SECONDS) {
                script.sh "sudo docker exec $CONTAINER_ID bash -c 'echo \"CREATE USER taskboard IDENTIFIED BY taskboard;\" | /opt/oracle/product/11.2.0/dbhome_1/bin/sqlplus system/oracle@localhost:1521/orcl'"
            }
        } catch (ex) {
            // ignore
        }
    }
}

return this
