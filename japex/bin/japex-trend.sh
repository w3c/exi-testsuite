#!/bin/sh

#
# Copyright 2005-2006 Sun Microsystems, Inc. All rights reserved.
#
if [ -z "$JAPEX_HOME" ]; then
	echo "ERROR: Set JAPEX_HOME to the root of the Japex distribution"
	exit 1
fi

JAPEX_CLASSPATH=`find ${JAPEX_HOME}/lib -name \*.jar | tr '\n' ':'`:`find ${JAPEX_HOME}/jdsl -name \*.jar | tr '\n' ':'`

if [ -f "/usr/bin/cygpath" ]; then
	CLASSPATH=`/usr/bin/cygpath -wap .:${JAPEX_CLASSPATH}`
else
	CLASSPATH=.:${JAPEX_CLASSPATH}
fi

$JAVA_HOME/bin/java -cp "$CLASSPATH" com.sun.japex.TrendReport "$@"



