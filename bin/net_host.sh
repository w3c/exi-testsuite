#!/bin/bash

java -Xmx1024M -cp `dirname $0`/../framework/dist/ttfms-framework.jar org.w3c.exi.ttf.NetworkHost "$@"
