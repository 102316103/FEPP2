ALTER TABLE "BATCH_JOB_DETAILS" ALTER COLUMN "JOB_DATA" SET DATA TYPE BLOB(5000);
ALTER TABLE "BATCH_TRIGGERS" ALTER COLUMN "JOB_DATA" SET DATA TYPE BLOB(5000);
ALTER TABLE "BATCH_BLOB_TRIGGERS" ALTER COLUMN "BLOB_DATA" SET DATA TYPE BLOB(5000);
ALTER TABLE "BATCH_CALENDARS" ALTER COLUMN "CALENDAR" SET DATA TYPE BLOB(5000);