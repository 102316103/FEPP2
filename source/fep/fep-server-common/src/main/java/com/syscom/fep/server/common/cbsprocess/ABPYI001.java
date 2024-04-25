package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.model.Npsunit;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_PY_I001;
import com.syscom.fep.vo.text.ims.AB_PY_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 代理全繳交易上送主機
 *
 * @author vincent
 *
 */

public class ABPYI001 extends ACBSAction {

	public ABPYI001(MessageBase txData) {
		super(txData, new AB_PY_O001());
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
		/* TITA 請參考合庫主機電文規格(AB_PY_I001) */
		// Header
		AB_PY_I001 cbstita = new AB_PY_I001();
		if("EAT".equals(feptxn.getFeptxnChannel())){
			RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getEATMRequest().getBody().getRq().getSvcRq();
			cbstita.setMSG_CAT(atmReq.getMSGTYP());
			cbstita.setCARDTYPE(atmReq.getPICCDID());
			cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			// 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
					&& "K".equals(atmReq.getPICCDID()) && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
				cbstita.setTXNICCTAC(atmReq.getATMDATA().substring(758,778)); // LL+DATA
			}else{
				cbstita.setTXNICCTAC(StringUtils.repeat("40",10));
			}
			/* 預約交易 */
			/*  (FEPTXN_MSGKIND = “T” OR “X” ) */
			if ("RV".equals(atmReq.getLANGID())
					&& SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())) {
				cbstita.setEATM_RESERVE_FLAG("Y");
				cbstita.setORIGINAL_TX_DAYTIME(atmReq.getPIEODT());
			}
			if(StringUtils.isNotBlank(atmReq.getSPECIALDATA().substring(75, 99))){
				cbstita.setPY_PAYTXNOL1(atmReq.getSPECIALDATA().substring(75, 99));//SPECIALDATA[76,24] 
			}
			cbstita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
			// ATM交易序號
			cbstita.setATMTRANSEQ(atmReq.getTRANSEQ());
		}else{
			ATMGeneralRequest atmReq = this.getAtmRequest();
			cbstita.setMSG_CAT(atmReq.getMSGTYP());
			cbstita.setCARDTYPE(atmReq.getPICCDID());
			cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			// 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
					&& "K".equals(atmReq.getPICCDID()) && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
				cbstita.setTXNICCTAC(atmReq.getPICCTAC()); //TAC(未轉ASC，為原始電文值)
			}else{
				cbstita.setTXNICCTAC(StringUtils.repeat("40",10));
			}
			cbstita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
			// ATM交易序號
			cbstita.setATMTRANSEQ(atmReq.getTRANSEQ());
		}
		cbstita.setIMS_TRANS("MFEPAT00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
		// TXN_FLOW
		if (feptxn.getFeptxnFiscFlag() == 0) {
			cbstita.setTXN_FLOW("C"); // 自行
		} else {
			cbstita.setTXN_FLOW("A"); // 代理
		}
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		// PROCESS TYPE
		if ("0".equals(txType)) { // 查詢、檢核
			cbstita.setPROCESS_TYPE("CHK");
		} else if ("1".equals(txType)) { // 入扣帳
			cbstita.setPROCESS_TYPE("ACCT");
		} else if ("2".equals(txType)) { // 沖正
			cbstita.setPROCESS_TYPE("RVS");
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
		cbstita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
		if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
			cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
		} else {
			cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		}

		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 
		
		// Detail
		cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbstita.setTXNAMT(feptxn.getFeptxnTxAmtAct());
		cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
		if(StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno())) {
			cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
		}
		if(StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno())) {
			cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
		}
		if ("256".equals(StringUtils.substring(feptxn.getFeptxnPcode(), 0, 3)) // 繳費移轉計畫
				&& "18888888".equals(feptxn.getFeptxnBusinessUnit()) && "59999".equals(feptxn.getFeptxnPaytype())
				&& "9999".equals(feptxn.getFeptxnPayno()) && "99991231".equals(feptxn.getFeptxnDueDate())) {
			cbstita.setPY_SPECIAL_FLAG("ET");
		}

		// 取得財金全繳委託單位檔之「帳務代理銀行」
		// 讀取 委託單位檔 NPSUNIT.NPSUNIT_BKNO
		Npsunit npsunit = npsunitExtMapper.selectByPrimaryKey(feptxn.getFeptxnBusinessUnit(), feptxn.getFeptxnPaytype(),
				feptxn.getFeptxnPayno());
		if (npsunit == null) {
			cbstita.setPY_HOST_BRANCH("");
		} else {
			cbstita.setPY_HOST_BRANCH(npsunit.getNpsunitBkno());
		}
		cbstita.setPY_PAYUNTNO(feptxn.getFeptxnBusinessUnit()); // 委託單位代號
		cbstita.setPY_TAXTYPE(feptxn.getFeptxnPaytype()); // 繳費類別
		cbstita.setPY_PAYFEENO(feptxn.getFeptxnPayno()); // 費用代號
		cbstita.setPY_PAYTXNOL(feptxn.getFeptxnReconSeqno()); // 銷帳編號
		cbstita.setPY_PAYDDATE(feptxn.getFeptxnDueDate()); // 繳款期限


		if ("A".equals(cbstita.getTXN_FLOW())
				&& "ACCT".equals(cbstita.getPROCESS_TYPE())) {// 代理記帳須提供
			// 使用 DecimalFormat 将结果格式化为 4 位数
			DecimalFormat df = new DecimalFormat("0000");
			cbstita.setPY_CHARGCUS(new BigDecimal(df.format(feptxn.getFeptxnNpsFeeCustpay().multiply(new BigDecimal("10")))));
			cbstita.setPY_CHARGUNT(new BigDecimal(
					df.format(feptxn.getFeptxnNpsFeeRcvr().multiply(new BigDecimal("10")))+
							df.format(feptxn.getFeptxnNpsFeeAgent().multiply(new BigDecimal("10")))+
							df.format(feptxn.getFeptxnNpsFeeTrout().multiply(new BigDecimal("10")))+
							df.format(feptxn.getFeptxnNpsFeeTrin().multiply(new BigDecimal("10")))+
							df.format(feptxn.getFeptxnNpsFeeFisc().multiply(new BigDecimal("10")))));
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
		/* 電文內容格式請參照TOTA電文格式(AB_PY_O001) */
		/* 拆解主機回應電文 */
		AB_PY_O001 tota = new AB_PY_O001();
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
	private FEPReturnCode updateFEPTxn(AB_PY_O001 cbsTota) throws Exception {
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
			// CBS 帳務日(民國年須轉西元年)
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
			// 合庫轉入帳務分行
			feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // ATM清算分行別取合庫轉出帳務分行
            feptxn.setFeptxnAtmBrno(feptxn.getFeptxnBrno());
            // 交易帳號掛帳行
            if (feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
            	feptxn.setFeptxnTxBrno(feptxn.getFeptxnBrno());
            }else if (feptxn.getFeptxnTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
            	feptxn.setFeptxnTxBrno(feptxn.getFeptxnTrinBrno());
            }
			// 主機交易時間
			feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
			/* 主機回傳的手續費 */
			// 手續費
			feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
			//帳戶餘額(下送空白不更新)
			feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
			// 促銷應用訊息
			if(StringUtils.isNotBlank(cbsTota.getLUCKYNO())){
				feptxn.setFeptxnLuckyno(cbsTota.getLUCKYNO());
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
	private void insertSMSMSG(AB_PY_O001 cbsTota) throws ParseException {
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
//			smsmsg.setSmsmsgZone("« TWN »");
//			smsmsg.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//			smsmsg.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//			smsmsg.setSmsmsgTxCur(feptxn.getFeptxnTxCur());
//			smsmsg.setSmsmsgTxAmt(feptxn.getFeptxnTxAmt());
//			smsmsg.setSmsmsgTxCurAct(feptxn.getFeptxnTxCurAct());
//			smsmsg.setSmsmsgTxAmtAct(feptxn.getFeptxnTxAmtAct());
//			smsmsg.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//			smsmsg.setSmsmsgNotifyFg("« Y »");
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
