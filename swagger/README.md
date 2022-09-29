# Swagger Codegen Instructions

- https://github.com/swagger-api/swagger-codegen/tree/3.0.0
- https://github.com/swagger-api/swagger-codegen/tree/3.0.0#prerequisites

### Prerequisites
If you're looking for the latest stable version (as of Aug 26th, 2022), you can grab it directly from Maven.org (Java 8 runtime at a minimum):

```
wget https://repo1.maven.org/maven2/io/swagger/codegen/v3/swagger-codegen-cli/3.0.35/swagger-codegen-cli-3.0.35.jar -O swagger-codegen-cli.jar

java -jar swagger-codegen-cli.jar -h
java -jar swagger-codegen-cli.jar generate -h
java -jar swagger-codegen-cli.jar config-help -l java
```

Then, download the most recent Event Portal API schema JSON file. From https://openapi-v2.solace.cloud or wherever is the latest (I have an internal dev sneak version).  Then:

```
java -jar swagger-codegen-cli.jar generate \
 -l java \
 -c ./config.json \
 -i ./api-docs-v2.json \
 -o out
```


Then once that's built, then you compile it into the JAR that the SDK Wrapper can use.

Note: unless Swagger updates it's Gradle version from archaic 2.x, you can't use Java >= 11.  I use Java 8.

```
$ cd out
$ chmod +x gradlew
$ ./gradlew assemble
.....
:processResources UP-TO-DATE
:classes
:jar
:assemble

BUILD SUCCESSFUL

Total time: 26.477 secs

$ ls build/libs/
ep-swagger-java-0.1.0.jar
```

This file can either be used locally in a `lib` folder, or copied over into Maven.


# TBD: OpenAPITools Codegen

## NONE OF THIS WORKS YET

This would be the preferred auto-codegen tool, as it is more active and more community supported.  It forked from the Swagger codegen a while ago.

However, it does not / cannot build our current verison of the EP API.  Once it does, we'll probably migrate to that.

- https://github.com/OpenAPITools/openapi-generator
- https://github.com/OpenAPITools/openapi-generator#13---download-jar

Would be similar instructions to Swagger, with a few differences (e.g. Swagger: `-l java`, OpenAPI: `-g java`).

```
wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/6.0.1/openapi-generator-cli-6.0.1.jar -O openapi-generator-cli.jar

java -jar openapi-generator-cli.jar generate \
 -g java \
 -i ./api-docs-v2.json \
 -o out2 \
 -c ./openapi-config.json \
 --invoker-package community.solace.ep.client \
 --model-package community.solace.ep.client.model \
 --api-package community.solace.ep.client.api \
 --group-id community.solace \
 --artifact-id ep-openapi-java \
 --artifact-version 0.0.0

```

```
java -jar openapi-generator-cli.jar help generate
java -jar openapi-generator-cli.jar config-help -g java
--strict-spec
--skip-validate-spec
ignoreAnyOfInEnum

```


```
[main] WARN  o.o.codegen.DefaultCodegen - 'BaseMessagingServiceDTO' defines discriminator 'type', but the referenced schema 'SolaceMessagingService' is incorrect. invalid optional definition of type, include it in required
[main] WARN  o.o.codegen.DefaultCodegen - 'BasePolicyDTO' defines discriminator 'type', but the referenced schema 'SolacePolicy' is incorrect. invalid optional definition of type, include it in required
[main] WARN  o.o.codegen.DefaultCodegen - allOf with multiple schemas defined. Using only the first one: ErrorResponse
[main] WARN  o.o.codegen.DefaultCodegen - allOf with multiple schemas defined. Using only the first one: BaseMessagingServiceDTO
[main] WARN  o.o.codegen.DefaultCodegen - allOf with multiple schemas defined. Using only the first one: BasePolicyDTO
[main] WARN  o.o.codegen.DefaultCodegen - 'BasePolicyDTO' defines discriminator 'type', but the referenced schema 'SolacePolicy' is incorrect. invalid optional definition of type, include it in required
[main] WARN  o.o.codegen.DefaultCodegen - 'getEnums_400_response' defines discriminator 'errorType', but the referenced schema 'ErrorResponse' is incorrect. invalid optional definition of errorType, include it in required
[main] WARN  o.o.codegen.DefaultCodegen - 'getEnums_400_response' defines discriminator 'errorType', but the referenced schema 'InvalidStateReference' is incorrect. invalid optional definition of errorType, include it in required
```

Once Solace R&D fixes that, we should be able to use this instead.

