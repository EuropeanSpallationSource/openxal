﻿
ALTER TABLE "MACHINE_MODEL"."RUNS" ALTER COLUMN "MODEL_MODES_ID" TYPE  VARCHAR (60);
UPDATE "MACHINE_MODEL"."RUNS" SET "MODEL_MODES_ID"='from-mebt';