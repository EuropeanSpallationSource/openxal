#!/usr/bin/gmake
#!-*- make -*-
#
# Abs: Compiles and builds MEME testService 
# 
# Rem: 
#
# Usage: 
#         cd top level meme directory - containing services/ support/ etc
#         export MEMEROOT=`pwd`
#         source script/setup_epics.bash
#         make [clean build]
#
# Ref: 
# ----------------------------------------------------------------------------
# Auth: 10-Oct-2013, Greg White (greg@slac.stanford.edu)
#       09-Dec-2014, Greg White (greg@slac.stanford.edu)
#       Use Order-only-prerequisites for directories, see gnu make guide:
#       https://www.gnu.org/software/make/manual/html_node/Prerequisite-Types.html
# ============================================================================

TESTSERVICE_ROOT=${MEMEROOT}/services/testservice
JAR_NAME=meme_testserver.jar
JAVAC=javac

# EPICS
EPICS_PVJAVA?=/usr/local/esss/epics/epics-java
CLASSPATH=$(EPICS_PVJAVA)/pvAccessJava.jar:$(EPICS_PVJAVA)/pvDataJava.jar:$(MEMEROOT)/support/lib/memesupport.jar

# service specific
sources = $(wildcard src/edu/stanford/slac/meme/service/testservice/*.java)
classfiles = $(patsubst src/%.java, classes/%.class, $(sources))
scripts = script/testServerRunner script/testServerClientRunner
configs = script/testserverlogging.properties
jarfile = $(TESTSERVICE_ROOT)/lib/$(JAR_NAME)


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

