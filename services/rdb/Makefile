#!/usr/bin/gmake
#!-*- make -*-
#
# Abs: Compiles and builds MEME rdbService 
# 
# Rem: rdbService is a simple EPICS v4 RPC service to get data out
#      of a relational databaseand make it available as a
#      EPICS V4 PV value.
#
# Usage:
# Usage: IN BASH
#        export MEMEROOT=<pathname of meme dir, containing support/ dir>
#        [optionally source script/setup_meme_HostDefaults_mac.bash  (or other personal host defaults file)]
#        source script/setup_meme.bash
#        cd ../support/
#        [make sure to set Java to at least 1.7. Eg on mac export JAVA_HOME=`/usr/libexec/java_home -v 1.7`]
#        make
#
# Ref: 
# ----------------------------------------------------------------------------
# Auth: 28-Aug-2014, Greg White (greg@slac.stanford.edu)
# Mod:  09-Apr-2015, Greg White (greg@slac.stanford.edu)
#       Edits for ESS install.
#       09-Dec-2014, Greg White (greg@slac.stanford.edu)
#       Use Order-only-prerequisites for directories, see gnu make guide:
#       https://www.gnu.org/software/make/manual/html_node/ \
#          Prerequisite-Types.html
#       29-Aug-2014, Greg White (greg@slac.stanford.edu)
#       Added building jar from classes, so can easily distriute executable
# ============================================================================

SERVICE_ROOT=${MEMEROOT}/services/rdb
JAR_NAME=meme_rdb.jar
JAVAC=javac -Xlint

# EPICS
EPICS_PVJAVA?=/usr/local/esss/epics/epics-java
EPICS_CLASSPATH = $(EPICS_PVJAVA)/pvAccessJava.jar:$(EPICS_PVJAVA)/pvDataJava.jar

# PostgressQL database access
JDBC_CLASSPATH?=$(SERVICE_ROOT)/ext/postgresql-9.4-1201.jdbc41.jar

# CLASSPATH
CLASSPATH = $(MEMEROOT)/support/classes:${EPICS_CLASSPATH}:${JDBC_CLASSPATH}

sources = $(wildcard src/edu/stanford/slac/meme/service/rdb/*.java)
classfiles = $(patsubst src/%.java, classes/%.class, $(sources))
scripts = script/rdbServerRunner
configs = script/rdbserverlogging.properties script/rdb.xdb
jarfile = $(SERVICE_ROOT)/lib/$(JAR_NAME)

classes/%.class : src/%.java
	$(JAVAC) -sourcepath src -classpath $(CLASSPATH) -d classes $<


$(jarfile) : $(classfiles) | lib
	jar -cvf $(jarfile) -C classes/ . 
	jar -uvf $(jarfile) -C ../../support/classes .

build : $(jarfile) | bin
	rsync -u $(scripts) bin/ --exclude '*~' 
	rsync -u $(configs) lib/ --exclude '*~'

$(classfiles) : | classes

classes lib bin :
	mkdir -p classes lib bin

clean :
	rm -fr classes lib bin

