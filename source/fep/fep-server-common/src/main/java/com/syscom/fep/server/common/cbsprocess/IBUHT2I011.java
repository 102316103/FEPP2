package com.syscom.fep.server.common.cbsprocess;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.vo.text.ims.IB_WD_O001;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.NpsunitMapper;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.IB_UHT2_I011;
import com.syscom.fep.vo.text.ims.IB_UHT2_O011;

/**
 * 組送CBS 主機原存消費扣款_變動費率Request交易電文
 *
 * @author Joseph
 */

public class IBUHT2I011 extends ACBSAction {

    public IBUHT2I011(MessageBase txData) {
        super(txData, new IB_UHT2_I011());
    }

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
    private NpsunitMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);
    FISC_INBK inbkReq = this.getInbkRequest();


    /**
     * 組CBS 原存交易Request電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        //組CBS 原存交易Request電文, 電文內容格式請參照: D_I6_T2合庫FEP_主機電文規格-原存行消費扣款變動(2541-3)V1.0(1111018).doc
        IB_UHT2_I011 cbsTita = new IB_UHT2_I011();
        // HEADER
        cbsTita.setIMS_TRANS("MFEPFG00"); // 主機業務別 長度8
        cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
        cbsTita.setSYS_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime()); // FEP交易日期時間 長度14
        cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()), 8, "0")); // FEP電子日誌序號 長度8
        cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
        cbsTita.setMSG_CAT("00");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 給0O */
        cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
        cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
        cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白
        if ("0".equals(txType)) {
            cbsTita.setPROCESS_TYPE("CHK"); /* 檢核 */
        } else if ("1".equals(txType)) {
            cbsTita.setPROCESS_TYPE("ACCT"); /* 帳務 */
        }
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
        cbsTita.setCARDTYPE("K"); // 交易卡片型態 長度1 /* K:晶片卡 */
        cbsTita.setRESPONSE_CODE(feptxn.getFeptxnReqRc()); // 回應代號(RC) 長度4
        cbsTita.setHRVS(StringUtils.repeat(" ", 33)); // 保留欄位 長度33
        if (FISCPCode.PCode2543.getValueStr().equals(feptxn.getFeptxnPcode())) {
            cbsTita.setICCHIPSTAN(StringUtils.repeat(" ", 8)); // IC卡交易序號
            cbsTita.setTERM_CHECKNO(StringUtils.repeat(" ", 8)); // 端末設備查核碼
            cbsTita.setTERMTXN_DATETIME(StringUtils.repeat(" ", 14)); // 交易日期時間
            cbsTita.setICMEMO(StringUtils.repeat("40", 30)); // IC卡備註欄
            cbsTita.setTXNICCTAC(StringUtils.repeat("40", 10)); // 交易驗證碼
        } else if (FISCPCode.PCode2541.getValueStr().equals(feptxn.getFeptxnPcode())
                || FISCPCode.PCode2542.getValueStr().equals(feptxn.getFeptxnPcode())) {
            cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno()); // IC卡交易序號
            cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk()); // 端末設備查核碼
            cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDatetimeFisc()); // 交易日期時間
            /* 2023/6/13 修改for 改用財金電文ICMARK欄位 */
            if(StringUtils.isNotBlank(this.getInbkRequest().getICMARK())){
                cbsTita.setICMEMO(this.getInbkRequest().getICMARK()); // IC卡備註欄
            }else{
                cbsTita.setICMEMO(StringUtils.repeat("40",30)); // IC卡備註欄
            }
            /* 2023/6/13 修改for 改用財金電文TAC欄位 */
            if (StringUtils.isNotBlank(this.getInbkRequest().getTAC())) {
                cbsTita.setTXNICCTAC("000A" + this.getInbkRequest().getTAC()); // 交易驗證碼
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10)); // 交易驗證碼
            }
        }
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt()); // 交易金額
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片帳號(轉出帳號)
        if (FISCPCode.PCode2541.getValueStr().equals(feptxn.getFeptxnPcode())
                || FISCPCode.PCode2542.getValueStr().equals(feptxn.getFeptxnPcode())) {
            cbsTita.setUH_Bill_No(feptxn.getFeptxnOrderNo()); // 訂單號碼
        } else {
            cbsTita.setUH_Bill_No(StringUtils.repeat(" ", 16));
        }
        cbsTita.setUH_Merchant_ID(feptxn.getFeptxnMerchantId()); // 特約商店代號
        if (FISCPCode.PCode2542.getValueStr().equals(feptxn.getFeptxnPcode())) {
            cbsTita.setUH_ORG_STAN(feptxn.getFeptxnOriStan()); // 原交易序號
            cbsTita.setUH_Reason_Code(feptxn.getFeptxnRsCode()); // 消費扣款沖正理由
        } else {
            cbsTita.setUH_ORG_STAN(StringUtils.repeat(" ", 10)); // 原交易序號
            cbsTita.setUH_Reason_Code(StringUtils.repeat(" ", 2)); // 消費扣款沖正理由
        }
        if ((FISCPCode.PCode2541.getValueStr().equals(feptxn.getFeptxnPcode())
                || FISCPCode.PCode2543.getValueStr().equals(feptxn.getFeptxnPcode()))
                && inbkReq.getTxnSourceInstituteId().substring(0, 2).equals("39") && inbkReq.getTxnSourceInstituteId().substring(3, 7).equals("7999")
                && !inbkReq.getTxnSourceInstituteId().equals("3997999")) {
            cbsTita.setUH_special_flag("UE");  /* 電支平台-約定連結付款交易 */
        } else {
            cbsTita.setUH_special_flag(StringUtils.repeat(" ", 2));
        }
        cbsTita.setDRVS(StringUtils.repeat(" ", 320)); // 保留欄位
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
        /* 電文內容格式請參照TOTA電文格式(IB_UHT2_O011) */
        /* 拆解主機回應電文 */
        IB_UHT2_O011 tota = new IB_UHT2_O011();
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
    private FEPReturnCode updateFEPTxn(IB_UHT2_O011 cbsTota, String type) throws Exception {
        FEPReturnCode rtnCode;
		feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME()); /* 主機交易時間 */
		feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
		feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
        feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
        feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE()); /* 帳戶手續費 */
        feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE()); /* 提領幣別手續費 */
        feptxn.setFeptxnBalb(cbsTota.getACT_BALANCE()); /* 帳戶餘額 */

        feptxn.setFeptxnLuckyno(cbsTota.getLUCKYNO()); /* 促銷應用訊息 */
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
            } else if("N".equals(cbsTota.getIMSACCT_FLAG())){
                feptxn.setFeptxnAccType((short) 0); /* 未記帳 */
            }
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
                /* 寫入簡訊資料檔 */
                this.insertSMSMSG(cbsTota);
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
	private void insertSMSMSG(IB_UHT2_O011 cbsTota) throws ParseException {
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
