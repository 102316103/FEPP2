package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.AllbankMapper;
import com.syscom.fep.mybatis.mapper.BsdaysMapper;
import com.syscom.fep.mybatis.mapper.HotacqcntryMapper;
import com.syscom.fep.mybatis.mapper.IntltxnMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.mapper.QrptxnMapper;
import com.syscom.fep.mybatis.mapper.SysconfMapper;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Hotacqcntry;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.mybatis.model.Qrptxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.App;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.Dapp;
import com.syscom.fep.server.common.business.host.Ncnb;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.CBSHostType;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.FISCType;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.T24Version;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.ATMNCCardStatus;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.enums.DAPPAppMsg;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * @author Richard
 */
public class IFTRequestI extends INBKAABase {
    private Object tota = null;

    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode3 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode4 = CommonReturnCode.Normal;
    private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
    private boolean isEC = false;
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
    public IFTRequestI(FISCData txnData) throws Exception {
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
            if (isExitProgram) {
                return StringUtils.EMPTY;
            }

            //2.新增交易記錄
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode2 = addTxData();
                if (_rtnCode2 != CommonReturnCode.Normal) {
                    getLogContext().setMessage(_rtnCode2.toString());
                    getLogContext().setRemark("新增交易記錄有誤!!");
                    logMessage(Level.INFO, getLogContext());
                    return StringUtils.EMPTY;
                }
            }

            //3.商業邏輯檢核 & 電文Body檢核
            if (_rtnCode == CommonReturnCode.Normal && _rtnCode2 == CommonReturnCode.Normal) { /*CheckHeader Error*/
                strFISCRc = checkBusinessRule();
            }

            //4.SendToCBS
            if (strFISCRc == CommonReturnCode.Normal) {
                _rtnCode = sendToCBSAndAsc();
                if (isExitProgram) {
                    getLogContext().setMessage("sendToCBS error");
                    logMessage(getLogContext());
                    return StringUtils.EMPTY;
                }
            }

