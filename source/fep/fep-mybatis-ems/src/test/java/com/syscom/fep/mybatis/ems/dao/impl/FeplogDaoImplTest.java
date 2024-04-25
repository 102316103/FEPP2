package com.syscom.fep.mybatis.ems.dao.impl;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.ems.model.Feplog;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ems.MybatisEmsBaseTest;
import com.syscom.fep.mybatis.ems.dao.FeplogDao;
import com.syscom.fep.mybatis.ems.ext.model.FeplogExt;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeplogDaoImplTest extends MybatisEmsBaseTest {
    private static final String ProgramName = FeplogDaoImplTest.class.getSimpleName();
    @Autowired
    private FeplogDao feplogDao;

    private FeplogExt feplog;

    @BeforeEach
    public void setUp() throws ParseException {
        feplogDao.setTableNameSuffix("1", StringUtils.join(ProgramName, ".setUp"));
        feplog = new FeplogExt();
        feplog.setTableNameSuffix("1");
        feplog.setAtmno("atmno");
        feplog.setAtmseq("atmseq");
        feplog.setBkno("807");
        feplog.setChannel("ATM");
        feplog.setEj(1234567890L);
        feplog.setLogdate(FormatUtil.parseDataTime("20220625", FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        feplog.setMessageflow("Request");
        feplog.setMessageid("0000000000");
        feplog.setProgramflow("WCFIn");
        feplog.setProgramname("ATMHandler");
        feplog.setRemark("remark");
        feplog.setStan("stan");
        feplog.setSteps(1L);
        feplog.setThreadid("main");
        feplog.setTrinactno("trinactno");
        feplog.setTrinbank("trinbk");
        feplog.setTroutactno("troutactno");
        feplog.setTroutbank("troutb");
        feplog.setTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        feplog.setTxmessage("測試我是測試我是UT測試哈哈哈哈哈");
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testFree() {
        feplogDao.insert(feplog);
        feplogDao.insertSelective(feplog);
    }

    @Test
    public void testGetMultiFepLogByDef() throws Exception {
        Date beginDate = FormatUtil.parseDataTime("20220620", FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
        Date endDate = FormatUtil.parseDataTime("20220630", FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);

        feplogDao.insert(feplog);
        feplog.setLogdate(beginDate);
        feplogDao.insert(feplog);
        feplog.setLogdate(endDate);

        feplogDao.getMultiFepLogByDef(feplog, beginDate, endDate, Arrays.asList(feplog.getEj()), 1, 20);
    }
}
