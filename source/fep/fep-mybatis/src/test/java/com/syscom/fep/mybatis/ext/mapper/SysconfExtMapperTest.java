package com.syscom.fep.mybatis.ext.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;

import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Sysconf;

import java.util.List;

public class SysconfExtMapperTest extends MybatisBaseTest {
    @Autowired
    private SysconfExtMapper mapper;

    @Test
    public void testSelectByPrimaryKey() {
        Sysconf actual = mapper.selectByPrimaryKey((short) 4, "APPKINDFTPPassword");
        assertEquals("c2lub3BhYw==", actual.getSysconfValue());
        assertEquals("sinopac", ConvertUtil.toString(Base64Utils.decodeFromString(actual.getSysconfValue()), PolyfillUtil.toCharsetName("950")));
    }

    @Test
    public void testQueryAllData() {
        String orderBy = "SYSCONF_NAME";
        List<Sysconf> list = mapper.queryAllData(orderBy);
        orderBy = "SYSCONF_NAME;DELETE FROM WEBAUTHLOG";
        list = mapper.queryAllData(orderBy);
    }
}
