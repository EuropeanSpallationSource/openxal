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
# Mod:  15-Apr-2015, Greg White, changed EPICS_PVA_ADDR_LIST from physics01
#                    to icsserv02 - the new home for daemons etc
# =======================================================

# Set gateways and services
# export EPICS_CA_ADDR_LIST=<ESS-EPICS-GATEWAY-ADDRESS>
export EPICS_PVA_ADDR_LIST=icsserv02.esss.lu.se

# Set ESS production EPICS ports.
export EPICS_CA_REPEATER_PORT=5065
export EPICS_CA_SERVER_PORT=5064

# Stop answers from other servers on the same network 
# (eg those on dev host dev01)
export EPICS_CA_AUTO_ADDR_LIST=FALSE
export EPICS_PVA_AUTO_ADDR_LIST=FALSE
# export EPICS_PVA_BROADCAST_PORT=5076
