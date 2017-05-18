EXI test cases of XAL (XML Accelerator Language)
================================================
EXI Contact: gwhite at stanford.edu, 1-Apr-2006

XAL itself, is a software framework for particle accelerator online
modeling software. It makes use of XML descriptions of the devices, such as 
magnets of various field shapes, beam position monitors, and others,  
and XML files of the translation matrices which describe 
how the beam propagates from one device to another.

From: Stanford Linear Accelerator Center (SLAC), Stanford University.
References: http://www.connotea.org/tag/XAL

Relevance to XML: 

XAL is becoming frequently used by the accelerator physics community to model
their machines.

Provenance: 
-----------
Greg White, greg at stanford.edu

Source URI(s):
http://www.connotea.org/tag/XAL

Data
----
These files, although they're from SLAC, in fact describe the Spallation
Neutron Source (SNS).

All files except .dtd are XML.

Some important types of XAL XML file:
1. *.xdxf files model an accelerator or part of an accelerator
2. *.probe file contain the "Twiss" parameters (basically 1st order phase
space characteristics of the accelerator described by an .xdsf
file). That is, the result of a particle tracking program's analysis
of the xdxf file.

