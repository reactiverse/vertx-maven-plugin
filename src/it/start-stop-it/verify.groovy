def file = new File(basedir, "build.log")
assert file.exists()
assert file.text.contains("Starting vert.x application...")
assert file.text.contains("Stopping vert.x application")
