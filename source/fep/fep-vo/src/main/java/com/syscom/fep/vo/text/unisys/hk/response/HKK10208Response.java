package com.syscom.fep.vo.text.unisys.hk.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.unisys.hk.HKGeneral;
import com.syscom.fep.vo.text.unisys.hk.HKUnisysTextBase;

public class HKK10208Response extends HKUnisysTextBase {
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

	// 英文姓名
	@Field(length = 84, optional = true)
	private String _NAME = "";

	// 中文姓名
	@Field(length = 84, optional = true)
	private String _CNAME = "";

	// 可用餘額
	@Field(length = 28, optional = true)
	private String _HWBAL = "";

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
	@Field(length = 696, optional = true)
	private String _FILLER1 = "";

	private static final int _TotalLength = 624;

	/**
	 * 輸入行機台編號
	 */
	public final String getTRMNO() {
		return _TRMNO;
	}

	public final void setTRMNO(String value) {
		_TRMNO = value;
	}

	/**
	 * 交易傳輸編號
	 */
	public final String getTXTNO() {
		return _TXTNO;
	}

	public final void setTXTNO(String value) {
		_TXTNO = value;
	}

	/**
	 * 端末TASK ID
	 */
	public final String getTTSKID() {
		return _TTSKID;
	}

	public final void setTTSKID(String value) {
		_TTSKID = value;
	}

	/**
	 * 櫃台機種類
	 * 
	 * 2
	 */
	public final String getTRMTYP() {
		return _TRMTYP;
	}

	public final void setTRMTYP(String value) {
		_TRMTYP = value;
	}

	/**
	 * TXTSK
	 */
	public final String getTXTSK() {
		return _TXTSK;
	}

	public final void setTXTSK(String value) {
		_TXTSK = value;
	}

	/**
	 * 是否還有TOTA
	 * 
	 * 1
	 */
	public final String getMSGEND() {
		return _MSGEND;
	}

	public final void setMSGEND(String value) {
		_MSGEND = value;
	}

	/**
	 * 訊息編號
	 */
	public final String getMSGID() {
		return _MSGID;
	}

	public final void setMSGID(String value) {
		_MSGID = value;
	}

	/**
	 * 訊息長度
	 */
	public final String getMSGLNG() {
		return _MSGLNG;
	}

	public final void setMSGLNG(String value) {
		_MSGLNG = value;
	}

	/**
	 * 交易日期
	 */
	public final String getTXDAY() {
		return _TXDAY;
	}

	public final void setTXDAY(String value) {
		_TXDAY = value;
	}

	/**
	 * 帳戶餘額
	 */
	public final String getAVBAL() {
		return _AVBAL;
	}

	public final void setAVBAL(String value) {
		_AVBAL = value;
	}

	/**
	 * 英文姓名
	 */
	public final String getNAME() {
		return _NAME;
	}

	public final void setNAME(String value) {
		_NAME = value;
	}

	/**
	 * 中文姓名
	 */
	public final String getCNAME() {
		return _CNAME;
	}

	public final void setCNAME(String value) {
		_CNAME = value;
	}

	/**
	 * 可用餘額
	 */
	public final String getHWBAL() {
		return _HWBAL;
	}

	public final void setHWBAL(String value) {
		_HWBAL = value;
	}

	/**
	 * 折算原幣匯率
	 */
	public final String getCUSFREE() {
		return _CUSFREE;
	}

	public final void setCUSFREE(String value) {
		_CUSFREE = value;
	}

	/**
	 * ENGARD
	 */
	public final String getENGADR() {
		return _ENGADR;
	}

	public final void setENGADR(String value) {
		_ENGADR = value;
	}

	/**
	 * 幣別
	 */
	public final String getDPCUR() {
		return _DPCUR;
	}

	public final void setDPCUR(String value) {
		_DPCUR = value;
	}

	/**
	 * 折算原幣別
	 */
	public final String getDPAMT() {
		return _DPAMT;
	}

	public final void setDPAMT(String value) {
		_DPAMT = value;
	}

	/**
	 * DBFXRATE
	 */
	public final String getDBFXRATE() {
		return _DBFXRATE;
	}

	public final void setDBFXRATE(String value) {
		_DBFXRATE = value;
	}

	/**
	 * 
	 */
	public final String getFILLER1() {
		return _FILLER1;
	}

	public final void setFILLER1(String value) {
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
