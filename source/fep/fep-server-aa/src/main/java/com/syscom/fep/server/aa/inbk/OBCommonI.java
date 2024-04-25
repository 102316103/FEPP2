package com.syscom.fep.server.aa.inbk;

import java.util.Calendar;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.vo.constant.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ObtltxnExtMapper;
import com.syscom.fep.mybatis.model.Obtltxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FISCType;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.T24TxType;

/**
 * @author ZhaoKai
 */
@Deprecated
public class OBCommonI extends INBKAABase {
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
    private boolean isOB = false;
    private boolean isEC = false;
    private Obtltxn defOBTLTXN = new Obtltxn();
    private Obtltxn oriOBTLTXN = new Obtltxn();
    private ObtltxnExtMapper dbOBTLTXN = SpringBeanFactoryUtil.getBean(ObtltxnExtMapper.class);
    ;
    private String oriTXRUST; // for 2556 保留原OBTLTXN的交易狀態
    private boolean isQRPMain = false; // 豐錢包主掃交易

    public OBCommonI(FISCData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() {
        try {
            // 拆解並檢核財金電文
            _rtnCode = ProcessRequestHeader();

            // 換日後該筆交易應重抓DBFepTxn在INSERT FEPTXN時才會寫入換日後的FEPTXNXX
            if (!SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8).equals(feptxnDao.getTableNameSuffix())) {
                feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, ".processRequestData"));
                getTxData().setFeptxnDao(feptxnDao);
                getFiscBusiness().setFeptxnDao(feptxnDao);
            }

