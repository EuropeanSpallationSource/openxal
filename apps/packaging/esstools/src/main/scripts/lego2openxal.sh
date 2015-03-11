#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/lego2openxal`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java $1 -cp ":*" org.openepics.discs.exporters.OpenXALExporter
