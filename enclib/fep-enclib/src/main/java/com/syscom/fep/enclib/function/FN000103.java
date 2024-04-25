package com.syscom.fep.enclib.function;

import com.syscom.fep.enclib.ENCKey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class FN000103 extends ENCFunction {
	/**
	 * 這個構建函數一定要增加
	 * 
	 * @param suipData
	 */
	public FN000103(SuipData suipData) {
		super(suipData);
	}

	/**
	 * 與財金或銀行間換key成功時，異動key欄位
	 */
	@Override
	public SuipData process() throws Exception {
		@SuppressWarnings("unused")
		final String fn = "03";
		ENCRC rc = ENCRC.ENCLibError;
		ENCLogData log = this.suipData.getTxLog();

		// 1.傳入參數檢核
		rc = this.checkFN000103Data();
		if (rc != ENCRC.Normal) {
			this.suipData.setRc(rc.getValue());
			return this.suipData;
		}

		// 2. replace key from pending key to current key
		ENCKey keyData = new ENCKey(this.suipData.getKeyIdentity(), log);
		rc = keyData.updateKey("", ENCKey.UpdateType.PendingToCurrent);
		this.suipData.setRc(rc.getValue());
		return this.suipData;
	}

	private ENCRC checkFN000103Data() {
		// Key:1T2MAC OPC 950 1
		if (!this.checkKeyIdentity(this.suipData.getKeyIdentity().trim())) {
			return ENCRC.KeyLengthError;
		}
		return ENCRC.Normal;
	}
}
