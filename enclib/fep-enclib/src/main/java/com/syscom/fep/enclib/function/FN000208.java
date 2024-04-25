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

public class FN000208 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000208(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 香港臨櫃檢核磁條密碼及變更磁條密碼產生OFFSET
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "28";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();
		// 1.傳入參數檢核
		rc = this.checkFN000208Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取Key
		RefString refPvk = new RefString(null);
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.getKey(0, refPvk);
		String pvk = refPvk.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		// 讀取第2把key
		RefString refKey = new RefString(null);
		rc = keyData.getKey(1, refKey);
		String key = refKey.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 3.Call suip取得回應資料
		// Input_data_1 ="0045"+meth(H1)+卡號(N16)+"0000"+帳號(N12)+OFFSET(12)
		// Input_data_2 ="0016"+PinBlock(H16)
		int inputData1Len = Integer.parseInt(this.suipData.getInputData1().substring(0, 4)) - this.suipData.getInputData1().substring(4, 4 + 2).length(); // 去掉meth(H1)的長度
		String meth = this.suipData.getInputData1().substring(4, 4 + 2); // 0x00 - 驗證OFFSET, 0x01 - 產生OFFSET
		String inputData = StringUtils.join(PolyfillUtil.toString(inputData1Len, "0000"), this.suipData.getInputData1().substring(6), this.suipData.getInputData2());
		String command = this.getSuipCommand(fn + meth, pvk, keyData.getKeys()[1].KeyType, key, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseFN000208ReturnData(rtn, meth);

		return this.suipData;
	}

	private ENCRC checkFN000208Data() {
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// Input_data_1 ="0045"+meth(H1)+卡號(N16)+"0000"+帳號(N12)+OFFSET(12)
		if (!this.checkInputData(this.suipData.getInputData1().trim())) {
			return ENCRC.InputString1Error;
		}
		// Input_data_2 ="0016"+PinBlock(H16)
		if (!this.checkInputData(this.suipData.getInputData2().trim())) {
			return ENCRC.InputString2Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000208ReturnData(String suipReturn, String meth) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		// Return Data 12 byte header + 4 byte rc + 12 byte offset=28
		if (suipReturn.length() > 55) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至SuipData中
			if (this.suipData.getRc() == 0) {
				// 產生OFFSET要回傳OutputData1
				if ("01".equals(meth)) {
					this.suipData.setOutputData1(StringUtils.join("0012", StringUtil.fromHex(suipReturn.substring(32, 32 + 24))));
					this.suipData.setOutputData2(StringUtils.EMPTY);
				}
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}
}
