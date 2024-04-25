package com.syscom.fep.vo.text.atm.response;

import java.util.Calendar;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.vo.VoBaseTest;
import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderOverride;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderRsStat;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderRsStatRsStatCode;
import com.syscom.fep.vo.text.atm.response.VAA2566Response.VAA2566Rs;
import com.syscom.fep.vo.text.atm.response.VAA2566Response.VAA2566SvcRs;

public class VAA2566ResponseTest extends VoBaseTest {
	private VAA2566Response response;

	@BeforeEach
	public void setup() {
		response = new VAA2566Response();
		response.setRsHeader(new FEPRsHeader());
		response.getRsHeader().setChlEJNo(UUID.randomUUID().toString());
		response.getRsHeader().setEJNo(UUID.randomUUID().toString());
		response.getRsHeader().setRqTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS));
		response.getRsHeader().setRsTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS));
		response.getRsHeader().setOverrides(new FEPRsHeaderOverride[2]);
		response.getRsHeader().getOverrides()[0] = new FEPRsHeaderOverride();
		response.getRsHeader().getOverrides()[0].setCode("123");
		response.getRsHeader().getOverrides()[0].setValue("234");
		response.getRsHeader().getOverrides()[1] = new FEPRsHeaderOverride();
		response.getRsHeader().getOverrides()[1].setCode("223");
		response.getRsHeader().getOverrides()[1].setValue("334");
		response.getRsHeader().setRsStat(new FEPRsHeaderRsStat());
		response.getRsHeader().getRsStat().setDesc("Richard");
		response.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeaderRsStatRsStatCode());
		response.getRsHeader().getRsStat().getRsStatCode().setType("Type-1");
		response.getRsHeader().getRsStat().getRsStatCode().setValue("Value-1");
		response.setSvcRs(new VAA2566SvcRs());
		response.getSvcRs().setRs(new VAA2566Rs());
		response.getSvcRs().getRs().setACCSTAT("ACCSTAT");
		response.getSvcRs().getRs().setACRESULT("ACRESULT");
		response.getSvcRs().getRs().setBKNO("BKNO");
		response.getSvcRs().getRs().setHC("HC");
		response.getSvcRs().getRs().setModeO("ModeO");
		response.getSvcRs().getRs().setRESULT("RESULT");
		response.getSvcRs().getRs().setSTAN("STAN");
		response.getSvcRs().getRs().setTBSDY("TBSDY");
		response.getSvcRs().getRs().setTXACT("TXACT");
	}

	@Test
	public void testToXml() {
		LogHelperFactory.getUnitTestLogger().info(XmlUtil.toXML(response));
		LogHelperFactory.getUnitTestLogger().info(XmlUtil.toXML(response, true));
	}

	@Test
	public void testFromXml() {
		String xml = "<FEP>" +
				"  <RsHeader>" +
				"    <ChlEJNo>68efeca5-489a-4cb6-872c-bc60557faf26</ChlEJNo>" +
				"    <EJNo>217eb0c9-ec4b-4ee7-a654-ac24af6ed5e4</EJNo>" +
				"    <RqTime>2021/07/15 16:10:45.585</RqTime>" +
				"    <RsTime>2021/07/15 16:10:45.603</RsTime>" +
				"    <RsStat>" +
				"      <Desc>Richard</Desc>" +
				"      <RsStatCode>" +
				"        <type>Type-1</type>" +
				"        <Value>Value-1</Value>" +
				"      </RsStatCode>" +
				"    </RsStat>" +
				"    <Overrides>" +
				"      <Override>" +
				"        <code>123</code>" +
				"        <Value>234</Value>" +
				"      </Override>" +
				"      <Override>" +
				"        <code>223</code>" +
				"        <Value>334</Value>" +
				"      </Override>" +
				"    </Overrides>" +
				"  </RsHeader>" +
				"  <SvcRs>" +
				"    <Rs>" +
				"      <HC>HC</HC>" +
				"      <BKNO>BKNO</BKNO>" +
				"      <TXACT>TXACT</TXACT>" +
				"      <RESULT>RESULT</RESULT>" +
				"      <ACRESULT>ACRESULT</ACRESULT>" +
				"      <ACCSTAT>ACCSTAT</ACCSTAT>" +
				"      <STAN>STAN</STAN>" +
				"      <TBSDY>TBSDY</TBSDY>" +
				"      <MODE_O>ModeO</MODE_O>" +
				"    </Rs>" +
				"  </SvcRs>" +
				"</FEP>";
		LogHelperFactory.getUnitTestLogger().info(new Gson().toJson(XmlUtil.fromXML(xml, VAA2566Response.class)));
	}
}
