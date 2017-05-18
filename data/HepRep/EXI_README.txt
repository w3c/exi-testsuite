EXI test cases of Hep Rep.
====================================================
EXI Contact: gwhite at stanford.edu, 28-Aug-2006

The HepRep Interface Definition forms the central part of a complete
generic interface for client server based particle physics detector
"event" displays. HepRep is also used in medical and astronomical
visualization. The HepRep interface supports all of the desirable
features of a client server event display, provides for the correct
distribution of computing work between the two parts of the system and
effectively addresses the many important maintenance issues involved
in such a system.

The BaBar Collaboration at the Stanford Linear Accelerator Center has
been using HepRep version 1 in their WIRED Event Display since early
2000 and is now converting to HepRep version 2. This new HepRep
version of WIRED will be shared by the Atlas Collaboration at CERN,
the European Laboratory for Particle Physics.

Provenance: 
----------- 
Joeseph Perl, Stanford University.


Data
----
From http://www.slac.stanford.edu/~perl/heprep1xml:

Geant4 High Energy Physics Examples
HEP_G4Data{0-4}.heprep These range in size, 140Kbyte to 32Mbyte, to help us 
fit compaction and efficiency to size, given a constant numerical data density

Geant4 Medical Example:
Medical_G4Data2.heprep (3.1 Mbytes)
Medical_G4Data3.heprep (34.2 Mbytes)

Babar detector offline analysis example:
babar.heprep (2.2Mbytes)

BaBar Online Example
run_51994_(142)_35180b_fa726287.heprep (0.988Mbytes)

from http://www.slac.stanford.edu/~perl/heprep2xml:
Event-1.heprep (2.7 Mbytes)
Event-13.heprep (2.6 Mbytes)

Schema
------
The schema file HepRep.xsd, referenced in the data files, is also provided.
This schema file (as modified by J. Schneider 06/Sep/2006 for the 2001 XML Schema
standard) successfully validates the above heprep files EXCEPT
the Event-*.heprep files.
