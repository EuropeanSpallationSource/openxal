#-*-bash-*-
#
# Name: setup_epicsbase.bash
#
# Summary: This script mich be sourced prior to running EPICS 
# client and service software. 
#
# Usage: source setuo_epicsbase.bash
#
# Description: Source this script to set the filesystem and computer 
# architecture specific things of the computer running 
# EPICS base client and service software. 
#
# NOTE: EPICS network specifics, such as EPICS_CA_ADDR_LIST,
# are NOT defined in this file, so that the same filesystem 
# setup canbe used with different network configs (such as for
# dev and prod EPICS networks).
#
# Referenced environment variables named <something>HOSTDEF,
# for example EPICS_HOST_ARCH_HOSTDEF, are used in case
# a user wants to source a configuration file prior to
# sourcing this file, which sets computer specific defaults,
# for instance to setup both a Mac based and a Linux based 
# enviornment, set the HOSTDEF thinsg first, and then run this
# file. This file would be constant between architectures.
#
# See: envs_epics.bash to set EPICS network configutation.
# 
# -----------------------------------------------------------
# Auth: Greg White, 9-Apr-2015.
# Mod:
# ===========================================================# 

# CPU Architecture of the 
EPICS_HOST_ARCH=${EPICS_HOST_ARCH_HOSTDEF:-linux-x86_64}

# Locations
# Filesystem location of EPICS base and EPICS V4.
EPICS_BASE=${EPICS_BASE_HOSTDEF:-/usr/local/esss/epics/base}
EPICS_PVCPP=${EPICS_PVCPP_HOSTDEF:-/usr/local/esss/epics/epics-cpp}
EPICS_PVJAVA=${EPICS_PVJAVA_HOSTDEF:-/usr/local/esss/epics/epics-java}

# Paths. 
# Add both EPICS base and EPICS V4 libs and bins to PATHS.
#
LD_LIBRARY_PATH=\
${EPICS_BASE}/lib/${EPICS_HOST_ARCH}:\
${EPICS_PVCPP}/pvAccessCPP/lib/${EPICS_HOST_ARCH}:\
${EPICS_PVCPP}/pvDataCPP/lib/${EPICS_HOST_ARCH}:\
${EPICS_PVCPP}/pvCommonCPP/lib/${EPICS_HOST_ARCH}:\
${EPICS_PVCPP}/normativeTypesCPP/lib/${EPICS_HOST_ARCH}:\
${LD_LIBRARY_PATH}

PATH=\
${EPICS_BASE}/bin/${EPICS_HOST_ARCH}:\
${EPICS_PVCPP}/pvAccessCPP/bin/${EPICS_HOST_ARCH}:\
${EPICS_PVCPP}/pvDataCPP/bin/${EPICS_HOST_ARCH}:\
${PATH}

# Exports
#
export EPICS_BASE 
export EPICS_HOST_ARCH
export EPICS_PVCPP EPICS_PVJAVA
export LD_LIBRARY_PATH PATH
