package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

/**
 * @author Richard
 */
public class FN000101 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000101(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 與FISC換key時，產生隨機數N+1，N+2之加密資料及KeySyncCheckItem
	 */
	@Override
	public SuipData process() throws Exception {
		String fn = "01";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFN000101Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取Key:101有2把Key
		RefString refKey2 = new RefString(null);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(1, refKey2); // 讀取第2把
		String key2 = refKey2.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 3.Call suip取得回應資料
		String inputData = StringUtils.join(this.suipData.getInputData2(), this.suipData.getInputData1());
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
		rc = this.parseFN000101ReturnData(rtn, keyData.getKeys()[0].KeyLength, refNewKey);
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

	private ENCRC checkFN000101Data() {
		// Key:2T2MAC RMF 950 1 CDK FISC 950 1
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// Input_data_1 ="0032"+Ekcd(key)(H16)+Ekcd(N)(H16)
		// Input_data_1 ="0048"+Ekcd(key)(H32)+Ekcd(N)(H16)
		// Input_data_1 ="0064"+Ekcd(key)(H48)+Ekcd(N)(H16)
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}
		// Input_data_2 ="0016"+"9500000000000000"
		if (!this.checkInputData(this.suipData.getInputData2().trim())) {
			return ENCRC.InputString2Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000101ReturnData(String suipReturn, int returnKeyLength, RefString key) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		key.set(StringUtils.EMPTY);
		// 101 Return Data: 12 byte header + 4byte rc + 16 byte Ekcd(N+1) + 16 byte Ekcd(N+2) + 8 byte KeySync1 + 8 byte KeySync2 + n byte Key = 64 + n
		if (suipReturn.length() >= (64 + returnKeyLength) * 2) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			this.suipData.setOutputData1(StringUtils.join("0032", StringUtil.fromHex(suipReturn.substring(32, 32 + 64))));
			this.suipData.setOutputData2(StringUtils.join("0016", StringUtil.fromHex(suipReturn.substring(96, 96 + 32))));
			key.set(StringUtil.fromHex(suipReturn.substring(128, 128 + returnKeyLength * 2)));
			rc = ENCRC.Normal;
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
