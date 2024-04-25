package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.ServerCommonBaseTest;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IMSAdapterTest extends ServerCommonBaseTest {
    private FISCHandler fiscHandler;
    private FISCSubSystem fiscSubSystem = FISCSubSystem.INBK;
    private FISCGeneral general = new FISCGeneral();
    private FISCData fData;

    @BeforeEach
    public void setup() throws Exception {
        fiscHandler = new FISCHandler();
        LogData request = new LogData();
        request.setProgramFlowType(ProgramFlow.AAServiceIn);
        fiscHandler.setEj(request.getEj());
        fiscHandler.setLogContext(request);
        fData = new FISCData();
        fData.setMsgCtl(new Msgctl());
        // FISCGeneral general = new FISCGeneral();
        general.setSubSystem(fiscSubSystem);
        this.invokeSetField(fiscHandler, "general", general);

        @SuppressWarnings("unused")
        String fiscRes = StringUtils.EMPTY;
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetSysstatData() throws Exception {

        FEPCache.reloadCache(CacheItem.SYSSTAT);

    }

    @Test
    public void testsendReceive() throws Exception {
        IMSAdapter imsa1 = new IMSAdapter(fData);
        // imsa.setCBSTxid("IBWDO001");
        ReflectUtil.setFieldValue(imsa1, "TxRs", "N");
        ReflectUtil.setFieldValue(imsa1, "_isCBSTest", "Y");
        for (int i = 0; i < 100; i++) {
            imsa1.sendReceive();
        }
//        new Thread("1111111111111"){
//            public void run(){
//                IMSAdapter imsa1 = new IMSAdapter(fData);
//                // imsa.setCBSTxid("IBWDO001");
//                ReflectUtil.setFieldValue(imsa1, "TxRs", "N");
//                ReflectUtil.setFieldValue(imsa1, "_isCBSTest", "Y");
//                for (int i = 0; i < 100; i++) {
//                    imsa1.sendReceive();
//                }
//            }
//        }.start();
//
//        new Thread("222222222222"){
//            public void run(){
//                IMSAdapter imsa1 = new IMSAdapter(fData);
//                // imsa.setCBSTxid("IBWDO001");
//                ReflectUtil.setFieldValue(imsa1, "TxRs", "N");
//                ReflectUtil.setFieldValue(imsa1, "_isCBSTest", "Y");
//                for (int i = 0; i < 100; i++) {
//                    imsa1.sendReceive();
//                }
//            }
//        }.start();
//
//        Thread.sleep(60000);
    }
}
