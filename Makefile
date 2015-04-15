# Top level general makefile for MEME
# 
# Usage: make [ACTION={clean,build}]  (ACTION must be uppercase)
# Example: make ACTION=build
#=======================================

all : support testservice optics rdb

support : FORCE
	$(MAKE) -C support $(ACTION)

testservice : support
	$(MAKE) -C services/testservice $(ACTION)

optics : support
	$(MAKE) -C services/optics $(ACTION)

rdb : support
	$(MAKE) -C services/rdb $(ACTION)

build : ACTION=build 
build : testservice optics rdb

FORCE :