            // PrepareFEPTXN
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().prepareFEPTXN();
            }
            if (isOB) {
                if (_rtnCode == CommonReturnCode.Normal) {
                    RefBase<Obtltxn> defOBTLTXNBase = new RefBase<>(defOBTLTXN);
                    RefBase<Obtltxn> oriOBTLTXNBase = new RefBase<>(oriOBTLTXN);
                    _rtnCode = getFiscBusiness().prepareObtltxn(defOBTLTXNBase, oriOBTLTXNBase, MessageFlow.Request);
                    defOBTLTXN = defOBTLTXNBase.get();
                    oriOBTLTXN = oriOBTLTXNBase.get();
                }
                if (_rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark("PrepareObtltxn-收到財金發動之跨境電子支付在準備OBTLTXN發生異常");
                    logMessage(Level.INFO, getLogContext());
                    _rtnCode = CommonReturnCode.Normal;
                    strFISCRc = FISCReturnCode.MessageFormatError;
                    getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); // R 拒絶
                }

                // 2019/08/19 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：主掃交易不讀卡檔，卡片帳號設定為空白
                if (isQRPMain) {
                    getFiscBusiness().getFeptxn().setFeptxnMajorActno("");
                }
            }

            // 新增交易記錄
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = addTxData();
            }
            if (strFISCRc == CommonReturnCode.Normal) {
                strFISCRc = _rtnCode;
            }

            // 商業邏輯檢核 & 電文Body檢核
            if (_rtnCode == CommonReturnCode.Normal && strFISCRc == CommonReturnCode.Normal) {
                strFISCRc = checkBusinessRule();

                // 首次身分驗證狀況(P33)
                if (strFISCRc == CommonReturnCode.Normal && FISCPCode.PCode2555.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && "Y".equals(defOBTLTXN.getObtltxnYesfg())) {
                    strFISCRc = sendToCBSAndP33();
                }

                // 帳務主機處理
                if (strFISCRc == CommonReturnCode.Normal) {
                    _rtnCode = sendToCBSAndAsc();
                }
            }

            // 組回傳財金Response電文
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareForFISC();
            }

            // 更新交易記錄(FEPTXN & OBTLTXN)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = updateTxData();
            }

            // 更新跨行代收付
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = processAPTOT();
            }

            // 將組好的財金電文送給財金
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
            }

        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(getLogContext());
        } finally {
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage(getFiscRes().getFISCMessage());
            getTxData().getLogContext().setProgramName(this.aaName);
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.DEBUG, getLogContext());
        }

        return "";
    }

    /**
     * 拆解並檢核由財金發動的Request電文
     *
     * @return
     */
    private FEPReturnCode ProcessRequestHeader() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        // 檢核財金電文 Header
        rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);

        if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
                || rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
            getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
            return rtnCode;
        }

        // 判斷是否為晶片金融卡跨境電子支付交易
        // 避免OPC(3106)做2000-ExceptionCheckOut時，回的rtnCode=ReceiverBankServiceStop會被蓋掉的問題
        if (StringUtils.isNotBlank(getFiscReq().getOriData())) {
            // 大Class BITMAP36電文定義名稱仍為 ORI_DATA 不改名 但拆解會用 OB_DATA
            isOB = true;
        }

        // 2019/08/19 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：以端末設備型態判斷主被掃交易
        // 主掃交易
        if (FISCPCode.PCode2555.getValueStr().equals(getFiscReq().getProcessingCode()) && FISCType.Type659A.equals(getFiscReq().getAtmType())) {
            isQRPMain = true;
            getLogContext().setRemark("主掃交易");
            logMessage(Level.INFO, getLogContext());
        }

        if (rtnCode != CommonReturnCode.Normal) {
            strFISCRc = rtnCode;
            rtnCode = CommonReturnCode.Normal;
        }

        return rtnCode;
    }

    /**
     * 新增交易記錄
     *
     * @return
     */
    private FEPReturnCode addTxData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {

            rtnCode = getFiscBusiness().insertFEPTxn();
            if (rtnCode != CommonReturnCode.Normal) {
                transactionManager.rollback(txStatus);
                return rtnCode;
            }
            if (isOB) {
                if (dbOBTLTXN.insertSelective(defOBTLTXN) < 1) {
                    transactionManager.rollback(txStatus);
                    return IOReturnCode.UpdateFail;
                }
            }

            transactionManager.commit(txStatus);
            return rtnCode;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);

            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
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
            // (1) 檢核單筆限額
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()) && getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
                rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark("超過單筆限額");
                    logMessage(Level.INFO, getLogContext());
                    return rtnCode;
                }
            }

            // (2) 檢核MAC & TAC
            if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnIcTac())) {// 晶片卡
                // 跨境電子支付交易(2555)，檢核MAC& TAC
                // 2019/08/19 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：增加QR CODE2555主掃交易，檢核交易序號是否重複和驗TAC及MAC
                if (isQRPMain) {
                    // 豐錢包APP交易
                    getFiscBusiness().getFeptxn().setFeptxnZoneCode(ZoneCode.TWN);
                    // 檢核電子支付QRP交易是否重複
                    rtnCode = getFiscBusiness().checkOBQRPDup();
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }
                    // 豐錢包APP交易驗TAC
                    rtnCode = encHelper.checkAppTac(getFiscBusiness().getFeptxn().getFeptxnAtmType().substring(3, 4));
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }
                    // 檢核財金MAC
                    rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }
                } else {
                    // 晶片交易驗TAC
                    if (getFiscReq().getTAC().length() < 16) {// 長度小於16
                        getLogContext().setRemark("財金電文TAC的長度小於16位");
                        logMessage(Level.INFO, getLogContext());
                        return FISCReturnCode.LengthError;
                    }
                    if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnIcmark())
                            || StringUtils.leftPad("0", getTxData().getTxObject().getINBKRequest().getICMARK().length(), '0').equals(getTxData().getTxObject().getINBKRequest().getICMARK())) {
                        getLogContext().setRemark(StringUtils.join("財金電文ICMARK為空白或全為0=", getTxData().getTxObject().getINBKRequest().getICMARK()));
                        logMessage(Level.INFO, getLogContext());
                        return FEPReturnCode.ICMARKError;
                    }
                    rtnCode = encHelper.checkFiscMacAndTac(getFiscReq().getMAC(), getTxData().getMsgCtl().getMsgctlTacType());
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }
                }
            } else {
                // 跨境電子支付退款交易(2556)，只檢核MAC
                rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            // (3) 拒絕預借現金交易
            if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnTroutKind())) {
                getLogContext().setRemark("轉出帳號不得為信用卡號");
                logMessage(Level.INFO, getLogContext());
                return FISCReturnCode.CCardServiceNotAllowed;
            }

            // (4) 檢核 Card Status
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()) && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnIcTac())) {
                // 2019/08/19 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：豐錢包APP交易，不檢核卡檔
                // 2019/08/19 Modify by Ruling for 數位帳戶上線後調整：豐錢包APP交易，增加檢核數位帳戶單筆限額
                if (isQRPMain) {
                    // 豐錢包APP交易，不檢核卡檔改送主機查ID及數位記號
                    T24 hostT24 = new T24(getTxData());
                    rtnCode = hostT24.sendToT24(T24Version.B0001, (byte) T24TxType.Accounting.getValue(), true);
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    } else {
                        if (getTxData().getT24Response() != null && getTxData().getT24Response().getTotaEnquiryContents().size() > 0) {
                            // 主機回傳IDNO先寫入FEPTXN於P33核驗身份時使用
                            if (getTxData().getT24Response().getTotaEnquiryContents().get(0).containsKey("IDNO")
                                    && StringUtils.isNotBlank(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("IDNO"))) {
                                getFiscBusiness().getFeptxn().setFeptxnIdno(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("IDNO"));
                            } else {
                                getLogContext().setRemark(StringUtils.join("T24.", T24Version.B0001, "下行電文無IDNO欄位或為空值"));
                                logMessage(Level.INFO, getLogContext());
                                return FEPReturnCode.OtherCheckError;
                            }

                            // 檢核數位帳戶單筆限額
                            if (getTxData().getT24Response().getTotaEnquiryContents().get(0).containsKey("DIGITAL.FG")
                                    && (StringUtils.isNotBlank(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("DIGITAL.FG")))) {
                                rtnCode = getFiscBusiness().checkDGLimit(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("DIGITAL.FG"));
                                if (rtnCode != CommonReturnCode.Normal) {
                                    return rtnCode;
                                }
                            }
                        }
                    }
                } else {
                    // 晶片卡交易，檢核卡檔
                    if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnMajorActno())) {
                        getLogContext().setRemark("卡片帳號為空白或NULL");
                        logMessage(Level.INFO, getLogContext());
                        return FEPReturnCode.NotICCard;
                    }

                    // 一般金融卡
                    rtnCode = getFiscBusiness().checkCardStatus();
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }

                    // 海外卡不提供跨國晶片卡交易
                    if (!ZoneCode.TWN.equals(getFiscBusiness().getFeptxn().getFeptxnZoneCode())) {
                        getLogContext().setRemark(StringUtils.join("海外卡不提供跨國晶片卡交易, FEPTXN_ZONE_CODE=", getFiscBusiness().getFeptxn().getFeptxnZoneCode()));
                        logMessage(Level.INFO, getLogContext());
                        return FISCReturnCode.CCardServiceNotAllowed; // 4501:該卡片之帳戶為問題帳戶
                    }

                    // 客戶必須申請約轉(2)或不限定約轉(3)才可執行跨境電子支付交易
                    if (getFiscBusiness().getCard().getCardTfrFlag() != 2 && getFiscBusiness().getCard().getCardTfrFlag() != 3) {
                        getLogContext().setRemark(StringUtils.join("客戶必須申請約轉(2)或不限定約轉(3), CARD_TFR_FLAG=", getFiscBusiness().getCard().getCardTfrFlag().toString()));
                        logMessage(Level.INFO, getLogContext());
                        return FEPReturnCode.NotApplyTransferActno; // 4508:該帳戶未申請轉帳約定
                    }

                    // 2019/08/19 Modify by Ruling for 數位帳戶上線後調整：晶片卡交易，檢核數位帳戶跨境電子支付單筆限額
                    // 晶片卡交易，檢核數位帳戶跨境電子支付單筆限額
                    if (StringUtils.isNotBlank(getFiscBusiness().getCard().getCardDigitalactno())) {
                        rtnCode = getFiscBusiness().checkDGLimit(getFiscBusiness().getCard().getCardDigitalactno());
                        if (rtnCode != CommonReturnCode.Normal) {
                            return rtnCode;
                        }
                    }
                }
            }

            // (5) 檢核和更新原始交易狀態(for 2556)
            if (FISCPCode.PCode2556.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
                rtnCode = checkoriOBTLTXN();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 檢核和更新原始交易狀態
     *
     * @return
     */
    private FEPReturnCode checkoriOBTLTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            oriOBTLTXN = new Obtltxn();
            oriOBTLTXN.setObtltxnTbsdyFisc(getFiscBusiness().getFeptxn().getFeptxnDueDate());
            oriOBTLTXN.setObtltxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
            oriOBTLTXN.setObtltxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
            Obtltxn obtltxn = dbOBTLTXN.queryOBTLXNByStan(oriOBTLTXN.getObtltxnTbsdyFisc(), oriOBTLTXN.getObtltxnBkno(), oriOBTLTXN.getObtltxnStan());
            if (obtltxn == null) {
                rtnCode = FEPReturnCode.CheckFieldError;
                getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // N：無帳務沖正
                getLogContext().setRemark(StringUtils.join(
                        "QueryOBTLXNByStan 找不到原交易, OBTLTXN_TBSDY_FISC=", oriOBTLTXN.getObtltxnTbsdyFisc(),
                        ", OBTLTXN_BKNO=", oriOBTLTXN.getObtltxnBkno(),
                        ",  FEPTXN_ORI_STAN=", oriOBTLTXN.getObtltxnStan()));
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.INFO, getLogContext());
                transactionManager.commit(txStatus);
                return rtnCode;
            }
            oriOBTLTXN = obtltxn;
            // 檢核原交易是否成功
            if (!FeptxnTxrust.Successed.equals(oriOBTLTXN.getObtltxnTxrust())
                    && !FeptxnTxrust.Pending.equals(oriOBTLTXN.getObtltxnTxrust())
                    && !FeptxnTxrust.ReverseSuccessed.equals(oriOBTLTXN.getObtltxnTxrust())) {
                rtnCode = FEPReturnCode.CheckFieldError; // 交易狀態有誤
                getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.OriTxnRejected); // I：原交易已拒絕
                // 2019/06/24 Modify by Ruling for 修改Log說明避免發生Exception
                getLogContext().setRemark(StringUtils.join("原交易不成功(A or B or D), FEPTXN_TXRUST=", oriOBTLTXN.getObtltxnTxrust()));
                // LogContext.Remark = String.Format("原交易不成功(A or B or D), FEPTXN_TXRUST={0}", fiscBusiness.OriginalFEPTxn.FEPTXN_TXRUST)
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.INFO, getLogContext());
                transactionManager.commit(txStatus);
                return rtnCode;
            } else {
                oriTXRUST = oriOBTLTXN.getObtltxnTxrust(); // 保留原交易狀態
            }
            // 比對原交易資料
            if (!oriOBTLTXN.getObtltxnMerchantId().equals(defOBTLTXN.getObtltxnMerchantId())
                    || !oriOBTLTXN.getObtltxnSetCur().equals(defOBTLTXN.getObtltxnSetCur())
                    || !oriOBTLTXN.getObtltxnSetExrate().equals(defOBTLTXN.getObtltxnSetExrate())
                    || !oriOBTLTXN.getObtltxnTbsdyFisc().equals(defOBTLTXN.getObtltxnOriTxDate())
                    || !oriOBTLTXN.getObtltxnOrderNo().equals(defOBTLTXN.getObtltxnOriOrderNo())
                    || !FISCPCode.PCode2555.getValueStr().equals(oriOBTLTXN.getObtltxnPcode())) {
                rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
                getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // N：無帳務沖正
                getLogContext().setRemark(StringUtils.join(
                        "與原交易欄位不相同, MERCHANT_ID=", defOBTLTXN.getObtltxnMerchantId(),
                        " 原MERCHANT_ID=", oriOBTLTXN.getObtltxnMerchantId(),
                        ", SET_CUR=", defOBTLTXN.getObtltxnSetCur(),
                        " 原SET_CUR=", oriOBTLTXN.getObtltxnSetCur(),
                        ", SET_EXRATE=", defOBTLTXN.getObtltxnSetExrate(),
                        " 原SET_EXRATE=", oriOBTLTXN.getObtltxnSetExrate(),
                        ", TX_DATE=", defOBTLTXN.getObtltxnOriTxDate(),
                        " 原TX_DATE=", oriOBTLTXN.getObtltxnTbsdyFisc(),
                        ", ORDER_NO=", defOBTLTXN.getObtltxnOriOrderNo(),
                        " 原ORDER_NO=", oriOBTLTXN.getObtltxnOrderNo(),
                        ", 原PCODE <> 2555, 原PCODE=", oriOBTLTXN.getObtltxnPcode()));
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.INFO, getLogContext());
                transactionManager.commit(txStatus);
                return rtnCode;
            }
            // 比對退貨日期-交易日期 > 360天
            RefBase<Calendar> txdate = new RefBase<>(null);
            RefBase<Calendar> orTxdate = new RefBase<>(null);
            if (!CalendarUtil.validateDateTime(defOBTLTXN.getObtltxnTxDatetimeFisc(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN, txdate)) {
                rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
                getLogContext().setRemark(StringUtils.join("比對退貨期限-交易日，轉換日期格式有誤, txdate:", defOBTLTXN.getObtltxnTbsdyFisc()));
                logMessage(Level.INFO, getLogContext());
                transactionManager.commit(txStatus);
                return rtnCode;
            }
            if (!CalendarUtil.validateDateTime(defOBTLTXN.getObtltxnTxDatetimeFisc(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN, orTxdate)) {
                rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
                getLogContext().setRemark(StringUtils.join("比對退貨期限-原交易日，轉換日期格式有誤, orTxdate:", oriOBTLTXN.getObtltxnTbsdyFisc()));
                logMessage(Level.INFO, getLogContext());
                transactionManager.commit(txStatus);
                return rtnCode;
            }
            if (CalendarUtil.getDayPeriod(txdate.get(), orTxdate.get()) > 360) {
                rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
                getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // N：無帳務沖正
                getLogContext().setRemark("退貨日期-交易日期 > 360天");
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.INFO, getLogContext());
                transactionManager.commit(txStatus);
                return rtnCode;
            }
            // 比對退貨金額是否大於原交易金額
            if (defOBTLTXN.getObtltxnTotTwdAmt().doubleValue() > (oriOBTLTXN.getObtltxnTotTwdAmt().doubleValue() - oriOBTLTXN.getObtltxnTotRetAmt().doubleValue())) {
                rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
                getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // N：無帳務沖正
                getLogContext().setRemark("退貨金額大於原交易金額");
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.INFO, getLogContext());
                transactionManager.commit(txStatus);
                return rtnCode;
            }
            oriOBTLTXN.setObtltxnTxrust(FeptxnTxrust.Processing); // T：沖銷或授權完成進行中
            dbOBTLTXN.updateByPrimaryKeySelective(oriOBTLTXN);
            transactionManager.commit(txStatus);
            getFiscBusiness().getFeptxn().setFeptxnTroutActno(oriOBTLTXN.getObtltxnTroutActno());
            getFiscBusiness().getFeptxn().setFeptxnTroutKind(oriOBTLTXN.getObtltxnTroutKind());
            getFiscBusiness().getFeptxn().setFeptxnMajorActno(oriOBTLTXN.getObtltxnMajorActno());
            getFiscBusiness().getFeptxn().setFeptxnCardSeq(oriOBTLTXN.getObtltxnCardSeq());
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkoriOBTLTXN"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 帳務主機處理(SendToCBS/ASC(if need))
     *
     * @return
     */
    private FEPReturnCode sendToCBSAndAsc() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        T24 hostT24 = new T24(getTxData());
        byte TxType = 0;
        boolean ProcessTag = false;

        try {
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
                if (getTxData().getMsgCtl().getMsgctlCbsFlag() != 0) {
                    TxType = getTxData().getMsgCtl().getMsgctlCbsFlag().byteValue();
                    ProcessTag = true;
                } else {
                    TxType = 1;
                    ProcessTag = false;
                }

                rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, ProcessTag);

                if (rtnCode != CommonReturnCode.Normal) {
                    if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
                        strFISCRc = rtnCode;
                        return CommonReturnCode.Normal;
                    } else {
                        getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                        getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); // S Reject-abnormal
                        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
                        getFiscBusiness().updateTxData();
                        return rtnCode;
                    }
                }
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBSAndAsc"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 首次身分驗證狀況(P33)
     *
     * @return
     */
    private FEPReturnCode sendToCBSAndP33() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        T24 hostT24 = new T24(getTxData());
        try {
            // 2019/08/28 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：豐錢包APP主掃交易於商業邏輯檢核已有送B0001查詢ID及數位記號，不需重複送，非豐錢包APP主掃交易才要送主機查詢ID
            if (!isQRPMain) {
                // 送至T24主機取得身份證號
                rtnCode = hostT24.sendToT24(T24Version.B0001, (byte) T24TxType.Accounting.getValue(), true);
                if (rtnCode == CommonReturnCode.CBSResponseError) {
                    rtnCode = CommonReturnCode.P33IssuerReject;
                }
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }

                if (getTxData().getT24Response() != null && getTxData().getT24Response().getTotaEnquiryContents().size() > 0) {
                    if (getTxData().getT24Response().getTotaEnquiryContents().get(0).containsKey("IDNO")
                            && StringUtils.isNotBlank(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("IDNO"))) {
                        getFiscBusiness().getFeptxn().setFeptxnIdno(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("IDNO"));
                    } else {
                        getLogContext().setRemark("SendToCBSAndP33-AA拆解T24下行電文, IDNO無此欄位或為空值");
                        logMessage(Level.INFO, getLogContext());
                        return CommonReturnCode.P33IssuerReject;
                    }
                } else {
                    getLogContext().setRemark("SendToCBSAndP33-AA收到NULL的T24下行電文");
                    logMessage(Level.INFO, getLogContext());
                    return CommonReturnCode.P33IssuerReject;
                }
            }

            // 執行首次身分驗證狀況(P33)
            rtnCode = getFiscBusiness().sendToP33();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBSAndP33"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 組回傳財金Response電文
     *
     * @return
     */
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        if (strFISCRc != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(strFISCRc.getValue());
            if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                getLogContext().setProgramName(ProgramName);
                getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(strFISCRc, FEPChannel.FISC, getLogContext()));
            }
        } else {
            getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
        }

        // 退款交易之原帳戶已銷戶/關閉，仍應回覆財金交易成功
        if (FISCPCode.PCode2556.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())
                && INBKConfig.getInstance().getOBRT24ErrCode().equals(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
            getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
            // Fly 2019/1/22 (週二) 上午 09:16 調整收到CON後才能發送MAIL
            // fiscBusiness.PrepareOBNotifyMAIL(fiscBusiness.FepTxn.FEPTXN_REQ_DATETIME, fiscBusiness.FepTxn.FEPTXN_STAN, fiscBusiness.FepTxn.FEPTXN_TX_AMT)
        }

        rtnCode = getFiscBusiness().prepareHeader("0210");
        if (rtnCode != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
        }

        rtnCode = prepareBody();

        rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
        if (rtnCode != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
            getFiscRes().setBitMapConfiguration("0000000000000000");
        }

        rtnCode = getFiscRes().makeFISCMsg();

        return rtnCode;
    }

    /**
     * 組財金電文Body部份
     *
     * @return
     */
    private FEPReturnCode prepareBody() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String wk_BITMAP = null;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        try {
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {// +REP
                // 跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組
                wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
            } else {// -REP
                wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
            }

            for (int i = 2; i <= 63; i++) {
                if (wk_BITMAP.charAt(i) == '1') {
                    switch (i) {
                        case 2: // 交易金額
                            getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString());
                            break;
                        case 5: // 代付單位 CD/ATM 代號
                            getFiscRes().setATMNO(getFiscBusiness().getFeptxn().getFeptxnAtmno());
                            break;
                    }
                }
            }

            // 產生 MAC
            RefString refMac = new RefString(getFiscRes().getMAC());
            rtnCode = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
            getFiscRes().setMAC(refMac.get());
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                getFiscRes().setMAC("00000000");
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareBody"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * UpdateTxData部份
     *
     * @return
     */
    private FEPReturnCode updateTxData() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // (1) 更新 FEPTXN
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
                    getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
                    getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); // B Pending
                } else {// (2 way)
                    getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // A 成功
                }

                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    isEC = false;
                    getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
                }
            } else if (FeptxnTxrust.Initial.equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
                getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); // R 拒絕
            }

            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
            getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /// *AA Close*/

            rtnCode = getFiscBusiness().updateTxData();
            if (rtnCode != CommonReturnCode.Normal) {
                // 若更新失敗則不送回應電文, 人工處理
                transactionManager.rollback(txStatus);
                return rtnCode;
            }

            // (2) 判斷是否需更新 OBTLTXN
            if (isOB) {
                defOBTLTXN.setObtltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
                defOBTLTXN.setObtltxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
                defOBTLTXN.setObtltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
                defOBTLTXN.setObtltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
                defOBTLTXN.setObtltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
                defOBTLTXN.setObtltxnMajorActno(getFiscBusiness().getFeptxn().getFeptxnMajorActno());
                defOBTLTXN.setObtltxnCardSeq(getFiscBusiness().getFeptxn().getFeptxnCardSeq());
                defOBTLTXN.setObtltxnOriStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
                if (FISCPCode.PCode2556.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                        && INBKConfig.getInstance().getOBRT24ErrCode().equals(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
                    // 銷戶後退款， 將原交易績效行搬回
                    getFiscBusiness().getFeptxn().setFeptxnBrno(oriOBTLTXN.getObtltxnBrno());
                    defOBTLTXN.setObtltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
                }
                if (dbOBTLTXN.updateByPrimaryKeySelective(defOBTLTXN) < 1) {
                    // 若更新失敗則不送回應電文, 人工處理
                    transactionManager.rollback(txStatus);
                    getLogContext().setRemark("updateTxData-更新OBTLTXN失敗");
                    logMessage(Level.INFO, getLogContext());
                    return IOReturnCode.UpdateFail;
                }
            }

            // (3) 判斷是否需更新原始交易 for 2556
            if (FISCPCode.PCode2556.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
                if (oriOBTLTXN != null) {
                    if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                        oriOBTLTXN.setObtltxnTxrust(FeptxnTxrust.ReverseSuccessed); // D 已沖正成功

                        if (dbOBTLTXN.updateByPrimaryKeySelective(oriOBTLTXN) < 1) {
                            // 若更新失敗則不送回應電文, 人工處理
                            transactionManager.rollback(txStatus);
                            getLogContext().setRemark("updateTxData-2556+Rep更新oriOBTLTXN失敗");
                            logMessage(Level.INFO, getLogContext());
                            return IOReturnCode.UpdateFail;
                        }
                    } else {// -REP
                        if (FeptxnTxrust.Processing.equals(oriOBTLTXN.getObtltxnTxrust())) {// T 進行中for沖銷
                            oriOBTLTXN.setObtltxnTxrust(oriTXRUST); // 將原始交易之狀態還原
                            if (dbOBTLTXN.updateByPrimaryKeySelective(oriOBTLTXN) < 1) {
                                // 若更新失敗則不送回應電文，人工處理
                                transactionManager.rollback(txStatus);
                                getLogContext().setRemark("updateTxData-2556-Rep更新oriOBTLTXN失敗");
                                logMessage(Level.INFO, getLogContext());
                                return IOReturnCode.UpdateFail;
                            }
                        }
                    }
                }
            }
            transactionManager.commit(txStatus);
            return rtnCode;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 更新跨行代收付
     *
     * @return
     */
    private FEPReturnCode processAPTOT() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            rtnCode = getFiscBusiness().processOBAptot(isEC);
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                getFiscBusiness().updateTxData();
            }
        }
        return rtnCode;
    }
}
