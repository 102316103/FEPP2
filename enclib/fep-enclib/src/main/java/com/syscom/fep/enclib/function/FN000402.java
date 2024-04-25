package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000402 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000402(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 產生全繳API交易電文MAC欄位資料
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "41";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFN000402Data();
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
		String inputData = StringUtils.join("0064", this.suipData.getInputData2().substring(4, 4 + 64), this.suipData.getInputData1());
		String command = this.getSuipCommand(fn, keyData.getKeys()[0].KeyType, key, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseFN000402ReturnData(rtn);
		return this.suipData;
	}

	private ENCRC checkFN000402Data() {
		// 1T3ICC C6 807
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		// Input_data_1="0072"+交易序號(N8)+交易代號(N4)+交易金額(NS14)+端末代號(N8)+端末查核碼(AN8)+交易日期時間(N14)+轉出帳號(N16)
		// Input_data_1="0088"+交易序號(N8)+交易代號(N4)+交易金額(NS14)+端末代號(N8)+端末查核碼(AN8)+交易日期時間(N14)+轉入帳號(N16)+轉出帳號(N16)
		// Input_data_1="0040"+交易序號(N8)+交易代號(N4)+端末查核碼(AN8)+交易帳號/轉出帳號(N16)
		// Input_data_1="0096"+交易序號(N8)+交易代號(N4)+繳費金額(NS14)+端末代號(N8)+端末查核碼(AN8)+交易日期時間(N14)+繳款類別(N5)+轉出帳號(N16)+銷帳編號(N16)
		// Input_data_1=“0024”+交易序號(N8)+DivData-1(N16)
		// Input_data_2=“0064”+ICVData-1(H16)+DivData-1(N16)+ DivData-2(N16)+ DivData-3(N16)
		if (!this.checkInputData(this.suipData.getInputData1())) {
			return ENCRC.InputString1Error;
		}
		if (!this.checkInputData(this.suipData.getInputData2())) {
			return ENCRC.InputString2Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000402ReturnData(String suipReturn) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		// Return Data 12byte header + 4byte rc+ 16byte tac
		if (suipReturn.length() > 63) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			if (this.suipData.getRc() == 0) {
				// Compare TAC
				String tacFromSuip = StringUtil.fromHex(suipReturn.substring(32, 32 + 32));
				String tacSendToSuip = this.suipData.getInputData2().substring(68, 68 + 16);
				if (!tacSendToSuip.equals(tacFromSuip)) {
					LogHelperFactory.getGeneralLogger().info("The tac = [", tacFromSuip, "] from SUIP, which is not equals to the tac = [", tacSendToSuip, "] send to Suip in InputData2");
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