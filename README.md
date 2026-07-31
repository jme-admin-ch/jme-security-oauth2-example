# JME OAuth2 Security Example

This project demonstrates OAuth2-protected resources and OAuth2 clients built with the
[jEAP Spring Boot security starter](https://github.com/jeap-admin-ch/jeap-spring-boot-starters). It provides two complete
client-credentials service chains: one using simple roles and one using a custom authorities resolver. A local OAuth2
authorization server makes the example self-contained.

## Modules

| Module | Runtime name | Port | Purpose |
| --- | --- | ---: | --- |
| `jme-security-oauth2-auth-scs` | `jme-security-oauth2-auth-scs` | 8081 | Local OAuth2 authorization server with example token customization |
| `jme-security-oauth2-resource-service` | `jme-security-oauth2-resource-service` | 8070 | OAuth2 resource demonstrating partner-scoped simple roles |
| `jme-security-oauth2-resource-authorities-service` | `jme-security-oauth2-resource-authorities-service` | 8072 | OAuth2 resource demonstrating custom authorities |
| `jme-security-oauth2-client-service` | `jme-security-oauth2-client-service` | 8090 | Client for the simple-role resource service |
| `jme-security-oauth2-client-authorities-service` | `jme-security-oauth2-client-authorities-service` | 8092 | Client for the custom-authorities resource service |
| `jme-security-oauth2-test` | n/a | n/a | Starts and verifies both complete service chains |

## Architecture

Start the authorization server first, followed by both resources and then both clients:

```text
jme-security-oauth2-auth-scs
  +-- jme-security-oauth2-resource-service
  |     +-- jme-security-oauth2-client-service
  +-- jme-security-oauth2-resource-authorities-service
        +-- jme-security-oauth2-client-authorities-service
```

The authorization server issues local client-credentials tokens. The clients obtain those tokens and forward them when
calling their resource services. The resource services validate the tokens and demonstrate declarative and programmatic
authorization. Platform-specific wrappers can consume the published thin service artifacts while retaining the runtime
names shown above.

## Prerequisites

- Java 25
- A network connection for resolving Maven dependencies on the first build

The Maven wrapper downloads Maven 3.9.16 automatically.

## Build And Test

Run the complete build, including unit tests, license checks, and the integration test that starts all five applications:

```shell
./mvnw clean verify
```

The integration test uses the local profiles and ports listed in the module table. Ensure those ports are free before
starting it.

## Run Locally

Run each command in a separate terminal and keep the dependency order shown below:

```shell
./mvnw -pl jme-security-oauth2-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-oauth2-resource-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-oauth2-resource-authorities-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-oauth2-client-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-oauth2-client-authorities-service spring-boot:run -Dspring-boot.run.profiles=local
```

## Sample Endpoints

The simple-role chain is available through the client on port 8090:

- `http://localhost:8090/jme-security-oauth2-client-service/api/things`
- `http://localhost:8090/jme-security-oauth2-client-service/api/partners`
- `http://localhost:8090/jme-security-oauth2-client-service/api/info`
- `http://localhost:8090/jme-security-oauth2-client-service/api/current-user`

The custom-authorities chain is available through the client on port 8092:

- `http://localhost:8092/jme-security-oauth2-client-authorities-service/api/things`
- `http://localhost:8092/jme-security-oauth2-client-authorities-service/api/things/1`
- `http://localhost:8092/jme-security-oauth2-client-authorities-service/api/info`
- `http://localhost:8092/jme-security-oauth2-client-authorities-service/api/current-user`

## Local Fixture Credentials

Configuration files contain obvious credentials such as `secret` solely for the included local authorization server,
basic-auth examples, and automated tests. They are not suitable for deployed environments. Supply credentials and allowed
CORS origins through external configuration when embedding these artifacts in a platform wrapper.

## JME And License

This repository is part of the public [JME project](https://github.com/jme-admin-ch/jme). It is licensed under the
[Apache License 2.0](./LICENSE).
