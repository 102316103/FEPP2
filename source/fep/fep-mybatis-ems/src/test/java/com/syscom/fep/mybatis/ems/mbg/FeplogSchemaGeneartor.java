package com.syscom.fep.mybatis.ems.mbg;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.CleanPathUtil;

public class FeplogSchemaGeneartor {
	@Test
	public void test() throws IOException {
		StringBuilder sb = new StringBuilder();
		String tableName = "";
		for (int i = 1; i <= 7; i++) {
			tableName = "FEPLOG" + StringUtils.leftPad(String.valueOf(i), 1, '0');
			sb.append("DROP TABLE \"" + tableName + "\";\r\n");
			sb.append("CREATE TABLE \"" + tableName + "\" (\r\n"
					+ "	\"LOGNO\" BIGINT NOT NULL, \r\n"
					+ "	\"LOGDATE\" TIMESTAMP, \r\n"
					+ "	\"THREADID\" VARCHAR(25), \r\n"
					+ "	\"EJ\" DECIMAL(10,0), \r\n"
					+ "	\"CHANNEL\" VARCHAR(7), \r\n"
					+ "	\"MESSAGEID\" VARCHAR(30), \r\n"
					+ "	\"MESSAGEFLOW\" VARCHAR(25), \r\n"
					+ "	\"PROGRAMFLOW\" VARCHAR(15), \r\n"
					+ "	\"PROGRAMNAME\" VARCHAR(100), \r\n"
					+ "	\"STAN\" VARCHAR(8), \r\n"
					+ "	\"ATMSEQ\" VARCHAR(8), \r\n"
					+ "	\"ATMNO\" VARCHAR(8), \r\n"
					+ "	\"TRINBANK\" VARCHAR(6), \r\n"
					+ "	\"TRINACTNO\" VARCHAR(16), \r\n"
					+ "	\"TROUTBANK\" VARCHAR(6), \r\n"
					+ "	\"TROUTACTNO\" VARCHAR(16), \r\n"
					+ "	\"TXDATE\" VARCHAR(10), \r\n"
					+ "	\"TXMESSAGE\" CLOB, \r\n"
					+ "	\"REMARK\" CLOB, \r\n"
					+ "	\"BKNO\" CHAR(3), \r\n"
					+ "	\"STEPS\" DECIMAL(10,0)\r\n"
					+ "	);\r\n"
					+ "ALTER TABLE \"" + tableName + "\" ADD CONSTRAINT \"PK_" + tableName + "\" PRIMARY KEY (\"LOGNO\");\r\n\r\n");
			sb.append("DROP SEQUENCE \"" + tableName + "_LOGNO_SEQ\";\r\n"
					+ "CREATE SEQUENCE \"" + tableName + "_LOGNO_SEQ\" AS BIGINT START WITH 1 INCREMENT BY 1 NO MAXVALUE CACHE 20;\r\n\r\n");
			sb.append("CREATE OR REPLACE TRIGGER \"" + tableName + "_LOGNO_SEQ_TRIG\"\r\n"
					+ "  BEFORE INSERT ON \"" + tableName + "\"\r\n"
					+ "  REFERENCING NEW AS \"NEW\"\r\n"
					+ "  FOR EACH ROW\r\n"
					+ "BEGIN\r\n"
					+ "  DECLARE V_NEWVAL BIGINT DEFAULT 0;\r\n"
					+ "  DECLARE V_INCVAL BIGINT DEFAULT 0;\r\n"
					+ "  SELECT NEXTVAL FOR " + tableName + "_LOGNO_SEQ INTO V_NEWVAL FROM SYSIBM.SYSDUMMY1;\r\n"
					+ "  -- If this is the first time this table have been inserted into (sequence == 1)\r\n"
					+ "  IF V_NEWVAL = 1 THEN\r\n"
					+ "    --get the max indentity value from the table\r\n"
					+ "    SELECT NVL(MAX(LOGNO), 0) INTO V_NEWVAL FROM " + tableName + ";\r\n"
					+ "    SET V_NEWVAL = V_NEWVAL + 1;\r\n"
					+ "    --set the sequence to that value\r\n"
					+ "    FETCH_LOOP :\r\n"
					+ "    LOOP \r\n"
					+ "      IF V_INCVAL >= V_NEWVAL THEN\r\n"
					+ "        LEAVE FETCH_LOOP;\r\n"
					+ "      END IF;\r\n"
					+ "      SELECT NEXTVAL FOR " + tableName + "_LOGNO_SEQ INTO V_INCVAL FROM SYSIBM.SYSDUMMY1;\r\n"
					+ "     END LOOP FETCH_LOOP;\r\n"
					+ "  END IF;\r\n"
					+ "  -- assign the value from the sequence to emulate the identity column\r\n"
					+ "  SET NEW.LOGNO = V_NEWVAL;\r\n"
					+ "END;\r\n\r\n\r\n");
		}

		FileUtils.writeStringToFile(new File(CleanPathUtil.cleanString("C:/Users/Richard/Desktop/FEPLOG_db2.sql")), sb.toString(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void generateMBG() {
		String tableName = "";
		for (int i = 1; i <= 31; i++) {
			tableName = "FEPTXN" + StringUtils.leftPad(String.valueOf(i), 2, '0');
			LogHelperFactory.getTraceLogger().info(
					"<table tableName=\"" + tableName
							+ "\" enableCountByExample=\"false\" enableUpdateByExample=\"false\" enableDeleteByExample=\"false\" enableSelectByExample=\"false\" selectByExampleQueryId=\"false\" />");
		}
	}
}
