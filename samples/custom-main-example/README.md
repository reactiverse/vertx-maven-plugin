# Custom Main Example

This project illustrates how to your own `main` class to start the application.


## Redeployment

First, launch the application in _redeploy mode_ using:

```bash
mvn compile vertx:run
```

Then, open a browser to http://localhost:8040, you should see `Ola World, it works !`. The Vert.x instance and the verticle 
 are managed in our own _main_ class (`org.vertx.demo.MyMain`). You can change this class, it triggers the 
redeployment.

The main class is configured in the `pom.xml` file using the `vertx.launcher` property:

```xml
<vertx.launcher>org.vertx.demo.MyMain</vertx.launcher>
```

When done with development, hit `CTRL+C`. 

## Packaging

Package the application using:

```bash
mvn clean package
```

The _fat-jar_ is _executable_, so run it using:

```bash
java -jar target/custom-main-example-0.1-SNAPSHOT.jar 
```

Hit `CTRL+C` to stop the process.
