package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.CB_P1T_I001;
import com.syscom.fep.vo.text.ims.CB_P1T_O001;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

/**
 * 組送CBS主機自行磁條卡密碼變更Request交易電文
 * 
 * @author Joseph
 *
 */

public class CBP1TI001 extends ACBSAction {

	public CBP1TI001(MessageBase txData) {
		super(txData, new CB_P1T_O001());
	}

	/**
	 * 組CBS TITA電文
	 * 
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(CB_P1T_I001) */
		// Header
		CB_P1T_I001 cbstita = new CB_P1T_I001();
		ATMGeneralRequest atmReq = this.getAtmRequest();
		cbstita.setIMS_TRANS("MFEPAT00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
		cbstita.setTXN_FLOW("C"); // 自行
		cbstita.setMSG_CAT(atmReq.getMSGTYP());
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
		cbstita.setCARDISSUE_BANK(atmReq.getCARDPART1().substring(3, 6));// ATM.REQ.CARDPART1[4:3]
		cbstita.setCARDTYPE("P"); //磁條密碼變更
		cbstita.setRESPONSE_CODE("0000"); //正常才上送
		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 
		//Detail
		cbstita.setICMEMO(StringUtils.repeat("40",30));
		cbstita.setTXNICCTAC(StringUtils.repeat("40",10));
		cbstita.setFROMACT(atmReq.getCARDPART1().substring(6, 21)); //卡片帳號  ATM.REQ.CARDPART1[7:15]
		cbstita.setPIEPIN(feptxn.getFeptxnPinblock()); //磁條密碼
		cbstita.setPIPTRIES(atmReq.getPIPTRIES()); //密碼TRY次數
		this.setoTita(cbstita);
		this.setTitaToString(cbstita.makeMessage());
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
		/* 電文內容格式請參照TOTA電文格式(CB_P1T_O001) */
		/* 拆解主機回應電文 */
		CB_P1T_O001 tota = new CB_P1T_O001();
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
	private FEPReturnCode updateFEPTxn(CB_P1T_O001 cbsTota) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnCbsTimeout((short) 0);
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
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
