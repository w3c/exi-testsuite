Requirements
------------

JDK 5.0 is required to build and run.
(Onward JDK versions, including JDK 6.0 are *not* supported as of this release
 of EXI Framework.)

Directory structure
-------------------

The directory 'japex' contains the Japex distribution.

The directory 'data' contains the test case XML documents.

The directory 'framework' contains the framework code for measurement of 
properties. A candidate will use this framework to implement concrete Japex
drivers for the measurement of properties of the candidate.

The directory 'candidate' contains sub-directories for each candidate that 
contain the Japex drivers and support code specific to that candidate. Note
that a candidate may or may not include implementation binaries in CVS. 

The directory 'config' contains the Japex configuration files for the
measurement of properties. The configuration files are composed of configuration
files for drivers and tests cases.

Building
--------

To build, type the following:

  ant

Building is required before running.


Running
-------

To run (after building) the measurement for the compaction property on all
available candidates and test data type the following:

  ant run-compaction

After measurement is complete a japex report will be generated and placed in 
the reports directory.
