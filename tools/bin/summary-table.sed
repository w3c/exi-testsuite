#!/bin/sed
# sed script to delete 3 consecutive lines. (This script
# fails under GNU sed earlier than version 3.02.)
: more
$!N
s/\n/&/2;
t enough
$!b more
: enough
/^<\/results-table>\n<?xml version="1.0"?>\n<results-table.*$/d
P;D
#---end of script---
