
-- Keeps track of each tracking "run" done by XAL GUI.
-- Each row in the GUI's runs table corresponds to one row of this table.
CREATE TABLE "MACHINE_MODEL"."RUNS"
  (
    "ID"                   SERIAL ,
--    "HARDWARE_SETTINGS_ID" INTEGER , -- no written by anything. Original design over-engineering.
--    "XML_DOCS_ID"          INTEGER , -- Original design over-engineering.
    "CREATED_BY"           VARCHAR (30) NOT NULL DEFAULT CURRENT_USER ,
    "DATE_CREATED"         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--    "UPDATED_BY"           VARCHAR (30) , -- Original design flaw.
--    "DATE_UPDATED"         TIMESTAMP , -- Original design flaw.
    "RUN_SOURCE_CHK"       VARCHAR (60) ,
--    "RUN_ELEMENT_FILENAME" VARCHAR (60) , --- Never updated, original design over-engineering.
    "RUN_ELEMENT_DATE"     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    "RUN_DEVICE_FILENAME"  VARCHAR (60) , -- - Never updated, original design over-engineering.
    "RUN_DEVICE_DATE"      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    "COMMENTS"             VARCHAR (200) ,
    "MODEL_MODES_ID"       INTEGER,
    PRIMARY KEY ( "ID" )
  );

ALTER TABLE "MACHINE_MODEL"."RUNS" ADD CONSTRAINT CKC_RUN_SOURCE_CHK_RUNS CHECK ( "RUN_SOURCE_CHK" IS NULL OR ( "RUN_SOURCE_CHK" IN ('DESIGN','EXTANT') )) ;
  
-- Keeps track of the kind of device we're talking about; E.g. MONItor (aka BPM), QUAD, XCOR etc.
-- In particualar DEFAULT_SLICING_POS_CHK is the number of "slices" at which optics are recorded for each device type.
-- For a QUAD there are 3 - beginning, middle and end. For all others only 1.
CREATE TABLE "MACHINE_MODEL"."DEVICE_TYPES"
  (
    "ID"                      SERIAL,
    "DEVICE_TYPE"             VARCHAR (4) NOT NULL UNIQUE,
    "CREATED_BY"              VARCHAR (30) NOT NULL ,
    "DATE_CREATED"            TIMESTAMP NOT NULL ,
    "UPDATED_BY"              VARCHAR (30) DEFAULT CURRENT_USER,
    "DATE_UPDATED"            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "DEFAULT_SLICING_POS_CHK" INTEGER,
    PRIMARY KEY ( "ID" )
  );

 ALTER TABLE "MACHINE_MODEL"."DEVICE_TYPES" ADD CONSTRAINT CKC_DEFAULT_SLICING_P_DEVICE_T CHECK ( "DEFAULT_SLICING_POS_CHK" IS NULL OR ( "DEFAULT_SLICING_POS_CHK" IN (0,1,2) )) ;

-- The Twiss parameters and R-matrices for each device element, for each run. 
-- Ie, the number of rows = roughly number of runs x number of elements in each model (so a big table).
-- Most devices are equiv to one elment, some devices, such correspond to >1.
-- For example quads correspond to 3, being the twiss and R-matrices at the beginning, middle and end of the quadrupole.
 CREATE TABLE "MACHINE_MODEL"."ELEMENT_MODELS"
  (
    "ID"                       SERIAL,
    "RUNS_ID"                  INTEGER REFERENCES "MACHINE_MODEL"."RUNS" ( "ID" ) ON DELETE CASCADE,
--    "LCLS_ELEMENTS_ELEMENT_ID" INTEGER , -- until ESS infrastructure db is created and want link to device specs.
    "CREATED_BY"               VARCHAR (30) NOT NULL DEFAULT CURRENT_USER,
    "DATE_CREATED"             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    "UPDATED_BY"               VARCHAR (30) , --  not used. Original design flaw
--    "DATE_UPDATED"             TIMESTAMP , --  not used. Original design flaw
    "ELEMENT_NAME"             VARCHAR (60) ,
    "INDEX_SLICE_CHK"          INTEGER ,
    "ZPOS"                     DOUBLE PRECISION ,
    "EK"                       DOUBLE PRECISION ,
    "ALPHA_X"                  DOUBLE PRECISION ,
    "ALPHA_Y"                  DOUBLE PRECISION ,
    "BETA_X"                   DOUBLE PRECISION ,
    "BETA_Y"                   DOUBLE PRECISION ,
    "PSI_X"                    DOUBLE PRECISION ,
    "PSI_Y"                    DOUBLE PRECISION ,
    "ETA_X"                    DOUBLE PRECISION ,
    "ETA_Y"                    DOUBLE PRECISION ,
    "ETAP_X"                   DOUBLE PRECISION ,
    "ETAP_Y"                   DOUBLE PRECISION ,
    "R11"                      DOUBLE PRECISION ,
    "R12"                      DOUBLE PRECISION ,
    "R13"                      DOUBLE PRECISION ,
    "R14"                      DOUBLE PRECISION ,
    "R15"                      DOUBLE PRECISION ,
    "R16"                      DOUBLE PRECISION ,
    "R21"                      DOUBLE PRECISION ,
    "R22"                      DOUBLE PRECISION ,
    "R23"                      DOUBLE PRECISION ,
    "R24"                      DOUBLE PRECISION ,
    "R25"                      DOUBLE PRECISION ,
    "R26"                      DOUBLE PRECISION ,
    "R31"                      DOUBLE PRECISION ,
    "R32"                      DOUBLE PRECISION ,
    "R33"                      DOUBLE PRECISION ,
    "R34"                      DOUBLE PRECISION ,
    "R35"                      DOUBLE PRECISION ,
    "R36"                      DOUBLE PRECISION ,
    "R41"                      DOUBLE PRECISION ,
    "R42"                      DOUBLE PRECISION ,
    "R43"                      DOUBLE PRECISION ,
    "R44"                      DOUBLE PRECISION ,
    "R45"                      DOUBLE PRECISION ,
    "R46"                      DOUBLE PRECISION ,
    "R51"                      DOUBLE PRECISION ,
    "R52"                      DOUBLE PRECISION ,
    "R53"                      DOUBLE PRECISION ,
    "R54"                      DOUBLE PRECISION ,
    "R55"                      DOUBLE PRECISION ,
    "R56"                      DOUBLE PRECISION ,
    "R61"                      DOUBLE PRECISION ,
    "R62"                      DOUBLE PRECISION ,
    "R63"                      DOUBLE PRECISION ,
    "R64"                      DOUBLE PRECISION ,
    "R65"                      DOUBLE PRECISION ,
    "R66"                      DOUBLE PRECISION ,
    "LEFF"                     DOUBLE PRECISION ,
    "SLEFF"                    DOUBLE PRECISION ,
    "ORDINAL"                  DOUBLE PRECISION ,
    "SUML"                     DOUBLE PRECISION ,
    PRIMARY KEY ( "ID" )
  );

  ALTER TABLE "MACHINE_MODEL"."ELEMENT_MODELS" ADD CONSTRAINT CKC_INDEX_SLICE_CHK_ELEMENT_ CHECK ( "INDEX_SLICE_CHK" IS NULL OR ( "INDEX_SLICE_CHK" IN (0,1,2) )) ;

