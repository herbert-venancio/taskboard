[![Quality Gate](http://10.44.1.250:9000/api/badges/gate?key=br.com.objective:taskboard)](http://10.44.1.250:9000/dashboard/index/br.com.objective:taskboard)

This application **must be run with Java 8** (404 errors on ws may indicate wrong JRE version).

## Development Setup

1. Install NodeJS: `sudo apt-get install nodejs`
2. Install NPM: `sudo apt-get install npm`
4. Clone project and execute `mvn clean install` to assure dependencies are ok. (In case of error downloading from repository "git://github.com/PolymerElements/neon-elements.git" execute `git config --global url."https://".insteadOf git://`)
5. Download [Eclipse](http://www.eclipse.org/downloads/) 
6. Import the project on eclipse
7. Import format configuration file taskboard-format.xml (`Project > Configuration > Java Code Style > Formatter > Import`).

## Development Workflow

- After update sources (e.g. git pull): Run `mvn generate-resources` to install and build front-end dependencies;

## Integration Tests

The integration tests run every time a mvn install/deploy/release/verify is executed. You must run it at least once before running integration tests from eclipse.

To run integration tests from eclipse:

1. Run TestMain to run the server
2. Run the tests

The tests require Firefox version 54+ installed in your system. Make sure your version matches at least 54.   

## How to run the integration tests without losing computer control

Wanna run the tests from maven and still keep using your computer? Here's how:

1. Run the following command in the terminal (you might have to install Xvfb first):

`Xvfb :1 &` 

2. Now, run your tests with the following commands:

```
export DISPLAY=:1
mvn clean verify
```

That's all!


## Generating a war
The generated artifact is a war. Just run:

`mvn package`

After build it'll be located at:

`application/target/application-{version}.war`

## Updating the Followup Template

To update the followup template:

1. Ensure 'From Jira' tab has no data at all
2. unzip the template
3. run the following command:
  - xmllint --format xl/worksheets/sheet7.xml > sheet7-reformatted.xml
4. open sheet7-reformatted.xml and copy the contents of tag <row r="1"..></row> over the same row on template src/main/resources/followup-template/sheet7-template.xml
5. remove xl/worksheets/sheet7.xml and the reformatted file
6. copy the contents of ./xl/sharedStrings.xml and execute the following command:
   - xmllint --format sharedStrings.xml > ./src/main/resources/followup-template/sharedStrings-initial.xml
7. remove sharedStrings.xml
8. zip the contents again into ./src/main/resources/followup-template/Followup-template.xlsm
9. And you're done.

## Configuring Sizing Import

1. Follow the [Step 1 of this tutorial](https://developers.google.com/sheets/api/quickstart/js) to create a credential and to use the Google Sheets API. Then, export the JSON file of your key.
2. Configure the "google-api.client-secrets-file" property on `src/main/resources/application-[environment].properties`.\
`google-api.client-secrets-file=[path to googleapps-credentials.json]`
3. Create a folder named "credentials" and configure the "google-api.credential-store" property on `src/main/resources/application-[environment].properties`.\
`google-api.credential-store-dir=[path to /credentials]`

## How to test the migrations on Oracle

To test the migrations on Oracle, you'll need to get an Oracle database instance. 

1. First, you have to add the following line to your `/etc/docker/daemon.json` : 

    ```
    { "insecure-registries":["dockercb:5000"] }
    ```
2. Restart your docker daemon with the following command 

    ```sudo docker restart```

3. Start the Oracle container running the command: 

    ```
    sudo docker run -e ORACLE_HOME=/opt/oracle/product/11.2.0/dbhome_1 -h oracle11g --privileged -d dockercb:5000/ng-oracle
    ```

4. For last, set the lines below on application[-dev/prod].properties. Don't forget to comment any lines that make reference to MySql.

    ```
    spring.datasource.driverClassName=oracle.jdbc.driver.OracleDriver
    spring.datasource.url=jdbc:oracle:thin:system/oracle@localhost:1521/orcl
    spring.datasource.validation-query=select 1 from dual
    spring.datasource.testWhileIdle=true
    ...
    flyway.locations=db/migration/oracle
    ```

## How to solve when javascript code is "encrypted" in browser

Execute `mvn clean package` or delete the folder `target/test-classes/static/`

## Running application

#### Development Mode

1. Configure the empty properties on `src/main/resources/application-dev.properties`.\
There are many ways to do that. See [Spring Boot External Config](http://docs.spring.io/spring-boot/docs/1.3.1.RELEASE/reference/html/boot-features-external-config.html).

2. Execute main on class `objective.taskboard.Application`.  
The application will be available on: [http://localhost:8080/](http://localhost:8080/)

3. Run the npm run watch command as below, so every change you make on the web app will be availabe immediately after saving.

```
npm run watch
```

### Sonar

To generate the sonar report locally, run the following command:

mvn sonar:sonar -Dsonar.host.url=http://sonar-sdlc -Dsonar.analysis.mode=preview -Dsonar.issuesReport.html.enable=true -Dsonar.issuesReport.console.enable=true

#### Production Mode

1. Configure the empty properties on `src/main/resources/application-prod.properties`.\
There are many ways to do that. See [Spring Boot External Config](http://docs.spring.io/spring-boot/docs/1.3.1.RELEASE/reference/html/boot-features-external-config.html).

2. Execute/deploy app:
    * one option is to configure the properties as system properties on container.

## Filters

Term | Description
--- | ---
searchFilter | Filter by text.
hierarchicalFilter | Triggered by a button on the issue card, filters by hierarchy and dependencies.
aspectsFilter | Located on sidebar menu, load filter itens from server.
aspectItemFilter | Itens on aspectFilter (issueType, team, project).
aspectSubitemFilter | Itens on aspectItemFilter
filtersPreferences | User defined preferences, aspectSubitensFilter configuration.


## Create subtask on transition

It's possible to create a subtask on specific transitions (multiple configurations are supported). Example:

```properties
jira.subtask-creation[0].issue-type-parent-id=10601                       # issue type from the issue being transitioned
jira.subtask-creation[0].status-id-from=10651                             # transition source status
jira.subtask-creation[0].status-id-to=10052                               # transition target status
jira.subtask-creation[0].issue-type-id=12                                 # subtask issue type
jira.subtask-creation[0].summary-prefix=SUB -                             # prefix for subtask summary
jira.subtask-creation[0].t-shirt-size-parent-id=customfield_11441         # t-shirt-size customfield used to extract the subtask t-shirt-size
jira.subtask-creation[0].t-shirt-size-subtask-id=customfield_11457        # t-shirt-size customfield that will be set on subtask
jira.subtask-creation[0].t-shirt-size-default-value=M                     # default t-shirt-size for subtask, if not available on parent (optional)
jira.subtask-creation[0].skip-creation-when-t-shirt-parent-is-absent=true # will skip subtask creation when t-shirt-size of parent is not present (optional, default = false)
jira.subtask-creation[0].transition-id=11                                 # transition executed on the subtask (optional)
jira.subtask-creation[0].custom-field-condition.id=customfield_10204      # custom field used for conditional subtask creation (optional)
jira.subtask-creation[0].custom-field-condition.value=Yes                 # custom field value used for conditional subtask creation (optional) 
```
## Troubleshooting

#### What if I run inside eclipse and the site is blank?
If for some reason there are missing resources in the folder `target/classes/static` like `bower_components`,
you will see just a blank page on the browser when you run the tests from eclipse. 
Just execute `mvn package` to regenerate these resources and rerun the tests.
