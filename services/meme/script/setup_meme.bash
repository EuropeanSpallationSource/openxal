# Set the root of the MEME project tree
export MEMEROOT=${MEMEROOT:-${MEMEROOT_HOSTDEF:-/afs/slac/g/lcls/package/meme}}

# Oracle dev
export OJDBC_CLASSPATH=\
${OJDBC_CLASSPATH_HOSTDEF:-/afs/slac/package/oracle/@sys/11.2.0_64/jdbc/lib/ojdbc6.jar}
export ORACLEWALLETS=\
${ORACLEWALLETS_HOSTDEF:-/afs/slac/g/lcls/tools/oracle/wallets}

# CVS 
# 
export CVSROOT=${CVSROOT_HOSTDEF:-':ext:greg@rhel6-64.slac.stanford.edu:/afs/slac/g/lcls/cvs'}
export CVSIGNORE=${CVSIGNORE_HOSTDEF:-'O.* *~ *.class .classpath *.BAK core* .project workspace tmp bin lib .bck .DS_Store semantic.cache'}
export CVS_RSH=${CVS_RSH_HOSTDEF:-ssh}
export CVSEDITOR=CVSEDITOR_HOSTDEF:-'emacs -nw'}
