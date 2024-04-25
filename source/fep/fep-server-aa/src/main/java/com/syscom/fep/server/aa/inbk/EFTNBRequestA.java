package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhaoKai
 */
public class EFTNBRequestA extends INBKAABase{
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    private String rtnMessage = "";
    private boolean needResponseMsg = true;

    public EFTNBRequestA(ATMData txnData) throws Exception {
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

            //4. 帳務主機處理：本行轉入-進帳務主機查詢帳號
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
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) && DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
                _rtnCode = getFiscBusiness().processAptot(false);
            }

            //8. 組回應電文回給 ATM&判斷是否需組 CON 電文回財金/本行轉入交易掛帳
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
        //Dim defALLBANK As New Tables.DefALLBANK
        //Dim dbfALLBANK As New Tables.DBALLBANK(FEPConfig.DBName)

        try {
            //檢核外圍 EJ 是否重覆
            rtnCode = getFiscBusiness().checkChannelEJFNO();
            if (rtnCode != CommonReturnCode.Normal) {
                getLogContext().setRemark(StringUtils.join("檢核外圍 EJ 重覆, FEPTXN_CHANNEL_EJFNO=",getFeptxn().getFeptxnChannelEjfno()));
                logMessage(Level.INFO, getLogContext());
                return rtnCode;
            }

            //判斷是否已換日，若 MODE 不同則回覆錯誤
            if (getFeptxn().getFeptxnAtmod().shortValue() != getFeptxn().getFeptxnTxnmode().shortValue()) {
                getLogContext().setRemark(StringUtils.join("MODE 不同, Req電文的MODE=", getFeptxn().getFeptxnAtmod(), "ZONE檔的MODE=",  getFeptxn().getFeptxnTxnmode()));
                logMessage(Level.INFO, getLogContext());
                rtnCode = FEPReturnCode.FISCBusinessDateChanged;
                return rtnCode;
            }

            //檢核財金及參加單位之系統狀態
            rtnCode = getFiscBusiness().checkINBKStatus(getFeptxn().getFeptxnPcode(), true);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //檢核交易連線狀態
            if (!getATMtxData().isTxStatus()) {
                getLogContext().setRemark(StringUtils.join("檢核交易連線狀態為false, MSGCTL_STATUS=", getATMtxData().isTxStatus()));
                logMessage(Level.INFO, getLogContext());
                rtnCode = CommonReturnCode.InterBankServiceStop;
                return rtnCode;
            }

            //2016/02/05 Modify by Ruling for 繳費網PHASEI:汽燃費要寫FEPTXN_PBTYPE
            //ben20221118  getFeptxn().setFeptxnPbtype(StringUtils.leftPad(getATMRequest().getPTYPE(),2,'0'));

            //ben20221118
            /*
            if (!StringUtils.leftPad(getATMRequest().getPTYPE(),2, '0').equals("04")) {
                //FepTxn.FEPTXN_PBTYPE = ATMRequest.PTYPE.PadLeft(2, "0"c)

                //檢核永豐繳費網客戶檔
                rtnCode = getFiscBusiness().checkBPUNIT();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            } else {
                //2018/07/03 Modify by Ruling for 增加檢核識別碼長度必須為6位
            	//--ben-20220922-//if (getATMRequest().getIDENTITY().trim().length() != 6) {
            	//--ben-20220922-//    getLogContext().setRemark(StringUtils.join("檢核識別碼長度不為6位, 識別碼=", getATMRequest().getIDENTITY()));
            	//--ben-20220922-//    logMessage(Level.INFO, getLogContext());
            	//--ben-20220922-//    rtnCode = ATMReturnCode.OtherCheckError;
            	//--ben-20220922-//    return rtnCode;
            	//--ben-20220922-//}

                //檢核汽燃費
                rtnCode = getFiscBusiness().checkFuelPayType();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
              //--ben-20220922-//getFeptxn().setFeptxnTrinBkno(getATMRequest().getBknoD());
                //2018/07/12 Modify by Ruling 汽燃費繳款期限移至 PrepareFEPTXN 寫入
                //FepTxn.FEPTXN_DUE_DATE = ATMRequest.DUEDATE
              //--ben-20220922-//getFeptxn().setFeptxnRemark(StringUtils.join(getFeptxn().getFeptxnPayno() , StringUtils.rightPad(getFeptxn().getFeptxnReconSeqno().trim(), 16, '0') , getFeptxn().getFeptxnDueDate() , getATMRequest().getIDENTITY()));
            }
			*/
            
            //2018/01/08 Modify by Ruling for 應永豐要求轉出入銀行代號不檢核全國銀行檔(ALLBANK)
            //'檢核轉出/轉入銀行代號
            //defALLBANK.ALLBANK_BKNO = FepTxn.FEPTXN_TROUT_BKNO
            //If dbfALLBANK.QueryALLBANKByBKNO(defALLBANK) <= 0 Then
            //    LogContext.Remark = String.Format("檢核轉出銀行代號不在ALLBANK檔內, ALLBANK_BKNO={0}", FepTxn.FEPTXN_TROUT_BKNO)
            //    LogMessage(LogLevel.Info, LogContext)
            //    rtnCode = RMReturnCode.BankNumberNotFound
            //    Return rtnCode
            //End If

            //defALLBANK.ALLBANK_BKNO = FepTxn.FEPTXN_TRIN_BKNO
            //If dbfALLBANK.QueryALLBANKByBKNO(defALLBANK) <= 0 Then
            //    LogContext.Remark = String.Format("檢核轉入銀行代號不在ALLBANK檔內, ALLBANK_BKNO={0}", FepTxn.FEPTXN_TRIN_BKNO)
            //    LogMessage(LogLevel.Info, LogContext)
            //    rtnCode = RMReturnCode.BankNumberNotFound
            //    Return rtnCode
            //End If

            //2016/12/08 Modify by Ruling for 修正11/28繳費網轉出帳號超過16位，增加檢核轉出帳號及轉入帳號
            //檢核轉出帳號
          //--ben-20220922-//if (getATMRequest().getTXACT().length() > 16) {
          //--ben-20220922-//    getLogContext().setRemark(StringUtils.join("檢核轉出帳號超過16位, TXACT=", getATMRequest().getTXACT()));
          //--ben-20220922-//    logMessage(Level.INFO, getLogContext());
          //--ben-20220922-//     rtnCode = ATMReturnCode.TranOutACTNOError;
          //--ben-20220922-//     return rtnCode;
          //--ben-20220922-// }

            //檢核轉入帳號
          //--ben-20220922-//if (getATMRequest().getActD().length() > 16) {
          //--ben-20220922-//      getLogContext().setRemark(StringUtils.join("檢核轉入帳號超過16位, ACT_D=", getATMRequest().getActD()));
          //--ben-20220922-//      logMessage(Level.INFO, getLogContext());
          //--ben-20220922-//      rtnCode = ATMReturnCode.TranInACTNOError;
          //--ben-20220922-//     return rtnCode;
          //--ben-20220922-// }

            //繳信用卡費檢核轉入帳號
            if ("03".equals(getFeptxn().getFeptxnPbtype())) {
                if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                    getLogContext().setRemark(StringUtils.join("檢核轉入行非本行, FEPTXN_TRIN_BKNO=", getFeptxn().getFeptxnTrinBkno()));
                    logMessage(Level.INFO, getLogContext());
                    rtnCode = RMReturnCode.BankNumberNotFound;
                    return rtnCode;
                }

                rtnCode = getATMBusiness().checkTrinCredit();
                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark("檢核轉入帳號失敗");
                    logMessage(Level.INFO, getLogContext());
                    return rtnCode;
                }

                //2017/03/23 Modify by Ruling for 增加送信用卡主機檢核銷帳編號
                getFeptxn().setFeptxnYymmdd(CalendarUtil.adStringToROCString(getFeptxn().getFeptxnTxDate()).substring(1, 7));
                getFeptxn().setFeptxnReconSeqno(StringUtils.leftPad(getFeptxn().getFeptxnReconSeqno().trim(), 16, '0'));
                rtnCode = getATMBusiness().checkEFTCCard(getFeptxn().getFeptxnReconSeqno());
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            //檢核委託單位代號或繳款類別
            if (StringUtils.isBlank(getFeptxn().getFeptxnBusinessUnit()) || StringUtils.isBlank(getFeptxn().getFeptxnPaytype())) {
                getLogContext().setRemark("委託單位代號或繳款類別為NULL或空白");
                logMessage(Level.INFO, getLogContext());
                rtnCode = FISCReturnCode.NPSNotFound;
                return rtnCode;
            } else {
                //檢核委託單位代號
                rtnCode = getFiscBusiness().checkNpsunit(getFeptxn());
                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark(StringUtils.join("委託單位代號檔找不到資料, FEPTXN_BUSINESS_UNIT=",getFeptxn().getFeptxnBusinessUnit(), "FEPTXN_PAYTYPE=", getFeptxn().getFeptxnPaytype(), "FEPTXN_PAYNO=", getFeptxn().getFeptxnPayno()));
                    logMessage(Level.INFO, getLogContext());
                    return rtnCode;
                }

                //2017/12/27 Modify by Ruling for 增加檢核繳費網委託單位代號是否為永豐帳務代理
                if (!"04".equals(getFeptxn().getFeptxnPbtype()) && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFISCTxData().getNpsunit().getNpsunitBkno().substring(0, 3))) {
                    getLogContext().setRemark(StringUtils.join("繳費網委託單位代號不為永豐帳務代理, NPSUNIT_BKNO=" + getFiscBusiness().getFISCTxData().getNpsunit().getNpsunitBkno().substring(0, 3)));
                    logMessage(Level.INFO, getLogContext());
                    rtnCode = FISCReturnCode.NPSNotFound; //委託單位代號錯誤
                    return rtnCode;
                }
            }

            //檢核單筆限額
            if (getATMtxData().getMsgCtl().getMsgctlCheckLimit().intValue() != 0) {
                rtnCode = getATMBusiness().checkTransLimit(getATMtxData().getMsgCtl());
                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark("檢核單筆限額失敗");
                    logMessage(Level.INFO, getLogContext());
                    return rtnCode;
                }
            }

            //檢核身份證號合理性
            rtnCode = getFiscBusiness().checkIDNO(getFeptxn().getFeptxnIdno());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
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
    private FEPReturnCode sendToCBS() throws Exception {
        T24 hostT24 = new T24(getATMtxData());
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
            //轉入交易進CBS主機查詢
            rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.Accounting.getValue(), false);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
        } else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
            //2016/07/19 Modify by Ruling for 修正送T24主機電文FISC.BKNOSTAN沒帶值的問題
            //先取 STAN 以供主機電文使用
            getFeptxn().setFeptxnStan(getFiscBusiness().getStan());

            //轉出交易進CBS主機扣帳
            rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.Accounting.getValue(), true);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
        }
        return rtnCode;
    }

    /**
     組回應電文回給網銀以及判斷是否需組 CON 電文回財金上/主機沖正

     @return
     判斷欄位是否有值,必須用Not IsNullorEmpty(Item)判斷,之後用Not IsNullorEmpty(Item.Trim)
     */
    private FEPReturnCode sendToConfirm() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        T24 hostT24 = new T24(getATMtxData());
        MsgfileExtMapper dbMSGFILE = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
        List<Msgfile> msgfiles = new ArrayList<>();
        try {
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnPending((short) 2); //解除 PENDING
                    getFeptxn().setFeptxnReplyCode(StringUtils.leftPad("",4,' '));
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); //成功
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
                    //送 Confirm 給財金(FEPReturnCode2<>Normal仍要繼續執行，所以rtnCode沒判斷是否為Normal)
                    rtnCode = getFiscBusiness().sendConfirmToFISC();
                    //轉入交易，進T24主機入帳
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                        rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid1(), T24TxType.Accounting.getValue(), true);
                    }
                } else {
                    getFeptxn().setFeptxnReplyCode(StringUtils.leftPad("",4,' '));
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); //PENDING
                }

                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
                rtnCode = getFiscBusiness().updateTxData();

                prepareATMRep();

            } else {
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc().trim())) {
                    getFeptxn().setFeptxnPending((short) 2); //解除 PENDING
                    if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                        //+REP
                        getLogContext().setProgramName(ProgramName);
                        getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
                        //將_rtnCode轉成前端對應通道，若有WEBATM的通道必須先轉成ATM通道
                        getLogContext().setProgramName(ProgramName);
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext()));
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); //Accept-Reverse
                        //送 Confirm 給財金(FEPReturnCode2<>Normal仍要繼續執行，所以rtnCode沒判斷是否為Normal)
                        rtnCode = getFiscBusiness().sendConfirmToFISC();
                    } else {
                        //-REP
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                        //將FEPTXN_REP_RC轉成前端對應通道，若有WEBATM的通道必須先轉成ATM通道
                        getLogContext().setProgramName(ProgramName);
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getATMtxData().getTxChannel(), getLogContext()));
                    }
                } else {
                    //fepReturnCode <> Normal
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
                    if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
                        //將_rtnCode轉成前端對應通道，若有WEBATM的通道必須先轉成ATM通道
                        getLogContext().setProgramName(ProgramName);
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext()));
                    }
                }

                //Fly 2016/04/27 配合繳費網需求, 於交易失敗時, 回傳錯誤代碼及錯誤說明
                if (StringUtils.isBlank(getLogContext().getResponseMessage())) {
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && !NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                        msgfiles = dbMSGFILE.selectByMsgfileErrorcode(getFeptxn().getFeptxnRepRc());
                    } else {
                        msgfiles = dbMSGFILE.selectByMsgfileErrorcode(String.valueOf(_rtnCode.getValue()));
                    }
                    if (msgfiles.size() > 0) {
                        getLogContext().setResponseMessage(msgfiles.get(0).getMsgfileShortmsg());
                    }
                }

                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
                getFiscBusiness().updateTxData();

                prepareATMRep();

                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno()) && getFeptxn().getFeptxnAccType() == 1) {
                    //已記帳-轉出交易，進T24主機沖正
                    //FEPReturnCode2<>Normal仍要繼續執行，所以rtnCode沒判斷是否為Normal
                    rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.EC.getValue(), true);
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToConfirm");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private FEPReturnCode prepareATMRep() {

        if (StringUtils.isBlank(getATMtxData().getTxResponseMessage()) && needResponseMsg) {
            rtnMessage = prepareATMResponseData();
        } else {
            rtnMessage = getATMtxData().getTxResponseMessage();
        }

        return CommonReturnCode.Normal;
    }

    /** 更新FEPTXN
     @return FEPReturnCode

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

      //--ben-20220922-//if (FEPChannel.NETBANK.toString().equals(getFeptxn().getFeptxnChannel())) {
      //--ben-20220922-//     getFeptxn().setFeptxnChannel(getATMRequest().getCHLCODE());
      //--ben-20220922-// }

        rtnCode = getFiscBusiness().updateTxData();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        return CommonReturnCode.Normal;

    }

}
