# Kotlin Example

This project illustrates how to configure the Vert.x Maven Plugin to develop and package an application using Kotlin:

* package your application as a _fat jar_
* enable the redeployment of the application during development


## Redeployment

First, launch the application in _redeploy mode_ using:

```bash
mvn compile vertx:run
```

Then, open a browser to http://localhost:8080, you should see the message. Edit the `org.vertx.demo.MyKotlinVerticle` 
verticle and the redeployment is triggered.

When done with development, hit `CTRL+C`. 

## Packaging

Package the application using:

```bash
mvn clean package
```

The _fat-jar_ is _executable_, so run it using:

```bash
java -jar target/kotlin-example-0.1-SNAPSHOT.jar 
```

Hit `CTRL+C` to stop the process.
