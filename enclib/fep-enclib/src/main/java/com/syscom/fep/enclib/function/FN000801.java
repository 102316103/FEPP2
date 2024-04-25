package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000801 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 合庫ATM要求換master key
	 * 
	 * @param suipData
	 */
	public FN000801(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 檢核ATM Session MAC_DATA
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "81";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();
		// 1.傳入參數檢核
		rc = this.checkFN000801Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取key
		RefString refKey1 = new RefString(StringUtils.EMPTY);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(refKey1);
		String key1 = refKey1.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 3.Call suip取得回應資料
		String atmid = this.suipData.getKeyIdentity().substring(13, 8 + 13);
		String keytype = "";

		if (keyData.getKeys()[0].KeyKind.equals("IMK")) {
			keytype = "00"; // Master key
		} else {
			keytype = "01"; // ICK
		}
		// System.out.println("ATMID:" + atmid);
		// System.out.println("key1:" + key1);
		// String inputData =
		// "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		String command = this.getSuipCommandFor801(fn, atmid, keytype, key1);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		RefString refNewKey = new RefString(null);
		// RefString refNewKey1 = new RefString(null);
		rc = this.parseFN000801ReturnData(keytype, rtn, keyData, refNewKey);
		String newKey = refNewKey.get();
		// String newKey1 = refNewKey1.get();
		if (rc != ENCRC.Normal) {
			return this.suipData;
		}

		// 5.有換Key時要更新Key檔
		String upKey1 = "";
		if (keytype.equals("00")) {
			upKey1 = StringUtils.join("1T2TMK ATM   ", atmid + " 1"); // 更新ATM MASTER KEY
		} else {
			upKey1 = StringUtils.join("1S1MAC ATM   ", atmid + " 1"); // 更新ATM MAC KEY
		}

		ENCKey keyData1 = new ENCKey(upKey1, log);
		rc = keyData1.updateKey(0, newKey, ENCKey.UpdateType.Current);
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		// rc = keyData1.updateKey(1, newKey1, ENCKey.UpdateType.Current);
		// if (rc != ENCRC.Normal) {
		// this.suipData.setRc(rc.getValue());
		// return this.suipData;
		// }
		return this.suipData;
	}

	private ENCRC checkFN000801Data() {
		// 1T3PPK ATM "+ATM_ID"
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000801ReturnData(String keytype, String suipReturn, ENCKey originalKey, RefString key)
			throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		key.set(StringUtils.EMPTY);
		// key1.set(StringUtils.EMPTY);
		// 801 Return Data: 12 byte header + 4 byte rc + 16byte lmk(Store Key DB) +
		// 16byte tmk + 16byte kcv
		// byte tmk(to atm) + 8 byte kcv
		if (suipReturn.length() > 47) {
			String suipRc = suipReturn.substring(24, 24 + 8);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
			if (this.suipData.getRc() == 0) {

				if (keytype.equals("00")) { // master key 32 byte
					key.set(StringUtil.fromHex(suipReturn.substring(32, 32 + 64)));
					this.suipData.setOutputData1(
							StringUtils.join("0032", StringUtil.fromHex(suipReturn.substring(96, 96 + 64))));
					this.suipData.setOutputData2(
							StringUtils.join("0016", StringUtil.fromHex(suipReturn.substring(160, 160 + 32))));
				} else { // exchange key 16byte
					key.set(StringUtil.fromHex(suipReturn.substring(32, 32 + 32)));
					this.suipData.setOutputData1(
							StringUtils.join("0016", StringUtil.fromHex(suipReturn.substring(64, 64 + 32))));
					this.suipData.setOutputData2(
							StringUtils.join("0016", StringUtil.fromHex(suipReturn.substring(96, 96 + 32))));
				}

				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
