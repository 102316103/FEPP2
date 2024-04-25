package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000412 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000412(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 產生全繳API交易電文MAC欄位資料
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "4B";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFN000412Data();
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

		// 3.Call suip取得回應資料
		String inputData = StringUtils.join("0064", this.suipData.getInputData2().substring(4, 4 + 64), this.suipData.getInputData1());
		String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseFN000412ReturnData(rtn);
		return this.suipData;
	}

	private ENCRC checkFN000412Data() {
		// Key:MAC ATM ATM_ID
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		/*
		 * 1.Input_data_1 = "0064" + 交易訊息MSG(N64)
		 * Input_data_2 = "0064" + ICV(N16) + DivData(N48)
		 * 2.Input_data_1 = "0064" + 交易訊息MSG(N64)
		 * Input_data_2 = "0072" + ICV(N16) + DivData(N48) + MAC_DATA(N8)
		 */
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}
		if (!this.checkInputData(this.suipData.getInputData2().trim())) {
			return ENCRC.InputString2Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000412ReturnData(String suipReturn) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		// Return Data 12byte header + 4byte rc + 8byte mac
		if (suipReturn.length() > 47) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			if (this.suipData.getRc() == 0) {
				// Compare MAC
				String mac = StringUtil.fromHex(suipReturn.substring(32, 32 + 16));
				if (!this.suipData.getInputData2().substring(68, 68 + 8).equals(mac)) {
					this.suipData.setRc(ENCRC.CheckReturn2Error.getValue());
				}
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
