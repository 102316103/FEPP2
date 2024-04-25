package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APN;
import com.syscom.fep.vo.text.ims.IMSTextBase;

/**
 * @author vincent
 */
public class TXSelfRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

    public TXSelfRequestA(INBKData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 1. Prepare : 交易記錄初始資料
            rtnCode = getATMBusiness().prepareFEPTXN();

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTXN)
                this.addTxData();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                this.checkBusinessRule();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
                this.sendToCBS();
            }

            FISC fiscBusiness = getFiscBusiness();
            if (rtnCode == FEPReturnCode.Normal) {
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
            }

            boolean repRcEq4001 = true;
            if (rtnCode == FEPReturnCode.Normal) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessage();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. SendToCBS/ASC: 送主機處理帳務
                this.sendToCBS2();
                if (rtnCode != FEPReturnCode.Normal) {
                    if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                        /* 沖回跨行代收付(APTOT) */
                        rtnCode2 = fiscBusiness.processAptot(true);
                    }
                    // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
                }
            }

            // 9. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
            if (!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
                rtnMessage = this.labelEndOfFunc();
            }
            // 10. 判斷是否需組 CON 電文回財金
            this.updateTxData();

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }

        try {
            // 11. 更新交易記錄(FEPTXN)
            this.updateTxData2();

            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage(rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }

        return rtnMessage;
    }

    /**
     * 2. AddTxData: 新增交易記錄( FEPTxn)
     *
     * @return
     * @throws Exception
     */
    private void addTxData() throws Exception {
        try {
            // 新增交易記錄(FEPTxn) Returning FEPReturnCode
            /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".addTxData"));
            int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
            if (insertCount <= 0) { // 新增失敗
                rtnCode = FEPReturnCode.FEPTXNInsertError;
            }
        } catch (Exception ex) { // 新增失敗
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNInsertError;
        }
    }

    /**
     * 3. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {

        /* 檢核 ATM 電文 */
        rtnCode = checkRequestFromATM(getATMtxData());
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
        }

        /* 檢核中文附言欄不得超過14個中文字 */
        String feptxnChrem = feptxn.getFeptxnChrem();
        if (StringUtils.isNotBlank(feptxnChrem)) {
            if (feptxnChrem.length() > 14) {
                rtnCode = FEPReturnCode.OtherCheckError; /* E711:其他類檢核錯誤 */
                return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
            }

            RefString output = new RefString("");
            getFiscBusiness().convertFiscEncode(getFeptxn().getFeptxnChrem(), output); // (UNICODE轉CNS11643)
            if (output.get().length() > 80) { // 長度>80
                rtnCode = FEPReturnCode.OtherCheckError; /* E711:其他類檢核錯誤 */
                getLogContext().setRemark("中文摘要轉成CNS11643超過80位" + output.get());
                logMessage(Level.INFO, getLogContext());
                return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
            }
        }
    }

    /**
     * 4. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        if (StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
            // 轉入方為本行時,先送CBS查詢帳號
            feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
            String AATxTYPE = "0"; // 上CBS查詢、檢核
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
        }
    }

    /**
     * 8. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlCbsFlag())) {
            /* 進主機入扣帳/手續費 */
            String AATxTYPE = "1"; // 上CBS入扣帳
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
        }
    }

    /**
     * 9. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
     *
     * @throws Exception
     */
    private String labelEndOfFunc() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            ATMGeneralRequest atmReq = this.getATMRequest();
            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
            RefString rfs = new RefString();
            String totaToact;
            if (rtnCode != FEPReturnCode.Normal) {
                ATM_FAA_CC1APC atm_faa_cc1apc = new ATM_FAA_CC1APC();
                // 組Header(OUTPUT-1)
                atm_faa_cc1apc.setWSID(atmReq.getWSID());
                atm_faa_cc1apc.setRECFMT("1");
                atm_faa_cc1apc.setMSGCAT("F");
                atm_faa_cc1apc.setMSGTYP("PC"); // - response
                atm_faa_cc1apc.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                atm_faa_cc1apc.setTRANTIME(systemTime.substring(8, 14)); // 系統時間
                atm_faa_cc1apc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                atm_faa_cc1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
                atm_faa_cc1apc.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

                // 組D0(OUTPUT-2)畫面顯示(Display message)
                // 組 D0(004)
                atm_faa_cc1apc.setDATATYPE("D0");
                atm_faa_cc1apc.setDATALEN("004");
                atm_faa_cc1apc.setACKNOW("0");
                // 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
                String pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
                        getTxData().getLogContext());
                // 其他未列入的代碼，一律回 226
                if (StringUtils.isBlank(pageNo)) {
                    pageNo = "226"; // 交易不能處理
                }
                atm_faa_cc1apc.setPAGENO(pageNo);

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                atm_faa_cc1apc.setPTYPE("S0");
                atm_faa_cc1apc.setPLEN("189");
                atm_faa_cc1apc.setPBMPNO("010000"); // FPC
                // 西元年轉民國年
                atm_faa_cc1apc.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
                atm_faa_cc1apc.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
                // 端末機代號欄位=>繳稅交易放：轉出行-轉出分行財稅代號
                String feptxnTroutBkno7 = feptxn.getFeptxnTroutBkno7();
                atm_faa_cc1apc.setPTID(StringUtils.substring(feptxnTroutBkno7, 0, 3) + "-"
                        + StringUtils.substring(feptxnTroutBkno7, 3, 7));
                // 格式 :$$$,$$$,$$9 ex :$10,000
                atm_faa_cc1apc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
                // 格式:$999 ex :$0
                atm_faa_cc1apc.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
                // 帳戶餘額欄位=>繳稅交易放：繳稅項目
                String feptxnTxCode = feptxn.getFeptxnTxCode();
                String feptxnPaytype = feptxn.getFeptxnPaytype();
                if ("T5".equals(feptxnTxCode)) { // 15類自繳稅
                    // 資料格式=> "TYPE:"+繳費類別
                    atm_faa_cc1apc.setPBAL("TYPE:" + feptxnPaytype);
                } else if ("T6".equals(feptxnTxCode)) { // 非15類，核定稅
                    // 資料格式=> "TYPE:"+繳費類別+"-"+繳款期限
                    atm_faa_cc1apc.setPBAL("TYPE:" + feptxnPaytype + "-" + feptxn.getFeptxnDueDate());
                }
                // 轉入/存入欄位=>繳稅交易放：繳稅資料
                if ("T5".equals(feptxnTxCode)) { // 15類自繳稅
                    // 稽徵機關+ID
                    atm_faa_cc1apc.setPTRINACCT(feptxn.getFeptxnTaxUnit() + feptxn.getFeptxnIdno());
                } else if ("T6".equals(feptxnTxCode)) { // 非15類，核定稅
                    // 銷帳編號
                    atm_faa_cc1apc.setPTRINACCT(feptxn.getFeptxnReconSeqno());
                }
                atm_faa_cc1apc.setPATXBKNO(feptxn.getFeptxnBkno());
                atm_faa_cc1apc.setPSTAN(feptxn.getFeptxnStan());
                // 主機回應代碼
                // ATM回應代碼(空白放 "000") // [20221216]
                atm_faa_cc1apc.setPRCCODE(feptxn.getFeptxnCbsRc());
                // 轉出行
                atm_faa_cc1apc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                // 轉入行=>繳稅交易放 "TAX"
                atm_faa_cc1apc.setPITXBKNO("TAX");
                // 處理有收到CBS Response的欄位值
                if (tota != null) {
                    //交易種類
                    atm_faa_cc1apc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                    //轉出帳號(明細表顯示內容)
                    atm_faa_cc1apc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                    atm_faa_cc1apc.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
                }
                rfs.set("");
                rtnMessage = atm_faa_cc1apc.makeMessage();
                rtnCode = atmEncHelper.makeAtmMac(rtnMessage, rfs);
                if (rtnCode != FEPReturnCode.Normal) {
                    atm_faa_cc1apc.setMACCODE(""); /* 訊息押碼 */
                } else {
                    atm_faa_cc1apc.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                rtnMessage = atm_faa_cc1apc.makeMessage();
            } else {
                ATM_FAA_CC1APN atm_faa_cc1apn = new ATM_FAA_CC1APN();
                // 組Header(OUTPUT-1)
                atm_faa_cc1apn.setWSID(atmReq.getWSID());
                atm_faa_cc1apn.setRECFMT("1");
                atm_faa_cc1apn.setMSGCAT("F");
                atm_faa_cc1apn.setMSGTYP("PN"); // + response
                atm_faa_cc1apn.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                atm_faa_cc1apn.setTRANTIME(systemTime.substring(8, 14)); // 系統時間
                atm_faa_cc1apn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                atm_faa_cc1apn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
                atm_faa_cc1apn.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

                // 組D0(OUTPUT-2)畫面顯示(Display message)
                // 組 D0(022)
                atm_faa_cc1apn.setDATATYPE("D0");
                atm_faa_cc1apn.setDATALEN("022");
                atm_faa_cc1apn.setACKNOW("0");
                BigDecimal ACTBALANCE = feptxn.getFeptxnBalb();
                // 格式 :正值放$,負值放-,$99,999,999,999.00(共18位) // 已確認是單純只總長度，後面轉字串會補滿18位，這裡不用處理
                // ex:$99,355,329;-33,123.00
                if (ACTBALANCE.compareTo(BigDecimal.ZERO) >= 0) {
                    atm_faa_cc1apn.setBALANCE(FormatUtil.decimalFormat(ACTBALANCE, "$#,##0"));
                } else {
                    atm_faa_cc1apn.setBALANCE(FormatUtil.decimalFormat(ACTBALANCE, "-#,##0.00"));
                }

                // 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
                String pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
                        getTxData().getLogContext());
                // 其他未列入的代碼，一律回 226
                if (StringUtils.isBlank(pageNo)) {
                    pageNo = "226"; // 交易不能處理
                }
                atm_faa_cc1apn.setPAGENO(pageNo);

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                atm_faa_cc1apn.setPTYPE("S0");
                atm_faa_cc1apn.setPLEN("189");
                atm_faa_cc1apn.setPBMPNO("000010"); // FPN
                // 西元年轉民國年
                atm_faa_cc1apn.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
                atm_faa_cc1apn.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
                // 端末機代號欄位=>繳稅交易放：轉出行-轉出分行財稅代號
                String feptxnTroutBkno7 = feptxn.getFeptxnTroutBkno7();
                atm_faa_cc1apn.setPTID(StringUtils.substring(feptxnTroutBkno7, 0, 3) + "-"
                        + StringUtils.substring(feptxnTroutBkno7, 3, 7));
                // 格式 :$$$,$$$,$$9 ex :$10,000
                atm_faa_cc1apn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
                // 格式:$999 ex :$0
                atm_faa_cc1apn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
                // 帳戶餘額欄位=>繳稅交易放：繳稅項目
                String feptxnTxCode = feptxn.getFeptxnTxCode();
                String feptxnPaytype = feptxn.getFeptxnPaytype();
                if ("T5".equals(feptxnTxCode)) { // 15類自繳稅
                    // 資料格式=> "TYPE:"+繳費類別
                    atm_faa_cc1apn.setPBAL("TYPE:" + feptxnPaytype);
                } else if ("T6".equals(feptxnTxCode)) { // 非15類，核定稅
                    // 資料格式=> "TYPE:"+繳費類別+"-"+繳款期限
                    atm_faa_cc1apn.setPBAL("TYPE:" + feptxnPaytype + "-" + feptxn.getFeptxnDueDate());
                }
                // 轉入/存入欄位=>繳稅交易放：繳稅資料
                if ("T5".equals(feptxnTxCode)) { // 15類自繳稅
                    // 稽徵機關+ID
                    atm_faa_cc1apn.setPTRINACCT(feptxn.getFeptxnTaxUnit() + feptxn.getFeptxnIdno());
                } else if ("T6".equals(feptxnTxCode)) { // 非15類，核定稅
                    // 銷帳編號
                    atm_faa_cc1apn.setPTRINACCT(feptxn.getFeptxnReconSeqno());
                }
                atm_faa_cc1apn.setPATXBKNO(feptxn.getFeptxnBkno());
                atm_faa_cc1apn.setPSTAN(feptxn.getFeptxnStan());
                // 主機回應代碼
                // ATM回應代碼(空白放 "000") // [20221216]
                atm_faa_cc1apn.setPRCCODE(feptxn.getFeptxnCbsRc());
                // 轉出行
                atm_faa_cc1apn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
                // 轉入行=>繳稅交易放 "TAX"
                atm_faa_cc1apn.setPITXBKNO("TAX");
                // 處理有收到CBS Response的欄位值
                if (tota != null) {
                    //交易種類
                    atm_faa_cc1apn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                    //轉出帳號(明細表顯示內容)
                    atm_faa_cc1apn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                    atm_faa_cc1apn.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
                }
                rfs.set("");
                rtnMessage = atm_faa_cc1apn.makeMessage();
                rtnCode = atmEncHelper.makeAtmMac(rtnMessage, rfs);
                if (rtnCode != FEPReturnCode.Normal) {
                    atm_faa_cc1apn.setMACCODE(""); /* 訊息押碼 */
                } else {
                    atm_faa_cc1apn.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                rtnMessage = atm_faa_cc1apn.makeMessage();
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 10. 判斷是否需組 CON 電文回財金
     */
    private void updateTxData() {
        String feptxnRepRc = feptxn.getFeptxnRepRc();
        if ((rtnCode == FEPReturnCode.Normal) && "4001".equals(feptxnRepRc)) {
            feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
            feptxn.setFeptxnReplyCode("    "); // 4個SPACES
            feptxn.setFeptxnTxrust("B"); /* 成功 */
            feptxn.setFeptxnConRc("4001"); /* +CON */
            rtnCode2 = getFiscBusiness().sendConfirmToFISC();
        } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
            if ("4001".equals(feptxnRepRc)) { /* +REP */
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                        FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
                feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
                if (!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
                    feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                            FEPChannel.FEP, FEPChannel.FISC, getTxData().getLogContext()));
                    rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                }
            } else { /* -REP */
                feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
                        getTxData().getTxChannel(), getTxData().getLogContext()));
            }
        } else {
            /* FEPReturnCode <> Normal */
            /* 2020/3/6 修改，主機有回應錯誤時，修改交易結果 */
            if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc())){
                feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            }else{
                feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
            }

            if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                        FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
            }
        }
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
        this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
    }

    /**
     * 11. 更新交易記錄(FEPTXN)
     */
    private void updateTxData2() {
        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
        } else {
            feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
        this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
    }

    /**
     * 更新feptxn
     *
     * @return
     */
    private void updateFeptxn() {
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFeptxn"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
    }
}
