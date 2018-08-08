# Custom Launcher Example

This project illustrates how to customize the Vert.x Launcher by providing your own. So this example use a main verticle
 and a custom launcher (used to deploy the verticle).


## Redeployment

First, launch the application in _redeploy mode_ using:

```bash
mvn compile vertx:run
```

Then, open a browser to http://localhost:8040, you should see `Bonjour World, it works !`. The `Bonjour` part comes from 
a configuration injected by our custom launcher (`org.vertx.demo.MyLauncher`). You can change this class, it triggers the 
redeployment.

The `Launcher` class is configured in the `pom.xml` file using the `vertx.launcher` property:

```xml
<vertx.launcher>org.vertx.demo.MyLauncher</vertx.launcher>
```

The main verticle is declared in the `vertx.verticle` property.

When done with development, hit `CTRL+C`. 

## Packaging

Package the application using:

```bash
mvn clean package
```

The _fat-jar_ is _executable_, so run it using:

```bash
java -jar target/custom-launcher-example-0.1-SNAPSHOT.jar 
```

Hit `CTRL+C` to stop the process.
