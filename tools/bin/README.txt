This directory contains scripts & tools for use with the EXI test suite. 
We hope you will find them useful.


summary-table (summary-table.sh/summary-table.bat)

Generates an application-independent XML file (summary-table.xml) from the 
results of the Japex reports found in <TTFMS>/reports/*/report.xml. The 
result file data contains the measurement data in tabular structure. It 
can be opened directly in e.g. MS Excel, and contains the data suitable 
for use with the 'PivotTable' function. The 'PivotTable' allows flexible 
& easy summarization, filtering & graphing of the data.


summary-table-odf (summary-table-odf.sh/summary-table-odf.bat)

Generates an OpenDocument spreadsheet file (summary-table-odf.ods) containing 
the summary-table data. This file can be opened directly with OpenOffice.org 
or any other OpenDocument-compliant application. The filtering/summarization 
tool of OpenOffice is called 'DataPilot', and pretty much allows the same 
analysis as described above.
