package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000802 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000802(SuipData suipData) {
		super(suipData);
	}

	/**
	 * ATM要求換PPK key，由ENC隨機產生新PPK key並更新Key-File
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "82";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();
		// 1.傳入參數檢核
		rc = this.checkFN000802Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取PPK
		RefString refKey1 = new RefString(StringUtils.join("1T3CDK TCP   ", this.suipData.getKeyIdentity().substring(13)));
		ENCKey keyData = new ENCKey(refKey1.get(), log);
		rc = keyData.getKey(refKey1);
		String key1 = refKey1.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 3.Call suip取得回應資料
		String atmid = this.suipData.getKeyIdentity().substring(13, 8 + 13);
		String inputData = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		String command = this.getSuipCommand(fn, atmid, keyData.getKeys()[0].KeyType, key1, inputData);
		RefString refRtn = new RefString(null);
		String rtn = refRtn.get();
		rc = this.sendReceive(command, log, refRtn);
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		RefString refNewKey = new RefString(null);
		rc = this.parseFN000802ReturnData(rtn, keyData, refNewKey);
		String newKey = refNewKey.get();
		if (rc != ENCRC.Normal) {
			// this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 5.有換Key時要更新Key檔
		String upKey = "1T3MAC TCP   " + this.suipData.getKeyIdentity().substring(13, 14 + 13);
		ENCKey keyData1 = new ENCKey(upKey, log);
		rc = keyData1.updateKey(newKey, ENCKey.UpdateType.Current);
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
		}
		return this.suipData;
	}

	private ENCRC checkFN000802Data() {
		// 1T3PPK ATM "+ATM_ID+" "
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000802ReturnData(String suipReturn, ENCKey originalKey, RefString key) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		key.set(StringUtils.EMPTY);
		// 101 Return Data: 12 byte header + 4byte rc + 16 byte Ekcd(N+1) + 16 byte Ekcd(N+2) + 8 byte KeySync1 + 8 byte KeySync2 + n byte Key = 64 + n
		if (suipReturn.length() >= 256) {
			String suipRc = suipReturn.substring(24, 2 + 24);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			if (this.suipData.getRc() == 0) {
				// Return_data_1="0064"+新MACkey(H48)+新KeySyncCheckValue(H16)
				this.suipData.setOutputData1(StringUtils.join("0064", StringUtil.fromHex(suipReturn.substring(128, 128 + 128))));
				key.set(StringUtil.fromHex(suipReturn.substring(32, 96 + 32)));
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
