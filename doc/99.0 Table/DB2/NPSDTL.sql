ALTER TABLE "NPSDTL" DROP CONSTRAINT "PK_NPSDTL";

ALTER TABLE "NPSDTL" 
	ADD CONSTRAINT "PK_NPSDTL" PRIMARY KEY
		("NPSDTL_BAT_NO",
		 "NPSDTL_SEQ_NO");