            //6.PrepareFISC:準備回財金的相關資料
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareForFISC();
            }

            //7.UpdateTxData: 更新交易記錄(FEPTXN & INTLTXN)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = updateTxData();
            }

            //8.ProcessAPTOT:更新跨行代收付
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = processAPTOT();
            }

            //9.將組好的財金電文送給財金
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
            }

            //10.判斷是否需傳送2160電文給財金
            if (_rtnCode == CommonReturnCode.Normal) {
                if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnSend2160())
                        && ("Y".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()) ||
                        "A".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()))) {
                    _rtnCode = insertINBK2160();
                }
            }
        } catch (Exception e) {
            this._rtnCode = CommonReturnCode.ProgramException;
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(this.logContext);
        } finally {
            this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            this.getTxData().getLogContext().setMessage("FiscResponse:" + this.getFiscRes().getFISCMessage());
            this.getTxData().getLogContext().setProgramName(this.aaName);
            this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.DEBUG, this.logContext);
        }
        // 2011/03/17 modified by Ruling for 若回rtnCode給Handler，FISCGW會將此值回給財金，但此時AA已結束不需在回財金，故改成回空白
        return StringUtils.EMPTY;
    }

    /**
     * 拆解並檢核由財金發動的Request電文
     *
     * @return
     */
    private FEPReturnCode processRequestHeader() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);

        if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
                || rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
            getFiscBusiness().setFeptxn(null);
            getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
            isExitProgram = true;
            return rtnCode;
        }
        return rtnCode;

    }

    private FEPReturnCode insertINBK2160() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            //檢核Header
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
            //檢核單筆限額
            if (getFiscBusiness().getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
                    && !"0".equals(getTxData().getMsgCtl().getMsgctlCheckLimit())) {
                if (FISCPCode.PCode2521.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                        && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnNoticeId())) {
                    if (!"9999".equals(getFiscBusiness().getFeptxn().getFeptxnNoticeId())
                            && !"9998".equals(getFiscBusiness().getFeptxn().getFeptxnNoticeId())) {
                        getLogContext().setRemark("存入非9999及9998, FEPTXN_NOTICE_ID=" + getFiscBusiness().getFeptxn().getFeptxnNoticeId());
                        logMessage(getLogContext());
                        return ATMReturnCode.OtherCheckError;
                    }
                    // 2020/11/16 Modify by Ruling for 手機門號跨行轉帳：原存跨行存款交易(2521)拒絶手機門號轉帳
                    if ("Y".equals(((FeptxnExt) getFiscBusiness().getFeptxn()).getFeptxnMtp())) {
                        getLogContext().setRemark("原存跨行存款交易(2521)拒絕手機門號轉帳");
                        logMessage(getLogContext());
                        return FEPReturnCode.TranInACTNOError;
                    }
                    if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnIcmark()) || getFiscBusiness().getFeptxn().getFeptxnIcmark().trim().length() < 20
                            || !PolyfillUtil.isNumeric(getFiscBusiness().getFeptxn().getFeptxnIcmark().trim())) {
                        getLogContext().setRemark("IC卡備註欄為空白或長度小於20位或內含非數字" + getFiscBusiness().getFeptxn().getFeptxnIcmark());
                        logMessage(getLogContext());
                        return ATMReturnCode.TranInACTNOError;
                    }

                    if ("0000".equals(getFiscBusiness().getFeptxn().getFeptxnIcmark().substring(0, 4))
                            || StringUtils.leftPad("0", 16, '0').equals(getFiscBusiness().getFeptxn().getFeptxnIcmark().substring(4, 20))) {
                        getLogContext().setRemark(
                                "IC卡備註欄之金融卡帳號為0, 銀行別=" + getFiscBusiness().getFeptxn().getFeptxnIcmark().substring(0, 4) + ", 帳號=" + getFiscBusiness().getFeptxn().getFeptxnIcmark().substring(4, 20));
                        logMessage(getLogContext());
                        return ATMReturnCode.TranInACTNOError;
                    }

                    /* 12/22 修改, 檢核 9999/9998 與 ICMARK 是否相符 */
                    /* 轉出行代號(BBB9999) :存入金融卡號, */
                    if ("9999".equals(getFiscBusiness().getFeptxn().getFeptxnNoticeId())) {
                        /* 檢核轉入行及帳號是否 IC卡備註欄之金融卡帳號一致 */
                        if (!StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(1, 4).equals(getFiscBusiness().getFeptxn().getFeptxnTrinBkno())
                                || !StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(4, 20).equals(getFiscBusiness().getFeptxn().getFeptxnTrinActno())) {
                            getLogContext()
                                    .setRemark("存入本人(B9999)轉入行及帳號與IC卡備註欄之金融卡帳號不一致, 轉入帳號=" + getFiscBusiness().getFeptxn().getFeptxnTrinBkno() + "-" + getFiscBusiness().getFeptxn().getFeptxnTrinActno()
                                            + ", IC卡備註欄之金融卡帳號=" + StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(1, 4) + "-"
                                            + StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(4, 20));
                            logMessage(getLogContext());
                            return ATMReturnCode.TranInACTNOError;
                        }
                    } else {
                        /* 轉出行代號(BBB9998): 存入其他跨行帳號 */
                        /* 檢核轉入行及帳號必須與IC卡備註欄之金融卡帳號不同 */
                        if (StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(1, 4).equals(getFiscBusiness().getFeptxn().getFeptxnTrinBkno())
                                && StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(4, 20).equals(getFiscBusiness().getFeptxn().getFeptxnTrinActno())) {
                            getLogContext()
                                    .setRemark("存入非本人(B9998)轉入行及帳號與IC卡備註欄之金融卡帳號一致, 轉入帳號=" + getFiscBusiness().getFeptxn().getFeptxnTrinBkno() + "-" + getFiscBusiness().getFeptxn().getFeptxnTrinActno()
                                            + ", IC卡備註欄之金融卡帳號=" + StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(1, 4) + "-"
                                            + StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(4, 20));
                            logMessage(getLogContext());
                            return ATMReturnCode.TranInACTNOError;
                        }
                    }
                }
            }
            //檢核MAC
            //註解合庫開發環境暫時不測試
