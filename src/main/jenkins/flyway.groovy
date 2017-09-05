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
        CONTAINER_ID = script.sh(script: '''sudo docker run \
            -e MYSQL_ROOT_PASSWORD=my-secret-pw \
            -e MYSQL_DATABASE=taskboard \
            -e MYSQL_USER=taskboard \
            -e MYSQL_PASSWORD=taskboard \
            -d mariadb:5.5
        ''', returnStdout: true).trim()
        DATABASE_IP = extractIP()
        waitMysqlAcceptConnections()
    }

    void startOracleContainer() {
        CONTAINER_ID = script.sh(script: '''sudo docker run \
            -e ORACLE_HOME=/u01/app/oracle/product/11.2.0/xe \
            -d wnameless/oracle-xe-11g:14.04.4
        ''', returnStdout: true).trim()
        DATABASE_IP = extractIP()
        waitOracleAcceptConnections()
        initOracle()
    }

    void destroyContainer() {
        script.sh(script: """
            sudo docker stop $CONTAINER_ID
            sudo docker rm $CONTAINER_ID
        """, returnStatus: true)
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
                -Dflyway.url="jdbc:oracle:thin:@$DATABASE_IP:1521/xe" \
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
            script.timeout(1) {
                script.sh(script: $/sudo docker exec $CONTAINER_ID bash -c "until ( echo \"select 'oracle is ready' from dual;\" | /u01/app/oracle/product/11.2.0/xe/bin/sqlplus system/oracle@localhost:1521 | grep 'oracle is ready' ); do sleep 1; done"/$
                        , returnStatus: true)
            }
        } catch (ex) {
            // ignore
        }
    }

    private initOracle() {
        script.sh "sudo docker exec -i $CONTAINER_ID bash -c 'echo \"CREATE USER taskboard IDENTIFIED BY taskboard;\" | /u01/app/oracle/product/11.2.0/xe/bin/sqlplus system/oracle@localhost:1521'"
    }
}

return this