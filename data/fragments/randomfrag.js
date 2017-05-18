// The purpose of this simple script is to create XML fragment(s) from the input-file(s)
// by extracting a set of random elements from the input-file(s). The number of elements
// extracted can be controlled using the <probability> argument. A probability of 0.25
// will extract roughly 25% of the elements in the document.

// report usage if needed
if (arguments.length < 2) {
  print("usage: randomfrag <probability> <input-file>*");
  quit();
}

// preserve whitespace of original input-file(s)
XML.prettyPrinting = false;
XML.ignoreWhitespace = false;

var probability = arguments[0];

// process each input file
for (i = 1; i < arguments.length; i++) {
  // read the XML
  var doc = new XML(readFile(arguments[i]));

  // extract random fragments
  var result = "";
  var selected = 0;
  for each (elem in doc..*) {
    if (elem.nodeKind() == "element" && Math.random() < probability) {
      result += elem.toXMLString();
      selected++
    }
  }

  print("writing file: " + arguments[i] + ".frag with " + selected + " of " + doc..*.length() + " fragments selected.");
  writeFile(arguments[i] + ".frag", result);
}

// Use Java to write files
function writeFile(filename, content) {
  file = new java.io.File(filename);
  os = new java.io.FileOutputStream(file);
  writer = new java.io.OutputStreamWriter(os, "UTF-8");
  writer.write(content);
  writer.close();
}

