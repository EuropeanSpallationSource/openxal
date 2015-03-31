#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/experimentautomator`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -jar "openxal.apps.experimentautomator-$OPENXAL_VERSION.jar"
