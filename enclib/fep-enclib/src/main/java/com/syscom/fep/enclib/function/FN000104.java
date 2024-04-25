package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000104 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000104(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 他行要求與本行換RM之MAC key時，將New Key及KeySyncCheckItem存入暫存欄位
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "04";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFN000104Data();
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
		String inputData = StringUtils.join(this.suipData.getInputData2(), this.suipData.getInputData1());
		String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		RefString refNewKey = new RefString(null);
		rc = this.parseFN000104ReturnData(rtn, keyData.getKeys()[0].KeyLength, refNewKey);
		String newKey = refNewKey.get();
		if (rc != ENCRC.Normal) {
			return this.suipData;
		}

		// 5.有換Key時要更新Key檔
		String key2 = StringUtils.join(this.suipData.getKeyIdentity().substring(0, 3), "MAC", this.suipData.getKeyIdentity().substring(6));
		ENCKey keyData2 = new ENCKey(key2, log);
		rc = keyData2.updateKey(0, newKey, ENCKey.UpdateType.Pending);
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
		}
		return this.suipData;
	}

	private ENCRC checkFN000104Data() {
		// Key:1T2CDK RMR 000 1
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// (single ENC)
		// Input_data_1 ="0024"+Ekcd(kMAC)(H16)+KeySyncCheckValue(H8)
		// (3_ENC 2_length)
		// Input_data_1 ="0040"+Ekcd(kMAC)(H32)+KeySyncCheckValue (H8)
		// (3_ENC 3_length)
		// Input_data_1 ="0056"+Ekcd(kMAC)(H48)+KeySyncCheckValue (H8)
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}
		// Input_data_2 ="0006"+發信單位代號(N3)+收信單位代號(N3)
		if (!this.checkInputData(this.suipData.getInputData2().trim())) {
			return ENCRC.InputString2Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000104ReturnData(String suipReturn, int originalKeyLength, RefString key) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		key.set(StringUtils.EMPTY);
		// 101 Return Data: 12 byte header + 4byte rc + newkey
		if (suipReturn.length() >= (16 + originalKeyLength) * 2) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			if (this.suipData.getRc() == 0) {
				key.set(StringUtil.fromHex(suipReturn.substring(32, 32 + originalKeyLength * 2)));
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
