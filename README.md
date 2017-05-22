# exi-testsuite
[EXI WG](https://www.w3.org/XML/EXI/) test suite

## Requirements

* ant 
* Java5+

## Build testsuite

* `ant clean`
* `ant dist`

# Run testsuite

## [Canonical EXI](https://www.w3.org/TR/exi-c14n/) 

Run the following ant command which creates canonical EXI documents in the reports folder.

`ant run-iot-c14n-classes-java -DtestCases=config/testCases-canonical/all.xml`

Compare the generated files and folders on a byte by byte level (e.g., try [Winmerge](http://winmerge.org)).

Note: If you want to add your Canonical EXI implementation you need to add a driver in the config folder.
