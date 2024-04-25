package com.syscom.fep.server.aa.inbk;

import java.io.File;
import java.math.BigDecimal;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.vo.constant.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.github.pagehelper.util.StringUtil;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
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
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
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
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;

/**
 * @author Richard
 */
public class WDRequstI extends INBKAABase {
    private Object tota = null;

    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode3 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode4 = CommonReturnCode.Normal;
    private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
    private boolean isExitProgram = false;

    private boolean isEC = false;
    private boolean isNWD = false;
    private Nwdtxn defNWDTXN;
    private NwdtxnMapper dbNWDTXN = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);

    /**
     * AA的建構式,在這邊初始化及設定其他相關變數

     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     *                <p>
     *                初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
     *                ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
     *                FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
     * @throws Exception
     */
    public WDRequstI(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     */
    @Override
    public String processRequestData() {
        try {
            // 拆解並檢核財金電文
            _rtnCode = this.processRequestHeader();


            //Prepare()交易記錄初始資料
            _rtnCode2 = getFiscBusiness().prepareFEPTXN();
            if (_rtnCode2 != CommonReturnCode.Normal) {
                getLogContext().setMessage(_rtnCode2.toString());
                getLogContext().setRemark("準備FEPTXN有誤!!");
                logMessage(Level.INFO, getLogContext());
                return _rtnCode2.toString();
            }

            if (isNWD) {
                /* 將無卡提款序號寫入卡號 */
                getFiscBusiness().getFeptxn().setFeptxnMajorActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
                RefBase<Nwdtxn> nwdtxnRefBase = new RefBase<>(new Nwdtxn());
                _rtnCode = getFiscBusiness().prepareNWDTXN(nwdtxnRefBase);
                defNWDTXN = nwdtxnRefBase.get();
            }

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

            //4. SendToCBS: 帳務主機處理
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = sendToCBS();
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
     * 4. SendToCBS:送往CBS主機處理
     *
     * @throws Exception
     */
    private FEPReturnCode sendToCBS() throws Exception {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            /* 交易前置處理查詢處理 */
            String txType = "1"; // 上CBS查詢、檢核
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(txType);
            tota = hostAA.getTota();
            if (rtnCode != CommonReturnCode.Normal) {
                isExitProgram = true;
                if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
                    return CommonReturnCode.Normal;
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
                    getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
                    getFiscBusiness().updateTxData();
                    if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
                        getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); /*成功*/
                        if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
                            // 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
                            isExitProgram = true;
                            return IOReturnCode.FEPTXNUpdateError;
                        }
                    }
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

    /**
     * 商業邏輯檢核
     * 20221125 Bruce add
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        try {
            //(1) 	檢核MAC 及壓 MAC 註解先不測試
            rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
            this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //(2) 檢核無卡提款PINBLOCK
            /* 1/30 修改 for 無卡提款 */
            if (isNWD) {
                /* 無卡提款, SYNC_PPKEY(Bitmap 39) 必須有值 */
                if (StringUtils.isEmpty(this.getFiscReq().getSyncPpkey())) {
                    return FEPReturnCode.OtherCheckError;/* 2999:其他類檢核錯誤 */
                }
                /* 無卡提款, PINBLOCK(Bitmap 5) 必須有值 */
                if (StringUtils.isEmpty(this.getFiscReq().getPINBLOCK())) {
                    return FEPReturnCode.OtherCheckError;/* 2999:其他類檢核錯誤 */
                }

                /* 2024/2/2 無卡提款交易新增檢核 SYNC_PPKEY */
                if(!SysStatus.getPropertyValue().getSysstatF3dessync().equals(this.getFiscReq().getSyncPpkey())){
                    return FEPReturnCode.PPKeySyncError;
                }
            }

            //(3) 檢核提款金額及單筆限額
            if (this.getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
                _rtnCode = this.getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
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
     * 拆解並檢核由財金發動的Request電文
     * Bruce 20221125
     *
     * @return
     */
    private FEPReturnCode processRequestHeader() {
        FEPReturnCode rtnCode1 = CommonReturnCode.Normal;
        //(1) 	檢核財金電文 Header
        rtnCode1 = getFiscBusiness().checkHeader(getFiscReq(), true);

        if (rtnCode1 != CommonReturnCode.Normal) {  /*FISC RC:Garbled Message*/
            getFiscBusiness().setFeptxn(null);
            getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode1, getFiscCon());
            return rtnCode1;
        }

        //(2) 	判斷是否為無卡提款交易
        if (FISCPCode.PCode2510.getValueStr().equals(getFiscReq().getProcessingCode()) && "6071".equals(getFiscReq().getAtmType())) {
            isNWD = true;
            this.getLogContext().setRemark("無卡提款");
            this.logMessage(getLogContext());
        }
        return rtnCode1;

    }

//	/**
//	 * 拆解並檢核由財金發動的Request電文
//	 * 
//	 * @return
//	 * 
//	 */
//	private FEPReturnCode processRequestHeader() {
//		FEPReturnCode rtnCode = CommonReturnCode.Normal;
//
//		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
//
//		if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
//				|| rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
//			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
//			return rtnCode;
//		}
//
//		// 2014/09/12 Modify by Ruling for 24XX類的國際卡交易，避免OPC(3106)做2000-ExceptionCheckOut時，回的rtnCode=ReceiverBankServiceStop會被蓋掉的問題
//		if (StringUtils.isNotBlank(getFiscReq().getOriData()) && rtnCode == CommonReturnCode.Normal) {
//			rtnCode = getFiscBusiness().checkORI_DATA(MessageFlow.Request);
//			isPlusCirrus = true;
//		}
//
//		// 2018/02/12 Modify by Ruling for 跨行無卡提款
//		if (FISCPCode.PCode2510.getValueStr().equals(getFiscReq().getProcessingCode()) && "6071".equals(getFiscReq().getAtmType())) {
//			isNWD = true;
//			getLogContext().setRemark("無卡提款");
//			logMessage(getLogContext());
//		}
//
//		// 2019/07/23 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：以端末設備型態判斷主被掃交易
//		// 被掃交易
//		if ((FISCPCode.PCode2541.getValueStr().equals(getFiscReq().getProcessingCode()) || FISCPCode.PCode2542.getValueStr().equals(getFiscReq().getProcessingCode()))
//				&& "6".equals(getFiscReq().getAtmType().substring(0, 1)) && ("B".equals(getFiscReq().getAtmType().substring(3, 4)) || "C".equals(getFiscReq().getAtmType().substring(3, 4)))) {
//			isQRPScaned = true;
//			getLogContext().setRemark("被掃交易");
//			logMessage(getLogContext());
//		}
//		// 2019/12/26 Modify by Ruling for QRP新增需求：配合消費扣款退貨交易(PCODE2543)，財金增加Option欄位(端末設備型態)，因端末設備型態為Option欄位，在substring前要先判斷是否有值，故與2541/2542被掃拆開來寫
//		if (FISCPCode.PCode2543.getValueStr().equals(getFiscReq().getProcessingCode()) && StringUtils.isNotBlank(getFiscReq().getAtmType())
//				&& "6".equals(getFiscReq().getAtmType().substring(0, 1)) && ("B".equals(getFiscReq().getAtmType().substring(3, 4)) || "C".equals(getFiscReq().getAtmType().substring(3, 4)))) {
//			isQRPScaned = true;
//			getLogContext().setRemark("被掃交易");
//			logMessage(getLogContext());
//		}
//
//		// 2019/12/26 Modify by Ruling for QRP新增需求：配合消費扣款退貨交易(PCODE2543)，財金增加Option欄位(端末設備型態)
//		// 2019/12/26 Modify by Ruling for 豐錢包新增Paytax繳稅功能：增加QRP繳稅-晶片卡跨行繳稅交易(PCODE2568)
//		// 主掃交易
//		if ((FISCPCode.PCode2541.getValueStr().equals(getFiscReq().getProcessingCode()) || FISCPCode.PCode2525.getValueStr().equals(getFiscReq().getProcessingCode())
//				|| FISCPCode.PCode2564.getValueStr().equals(getFiscReq().getProcessingCode()) || FISCPCode.PCode2543.getValueStr().equals(getFiscReq().getProcessingCode())
//				|| FISCPCode.PCode2568.getValueStr().equals(getFiscReq().getProcessingCode())) && FISCType.Type659A.equals(getFiscReq().getAtmType())) {
//			isQRPMain = true;
//			getLogContext().setRemark("主掃交易");
//			logMessage(getLogContext());
//		}
//
//		if (rtnCode != CommonReturnCode.Normal) {
//			strFISCRc = rtnCode;
//			return CommonReturnCode.Normal;
//		}
//		return rtnCode;
//
//	}

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
            //以TRANSACTION 新增交易記錄
            _rtnCode3 = getFiscBusiness().insertFEPTxn();
            if (_rtnCode3 != CommonReturnCode.Normal) {
                getLogContext().setMessage(_rtnCode2.toString());
                getLogContext().setRemark("新增交易記錄有誤!!");
                logMessage(Level.INFO, getLogContext());
                transactionManager.rollback(txStatus);
                return rtnCode;
            }

            if (isNWD) {
                if (dbNWDTXN.insertSelective(defNWDTXN) < 1) {
                    transactionManager.rollback(txStatus);
                    return IOReturnCode.UpdateFail;
                }
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



    private FEPReturnCode prepareForFISC() throws Exception {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
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
        return rtnCode;
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
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    if (!FISCPCode.PCode2430.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                            && !FISCPCode.PCode2470.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 非國際提款沖銷
                        isEC = false;
                        getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
                    }
                }
            } else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) { /*初值:0*/
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R");  /*拒絕-正常*/
            }

            // 2018/02/12 Modify by Ruling for 跨行無卡提款:更新預約檔
            if (isNWD) {
                getFiscBusiness().updateNWDReg();
            }

            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
            getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/

            rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
            if (rtnCode != CommonReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
                transactionManager.rollback(txStatus);
                return rtnCode;
            }
            // (2) 判斷是否需更新 NWDTXN
            if (isNWD) {
                defNWDTXN.setNwdtxnTxCurAct(getFiscBusiness().getFeptxn().getFeptxnTxCurAct());
                defNWDTXN.setNwdtxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct());
                defNWDTXN.setNwdtxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
                defNWDTXN.setNwdtxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
                defNWDTXN.setNwdtxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno()); // for Combo卡
                /* 2021/5/31 修改 for 將手續費寫入 NWDTXN */
                defNWDTXN.setNwdtxnFeeCur(getFiscBusiness().getFeptxn().getFeptxnFeeCur());
                defNWDTXN.setNwdtxnFeeCustpay(getFiscBusiness().getFeptxn().getFeptxnFeeCustpay());
                defNWDTXN.setNwdtxnFeeCustpayAct(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct());
                if (dbNWDTXN.updateByPrimaryKeySelective(defNWDTXN) < 1) {
                    transactionManager.rollback(txStatus);
                    return IOReturnCode.UpdateFail;
                }
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

    private FEPReturnCode sendToNB() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        // 2020/07/02 Modify by Ruling for 大戶APP無卡提款推播
        // 2020/09/09 Modify by Ruling for 新增MDAWHO Channel Code：依預約Channel為MDAWHO，發送大戶APP WebService
        if (FEPChannel.MOBILBANK.name().equals(defNWDTXN.getNwdtxnRegChannel())) {
            Ncnb hostNB = new Ncnb(getTxData());
            rtnCode = hostNB.sendToNCNB("2", "");

        }
