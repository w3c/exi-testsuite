EXI test cases of Epics Archiver control system archive data.
====================================================
EXI Contact: gwhite at stanford.edu, 28-Aug-2006

The EPICS (Experimental Physics and Industrial Control System)
Archiver continually stores the value of many hundreds of thousands of
data points in large experiment controls systems. Such data are
typically temperature sensors, actuator setpoints, vacuum or gas
pressure readings, telescope mirror alignment calibrations, etc. The
archiver also acts as an server to allow clients to query the
historical value of some named archived quantity over some time
period. That server s implemented in XMLRPC, and the methodResponse of
such a query is presented.
 
Provenance: 
----------- 
Sergei Chevtsov, Stanford University.


Data
----
xmlrpcMethodResponse.xml

Schema
------
No XML Schema is defined for the XML rpc interaction.
