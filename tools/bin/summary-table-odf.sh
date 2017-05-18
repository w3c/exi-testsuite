#!/bin/sh
#
# summary-table-odf: http://www.w3.org/XML/Group/EXI/TTFMS/tools/bin/summary-table-odf.sh
#
# Copyright © 2006 World Wide Web Consortium, (Massachusetts Institute of 
# Technology, European Research Consortium for Informatics and Mathematics, 
# Keio University). All Rights Reserved. This work is distributed under the W3C®
# Software License [1] in the hope that it will be useful, but WITHOUT ANY 
# WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. 
#
# [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
#
#
# 
# Generate an ODF spreadsheet document from the report data.
# The spreadsheet is intended to be used with the 'DataPilot' 
# for data analysis.
#
# Requires the following tools:
#   XSLT - an XSLT processor
#  zip - a zip zool
#
# Uses the following scripts / support files:
#   summary-table.sh - convert Japex report to application-neutral XML
#   summary-table-odf.xsl - convert regular XML-table into ODF format
#   summary-table-odf.ods - an ODF spreadsheet skeleton
#

#
# settings - adapt to your local setup
#
XSLT=xsltproc

# obtain paramters
DIR=$(dirname "$PWD/$0")
NAME=$(basename $0 .sh)

# move to tmp directory
pushd /tmp >/dev/null

# generate table result in tmp-directory
"$DIR/summary-table.sh"

# transform into ODF content.xml
$XSLT "$DIR/$NAME.xsl" summary-table.xml > content.xml

# assemble .ods file
cp "$DIR/$NAME.ods" .
zip -q -X $NAME.ods -m content.xml

# copy result file to current directory
popd >/dev/null
cp /tmp/$NAME.ods .
