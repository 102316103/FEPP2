package com.syscom.fep.service.ems;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.FEPBaseMethod;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.mybatis.ems.dao.FeplogDao;
import com.syscom.fep.mybatis.ems.ext.model.FeplogExt;
import com.syscom.fep.mybatis.ext.mapper.AlertExtMapper;
import com.syscom.fep.mybatis.model.Alert;
import com.syscom.fep.service.ServiceBaseTest;
import com.syscom.fep.enclib.ENCLib;
import com.syscom.fep.enclib.enums.ENCProgramFlow;
import com.syscom.fep.enclib.vo.ENCLogData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"mybatis", "jms-localhost", "integration", "taipei"})
public class EMSLoggingTest extends ServiceBaseTest {
    private LogData logData;

    @Autowired
    private AlertExtMapper alertExtMapper;
    @Autowired
    private FeplogDao fepDao;

    @BeforeEach
    public void setup() {
        logData = new LogData();
        logData.setAtmNo("123");
        logData.setAtmSeq("1");
        logData.setBkno("807");
        logData.setChact("chart");
        logData.setChannel(FEPChannel.FEP);
        logData.setDesBkno("807");
        logData.setEj(999);
        logData.setExternalCode("205");
        logData.setFiscRC("0");
        logData.setIoMethd(DBIOMethod.Insert);
        logData.setMessage("Message");
        logData.setMessageCorrelationId("950");
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setMessageGroup("00");
        logData.setMessageId("2510");
        logData.setNewWebATM(false);
        logData.setNotification(true);
        logData.setOperator("Richard");
        logData.setpCode("2510");
        logData.setPrimaryKeys("FEPTXN");
        logData.setProgramFlowType(ProgramFlow.AAOut);
        logData.setProgramName(this.getClass().getSimpleName());
        logData.setRemark(EMSLoggingTest.class.getName());
        logData.setResponseMessage("ResponseMessage");
        logData.setResponsible("Richard_Yu@email.lingan.com.cn");
        logData.setReturnCode(FEPReturnCode.Normal);
        logData.setServiceUrl("http://");
        logData.setStan("123456");
        logData.setStep(1);
        logData.setSubSys(SubSystem.RM);
        logData.setTableDescription("TableDescription");
        logData.setTableName("FEPTXN01");
        logData.setTrinActno("333");
        logData.setTrinBank("989");
        logData.setTroutActno("221133");
        logData.setTroutBank("879");
        logData.setTxDate("2022-06-15");
        logData.setTxUser("Richard");
        logData.setTxRquid("TxRquid");
    }

    @Test
    public void testFEPMessageAppender() throws Exception {
//        while (true) {
//            for (int i = 0; i < 50000; i++) {
//                FEPBaseMethod.logMessage(Level.INFO, logData);
//            }
//            Thread.sleep(500L);
//        }
        FEPBaseMethod.logMessage(Level.INFO, logData);
        FEPBaseMethod.logMessage(Level.INFO, logData);
        FEPBaseMethod.logMessage(Level.INFO, logData);
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void testFEPEMSMessage() throws Exception {
        FEPBase.logMessage(Level.INFO, logData);
        logData.setProgramException(ExceptionUtil.createNotImplementedException("方法尚未實作!!!"));
        FEPBase.sendEMS(logData);
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void testEMSAlert() throws Exception {
        while (true) {
            try {
                throw ExceptionUtil.createNotImplementedException("方法尚未實作!!!");
            } catch (Exception e) {
                logData.setProgramException(e);
                FEPBase.sendEMS(logData);
            }
            Thread.sleep(1000L);
        }
    }

    @Test
    public void testEMSAlertDbAccessException() {
        try {
            alertExtMapper.insert(new Alert());
        } catch (Exception e) {
            logData.setProgramException(e);
            FEPBase.sendEMS(logData);
        }
    }

    @Test
    public void testEMSAlertDbConnectionTimeoutException() {
        try {
            fepDao.insert(new FeplogExt() {{
                this.setTableNameSuffix("1");
            }});
        } catch (Exception e) {
            logData.setProgramException(e);
            FEPBase.sendEMS(logData);
        }
    }

    @Test
    public void testDeslog() throws InterruptedException {
        ENCLogData encLog = new ENCLogData();
        BeanUtils.copyProperties(this.logData, encLog);
        encLog.setProgramFlowType(ENCProgramFlow.ENCOut);
        encLog.setSystemId(FEPConfig.getInstance().getSystemId());
        encLog.setHostIp(FEPConfig.getInstance().getHostIp());
        encLog.setHostName(FEPConfig.getInstance().getHostName());
        ENCLib.writeLog(Level.INFO, encLog,
                "function",
                "keyid",
                "inputStr1",
                "inputStr2",
                "outStr1",
                "outStr2",
                "suipCommand1",
                "callingprogram1",
                "rc");
        ENCLib.writeLog(Level.INFO, encLog,
                "function",
                "keyid",
                "inputStr1",
                "inputStr2",
                "outStr1",
                "outStr2",
                "suipCommand2",
                "callingprogram1",
                "rc");
        ENCLib.writeLog(Level.INFO, encLog,
                "function",
                "keyid",
                "inputStr1",
                "inputStr2",
                "outStr1",
                "outStr2",
                "suipCommand3",
                "callingprogram3",
                "rc");
        Thread.sleep(Long.MAX_VALUE);
    }
}
