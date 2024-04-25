package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.IMSOutputCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.AB_VA_I001;
import com.syscom.fep.vo.text.ims.AB_VA_O001;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ABVAI001 extends ACBSAction {

	public ABVAI001(MessageBase txData) {
		super(txData, new AB_VA_O001());
	}

	RCV_VA_GeneralTrans_RQ atmRequest = this.getVARequest();

	FISC_INBK inbkReq = this.getInbkRequest();
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
		/* TITA 請參考合庫主機電文規格(AB_VA_I001) */
		// Header
		AB_VA_I001 cbstita = new AB_VA_I001();
		cbstita.setIMS_TRANS("MFEPEA00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
		// TXN_FLOW
		if (DbHelper.toShort(false).equals(feptxn.getFeptxnFiscFlag())) {
			cbstita.setTXN_FLOW("C"); // 自行
		} else {
			cbstita.setTXN_FLOW("A"); // 代理
		}
		cbstita.setMSG_CAT(atmRequest.getBody().getRq().getSvcRq().getTXNTYPE());
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		// PROCESS_TYPE
		if(!DbHelper.toBoolean(feptxn.getFeptxnFiscFlag()) && feptxn.getFeptxnNoticeId().substring(0,2).equals("10")){
			cbstita.setPROCESS_TYPE("CHK");
		}else{
			cbstita.setPROCESS_TYPE("SET");
		}
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
		cbstita.setCARDISSUE_BANK(atmRequest.getBody().getRq().getSvcRq().getAEIPCRBK());
		// cbstita.setCBSMAC();

		String notice12 = feptxn.getFeptxnNoticeId().substring(0, 2);
		String notice34 = feptxn.getFeptxnNoticeId().substring(2, 4);
		if(notice12.equals("10")){
			if(!DbHelper.toBoolean(feptxn.getFeptxnFiscFlag())){
				if(notice34.equals("00")){
					cbstita.setCARDTYPE("K");
				}else{
					cbstita.setCARDTYPE("X");
				}
			}else{
				cbstita.setCARDTYPE("N");
			}
		}else{
			cbstita.setCARDTYPE("N");
		}

		if(StringUtils.isNotBlank(feptxn.getFeptxnRepRc())){
			cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
		}else{
			cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		}

		// Detail
		// cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setICCHIPSTAN(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnStan()),8,"0"));
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbstita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());
		if(StringUtils.isNotBlank(atmRequest.getBody().getRq().getSvcRq().getICMARK()))
			cbstita.setICMEMO(atmRequest.getBody().getRq().getSvcRq().getICMARK());
		else
			cbstita.setICMEMO("463232393035323735353939393737363631323331363130312020202037");

		//第一次上送CBS
		boolean isSysstatHbknoEqFeptxnTroutBkno = SysStatus.getPropertyValue().getSysstatHbkno()
				.equals(feptxn.getFeptxnTroutBkno());
		if(isSysstatHbknoEqFeptxnTroutBkno && "RQ".equals(atmRequest.getBody().getRq().getSvcRq().getTXNTYPE())) {
			cbstita.setTERMTXN_DATETIME(FormatUtil.dateTimeFormat(CalendarUtil.parseDateTimeValue(Integer.valueOf(atmRequest.getBody().getRq().getSvcRq().getAEICDAY()), Integer.valueOf(atmRequest.getBody().getRq().getSvcRq().getAEICTIME())), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
			String icTac = StringUtils.leftPad(StringUtils.defaultIfEmpty(atmRequest.getBody().getRq().getSvcRq().getIC_TAC(), StringUtils.EMPTY), 16, "0"); ;
			cbstita.setTXNICCTAC(atmRequest.getBody().getRq().getSvcRq().getIC_TAC_LEN() + icTac);
		} else {
			cbstita.setTXNICCTAC("40404040404040404040");
		}
		cbstita.setTXNAMT(feptxn.getFeptxnTxAmt());
		cbstita.setAEIPYAC(feptxn.getFeptxnTroutActno());
		if("02".equals(notice12)) {
			if(!"03".equals(notice34)) {
				cbstita.setFROMACT(feptxn.getFeptxnMajorActno());
			}
		} else {
			// if("00".equals(notice34)){
			// 	cbstita.setFROMACT(feptxn.getFeptxnMajorActno());
			// }else{
				cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
			// }
		}
		if("10".equals(notice12) && DbHelper.toBoolean(feptxn.getFeptxnFiscFlag())) {
			cbstita.setCHARGCUS(feptxn.getFeptxnFeeCustpay());
		}else {
			cbstita.setCHARGCUS(BigDecimal.ZERO);
		}
		cbstita.setVACATE(notice12);
		cbstita.setAEIPYTP(notice34);
		if("10".equals(notice12)) {
			cbstita.setAEIPYUES(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYUES());
			cbstita.setAELFTP(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP());
			/* 以下三個欄位，若是跨行，財金回0210，才有值 */
			if(DbHelper.toBoolean(feptxn.getFeptxnFiscFlag())){
				cbstita.setAEILFRC1(feptxn.getFeptxnRemark().substring(0,2));
				cbstita.setAEILFRC2(feptxn.getFeptxnRemark().substring(2,4));
				cbstita.setOPENACT(feptxn.getFeptxnRemark().substring(4,6));
			}
			// else{
			// 	cbstita.setAEILFRC1("00");
			// 	cbstita.setAEILFRC2("00");
			// 	cbstita.setOPENACT("00");
			// }
		}
		// else{
		// 	cbstita.setAEIPYUES("00");
		// 	cbstita.setAELFTP("00");
		// 	cbstita.setAEILFRC1("00");
		// 	cbstita.setAEILFRC2("00");
		// 	cbstita.setOPENACT("00");
		// }
		cbstita.setIDNO(feptxn.getFeptxnIdno());

		if("02".equals(notice12)) {
			if(!"03".equals(notice34)) {
				cbstita.setCLCPYCI(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI());
			}
			cbstita.setPAYUNTNO(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO());
			cbstita.setTAXTYPE(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE());
			cbstita.setPAYFEENO(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());
			cbstita.setAEIPYAC2(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYAC2());
		}
		cbstita.setMOBILENO(atmRequest.getBody().getRq().getSvcRq().getMOBILENO());
		cbstita.setINSURE_IDNO(" ");
		if("10".equals(notice12)) {
			cbstita.setBIRTHDAY(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY().toString());
			cbstita.setTELHOME(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
		}
		cbstita.setAEIPYBK(atmRequest.getBody().getRq().getSvcRq().getAEIPYBK());
		cbstita.setAEIPYBH(atmRequest.getBody().getRq().getSvcRq().getAEIPYBH());
		cbstita.setSSLTYPE(atmRequest.getBody().getRq().getSvcRq().getSSLTYPE());

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
		/* 電文內容格式請參照TOTA電文格式(AB_VA_O001) */
		/* 拆解主機回應電文 */
		AB_VA_O001 tota = new AB_VA_O001();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);//塞入拆解後的tota讓AA取得

		/* 更新交易 */
		this.updateFEPTxn(tota);

		/* 新增交易通知 */
		if (StringUtils.isNotBlank(tota.getNOTICE_TYPE())) {
			insertSMSMSG(tota);
		}
		/* 回覆FEP */
		// 處理 CBS 回應
		if ("000".equals(tota.getIMSRC_TCB())) {
			/* 正常回應 */
			return FEPReturnCode.Normal;
		} else if (IMSOutputCode.Error.equals(tota.getIMSRC_TCB())) {
			/* 回應錯誤 */
			return FEPReturnCode.CBSCheckError;
		}
		return FEPReturnCode.CBSResponseError;
	}

	/**
	 * 更新交易
	 *
	 * @param cbsTota
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTxn(AB_VA_O001 cbsTota) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0);
		/* 變更FEPTXN交易記錄 */
		// IMSRC_TCB = "000" or empty表交易成功
		// IMSRC_FISC = "0000" or "4001" 表交易成功
		if(!"000".equals(cbsTota.getIMSRC_TCB()) && StringUtils.isNotBlank(cbsTota.getIMSRC_TCB())){
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			feptxn.setFeptxnAccType((short) 0); // 未記帳
			rtnCode = FEPReturnCode.CBSCheckError;
		}else{
			feptxn.setFeptxnCbsRc(NormalRC.CBS_OK);
			// CBS 帳務日(本行營業日)
			//轉西元年
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
			}else{
				feptxn.setFeptxnAccType((short)0);
			}
			//帳務分行
			feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
			feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
			// 主機交易時間
			feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
			/* 主機回傳的手續費 */
			// 手續費(轉出客戶)
			feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
			// 交易通知方式
			if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
				feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
			}
			String notice12 = feptxn.getFeptxnNoticeId().substring(0, 2);
			String notice34 = feptxn.getFeptxnNoticeId().substring(2, 4);
			Vatxn vatxn = this.getNBData().getVatxn();
			if("02".equals(notice12) && "03".equals(notice34)) {
				feptxn.setFeptxnTroutActno(cbsTota.getFROMACT());
				vatxn.setVatxnTroutActno(cbsTota.getFROMACT());
				vatxn.setVatxnBusinessUnit(cbsTota.getPAYUNTNO());
				vatxn.setVatxnPaytype(cbsTota.getTAXTYPE());
				vatxn.setVatxnFeeno(cbsTota.getPAYFEENO());
			} else if ("10".equals(notice12)) {
				vatxn.setVatxnResult(cbsTota.getAEILFRC1());
				vatxn.setVatxnAcresult(cbsTota.getAEILFRC2());
				vatxn.setVatxnAcstat(cbsTota.getOPENACT());
				vatxn.setVatxnTelresult(cbsTota.getMNO_CHANGE());
			}
			rtnCode = FEPReturnCode.Normal; /* 收到主機回應, 改成 Normal */
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
	private void insertSMSMSG(AB_VA_O001 cbsTota) throws ParseException {
//		String txDate = feptxn.getFeptxnTxDate();
//		Integer ejfno = feptxn.getFeptxnEjfno();
//		Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(txDate, ejfno);
//		// 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
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
