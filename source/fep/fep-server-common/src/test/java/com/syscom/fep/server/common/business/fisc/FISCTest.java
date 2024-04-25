package com.syscom.fep.server.common.business.fisc;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.common.ServerCommonBaseTest;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISCHeader;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FISCTest extends ServerCommonBaseTest {
	private static final String PROGRAM_NAME = FISCTest.class.getSimpleName();
	private FISCHandler fiscHandler;
	private FISCSubSystem fiscSubSystem = FISCSubSystem.INBK;
	private FISCGeneral general = new FISCGeneral();
	private FISCData fData;
	@Autowired
	private SpCaller spCaller;

	@BeforeEach
	public void setup() throws Exception {
		fiscHandler = new FISCHandler();
		LogData request = new LogData();
		request.setProgramFlowType(ProgramFlow.AAServiceIn);
		fiscHandler.setEj(request.getEj());
		fiscHandler.setLogContext(request);
		fData = new FISCData();
		// FISCGeneral general = new FISCGeneral();
		general.setSubSystem(fiscSubSystem);
		this.invokeSetField(fiscHandler, "general", general);

		@SuppressWarnings("unused")
		String fiscRes = StringUtils.EMPTY;
	}

	@AfterEach
	public void tearDown() throws Exception {}

	@Test
	public void testSetSysstatData() throws Exception {

		FEPCache.reloadCache(CacheItem.SYSSTAT);

	}

	@Test
	public void testCheckHeaderNormalForRequest() throws Exception {
		FISCHeaderOne mfiscHeader = new FISCHeaderOne();

		final String stringRequest2521 =
				"00000030323030323532313031393931313238303730303030303132303030303038303930353036333131303030303071AB9FEA240C0011000030012B303030303030303830303030303638363430313120383037303030303031323030303036353338303830393035303031353930303430303030343530353030303036383631363831383939303638CDC0A7";

		// message, 電文需要修改FEPCache中 SYSSTAT_FCDSYNC的值 為 71AB9FEA ，而不是 984B4A6F
		// 修改DB或其他處理
		mfiscHeader.setFISCMessage(stringRequest2521);
		mfiscHeader.parseFISCMsg();

		// checkHeader(FISCHeader mfiscHeader, boolean checkFEPTXN) {
		LogData request = new LogData();
		request.setProgramFlowType(ProgramFlow.AAServiceIn);
		request.setMessageFlowType(MessageFlow.Request);

		// logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
		request.setProgramName(StringUtils.join(PROGRAM_NAME, ".sendReceive"));
		request.setMessage(stringRequest2521);
		request.setRemark(StringUtils.join(PROGRAM_NAME, " Recv msg"));

		request.setMessage(StringUtils.EMPTY);
		RefBase<SubSystem> refSubSystem = new RefBase<SubSystem>(null);
		@SuppressWarnings("unused")
		RefBase<FISCSubSystem> refFISCSubSystem = new RefBase<FISCSubSystem>(null);
		request.setSubSys(refSubSystem.get());

		mfiscHeader.setEj(request.getEj());
		mfiscHeader.setLogContext(request);

		fData.setLogContext(request);
		fData.setMessageFlowType(mfiscHeader.getMessageKind());
		fData.setTxObject(this.general);
		fData.setMsgCtl(FEPCache.getMsgctrl("252400"));
		if (fData.getMsgCtl() == null) {
			fData.setMsgCtl(FEPCache.getMsgctrl("GARBLED"));
		}
		fData.setMessageID(mfiscHeader.getMsgId());

		fData.setEj(fiscHandler.getEj());
		RefBase<FISCHeader> msgReq = new RefBase<FISCHeader>(null);

		@SuppressWarnings("unused")
		FEPReturnCode rtnCodeFiscHeader =
				(FEPReturnCode) this.invokeMethod(fiscHandler, "getFISCRequestHeader", new Class[] { FISCSubSystem.class, String.class, RefBase.class },
						new Object[] { fiscSubSystem, stringRequest2521, msgReq });

		fData.setTxChannel(FEPChannel.ATM);
		fData.setTxSubSystem(refSubSystem.get());
		fData.setFiscTeleType(fiscSubSystem);
		fData.setTxRequestMessage(stringRequest2521);

		fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);

		FISC fisc = new FISC(fData);
		fisc.setLogContext(request);
		fisc.setFISCTxData(fData);
		// String msgId = this.getMsgId(this.general.getINBKRequest());

		// 拆財金電文Header, FISCHandler
		// FEPReturnCode rtn = this.getFISCRequestHeader(fiscSubSystem, data, refMsgReq);
		// FISCHeader msgReq = refMsgReq.get();

		FEPReturnCode rtnCode = fisc.checkHeader(mfiscHeader, false);
		LogHelperFactory.getUnitTestLogger().info("rtnCode:" + rtnCode);
	}

	@Test
	public void testCheckHeaderNormalForConfirm2524() {
		FISCHeaderOne mfiscHeader = new FISCHeaderOne();

		final String strConfirm2524TransIn =
				"000000303230323235323439393034353731383037303030303935303030303031303036303131343534303334303031E905813D24000000000000012B303030303030303130303030303033393537303030F1665540";

		mfiscHeader.setFISCMessage(strConfirm2524TransIn);
		mfiscHeader.parseFISCMsg();

		LogData request = new LogData();
		request.setProgramFlowType(ProgramFlow.AAServiceIn);
		request.setMessageFlowType(MessageFlow.Confirmation);

		// logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
		request.setProgramName(StringUtils.join(PROGRAM_NAME, ".sendReceive"));
		request.setMessage(strConfirm2524TransIn);
		request.setRemark(StringUtils.join(PROGRAM_NAME, " Recv msg"));

		request.setMessage(StringUtils.EMPTY);
		RefBase<SubSystem> refSubSystem = new RefBase<SubSystem>(null);
		@SuppressWarnings("unused")
		RefBase<FISCSubSystem> refFISCSubSystem = new RefBase<FISCSubSystem>(null);
		request.setSubSys(refSubSystem.get());

		mfiscHeader.setEj(request.getEj());
		mfiscHeader.setLogContext(request);

		fData.setLogContext(request);
		fData.setMessageFlowType(mfiscHeader.getMessageKind());
		fData.setTxObject(this.general);
		fData.setMsgCtl(FEPCache.getMsgctrl("252402"));
		if (fData.getMsgCtl() == null) {
			fData.setMsgCtl(FEPCache.getMsgctrl("GARBLED"));
		}
		fData.setMessageID(mfiscHeader.getMsgId());

		fData.setEj(fiscHandler.getEj());
		@SuppressWarnings("unused")
		RefBase<FISCHeader> msgReq = new RefBase<FISCHeader>(null);
		fData.setTxChannel(FEPChannel.ATM);
		fData.setTxSubSystem(refSubSystem.get());
		fData.setFiscTeleType(fiscSubSystem);
		fData.setTxRequestMessage(strConfirm2524TransIn);

		fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);

		FISC fisc = new FISC(fData);
		fisc.setLogContext(request);
		fisc.setFISCTxData(fData);
		FEPReturnCode rtnCode = fisc.checkHeader(mfiscHeader, false);
		LogHelperFactory.getUnitTestLogger().info("rtnCode:" + rtnCode);

	}

	@Test
	public void testCheckHeaderNormalForConfirm2510() {
		FISCHeaderOne mfiscHeader = new FISCHeaderOne();

		final String strConfirm2510TransIn =
				"000000303230323235313035343934303730383037303030303831353030303031303036313631373132313534303031984B4A6F24000000000000012B303030303030303130303030303931303332412020FA62547C";

		mfiscHeader.setFISCMessage(strConfirm2510TransIn);
		mfiscHeader.parseFISCMsg();

		LogData request = new LogData();
		request.setProgramFlowType(ProgramFlow.AAServiceIn);
		request.setMessageFlowType(MessageFlow.Confirmation);

		// logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
		request.setProgramName(StringUtils.join(PROGRAM_NAME, ".sendReceive"));
		request.setMessage(strConfirm2510TransIn);
		request.setRemark(StringUtils.join(PROGRAM_NAME, " Recv msg"));

		request.setMessage(StringUtils.EMPTY);
		RefBase<SubSystem> refSubSystem = new RefBase<SubSystem>(null);
		@SuppressWarnings("unused")
		RefBase<FISCSubSystem> refFISCSubSystem = new RefBase<FISCSubSystem>(null);
		request.setSubSys(refSubSystem.get());

		mfiscHeader.setEj(request.getEj());
		mfiscHeader.setLogContext(request);

		fData.setLogContext(request);
		fData.setMessageFlowType(mfiscHeader.getMessageKind());
		fData.setTxObject(this.general);
		fData.setMsgCtl(FEPCache.getMsgctrl("251000"));
		if (fData.getMsgCtl() == null) {
			fData.setMsgCtl(FEPCache.getMsgctrl("GARBLED"));
		}
		fData.setMessageID(mfiscHeader.getMsgId());

		fData.setEj(fiscHandler.getEj());
		@SuppressWarnings("unused")
		RefBase<FISCHeader> msgReq = new RefBase<FISCHeader>(null);
		fData.setTxChannel(FEPChannel.ATM);
		fData.setTxSubSystem(refSubSystem.get());
		fData.setFiscTeleType(fiscSubSystem);
		fData.setTxRequestMessage(strConfirm2510TransIn);

		fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);

		fData.getTxObject().setINBKRequest(general.getINBKRequest()); // if used FISCHeader,it is a private variable
		fData.getTxObject().setINBKResponse(new FISC_INBK());
		fData.getTxObject().setINBKConfirm(new FISC_INBK());

		FISC fisc = new FISC(fData);
		fisc.setLogContext(request);
		fisc.setFISCTxData(fData);
		FEPReturnCode rtnCode = fisc.checkHeader(mfiscHeader, false);
		LogHelperFactory.getUnitTestLogger().info("rtnCode:" + rtnCode);

	}

	@SuppressWarnings("unused")
//	private final String generateStan() {
//		long nextStan = spCaller.getStan().getStan();
//		return StringUtils.leftPad(String.valueOf(nextStan), 7, '0');
//	}

	private class FISCHeaderOne extends FISCHeader {

		private int length;
		private String[] mIndexValue = new String[65];
		@SuppressWarnings("unused")
		private String msgId = StringUtils.EMPTY;

		@Override
		public int getTotalLength() {
			return length;
		}

		@Override
		public String getGetPropertyValue(int index) {
			if ((index > 63) || (index < 0)) {
				index = 0;
			}
			return mIndexValue[index];
		}

		@Override
		public void setGetPropertyValue(int index, String value) {}

		public String getMsgId() {
			// 讀出交易控制檔本筆交易資料
			// INBK MsgID=PCODE+MsgType後2碼
			String msgId = StringUtils.join(this.getProcessingCode(), this.getMessageType().substring(2));
			if ("0202".equals(this.getMessageType()) && "2000".equals(this.getProcessingCode())) {
				msgId = this.getSyncCheckItem();
			}
			return msgId;
		}

	}
}
