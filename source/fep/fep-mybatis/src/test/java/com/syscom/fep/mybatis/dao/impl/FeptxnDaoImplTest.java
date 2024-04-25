package com.syscom.fep.mybatis.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.FeptxnExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Feptxn;

// @ActiveProfiles({"integration","mybatis","jms","taipei"})
public class FeptxnDaoImplTest extends MybatisBaseTest {
    private static final String ProgramName = FeptxnDaoImplTest.class.getSimpleName();
    @Autowired
    private FeptxnDao feptxnDao;
    @Autowired
    private FeptxnExtMapper feptxnExtMapper;

    private Feptxn feptxn;

    @BeforeEach
    public void setUp() {
        feptxnDao.setTableNameSuffix("01", StringUtils.join(ProgramName, ".setUp"));

        feptxn = new FeptxnExt();
        feptxn.setFeptxnTxDate("20210421");
        feptxn.setFeptxnEjfno(1000000000);

        feptxn.setFeptxnTxseq("10000000000");
        feptxn.setFeptxnConTxseq("20000000000");
        feptxn.setFeptxnVirTxseq("30000000000");
        feptxn.setFeptxnConVirTxseq("40000000000");
        feptxn.setFeptxnTxDateAtm("20210421");
        feptxn.setFeptxnAtmSeqno("88888888");
        feptxn.setFeptxnConAtmSeqno1("66666666");
        feptxn.setFeptxnConAtmSeqno2("55555555");
        feptxn.setFeptxnEcCbsRrn("Richard");
        feptxn.setFeptxnStan("0000000");
        feptxn.setFeptxnOriStan("0000000");
        feptxn.setFeptxnBkno("000");
        feptxn.setFeptxnTxTime("235959");
        feptxn.setFeptxnTbsdyFisc("99999999");
        feptxn.setFeptxnTbsdy("89898989");
        feptxn.setFeptxnTbsdyAct("12121212");
        feptxn.setFeptxnTxAmtAct(new BigDecimal("123.12"));
        feptxn.setFeptxnTxAmt(new BigDecimal("12341.22"));
        feptxn.setFeptxnAccType((short) 1);
        feptxn.setFeptxnClrType((short) 1);
        feptxn.setFeptxnNeedSendCbs((short) 1);
        feptxn.setFeptxnAtmod((short) 1);
        feptxn.setFeptxnBala(new BigDecimal("1232.21"));
        feptxn.setFeptxnBalb(new BigDecimal("1232.21"));
        feptxn.setFeptxnCbsTimeout((short) 999);
        feptxn.setFeptxnAscTimeout((short) 999);
        feptxn.setFeptxnFiscFlag((short) 0);
        feptxn.setFeptxnPending((short) 0);
        feptxn.setFeptxnAaRc(1234567890);
        feptxn.setFeptxnFiscTimeout((short) 500);
        feptxn.setFeptxnMsgflow("ok");
        feptxn.setFeptxnSubsys((short) 1);
        feptxn.setFeptxnChannel("Ftp");
        feptxn.setFeptxnTraceEjfno(888888888);
        feptxn.setFeptxnTxrust("A");
        feptxn.setFeptxnFeeCustpayAct(new BigDecimal("1234.22"));
        feptxn.setFeptxnFeeCustpay(new BigDecimal("2123.22"));
        feptxn.setFeptxnAaComplete((short) 1);
        feptxn.setFeptxnTmoFlag((short) 0);
        feptxn.setFeptxnCashAmt(new BigDecimal("12321.22"));
        feptxn.setFeptxnCoinAmt(new BigDecimal("33333.22"));

        feptxn.setFeptxnOrderDate("20210421");
        feptxn.setFeptxnExrate(new BigDecimal(5));
        feptxn.setFeptxnScash(new BigDecimal(123L));
        feptxn.setFeptxnAcrate(new BigDecimal(10));
        feptxn.setFeptxnDifrate(new BigDecimal(20));
        feptxn.setFeptxnSelfcd((short) 1);
        feptxn.setFeptxnTxFeeDr(new BigDecimal("10.00"));
        feptxn.setFeptxnTxFeeCr(new BigDecimal("10.10"));
        feptxn.setFeptxnTxFeeMbnkDr(new BigDecimal("20.10"));
        feptxn.setFeptxnTxFeeMbnkCr(new BigDecimal("20.20"));
        feptxn.setFeptxnAtmProfit(new BigDecimal("30.10"));
        feptxn.setFeptxnActProfit(new BigDecimal("30.20"));
        feptxn.setFeptxnActLoss(new BigDecimal("30.20"));
        feptxn.setFeptxnWay((short) 1);
        feptxn.setFeptxnNpsFeeRcvr(new BigDecimal("10.1"));
        feptxn.setFeptxnNpsFeeAgent(new BigDecimal("10.2"));
        feptxn.setFeptxnNpsFeeTrout(new BigDecimal("10.3"));
        feptxn.setFeptxnNpsFeeTrin(new BigDecimal("10.4"));
        feptxn.setFeptxnNpsFeeFisc(new BigDecimal("10.5"));
        feptxn.setFeptxnNpsFeeCustpay(new BigDecimal("10.6"));
        feptxn.setFeptxnNpsClr((short) 0);
        feptxn.setFeptxnNpsMonthlyFg((short) 1);
        feptxn.setFeptxnTxAmtPreauth(new BigDecimal("1000.99"));
        feptxn.setFeptxnTxAmtSet(new BigDecimal("2000.99"));
        feptxn.setFeptxnRwtseq(20000000);
        feptxn.setFeptxnBoxCnt((short) 100);
        feptxn.setFeptxnDspcnt1((short) 10000);
        feptxn.setFeptxnDspcnt2((short) 10000);
        feptxn.setFeptxnDspcnt3((short) 10000);
        feptxn.setFeptxnDspcnt4((short) 10000);
        feptxn.setFeptxnDspcnt5((short) 10000);
        feptxn.setFeptxnDspcnt6((short) 10000);
        feptxn.setFeptxnDspcnt7((short) 10000);
        feptxn.setFeptxnDspcnt8((short) 10000);
        feptxn.setUpdateUserid(01440021);
        feptxn.setUpdateTime(Calendar.getInstance().getTime());
        feptxn.setFeptxnVirCbsTimeout((short) 1);
        feptxn.setFeptxnVirAccType((short) 1);
        feptxn.setFeptxnCashWamt(new BigDecimal("1234.66"));
        feptxn.setFeptxnCoinWamt(new BigDecimal("6543.66"));
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testSelectByPrimaryKey() {
        try {
            feptxnDao.insert(feptxn);
            Feptxn actual = feptxnDao.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
            assertEquals(feptxn.getFeptxnNpsFeeRcvr(), actual.getFeptxnNpsFeeRcvr());
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testDeleteByPrimaryKey() {
        try {
            feptxnDao.deleteByPrimaryKey(feptxn);
            Feptxn actual = feptxnDao.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
//            assertEquals(null, actual);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testInsert() {
        try {
            feptxnDao.insert(feptxn);
            Feptxn actual = feptxnDao.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
            assertEquals(feptxn.getFeptxnNpsFeeRcvr(), actual.getFeptxnNpsFeeRcvr());
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testInsertSelective() {
        try {
            feptxnDao.insertSelective(feptxn);
            Feptxn actual = feptxnDao.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
            assertEquals(feptxn.getFeptxnNpsFeeRcvr(), actual.getFeptxnNpsFeeRcvr());
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    @Rollback(false)
    public void testUpdateByPrimaryKeySelective() {
        try {
            feptxn.setUpdateUser(325);
            feptxnDao.updateByPrimaryKeySelective(feptxn);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testUpdateByPrimaryKey() {
        try {
            feptxnDao.updateByPrimaryKey(feptxn);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testGetFEPTXNForTMO() {
        feptxnDao.setTableNameSuffix("17", StringUtils.join(ProgramName, ".testGetFEPTXNForTMO"));
        try {
            @SuppressWarnings("unused")
            Feptxn feptxn = feptxnDao.getFEPTXNForTMO("20210427", "62032", "00094485");
            // assertTrue(feptxn != null);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testGetFEPTXNByStanAndBkno() {
        feptxnDao.setTableNameSuffix("17", StringUtils.join(ProgramName, ".testGetFEPTXNByStanAndBkno"));
        try {
            @SuppressWarnings("unused")
            Feptxn actual = feptxnDao.getFEPTXNByStanAndBkno("0000016", "889");
            // assertTrue(actual != null);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testGetFEPTXNByReqDateAndStan() {
        feptxnDao.setTableNameSuffix("17", StringUtils.join(ProgramName, ".testGetFEPTXNByReqDateAndStan"));
        try {
            @SuppressWarnings("unused")
            Feptxn actual = feptxnDao.getFEPTXNByReqDateAndStan("20210427", "889", "0000016");
            // assertTrue(actual != null);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testQueryByChannelEJ() {
        feptxnDao.setTableNameSuffix("17", StringUtils.join(ProgramName, ".testQueryByChannelEJ"));
        try {
            feptxnDao.queryByChannelEJ("20210427", "889", 1, "0000016");
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testSelectFEPTXNForCheckATMSeq() {
        feptxnDao.setTableNameSuffix("17", StringUtils.join(ProgramName, ".selectFEPTXNForCheckATMSeq"));
        try {
            feptxnDao.selectFEPTXNForCheckATMSeq("20210427", "889", "0000016");
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testSelectByDatetimeAndPcodesAndBknosAndStansAndEjnos() {
        feptxnDao.setTableNameSuffix("17", StringUtils.join(ProgramName, ".selectByDatetimeAndPcodesAndBknosAndStansAndEjnos"));
        try {
            feptxnDao.selectByDatetimeAndPcodesAndBknosAndStansAndEjnos("20210427", "889", "0000016", "", 1, "01", 1, 1);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testGetFeptxn() {
        feptxnDao.setTableNameSuffix("17",
                StringUtils.join(ProgramName, ".testGetFeptxn"));
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("checkFeptxnBenefit", true);
            args.put("checkMask", true);
            args.put("feptxnExcludeTxCode", Arrays.asList("AAA", "BBB"));
            feptxnDao.getFeptxn(args);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testGetFeptxnSummary() {
        feptxnDao.setTableNameSuffix("17",
                StringUtils.join(ProgramName, ".testGetFeptxnSummary"));
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("checkFeptxnBenefit", true);
            args.put("checkMask", true);
            args.put("feptxnExcludeTxCode", Arrays.asList("AAA", "BBB"));
            feptxnDao.getFeptxnSummary(args);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        }
    }

    @Test
    public void testForTest() {
        FeptxnExtMapper mapper = SpringBeanFactoryUtil.getBean(FeptxnExtMapper.class);
        FeptxnExt feptxn = new FeptxnExt();
        feptxn.setTableNameSuffix("06");
        feptxn.setFeptxnTxDate("20211111");
        feptxn.setFeptxnEjfno(21537);
        Feptxn record = mapper.forTest(feptxn);
        UnitTestLogger.info(record);

        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_SQL_SESSION_FACTORY);
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE, false);
            mapper = sqlSession.getMapper(FeptxnExtMapper.class);
            record = mapper.forTest(feptxn);
            UnitTestLogger.info(record);
        } catch (Exception e) {
            UnitTestLogger.exceptionMsg(e, e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.commit(true);
                sqlSession.close();
            }
        }
    }

    @Test
    public void testThread() {
        new TestThread("t1");
        new TestThread("t2");
        new TestThread("t3");
        new TestThread("t4");
        new TestThread("t5");
    }

    @Autowired
    private PlatformTransactionManager fepdbTransactionManager; // 注意UT下用注入方式獲取，非UT下用SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER)獲取

    private class TestThread extends Thread {

        public TestThread(String name) {
            super(name);
            this.start();
        }

        @Override
        public void run() {
            FeptxnExt feptxn = new FeptxnExt();
            feptxn.setTableNameSuffix("06");
            feptxn.setFeptxnTxDate("20211111");
            feptxn.setFeptxnEjfno(21537);
            while (true) {
                TransactionStatus txStatus = fepdbTransactionManager.getTransaction(new DefaultTransactionDefinition());
                try {
                    Feptxn record = feptxnExtMapper.forTest(feptxn);
                    UnitTestLogger.info(record);
                    fepdbTransactionManager.commit(txStatus);
                } catch (Exception e) {
                    fepdbTransactionManager.rollback(txStatus);
                    UnitTestLogger.exceptionMsg(e, e.getMessage());
                }
                try {
                    sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testGetFeptxnByEj() {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("tableNameSuffix", "01");
        argsMap.put("feptxnEjfno", Arrays.asList(31690200));
        feptxnDao.getFeptxnByEj(argsMap);
    }
}
