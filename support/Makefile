#!/usr/bin/gmake
#!-*-make-*-
#
# Abs: Compiles and builds MEME services support classes 
# 
# Rem: 
#
# Usage: IN BASH
#        export MEMEROOT=<pathname of meme dir, containing support/ dir>
#        source script/memeHostDefaults_mac.bash  (or other personal host defaults file)
#        cd ../support/
#        make
#
# Ref: 
# ----------------------------------------------------------------------------
# Auth: 13-Sep-2013, Greg White (greg@slac.stanford.edu)
# Mod:  9-Oct-2014, Greg White (greg@slac.stanford.edu)
#       Add specific jar file versions, and better jar file construction
# ============================================================================

JAVAC=javac

# 4.4
EPICS_PVJAVA?=/usr/local/esss/epics/epics-java
CLASSPATH=$(EPICS_PVJAVA)/pvAccessJava.jar:$(EPICS_PVJAVA)/pvDataJava.jar:$(MEMEROOT)/support/classes

classes/%.class : src/%.java
	$(JAVAC) -sourcepath src -classpath $(CLASSPATH) -d classes $<

sources = $(wildcard src/edu/stanford/slac/meme/support/err/*.java) \
          $(wildcard src/edu/stanford/slac/meme/support/sys/*.java) 
classfiles = $(patsubst src/%.java, classes/%.class, $(sources))


all : prelim $(classfiles)

prelim : 
	mkdir -p classes

clean :
	find classes -name "*.class" -exec rm {} \;

build : all
	mkdir -p lib
	jar -cvf lib/memesupport.jar -C classes/ .

# install : 
