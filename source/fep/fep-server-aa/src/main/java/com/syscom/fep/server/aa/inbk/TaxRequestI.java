package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;

import com.syscom.fep.base.enums.*;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.*;
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
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
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
import com.syscom.fep.vo.constant.FISCType;
import com.syscom.fep.vo.enums.ATMNCCardStatus;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.enums.DAPPAppMsg;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 處理財金發動跨行繳稅交易電文
 *
 * @author Joseph
 */
public class TaxRequestI extends INBKAABase {
    private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode3 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode4 = CommonReturnCode.Normal;

    private Object tota = null;

    private boolean isPlusCirrus = false;
    private Intltxn defINTLTXN = new Intltxn();
    private Intltxn oriINTLTXN = new Intltxn();
    private IntltxnMapper dbINTLTXN = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
    private boolean isEC = false;
    private boolean isNWD = false;
    private Nwdtxn defNWDTXN;
    private NwdtxnMapper dbNWDTXN = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
    private String rsCARD = "N";
    private String _OWDCount;
    private boolean isQRPScaned = false; // 豐錢包被掃交易
    private boolean isQRPMain = false; // 豐錢包主掃交易
    private Qrptxn defQRPTXN;
    private QrptxnMapper dbQRPTXN = SpringBeanFactoryUtil.getBean(QrptxnMapper.class);

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
    public TaxRequestI(FISCData txnData) throws Exception {
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
                    return _rtnCode2.toString();
                }
            }

            //3.商業邏輯檢核 & 電文Body檢核
            if (_rtnCode == CommonReturnCode.Normal && _rtnCode2 == CommonReturnCode.Normal) { /*CheckHeader Error*/
                _rtnCode = checkBusinessRule();
            }

            //4.SendToCBS/ASC
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = this.sendToCBSAndAsc();
            }

            //6.PrepareFISC:準備回財金的相關資料
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = this.prepareForFISC();
            }

            //7.UpdateTxData: 更新交易記錄(FEPTXN & INTLTXN)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = this.updateTxData();
            }

            /* 10/16 修改, 更新APTOT之後再送Rsp電文回財金 */
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
                        && "Y".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160())){
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
            rtnCode = getFiscBusiness().prepareInbk2160();
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
            if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnPaytype())) {
                rtnCode = getFiscBusiness().checkPAYTYPE();
                if (rtnCode != FEPReturnCode.Normal) {
                    return rtnCode;
                }
            }

            //檢核MAC
            rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
            this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

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
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
//            feptxn.setFeptxnTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(TxType);
            tota = hostAA.getTota();

            if(rtnCode != FEPReturnCode.Normal){
                if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())){
                    return CommonReturnCode.Normal;
                }else{
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); /*Reject-abnormal*/
                    getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)1); /*AA Close*/
                    getFiscBusiness().updateTxData();
                    if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())){
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

        return _rtnCode;
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
                /* 1/30 跨行無卡提款交易成功, 更新預約檔  */
                if (isNWD) {
                    getFiscBusiness().updateNWDReg();
                }
            } else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {// spec change 20101124
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); // 拒絕
                /* 1/30 跨行無卡提款交易成功, 更新預約檔  */
                if (isNWD) {
                    getFiscBusiness().updateNWDReg();
                }
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
                /*跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組*/
                wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
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
                            getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(),8,"0"));
                            break;
                        }
                        case 6: { /* 可用餘額 */
                            /* 11/19 配合永豐修改, 改送帳戶餘額(FEPTXN_BALB) */
                            getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
                            break;
                        }
                        case 13:{
                            /*TROUT_BKNO for 2531,2568,2569繳稅交易*/
                            //20220927以主機回應回傳
                            getFiscRes().setTroutBkno(getFiscBusiness().getFeptxn().getFeptxnTroutBkno7());
                            break;
                        }
                        case  14:{ /* 跨行手續費 */
                            getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
                            break;
                        }
                        case 16: { /* 狀況代號 */
                            getFiscRes().setRsCode(getFiscBusiness().getFeptxn().getFeptxnRsCode());
                            break;
                        }
                        case 21:{ /* 促銷訊息 */
                            //20220927 改用CBS_TOTA.LUCKYNO
                            getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnLuckyno());
                            break;
                        }
                        case 37:{ /* 帳戶餘額*/
                            getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
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
