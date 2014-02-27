#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/extlatgenerator`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.extlatgenerator-1.0.0-SNAPSHOT.jar:*" xal.app.extlatgenerator.Main