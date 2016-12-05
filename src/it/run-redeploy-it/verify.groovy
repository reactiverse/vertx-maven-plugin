String base = basedir
def file = new File(base, "build.log")
assert file.exists()
assert file.text.contains("Starting the vert.x application in redeploy mode")