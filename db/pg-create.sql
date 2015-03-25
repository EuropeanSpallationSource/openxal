
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
