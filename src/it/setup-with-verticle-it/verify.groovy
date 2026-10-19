import io.reactiverse.vertx.maven.plugin.Verify

File pomFile = new File(basedir, "pom.xml")
Verify.verifySetup(pomFile)

assert new File(basedir, "src/main/java/org/acme/MyVerticle.java").exists()

String pomContent = pomFile.text
assert pomContent.contains("<vertx.verticle>org.acme.MyVerticle</vertx.verticle>")
