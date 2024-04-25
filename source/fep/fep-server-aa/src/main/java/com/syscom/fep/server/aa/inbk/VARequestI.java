package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.enums.*;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.VacateMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 負責處理財金發動的約定及核驗服務交易Req電文
 *
 * @author Joseph
 */
public class VARequestI extends INBKAABase {
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode3 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode4 = CommonReturnCode.Normal;

    @SuppressWarnings("unused")
    private boolean isIC = false;
    private Vatxn defVATXN = new Vatxn();
    @SuppressWarnings("unused")
    private Intltxn oriINTLTXN = new Intltxn();
    private VatxnMapper dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
    private SysconfExtMapper dbSYSCONF = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);

    private boolean isEC = false;
    private Object tota = null;

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
    public VARequestI(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     *
     * @return Response電文
     */
    @Override
    public String processRequestData() {
        try {
            //1.拆解並檢核財金電文
            _rtnCode = processRequestHeader();

            //2.新增交易記錄
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode2 = addTxData();
                if (_rtnCode2 != CommonReturnCode.Normal) {
                    return _rtnCode2.toString();
                }
            }

            //3.商業邏輯檢核 & 電文Body檢核
            if (_rtnCode == CommonReturnCode.Normal && _rtnCode2 == CommonReturnCode.Normal) { /*CheckHeader Error*/
                _rtnCode = checkBusinessRule();
            }

            //4.SendToCBS
            _rtnCode = sendToCBS();


            //6.組回傳財金Response電文
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareForFISC();
            }

            //7.更新交易記錄(FEPTXN & VATXN)
            if (_rtnCode4 == FEPReturnCode.Normal) {
                _rtnCode = updateTxData();
            }

            //8.ProcessAPTOT:更新跨行代收付
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = processAPTOT();
            }

            //9.SendToFISC送回覆電文到財金
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
            }

            //10.判斷是否需傳送2160電文給財金
            if (_rtnCode == CommonReturnCode.Normal) {
                if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnSend2160())
                        && ("A".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()) || "Y".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()))) {
                    _rtnCode = insertINBK2160();
                }
            }

        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(getLogContext());
        } finally {
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage("FiscResponse:"+getFiscRes().getFISCMessage());
            getTxData().getLogContext().setProgramName(this.aaName);
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.DEBUG, getLogContext());
        }
        return "";
    }

    private FEPReturnCode insertINBK2160() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            rtnCode = getFiscBusiness().prepareInbk2160();
            if (rtnCode != CommonReturnCode.Normal) {
                getLogContext().setMessage(rtnCode.toString());
                getLogContext().setRemark("寫入檔案發生錯誤!!");
                logMessage(Level.INFO, getLogContext());
                return FEPReturnCode.INBK2160InsertError;
            } else {
                return FEPReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequest");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // ''' <summary>
    // ''' 更新跨行代收付
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
    // ''' <modify>
    // ''' <modifier>Ruling</modifier>
    // ''' <date>2018/12/19</date>
    // ''' <reason>原存金融帳戶核驗(類別=10)，新增寫入APTOT</reason>
    // ''' </modify>
    private FEPReturnCode processAPTOT() {
        /* 2018/12/18 原存金融帳戶核驗(類別=10), 新增寫入APTOT */
        if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())
                && defVATXN != null
                && "10".equals(defVATXN.getVatxnCate().trim())
                && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            _rtnCode4 = getFiscBusiness().processAptot(isEC);
            if (_rtnCode4 != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
                getFiscBusiness().updateTxData();
            }
        }
        return _rtnCode4;
    }

    private FEPReturnCode addTxData() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            //Prepare()交易記錄初始資料
            _rtnCode2 = getFiscBusiness().prepareFEPTXN();
            if (_rtnCode2 != CommonReturnCode.Normal) {
                return _rtnCode2;
            }
            if (_rtnCode2 == CommonReturnCode.Normal) {
                RefBase<Vatxn> vatxnRefBase = new RefBase<>(defVATXN);
                _rtnCode2 = getFiscBusiness().prepareVATXN(vatxnRefBase);
                defVATXN = vatxnRefBase.get();
            }
            if (_rtnCode2 != CommonReturnCode.Normal) {
                getLogContext().setRemark("PrepareVATXN-收到財金發動之約定及核驗服務在準備VATXN發生異常");
                logMessage(getLogContext());
                _rtnCode2 = FISCReturnCode.MessageFormatError;
                getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
            }

            //以TRANSACTION 新增交易記錄
            _rtnCode3 = getFiscBusiness().insertFEPTxn();
            if (_rtnCode3 != CommonReturnCode.Normal) {
                transactionManager.rollback(txStatus);
                return _rtnCode3;
            }

            if (dbVATXN.insert(defVATXN) < 1) {
                transactionManager.rollback(txStatus);
                return IOReturnCode.UpdateFail;
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

    // ''' <summary>
    // ''' UpdateTxData部份
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
    private FEPReturnCode updateTxData() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);

            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) { //(3 way)
                    getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); /* Pending*/
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); /* Pending*/
                } else { // '(2 way)
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); /*成功*/
                }
            } else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) { /*初值:0*/
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); /*拒絕-正常*/
            }
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); /*FISC Response*/
            getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/

            rtnCode = getFiscBusiness().updateTxData();
            if (rtnCode != CommonReturnCode.Normal) { // '若更新失敗則不送回應電文，人工處理
                transactionManager.rollback(txStatus);
                return rtnCode;
            }

            // '判斷是否需更新 VATXN
            defVATXN.setVatxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
            defVATXN.setVatxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
            defVATXN.setVatxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
            defVATXN.setVatxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
            /* 2019/02/21 修改, 寫入VATXN */
            //20221019直接用CBS_TOTA回傳內容
            String AEILFRC1 = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_AEILFRC1.getValue());
            String AEILFRC2 = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_AEILFRC2.getValue());
            String OPENACT = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_OPENACT.getValue());
            defVATXN.setVatxnResult(AEILFRC2);
            defVATXN.setVatxnAcresult(AEILFRC1);
            defVATXN.setVatxnAcstat(OPENACT);

            if (dbVATXN.updateByPrimaryKey(defVATXN) < 1) { // '若更新失敗則不送回應電文，人工處理
                transactionManager.rollback(txStatus);
                return IOReturnCode.UpdateFail;
            }

            transactionManager.commit(txStatus);
            return rtnCode;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    // ''' <summary>
    // ''' 組回傳財金Response電文
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
    private FEPReturnCode prepareForFISC() {
        if (_rtnCode != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
            } else {
            }
        } else if (_rtnCode2 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode2, FEPChannel.FISC, getLogContext()));
            } else {
            }
        } else if (_rtnCode3 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode3.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode3, FEPChannel.FISC, getLogContext()));
            } else {
            }
        } else {
            getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
        }
        /* 11/26 修改, 統一發票入帳帳號核驗, 將回給財金錯誤代碼 4501/2999改為 4507 */
        if (FISCPCode.PCode2566.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                && "11".equals(defVATXN.getVatxnCate())) {
            /* 2021/6/28 修改, 統一發票入帳帳號核驗, FEP配合轉碼 */
            if ("4501".equals(getFiscBusiness().getFeptxn().getFeptxnRepRc()) ||
                    getFiscBusiness().getFeptxn().getFeptxnRepRc().equals(AbnormalRC.FISC_Error)) {
                logContext.setRemark("回給財金錯誤代碼由[" + getFiscBusiness().getFeptxn().getFeptxnRepRc() + "]轉換為[4507]");
                logMessage(Level.INFO, logContext);
                getFiscBusiness().getFeptxn().setFeptxnRepRc("4507");
            }
        }

        _rtnCode4 = getFiscBusiness().prepareHeader("0210");
        if (_rtnCode4 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
        }

        _rtnCode4 = prepareBody();

        _rtnCode4 = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
        this.logContext.setMessage("after makeBitmap RC:" + _rtnCode4.toString());
        logMessage(this.logContext);
        if (_rtnCode4 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
            getFiscRes().setBitMapConfiguration("0000000000000000");
        }

        _rtnCode = getFiscRes().makeFISCMsg();

        return _rtnCode;
    }

    // ''' <summary>
    // ''' 組財金電文Body部份
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
    private FEPReturnCode prepareBody() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        String wk_BITMAP = "";

        try {
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
                /*跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組*/
                wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
            } else {  /*-REP*/
                wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
            }
            // 依據wk_BITMAP(判斷是否搬值)
            for (int i = 2; i <= 63; i++) {
                if ("1".equals(wk_BITMAP.substring(i, i + 1))) {
                    switch (i) {
                        case 5: { /* 代付單位 CD/ATM 代號 */
                            getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(), 8, "0"));
                            break;
                        }
                        case 14: { /* 跨行手續費 */
                            getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
                            break;
                        }
                        case 45: { /* 附言欄*/
                            String fromAct = this.getImsPropertiesValue(tota, ImsMethodName.O_FROM_ACT.getValue());
                            String AEILFRC1 = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_AEILFRC1.getValue());
                            String AEILFRC2 = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_AEILFRC2.getValue());
                            String OPENACT = this.getImsPropertiesValue(tota, ImsMethodName.O_LF_OPENACT.getValue());

                            switch (defVATXN.getVatxnCate()) {
                                case "01":
                                    getFiscRes().setMEMO(getFiscReq().getMEMO().substring(0, 65) + fromAct);
                                    break;
                                case "02":
                                    getFiscRes().setMEMO(getFiscReq().getMEMO().substring(0, 65) + fromAct + getFiscReq().getMEMO().substring(81, 91));
                                    break;
                                case "11": // '統一發票中奬入帳帳號檢核
                                    getFiscRes().setMEMO(getFiscReq().getMEMO());
                                    break;
                                case "10":
                                    /* 2023/4/28 修改 for 行動電話異動核驗 */
                                    if (getFiscBusiness().getFeptxn().getFeptxnRepRc().equals("4001") && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnRemark())) {
                                        getFiscRes().setMEMO(getFiscReq().getMEMO().substring(0, 90) + getFiscBusiness().getFeptxn().getFeptxnRemark().substring(0, 10));
                                    } else {
                                        getFiscRes().setMEMO(getFiscReq().getMEMO());
                                    }
                                    break;
                            }
                            break;
                        }
                    }
                }
            }

            // '產生 MAC
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

    // ''' <summary>
    // ''' 6. SendToCBS/ASC(if need): 帳務主機處理
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
    private FEPReturnCode sendToCBS() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
                String TxType = "0";

                this.getTxData().setVatxn(defVATXN);
                String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
