# Changelog endpoint
The `changelog-endpoint` module is a REST service intended for Iroha data model migrations: adding signatories, setting account details, creating domains, appending roles and etc.

## Brief API documentation
Available endpoints:
1) `/changelog/changelogFile` - executes changelog Groovy script file which absolute path is defined in a JSON request.  
2) `/changelog/changelogScript`- executes changelog Groovy script which code is defined in a JSON request.
 
Complete documentation is available at http://localhost:9999/apidocs/

## Config review (changelog.properties)
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
