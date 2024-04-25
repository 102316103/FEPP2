package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ObtltxnExtMapper;
import com.syscom.fep.mybatis.model.Obtltxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.host.App;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.FISCType;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 負責處理財金發動的跨境電子支付Confirm電文
 * pcode=2555 跨境電子支付交易
 * pcode=2556 跨境電子支付退款交易
 *
 * @author Richard    --> Ben
 */
public class OBConfirmI extends INBKAABase {
    private Obtltxn record = new Obtltxn();
    private FEPReturnCode rc = CommonReturnCode.Normal;
    private FEPReturnCode rc2 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    //private FEPReturnCode _rtnCode1 = CommonReturnCode.Normal; // for主流程第三點更新交易記錄(FEPTXN & OBTLTXN)，儲存更新失敗的FEPReturnCode
    private boolean isExitProgram = false;

    /**
     * AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception
     */
    public OBConfirmI(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     */
    @Override
    public String processRequestData() throws Exception {
        try {
            //1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
            rc = this.processRequest();
            //程式結束
            if (isExitProgram) {
                return StringUtils.EMPTY;
            }
            if (CommonReturnCode.Normal.equals(rc)) {
                //2.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
                rc = this.checkBusinessRule();
                //EXIT PROGRAM
                if (isExitProgram) {
                    return StringUtils.EMPTY;
                }
                if (CommonReturnCode.Normal.equals(rc)) {
                    //3.UpdateTxData:更新交易記錄 (FEPTXN & OBTLTXN)
                    rc = this.updateTxData();

                    //程式結束
                    if (isExitProgram) {
                        getLogContext().setMessage(rc.toString());
                        getLogContext().setRemark("寫入檔案發生錯誤!!");
                        logMessage(Level.INFO, getLogContext());
                        return StringUtils.EMPTY;
                    }
                    //4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS)
                    this.processAPTOTSendToCBS();
                }
            }
            //6.更新交易記錄(FEPTXN) : if need
            this.updateFEPTXN();
            //7.簡訊/EMAL/推播:
            this.sendToMailHunter();
            //8.判斷是否需傳送2160電文給財金
            if (StringUtils.isNotBlank(feptxn.getFeptxnSend2160())
                    && "Y".equals(feptxn.getFeptxnSend2160())) {
                /* 寫入2160發送資料檔 */
                this.insertINBK2160();
            }
            //9.FEP通知主機交易結束
            rc = SendToCBS();
        } catch (Exception e) {
            this._rtnCode = CommonReturnCode.ProgramException;
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(this.logContext);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
     *
     * @return
     */
    private FEPReturnCode processRequest() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            // 檢核Header
            rtnCode = this.getFiscBusiness().checkHeader(this.getFiscCon(), true);
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().setFeptxn(null);
                getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
                isExitProgram = true;
                return rtnCode;
            }

            getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
            getTxData().setFeptxn(getFiscBusiness().getFeptxn());

            //FISCRC=6101(跨行可用餘額小於零，不得交易)，要寄Email需將值塞入LogContext.TroutActno
            getLogContext().setAtmNo(getFiscBusiness().getFeptxn().getFeptxnAtmno());
            getLogContext().setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
            getLogContext().setTrinBank(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
            getLogContext().setTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
            getLogContext().setTroutBank(getFiscBusiness().getFeptxn().getFeptxnTroutBkno());

            return rtnCode;
        } catch (Exception e) {
            this.getLogContext().setProgramException(e);
            this.getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequest"));
            sendEMS(this.getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 商業邏輯檢核,電文Body檢核
     *
     * @return
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(this.getFiscBusiness().getFeptxn(), this.getTxData());
        String wk_TX_DATE = null;

        try {
            // 檢核 Mapping 欄位
//			wk_TX_DATE = CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6), 7, '0'));
//			if (!wk_TX_DATE.equals(getFiscBusiness().getFeptxn().getFeptxnReqDatetime().substring(0, 8))
//					|| !getFiscBusiness().getFeptxn().getFeptxnReqDatetime().substring(8, 14)
//							.equals(getFiscCon().getTxnInitiateDateAndTime().substring(6, 12))
//					|| !getFiscBusiness().getFeptxn().getFeptxnDesBkno()
//							.equals(getFiscCon().getTxnDestinationInstituteId().substring(0, 3))
//					|| (StringUtils.isNotBlank(getFiscCon().getATMNO())
//							&& !getFiscCon().getATMNO().equals(getFiscBusiness().getFeptxn().getFeptxnAtmno()))
//					|| (StringUtils.isNotBlank(getFiscCon().getTxAmt()) && MathUtil
//							.compareTo(getFiscBusiness().getFeptxn().getFeptxnTxAmt(), getFiscCon().getTxAmt()) != 0)) {
//				return FISCReturnCode.OriginalMessageDataError;
//			}

            /* Confirm電文比對原交易 */
            String aa = (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6), 7, "0"))) + this.getFiscCon().getTxnInitiateDateAndTime().substring(6, 12);
            if (!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(aa) ||
                    (!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0, 3))) ||
                    (StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))
                    || (StringUtils.isNotBlank(this.getFiscCon().getTxAmt()) && !new DecimalFormat("0.00").format(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct()).equals(new DecimalFormat("0.00").format(new BigDecimal(this.getFiscCon().getTxAmt()))))) {
                return FEPReturnCode.OriginalMessageDataError;
            }

            //2.2 檢核交易是否未完成
            if (!"B".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
                //10/20 修改, 財金錯誤代碼改為 '0101'
                rtnCode = FISCReturnCode.MessageFormatError;   //**相關欄位檢查錯誤
                return rtnCode;
            }
            /*9/22 修改 for CON 送2次 */
            if (getFiscBusiness().getFeptxn().getFeptxnTraceEjfno() != 0) {
                getLogContext().setRemark("已有Confirm電文, FEPTXN_TRACE_EJFNO=" + getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().toString());
                logMessage(Level.INFO, getLogContext());
                rtnCode = FISCReturnCode.MessageFormatError; // **相關欄位檢查錯誤
                getFiscBusiness().setFeptxn(null); //第2次Confirm則不更新FEPTXN
                getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
                isExitProgram = true;
                return rtnCode;
            }
            //2.3 檢核 MAC
            getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
            // '2017/11/17 Modify by Ruling for 收到財金確認電文時間寫入FEPTXN
            getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            rtnCode = encHelper.checkFiscMac(getFiscCon().getMessageType(), getFiscCon().getMAC());
            this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnConRc(null);    //**訊息押碼錯誤
                return FEPReturnCode.ENCCheckMACError;
            }
            return rtnCode;
        } catch (Exception e) {
            this.getLogContext().setProgramException(e);
            this.getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
            sendEMS(this.getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 3.UpdateTxData:更新交易記錄 (FEPTXN & OBTLTXN)
     *
     * @return
     */
    private FEPReturnCode updateTxData() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            this.getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                if (NormalRC.FISC_ATM_OK.equals(getFiscCon().getResponseCode())) {
                    this.getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // A 成功
                } else {
                    this.getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); // C Accept-Reverse
                }
                this.getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
            }
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // F3
            getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
            getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
			
			/*
			// 換日後 fiscBusiness.DBFEPTXN 以及 txData.DBFEPTXN 都應該使用實際交易日期，避免後面更新不到FEPTXN
			this.feptxnDao.setTableNameSuffix(this.getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, ".updateTxData"));
			this.getTxData().setFeptxnDao(this.feptxnDao);
			this.getFiscBusiness().setFeptxnDao(this.feptxnDao);
			*/

            int i = this.feptxnDao.updateByPrimaryKeySelective(this.getFiscBusiness().getFeptxn());
            if (i <= 0) {
                transactionManager.rollback(txStatus);
                rtnCode = IOReturnCode.FEPTXNUpdateError;
                isExitProgram = true;
                return rtnCode;
            }
            //須讀取 OBTLTXN
            ObtltxnExtMapper obtltxnExtMapper = SpringBeanFactoryUtil.getBean(ObtltxnExtMapper.class);
            record = obtltxnExtMapper.selectByPrimaryKey(getFiscBusiness().getFeptxn().getFeptxnTxDate(), getFiscBusiness().getFeptxn().getFeptxnEjfno());
            if (record == null) {
                // 找不到OBTLTXN
                transactionManager.rollback(txStatus);
                this.getLogContext().setRemark(StringUtils.join(
                        "updateTxData-查詢OBTLTXN失敗, OBTLTXN_TX_DATE=", getFiscBusiness().getFeptxn().getFeptxnTxDate(),
                        ", OBTLTXN_EJFNO=", getFiscBusiness().getFeptxn().getFeptxnEjfno()));
                logMessage(Level.INFO, this.getLogContext());
                rtnCode = IOReturnCode.QueryNoData;
                isExitProgram = true;
                return rtnCode;
            } else {
                // 有找到OBTLTXN
                record.setObtltxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
                record.setObtltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
                int iRes = obtltxnExtMapper.updateByPrimaryKeySelective(record);
                if (iRes <= 0) {
                    transactionManager.rollback(txStatus);
                    this.getLogContext().setRemark(StringUtils.join(
                            "updateTxData-更新OBTLTXN失敗, OBTLTXN_TX_DATE=", getFiscBusiness().getFeptxn().getFeptxnTxDate(),
                            ", OBTLTXN_EJFNO=", getFiscBusiness().getFeptxn().getFeptxnEjfno()));
                    logMessage(Level.INFO, this.getLogContext());
                    rtnCode = IOReturnCode.UpdateFail;
                    isExitProgram = true;
                    return rtnCode;
                }
            }

            // 累計退費金額
            if (FISCPCode.PCode2556.getValueStr().equals(this.getFiscBusiness().getFeptxn().getFeptxnPcode())
                    && NormalRC.FISC_ATM_OK.equals(this.getFiscBusiness().getFeptxn().getFeptxnRepRc())
                    && NormalRC.FISC_ATM_OK.equals(this.getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                Obtltxn obtltxn = new Obtltxn();
                obtltxn.setObtltxnTbsdyFisc(this.getFiscBusiness().getFeptxn().getFeptxnDueDate());
                obtltxn.setObtltxnBkno(this.getFiscBusiness().getFeptxn().getFeptxnBkno());
                obtltxn.setObtltxnStan(this.getFiscBusiness().getFeptxn().getFeptxnOriStan());
                obtltxn.setObtltxnTotRetAmt(record.getObtltxnTotRetAmt().add(record.getObtltxnTotTwdAmt())); // 累加寫在UpdateByStan用SQL的"+="
                int iRes = obtltxnExtMapper.updateByStan(obtltxn);
                if (iRes <= 0) {
                    transactionManager.rollback(txStatus);
                    this.getLogContext().setRemark("updateTxData-更新oriOBTLTXN的累計退貨金額失敗");
                    logMessage(Level.INFO, this.getLogContext());
                    rtnCode = IOReturnCode.UpdateFail;
                    return rtnCode;
                }
            }
            transactionManager.commit(txStatus);
            return CommonReturnCode.Normal;
        } catch (Exception e) {
            // 若失敗則復原
            transactionManager.rollback(txStatus);
            this.getLogContext().setProgramException(e);
            this.getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
            sendEMS(this.getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC
     *
     * @return
     * @throws Exception
     */
    private void processAPTOTSendToCBS() throws Exception {
        //FEPReturnCode rtnCode = CommonReturnCode.Normal;
        if (NormalRC.FISC_ATM_OK.equals(this.getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            // +REP
            if (!NormalRC.FISC_ATM_OK.equals(this.getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                // -CON
                //沖轉跨行代收付
                rc = this.getFiscBusiness().processOBAptot(true);
                this.getLogContext().setProgramName(ProgramName);
                this.getLogContext().setRemark(StringUtils.join("FEPTXN_CBS_TX_CODE=", this.getFiscBusiness().getFeptxn().getFeptxnCbsTxCode()));
                logMessage(Level.DEBUG, this.getLogContext());

                //跨境電子支付(2555)交易
                if (FISCPCode.PCode2555.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {        // 沖正
                    // -CON 沖轉主機帳務
                    String TxType = "2";        //上CBS沖正
                    String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
                    this.getTxData().setObtlTxn(record);
                    ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
                    rc2 = new CBS(hostAA, getTxData()).sendToCBS(TxType);
                }
                this.getLogContext().setProgramName(ProgramName);
                TxHelper.getMessageFromFEPReturnCode(this.getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC, this.getLogContext());
                //由GetMessageFromFEPReturnCode執行 SendEMS
            } else {
                // +CON
                //跨境電子支付退款(2556)交易
                if (FISCPCode.PCode2556.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
                    String TxType = "1";        //上CBS沖正
                    String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
                    this.getTxData().setObtlTxn(record);
                    ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
                    rc = new CBS(hostAA, getTxData()).sendToCBS(TxType);
                }
            }
        }
        //return rtnCode;
    }
    private FEPReturnCode SendToCBS() throws Exception {
        FEPReturnCode rc2 = CommonReturnCode.Normal;
        /*沖轉主機帳務*/
        String AATxTYPE = "";
        String AATxRs = "N";
        String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1();
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
        rc2 = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE,AATxRs);
        return rc2;
    }
    /*
     * 更新交易記錄(FEPTXN) : if need
     */
    private void updateFEPTXN() {
        if (getFiscBusiness().getFeptxn().getFeptxnAaRc() == CommonReturnCode.Normal.getValue()) {
            if (rc != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rc.getValue());
            } else if (rc2 != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rc2.getValue());
            }
        }
        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));
        rc = getFiscBusiness().updateTxData();
    }

    private void sendToMailHunter() {
        try {
            if (NormalRC.FISC_ATM_OK.equals(this.getFiscBusiness().getFeptxn().getFeptxnRepRc())
                    && NormalRC.FISC_ATM_OK.equals(this.getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                switch (getFiscBusiness().getFeptxn().getFeptxnNoticeType()) {
                    case "P":
                        this.getFiscBusiness().preparePush(feptxn);
                        break;    //送推播
                    case "M":
                        this.getFiscBusiness().prepareSms(feptxn);
                        break;    //簡訊
                    case "E":
                        this.getFiscBusiness().prepareMail(feptxn);
                        break;    //Email

                }
            }
        } catch (Exception e) {
            this.getLogContext().setProgramException(e);
            this.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.getLogContext());
        }
    }

    /**
     * 傳送2160電文給財金
     *
     * @return
     */
    public FEPReturnCode insertINBK2160() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            rtnCode = getFiscBusiness().prepareInbk2160();
            if (rtnCode != CommonReturnCode.Normal) {
                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setMessage(rtnCode.toString());
                    getLogContext().setRemark("寫入檔案(INBK2160)發生錯誤!!");
                    logMessage(Level.INFO, getLogContext());
                    return FEPReturnCode.INBK2160InsertError;
                } else {
                    return FEPReturnCode.Normal;
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".AddTXData");
            sendEMS(getLogContext());
        }
        return FEPReturnCode.Normal;
    }
}
