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


## Generating a war
By default, the genarated artifact is a **jar**. Alternatively you can generate a **war** by activating the `packaging-war` maven profile: \
 `mvn package -P packaging-war`


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
