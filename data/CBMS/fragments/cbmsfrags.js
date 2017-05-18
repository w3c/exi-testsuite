// This script takes a set of ESG files and extracts the set of XML fragments that would
// normally be transmitted independently according to the DVB IPDC ESG specification.
//
// Note: This script conforms to the latest Javascript standards, including 
// ECMAScript for XML (ECMA-357). It also depends on host objects from the
// Mozilla Rhino Javascript implementation (see: http://www.mozilla.org/rhino/).

// report usage if needed
if (arguments.length < 1) {
  print("usage: cbmsfrags <input-file>*");
  quit();
}

default xml namespace = "urn:dvb:ipdc:esg:2005";

// process each ESG file given on command-line
for (i = 0; i < arguments.length; i++) {
  // parse the ESG file
  var esg = new XML(readFile(arguments[i]));

  // collect legal fragments defined by DVB IPDC ESG specification
  var fragments = esg..Content 
                + esg..ScheduleEvent 
                + esg..Service 
                + esg..ServiceBundle 
                + esg..Acquisition 
                + esg..Purchase
                + esg..PurchaseChannel;

  // write selected fragments to disk
  for each (frag in fragments) {
    print("writing fragment to file: " + arguments[i] + "." + frag.localName() + frag.childIndex() + ".frag");
    writeFile(arguments[i] + "." + frag.localName() + frag.childIndex() + ".frag", frag.toXMLString());
  }
}

// Use Java to write files. This is not built into the Mozilla Rhino Javascript engine
function writeFile(filename, content) {
  file = new java.io.File(filename);
  os = new java.io.FileOutputStream(file);
  writer = new java.io.OutputStreamWriter(os, "UTF-8");
  writer.write(content);
  writer.close();
}

