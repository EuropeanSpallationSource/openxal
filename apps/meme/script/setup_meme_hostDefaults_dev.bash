#!-*-bash-*- 
#
# This script sets the default or expected values of environment variables 
# as they would be different from Host to host. These can then be used to set 
# the build and runtime environment of MEME.
#
# Usage:
#      source memeHostDefaults_lclsdev.bash prior to running MEME setup
# 
# ----------------------------------------------------------------------------
# Auth: Greg White, SLAC, 5-Sep-2013
# Mod: 
# ============================================================================
  

# Oracle defaults (set for 32 bit, since java on lcls-dev2 and -prod01 is 32 bit)
#
OJDBC_CLASSPATH_HOSTDEF=/afs/slac/package/oracle/@sys/11.1.0/jdbc/lib/ojdbc6.jar
OCI_PATH_HOSTDEF=/afs/slac/package/oracle/@sys/11.1.0/instantclient
ORACLEWALLETS_HOSTDEF=/afs/slac/g/lcls/tools/oracle/wallets
MEME_CONNECTION_STRING_DEF=jdbc:oracle:thin:@slacprod.slac.stanford.edu:1521:SLACPROD

# Java defaults
#
JAVA_HOME_HOSTDEF=/afs/slac/g/lcls/package/java/jdk1.7.0_05

# CVS 
# 
CVSROOT_HOSTDEF='/afs/slac/g/lcls/cvs'
CVSIGNORE_HOSTDEF='O.* *~ *.class classes .classpath *.BAK core* .project workspace tmp bin lib .bck  semantic.cache'
CVS_RSH_HOSTDEF=ssh
CVSEDITOR_HOSTDEF='emacs -nw'

export OJDBC_CLASSPATH_HOSTDEF OCI_PATH_HOSTDEF
export ORACLEWALLETS_DEF_HOSTDEF
export MEME_CONNECTION_STRING_DEF
export CVSROOT_HOSTDEF CVSIGNORE_HOSTDEF CVS_RSH_HOSTDEF CVSEDITOR_HOSTDEF
