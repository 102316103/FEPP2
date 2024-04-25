package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

/**
 * 2021-11-12 Richard add for ATM Gateway
 * 
 * @author Richard
 *
 */
public class FN000803 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000803(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 檢核ATM Session MAC_DATA
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "83";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();
		// 1.傳入參數檢核
		rc = this.checkFN000803Data();
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
		String inputData = this.suipData.getInputData1();
		String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseFN000803ReturnData(rtn);

		return this.suipData;
	}

	private ENCRC checkFN000803Data() {
		// Key:1T3MAC ATM 80703950 1
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// Input_data_1 ="0016"+Random Number(H16)
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}
		// Input_data_2 ="0016"+MAC(H16)
		if (!this.checkInputData(this.suipData.getInputData2().trim())) {
			return ENCRC.InputString2Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000803ReturnData(String suipReturn) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		// 803 Return Data 12 byte header + 4 byte rc + 16 byte MAC_Data = 32 byte
		if (suipReturn.length() >= 63) {
			String suipRc = suipReturn.substring(24, 2 + 24);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			if (this.suipData.getRc() == 0) {
				String mac = StringUtil.fromHex(suipReturn.substring(32, 32 + 32));
				if (!this.suipData.getInputData2().substring(4, 16 + 4).equals(mac)) {
					this.suipData.setRc(ENCRC.CheckReturn1Error.getValue());
				}
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
