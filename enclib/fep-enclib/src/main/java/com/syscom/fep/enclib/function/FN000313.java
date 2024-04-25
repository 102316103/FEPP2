package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000313 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000313(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 產生信用卡MAC DATA欄位資料
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "3C";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFN000313Data();
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

		// log.setProgramName("FN000313.process");
		// log.setRemark("Get KEY :" + key);
		// ENCLib.writeLog(Level.DEBUG, log, "FN000313", this.suipData.getKeyIdentity(),
		// this.suipData.getInputData1(), this.suipData.getInputData2(),
		// StringUtils.EMPTY, StringUtils.EMPTY,
		// StringUtils.EMPTY, null, StringUtils.EMPTY);

		// 3.Call suip取得回應資料
		String inputData = this.suipData.getInputData1();
		String command = this.getSuipCommand(fn + "00", keyData.getKeys()[0].KeyType, key, inputData);
		RefString refRtn = new RefString(null);

		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseFN000313ReturnData(rtn);
		return this.suipData;
	}

	private ENCRC checkFN000313Data() {
		// Key:MAC ATM ATM_ID
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// Input_data_1 = "0016"+ICV0
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}

		return ENCRC.Normal;
	}

	private ENCRC parseFN000313ReturnData(String suipReturn) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		// 311 Return Data 12byte header + 4byte rc+ 8byte key=24
		if (suipReturn.length() > 47) {
			String suipRc = suipReturn.substring(24, 24 + 8);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
			if (this.suipData.getRc() == 0) {
				// Return_data_1=”0008”+MAC_DATA(H8)
				// this.suipData.setOutputData1(
				// StringUtils.join("0008", StringUtil.fromHex(suipReturn.substring(32, 32 +
				// 16))));

				this.suipData.setOutputData1(
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
