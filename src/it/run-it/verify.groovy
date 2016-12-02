def file = new File(basedir, "build.log")
assert file.exists()
assert file.text.contains("Succeeded in deploying verticle")