//                feptxn.setFeptxnTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
                rtnCode = new CBS(hostAA, getTxData()).sendToCBS(TxType);
                tota = hostAA.getTota();

                if (rtnCode != CommonReturnCode.Normal) {
                    if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
                        return CommonReturnCode.Normal;
                    } else {
                        getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                        getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
                        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
                        getFiscBusiness().updateTxData();
                        return rtnCode;
                    }
                } else {
                    /* 2019/01/28 修改 for 2566 */
                    if ("10".equals(defVATXN.getVatxnCate())) {
                        this.logContext.setRemark("項目核驗結果" + StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnRemark(), 6, " ").substring(0, 2));
                        logMessage(getLogContext());

                        if (VAChkType.TWDNoCard.equals(defVATXN.getVatxnType())
                                || VAChkType.ForeignNoCard.equals(defVATXN.getVatxnType())) {
                            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRemark())
                                    || getFiscBusiness().getFeptxn().getFeptxnRemark().trim().length() < 4) {
                                this.logContext.setRemark("帳戶核驗結果為NULL或空值或欄位長度不足4位");
                                logMessage(getLogContext());
                                _rtnCode = FISCReturnCode.CheckIDNOError;
                            }

                            if (!AcctChkStatus.ChkSuccess.equals(getFiscBusiness().getFeptxn().getFeptxnRemark().substring(2, 4))) {
                                this.logContext.setRemark("帳戶核驗結果為" + getFiscBusiness().getFeptxn().getFeptxnRemark().substring(2, 2));
                                logMessage(getLogContext());
                                _rtnCode = FEPReturnCode.NotICCard;
                            }
                        }
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToCBS"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    // ''' <summary>
    // ''' 商業邏輯檢核
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = CommonReturnCode.ProgramException;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        try {
            Vacate vacate = new Vacate();
            if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnNoticeId())) {
                vacate.setVacateNo(getFiscBusiness().getFeptxn().getFeptxnNoticeId().substring(0, 2));
            }
            VacateMapper vacateMapper = SpringBeanFactoryUtil.getBean(VacateMapper.class);
            if (vacateMapper.selectByPrimaryKey(vacate.getVacateNo()) == null) {
                this.logContext.setRemark("業務類別未上線 VACATE_NO=" + vacate.getVacateNo());
                logMessage(getLogContext());
                return FEPReturnCode.MessageFormatError; /* 0101:訊息格式或內容編輯錯誤*/
            }
            switch (defVATXN.getVatxnType()) {
                // 跨行金融帳戶資訊核驗
                case "10":
                    switch (defVATXN.getVatxnType()) {
                        case VAChkType.ICCard: { // 金融卡核驗
                            if (!(VAChkItem.ACCT.equals(defVATXN.getVatxnItem())
                                    || VAChkItem.ID.equals(defVATXN.getVatxnItem())
                                    || VAChkItem.Mobile.equals(defVATXN.getVatxnItem())
                                    || VAChkItem.Birthday.equals(defVATXN.getVatxnItem())
                                    || VAChkItem.HPhone.equals(defVATXN.getVatxnItem()))) {
                                this.logContext.setRemark("金融卡核驗項目為" + defVATXN.getVatxnItem() + "非[00,01,02,03,04]核驗項目");
                                logMessage(getLogContext());
                                return FEPReturnCode.MessageFormatError;
                            }
                            break;
                        }
                        case VAChkType.TWDNoCard:
                        case VAChkType.ForeignNoCard:
                            //2021/09/11 Modify by Ruling for 跨行金融帳戶資訊核驗增加法人戶：類別10的交易類別01台幣帳戶核驗、02外幣帳戶核驗，增加判斷核驗項目15生效日
                            String item15EffectDate = "";
                            Sysconf defSYSCONF = new Sysconf();
                            defSYSCONF.setSysconfSubsysno((short) 1);
                            defSYSCONF.setSysconfName("VAItem15EffectDate");
                            defSYSCONF = dbSYSCONF.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
                            if (defSYSCONF != null) {
                                item15EffectDate = defSYSCONF.getSysconfValue();
                                logContext.setRemark("跨行金融帳戶資訊核驗(2566)核驗項目15生效日=" + defSYSCONF.getSysconfValue());
                            } else {
                                logContext.setRemark("讀不到跨行金融帳戶資訊核驗(2566)核驗項目15生效日(SYSCONF), SYSCONF_SUBSYSNO=" + defSYSCONF.getSysconfSubsysno() +
                                        " SYSCONF_NAME=" + defSYSCONF.getSysconfName());
                                logMessage(Level.INFO, logContext);
                                sendEMS(logContext);
                                return IOReturnCode.SYSCONFNotFound;
                            }
                            if (StringUtils.isNotBlank(item15EffectDate)) {
                                if (getFiscBusiness().getFeptxn().getFeptxnTxDate().compareTo(item15EffectDate) >= 0) {
                                    //核驗項目15生效日後
                                    if (defVATXN.getVatxnItem().equals(VAChkItem.IDMobile) || defVATXN.getVatxnItem().equals(VAChkItem.IDMobileBirthday)
                                            || defVATXN.getVatxnItem().equals(VAChkItem.IDMobileHPhone)
                                            || defVATXN.getVatxnItem().equals(VAChkItem.IDMobileBirthdayHPhone)
                                            || defVATXN.getVatxnItem().equals(VAChkItem.IDLegalPerson)) {
                                        logContext.setRemark("台幣或外幣帳戶核驗項目為" + defVATXN.getVatxnItem() + "非[11,12,13,14,15]核驗項目");
                                        logMessage(Level.INFO, logContext);
                                        return FISCReturnCode.MessageFormatError;
                                    }
                                }
                            } else {
                                //核驗項目15生效日前，拒?交易
                                if (!(VAChkItem.IDMobile.equals(defVATXN.getVatxnItem())
                                        || VAChkItem.IDMobileBirthday.equals(defVATXN.getVatxnItem())
                                        || VAChkItem.IDMobileHPhone.equals(defVATXN.getVatxnItem())
                                        || VAChkItem.IDMobileBirthdayHPhone.equals(defVATXN.getVatxnItem()))) {
                                    this.logContext.setRemark("台幣或外幣帳戶核驗項目為" + defVATXN.getVatxnItem() + "非[11,12,13,14]核驗項目");
                                    logMessage(getLogContext());
                                    return FEPReturnCode.MessageFormatError;
                                }
                            }
                            break;
                    }
                    break;
                // 統一發票中獎獎金入帳帳號核驗
                case "11":
                case "02": /* 線上約定繳費 */
                    if (StringUtils.isBlank(defVATXN.getVatxnIdno())) {
                        this.logContext.setRemark("問題帳戶  VATXN_IDNO為NULL或空值");
                        logMessage(getLogContext());
                        return FEPReturnCode.CheckIDNOError;
                    } else {
                        getFiscBusiness().getFeptxn().setFeptxnIdno(defVATXN.getVatxnIdno());
                    }
                    break;
                case "01": /* 境內電子支付約定連結申請 */
                    if (!"00".equals(defVATXN.getVatxnType()) && !"01".equals(defVATXN.getVatxnType())
                            && !"03".equals(defVATXN.getVatxnType())) {
                        return FEPReturnCode.MessageFormatError;
                    }
                    if (StringUtils.isBlank(defVATXN.getVatxnIdno())) {
                        this.logContext.setRemark("問題帳戶  VATXN_IDNO為NULL或空值");
                        logMessage(getLogContext());
                        return FEPReturnCode.CheckIDNOError;
                    } else {
                        getFiscBusiness().getFeptxn().setFeptxnIdno(defVATXN.getVatxnIdno());
                    }
                    break;
            }

            // 檢核MAC
            rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
            this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    // ''' <summary>
    // ''' 新增交易記錄
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
    private FEPReturnCode insertFEPTXN() {
        FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            VatxnMapper dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
            rtnCode = getFiscBusiness().insertFEPTxn();
            if (rtnCode != CommonReturnCode.Normal) {
                transactionManager.rollback(txStatus);
                return rtnCode;
            }

            if (dbVATXN.insert(defVATXN) < 1) {
                transactionManager.rollback(txStatus);
                return IOReturnCode.UpdateFail;
            }

            transactionManager.commit(txStatus);
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".addTxData"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    // ''' <summary>
    // ''' 拆解並檢核由財金發動的Request電文
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
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
}
