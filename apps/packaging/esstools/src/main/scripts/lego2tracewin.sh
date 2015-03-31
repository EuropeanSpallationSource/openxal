#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/lego2tracewin`
DIR=`dirname $LINKTARGET`
java -cp "$DIR/../lib/openxal/openxal.library-$OPENXAL_VERSION.jar" org.openepics.discs.exporters.TraceWinExporter "$@"
