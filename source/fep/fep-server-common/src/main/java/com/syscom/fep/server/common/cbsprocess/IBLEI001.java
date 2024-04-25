package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.NpsunitMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.IB_LE_I001;
import com.syscom.fep.vo.text.ims.IB_LE_O001;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 組送CBS 主機原存約定及核驗Request交易電文
 * 
 * @author Joseph
 *
 */

public class IBLEI001 extends ACBSAction {

	public IBLEI001(MessageBase txData) {
		super(txData, new IB_LE_I001());
	}

//	private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
	private NpsunitMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);
	Vatxn vatxn=this.getFiscData().getVatxn();

	/**
	 * 組CBS 原存交易Request電文
	 * 
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		//組CBS 原存交易Request電文, 電文內容格式請參照: D_I7_合庫FEP_主機電文規格-原存行約定及核驗交易V1.0(111xxxx).doc
		IB_LE_I001 cbsTita = new IB_LE_I001();
		// HEADER
		cbsTita.setIMS_TRANS("MFEPFG00"); // 主機業務別 長度8
		cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
		cbsTita.setSYS_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime()); // FEP交易日期時間 長度14
		cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()),8,"0")); // FEP電子日誌序號 長度8
		cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
		cbsTita.setMSG_CAT("00");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 固定給00 */
		cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
		cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
		cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白
		if("10".equals(vatxn.getVatxnCate()) || "11".equals(vatxn.getVatxnCate())){
			cbsTita.setPROCESS_TYPE("CHK"); /*檢核*/
		}else{
			cbsTita.setPROCESS_TYPE("SET"); /*資料設定*/
		}
		// 財金營業日(西元年須轉民國年)
		String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
		if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			feptxnTbsdyFisc = "000000";
		} else {
			feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
		}
		cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
		cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno()); // 設備代理行 長度3
		cbsTita.setTXNSTAN(feptxn.getFeptxnStan()); // 跨行交易序號 長度7
		cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());// 端末機代號 長度8
		cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType()); // 端末設備型態 長度4
		cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno()); // 發卡行/扣款行 長度3
		if(StringUtils.isNotBlank(feptxn.getFeptxnIcmark())){
			cbsTita.setCARDTYPE("K"); /* 晶片卡 */
		}else{
			cbsTita.setCARDTYPE("X"); /* 未使用卡片 */
		}
		cbsTita.setRESPONSE_CODE(feptxn.getFeptxnReqRc()); // 回應代號(RC) 長度4
		cbsTita.setHRVS(StringUtils.repeat(" ", 33)); // 保留欄位 長度33
		/* CBS Request DETAIL */
		cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno()); // IC卡交易序號
		cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk()); // 端末設備查核碼
		cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDatetimeFisc()); // 交易日期時間
		if(StringUtils.isNotBlank(this.getInbkRequest().getICMARK())){
			cbsTita.setICMEMO(this.getInbkRequest().getICMARK()); // IC卡備註欄
		}else{
			cbsTita.setICMEMO(StringUtils.repeat("40",30)); // IC卡備註欄
		}
		/*
		 * FEPTXN 為Varch(16), 財金規格B(V)(LL+DATA)合庫固定DATA為8位LL + X(08) , 必須把FEPTXN_IC_TAC
		 * 16 byte pack成8byte
		 */
		if (StringUtils.isNotBlank(this.getInbkRequest().getTAC())) {
			cbsTita.setTXNICCTAC("000A" + this.getInbkRequest().getTAC()); // 交易驗證碼
		}else{
			cbsTita.setTXNICCTAC(StringUtils.repeat("40",10)); // 交易驗證碼
		}
		cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt()); // 交易金額
		cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片提款帳號(無卡提款序號)
		cbsTita.setLE_CHARGCUS(feptxn.getFeptxnFeeCustpayAct().toString()); // 跨行手續費
		cbsTita.setLE_2566_TYPE(vatxn.getVatxnCate()); // 業務類別
		cbsTita.setLE_AEIPYTP(vatxn.getVatxnType()); // 交易類別
		cbsTita.setLE_AEIPYUES(vatxn.getVatxnUse()); // 核驗用途(01: 金融FIDO)
		cbsTita.setLF_AELFTP(vatxn.getVatxnItem()); // 核驗項目
		cbsTita.setLF_AEILFRC2(StringUtils.repeat(" ",2)); // 核驗結果
		cbsTita.setLF_AEILFRC1(StringUtils.repeat(" ",2)); // 帳號核驗結果
		cbsTita.setLF_AEI_OPENACT(StringUtils.repeat(" ",2)); // 開戶狀態
		cbsTita.setLE_IDNO(vatxn.getVatxnIdno()); // 約定連結申請人ID核驗之ID或統編
		cbsTita.setLE_CLCPYCI(vatxn.getVatxnBusino()); // 委託單位統編/電支機構之統編
		cbsTita.setLE_PAYUNTNO(vatxn.getVatxnBusinessUnit()); // 委託單位代號
		cbsTita.setLE_TAXTYPE(vatxn.getVatxnPaytype()); // 繳費類別
		cbsTita.setLE_PAYFEENO(vatxn.getVatxnFeeno()); // 類別代號
		cbsTita.setLE_MOBILENO(vatxn.getVatxnMobile()); // 核驗之行動電話
		cbsTita.setLE_EIPYAC2(vatxn.getVatxnPactno()); // 委託單位客戶帳號/電支開立之帳號
		cbsTita.setLE_AEIPYAC(vatxn.getVatxnActno()); // 約定扣款帳號
		cbsTita.setLE_Insure_IDNO(vatxn.getVatxnInsno()); // 保險識別編號
		cbsTita.setBIRTHDAY(vatxn.getVatxnBirthday()); // 核驗之生日
		cbsTita.setTEL_HOME(vatxn.getVatxnHphone()); // 核驗之住家電話
        //20221027 Modify 全繳線上約定作業才需讀取讀取委託單位之帳務代理行
		Npsunit npsunit=new Npsunit();
		if("02".equals(vatxn.getVatxnCate())){
			npsunit.setNpsunitNo(vatxn.getVatxnBusinessUnit()); /*委託單位代號*/
			npsunit.setNpsunitPaytype(vatxn.getVatxnPaytype()); /*繳款類別*/
			npsunit.setNpsunitFeeno(vatxn.getVatxnFeeno()); /*費用代號*/
			Npsunit rc=npsunitExtMapper.selectByPrimaryKey(npsunit.getNpsunitNo(),npsunit.getNpsunitPaytype(),npsunit.getNpsunitFeeno());
			if(rc == null){
				return FEPReturnCode.NPSNotFound; /* 委託單位代號錯誤(NPSNotFound ) */
			}
			cbsTita.setLE_AEIPYBK(rc.getNpsunitBkno().substring(0,3)); // 帳務代理行銀行代號
			cbsTita.setLE_AEIPYBH(StringUtils.repeat(" ",3)); // 帳務代理分行/核驗代理行分行代號
		}else{
			if("10".equals(vatxn.getVatxnCate())){
				cbsTita.setLE_AEIPYBK(StringUtils.repeat(" ",3)); // 帳務代理行銀行代號
				cbsTita.setLE_AEIPYBH(StringUtils.repeat(" ",3)); // 帳務代理分行/核驗代理行分行代號
			}else{
				cbsTita.setLE_AEIPYBK(StringUtils.repeat(" ",3)); // 帳務代理行銀行代號
				cbsTita.setLE_AEIPYBH(StringUtils.repeat(" ",3)); // 帳務代理分行/核驗代理行分行代號
			}
		}
		cbsTita.setLE_SSLTYPE(StringUtils.repeat(" ",1)); // 安控機制
		cbsTita.setLF_MNO_CHANGE(StringUtils.repeat(" ",1)); //行動電話異動核驗
		cbsTita.setDRVS(StringUtils.repeat(" ", 248)); // 保留欄位
		this.setoTita(cbsTita);
		this.setTitaToString(cbsTita.makeMessage());
		this.setASCIItitaToString(cbsTita.makeMessageAscii());
		return FEPReturnCode.Normal;
	}

	/**
	 * 拆解CBS TOTA電文
	 * 
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
		/* 電文內容格式請參照TOTA電文格式(IB_LE_O001) */
		/* 拆解主機回應電文 */
		IB_LE_O001 tota = new IB_LE_O001();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);

		/* 更新FEPTXN */
		FEPReturnCode rtnCode = this.updateFEPTxn(tota, type);

		/* 回覆FEP */
		// 處理 CBS 回應
		return rtnCode;
	}

	/**
	 * 更新FEPTXN
	 * 
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTxn(IB_LE_O001 cbsTota, String type) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE()); /* 帳戶手續費 */
		feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE()); /* 提領幣別手續費 */
		feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());  /* 主機交易時間 */
		/* 境內電子支付約定連結申請(2566-01)/線上約定繳費(2566-02)*/
		if("01".equals(vatxn.getVatxnCate()) || "02".equals(vatxn.getVatxnCate())){
			feptxn.setFeptxnRemark(cbsTota.getO_FROM_ACT()); /* 約定扣款帳號 */
		}
		/* 金融帳戶核驗(2566-10) */
		if("10".equals(vatxn.getVatxnCate())){
			vatxn.setVatxnResult(cbsTota.getO_LF_AEILFRC2()); /* 帳號核驗結果 */
			vatxn.setVatxnAcresult(cbsTota.getO_LF_AEILFRC1()); /* 項目核驗結果 */
			vatxn.setVatxnAcstat(cbsTota.getO_LF_OPENACT()); /* 開戶狀態 */
			/* 2023/4/27 新增行動電話異動核驗 */
			vatxn.setVatxnTelresult(cbsTota.getO_LF_MNO_CHANGE());
			feptxn.setFeptxnRemark(cbsTota.getO_LF_AEILFRC2()+cbsTota.getO_LF_AEILFRC1()+cbsTota.getO_LF_OPENACT()+cbsTota.getO_LF_MNO_CHANGE());
		}
		feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
		feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
		feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160()); // 待增加
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */
		// IMSRC_TCB = "000" or empty表交易成功
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		if (!cbsTota.getIMSRC_TCB().equals("000")) {
			feptxn.setFeptxnAccType((short) 0); //未記帳
			rtnCode = FEPReturnCode.CBSCheckError;
		} else {
			/* CBS回覆成功 */
			/* 主機回傳民國年須轉成西元年 */
			String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
			if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
					|| (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
				imsbusinessDate = "00000000";
			} else {
				imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
			}
			feptxn.setFeptxnTbsdy(imsbusinessDate);
			/* 2022/11/21 寫入帳務分行 */
			feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
			feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
			// 記帳類別
			if ("Y".equals(cbsTota.getIMSACCT_FLAG())) { // 主機記帳狀況
				feptxn.setFeptxnAccType((short) 1); /* 已記帳 */
			} else {
				feptxn.setFeptxnAccType((short) 0); /* 未記帳 */
			}
			rtnCode = FEPReturnCode.Normal;
		}
		this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
		return rtnCode;
	}
}
