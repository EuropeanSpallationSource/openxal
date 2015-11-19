
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
    "LCLS_ELEMENTS_ELEMENT_ID" INTEGER , -- until ESS infrastructure db is created and want link to device specs.
    "CREATED_BY"               VARCHAR (30) NOT NULL DEFAULT CURRENT_USER,
    "DATE_CREATED"             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    "UPDATED_BY"               VARCHAR (30) , --  not used. Original design flaw
--    "DATE_UPDATED"             TIMESTAMP , --  not used. Original design flaw
    "ELEMENT_NAME"             VARCHAR (60) ,
    "INDEX_SLICE_CHK"          INTEGER ,
    "ZPOS"                     NUMERIC ,
    "EK"                       NUMERIC ,
    "ALPHA_X"                  NUMERIC ,
    "ALPHA_Y"                  NUMERIC ,
    "BETA_X"                   NUMERIC ,
    "BETA_Y"                   NUMERIC ,
    "PSI_X"                    NUMERIC ,
    "PSI_Y"                    NUMERIC ,
    "ETA_X"                    NUMERIC ,
    "ETA_Y"                    NUMERIC ,
    "ETAP_X"                   NUMERIC ,
    "ETAP_Y"                   NUMERIC ,
    "R11"                      NUMERIC ,
    "R12"                      NUMERIC ,
    "R13"                      NUMERIC ,
    "R14"                      NUMERIC ,
    "R15"                      NUMERIC ,
    "R16"                      NUMERIC ,
    "R21"                      NUMERIC ,
    "R22"                      NUMERIC ,
    "R23"                      NUMERIC ,
    "R24"                      NUMERIC ,
    "R25"                      NUMERIC ,
    "R26"                      NUMERIC ,
    "R31"                      NUMERIC ,
    "R32"                      NUMERIC ,
    "R33"                      NUMERIC ,
    "R34"                      NUMERIC ,
    "R35"                      NUMERIC ,
    "R36"                      NUMERIC ,
    "R41"                      NUMERIC ,
    "R42"                      NUMERIC ,
    "R43"                      NUMERIC ,
    "R44"                      NUMERIC ,
    "R45"                      NUMERIC ,
    "R46"                      NUMERIC ,
    "R51"                      NUMERIC ,
    "R52"                      NUMERIC ,
    "R53"                      NUMERIC ,
    "R54"                      NUMERIC ,
    "R55"                      NUMERIC ,
    "R56"                      NUMERIC ,
    "R61"                      NUMERIC ,
    "R62"                      NUMERIC ,
    "R63"                      NUMERIC ,
    "R64"                      NUMERIC ,
    "R65"                      NUMERIC ,
    "R66"                      NUMERIC ,
    "LEFF"                     NUMERIC ,
    "SLEFF"                    NUMERIC ,
    "ORDINAL"                  NUMERIC ,
    "SUML"                     NUMERIC ,
    PRIMARY KEY ( "ID" )
  );

--  ALTER TABLE "MACHINE_MODEL"."ELEMENT_MODELS" ADD CONSTRAINT CKC_INDEX_SLICE_CHK_ELEMENT_ CHECK ( "INDEX_SLICE_CHK" IS NULL OR ( "INDEX_SLICE_CHK" IN (0,1,2) )) ;

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


-- displays all GOLD models, the last DESIGN and EXTAND marked with 'PRESENT'
CREATE OR REPLACE VIEW "MACHINE_MODEL"."V_GOLD_REPORT" AS
(SELECT G."ID", G."RUNS_ID" as "RUN_ID", 'PRESENT' as "GOLD_STATUS_NO_CSS", G."COMMENTS", G."CREATED_BY", G."DATE_CREATED", R."RUN_SOURCE_CHK"  FROM "MACHINE_MODEL"."GOLD" G, "MACHINE_MODEL"."RUNS" R
WHERE 
  G."RUNS_ID" = R."ID" and
  R."RUN_SOURCE_CHK" = 'DESIGN'
ORDER BY 
  G."DATE_CREATED" DESC
LIMIT 1)

UNION

(SELECT G."ID", G."RUNS_ID" as "RUN_ID", 'PRESENT' as "GOLD_STATUS_NO_CSS", G."COMMENTS", G."CREATED_BY", G."DATE_CREATED", R."RUN_SOURCE_CHK"  FROM "MACHINE_MODEL"."GOLD" G, "MACHINE_MODEL"."RUNS" R
WHERE 
  G."RUNS_ID" = R."ID" and
  R."RUN_SOURCE_CHK" = 'EXTANT'
ORDER BY 
  G."DATE_CREATED" DESC
LIMIT 1)

UNION

(SELECT G."ID", G."RUNS_ID" as "RUN_ID", 'PREVIOUS' as "GOLD_STATUS_NO_CSS", G."COMMENTS", G."CREATED_BY", G."DATE_CREATED", R."RUN_SOURCE_CHK"  FROM "MACHINE_MODEL"."GOLD" G, "MACHINE_MODEL"."RUNS" R
WHERE 
  G."RUNS_ID" = R."ID"
ORDER BY 
  G."DATE_CREATED" DESC
);


-- At SLAC this table makes a link between a device name and the model runs that have optics (Twiss and R-matrices) for that device, via a FK to device ids
-- in the infrastructure DB. But for ESS that FK has been removed for now. In future you will need some mechanism like that
-- so one can query for optics based on a PV name which is in turn based on the device name. This table, with an appropriate column added to identify
-- a device, will form the translation from device name to runs containing elements corresponding to that device.
CREATE TABLE "MACHINE_MODEL"."MODEL_DEVICES"
  (
    "ID"                       SERIAL ,
    "RUNS_ID"                  INTEGER REFERENCES "MACHINE_MODEL"."RUNS" ( "ID" ) ON DELETE CASCADE,
    "LCLS_ELEMENTS_ELEMENT_ID" INTEGER , -- Until you have an infrastructure db, delete it.
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


CREATE OR REPLACE FUNCTION "MACHINE_MODEL"."GOLD_THIS_MODEL"(IN "P_RUNS_ID" INTEGER, IN "P_COMMENTS" VARCHAR (200), OUT "P_ERRMSG" VARCHAR) RETURNS VARCHAR AS
$BODY$
BEGIN
  INSERT INTO "MACHINE_MODEL"."GOLD" ("RUNS_ID", "COMMENTS") VALUES ("P_RUNS_ID", "P_COMMENTS");
END
$BODY$
LANGUAGE plpgsql;

