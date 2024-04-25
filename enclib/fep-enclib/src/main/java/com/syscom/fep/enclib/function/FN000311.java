package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.ENCLib;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;
import org.slf4j.event.Level;

public class FN000311 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000311(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 產生信用卡MAC DATA欄位資料
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "3B";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFN000311Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取Key
		RefString refKey = new RefString(null);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(refKey);
		String key = refKey.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		log.setProgramName("FN000311.process");
		log.setRemark("Get KEY :" + key);
		ENCLib.writeLog(Level.DEBUG, log, "FN000311", this.suipData.getKeyIdentity(),
				this.suipData.getInputData1(), this.suipData.getInputData2(), StringUtils.EMPTY, StringUtils.EMPTY,
				StringUtils.EMPTY, null, StringUtils.EMPTY);

		// 3.Call suip取得回應資料
		String inputData = StringUtils.join(this.suipData.getInputData1(), this.suipData.getInputData2());

		String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseFN000311ReturnData(rtn);
		return this.suipData;
	}

	private ENCRC checkFN000311Data() {
		// Key:MAC ATM ATM_ID
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// Input_data_1 = "0016"+ICV0
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}
		// Input_data_2="0022"+交易啟動BANK_ID(N3)+STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)
		if (!this.checkInputData(this.suipData.getInputData2().trim())) {
			return ENCRC.InputString2Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000311ReturnData(String suipReturn) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		// 311 Return Data 12byte header + 4byte rc+ 8byte key=24
		if (suipReturn.length() > 47) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
			if (this.suipData.getRc() == 0) {
				// Return_data_1=”0008”+MAC_DATA(H8)
				this.suipData.setOutputData1(
						StringUtils.join("0008", StringUtil.fromHex(suipReturn.substring(32, 32 + 16))));
				// Modify by David Tai on 2014-11-25 for TSM密碼KEK加密需要16 Bytes
				this.suipData.setOutputData2(
						StringUtils.join("0016", StringUtil.fromHex(suipReturn.substring(32, 32 + 32))));
				// this.suipData.OutputData2 = "";
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
