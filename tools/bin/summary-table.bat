@echo off
REM
REM summary-table.bat: http://www.w3.org/XML/Group/EXI/TTFMS/tools/bin/summary-table.bat
REM
REM Copyright © 2006 World Wide Web Consortium, (Massachusetts Institute of 
REM Technology, European Research Consortium for Informatics and Mathematics, 
REM Keio University). All Rights Reserved. This work is distributed under the 
REM W3C® Software License [1] in the hope that it will be useful, but WITHOUT 
REM ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
REM FITNESS FOR A PARTICULAR PURPOSE. 
REM
REM [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
REM
REM 
REM Generate an application-neutral database-like XML table from the report data.
REM The result may be used to generate summary tables by loading them in
REM spreadsheet programs.
REM
REM Requires the following tools:
REM   XSLT - an XSLT processor
REM  SED - a sed (Stream EDitor) tool
REM
REM Uses the following scripts / support files:
REM   summary-table.xsl - convert the Japex reports into database-tables
REM   summary-table.sed - 'glue' several XML files together
REM

setlocal

REM
REM settings - adapt to your local setup
REM

set XSLT=xsltproc.exe
set SED=sed.exe

REM
REM read japex-tables, and generate report
REM

REM obtain program directory, so we can access support files
set PROGRAM_DIRECTORY=%~dps0
set NAME=%~n0
set REPORTS=%PROGRAM_DIRECTORY%../../reports/*/report.xml

REM run XSLT on japex files
%XSLT% %PROGRAM_DIRECTORY%%NAME%.xsl %REPORTS% | %SED% -f %PROGRAM_DIRECTORY%%NAME%.sed > %NAME%.xml

endlocal