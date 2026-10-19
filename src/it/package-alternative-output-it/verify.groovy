File contextDir = new File(basedir, "target/context")
assert contextDir.isDirectory()
assert contextDir.list().any { it.endsWith(".jar") }
