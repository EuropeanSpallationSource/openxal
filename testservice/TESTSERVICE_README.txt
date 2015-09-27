MEME TESTSERVICE_README.txt

This is the README file of the TESTSERVICE of the MAD EPICS Matlab Environment (MEME).

The test Service is a minimal EPICS V4 service which demonstrates the 
use of EPICS V4 RPC support and structured return objects.

Auth: Greg White, 29-Aug-2014 
Mod:  

      
EXAMPLE
-------
  # Set up EPICS V4
  cd ~/Development/epicsV4/R4.3.0/
  source epicsHostDefaults_mac.bash   
  source ENVS.bash 

  eget -s archiveservice -a entity=quad45:bdes/history \
      -a starttime=2011-09-16T02.12.00 -a endtime=2011-09-16T15.23.17
                  sampled time                 sampled value
  Fri Sep 16 02:12:56 PDT 2011                          42.2
  Fri Sep 16 04:34:03 PDT 2011                          2.76
  Fri Sep 16 06:08:41 PDT 2011                          45.3

  eget -s qm14:twiss
  unsupported normative type
  structure 
    double energy 0.22
    double psix 20.8572
    double alphax 29.3321
    ...

