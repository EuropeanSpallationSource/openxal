#-*-bash-*-
#
# envs_epics_prop.bash sets runtime EPICS CA and PVA network confirguration 
# parameters for the production ESS EPICS controls network.
#
# -------------------------------------------------------
# Auth: Greg White, 9-Apr-2015
# Mod:
# =======================================================

# Set gateways and services
# export EPICS_CA_ADDR_LIST=<ESS-EPICS-GATEWAY-ADDRESS>
export EPICS_PVA_ADDR_LIST=physics01.esss.lu.se

# Set ESS production EPICS ports.
export EPICS_CA_REPEATER_PORT=5065
export EPICS_CA_SERVER_PORT=5064

# Stop answers from other servers on the same network 
# (eg those on dev host dev01)
export EPICS_CA_AUTO_ADDR_LIST=FALSE
export EPICS_PVA_AUTO_ADDR_LIST=FALSE
# export EPICS_PVA_BROADCAST_PORT=5076
