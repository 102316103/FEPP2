package com.syscom.fep.vo.text.unisys.hk.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.unisys.hk.HKGeneral;
import com.syscom.fep.vo.text.unisys.hk.HKUnisysTextBase;

public class HKJ10207Response extends HKUnisysTextBase {
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

	// 交易日期
	@Field(length = 16, optional = true)
	private String _TXDAY = "";

	// 帳戶餘額
	@Field(length = 28, optional = true)
	private String _AVBAL = "";

	// PBCOL
	@Field(length = 4, optional = true)
	private String _PBCOL = "";

	// 英文姓名
	@Field(length = 84, optional = true)
	private String _NAME = "";

	// 中文姓名
	@Field(length = 84, optional = true)
	private String _CNAME = "";

	// REFNO
	@Field(length = 32, optional = true)
	private String _REFNO = "";

	// 可用餘額
	@Field(length = 28, optional = true)
	private String _HWBAL = "";

	// PBBAL
	@Field(length = 28, optional = true)
	private String _PBBAL = "";

	// 折算原幣匯率
	@Field(length = 8, optional = true)
	private String _CUSFREE = "";

	// ENGARD
	@Field(length = 210, optional = true)
	private String _ENGADR = "";

	// 幣別
	@Field(length = 4, optional = true)
	private String _DPCUR = "";

	// 折算原幣別
	@Field(length = 26, optional = true)
	private String _DPAMT = "";

	// EDBFXRATE
	@Field(length = 20, optional = true)
	private String _DBFXRATE = "";

	//
	@Field(length = 632, optional = true)
	private String _FILLER1 = "";

	private static int _TotalLength = 624;

	/**
	 * 輸入行機台編號
	 */
	public String getTRMNO() {
		return _TRMNO;
	}

	public void setTRMNO(String value) {
		_TRMNO = value;
	}

	/**
	 * 交易傳輸編號
	 */
	public String getTXTNO() {
		return _TXTNO;
	}

	public void setTXTNO(String value) {
		_TXTNO = value;
	}

	/**
	 * 端末TASK ID
	 */
	public String getTTSKID() {
		return _TTSKID;
	}

	public void setTTSKID(String value) {
		_TTSKID = value;
	}

	/**
	 * 櫃台機種類
	 * 
	 * 2
	 */
	public String getTRMTYP() {
		return _TRMTYP;
	}

	public void setTRMTYP(String value) {
		_TRMTYP = value;
	}

	/**
	 * TXTSK
	 */
	public String getTXTSK() {
		return _TXTSK;
	}

	public void setTXTSK(String value) {
		_TXTSK = value;
	}

	/**
	 * 是否還有TOTA
	 * 
	 * 1
	 */
	public String getMSGEND() {
		return _MSGEND;
	}

	public void setMSGEND(String value) {
		_MSGEND = value;
	}

	/**
	 * 訊息編號
	 */
	public String getMSGID() {
		return _MSGID;
	}

	public void setMSGID(String value) {
		_MSGID = value;
	}

	/**
	 * 訊息長度
	 */
	public String getMSGLNG() {
		return _MSGLNG;
	}

	public void setMSGLNG(String value) {
		_MSGLNG = value;
	}

	/**
	 * 交易日期
	 */
	public String getTXDAY() {
		return _TXDAY;
	}

	public void setTXDAY(String value) {
		_TXDAY = value;
	}

	/**
	 * 帳戶餘額
	 */
	public String getAVBAL() {
		return _AVBAL;
	}

	public void setAVBAL(String value) {
		_AVBAL = value;
	}

	/**
	 * PBCOL
	 */
	public String getPBCOL() {
		return _PBCOL;
	}

	public void setPBCOL(String value) {
		_PBCOL = value;
	}

	/**
	 * 英文姓名
	 */
	public String getNAME() {
		return _NAME;
	}

	public void setNAME(String value) {
		_NAME = value;
	}

	/**
	 * 中文姓名
	 */
	public String getCNAME() {
		return _CNAME;
	}

	public void setCNAME(String value) {
		_CNAME = value;
	}

	/**
	 * REFNO
	 */
	public String getREFNO() {
		return _REFNO;
	}

	public void setREFNO(String value) {
		_REFNO = value;
	}

	/**
	 * 可用餘額
	 */
	public String getHWBAL() {
		return _HWBAL;
	}

	public void setHWBAL(String value) {
		_HWBAL = value;
	}

	/**
	 * PBBAL
	 */
	public String getPBBAL() {
		return _PBBAL;
	}

	public void setPBBAL(String value) {
		_PBBAL = value;
	}

	/**
	 * 折算原幣匯率
	 */
	public String getCUSFREE() {
		return _CUSFREE;
	}

	public void setCUSFREE(String value) {
		_CUSFREE = value;
	}

	/**
	 * ENGARD
	 */
	public String getENGADR() {
		return _ENGADR;
	}

	public void setENGADR(String value) {
		_ENGADR = value;
	}

	/**
	 * 幣別
	 */
	public String getDPCUR() {
		return _DPCUR;
	}

	public void setDPCUR(String value) {
		_DPCUR = value;
	}

	/**
	 * 折算原幣別
	 */
	public String getDPAMT() {
		return _DPAMT;
	}

	public void setDPAMT(String value) {
		_DPAMT = value;
	}

	/**
	 * EDBFXRATE
	 */
	public String getDBFXRATE() {
		return _DBFXRATE;
	}

	public void setDBFXRATE(String value) {
		_DBFXRATE = value;
	}

	/**
	 * 
	 */
	public String getFILLER1() {
		return _FILLER1;
	}

	public void setFILLER1(String value) {
		_FILLER1 = value;
	}

	/**
	 * 電文總長度
	 * 
	 * 該組電文總長度
	 */
	@Override
	public int getTotalLength() {
		return _TotalLength;
	}

	@Override
	public HKGeneral parseFlatfile(String flatfile) throws Exception {
		return this.parseFlatfile(this.getClass(), flatfile);
	}

	@Override
	public String makeMessageFromGeneral(HKGeneral general) throws Exception {
		return null;
	}

	@Override
	public void toGeneral(HKGeneral general) throws Exception {}
}
