package com.syscom.fep.mybatis.mbg;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.mybatis.MybatisBaseTest;
import org.springframework.test.context.ActiveProfiles;

//@ActiveProfiles({"integration","mybatis","jms","safeaa","taipei-p1"})
public class TableElemetsGenerator extends MybatisBaseTest {

    private static final String MBG_TABLE_ELEMENT_TEMPLATE = "<table tableName=\"{0}\" enableCountByExample=\"false\" enableUpdateByExample=\"false\" enableDeleteByExample=\"false\" enableSelectByExample=\"false\" selectByExampleQueryId=\"false\" />";
    @Autowired
    private SqlSessionTemplate fepdbSqlSessionTemplate;

    @Test
    public void generate() {
        boolean foundFeptxnTable = false;
        StringBuilder mbgTableElements = new StringBuilder();
        try {
            Connection conn = fepdbSqlSessionTemplate.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT TABNAME FROM SYSCAT.TABLES WHERE TABSCHEMA = 'DBO' AND OWNER = 'DB2ADMIN' AND TYPE = 'T' ORDER BY TABNAME");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String tableName = rs.getString("TABNAME");
                if (tableName.startsWith("SYSCOM")) {
                    if (!("SYSCOMAUDITTRAIL".equals(tableName) || "SYSCOMSERIAL".equals(tableName))) {
                        continue;
                    }
                } else if (tableName.startsWith("BATCH_") || "ENCKEY".equals(tableName) || tableName.startsWith("SYS_EXPORT_") || tableName.startsWith("MD_REGEX_")) {
                    continue;
                } else if (tableName.startsWith("FEPTXN")) {
                    if (foundFeptxnTable) {
                        continue;
                    }
                    foundFeptxnTable = true;
                    tableName = "FEPTXN";
                }
                UnitTestLogger.info("start with ", tableName);
                if (tableName.equalsIgnoreCase("FEPTXN")) {
                    tableName = StringUtils.join(tableName, "01");
                }
                mbgTableElements.append(MessageFormat.format(MBG_TABLE_ELEMENT_TEMPLATE, tableName)).append("\r\n");
            }

            UnitTestLogger.info(mbgTableElements.toString());
            FileUtils.write(new File(FileUtils.getUserDirectory(), CleanPathUtil.cleanString("Desktop/MbgTableElementsForDB2.xml")),
                    StringUtils.join(
                            "<generatorConfiguration>\r\n",
                            mbgTableElements.toString(),
                            "</generatorConfiguration>"),
                    StandardCharsets.UTF_8, false);
        } catch (Exception e) {
            UnitTestLogger.error(e, e.getMessage());
        }
    }
}