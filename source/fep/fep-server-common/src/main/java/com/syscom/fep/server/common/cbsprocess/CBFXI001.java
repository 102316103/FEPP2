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
import com.syscom.fep.vo.text.ims.CB_FX_I001;
import com.syscom.fep.vo.text.ims.CB_FX_O001;

/**
 * 
 * 自行-指靜脈取消
 * 
 * 
 */
public class CBFXI001 extends ACBSAction {

	public CBFXI001(MessageBase txData) {
		super(txData, new CB_FX_O001());
	}
	
	/**
	 * 組CBS TITA電文
	 * 
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txtype) throws Exception {
		/* TITA 請參考合庫主機電文規格(CB_FX_I001)*/
		/* ATM.REQ 請參考 ATM_GeneralTrans */
		CB_FX_I001 cbstita = new CB_FX_I001();
		cbstita.setIMS_TRANS("MFEPAT00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));
		cbstita.setTXN_FLOW("C"); // 自行
		ATMGeneralRequest atmReq = this.getAtmRequest();
		cbstita.setMSG_CAT(atmReq.getMSGTYP());
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		cbstita.setPROCESS_TYPE("CHK"); // 查詢、檢核		
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
		// 交易卡片型態
		cbstita.setCARDTYPE(atmReq.getPICCDID());
		cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 
		// Detail
		cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
		cbstita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
		cbstita.setTXNICCTAC(atmReq.getPICCTAC()); //TAC(未轉ASC，為原始電文值)
		cbstita.setFROMACT(feptxn.getFeptxnMajorActno()); // 卡片帳號
		cbstita.setBIRDAY(atmReq.getSPECIALDATA().substring(46, 53));//客戶出生日期 SPECIALDATA[47:7]  

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
		/* 電文內容格式請參照TOTA電文格式(CB_FX_O001) */
		CB_FX_O001 tota = new CB_FX_O001();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);
		
		/* 更新交易 */
		FEPReturnCode rtnCode = this.updateFEPTxn(tota);
		
		/* 回覆FEP */
		return rtnCode;
	}
	
	/**
	 * 更新交易
	 * 
	 * @param cbsTota
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTxn(CB_FX_O001 cbsTota) throws Exception {
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
			rtnCode = FEPReturnCode.CBSCheckError;
		} else {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			// 主機交易時間
			feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
			/* 正常回應 */
			rtnCode = FEPReturnCode.Normal;
		}
		
		String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8); // 檔名SEQ為FEPTXN_TBSDY_FISC[7:2]
		feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFEPTxn"));
		feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
		return rtnCode;
	}
}
