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

class Flyway implements Serializable {

    private script
    private CONTAINER_ID
    private DATABASE_IP

    Flyway(def script) {
        this.script = script
    }

    void startMariaDBContainer() {
        CONTAINER_ID = script.sh(script: '''docker run \
            -e MYSQL_ROOT_PASSWORD=my-secret-pw \
            -e MYSQL_DATABASE=taskboard \
            -e MYSQL_USER=taskboard \
            -e MYSQL_PASSWORD=taskboard \
            -d mariadb:5.5
        ''', returnStdout: true).trim()
        DATABASE_IP = extractIP()
        waitMysqlAcceptConnections()
    }

    void destroyContainer() {
        script.sh(script: """
            docker stop $CONTAINER_ID
            docker rm $CONTAINER_ID
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

    private extractIP() {
        return script.sh(script: """
            docker inspect --format '{{ .NetworkSettings.IPAddress }}' $CONTAINER_ID
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
}

return this