-- Which model runs have been designated by operations to be good ones, both presently and in the past.
  CREATE TABLE "MACHINE_MODEL"."GOLD"
  (
    "ID"           SERIAL ,
    "RUNS_ID"      INTEGER REFERENCES "MACHINE_MODEL"."RUNS" ( "ID" ) ON DELETE CASCADE,
    "COMMENTS"     VARCHAR (200) ,
    "CREATED_BY"   VARCHAR (30) NOT NULL DEFAULT CURRENT_USER ,
    "DATE_CREATED" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
--    "UPDATED_BY"   VARCHAR (30) , -- not used. Original design flaw. There is no rowwise modification of this table.
--    "DATE_UPDATED" TIMESTAMP, -- not used. Original design flaw. There is no rowwise modification of this table.
    PRIMARY KEY ( "ID" )
  );


-- TODO probaby a view
  CREATE TABLE "MACHINE_MODEL"."V_GOLD_REPORT"
  (
    "ID"           SERIAL ,
    "RUN_ID"      INTEGER REFERENCES "MACHINE_MODEL"."RUNS" ( "ID" ) ON DELETE CASCADE,
    "GOLD_STATUS_NO_CSS"     VARCHAR (200) ,
    PRIMARY KEY ( "ID" )
  );

-- At SLAC this table makes a link between a device name and the model runs that have optics (Twiss and R-matrices) for that device, via a FK to device ids
-- in the infrastructure DB. But for ESS that FK has been removed for now. In future you will need some mechanism like that
-- so one can query for optics based on a PV name which is in turn based on the device name. This table, with an appropriate column added to identify
-- a device, will form the translation from device name to runs containing elements corresponding to that device.
CREATE TABLE "MACHINE_MODEL"."MODEL_DEVICES"
  (
    "ID"                       SERIAL ,
    "RUNS_ID"                  INTEGER REFERENCES "MACHINE_MODEL"."RUNS" ( "ID" ) ON DELETE CASCADE,
--    "LCLS_ELEMENTS_ELEMENT_ID" INTEGER , -- Until you have an infrastructure db, delete it.
    "DEVICE_TYPES_ID"          INTEGER REFERENCES "MACHINE_MODEL"."DEVICE_TYPES" ( "ID" ) ON DELETE CASCADE,
    "CREATED_BY"               VARCHAR (30) NOT NULL DEFAULT CURRENT_USER ,
    "DATE_CREATED"             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--    "UPDATED_BY"               VARCHAR (30) , -- not used. Original design flaw. There is no rowwise modification of this table.
--    "DATE_UPDATED"             TIMESTAMP , -- not used. Original design flaw. There is no rowwise modification of this table.
    "DEVICE_PROPERTY"          VARCHAR (30) ,
    "DEVICE_VALUE"             DOUBLE PRECISION,
    PRIMARY KEY ( "ID" )
  );

ALTER TABLE "MACHINE_MODEL"."MODEL_DEVICES" ADD CONSTRAINT CKC_MODEL_DEVICES_DEV_PROP CHECK ( "DEVICE_PROPERTY" IS NULL OR ( "DEVICE_PROPERTY" IN ('B','BACT','BDES','A','P','ADES','PDES') )) ;
