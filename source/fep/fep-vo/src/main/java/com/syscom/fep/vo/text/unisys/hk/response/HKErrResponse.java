package com.syscom.fep.vo.text.unisys.hk.response;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.unisys.hk.HKGeneral;
import com.syscom.fep.vo.text.unisys.hk.HKGeneralResponse;
import com.syscom.fep.vo.text.unisys.hk.HKUnisysTextBase;

public class HKErrResponse extends HKUnisysTextBase {

	// 輸入行機台編號
	@Field(length = 10)
	private String _TRMNO = "";

	// 交易傳輸編號
	@Field(length = 10)
	private String _TXTNO = "";

	// 端末TASK ID
	@Field(length = 4)
	private String _TTSKID = "";

	// 櫃台機種類
	@Field(length = 2)
	private String _TRMTYP = "";

	// TXTSK
	@Field(length = 2)
	private String _TXTSK = "";

	// 是否還有TOTA
	@Field(length = 2)
	private String _MSGEND = "";

	// 訊息編號
	@Field(length = 8)
	private String _MSGID = "";

	// 訊息長度
	@Field(length = 6)
	private String _MSGLNG = "";

	// 帳面餘額
	@Field(length = 28, optional = true)
	private String _BAL = "";

	//
	@Field(length = 1176, optional = true)
	private String _FILLER1 = "";

	private static final int _TotalLength = 624;

	public String getTRMNO() {
		return _TRMNO;
	}

	public void setTRMNO(String _TRMNO) {
		this._TRMNO = _TRMNO;
	}

	public String getTXTNO() {
		return _TXTNO;
	}

	public void setTXTNO(String _TXTNO) {
		this._TXTNO = _TXTNO;
	}

	public String getTTSKID() {
		return _TTSKID;
	}

	public void setTTSKID(String _TTSKID) {
		this._TTSKID = _TTSKID;
	}

	public String getTRMTYP() {
		return _TRMTYP;
	}

	public void setTRMTYP(String _TRMTYP) {
		this._TRMTYP = _TRMTYP;
	}

	public String getTXTSK() {
		return _TXTSK;
	}

	public void setTXTSK(String _TXTSK) {
		this._TXTSK = _TXTSK;
	}

	public String getMSGEND() {
		return _MSGEND;
	}

	public void setMSGEND(String _MSGEND) {
		this._MSGEND = _MSGEND;
	}

	public String getMSGID() {
		return _MSGID;
	}

	public void setMSGID(String _MSGID) {
		this._MSGID = _MSGID;
	}

	public String getMSGLNG() {
		return _MSGLNG;
	}

	public void setMSGLNG(String _MSGLNG) {
		this._MSGLNG = _MSGLNG;
	}

	public String getBAL() {
		return _BAL;
	}

	public void setBAL(String _BAL) {
		this._BAL = _BAL;
	}

	public String getFILLER1() {
		return _FILLER1;
	}

	public void setFILLER1(String _FILLER1) {
		this._FILLER1 = _FILLER1;
	}

	@Override
	public HKGeneral parseFlatfile(String flatfile) throws Exception {
		return this.parseFlatfile(this.getClass(), flatfile);
	}

	@Override
	public String makeMessageFromGeneral(HKGeneral general) throws Exception {
		HKGeneralResponse tempVar = general.getmResponse();
		this.setTRMNO(StringUtil.toHex(StringUtils.rightPad(tempVar.getTRMNO(), 5, ' '))); // 輸入行機台編號
		this.setTXTNO(StringUtil.toHex(StringUtils.rightPad(tempVar.getTXTNO(), 5, ' '))); // 交易傳輸編號
		this.setTTSKID(StringUtil.toHex(StringUtils.rightPad(tempVar.getTTSKID(), 2, ' '))); // 端末TASK ID
		this.setTRMTYP(StringUtil.toHex(StringUtils.rightPad(tempVar.getTRMTYP(), 1, ' '))); // 櫃台機種類
		this.setTXTSK(StringUtil.toHex(StringUtils.rightPad(tempVar.getTXTSK(), 1, ' '))); // TXTSK
		this.setMSGEND(StringUtil.toHex(StringUtils.rightPad(tempVar.getMSGEND(), 1, ' '))); // 是否還有TOTA
		this.setMSGID(StringUtil.toHex(StringUtils.rightPad(tempVar.getMSGID(), 4, ' '))); // 訊息編號
		this.setMSGLNG(StringUtil.toHex(StringUtils.rightPad(tempVar.getMSGLNG(), 3, ' '))); // 訊息長度
		this.setBAL(this.toHex(tempVar.getBAL(), 14, 2)); // 帳面餘額
		this.setFILLER1(StringUtil.toHex(StringUtils.leftPad("", 588, ' ')));
		return _TRMNO + _TXTNO + _TTSKID + _TRMTYP + _TXTSK + _MSGEND + _MSGID + _MSGLNG + _BAL + _FILLER1;
	}

	@Override
	public void toGeneral(HKGeneral general) {}

	@Override
	public int getTotalLength() {
		return _TotalLength;
	}
}
