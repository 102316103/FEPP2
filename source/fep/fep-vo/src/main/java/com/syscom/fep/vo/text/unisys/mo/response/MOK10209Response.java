package com.syscom.fep.vo.text.unisys.mo.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.unisys.mo.MOGeneral;
import com.syscom.fep.vo.text.unisys.mo.MOUnisysTextBase;

public class MOK10209Response extends MOUnisysTextBase {
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

	// MSGID
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

	// 英文地址
	@Field(length = 210, optional = true)
	private String _ENGADR = "";

	// 幣別
	@Field(length = 4, optional = true)
	private String _DPCUR = "";

	// DPAMT
	@Field(length = 26, optional = true)
	private String _DPAMT = "";

	// EDBFXRATE
	@Field(length = 20, optional = true)
	private String _DBFXRATE = "";

	//
	@Field(length = 696, optional = true)
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
	 * MSGID
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
	 * 可用餘額
	 */
	public String getHWBAL() {
		return _HWBAL;
	}

	public void setHWBAL(String value) {
		_HWBAL = value;
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
	 * 英文地址
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
	 * DPAMT
	 */
	public String getDPAMT() {
		return _DPAMT;
	}

	public void setDPAMT(String value) {
		_DPAMT = value;
	}

	/**
	 * DBFXRATE
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
	public MOGeneral parseFlatfile(String flatfile) throws Exception {
		return this.parseFlatfile(this.getClass(), flatfile);
	}

	@Override
	public String makeMessageFromGeneral(MOGeneral general) throws Exception {
		return null;
	}

	@Override
	public void toGeneral(MOGeneral general) throws Exception {}

}
