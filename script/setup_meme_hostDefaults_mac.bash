#!-*-bash-*- 
#
# This script sets the default or expected values of environment variables 
# as they would be different from Host to host. These can then be used to set 
# the build and runtime environment of MEME.
#
# Usage:
#      source memeHostDefaults_mac.bash prior to running MEME setup
# 
# ----------------------------------------------------------------------------
# Auth: Greg White, SLAC, 5-Sep-2013
# Mod:  Greg White, 28-Jun-2014
#       Added MEMEROOT_HOSTDEF
# ============================================================================


# The root of MEME development
MEMEROOT_HOSTDEF=/Users/greg/Development/meme/lclscvs/package/meme
  
# Oracle defaults
#
OJDBC_CLASSPATH_HOSTDEF=/Applications/Oracle/Instantclient_11_2_bl64/ojdbc6.jar
# /Applications/instantclient_11_2/ojdbc6.jar
OCI_PATH_HOSTDEF=/Applications/instantclient_11_2
ORACLEWALLETS_HOSTDEF=/usr/local/lcls/tools/oracle/wallets
MEME_CONNECTION_STRING_DEF=jdbc:oracle:thin:@slacprod.slac.stanford.edu:1521:SLACPROD
MEME_CONNECTION_PWD_DEF=""

# Java defaults
#
JAVA_HOME_HOSTDEF=`/usr/libexec/java_home -v 1.7`

# CVS 
# 
CVSROOT_HOSTDEF=':ext:greg@rhel6-64.slac.stanford.edu:/afs/slac/g/lcls/cvs'
CVSIGNORE_HOSTDEF='O.* *~ *.class classes .classpath *.BAK core* .project workspace tmp bin lib opt .bck .DS_Store semantic.cache CVS'
CVS_RSH_HOSTDEF=ssh
CVSEDITOR_HOSTDEF='emacs -nw'

export MEMEROOT_HOSTDEF
export OJDBC_CLASSPATH_HOSTDEF OCI_PATH_HOSTDEF
export ORACLEWALLETS_DEF_HOSTDEF
export MEME_CONNECTION_STRING_DEF
export MEME_CONNECTION_PWD_DEF
export CVSROOT_HOSTDEF CVSIGNORE_HOSTDEF CVS_RSH_HOSTDEF CVSEDITOR_HOSTDEF
