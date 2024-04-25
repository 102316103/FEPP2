package com.syscom.fep.server.common.cbsprocess;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.CB_SS_I001;
import com.syscom.fep.vo.text.ims.CB_SS_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;

/**
 * 組送CBS主機舊卡啟用新卡Request交易電文
 * 
 * @author Joseph
 *
 */

public class CBSSI001 extends ACBSAction {

	public CBSSI001(MessageBase txData) {
		super(txData, new CB_SS_O001());
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
		/* TITA 請參考合庫主機電文規格(CB_SS_I001) */
		// Header
		CB_SS_I001 cbstita = new CB_SS_I001();
		if("EAT".equals(feptxn.getFeptxnChannel())){
			RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getEATMRequest().getBody().getRq().getSvcRq();
			cbstita.setMSG_CAT(atmReq.getMSGTYP());
			cbstita.setCARDTYPE(atmReq.getPICCDID()); //交易卡片型態
			cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			cbstita.setFACODE(atmReq.getFADATA().substring(0,2));
			cbstita.setCARDTYPE(atmReq.getTACODE().substring(0,1));
			if(atmReq.getTACODE().substring(0,1).equals("C")){
				cbstita.setBIRTHDAY(StringUtils.rightPad(atmReq.getFADATA().substring(0,7),8," "));
			}
		}else{
			ATMGeneralRequest atmReq = this.getAtmRequest();
			cbstita.setMSG_CAT(atmReq.getMSGTYP());
			cbstita.setCARDTYPE(atmReq.getPICCDID()); //交易卡片型態
			cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			cbstita.setFACODE(atmReq.getFADATA().substring(0,2));
			cbstita.setCARDTYPE(atmReq.getTACODE().substring(0,1));
			if(atmReq.getTACODE().substring(0,1).equals("C")){
				cbstita.setBIRTHDAY(StringUtils.rightPad(atmReq.getFADATA().substring(0,7),8," "));
			}
		}
		cbstita.setIMS_TRANS("MFEPAT00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
		cbstita.setTXN_FLOW("C"); // 自行
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		cbstita.setPROCESS_TYPE("CHK"); //查詢、檢核
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
		cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 
		// Detail
		cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbstita.setICMEMO(getAtmData().getTxRequestMessage().substring(446,506)); // IC卡備註欄
		cbstita.setTXNICCTAC(getAtmData().getTxRequestMessage().substring(758,778)); // LL+DATA
		cbstita.setFROMACT(feptxn.getFeptxnTroutActno()); //卡片帳號

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
		/* 電文內容格式請參照TOTA電文格式(CB_SS_O001) */
		/* 拆解主機回應電文 */
		CB_SS_O001 tota = new CB_SS_O001();
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
	private FEPReturnCode updateFEPTxn(CB_SS_O001 cbsTota) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0);
		feptxn.setFeptxnAccType((short) 0); // 未記帳
		/* 變更FEPTXN交易記錄 */
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		if (!cbsTota.getIMSRC_TCB().equals("000")) {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			feptxn.setFeptxnAccType((short) 0); //未記帳
			rtnCode = FEPReturnCode.CBSCheckError;
		} else {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			// 主機交易時間
			feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
			rtnCode = FEPReturnCode.Normal;
		}
		this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
		return rtnCode;
	}
}
