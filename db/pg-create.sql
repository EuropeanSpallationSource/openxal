
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


CREATE OR REPLACE FUNCTION "MACHINE_MODEL"."UPLOAD_MODEL" (
			IN "P_RUNS_ARRAY" 		VARCHAR[],
                        IN "P_MODEL_DEVICES_ARRAY" 	VARCHAR[],
                        IN "P_ELEMENT_MODELS_ARRAY" 	VARCHAR[],
                        OUT P_RUNS_ID              	INTEGER,
                        OUT P_ERRMSG               	VARCHAR) RETURNS RECORD AS
$BODY$
DECLARE
  runs_id integer;
  run VARCHAR[];
  model_devices VARCHAR[];
  element_models VARCHAR[];
BEGIN
  run := "P_RUNS_ARRAY";
  INSERT INTO "MACHINE_MODEL"."RUNS" ("RUN_SOURCE_CHK", "COMMENTS", "MODEL_MODES_ID")
    VALUES(run[1+4],
	   run[1+9], 
           CAST(run[1+10] AS INTEGER)) 
    RETURNING "ID" INTO runs_id;
    -- runs[2] = "MACHINE_MODEL";

  model_devices := "P_MODEL_DEVICES_ARRAY";
  FOR i IN 0..(ARRAY_LENGTH(model_devices,1)/4-1) LOOP
	INSERT INTO "MACHINE_MODEL"."MODEL_DEVICES" ("RUNS_ID", "LCLS_ELEMENTS_ELEMENT_ID", "DEVICE_TYPES_ID", "DEVICE_PROPERTY", "DEVICE_VALUE")
	  VALUES (runs_id,
	     CAST(model_devices[1+i*4+0] AS INTEGER),
	     CAST(model_devices[1+i*4+1] AS INTEGER),
	     model_devices[1+i*4+2],
	     CAST(model_devices[1+i*4+3] AS DOUBLE PRECISION));
  END LOOP;

  element_models := "P_ELEMENT_MODELS_ARRAY";
  FOR i IN 0..(ARRAY_LENGTH(element_models,1)/55-1) LOOP
	INSERT INTO "MACHINE_MODEL"."ELEMENT_MODELS" (
    "RUNS_ID",
    "LCLS_ELEMENTS_ELEMENT_ID",
    "ELEMENT_NAME",
    "INDEX_SLICE_CHK",
    "ZPOS",
    "EK"  ,
    "ALPHA_X",
    "ALPHA_Y",
    "BETA_X" ,
    "BETA_Y" ,
    "PSI_X"  ,
    "PSI_Y"  ,
    "ETA_X"  ,
    "ETA_Y"  ,
    "ETAP_X" ,
    "ETAP_Y" ,
    "R11" ,
    "R12" ,
    "R13" ,
    "R14" ,
    "R15" ,
    "R16" ,
    "R21" ,
    "R22" ,
    "R23" ,
    "R24" ,
    "R25" ,
    "R26" ,
    "R31" ,
    "R32" ,
    "R33" ,
    "R34" ,
    "R35" ,
    "R36" ,
    "R41" ,
    "R42" ,
    "R43" ,
    "R44" ,
    "R45" ,
    "R46" ,
    "R51" ,
    "R52" ,
    "R53" ,
    "R54" ,
    "R55" ,
    "R56" ,
    "R61" ,
    "R62" ,
    "R63" ,
    "R64" ,
    "R65" ,
    "R66" ,
    "LEFF",
    "SLEFF"  ,
    "ORDINAL",
    "SUML")
	  VALUES (runs_id,
	     CAST(element_models[1+i*55+0] AS INTEGER),
	     element_models[1+i*55+1],
	     CAST(element_models[1+i*55+2] AS INTEGER),
	     CAST(element_models[1+i*55+3] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+4] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+5] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+6] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+7] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+8] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+9] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+10] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+11] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+12] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+13] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+14] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+15] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+16] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+17] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+18] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+19] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+20] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+21] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+22] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+23] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+24] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+25] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+26] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+27] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+28] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+29] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+30] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+31] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+32] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+33] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+34] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+35] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+36] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+37] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+38] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+39] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+40] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+41] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+42] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+43] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+44] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+45] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+46] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+47] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+48] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+49] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+50] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+51] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+52] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+53] AS DOUBLE PRECISION),
	     CAST(element_models[1+i*55+54] AS DOUBLE PRECISION)
	     );
  END LOOP;

  P_RUNS_ID := runs_id;
END
$BODY$
LANGUAGE plpgsql;

                        