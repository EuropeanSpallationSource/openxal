#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/linaclegodemo`
DIR=`dirname $LINKTARGET`
java -cp "$DIR/../lib/openxal/openxal.library-$OPENXAL_VERSION.jar" se.lu.esss.linaclego.LinacLego "$@"
