package com.syscom.fep.server.common.cbsprocess;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.ref.RefString;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.model.Nwdtxn;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_WD_I001;
import com.syscom.fep.vo.text.ims.AB_WD_O001;

public class ABWDI001 extends ACBSAction {

	public ABWDI001(MessageBase txData) {
		super(txData, new AB_WD_O001());
	}

	ATMGeneralRequest atmReq = this.getAtmRequest();
//	private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
	private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);

	/**
	 * 組CBS TITA電文
	 *
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(AB_WD_I001) */
		// Header
		AB_WD_I001 cbstita = new AB_WD_I001();
		if("US".equals(feptxn.getFeptxnTxCode()) || "JP".equals(feptxn.getFeptxnTxCode())){
			cbstita.setIMS_TRANS("MFEPAF00");
		}else{
			cbstita.setIMS_TRANS("MFEPAT00");
		}
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));

		boolean isSysstatHbknoEqFeptxnTroutBkno = SysStatus.getPropertyValue().getSysstatHbkno()
				.equals(feptxn.getFeptxnTroutBkno());
		if (isSysstatHbknoEqFeptxnTroutBkno) {
			cbstita.setTXN_FLOW("C"); // 自行
		} else {
			cbstita.setTXN_FLOW("A"); // 跨行
		}
		cbstita.setMSG_CAT(atmReq.getMSGTYP());
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		cbstita.setPROCESS_TYPE("ACCT");
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
		cbstita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
		cbstita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
		if (isSysstatHbknoEqFeptxnTroutBkno) {
			cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		} else {
			cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
		}
		//ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno());
		// Detail
		cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
		cbstita.setICMEMO(getAtmData().getTxRequestMessage().substring(446,506)); // IC卡備註欄
		if(feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())){
			cbstita.setTXNICCTAC(getAtmData().getTxRequestMessage().substring(758,778)); // LL+DATA
		}else{
			cbstita.setTXNICCTAC(StringUtils.repeat("40",10));
		}
		cbstita.setTXNAMT(feptxn.getFeptxnTxAmtAct());
		cbstita.setFROMACT(feptxn.getFeptxnTroutActno());

		// 不同 FSCODE 的處理
		switch (feptxn.getFeptxnTxCode()) {
		case "W2":
			cbstita.setFROMACT(atmReq.getFADATA().substring(2, 18));
			//自行卡才放
			if(feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
				cbstita.setPINBLOCK(feptxn.getFeptxnPinblock());
			}
			break;
		case "WF":
			//自行卡才放(WF目前是自行交易)
			if(feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
				cbstita.setPINBLOCK(feptxn.getFeptxnPinblock());
			}
			cbstita.setCUSTOMER_ID(feptxn.getFeptxnIdno());
			break;
		case "US":
		case "JP":
			cbstita.setEXCHANGE_RATE(StringUtils.substring(atmReq.getTADATA(), 0, 8));
			cbstita.setCUSTOMER_NATIONTYPE(StringUtils.substring(atmReq.getTADATA(), 10, 11)); // 身分別
			cbstita.setFCWDAMT(feptxn.getFeptxnTxAmt());
			break;
		default:
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
		/* 電文內容格式請參照TOTA電文格式(AB_WD_O001) */
		/* 拆解主機回應電文 */
		AB_WD_O001 tota = new AB_WD_O001();
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
	private FEPReturnCode updateFEPTxn(AB_WD_O001 cbsTota) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0);
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
			// 主機交易時間
			feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());

			// 交易金額
			if(cbsTota.getTXNAMT() != null){
				feptxn.setFeptxnTxAmtAct(cbsTota.getTXNAMT());
			}

			// 帳戶餘額
			if(cbsTota.getACTBALANCE() != null){
				feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
			}

			// 手續費折算原幣別
			feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());

			//提領當地手續費
			if(feptxn.getFeptxnTxCur().equals("TWD")){
				feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
			}else{ // 外幣
				feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE().divide(feptxn.getFeptxnExrate(),2, RoundingMode.HALF_UP));
			}

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
	private void insertSMSMSG(AB_WD_O001 cbsTota) throws ParseException {
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
