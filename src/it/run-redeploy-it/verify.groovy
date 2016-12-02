def file = new File(basedir, "build.log")
assert file.exists()
assert file.text.contains("Starting the vert.x application in redeploy mode")