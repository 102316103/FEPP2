package com.syscom.fep.server.common.cbsprocess;

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
import com.syscom.fep.vo.text.ims.CB_TXEND;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

/**
 * 組送CBS主機舊卡啟用新卡Request交易電文
 * 
 * @author Joseph
 *
 */

public class CBTXEND extends ACBSAction {

	public CBTXEND(MessageBase txData) {
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
		/* TITA 請參考合庫主機電文規格(CB_TXEND) */
		// Header
		CB_TXEND cbstita = new CB_TXEND();
		cbstita.setIMS_TRANS("MFEPMS00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(feptxn.getFeptxnTxDate()+feptxn.getFeptxnTxTime());
		cbstita.setFEP_EJNO(feptxn.getFeptxnEjfno() != null ? StringUtils.leftPad(feptxn.getFeptxnEjfno().toString(),8,"0") : "");
		cbstita.setMSG_CAT("AF");
		if(feptxn.getFeptxnChannel().length() >3){
			cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel().substring(0,3));
		}else{
			cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		}
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		if(StringUtils.isNotBlank(feptxn.getFeptxnTxCode())){
			cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		}
		cbstita.setPROCESS_TYPE("END"); //交易結束
		cbstita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
		cbstita.setTXNSTAN(feptxn.getFeptxnStan());
		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 

		this.setoTita(cbstita);
		this.setTitaToString(cbstita.makeMessage());
		this.setASCIItitaToString(cbstita.makeMessageAscii());
		return FEPReturnCode.Normal;
	}

	@Override
	public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
		return null;
	}
}
