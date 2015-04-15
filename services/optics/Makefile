#!/usr/bin/gmake
#!-*- make -*-
#
# Abs: Compiles and builds MEME optics service 
# 
# Rem: 
#         cd top level meme directory - containing services/ support/ etc
#         export MEMEROOT=`pwd`
#         source script/setup_epics.bash
#         make [clean build]
#
# Test:  Run on physics01 
#        ./services/optics/bin/opticsServerRunner
#        eget -s optics -a q=QUAD:LI23:901//R
# Ref: 
# ----------------------------------------------------------------------------
# Auth: 28-Aug-2014, Greg White (greg@slac.stanford.edu)
# Mod:  09-Dec-2014, Greg White (greg@slac.stanford.edu)
#       Use Order-only-prerequisites for directories, see gnu make guide:
#       https://www.gnu.org/software/make/manual/html_node/Prerequisite-Types.html
# ============================================================================

OPTICS_ROOT=${MEMEROOT}/services/optics
JAR_NAME=meme_optics.jar
JAVAC = javac -Xlint

# EPICS
EPICS_PVJAVA?=/usr/local/esss/epics/epics-java
EPICS_CLASSPATH=$(EPICS_PVJAVA)/pvAccessJava.jar:$(EPICS_PVJAVA)/pvDataJava.jar

# PostgressQL database access
JDBC_CLASSPATH?=$(OPTICS_ROOT)/ext/postgresql-9.4-1201.jdbc41.jar

# JAMA Matrix lib
JAMA_CLASSPATH = ${OPTICS_ROOT}/ext/Jama-1.0.3.jar

# CLASSPATH
CLASSPATH = :$(MEMEROOT)/support/lib/memesupport.jar:${EPICS_CLASSPATH}:${JDBC_CLASSPATH}:${JAMA_CLASSPATH}

sources = $(wildcard src/edu/stanford/slac/meme/service/optics/*.java)
classfiles = $(patsubst src/%.java, classes/%.class, $(sources))
scripts = script/opticsServerRunner
configs = script/opticslogging.properties
jarfile = $(OPTICS_ROOT)/lib/$(JAR_NAME)

classes/%.class : src/%.java
	$(JAVAC) -sourcepath src -classpath $(CLASSPATH) -d classes $<

$(jarfile) : $(classfiles) | lib
	jar -cvf $(jarfile) -C classes/ . 
	jar -uvf $(jarfile) -C ../../support/classes .

build : $(jarfile) | bin
	rsync -u $(scripts) bin/ --exclude '*~' 
	rsync -u $(configs) lib/ --exclude '*~'
	rsync -avu ext/ opt/  --exclude '*~' 

$(classfiles) : | classes

classes lib bin :
	mkdir -p classes lib bin

clean :
	rm -fr classes lib bin
