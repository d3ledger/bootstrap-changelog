# Changelog endpoint
The `changelog-endpoint` module is a REST service intended for Iroha data model migrations: adding signatories, setting account details, creating domains, appending roles, etc.

## Simplified flow
1) The service executes given Groovy script that implements `ChangelogInterface`.
2) Changelog transaction (generated by Groovy script) created is accompanied by a changelog history transaction in order to keep track of changes.
3) Then transactions are combined into an atomic batch. 
4) Superuser signs recently created batch and sends it to Iroha as MST. 

## Brief API documentation
Available endpoints:
1) `/changelog/changelogFile` - executes changelog Groovy script file absolute path of which is defined in a JSON request. 
2) `/changelog/changelogScript`- executes changelog Groovy script code of which is defined in a JSON request.
 
Complete documentation is available at http://localhost:9999/apidocs/

## Configuration overview (changelog.properties)
1) `changelog.irohaHost` - host of Iroha
2) `changelog.irohaPort` - port of Iroha
3) `changelog.port` - the changelog endpoint port

## Integration
The service may be run as a Docker container
```
changelog:
    image: nexus.iroha.tech:19002/changelog-deploy/changelog:1.0.0_rc5
    container_name: changelog
    depends_on:
      - {your iroha service name}
    volumes:
      - {path to the changelog config folder}:/opt/changelog/configs/
    ports:
      - {desired port}:9999
    networks:
      - {your network}
```