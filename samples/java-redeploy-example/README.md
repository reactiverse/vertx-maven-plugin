# Java Redeploy Example

This project illustrates how to configure the Vert.x Maven Plugin to:

* package your application as a _fat jar_
* enable the redeployment of the application during development
* start the application in the background, and stop it.


## Redeployment

First, launch the application in _redeploy mode_ using:

```bash
mvn compile vertx:run
```

Then, open a browser to http://localhost:8040, you should see `Hello World, it works !`. Now edit the 
`org.vertx.demo.MainVerticle` file. For instance, change the `"Hello World, it works !"` to `"Hello Vert.x, it works !"`.
Save the file, the redeployment happens, and in the browser, after a refresh, you see the updated message.

When done with development, hit `CTRL+C`. 

## Packaging

Package the application using:

```bash
mvn clean package
```

It creates a _fat jar_ in the `target` directory:

```
drwxr-xr-x  4 clement  staff   128B Aug  8 10:07 classes
drwxr-xr-x  3 clement  staff    96B Aug  8 10:07 generated-sources
drwxr-xr-x  3 clement  staff    96B Aug  8 10:07 generated-test-sources
-rw-r--r--  1 clement  staff   5.9M Aug  8 10:07 java-redeploy-example-0.1-SNAPSHOT.jar
drwxr-xr-x  3 clement  staff    96B Aug  8 10:07 maven-archiver
drwxr-xr-x  3 clement  staff    96B Aug  8 10:07 maven-status
drwxr-xr-x  4 clement  staff   128B Aug  8 10:07 surefire-reports
drwxr-xr-x  3 clement  staff    96B Aug  8 10:07 test-classes
```

The _fat-jar_ is _executable_, so run it using:

```bash
java -jar target/java-redeploy-example-0.1-SNAPSHOT.jar
```

Hit `CTRL+C` to stop the process.

## Running the application in the background

To run the application in background, use:

```bash
mvn vertx:start
```

To stop it:

```bash
mvn vertx:stop
```

The `application id` is configured in the `pom.xml` file: `<appId>my-vertx-id</appId>`.

Notice that these commands are just sugar on top of the Vert.x launcher `start` and `stop` command.
