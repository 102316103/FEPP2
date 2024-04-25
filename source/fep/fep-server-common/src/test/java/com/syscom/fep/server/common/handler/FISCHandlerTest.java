package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.server.common.ServerCommonBaseTest;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISCHeader;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Jennifer
 *
 */
public class FISCHandlerTest extends ServerCommonBaseTest {

	private FISCHandler fiscHandler;
	private FISCSubSystem fiscSubSystem = FISCSubSystem.INBK;
	private FISCGeneral general = new FISCGeneral();

	@BeforeEach
	public void setup() throws Exception {
		// mockMvc = MockMvcBuilders.standaloneSetup(restController).build();
		fiscHandler = new FISCHandler();
		LogData request = new LogData();
		request.setProgramFlowType(ProgramFlow.AAServiceIn);
		fiscHandler.setEj(request.getEj());
		fiscHandler.setLogContext(request);
		@SuppressWarnings("unused")
		FISCData fData;
		// FISCGeneral general = new FISCGeneral();
		general.setSubSystem(fiscSubSystem);
		this.invokeSetField(fiscHandler, "general", general);

		@SuppressWarnings("unused")
		String fiscRes = StringUtils.EMPTY;
	}

	@AfterEach
	public void tearDown() throws Exception {}

	@Test
	public void testGetFISCRequestHeaderForIWD2510Request() throws Exception {
		String data =
				"0F0F0F000674013036330F0F20204957444130303130303431353632303332303030303030303030303030303230313130303432303630313130303432303030303934343834303039303035303530303130303135363030303030353035303031303031353630303030303030303030303030303030303030303030313234323B3938303D36363D343B3A34393030303039303035303530303130303135363030303D313538393031303030303030303030303030303030343030303030303030303030303030303030313D3030303030303030303030303D3030303030303030303030303D303D30333231373030303834323330303030313030303031303030303030202020202020202020202020202020202020202020303030303030303020202020202020202020202020203030303030303030303033333330323437323032313034323031313437333330303530353030313030313536303030353035303031303031353630303030313046323232343434363636313130303030303236343138343F35353833353F323632303C3C3B30303030303030303031303034323030303030202020202030303032303130303432303C37323C3136313E3030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030302020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020";
		RefBase<FISCHeader> msgReq = new RefBase<FISCHeader>(null);

		FEPReturnCode rtnCode =
				(FEPReturnCode) this.invokeMethod(fiscHandler, "getFISCRequestHeader", new Class[] { FISCSubSystem.class, String.class, RefBase.class }, new Object[] { fiscSubSystem, data, msgReq });
		assertEquals(rtnCode, FEPReturnCode.Normal);
		// assertEquals(general.getINBKRequest().getMessageKind(), MessageFlow.Request);

		assertEquals(general.getINBKConfirm().getMessageKind(), MessageFlow.Confirmation);
	}

	@Test
	public void testGetFISCRequestHeaderForRequest() throws Exception {
		String data =
				"00000030323030323531303533383735383238303730303030393530303030303038303831343137303530383030303066B9BC4A24A02001000013012B30303030303030313030303030393130333241202030303030303533343034333032414144323031393038313431373035303830383038313530303033393030343030303033333933303339303034303030303333393330313030353038383036323739393230000A4D82DA669457D14FC24DFA15";
		RefBase<FISCHeader> msgReq = new RefBase<FISCHeader>(null);

		FEPReturnCode rtnCode =
				(FEPReturnCode) this.invokeMethod(fiscHandler, "getFISCRequestHeader", new Class[] { FISCSubSystem.class, String.class, RefBase.class }, new Object[] { fiscSubSystem, data, msgReq });
		assertEquals(rtnCode, FEPReturnCode.Normal);
		assertEquals(general.getINBKRequest().getMessageKind(), MessageFlow.Request);
	}

	@Test
	public void testGetFISCRequestHeaderForConfirm() throws Exception {
		String data = "00000030323032323531303533383735383238303730303030393530303030303038303831343137303530383430303166B9BC4A24000000000000012B3030303030303031303030303039313033324120204BDAB65B";
		RefBase<FISCHeader> msgReq = new RefBase<FISCHeader>(null);

		FEPReturnCode rtnCode =
				(FEPReturnCode) this.invokeMethod(fiscHandler, "getFISCRequestHeader", new Class[] { FISCSubSystem.class, String.class, RefBase.class }, new Object[] { fiscSubSystem, data, msgReq });
		assertEquals(rtnCode, FEPReturnCode.Normal);
		assertEquals(general.getINBKConfirm().getMessageKind(), MessageFlow.Confirmation);
	}

	@Test
	public void testHex() throws Exception {
		String tobeHex = "030323130323531303030303136323630303930303030383037303030303130303632323131313531383430303";
		String header = StringUtil.fromHex(tobeHex);

		LogHelperFactory.getUnitTestLogger().info(header);
		String headerutf8 = new String(Hex.decodeHex(tobeHex), StandardCharsets.UTF_8);
		LogHelperFactory.getUnitTestLogger().info("headerutf8:" + headerutf8);
		String headerutfis0 = new String(Hex.decodeHex(tobeHex), StandardCharsets.ISO_8859_1);
		LogHelperFactory.getUnitTestLogger().info("headerutfis0:" + headerutfis0);

		String headerutf16 = new String(Hex.decodeHex(tobeHex), StandardCharsets.UTF_16);
		LogHelperFactory.getUnitTestLogger().info("headerutf16:" + headerutf16);

	}

}
