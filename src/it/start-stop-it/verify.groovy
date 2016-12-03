String base = basedir
def file = new File(base, "build.log")
assert file.exists()
assert file.text.contains("Starting vert.x application...")
assert file.text.contains("Stopping vert.x application")
