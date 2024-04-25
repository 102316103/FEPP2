package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000805 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000805(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 外圍要求換MAC key，由ENC隨機產生新MAC key並更新Key-File
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "85";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();
		// 1.傳入參數檢核
		rc = this.checkFN000805Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取KEY
		//RefString refKey = new RefString(null);
		RefString refKey = new RefString(null);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(refKey);
        if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		String key = refKey.get();
        key = StringUtil.toHex(StringUtils.rightPad(key, 48, '0'));
		
		String keyType = getKeyLength(keyData.getKeys()[0].KeyType);
        String atmId = keyData.getKeys()[0].KeySubCode;

        // 3.Call suip取得回應資料
        String inputData = StringUtil.toHex(atmId) + keyType + key + StringUtils.leftPad("", 96, '0');
        String command = this.getSuipCommand(fn, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		RefString refNewKey = new RefString(null);
		rc = this.parseFN000805ReturnData(rtn, keyType, refNewKey);
        String newKey = refNewKey.get();
		if (rc != ENCRC.Normal) {
			// this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

        //更新DB
        String upKey1 = StringUtils.join("1" + keyData.getKeys()[0].KeyType + "MAC ATM   ", atmId + " 1"); // 更新ATM MAC KEY
        ENCKey keyData1 = new ENCKey(upKey1, log);
		rc = keyData1.updateKey(0, newKey, ENCKey.UpdateType.Current);
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}		

		return this.suipData;
	}

	private ENCRC checkFN000805Data() {
		// 1T3PPK ATM "+ATM_ID+" "
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000805ReturnData(String suipReturn, String keyType, RefString key) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		key.set(StringUtils.EMPTY);
		// 101 Return Data: 12 byte header + 4byte rc + 16 byte Ekcd(N+1) + 16 byte Ekcd(N+2) + 8 byte KeySync1 + 8 byte KeySync2 + n byte Key = 64 + n
		if (suipReturn.length() >= 200) {
			String suipRc = suipReturn.substring(24, 2 + 24);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			if (this.suipData.getRc() == 0) {
				// Return_data_1="00NN"+DBKEY(depend on keytype)+TOOTHER_KEY(depend on keytype)+KCV(8)
                if(keyType.equals("01")) {
                    key.set(StringUtil.fromHex(suipReturn.substring(32, 32 + 32)));
                    String key1 = StringUtil.fromHex(suipReturn.substring(64, 64 + 32));
                    String kcv1 = StringUtil.fromHex(suipReturn.substring(96, 96 + 32));
                    this.suipData.setOutputData1(StringUtils.join("0032", key1 + kcv1));
                    
                }
                else if(keyType.equals("02")) {
                    key.set(StringUtil.fromHex(suipReturn.substring(32, 32 + 64)));
                    String key1 = StringUtil.fromHex(suipReturn.substring(96, 96 + 64));
                    String kcv1 = StringUtil.fromHex(suipReturn.substring(160, 160 + 32));
                    this.suipData.setOutputData1(StringUtils.join("0048", key1 + kcv1));
                }
                else {
                    key.set(StringUtil.fromHex(suipReturn.substring(32, 32 + 48)));
                    String key1 = StringUtil.fromHex(suipReturn.substring(80, 80 + 96));
                    String kcv1 = StringUtil.fromHex(suipReturn.substring(176, 176 + 32));
                    this.suipData.setOutputData1(StringUtils.join("0064", key1 + kcv1));
                }
				
				rc = ENCRC.Normal;
			}
			else {
				this.suipData.setRc(this.suipData.getRc());
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
