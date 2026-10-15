String base = basedir
def file = new File(base, "build.log")
assert file.exists()
assert file.text.contains("Succeeded in deploying verticle")
assert !file.text.contains("Running in module:")
assert file.text.contains("Falling back to classpath mode.")
assert file.text.contains("verticle is loaded by class name")
