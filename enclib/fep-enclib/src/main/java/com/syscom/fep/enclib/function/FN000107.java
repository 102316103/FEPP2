package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;


public class FN000107 extends ENCFunction {
	/**
	 * 合庫與主機換TR31 Key Block Change PPK
	 * 
	 * @param suipData
	 */
	public FN000107(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "07";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFNData();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取Key(ZMK)
		RefString refKey = new RefString(null);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(refKey);
		String key = refKey.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		
		// 3.Call suip取得回應資料
		String inputData = this.suipData.getInputData1().substring(4);
		String mode = this.suipData.getInputData2().substring(4, 6);
        //String mode= "01"; //0x00 – FISC,  0x01 –Host
		String command = this.getSuipCommand(fn + mode, keyData.getKeys()[0].KeyType, key, inputData);
		RefString refRtn = new RefString(null);

		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseReturnData(rtn);
		//rc = keyData2.updateOrInsertKey(refNewKey1.get());
		return this.suipData;
	}

	private ENCRC checkFNData() {
		// Key:1T2 ZMK KBPK    807     
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// Input_data_1 = "0016"+ICV0
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}

		return ENCRC.Normal;
	}

	private ENCRC parseReturnData(String suipReturn) throws DecoderException {
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
						StringUtils.join("0032", 
                        StringUtil.fromHex(suipReturn.substring(32, 32 + 64))));
                this.suipData.setOutputData2(
						StringUtils.join("0006", 
                        StringUtil.fromHex(suipReturn.substring(96, 96 + 12))));
				// this.suipData.OutputData2 = "";
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
