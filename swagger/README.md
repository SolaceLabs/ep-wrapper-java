- https://github.com/swagger-api/swagger-codegen/tree/3.0.0
- https://github.com/swagger-api/swagger-codegen/tree/3.0.0#prerequisites

# Prerequisites
If you're looking for the latest stable version (as of Aug 26th, 2022), you can grab it directly from Maven.org (Java 8 runtime at a minimum):

```
wget https://repo1.maven.org/maven2/io/swagger/codegen/v3/swagger-codegen-cli/3.0.35/swagger-codegen-cli-3.0.35.jar -O swagger-codegen-cli.jar

java -jar swagger-codegen-cli.jar --help
```

Then, download the most recent Event Portal API schema JSON file.  Then:

```
java -jar swagger-codegen-cli.jar generate \
 -l java \
 -i ./api-docs-v2.json \
 -o out \
 --invoker-package community.solace.ep.client \
 --model-package community.solace.ep.client.model \
 --api-package community.solace.ep.client.api \
 --group-id community.solace \
 --artifact-id ep-swagger-java \
 --artifact-version 0.1.0
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
