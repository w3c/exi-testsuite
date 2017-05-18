-----
Japex
-----

Introduction:

 Japex is a simple yet powerful tool to write Java-based micro-benchmarks. 
 It is similar in spirit to JUnit [1] in that if factors out most of the 
 repetitive programming logic that is necessary to run micro-benchmarks. 
 This logic includes loading and initializing multiple drivers, warming up 
 the VM, timing the inner loop, etc.

 The input to Japex is an XML file describing a test suite. The output is 
 a timestamped report available in both XML and HTML formats (although 
 generation of the latter can be turned off). HTML reports include one or 
 more bar charts generated using JFreeChart (see Building) which graphically 
 display the data for ease of comparison.

Building:

 You MUST set the variable JAPEX_HOME to point to the Japex distribution
 directory. For a CVS source tree, this means setting JAPEX_HOME to 
 point to the 'dist' directory which is created as part of the build
 process. For a binary distribution tree, this means setting JAPEX_HOME
 to point to the root directory of the installation.

 The binary distribution tree contains two directories with jar files
 (some of which are duplicated). These are 'lib' and 'jdsl'. The former
 is intended to be used to set the class path in an Ant script that is
 necessary to start up the harness. The latter is typically used in 
 a Japex config file, for example:

 <param name="japex.classPath" value="${JAPEX_HOME}/jdsl/*.jar"/>

 This is only needed if the benchmarks uses any of the drivers in the
 JDSL (Japex Driver Standard Library). Note that if your benchmark
 extends any of the drivers in JDSL, jar files from 'jdsl' will be
 needed at compilation time as well.
 
Type:

 >> ant dist

 to build the distribution. For convenience, the Japex build file will
 bundle the JFreeChart binaries as part of ./dist/japex.jar (check the
 appropriate licenses if you decide to re-distribute this jar file).

Running Sample:

 See samples/FastInfoset/README.txt

References:

 [1] http://junit.sourceforge.net
 [2] http://www.jfree.org/jfreechart/index.html

--
Contact: Santiago.PericasGeertsen@sun.com

