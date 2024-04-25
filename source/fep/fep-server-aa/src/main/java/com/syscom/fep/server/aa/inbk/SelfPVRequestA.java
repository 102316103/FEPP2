package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.ATMAdapter;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import static com.syscom.fep.vo.constant.NormalRC.FISC_ATM_OK;

public class SelfPVRequestA extends INBKAABase{

    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    private String rtnMessage = "";
    private boolean needResponseMsg = true;
    private String ivrRC = ""; //IVR驗密結果

    public SelfPVRequestA(ATMData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() throws Exception {
        try {
            //1. 準備FEP交易記錄檔
            _rtnCode = getATMBusiness().prepareFEPTXN();

            //2. 新增FEP交易記錄檔
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getATMBusiness().addTXData();
            }

            //3. 商業邏輯檢核(ATM電文)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = checkBusinessRule();
            }

            //4. 帳務主機處理
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = sendToCBS();
            }

            //5. 組送 FISC 之 Request 電文並等待財金之 Response 電文
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());
            }

            //6. 檢核 FISC 之 Response電文是否正確
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().checkResponseMessage();
            }

            //7. ProcessAPTOT:更新跨行代收付
            if (_rtnCode == CommonReturnCode.Normal && FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                _rtnCode = processAPTOT();
            }

            //8. 組回應電文回給 ATM &判斷是否需組 CON 電文回財金/上主機沖正
            _rtnCode2 = sendToConfirm();

        } catch (Exception ex) {
            rtnMessage = "";
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
        } finally {

            //9. 更新交易記錄(FEPTXN)
            updateFEPTXN();

            //10. 發送簡訊及EMAIL
            sendToMailSMS();

            getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getATMtxData().getLogContext().setMessage(rtnMessage);
            getATMtxData().getLogContext().setProgramName(this.aaName);
            getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
            getLogContext().setProgramName(this.aaName);
            logMessage(Level.DEBUG, getLogContext());
        }

        //先組回應ATM 故最後return空字串
        return rtnMessage;

    }

    /**
     商業邏輯檢核

     @return FEPReturnCode

     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            //3.1 檢核 ATM 電文 Header
            rtnCode = checkRequestFromATM(getATMtxData());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //3.2 檢核掌靜脈驗證資料(PIQ)
            if (StringUtils.isBlank(getFeptxn().getFeptxnIdno()) || getFeptxn().getFeptxnIdno().length() != 11) {
                getLogContext().setRemark("身份証號為Null或空白或長度不足11位");
                logMessage(Level.INFO, getLogContext());
                return FEPReturnCode.CheckIDNOError;
            }
            rtnCode = getATMBusiness().checkPVData();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //3.3 掌靜脈交易, 送IVR主機驗證理財密碼
            if (StringUtils.isNotBlank(getATMtxData().getMsgCtl().getMsgctlTxtype2().toString())) {
                if (StringUtils.isBlank(getFeptxn().getFeptxnPinblock())) {
                    getLogContext().setRemark("PINBLOCK為Null或空白");
                    logMessage(Level.INFO, getLogContext());
                    return ATMReturnCode.OtherCheckError;
                }
                rtnCode = getATMBusiness().sendToIVR();
                getFeptxn().setFeptxnAscRc(getFeptxn().getFeptxnCbsRc());
                if (NormalRC.FEP_OK.equals(getFeptxn().getFeptxnAscRc())) {
                    ivrRC = "S";
                } else {
                    ivrRC = "F";
                }
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     帳務主機處理：本行轉入-進帳務主機查詢帳號

     @return

     */
    private FEPReturnCode sendToCBS() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        T24 hostT24 = new T24(getATMtxData());
        try {
            //先取 STAN 以供主機電文使用
            getFeptxn().setFeptxnStan(getFiscBusiness().getStan());

            //送T24主機
            rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), (byte)T24TxType.Accounting.getValue(), true);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToCBS");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     更新跨行代收付

     @return

     */
    private FEPReturnCode processAPTOT() {
        FEPReturnCode rtncode = CommonReturnCode.Normal;
        if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
            rtncode = getFiscBusiness().processAptot(false);
            return rtncode;
        }
        return rtncode;
    }

    /**
     組回應電文回給 ATM以及 判斷是否需組 CON 電文回財金上/主機沖正

     @return
     判斷欄位是否有值,必須用Not IsNullorEmpty(Item)判斷,之後用Not IsNullorEmpty(Item.Trim)
     */
    private FEPReturnCode sendToConfirm() {
        T24 hostT24 = new T24(getATMtxData());
        try {
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                //交易成功
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnPending((short) 2); //解除 PENDING
                    getFeptxn().setFeptxnReplyCode("    ");
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); //成功
                    //轉帳交易直接送 Confirm 給財金(FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal)
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
                    _rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                } else {
                    getFeptxn().setFeptxnReplyCode("    ");
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); //PENDING
                }

                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
                getFiscBusiness().updateTxData();

                //送回應電文回給 ATM
                sendToATM();

                //寫入 ATM 清算資料
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
                    _rtnCode2 = getATMBusiness().insertATMC(1);
                }

                //更新 ATM 鈔匣資料(含ATMCASH/ATMSTAT)
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
                    _rtnCode2 = getATMBusiness().updateATMCash(1);
                }
            } else {
                //交易失敗
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
                    //有送財金, 財金有回錯誤的RC
                    getFeptxn().setFeptxnPending((short) 2); //解除 PENDING
                    if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                        //+REP (有收到財金+REP回應電文, 但檢核回應電文的內容有誤, 回-con)
                        getLogContext().setProgramName(ProgramName);
                        getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
                        getLogContext().setProgramName(ProgramName);
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext())); //將ReturnCode轉成前端對應通道
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); //Accept-Reverse

                        _rtnCode2 = getFiscBusiness().sendConfirmToFISC(); //FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
                    } else {
                        //-REP (有收到財金-REP回應電文)
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                        getLogContext().setProgramName(ProgramName);
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getATMtxData().getTxChannel(), getLogContext())); //將ReturnCode轉成前端對應通道
                    }
                } else {//fepReturnCode <> Normal
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); //R
                    } else {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); //S
                    }

                    if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
                        getLogContext().setProgramName(ProgramName);
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext())); //將ReturnCode轉成前端對應通道
                    }
                }

                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
                getFiscBusiness().updateTxData();

                //送回應電文回給 ATM
                sendToATM();

                if (getFeptxn().getFeptxnAccType() == 1) {//已記帳
                    //CBS主機沖正
                    _rtnCode2 = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), (byte) T24TxType.EC.getValue(), true); //FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
                }
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToConfirm");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     組回應電文

     @return

     */
    private FEPReturnCode sendToATM() {
        ATMAdapter oatmAdapter = new ATMAdapter(getATMtxData());
        FEPReturnCode rtncode = CommonReturnCode.Normal;
        try {
            //先送給ATM主機
        	//--ben-20220922-//oatmAdapter.setAtmNo(getATMRequest().getBRNO() + getATMRequest().getWSNO());
            if (StringUtils.isBlank(getATMtxData().getTxResponseMessage()) && needResponseMsg) {
                if (ATMTXCD.PFT.toString().equals(getFeptxn().getFeptxnTxCode())) {
                	//ben20221118  getATMResponse().setFpwChk(ivrRC);
                }
                rtnMessage = prepareATMResponseData();
            } else {
                rtnMessage = getATMtxData().getTxResponseMessage();
            }

            if (StringUtils.isNotBlank(rtnMessage)) {
                oatmAdapter.setMessageToATM(rtnMessage);
                rtncode = oatmAdapter.sendReceive();
            } else {
                //若需要回ATM電文，但rtnMessage是空的表示有問題需alert
                getLogContext().setRemark("ATM組出來的回應電文為空字串");
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.INFO, getLogContext());
            }

            //因為先送給ATM了所以要將回應字串清成空字串
            rtnMessage = "";

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToATM");
            sendEMS(getLogContext());
            rtncode = CommonReturnCode.ProgramException;
        }

        return rtncode;
    }

    /**
     更新FEPTXN

     @return

     */
    private FEPReturnCode updateFEPTXN() {
        FEPReturnCode rtnCode = null;

        if (_rtnCode != CommonReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        } else if (_rtnCode2 != CommonReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
        } else {
            getFeptxn().setFeptxnAaRc(CommonReturnCode.Normal.getValue());
        }

        getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

        rtnCode = getFiscBusiness().updateTxData();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        return CommonReturnCode.Normal;
    }

    /**
     發送簡訊及EMAIL


     */
    private void sendToMailSMS() {
        try {
            //本行客戶ATM單筆轉帳達三萬元, 發簡訊及EMAIL
            if (FISC_ATM_OK.equals(getATMBusiness().getFeptxn().getFeptxnRepRc()) && FISC_ATM_OK.equals(getATMBusiness().getFeptxn().getFeptxnConRc())) {
                if (ATMTXCD.PFT.toString().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()) && "FT".equals(getATMBusiness().getFeptxn().getFeptxnRsCode())) {
                    getATMBusiness().prepareATMFTEMAIL();
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToMailSMS");
            sendEMS(getLogContext());
        }
    }
}
