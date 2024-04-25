package com.syscom.fep.server.aa.atmp;

import java.util.Calendar;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B1PN;

/**
 * @author vincent
 */
public class TRSelfRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

    public TRSelfRequestA(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 記錄FEPLOG內容
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage()));
            this.logContext.setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
            logMessage(this.logContext);
    		
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

            if (rtnCode == FEPReturnCode.Normal) {
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());
            }

            boolean repRcEq4001 = true;
            if (rtnCode == FEPReturnCode.Normal) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = getFiscBusiness().checkResponseMessage();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }


            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 8. SendToCBS/ASC: 送主機處理帳務
                this.sendToCBS2();
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
            getTxData().getLogContext().setMessage("MessageToATM:" + rtnMessage);
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

        /* 檢核財金及參加單位之系統狀態 */
        rtnCode = getFiscBusiness().checkINBKStatus(feptxn.getFeptxnPcode(), true);
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
        String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
        if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
            rtnCode = FEPReturnCode.ENCCheckMACError; /* MAC Error */
            return; // GO TO 5 /* 更新交易記錄 */
        }
       
        String ATMMAC = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(getATMtxData().getTxRequestMessage(), 742, 758));// 轉 ASCII
		this.logContext.setMessage("Begin checkAtmMac mac:" + ATMMAC);
		logMessage(this.logContext);
		
        rtnCode = new ENCHelper(getTxData()).checkAtmMac(feptxn.getFeptxnAtmno(), StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 742), ATMMAC);
        this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(this.logContext);
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
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
            rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
        }
    }

    /**
     * 8. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        if (getTxData().getMsgCtl().getMsgctlCbsFlag() == 1) {
            /* 進主機入扣帳/手續費 */
            String AATxTYPE = "1"; // 上CBS入扣帳
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
            rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
            if(rtnCode == FEPReturnCode.Normal){
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    /* 沖回跨行代收付(APTOT) */
                    rtnCode = getFiscBusiness().processAptot(true);
                }
            }
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
            if (rtnCode != FEPReturnCode.Normal) {
                ATM_FAA_CC1APC atm_faa_cc1APC = new ATM_FAA_CC1APC();
                atm_faa_cc1APC.setWSID(atmReq.getWSID());
                atm_faa_cc1APC.setRECFMT("1");
                atm_faa_cc1APC.setMSGCAT("F");
                atm_faa_cc1APC.setMSGTYP("PC"); // - response
                atm_faa_cc1APC.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                atm_faa_cc1APC.setTRANTIME(systemTime.substring(8, 14)); // 系統時間
                atm_faa_cc1APC.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                atm_faa_cc1APC.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
                atm_faa_cc1APC.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”
                // 組D0(OUTPUT-2)畫面顯示(Display message)
                atm_faa_cc1APC.setDATATYPE("D0");
                atm_faa_cc1APC.setDATALEN("004");
                atm_faa_cc1APC.setACKNOW("0");
                // 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
                String pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
                        getTxData().getLogContext());
                // 其他未列入的代碼，一律回 226
                if (StringUtils.isBlank(pageNo)) {
                    pageNo = "226"; // 交易不能處理
                }
                atm_faa_cc1APC.setPAGENO(pageNo);
                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                atm_faa_cc1APC.setPTYPE("S0");
                atm_faa_cc1APC.setPLEN("191");
                atm_faa_cc1APC.setPBMPNO("010000"); // FPC
                // 西元年轉民國年
                atm_faa_cc1APC.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
                atm_faa_cc1APC.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
                atm_faa_cc1APC.setPTID(feptxn.getFeptxnAtmno());
                // 格式 :$$$,$$$,$$9 ex :$10,000
                atm_faa_cc1APC.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
                // 格式:$999 ex :$0
                atm_faa_cc1APC.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
                atm_faa_cc1APC.setPATXBKNO(feptxn.getFeptxnBkno());
                atm_faa_cc1APC.setPSTAN(feptxn.getFeptxnStan());
                // CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後碼,ex :11/06/29)
                atm_faa_cc1APC.setPBUSINESSDATE(this.dateStrToYYMMDD(feptxn.getFeptxnTbsdy()));
                // 轉入行
                atm_faa_cc1APC.setPITXBKNO(feptxn.getFeptxnTrinBkno());
                atm_faa_cc1APC.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                        getTxData().getLogContext()));
                // 轉出行
                atm_faa_cc1APC.setPOTXBKNO(feptxn.getFeptxnTroutBkno());

                // 處理有收到CBS Response的欄位值
                if (tota != null) {
                    //交易種類
                    atm_faa_cc1APC.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                    //轉出帳號(明細表顯示內容)
                    atm_faa_cc1APC.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                    //轉入帳號(明細表顯示內容)
                    atm_faa_cc1APC.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
                    //促銷應用訊息
                    atm_faa_cc1APC.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
                }
                rfs.set("");
                rtnMessage = atm_faa_cc1APC.makeMessage();
                rtnCode = atmEncHelper.makeAtmMac(feptxn.getFeptxnAtmno(), rtnMessage, rfs);
                if (rtnCode != FEPReturnCode.Normal) {
                    atm_faa_cc1APC.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                            getTxData().getLogContext()));
                    atm_faa_cc1APC.setMACCODE(""); /* 訊息押碼 */
                } else {
                    atm_faa_cc1APC.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
                rtnMessage = atm_faa_cc1APC.makeMessage();
            } else {
                ATM_FAA_CC1B1PN atm_faa_cc1B1PN = new ATM_FAA_CC1B1PN();

                atm_faa_cc1B1PN.setWSID(atmReq.getWSID());
                atm_faa_cc1B1PN.setRECFMT("1");
                atm_faa_cc1B1PN.setMSGCAT("F");
                atm_faa_cc1B1PN.setMSGTYP("PN"); // + response
                atm_faa_cc1B1PN.setMSGTYP("PC"); // - response
                atm_faa_cc1B1PN.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                atm_faa_cc1B1PN.setTRANTIME(systemTime.substring(8, 14)); // 系統時間
                atm_faa_cc1B1PN.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                atm_faa_cc1B1PN.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
                atm_faa_cc1B1PN.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”
                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                atm_faa_cc1B1PN.setPTYPE("S0");
                atm_faa_cc1B1PN.setPLEN("191");
                atm_faa_cc1B1PN.setPBMPNO("000010"); // FPN
                // 西元年轉民國年
                atm_faa_cc1B1PN.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
                atm_faa_cc1B1PN.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
                atm_faa_cc1B1PN.setPTID(feptxn.getFeptxnAtmno());
                // 格式 :$$$,$$$,$$9 ex :$10,000
                atm_faa_cc1B1PN.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
                // 格式:$999 ex :$0
                atm_faa_cc1B1PN.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
                atm_faa_cc1B1PN.setPATXBKNO(feptxn.getFeptxnBkno());
                atm_faa_cc1B1PN.setPSTAN(feptxn.getFeptxnStan());
                // CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後碼,ex :11/06/29)
                atm_faa_cc1B1PN.setPBUSINESSDATE(this.dateStrToYYMMDD(feptxn.getFeptxnTbsdy()));
                // 轉入行
                atm_faa_cc1B1PN.setPITXBKNO(feptxn.getFeptxnTrinBkno());
                atm_faa_cc1B1PN.setPRCCODE(feptxn.getFeptxnCbsRc());
                // 轉出行
                atm_faa_cc1B1PN.setPOTXBKNO(feptxn.getFeptxnTroutBkno());

                // 處理有收到CBS Response的欄位值
                if (tota != null) {
                    //交易種類
                    atm_faa_cc1B1PN.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                    //轉出帳號(明細表顯示內容)
                    atm_faa_cc1B1PN.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                    //轉入帳號(明細表顯示內容)
                    atm_faa_cc1B1PN.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
                    //促銷應用訊息
                    atm_faa_cc1B1PN.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
                }
                rfs.set("");
                rtnMessage = atm_faa_cc1B1PN.makeMessage();
                rtnCode = atmEncHelper.makeAtmMac(feptxn.getFeptxnAtmno(), rtnMessage, rfs);
                if (rtnCode != FEPReturnCode.Normal) {
                    atm_faa_cc1B1PN.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                            getTxData().getLogContext()));
                    atm_faa_cc1B1PN.setMACCODE(""); /* 訊息押碼 */
                } else {
                    atm_faa_cc1B1PN.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
                rtnMessage = atm_faa_cc1B1PN.makeMessage();

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
            // 組回應電文回給 ATM, 寫入 ATM Response Queue // TODO 等方法完成
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
            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc())) {
                feptxn.setFeptxnTxrust("R"); /* Reject-normal */
            } else {
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

    /**
     * 轉民國年,格式化為YY/MM/DD,年度取後碼,ex :11/06/29
     *
     * @param dateStr
     * @return
     */
    private String dateStrToYYMMDD(String dateStr) {
        String rtnDate;
        if ("00000000".equals(dateStr) || dateStr.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            rtnDate = "00/00/00";
        } else {
            dateStr = CalendarUtil.adStringToROCString(dateStr);
            int dateStrLength = dateStr.length();
            dateStr = dateStr.substring(dateStrLength - 6, dateStrLength).replaceAll("(.{2})", "$1/");
            rtnDate = dateStr.substring(0, dateStr.length() - 1);
        }
        return rtnDate;
    }
}
