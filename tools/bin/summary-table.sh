#!/bin/sh
#
# summary-table: http://www.w3.org/XML/Group/EXI/TTFMS/tools/bin/summary-table.sh
#
# Copyright (C) 2006 World Wide Web Consortium, (Massachusetts Institute of 
# Technology, European Research Consortium for Informatics and Mathematics, 
# Keio University). All Rights Reserved. This work is distributed under the W3C
# Software License [1] in the hope that it will be useful, but WITHOUT ANY 
# WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE. 
#
# [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
#
# 
# Generate an application-neutral database-like XML table from the report data.
# The result may be used to generate summary tables by loading them in
# spreadsheet programs.
#
# Requires the following tools:
#   XSLT - an XSLT processor
#  sed - a sed (Stream EDitor) tool
#
# Uses the following scripts / support files:
#   summary-table.xsl - convert the Japex reports into database-tables
#   summary-table.sed - 'glue' several XML files together
#

#
# settings - adapt to your local setup
#
XSLT=xsltproc

# obtain program directory & name, so we can access support files
DIR=$(dirname "$0")
NAME=$(basename "$0" .sh)

# run XSLT on japex files & generate output file
for fil in "$DIR"/../../reports/*/report.xml; do $XSLT "$DIR/$NAME.xsl" "$fil"; done | sed -f "$DIR/$NAME.sed" > $NAME.xml
