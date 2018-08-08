# WebPack Frontend Example

This project illustrates how to configure the Vert.x Maven Plugin to develop a web application using WebPack as frontend 
technology. It _repackages_ the web resources when they change. 

## Anatomy

The project use the frontend Maven plugin to install Node, Yarn and execute WebPack. The `node_modules` is located in 
the `target` directory. 

In addition to the `pom.xml` file, the `webpack.config.js` handle the web resource packaging. The `package.json` contains
the dependencies.

## Redeployment

First, launch the application in _redeploy mode_ using:

```bash
mvn compile vertx:run
```

On the first start it downloads node, yarn and webpack. Then it compiles the Java classes and the web resources. This
compilation is described in `webpack.config.js`. The output is copied to `target/classes/webroot` so it is served by the
Vert.x application.

Changing the Java source or the JavaScript source triggers the redeployment. Enjoy!

## Packaging

Package the application using:

```bash
mvn clean package
```

The _fat-jar_ is _executable_, so run it using:

```bash
java -jar target/frontend-example-0.1-SNAPSHOT.jar 
```

Hit `CTRL+C` to stop the process.
