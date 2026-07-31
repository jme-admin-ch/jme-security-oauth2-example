## JME Security Example (plain OAuth2, no PAMS)
This project collects example microservices that show how to implement and run OAuth2 protected resources and clients to access
such resources using the jeap-spring-boot-security-starter library.

* __jme-security-oauth2-resource-service__: Example of an OAuth2 resource based on the Spring WebMvc stack.  
* __jme-security-oauth2-resource-authorities-service__: Example of an OAuth2 resource with custom AuthoritiesResolver.
* __jme-security-oauth2-client-service__: Example of an OAuth2 client based on the Spring WebMvc stack.
* __jme-security-oauth2-client-authorities-service__: Example of an OAuth2 client running against `jme-security-oauth2-resource-authorities-service`.
* __jme-security-oauth2-auth-scs__: Example for configuring and running a jeap-oauth-mock-server instance as local or DEV OAuth2 authorization server.

### Simulated eIAM-issued access tokens
The jme-security-oauth2-auth-scs mock server project simulates eIAM-issued access tokens, e.g. it provides the user's
roles in a 'role' claim instead of the 'userroles' claim as expected by jEAP security. See the mock server customization
class EiamJwtAccessTokenCustomizer for details. Both resources configure an eIAM claim set converter provided by the
jeap-spring-boot-security-starter to adapt the simulated eIAM-issued tokens to jEAP security. See the configuration class
EaimClaimSetConverterConfiguration and the configuration option 'claim-set-converter-name' in the auth server configuration
of the resources for details.

## Installing / Getting started
Build and run the sub projects like any Spring Boot application. When running the examples locally on a developer machine,
start the application with the profile `local` activated.

At runtime, the OAuth2 resources and clients depend on a corresponding OAuth2 authentication server being available.
When running the examples locally on a developer machine, start the sub project jme-security-oauth2-auth-scs with the
profile `local` activated.

You don't need to start all the sub projects at the same time. You should always provide the authorization server first, then
start a resource and a client to access the resource. 

The resource service jme-security-oauth2-resource-service provides the REST resources:
* **partners resource**: manages partner data. OAuth2 protected.
  * list all partners which the user is allowed to access  
    e.g. /api/partners
  * get one partner by its id if the user is allowed to access the partner  
    e.g. /api/partners/11111
  * get one partner by its external reference if the user is allowed to access the partner  
    e.g. /api/partners/eins    
  * get one partner's name by the partner's external reference if the user is allowed to access the partner  
    e.g. /api/partners/eins/name
* **things resource**: manages 'things' that belong to a partner. OAuth2 protected.
  * list all things which the user is allowed to access  
    e.g. /api/things
  * list all things belonging to a certain partner if the user is allowed to access things for that partner  
    e.g. /api/partners/11111/things
  * get one thing belonging to a partner if the user is allowed to access things for that partner  
    e.g. /api/things/1
* **info resource**: aka 'OtherwiseProtectedResource'. Returns some fixed information. Basic auth protected.
  * get info if the user presents the correct basic auth credentials  
    e.g. /api/info
* **current-user info**: mirrors the current-user endpoint on the resource-service.
  * e.g. /api/current-user
  
The implementations of those resources show different typical authorization use cases and corresponding implementations
and sometimes even different implementation possibilities. Please see the source code and the Confluence documentation
for more details (https://confluence.bit.admin.ch/display/JEAP/Authentisierung+und+Autorisierung)

## Local Tests
* jme-security-oauth2-client-service
  * http://localhost:8090/jme-security-oauth2-client-service/api/partners
  * http://localhost:8090/jme-security-oauth2-client-service/api/info
  * http://localhost:8090/jme-security-oauth2-client-service/api/things
  * http://localhost:8090/jme-security-oauth2-client-service/api/current-user
* jme-security-oauth2-client-authorities-service
  * http://localhost:8092/jme-security-oauth2-client-authorities-service/api/info
  * http://localhost:8092/jme-security-oauth2-client-authorities-service/api/things
  * http://localhost:8092/jme-security-oauth2-client-authorities-service/api/things/1
  * http://localhost:8092/jme-security-oauth2-client-authorities-service/api/current-user

## Testing on RHOS dev

* jme-security-oauth2-client-service
  * https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-security-oauth2-client-service/api/partners
  * https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-security-oauth2-client-service/api/info
  * https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-security-oauth2-client-service/api/things
  * https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-security-oauth2-client-service/api/current-user
* jme-security-oauth2-client-authorities-service
  * https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-security-oauth2-client-authorities-service/api/info
  * https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-security-oauth2-client-authorities-service/api/things
  * https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-security-oauth2-client-authorities-service/api/things/3
  * https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-security-oauth2-client-authorities-service/api/current-user