//        else if (FEPChannel.DAPP.name().equals(defNWDTXN.getNwdtxnRegChannel())
//                || FEPChannel.MDAWHO.name().equals(defNWDTXN.getNwdtxnRegChannel())) {
//            Dapp hostDAPP = new Dapp(getTxData());
//            rtnCode = hostDAPP.sendToDAPP(DAPPAppMsg.SSCodeErrorLimit.getValue());
//        }
        return rtnCode;
    }

    /**
     * 解圈
     *
     * @return <modify>
     * <modifier>Ruling</modifier>
     * <date>2015/10/16</date>
     * <reason>為了避免Req電文還沒結束Con電文就進來，調整主流程順序先更新ProcessAPTOT再送財金:將原本在ProcessAPTOT的解圈程式移至這裡</reason>
     * </modify>
     */
    private FEPReturnCode processLock() {
        T24 hostT24 = new T24(getTxData());
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        if (FISCPCode.PCode2551.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 預約授權
            getLogContext().setRemark("Waiting 20 minutes");
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.DEBUG, getLogContext());
            // Waiting 20 minutes
            try {
                Thread.sleep(1200000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return rtnCode;
            }

            // add by Maxine on 2011/08/15 for 需要重新讀取該筆 FEPTXN,判斷是否需要解圈
            Feptxn feptxn = null;
            try {
                feptxn = this.feptxnDao.selectByPrimaryKey(getFiscBusiness().getFeptxn().getFeptxnTxDate(), getFiscBusiness().getFeptxn().getFeptxnEjfno());
            } catch (Exception e) {
                e.printStackTrace();
                return rtnCode;
            }
            if (feptxn == null) {
                return IOReturnCode.FEPTXNReadError;
            }

            if ("A".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
                getFiscBusiness().getFeptxn().setFeptxnTxrust("T");
                getFiscBusiness().updateTxData();
                // modify 20110225
                // rtnCode = hostT24.SendToT24(getTxData().getMsgCtl().MSGCTL_TWCBSTXID, 2, True) '解圏
                try {
                    rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), 4, true); // 解圏
                } catch (Exception e) {
                    e.printStackTrace();
                    return rtnCode;
                }
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
                getFiscBusiness().getFeptxn().setFeptxnTxrust("C");
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
                wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
                /* 1/18 修改 for 跨行無卡提款 */
                if (isNWD) {
                    wk_BITMAP = wk_BITMAP.substring(0, 21) + "1" + wk_BITMAP.substring(22);
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
                        case 6: {  /* 可用餘額 */
                            getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
                            break;
                        }
                        case 14: {  /* 跨行手續費 */
                            getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
                            break;
                        }
                        case 21: {  /*促銷訊息*/
                            if (isNWD) {
                                getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
                                /* 2019/7/3 修改for APP 交易*/
                            } else {
                                getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnLuckyno());
                            }
                            break;
                        }
                        case 37: { /* 帳戶餘額*/
                            getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
                            break;
                        }
                    }
                }
            }
            RefString refMac = new RefString(getFiscRes().getMAC());
            rtnCode = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
            getFiscRes().setMAC(refMac.get());
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
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

    /**
     * 檢核更新原始交易狀態
     *
     * @return
     */
    private FEPReturnCode checkoriFEPTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String I_TX_DATE = "";
        // QueryFEPTXNByStan:
        try {
            oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
            getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
            getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
            getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
            // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] 本營業日檔
            getFiscBusiness().setOriginalFEPTxn(oriDBFEPTXN.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));

            if (getFiscBusiness().getOriginalFEPTxn() == null) {
                if (FISCPCode.PCode2542.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
                    I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8);
                } else if (FISCPCode.PCode2552.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
                    I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnTxDatetimePreauth().substring(0, 8);
                } else {
                    I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnDueDate(); // for 2430,2470
                }
                rtnCode = searchOriginalFEPTxn(I_TX_DATE, getFiscBusiness().getFeptxn().getFeptxnBkno(), getFiscBusiness().getFeptxn().getFeptxnOriStan());
                if (rtnCode != CommonReturnCode.Normal) {
                    rtnCode = FISCReturnCode.TransactionNotFound; // 無此交易 spec change 20100720
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
                    getLogContext().setRemark("SearchFeptxn 無此交易");
                    getLogContext().setProgramName(ProgramName);
                    logMessage(Level.DEBUG, getLogContext());
                    return rtnCode;
                }
            }

            /// *檢核原交易是否成功*/
            if (!"A".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()) && !"B".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {// /*交易成功*/
                rtnCode = FISCReturnCode.TransactionNotFound; // 無此交易 'spec change 20100720
                getFiscBusiness().getFeptxn().setFeptxnTxrust("I"); // 原交易已拒絕
                return rtnCode;
            }

            /// *檢核原交易之 MAPPING 欄位是否相同*/
            if ((!FISCPCode.PCode2552.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                    && getFiscBusiness().getFeptxn().getFeptxnTxAmt().doubleValue() != getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmt().doubleValue())
                    || !getFiscBusiness().getFeptxn().getFeptxnAtmno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim())
                    || !getFiscBusiness().getFeptxn().getFeptxnAtmType().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType().trim())
                    || !getFiscBusiness().getFeptxn().getFeptxnIcmark().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcmark().trim())
                    || !getFiscBusiness().getFeptxn().getFeptxnMerchantId().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnMerchantId().trim())
                    || !getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim())) {
                rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
                getLogContext().setRemark(StringUtils.join("欄位資料不符, 原 FeptxnTxAmt:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmt().doubleValue(), "FeptxnTxAmt",
                        getFiscBusiness().getFeptxn().getFeptxnTxAmt().doubleValue(),
                        "原 FeptxnAtmno:", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim(), "FeptxnAtmno", getFiscBusiness().getFeptxn().getFeptxnAtmno().trim(),
                        "原 FeptxnAtmType:", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType().trim(), "FeptxnAtmType", getFiscBusiness().getFeptxn().getFeptxnAtmType().trim(),
                        "原 FeptxnIcmark:", getFiscBusiness().getOriginalFEPTxn().getFeptxnIcmark().trim(), "FeptxnIcmark", getFiscBusiness().getFeptxn().getFeptxnIcmark().trim(),
                        "原 FeptxnMerchantId:", getFiscBusiness().getOriginalFEPTxn().getFeptxnMerchantId().trim(), "FeptxnMerchantId", getFiscBusiness().getFeptxn().getFeptxnMerchantId().trim(),
                        "原 FeptxnTroutActno:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim(), "FeptxnTroutActno", getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim()));
                logMessage(Level.INFO, getLogContext());
                getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
                return rtnCode;
            }
            if (FISCPCode.PCode2542.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 消費扣款沖正
                if (!getFiscBusiness().getFeptxn().getFeptxnIcTac().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcTac().trim())
                        || !getFiscBusiness().getFeptxn().getFeptxnIcSeqno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqno().trim())
                        || !getFiscBusiness().getFeptxn().getFeptxnAtmChk().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmChk().trim())
                        || !getFiscBusiness().getFeptxn().getFeptxnOrderNo().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnOrderNo().trim())
                        || !getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().trim())) {
                    rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
                    getLogContext().setRemark(StringUtils.join("欄位資料不符, 原 FeptxnIcTac:", getFiscBusiness().getOriginalFEPTxn().getFeptxnIcTac().trim(), "FeptxnIcTac",
                            getFiscBusiness().getFeptxn().getFeptxnIcTac().trim(),
                            "原 FeptxnIcSeqno:", getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqno().trim(), "FeptxnIcSeqno", getFiscBusiness().getFeptxn().getFeptxnIcSeqno().trim(),
                            "原 FeptxnAtmChk:", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmChk().trim(), "FeptxnAtmChk", getFiscBusiness().getFeptxn().getFeptxnAtmChk().trim(),
                            "原 FeptxnOrderNo:", getFiscBusiness().getOriginalFEPTxn().getFeptxnOrderNo().trim(), "FeptxnOrderNo", getFiscBusiness().getFeptxn().getFeptxnOrderNo().trim(),
                            "原 FeptxnTxDatetimeFisc:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().trim(), "FeptxnTxDatetimeFisc",
                            getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().trim()));
                    logMessage(Level.INFO, getLogContext());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
                    return rtnCode;
                }
            } else if (FISCPCode.PCode2552.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 授權完成
                if (!getFiscBusiness().getFeptxn().getFeptxnTxAmtPreauth().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtPreauth())
                        || !getFiscBusiness().getFeptxn().getFeptxnIcSeqnoPreauth().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqnoPreauth().trim())
                        || !getFiscBusiness().getFeptxn().getFeptxnTxDatetimePreauth().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimePreauth().trim())) {
                    rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
                    getLogContext().setRemark(StringUtils.join("欄位資料不符, 原 FeptxnTxAmtPreauth:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtPreauth(), "FeptxnTxAmtPreauth",
                            getFiscBusiness().getFeptxn().getFeptxnTxAmtPreauth(),
                            "原 FeptxnIcSeqnoPreauth:", getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqnoPreauth().trim(), "FeptxnIcSeqnoPreauth",
                            getFiscBusiness().getFeptxn().getFeptxnIcSeqnoPreauth().trim(),
                            "原 FeptxnTxDatetimePreauth:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimePreauth().trim(), "FeptxnTxDatetimePreauth",
                            getFiscBusiness().getFeptxn().getFeptxnTxDatetimePreauth().trim()));
                    logMessage(Level.INFO, getLogContext());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
                    return rtnCode;
                }
            }
            oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
            getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("T"); // 沖銷或授權完成進行中
            oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
            // modified By Maxine on 2011/10/13 for 財金REQ Time改存入FEPTXN_REQ_DATETIME
            getFiscBusiness().getFeptxn().setFeptxnDueDate(getFiscBusiness().getOriginalFEPTxn().getFeptxnReqDatetime().substring(0, 8));
            // getFiscBusiness().getFeptxn().FEPTXN_DUE_DATE = getFiscBusiness().getOriginalFEPTxn().FEPTXN_TX_DATE
            if (!FISCPCode.PCode2542.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
                getFiscBusiness().getFeptxn().setFeptxnCbsRrn(getFiscBusiness().getOriginalFEPTxn().getFeptxnCbsRrn()); // 以便進行CBS沖正
            }
            // spec change 20101112
            if (!ZoneCode.TWN.equals(getFiscBusiness().getFeptxn().getFeptxnZoneCode())) {
                getFiscBusiness().getFeptxn().setFeptxnVirTxseq(getFiscBusiness().getOriginalFEPTxn().getFeptxnVirTxseq()); // 以便進行海外主機沖正
                getFiscBusiness().getFeptxn().setFeptxnFeeCustpayAct(getFiscBusiness().getOriginalFEPTxn().getFeptxnFeeCustpayAct());
                // spec change 20101117
                getFiscBusiness().getFeptxn().setFeptxnTbsdyAct(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyAct());
                getFiscBusiness().getFeptxn().setFeptxnTxnmode(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxnmode());
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "checkoriFEPTXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     * 以日期搜尋 FEPTXN
     *
     * @return
     */
    private FEPReturnCode searchOriginalFEPTxn(String txDate, String bkno, String stan) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FeptxnDao db = SpringBeanFactoryUtil.getBean("feptxnDao");
        Bsdays aBSDAYS = new Bsdays();
        BsdaysMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
        String wk_TBSDY = null;
        String wk_NBSDY = "";
        // Dim i As Int32
        try {
            db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
            getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
            getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
            getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
            getFiscBusiness().setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
            if (getFiscBusiness().getOriginalFEPTxn() == null) {
                aBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
                aBSDAYS.setBsdaysDate(txDate);
                aBSDAYS = dbBSDAYS.selectByPrimaryKey(aBSDAYS.getBsdaysZoneCode(), aBSDAYS.getBsdaysDate());
                if (aBSDAYS == null) {
                    return IOReturnCode.BSDAYSNotFound;
                }
                // ASK CONNIE
                if (DbHelper.toBoolean(aBSDAYS.getBsdaysWorkday())) {// 工作日
                    wk_TBSDY = aBSDAYS.getBsdaysDate();
                    wk_NBSDY = aBSDAYS.getBsdaysNbsdy();
                } else {
                    wk_TBSDY = aBSDAYS.getBsdaysNbsdy();
                }
                if (wk_TBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
                    db.setTableNameSuffix(wk_TBSDY.substring(6, 6 + 2), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
                    getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
                    getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
                    getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
                    getFiscBusiness()
                            .setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
                    if (getFiscBusiness().getOriginalFEPTxn() == null) {
                        if (StringUtils.isNotBlank(wk_NBSDY) && wk_NBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
                            db.setTableNameSuffix(wk_NBSDY.substring(6, 2), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
                            getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
                            getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
                            getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
                            getFiscBusiness().setOriginalFEPTxn(
                                    db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
                            if (getFiscBusiness().getOriginalFEPTxn() == null) {
                                rtnCode = IOReturnCode.FEPTXNNotFound;
                            }
                        } else {
                            rtnCode = IOReturnCode.FEPTXNNotFound;
                        }
                    }
                } else {
                    rtnCode = IOReturnCode.FEPTXNNotFound;
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "searchOriginalFEPTxn");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * (10) 檢核 MAC ， PINBLOCK及壓 MAC
     *
     * <history>
     * <modify>
     * <modifer>Husan </modifer>
     * <time>2010/12/01</time>
     * <reason>修正上主機部分改由參考HostBusiness</reason>
     * <modifer>Husan </modifer>
     * <time>2011/01/30</time>
     * <reason>connie spec change 檢核 PP key 是否同步改成SYSSTAT_F3DESSYNC</reason>
     * </modify>
     * </history>
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode checkMACPINBLOCKAndMakeMAC() throws Exception {
        // modify 2010/12/01
        Credit hostCreidt = new Credit(getTxData());
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        // modify 2011/01/30
        // 檢核 PP key 是否同步
        if (!SysStatus.getPropertyValue().getSysstatF3dessync().equals(getFiscReq().getSyncPpkey())) {
            rtnCode = FISCReturnCode.PPKeySyncError; // 0303-客戶亂碼基碼( PP-KEY )不同步
            return rtnCode;
        }

        // 2018/05/22 Modify by Ruling for MASTER DEBIT加悠遊
        // 2020/02/26 Modify by Ruling for Combo卡應該要送信用卡主機驗密，而不是送FN000307驗密造成交易失敗，Credit(C)改為Combo(M)
        // 檢核 MAC & PINBLOCK 及壓 MAC
        if (BINPROD.Combo.equals(getFiscBusiness().getFeptxn().getFeptxnTroutKind()) || BINPROD.Debit.equals(getFiscBusiness().getFeptxn().getFeptxnTroutKind())) {
            // COMBO卡
            // 2016/06/27 Modify by Ruling for 財金換KEY問題，將MakeMAC移至下面
            rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
            // rtnCode = encHelper.CheckFISCMACAndMakeMAC(fiscReq.MAC, fiscRes.MAC)
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            // COMBO卡需至信用卡主機驗密
            rtnCode = hostCreidt.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(), (byte) 1); // 需轉換 PINBLOCK & 壓MAC
            // spec change 20101020 add
            if (rtnCode != CommonReturnCode.Normal && StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnAscRc())) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
                getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
                getFiscBusiness().updateTxData();
                // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
                _rtnCode = rtnCode;
                return rtnCode;
            }
        } else {
            // rtnCode = mATMBusiness.CheckPVVCVV '/*國際金融卡PVV/CVV驗證 */ 'todo by henny
            // 2020/10/22 Modify by Ruling for 香港分行臨櫃變更國際提款密碼
            if (ZoneCode.HKG.equals(getFiscBusiness().getFeptxn().getFeptxnZoneCode()) && FISCPCode.PCode2410.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                    && StringUtils.isNotBlank(getFiscBusiness().getCard().getCardAuth())) {
                rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
                rtnCode = encHelper.checkHkAuth(getFiscReq().getPINBLOCK(), getFiscBusiness().getCard().getCardAuth());
            } else {
                RefString mac = new RefString(getFiscRes().getMAC());
                rtnCode = encHelper.checkFiscMacPinAndMakeMac(getFiscReq().getMAC(), mac);
                getFiscRes().setMAC(mac.get());
            }
            // rtnCode = encHelper.CheckFISCMACPINAndMakeMAC(getFiscReq().MAC, fiscRes.MAC)
        }
        // modify by henny 20110317 for spec change
        LogHelperFactory.getTraceLogger().trace("CheckFISCMACPINAndMakeMAC rtnCode=" + rtnCode);

        if (rtnCode == CommonReturnCode.Normal) {
            // 驗MAC及密碼正確
            rtnCode = getFiscBusiness().checkPWErrCnt(2);
        } else {
            LogHelperFactory.getTraceLogger().trace("密碼錯誤 call CheckPWErrCnt(1)");
            if (rtnCode == ENCReturnCode.ENCCheckPasswordError) {// 密碼錯誤
                rtnCode = getFiscBusiness().checkPWErrCnt(1);
                LogHelperFactory.getTraceLogger().trace("密碼錯誤 call CheckPWErrCnt(1) rtnCode=" + rtnCode);
            }
            return rtnCode;
        }
        return rtnCode;
    }
}
