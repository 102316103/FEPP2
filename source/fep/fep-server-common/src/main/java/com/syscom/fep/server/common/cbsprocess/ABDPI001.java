package com.syscom.fep.server.common.cbsprocess;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_DP_I001;
import com.syscom.fep.vo.text.ims.AB_DP_O001;

/**
 * 組送CBS主機跨行存款Request交易電文
 *
 * @author Joseph
 *
 */

public class ABDPI001 extends ACBSAction {

	public ABDPI001(MessageBase txData) {
		super(txData, new AB_DP_O001());
	}

//	private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
	private NpsunitExtMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);

	/**
	 * 組CBS TITA電文
	 *
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(AB_DP_I001) */
		// Header
		AB_DP_I001 cbstita = new AB_DP_I001();
		ATMGeneralRequest atmReq = this.getAtmRequest();
		cbstita.setIMS_TRANS("MFEPAT00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
		cbstita.setTXN_FLOW("A"); // 跨行(合庫代理)
		cbstita.setMSG_CAT(atmReq.getMSGTYP());
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		if(txType.equals("0")){ //檢核
			cbstita.setPROCESS_TYPE("CHK");
		}else if(txType.equals("1")) { //入扣帳
			cbstita.setPROCESS_TYPE("ACCT");
		}
		// 財金營業日(西元年須轉民國年)
		String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
		if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			feptxnTbsdyFisc = "000000";
		} else {
			feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
		}
		cbstita.setBUSINESS_DATE(feptxnTbsdyFisc);
		cbstita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
		cbstita.setTXNSTAN(feptxn.getFeptxnStan());
		cbstita.setTERMINALID(feptxn.getFeptxnAtmno());
		cbstita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
		cbstita.setCARDISSUE_BANK(feptxn.getFeptxnTrinBkno());
		cbstita.setCARDTYPE(atmReq.getPICCDID()); //交易卡片型態
		cbstita.setRESPONSE_CODE("0000"); //正常才上送
		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 
		//Detail
		//第一次上送檢核時，無論自行跨行卡 均須上送主機
		if(txType.equals("0")){
			cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
			cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
			cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			// IC卡備註欄依財金說明轉換放置 <發卡行代號及帳/卡號>
			String ICMEMO = ConvertUtil.asciiToHex("0"+feptxn.getFeptxnTrinBkno()+feptxn.getFeptxnTrinActno()+StringUtils.repeat("0",10));
			ICMEMO = EbcdicConverter.toHex(CCSID.English, ICMEMO.length(), ICMEMO);
			cbstita.setICMEMO(ICMEMO); //IC卡備註欄 (未轉ASC，為原始電文)
			cbstita.setTXNICCTAC(atmReq.getPICCTAC()); //TAC(未轉ASC，為原始電文值)
		}else{
			cbstita.setICMEMO(StringUtils.repeat("40",30));
			cbstita.setTXNICCTAC(StringUtils.repeat("40",10));
		}
		cbstita.setTXNAMT(feptxn.getFeptxnTxAmt());
		cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
		cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno()+"0000");
		if("DA".equals(feptxn.getFeptxnTxCode())){
			cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno()+"9998");
		}
		if("DC".equals(feptxn.getFeptxnTxCode())){
			cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno()+"9999");
		}
		cbstita.setTOACT(atmReq.getTADATA().trim());
		cbstita.setTR_SPECIAL_FLAG(feptxn.getFeptxnTxCode());
		cbstita.setTXMEMO(feptxn.getFeptxnTelephone());
		//第二次上送入帳時提供給主機
		if(txType.equals("1")){
			cbstita.setI_ACT(feptxn.getFeptxnAcctSup());
			cbstita.setTRAMT(feptxn.getFeptxnTxAmtAct());
		}
		this.setoTita(cbstita);
		this.setTitaToString(cbstita.makeMessage());
		this.setASCIItitaToString(cbstita.makeMessageAscii());
		return FEPReturnCode.Normal;
	}

	/**
	 * 拆解CBS回應電文
	 *
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
		/* 電文內容格式請參照TOTA電文格式(AB_DP_O001) */
		/* 拆解主機回應電文 */
		AB_DP_O001 tota = new AB_DP_O001();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);

		/* 更新交易 */
		FEPReturnCode rtnCode = this.updateFEPTxn(tota);

		/* 回覆FEP */
		// 處理 CBS 回應
		return rtnCode;
	}

	/**
	 * 更新交易
	 *
	 * @param cbsTota
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTxn(AB_DP_O001 cbsTota) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnCbsTimeout((short) 0);
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		/* 變更FEPTXN交易記錄 */
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		if (!cbsTota.getIMSRC_TCB().equals("000")) {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			feptxn.setFeptxnAccType((short) 0); //未記帳
			rtnCode = FEPReturnCode.CBSCheckError;
		}else if(!cbsTota.getIMSRC4_FISC().equals("4001") && !cbsTota.getIMSRC4_FISC().equals("0000")) {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
			feptxn.setFeptxnAccType((short) 0); //未記帳
			rtnCode = FEPReturnCode.CBSCheckError;
		}else {
			if(cbsTota.getIMSRC_TCB().equals("000")) {
				feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
			}else {
				feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			}
            // CBS 帳務日(本行營業日, 民國年須轉西元年)
			String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
			if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
					|| (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
				imsbusinessDate = "00000000";
			} else {
				imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
			}
			feptxn.setFeptxnTbsdy(imsbusinessDate);
			// 記帳類別
			if ("Y".equals(cbsTota.getIMSACCT_FLAG())) {
				feptxn.setFeptxnAccType((short) 1); // 已記帳
			}
			// 帳務分行
			feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
			feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
			// 主機交易時間
			feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
			// 實際存入金額(已扣手續費)
			feptxn.setFeptxnTxAmtAct(cbsTota.getDPAMT());
			// 手續費
			feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
			feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
			// 交易通知方式
			if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
				feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
				//傳送通知代碼(CBS摘要)
				feptxn.setFeptxnCbsDscpt(cbsTota.getNOTICE_NUMBER());
			}
			rtnCode = FEPReturnCode.Normal;
		}
		this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
		return rtnCode;
	}

	/**
	 * 新增交易通知
	 *
	 * @param cbsTota
	 * @throws ParseException
	 */
	private void insertSMSMSG(AB_DP_O001 cbsTota) throws ParseException {
//		String txDate = feptxn.getFeptxnTxDate();
//		Integer ejfno = feptxn.getFeptxnEjfno();
//		Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(txDate, ejfno);
//		// 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//		// 以SMSMSG_TX_DATE及SMSMSG_EJFNO 為key讀取 Table
//		if (smsmsg == null) {
//			smsmsg = new Smsmsg();
//			smsmsg.setSmsmsgTxDate(txDate);
//			smsmsg.setSmsmsgEjfno(ejfno);
//			smsmsg.setSmsmsgStan(feptxn.getFeptxnStan());
//			smsmsg.setSmsmsgPcode(feptxn.getFeptxnPcode());
//			smsmsg.setSmsmsgTroutActno(feptxn.getFeptxnTroutActno());
//			smsmsg.setSmsmsgTxTime(feptxn.getFeptxnTxTime());
//			smsmsg.setSmsmsgBrno(feptxn.getFeptxnBrno());
//			smsmsg.setSmsmsgZone("TWN");
//			smsmsg.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//			smsmsg.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//			smsmsg.setSmsmsgTxCur(feptxn.getFeptxnTxCur());
//			smsmsg.setSmsmsgTxAmt(feptxn.getFeptxnTxAmt());
//			smsmsg.setSmsmsgTxCurAct(feptxn.getFeptxnTxCurAct());
//			smsmsg.setSmsmsgTxAmtAct(feptxn.getFeptxnTxAmtAct());
//			smsmsg.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//			smsmsg.setSmsmsgNotifyFg("Y");
//			smsmsg.setSmsmsgSendType(cbsTota.getNOTICE_TYPE());
//			smsmsg.setSmsmsgChannel(feptxn.getFeptxnChannel());
//			Date datenow = (Date) new SimpleDateFormat("yyyy/MM/DD-HH.mm.ss.SSS").parse(
//					FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_TAX));
//			smsmsg.setUpdateTime(datenow);
//			smsmsg.setSmsmsgNoticeNo(cbsTota.getNOTICE_NUMBER());
//
//			if (smsmsgExtMapper.insert(smsmsg) <= 0) {
//				getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//				this.logMessage(getLogContext());
//			}
//		}
	}
}
