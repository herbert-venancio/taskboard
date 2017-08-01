[![Quality Gate](http://10.44.1.250:9000/api/badges/gate?key=br.com.objective:taskboard)](http://10.44.1.250:9000/dashboard/index/br.com.objective:taskboard)

This application **must be run with Java 8** (404 errors on ws may indicate wrong JRE version).

## Development setup

1. Install NodeJS: `sudo apt-get install nodejs`
2. Install NPM: `sudo apt-get install npm`
3. Install Bower: `npm install -g gulp bower` 
4. Clone project and execute `mvn clean install` to assure dependencies are ok. (In case of error downloading from repository "git://github.com/PolymerElements/neon-elements.git" execute `git config --global url."https://".insteadOf git://`)
5. Download [Eclipse](http://www.eclipse.org/downloads/) 
6. Download [Lombok](https://projectlombok.org/download.html) and follow instructions to install it on eclipse
7. Import the project on eclipse
8. Import format configuration file taskboard-format.xml (`Project > Configuration > Java Code Style > Formatter > Import`).


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
By default, the genarated artifact is a **jar**. Alternatively you can generate a **war** by activating the `packaging-war` maven profile: \
 `mvn package -P packaging-war`

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

## Running application

#### Development Mode

1. Configure the empty properties on `src/main/resources/application-dev.properties`.\
There are many ways to do that. See [Spring Boot External Config](http://docs.spring.io/spring-boot/docs/1.3.1.RELEASE/reference/html/boot-features-external-config.html).

2. Execute main on class `objective.taskboard.Application`.  
The application will be available on: [http://localhost:8080/](http://localhost:8080/)

#### Production Mode

1. Configure the empty properties on `src/main/resources/application-prod.properties`.\
There are many ways to do that. See [Spring Boot External Config](http://docs.spring.io/spring-boot/docs/1.3.1.RELEASE/reference/html/boot-features-external-config.html).

2. Execute/deploy app:
    * If you have generated a **jar** file: one way is to create a file named `application.properties` and run `java -Dspring.config.location=[path to application.properties] -jar [path to taskboard.jar]`.
    * If you have generated a **war** file: one option is to configure the properties as system properties on container.

## Filters

Term | Description
--- | ---
searchFilter | Filter by text.
hierarchicalFilter | Triggered by a button on the issue card, filters by hierarchy and dependencies.
aspectsFilter | Located on sidebar menu, load filter itens from server.
aspectItemFilter | Itens on aspectFilter (issueType, team, project).
aspectSubitemFilter | Itens on aspectItemFilter
filtersPreferences | User defined preferences, aspectSubitensFilter configuration.
