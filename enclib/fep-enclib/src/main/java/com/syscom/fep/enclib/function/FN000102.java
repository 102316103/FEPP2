package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000102 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000102(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 與FISC換key時，產生隨機數N+1，N+2之加密資料及KeySyncCheckItem
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "02";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFN000102Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取Key:102有2把Key
		RefString refKey2 = new RefString(null);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(1, refKey2); // 讀取第2把
		String key2 = refKey2.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 3.Call suip取得回應資料
		String inputData = StringUtils.join(this.suipData.getInputData1(), StringUtils.rightPad("", 30, ' '));
		String command = this.getSuipCommand(fn, keyData.getKeys()[1].KeyType, key2, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		RefString refNewKey = new RefString(null);
		rc = this.parseFN000102ReturnData(rtn, keyData.getKeys()[0].KeyLength, refNewKey);
		String newKey = refNewKey.get();
		if (rc != ENCRC.Normal) {
			return this.suipData;
		}

		// 5.有換Key時要更新Key檔
		rc = keyData.updateKey(0, newKey, ENCKey.UpdateType.Pending); // Update第1把
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
		}
		return this.suipData;
	}

	private ENCRC checkFN000102Data() {
		// Key:2T2MAC RMS 807 1 CDK RMS 807 1
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// Input_data_1 ="0006"+發信單位代號(N3)+收信單位代號(N3)
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000102ReturnData(String suipReturn, int returnKeyLength, RefString key) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		key.set(StringUtils.EMPTY);
		// 102 Return Data:
		// 12 byte header + 4byte rc + newKey + 8byte KeySyncCheckValue + newkey
		// Return_data_1="0024"+新key(H16)+新KeySyncCheckValue(H8)
		// (3_ENC 2_length)
		// Return_data_1="0040"+新key(H32)+新KeySyncCheckValue(H8)
		// (3_ENC 3_length)
		// Return_data_1="0056"+新key(H48)+新KeySyncCheckValue(H8)
		if (suipReturn.length() >= (16 + returnKeyLength + 8 + returnKeyLength) * 2) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			if (this.suipData.getRc() == 0) {
				int out1Length = returnKeyLength + 8;
				this.suipData.setOutputData1(
						StringUtils.join(PolyfillUtil.toString(out1Length, "0000"), StringUtil.fromHex(suipReturn.substring(32 + returnKeyLength * 2, 32 + returnKeyLength * 2 + out1Length * 2))));
				key.set(StringUtil.fromHex(suipReturn.substring(32, 32 + returnKeyLength * 2)));
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
