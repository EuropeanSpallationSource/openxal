#-*-bash-*-
#
# envs_epics_prop.bash sets runtime EPICS CA and PVA network confirguration 
# parameters for the production ESS EPICS controls network specifically
# with respect to the MEME system of EPICS V4 services.
#
# It's expected that the functional contents of this file would be
# copied to a general epics setup file for all host side clients in ESS.
#
# -------------------------------------------------------
# Auth:  9-Apr-2015, Greg White
# Mod:   16-Apr-2015, Greg White, Per advice of Matej Sekornaja, commented
#        out setting for EPICS_PVA_ADDR_LIST and EPICS_PVA_AUTO_ADDR_LIST to
#        work around apparent bug or server firewall config problem
#        when connecting physics* machines to icsserv02. Matej is looking
#        into this. When fixed, these two env vars should be set again.
#        15-Apr-2015, Greg White, changed EPICS_PVA_ADDR_LIST from physics01
#        to icsserv02 - the new home for daemons etc
# =======================================================

# Set gateways and services
# export EPICS_CA_ADDR_LIST=<ESS-EPICS-GATEWAY-ADDRESS>

# Commented out. See comment 16-Apr-2015.
#export EPICS_PVA_ADDR_LIST=icsserv02.esss.lu.se

# Set ESS production EPICS ports.
export EPICS_CA_REPEATER_PORT=5065
export EPICS_CA_SERVER_PORT=5064

# Stop answers from other servers on the same network 
# (eg those on dev host dev01)
export EPICS_CA_AUTO_ADDR_LIST=FALSE

# Commented out. See comment 16-Apr-2015
# export EPICS_PVA_AUTO_ADDR_LIST=FALSE

# export EPICS_PVA_BROADCAST_PORT=5076
