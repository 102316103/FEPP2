package com.syscom.fep.enclib.function;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000206 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000206(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 檢查金融卡之OFFSET與密碼是否正確(財金送來)
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "26";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();
		// 1.傳入參數檢核
		rc = this.checkFN000206Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取PPK
		RefString refKey1 = new RefString(StringUtils.EMPTY);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(0, refKey1);
		String key1 = refKey1.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		// 讀取第2把key
		RefString refKey2 = new RefString(StringUtils.EMPTY);
		rc = keyData.getKey(1, refKey2);
		String key2 = refKey2.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 3.Call suip取得回應資料
		String inputData = this.suipData.getInputData1();
		String command = this.getSuipCommand(fn, key1, keyData.getKeys()[1].KeyType, key2, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseFN000206ReturnData(rtn);
		return this.suipData;
	}

	private ENCRC checkFN000206Data() {
		/*
		 * 1. key_identify ="1S1PVK ATM   807     "+"PPK ATM   950     "
		 * Input_data_1 ="0036"+PinBlock(H16)+"0000"+帳號(N12)+OFFSET(N4)
		 */
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000206ReturnData(String suipReturn) {
		ENCRC rc = ENCRC.SuipReturnError;
		// Return Data 12 byte header + 4 byte rc=16
		if (suipReturn.length() > 32) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
			if (this.suipData.getRc() == 0) {
				// 1. Return_data_1= ""
				this.suipData.setOutputData1(StringUtils.EMPTY);
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
