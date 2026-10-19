File filteredFile = new File(basedir, "target/classes/assets/some-text.txt")
assert filteredFile.exists()
String content = filteredFile.text
assert content.contains("aloha")
assert content.contains("This file")
