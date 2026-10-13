String base = basedir
def file = new File(base, "build.log")
assert file.exists()
assert file.text.contains("Succeeded in deploying verticle")
assert file.text.contains("[LOGBACK-TEST] INFO org.vertx.demo.SimpleVerticle - extra-deps-check-message")
