package com.syscom.fep.ws.client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.ws.client.entity.fisc.FISCMessageOut;
import com.syscom.fep.ws.client.entity.WsClientType;

public class WsClientFactoryTest extends WsClientBaseTest {
    @Autowired
    private WsClientFactory wsClientFactory;

    @Test
    public void testSendReceiveForFISC() throws Exception {
        String requestData =
                "00000030323030323531303535393437373130303930303030383037303030303130303331383137323834303030303041EF0DB224A02000000013012B30303030303030313030303030303339363730303030303030323335323232303239313632323032313033313831373238333730303530353030313030313536303030353035303031303031353630303030313046323232343434363636313130000A4C4F385F8F77672ADD78C781";
        int timeout = 30;
        FISCMessageOut messageOut = new FISCMessageOut(requestData, timeout);
        String messageIn = wsClientFactory.sendReceive(WsClientType.FISC, null, messageOut);
        LogHelperFactory.getUnitTestLogger().info(messageIn);
    }

    @Test
    public void testSendReceiveForT24() throws Exception {
        String messageOut =
                "TMB.CHL.FUNDS.TRANSFER,A1000/I/PROCESS/NULL/0,SPHATM01/T24@ATM/TW8070146,,TI.CHNN.CODE::=FEP,TI.CHNN.CODE.S::=ATM,TRMNO::=14625,EJFNO::=20210205000057700638,FISC.DATE::=20210208,REG.FLAG::=,DEBIT.ACCT.NO::=14601800110015,DEBIT.CURRENCY::=TWD,DEBIT.AMOUNT::=20000,CREDIT.CURRENCY::=TWD,CREDIT.AMOUNT::=20000,EXCH.RATE::=0,ACCR.CHG.AMT::=0,IC.ACTNO::=14601800110015,CREDIT.ACCT.NO::=TWD1000299990146,T.PSB.MEMO.D::=ATM 現金,T.PSB.REM.S.D::=14625,T.PSB.REM.F.D::=14625,T.REG.TFR.TYPE::=NW,T.PSB.RINF.D:1:=80714625,FEE.PAYER::=1,ACCT.TXN.DR::=0014601800110015,ACCT.TXN.CR::=,AUTH.CODE::=";
        String messageIn = wsClientFactory.sendReceive(WsClientType.T24, null, messageOut);
        LogHelperFactory.getUnitTestLogger().info(messageIn);
    }
}
