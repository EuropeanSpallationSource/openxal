#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/pvhistogram`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.pvhistogram-1.0.0-SNAPSHOT.jar:*" -DOPENXAL_CONF=${CODAC_CONF}/openxal xal.app.pvhistogram.Main