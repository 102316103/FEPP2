package com.syscom.fep.enclib.function;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000307 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000307(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 檢核財金MAC & PINBLOCK & 產生MAC(Response)
	 * 三合一組合功能, 傳入三組參數:
	 * (1).檢核Request MAC,
	 * (2).檢核PIN及
	 * (3).產生Response MAC
	 * 錯誤RC:區分ENC/MAC/PIN
	 * 成功RC:傳回 MAC值
	 */
	@Override
	public SuipData process() throws Exception {
		final String fn = "37";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		RefBase<SuipData[]> refSuip = new RefBase<SuipData[]>(new SuipData[3]); // 應該有3組
		rc = this.checkFN000307Data(refSuip);
		SuipData[] suip = refSuip.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2.讀取Key:101有2把Key
		RefString refPvk = new RefString(null);
		ENCKey keyData1 = new ENCKey(suip[1].getKeyIdentity(), log);
		rc = keyData1.getKey(0, refPvk);
		String pvk = refPvk.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		RefString refKey2 = new RefString(StringUtils.EMPTY);
		rc = keyData1.getKey(1, refKey2);
		String key2 = refKey2.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		RefString refKey3 = new RefString(null);
		ENCKey keyData3 = new ENCKey(suip[0].getKeyIdentity(), log);
		rc = keyData3.getKey(refKey3);
		String key3 = refKey3.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}
		RefString refKey4 = new RefString(null);
		ENCKey keyData4 = new ENCKey(suip[2].getKeyIdentity(), log);
		rc = keyData4.getKey(refKey4);
		String key4 = refKey4.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 3.Call suip取得回應資料
		String pinBlock = suip[1].getInputData2();
		String mac = suip[0].getInputData2();
		String inputData = StringUtils.join(suip[0].getInputData1(), suip[1].getInputData1(), suip[2].getInputData1());
		String command = this.getSuipCommand(fn, pvk, keyData1.getKeys()[1].KeyType, key2, pinBlock, keyData3.getKeys()[0].KeyType, key3, keyData4.getKeys()[0].KeyType, key4, mac, inputData);
		RefString refRtn = new RefString(null);
		rc = this.sendReceive(command, log, refRtn);
		String rtn = refRtn.get();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 4.拆解Suip回傳結果
		rc = this.parseFN000307ReturnData(rtn);
		return this.suipData;
	}

	private ENCRC checkFN000307Data(RefBase<SuipData[]> suip) {
		// 複合function
		// Key:00031T2MAC ATM 950 1 00380103169470016487000000FA53965A18F3F786 000818D28FDB 2S1PVK ATM 807 1T2PPK ATM 950 1 0036060008007700130100000600800770018940 0016FA53965A18F3F786 1T2MAC ATM 950
		// 1 003801031694700164871040010000000000000000 0000
		// 0003 +
		// 第一組資料KeyId1(67) + Input1Length(4) + Input1(128) + Input2Length(4) + Input2(128)
		// 第二組資料KeyId1(67) + Input1Length(4) + Input1(128) + Input2Length(4) + Input2(128)
		// 第三組資料KeyId1(67) + Input1Length(4) + Input1(128) + Input2Length(4) + Input2(128)
		// 總長度997byte
		if (!this.checkCompoundInputData(this.suipData.getInputData1(), suip)) {
			return ENCRC.InputString1Error;
		}
		return ENCRC.Normal;
	}

	private ENCRC parseFN000307ReturnData(String suipReturn) throws DecoderException {
		ENCRC rc = ENCRC.SuipReturnError;
		// 307 Return Data: 12 byte header + 4 byte rc + 8 byte mac = 24 byte
		if (suipReturn.length() > 47) {
			String suipRc = suipReturn.substring(24, 24 + 2);
			this.suipData.setRc(Integer.parseInt(suipRc, 16)); // 先將Suip回應的RC塞至_suipData中
			if (this.suipData.getRc() == 0) {
				this.suipData.setOutputData1(StringUtils.join("0008", StringUtil.fromHex(suipReturn.substring(32, 32 + 16))));
				rc = ENCRC.Normal;
			}
		} else {
			this.suipData.setRc(rc.getValue());
		}
		return rc;
	}

}