//            if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnIcTac())) {// 晶片卡
//                rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
//                this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
//                logMessage(this.logContext);
//                if (rtnCode != CommonReturnCode.Normal) {
//                    return rtnCode;
//                }
//            }
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
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            String TxType;
            if (getFiscBusiness().getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                TxType = "1"; //入扣帳
            } else {
                TxType = "0"; //檢核
            }
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(TxType);
            tota = hostAA.getTota();
            if (rtnCode != CommonReturnCode.Normal) {
                if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
                    return rtnCode;
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
                    getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
                    getFiscBusiness().updateTxData();
                    isExitProgram = true;
                    return rtnCode;
                }
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
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) ;
            {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
            }
        } else if (_rtnCode2 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) ;
            {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode2, FEPChannel.FISC, getLogContext()));
            }
        } else if (_rtnCode3 != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode3.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) ;
            {
                this.logContext.setProgramName(StringUtils.join(ProgramName));
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode3, FEPChannel.FISC, getLogContext()));
            }
        } else {
            getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
        }
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
        _rtnCode = getFiscRes().makeFISCMsg();

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
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
                }
                // spec change 20101124
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) ;
                {
                    isEC = false;
                    getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
                }
            } else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) { /* 初值:0 */
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R");  /* 拒絕-正常 */
            }

            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); /* FISC Response */
            getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/


            rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
            if (rtnCode != CommonReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
                transactionManager.rollback(txStatus);
                return rtnCode;
            }

            transactionManager.commit(txStatus);
            return rtnCode;
        } catch (
                Exception ex) {
            if (!txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
            }
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "updateTxData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    private FEPReturnCode processAPTOT() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            _rtnCode4 = getFiscBusiness().processAptot(isEC);
            if (_rtnCode4 != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
                getFiscBusiness().updateTxData();
            }
        }
        return rtnCode;
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
        String wk_BITMAP = null;

        try {
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {// +REP
                /* 跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組 */
                if (FISCPCode.PCode2524.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTrinBkno())) {
                    wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
                } else {
                    wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
                }

                /* 2019/03/06 修改 for跨行轉帳小額交易手續費調降 */
                if ((FISCPCode.PCode2521.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                        || FISCPCode.PCode2523.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                        || FISCPCode.PCode2524.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()))
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTrinBkno())
                        && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnAcctSup())) {
                    wk_BITMAP = wk_BITMAP.substring(0, 56) + "1" + wk_BITMAP.substring(57);
                }
            } else {// -REP
                wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
            }

            // 依據wk_BITMAP(判斷是否搬值)
            for (int i = 2; i <= 63; i++) {
                // Loop IDX from 3 to 64
                if (wk_BITMAP.charAt(i) == '1') {
                    switch (i) {
                        case 2: {  /* 交易金額 */
                            getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmt().toString());
                            break;
                        }
                        case 5: { /* 代付單位 CD/ATM 代號 */
                            getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(), 8, "0"));
                            break;
                        }
                        case 6: {   /* 可用餘額 */
                            getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
                            break;
                        }
                        case 14: { /* 跨行手續費 */
                            getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
                            break;
                        }
                        case 21: {  /*促銷訊息*/
                            getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnLuckyno());
                            break;
                        }
                        case 37: { /* 帳戶餘額*/
                            getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
                            break;
                        }
                        case 50: {  /* 轉入帳號*/
                            getFiscRes().setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
                            break;
                        }
                        /* 2019/03/06 修改 for跨行轉帳小額交易手續費調降 */
                        case 56: {
                            getFiscRes().setAcctSup(getFiscBusiness().getFeptxn().getFeptxnAcctSup());
                            break;
                        }
                    }
                }
            }

            RefString refMac = new RefString(getFiscRes().getMAC());
            _rtnCode4 = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
            getFiscRes().setMAC(refMac.get());
            if (_rtnCode4 != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
                getFiscRes().setMAC("00000000");
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "prepareBody");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }
}
