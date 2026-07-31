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

### Simulated eIAM Access Tokens

The local authorization server simulates access tokens issued by eIAM. In particular, it writes simple roles to the
`role` claim rather than the `userroles` claim normally expected by jEAP Security. The
[`EiamJwtAccessTokenCustomizer`](jme-security-oauth2-auth-scs/src/main/java/ch/admin/bit/jeap/oauth/mock/server/token/EiamJwtAccessTokenCustomizer.java)
creates these claims. Both resource services configure the jEAP `EiamClaimSetConverter` to convert them to the standard
jEAP authentication model.

The simple-role resource uses the default jEAP authorities mapping and demonstrates partner and thing authorization. The
custom-authorities resource provides an
[`ExampleAuthoritiesResolver`](jme-security-oauth2-resource-authorities-service/src/main/java/ch/admin/bit/jeap/jme/security/oauth/resource/ExampleAuthoritiesResolver.java)
that derives `things:read`, `things:detail`, and `info:read` authorities from the token's roles.

## Prerequisites

- Java 25
- A network connection for resolving Maven dependencies on the first build
- `curl` for the commands below
- Optionally, `jq` for extracting an access token when calling a resource directly

The Maven wrapper downloads Maven 3.9.16 automatically.

## Build And Test

Run the complete build, including unit tests, license checks, and the integration test that starts all five applications:

```shell
./mvnw clean verify
```

The integration test uses the local profiles and ports listed in the module table. Ensure those ports are free before
starting it.

## Run Locally

You only need to run one three-service chain at a time. Run each command in a separate terminal and wait for the
`Started ...Application` message before starting the next service.

### IntelliJ IDEA

The repository includes the same five commands as shared Maven run configurations under `.run/`. IntelliJ discovers them
automatically after importing the root `pom.xml`. Start `Auth Server`, the resource for the chain you want to test, and its
matching client. These Maven configurations intentionally use the production runtime classpath; launching a main class with
test dependencies included activates test-only security auto-configuration and does not represent the running application.

### Simple-Role Chain

```shell
./mvnw -pl jme-security-oauth2-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-oauth2-resource-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-oauth2-client-service spring-boot:run -Dspring-boot.run.profiles=local
```

Verify that the client is ready. It can briefly report `503` while OAuth discovery is still initializing:

```shell
curl --fail http://localhost:8090/jme-security-oauth2-client-service/actuator/health
```

The expected result contains `"status":"UP"`. Then exercise the complete client-credentials flow:

```shell
curl --fail http://localhost:8090/jme-security-oauth2-client-service/api/things
curl --fail http://localhost:8090/jme-security-oauth2-client-service/api/partners
curl --fail http://localhost:8090/jme-security-oauth2-client-service/api/info
curl --fail http://localhost:8090/jme-security-oauth2-client-service/api/current-user
```

Representative results are:

```text
Got things: [..."value":"Thing1"..."value":"Thing9"...]
Partner list: [..."externalRef":"eins"...]
Info : Some info
{"myCustomValue":"fooBar","subject":"<generated UUID>","userRoles":["partner_read","thing_read"]}
```

### Custom-Authorities Chain

Keep the authorization server running, stop the simple resource and client if desired, and start these services in separate
terminals:

```shell
./mvnw -pl jme-security-oauth2-resource-authorities-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-security-oauth2-client-authorities-service spring-boot:run -Dspring-boot.run.profiles=local
```

Verify readiness and call the client facade:

```shell
curl --fail http://localhost:8092/jme-security-oauth2-client-authorities-service/actuator/health
curl --fail http://localhost:8092/jme-security-oauth2-client-authorities-service/api/things
curl --fail http://localhost:8092/jme-security-oauth2-client-authorities-service/api/things/1
curl --fail http://localhost:8092/jme-security-oauth2-client-authorities-service/api/info
curl --fail http://localhost:8092/jme-security-oauth2-client-authorities-service/api/current-user
```

Representative results are:

```text
Got things: [..."value":"Thing1"..."value":"Thing9"...]
Got thing with id '1': {"id":"1","value":"Thing1"}
Info : Some info
{"subject":"<generated UUID>","userRoles":["admin","superadmin","user"]}
```

The generated subject and the order of array or set elements can differ between runs.

## Resource Endpoints

The public client APIs forward requests to their resource service and add the required OAuth2 token or Basic credentials.
The simple-role client on port 8090 exposes:

| Path | Demonstrates |
| --- | --- |
| `/api/partners` | List the partners allowed by the token's roles |
| `/api/partners/11111` | Read a partner by ID |
| `/api/partners/eins` | Read a partner by external reference |
| `/api/partners/eins/name` | Read a partner's name |
| `/api/things` | List the things allowed by the token's roles |
| `/api/partners/11111/things` | List things belonging to a partner |
| `/api/things/1` | Read a thing by ID |
| `/api/info` | Forward to a resource protected with Basic authentication instead of OAuth2 |
| `/api/current-user` | Return the user represented by the forwarded OAuth2 token |

Prefix each path with:

```text
http://localhost:8090/jme-security-oauth2-client-service
```

The custom-authorities client on port 8092 exposes `/api/things`, `/api/things/{id}`, `/api/info`, and
`/api/current-user` below:

```text
http://localhost:8092/jme-security-oauth2-client-authorities-service
```

## Call A Resource Directly

The client facade is the easiest way to run the example. To inspect the underlying OAuth2 exchange instead, request a
token from the mock server and call the simple-role resource directly:

```shell
TOKEN="$(curl --fail --silent \
  --user jme-security-oauth2-client-service:secret \
  --data grant_type=client_credentials \
  http://localhost:8081/jme-security-oauth2-auth-scs/oauth2/token | jq -r .access_token)"

curl --fail \
  --header "Authorization: Bearer ${TOKEN}" \
  http://localhost:8070/jme-security-oauth2-resource-service/api/things
```

Unlike the client facade, the resource returns its JSON body directly rather than prefixing it with `Got things:`.

The simple resource also demonstrates an endpoint protected by a separate Basic-authentication filter chain:

```shell
curl --fail --user user:secret \
  http://localhost:8070/jme-security-oauth2-resource-service/api/info
```

The expected result is `Some info`.

## Local Fixture Credentials

| Purpose | Username or client ID | Password or client secret |
| --- | --- | --- |
| Simple-role OAuth2 client | `jme-security-oauth2-client-service` | `secret` |
| Custom-authorities OAuth2 client | `jme-security-oauth2-client-authorities-service` | `secret` |
| Basic-auth info endpoint | `user` | `secret` |
| Prometheus endpoint | `prometheus` | `secret` |

Configuration files contain obvious credentials such as `secret` solely for the included local authorization server,
basic-auth examples, and automated tests. They are not suitable for deployed environments. Supply credentials and allowed
CORS origins through external configuration when embedding these artifacts in a platform wrapper.

## JME And License

This repository is part of the public [JME project](https://github.com/jme-admin-ch/jme). It is licensed under the
[Apache License 2.0](./LICENSE).
