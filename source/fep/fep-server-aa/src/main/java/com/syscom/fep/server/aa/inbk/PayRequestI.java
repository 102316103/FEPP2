package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author Joseph
 */
public class PayRequestI extends INBKAABase {
    private Object tota = null;

    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode3 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode4 = CommonReturnCode.Normal;
    private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
    private boolean isEC = false;
    private boolean isNWD = false;
    private boolean isExitProgram = false;

    /**
     * AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     *                <p>
     *                初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
     *                ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
     *                FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
     * @throws Exception
     */
    public PayRequestI(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     */
    @Override
    public String processRequestData() {
        try {
            //1.拆解並檢核財金電文
            _rtnCode = this.processRequestHeader();

            //2.新增交易記錄
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode2 = addTxData();
                if (_rtnCode2 != CommonReturnCode.Normal) {
                    return StringUtils.EMPTY;
                }
            }

            //3.商業邏輯檢核 & 電文Body檢核
            if (_rtnCode == CommonReturnCode.Normal && _rtnCode2 == CommonReturnCode.Normal) { /*CheckHeader Error*/
                _rtnCode = checkBusinessRule();
            }

            //4.SendToCBS/ASC
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = this.sendToCBSAndAsc();
                if (isExitProgram) {
                    getLogContext().setMessage("sendToCBS error");
                    logMessage(getLogContext());
                    return StringUtils.EMPTY;
                }
            }

            //6.PrepareFISC:準備回財金的相關資料
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = this.prepareForFISC();
            }

            //7.UpdateTxData: 更新交易記錄(FEPTXN & INTLTXN)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = this.updateTxData();
            }

            //8.ProcessAPTOT:更新跨行代收付
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = this.processAPTOT();
            }

            //9.將組好的財金電文送給財金
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
            }

            //10.判斷是否需傳送2160電文給財金
            if(_rtnCode == CommonReturnCode.Normal){
                if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnSend2160())
                        && ("Y".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()) || "A".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()))){
                    _rtnCode =insertINBK2160();
                }
            }
        } catch (Exception e) {
            this._rtnCode = CommonReturnCode.ProgramException;
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(this.logContext);
        } finally {
            this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            this.getTxData().getLogContext().setMessage("FiscResponse:"+this.getFiscRes().getFISCMessage());
            this.getTxData().getLogContext().setProgramName(this.aaName);
            this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.DEBUG, this.logContext);
        }
        // 2011/03/17 modified by Ruling for 若回rtnCode給Handler，FISCGW會將此值回給財金，但此時AA已結束不需在回財金，故改成回空白
        return StringUtils.EMPTY;
    }

    private FEPReturnCode insertINBK2160() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            //檢核Header
            rtnCode = getATMBusiness().prepareInbk2160();
            if(rtnCode != CommonReturnCode.Normal){
                getLogContext().setMessage(rtnCode.toString());
                getLogContext().setRemark("寫入檔案發生錯誤!!");
                logMessage(Level.INFO, getLogContext());
                return FEPReturnCode.INBK2160InsertError;
            }else{
                return FEPReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequest");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }
    /**
     * 拆解並檢核由財金發動的Request電文
     *
     * @return
     */
    private FEPReturnCode processRequestHeader() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        // '檢核財金電文 Header
        rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);

        if (rtnCode != CommonReturnCode.Normal) {  /*FISC RC:Garbled Message*/
            getFiscBusiness().setFeptxn(null);
            getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
            return rtnCode;
        }

        return rtnCode;
    }

    private FEPReturnCode processAPTOT() {
        if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())
                && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            _rtnCode4 = getFiscBusiness().processAptot(isEC);
            if (_rtnCode4 != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
                getFiscBusiness().updateTxData();
            }
        }
        return _rtnCode4;
    }
    /**
     * 新增交易記錄
     *
     * @return
     */
    private FEPReturnCode addTxData() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            //Prepare()交易記錄初始資料
            _rtnCode2 = getFiscBusiness().prepareFEPTXN();
            if (_rtnCode2 != CommonReturnCode.Normal) {
                getLogContext().setMessage(_rtnCode2.toString());
                getLogContext().setRemark("準備FEPTXN有誤!!");
                logMessage(Level.INFO, getLogContext());
                return _rtnCode2;
            }
            //以TRANSACTION 新增交易記錄
            _rtnCode3 = getFiscBusiness().insertFEPTxn();
            if (_rtnCode3 != CommonReturnCode.Normal) {
                transactionManager.rollback(txStatus);
                return _rtnCode3;
            }

            transactionManager.commit(txStatus);
            return rtnCode;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 商業邏輯檢核
     *
     * @return
     */

    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        try {
            //檢核委託單位代號
            if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnBusinessUnit())) {
                // 檢核委託單位代號
                rtnCode = getFiscBusiness().checkNpsunit(getFiscBusiness().getFeptxn());
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            //檢核繳款類別
            if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnPaytype()) && StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnBusinessUnit())) {
                rtnCode = getFiscBusiness().checkPAYTYPE();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            //檢核MAC
//            rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
////            this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
////            logMessage(this.logContext);
////            if (rtnCode != CommonReturnCode.Normal) {
////                return rtnCode;
////            }

            //檢核提款金額及單筆限額
            if (getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
                rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark("超過單筆限額");
                    logMessage(getLogContext());
                    return rtnCode;
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "checkBusinessRule");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     * 6. SendToCBS/ASC(if need): 帳務主機處理
     *
     * <history>
     * <modify>
     * <modifer>Husan </modifer>
     * <time>2010/12/01</time>
     * <reason>修正上主機部分改由參考HostBusiness</reason>
     * <time>2010/12/7</time>
     * <reason>Feptxn_Remark記錄INTLTXN_ACQ_CNTRY</reason>
     * </modify>
     * </history>
     *
     * @return
     */
    private FEPReturnCode sendToCBSAndAsc() {
        // modify 2010/12/01
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            String TxType = "1"; //入扣帳
            //20221028一律使用MSGCTL_TWCBSTXID
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(TxType);
            tota = hostAA.getTota();

            if (rtnCode != FEPReturnCode.Normal) {
                isExitProgram = true;
                if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
                    return CommonReturnCode.Normal;
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); /*Reject-abnormal*/
                    getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/
                    getFiscBusiness().updateTxData();
                    if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
                        getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); /*成功*/
                        if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
                            // 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
                            return IOReturnCode.FEPTXNUpdateError;
                        }
                    }
                }
                return rtnCode;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".SendToCBSAndAsc");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    private FEPReturnCode prepareForFISC() throws Exception {
        if (_rtnCode != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
            }
        } else if (_rtnCode2 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode2, FEPChannel.FISC, getLogContext()));
            }
        } else if (_rtnCode3 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode3.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode3, FEPChannel.FISC, getLogContext()));
            }
        }
