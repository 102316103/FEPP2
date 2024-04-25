package com.syscom.fep.server.common.cbsprocess;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_WD_I002;
import com.syscom.fep.vo.text.ims.AB_WD_O002;

public class ABWDI002 extends ACBSAction {

	public ABWDI002(MessageBase txData) {
		super(txData, new AB_WD_O002());
	}

	ATMGeneralRequest atmReq = this.getAtmRequest();

	/**
	 * 組CBS TITA電文
	 *
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(AB_WD_I002) */
		// Header
		AB_WD_I002 cbstita = new AB_WD_I002();
		if("US".equals(atmReq.getFSCODE()) || "JP".equals(atmReq.getFSCODE())){
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
		// PROCESS_TYPE
		cbstita.setPROCESS_TYPE("WDE");
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
		cbstita.setCARDTYPE(atmReq.getPICCDID());
		// 不論自行或跨行，檢核正常才上送
		cbstita.setRESPONSE_CODE("0000");
		//ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno());
		// Detail
		cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
		cbstita.setATMSTATUS_CODE(feptxn.getFeptxnConExcpCode());
		cbstita.setRVSTXAMT("+" + StringUtils.leftPad(StringUtils.substring(atmReq.getSTATUS(), 2, 12), 13, "0")); // 沖正金額
		this.setoTita(atmReq);
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
		/* 電文內容格式請參照TOTA電文格式(AB_WD_O002) */
		/* 拆解主機回應電文 */
		AB_WD_O002 tota = new AB_WD_O002();
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
	private FEPReturnCode updateFEPTxn(AB_WD_O002 cbsTota) throws Exception {
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "4");
		feptxn.setFeptxnCbsTimeout((short) 0);
		
		/* 變更FEPTXN交易記錄 */
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		
		if (!cbsTota.getIMSRC_TCB().equals("000")) {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			feptxn.setFeptxnAccType((short) 0); //未記帳
		}else if(!cbsTota.getIMSRC4_FISC().equals("4001") && !cbsTota.getIMSRC4_FISC().equals("4002")
				&& !cbsTota.getIMSRC4_FISC().equals("4007") && StringUtils.isNotBlank(cbsTota.getIMSRC4_FISC()) ) {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
			feptxn.setFeptxnAccType((short) 3); //更正失敗
		}
		
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
		if ("Y".equals(cbsTota.getIMSRVS_FLAG())) {
			feptxn.setFeptxnAccType((short) 2); // 已更正
		}
		
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
        
        return FEPReturnCode.Normal;
	}
}
