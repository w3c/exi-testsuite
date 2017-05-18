
This directory contains Distributed Interactive Simulation (DIS) information
saved in an XML format. DIS is an IEEE standard for military simulations,
typically 3D virtual worlds. The IEEE DIS standard is a custom, binary
format. The Naval Postgraduate School has written an XML encoding of DIS,
which is what is saved here. The XML representation is not the product
of a standards body, but has been used in commercial and academic products.

The data collected here was from the AUV Workbench, a 3D visualization program
for working with unmanned autonomous vehicles, in this case underwater robots.
It includes mostly entity state PDUs, which is by far the most common type
of PDU in the DIS world. Many studies of actual DIS traffic show ESPDUs being
95% or more of traffic. The PDUs collected here include articulation parameters, used
in this case to display the orientation of sonar beams. The use of articulation
parameters is not rare, but it isn't always the most common thing in the
world, either. The choices for attribute names of articulation parameters
happen to be particularly verbose, which probably "artificically" inflates
the text XML document size somewhat. The tag and attribute names were chosen
in the interests of clarity for the human reader rather than programming
or storage efficiency.

This data case is interesting because we can compare the size of the 
EXI format to that of the original binary IEEE format. The binary file
has a size of 464,480 bytes, while the text XML file is 3,924,680 bytes,
or a little over eight times as large. I suspect a lot of this size
difference is the result of the very verbose tag names chosen by NPS
in its XML format in the interests of clarity over brevity. A tag replacement
scheme should remove this factor from the file sizes.

Don McGregor