//        else {
//            getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
//        }
        _rtnCode4 = getFiscBusiness().prepareHeader("0210");
        if (_rtnCode4 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
        }

        _rtnCode = prepareBody();

        _rtnCode4 = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
        this.logContext.setMessage("after makeBitmap RC:" + _rtnCode4.toString());
        logMessage(this.logContext);
        if (_rtnCode4 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
            getFiscRes().setBitMapConfiguration("0000000000000000");
        }

        _rtnCode=getFiscRes().makeFISCMsg();

        return _rtnCode4;
    }

    private FEPReturnCode updateTxData() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {

                if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
                    getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // Pending
                } else {// (2 way)
                    if (!FISCPCode.PCode2430.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                            && !FISCPCode.PCode2470.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 非國際提款沖銷
                        getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
                    } else {
                        getFiscBusiness().getFeptxn().setFeptxnTxrust("D"); // 已沖銷成功
                    }
                }
                // spec change 20101124
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    if (!FISCPCode.PCode2430.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                            && !FISCPCode.PCode2470.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 非國際提款沖銷
                        isEC = false;
                        getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
                    } else {
                        isEC = true;
                        getFiscBusiness().getFeptxn().setFeptxnClrType((short) 2);
                    }
                }
            } else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) { /*初值:0*/
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R");  /*拒絕-正常*/
                /* 1/30 跨行無卡提款交易失敗, 更新預約檔 */
            }
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); /*FISC Response*/
            getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/

            rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
            if (rtnCode != CommonReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
                transactionManager.rollback(txStatus);
                return rtnCode;
            }
            transactionManager.commit(txStatus);
            return rtnCode;
        } catch (Exception ex) {
            if (!txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
            }
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "updateTxData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }


    /**
     * 組財金電文Body部份
     *
     * @return <modify>
     * <modifier>HusanYin</modifier>
     * <reason>修正Const RC</reason>
     * <date>2010/11/25</date>
     * <reason>connie spec modify</reason>
     * <date>2010/11/29</date>
     * </modify>
     */
    private FEPReturnCode prepareBody() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        String wk_BITMAP = "";

        try {
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
                if (FISCPCode.PCode2564.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                        && getFiscBusiness().getFeptxn().getFeptxnTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
                } else {
                    wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
                }
            } else {  /*-REP*/
                wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
            }
            // 依據wk_BITMAP(判斷是否搬值)
            for (int i = 2; i <= 63; i++) {
                if ("1".equals(wk_BITMAP.substring(i, i + 1))) {
                    switch (i) {
                        case 2: { /* 交易金額 */
                            getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmt().toString());
                            break;
                        }
                        case 5: { /* 代付單位 CD/ATM 代號 */
                            getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(), 8, "0"));
                            break;
                        }
                        case 6: { /* 可用餘額 */
                            getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
                            break;
                        }
                        case 13: {
                            /*TROUT_BKNO for 2531,2568,2569繳稅交易*/
                            //20220927以主機回應回傳
                            getFiscRes().setTroutBkno(getFiscBusiness().getFeptxn().getFeptxnTroutBkno7());
                            break;
                        }
                        case 14: { /* 跨行手續費 */
                            getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
                            break;
                        }
                        case 21: { /* 促銷訊息 */
                            //20220926 改用CBS_TOTA.LUCKY_NO
                            getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnLuckyno());
                            break;
                        }
                        case 37: { /* 帳戶餘額*/
                            getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
                            break;
                        }
                        case 50: { /* 轉入帳號 */
                            getFiscRes().setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
                            break;
                        }
                    }
                }
            }
            //產生 MAC
            RefString refMac = new RefString(getFiscRes().getMAC());
            _rtnCode4 = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
            getFiscRes().setMAC(refMac.get());
            if (_rtnCode4 != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
                getFiscRes().setMAC("00000000");
            }
            return rtnCode;
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".prepareBody"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }
}
