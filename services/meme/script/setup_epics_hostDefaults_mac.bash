#!-*-bash-*- 
#
# This script sets the default or expected values of environment variables 
# as they would be different from Host to host. These can then be used to set 
# the build and runtime environment of MEME on a personal mac.
#
# Usage:
# 
# ----------------------------------------------------------------------------
# Auth: Greg White, SLAC, 5-Sep-2013
# Mod:  Greg White, 28-Jun-2014
#       Added MEMEROOT_HOSTDEF
# ============================================================================

EPICS_HOST_ARCH_HOSTDEF=darwin-x86

# Example EPICS mac location
EPICS_BASE_HOSTDEF=/usr/local/epics/base
EPICS_PVJAVA_HOSTDEF=/usr/local/epics/base/EPICS-Java-4.4.0
EPICS_PVCPP_HOSTDEF=/usr/local/epics/base/EPICS-CPP-4.4.0

# Java defaults
#
JAVA_HOME_HOSTDEF=`/usr/libexec/java_home -v 1.7`

export EPICS_BASE_HOSTDEF EPICS_PVJAVA_HOSTDEF EPICS_PVCPP_HOSTDEF JAVA_HOME_HOSTDEF

