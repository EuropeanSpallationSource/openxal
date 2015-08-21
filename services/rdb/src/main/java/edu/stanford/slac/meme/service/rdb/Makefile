# Exceute the service's Makefile.
#
# Usage: You must pass at least one goal. eg make build

MEMEROOT=$(realpath ../../../../../../../../..)

.PHONY : $(MAKECMDGOALS) 

$(MAKECMDGOALS) : 
	$(MAKE) -C ../../../../../../.. $(MAKECMDGOALS) $(MAKEFLAGS)

