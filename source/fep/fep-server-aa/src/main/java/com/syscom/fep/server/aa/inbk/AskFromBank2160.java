package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.Inbk2160ExtMapper;
import com.syscom.fep.mybatis.mapper.Inbk2160Mapper;
import com.syscom.fep.mybatis.model.Inbk2160;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vincent
 */
public class AskFromBank2160 extends INBKAABase {
    private String fiscResRC; // 財金RESPONSE電文的RC + Description
    private Inbk2160ExtMapper dbINBK2160 = SpringBeanFactoryUtil.getBean(Inbk2160ExtMapper.class);
    private Object tota = null;
    private FISC_INBK fiscINBKReq; // INBK REQ電文物件
    private List<Inbk2160> dsInbk2160 = new ArrayList<>();
    private Inbk2160Mapper inbk2160Mapper = SpringBeanFactoryUtil.getBean(Inbk2160Mapper.class);
    private ENCHelper encHelper;
    private Inbk2160ExtMapper inbk2160ExtMapper = SpringBeanFactoryUtil.getBean(Inbk2160ExtMapper.class);
    private Inbk2160 defINBK2160;
    private Inbk2160 requestInbk2160;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;

    public AskFromBank2160(INBKData txnData) throws Exception {
        super(txnData, "2160");
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";

        try {
            defINBK2160 = new Inbk2160();

            // 將收到的電文拆解放入物件中
            requestInbk2160 = new Inbk2160();
            String requestMessage = getINBKtxData().getTxRequestMessage();
            requestInbk2160.setInbk2160Pcode(requestMessage.substring(0, 4));
            requestInbk2160.setInbk2160TxDate(requestMessage.substring(4, 12));
            requestInbk2160.setInbk2160Ejfno(Integer.valueOf(requestMessage.substring(12)));

            // 1. 檢核財金跨行狀態
            rtnCode = getFiscBusiness().checkINBKStatus("2160", true);

            // 2. 讀取交易紀錄檔(INBK2160)
            if (rtnCode == CommonReturnCode.Normal) {
                rtnCode = readInbk2160();
            }

            // 3. 將INBK2160逐筆讀出並送至財金
            if (rtnCode == CommonReturnCode.Normal) {
                rtnCode = readDataAndSendToFISC();
            }

            // 8. 更新 INBK2160
            if (rtnCode == CommonReturnCode.Normal) {
                defINBK2160.setInbk2160AaRc("0");
            } else {
                defINBK2160.setInbk2160AaRc(rtnCode.toString());
            }
            defINBK2160.setInbk2160Pending((short) 2); /*取消Pending*/
            defINBK2160.setInbk2160Msgflow("F6"); /*已發動2160*/
            if ("00".equals(defINBK2160.getInbk2160PrcResult())) { /*成功*/
                defINBK2160.setInbk2160Txrust("A"); /* 處理結果=成功 */
            } else { /*處理結果-失敗*/
                defINBK2160.setInbk2160Txrust("R"); /*交易Reverse*/
            }
            dbINBK2160.updateByPrimaryKeySelective(defINBK2160);
            if (dbINBK2160.updateByPrimaryKeySelective(defINBK2160) < 1) { /*更新INBK2160失敗*/
                rtnCode = IOReturnCode.UpdateFail;
            }

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage(rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }

    /**
     * 2. 讀取交易紀錄檔(INBK2160)
     * @return
     */
    private FEPReturnCode readInbk2160() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            dsInbk2160 = inbk2160ExtMapper.selectOne(SysStatus.getPropertyValue().getSysstatHbkno(), requestInbk2160.getInbk2160TxDate(), requestInbk2160.getInbk2160Ejfno());
            if (dsInbk2160 != null) {
                return FEPReturnCode.Normal;
            } else {
                rtnCode = FEPReturnCode.FileNotExist;
                return rtnCode;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName + ".readInbk2160"));
            sendEMS(getLogContext());
            return IOReturnCode.QueryNoData;
        }
    }

    /**
     * 3. 將INBK2160逐筆讀出並送至財金
     * @return
     */
    private FEPReturnCode readDataAndSendToFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        int i = 0;
        int Inbk2160Count = 0;
        try {
            Inbk2160Count = dsInbk2160.size();

            for (i = 0; i < Inbk2160Count; i++) {
                setInbk2160(dsInbk2160.get(i));
                if (getInbk2160() == null) {
                    break;
                }

                // 產生 2160 財金通知電文
                rtnCode = prepareForFISC();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }

                // 異動交易記錄INBK2160
                rtnCode = updateTxDate();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
                defINBK2160 = getInbk2160();

                // 送Request電文至財金並等待回應
                RefBase<Inbk2160> inbk2160RefBase = new RefBase<Inbk2160>(defINBK2160);
                rtnCode = getFiscBusiness().sendInbk2160RequestToFISC(inbk2160RefBase);
                defINBK2160 = inbk2160RefBase.get();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }

                // 4. 檢核財金Response電文Header
                setFiscRes(getFiscBusiness().getFiscINBKRes());
                rtnCode = getFiscBusiness().checkHeader(getFiscRes(), false);

                if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError
                        || rtnCode == FISCReturnCode.STANError || rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
                    // Garbled Message
                    getFiscBusiness().sendGarbledMessage(fiscINBKReq.getEj(), rtnCode, getFiscRes());
                } else {
                    if (rtnCode != CommonReturnCode.Normal) {
                        defINBK2160.setInbk2160AaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
                        dbINBK2160.updateByPrimaryKeySelective(defINBK2160);
                    } else {
                        defINBK2160.setInbk2160RepRc(getFiscRes().getResponseCode());
                        defINBK2160.setInbk2160PrcResult("00"); /* 處理結果 */
                        if (dbINBK2160.updateByPrimaryKeySelective(defINBK2160) < 1) {
                            rtnCode = IOReturnCode.UpdateFail;
                            break;
                        }

                        // 5. 檢核訊息押碼(MAC)
                        // Prepare FEPTXN for DES
                        rtnCode = encHelper.checkFiscMac(fiscINBKReq.getMessageType(), fiscINBKReq.getMAC());
                        if (rtnCode != CommonReturnCode.Normal) {
                            defINBK2160.setInbk2160AaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
                        }
                        // 6. 檢核 ORI_STAN
                        if (!fiscINBKReq.getOriStan().equals(getFiscRes().getOriStan())) {
                            rtnCode = FISCReturnCode.OriginalMessageDataError; // 欄位 MAPPING 不符
                            defINBK2160.setInbk2160AaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
                        }
                        // 7. 檢核財金回應 RC
                        if (!NormalRC.FISC_OK.equals(getFiscRes().getResponseCode())) { // -REP
                            rtnCode = FEPReturnCode.parse(fiscINBKReq.getResponseCode());
                        } else {
                            rtnCode = CommonReturnCode.Normal;
                        }
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".readDataAndSendToFISC"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 異動交易記錄INBK2160
     *
     * @return
     */
    private FEPReturnCode updateTxDate() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            getInbk2160().setInbk2160Pending((short) 1);
            getInbk2160().setInbk2160Msgflow("F1");
            getInbk2160().setInbk2160AaRc("0601");
            getInbk2160().setInbk2160FiscTimeout((short) 1);
            getInbk2160().setInbk2160Txrust("S");
            Inbk2160 update2160 = getInbk2160();

            if (inbk2160Mapper.updateByPrimaryKey(update2160) < 1) {
                return IOReturnCode.FEPTXNUpdateError;
            } else {
                return rtnCode;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".preparePendHeader");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 產生 2160 財金通知電文
     *
     * @return
     */
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        fiscINBKReq = new FISC_INBK();
        try {
            // Prepare Header
            fiscINBKReq.setSystemSupervisoryControlHeader("00");
            fiscINBKReq.setSystemNetworkIdentifier("00");
            fiscINBKReq.setAdderssControlField("00");
            fiscINBKReq.setMessageType("0200");
            fiscINBKReq.setProcessingCode(getInbk2160().getInbk2160Pcode());
            fiscINBKReq.setSystemTraceAuditNo(getInbk2160().getInbk2160Stan());
            fiscINBKReq.setTxnDestinationInstituteId(StringUtils.rightPad(getInbk2160().getInbk2160DesBkno(), 7, '0'));
            fiscINBKReq.setTxnSourceInstituteId(StringUtils.rightPad(getInbk2160().getInbk2160Bkno(), 7, '0'));
            fiscINBKReq.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(getInbk2160().getInbk2160TxDate()).substring(1, 7) + getInbk2160().getInbk2160TxTime()); // (轉成民國年)
            fiscINBKReq.setResponseCode(getInbk2160().getInbk2160ReqRc());
            fiscINBKReq.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTcdsync());

            // Application Data Elements(ReqINBK)
            fiscINBKReq.setTxAmt(getInbk2160().getInbk2160TxAmt().toString());
            fiscINBKReq.setATMNO(getInbk2160().getInbk2160Atmno());
            String oriData = prepareOriDate(); // 組成送往財金電文
            fiscINBKReq.setOriData(oriData);
            fiscINBKReq.setTaxUnit(getInbk2160().getInbk2160Twmp());
            if (StringUtils.isNotBlank(getInbk2160().getInbk2160Chrem())) {
                fiscINBKReq.setCHREM(StringUtils.rightPad(getInbk2160().getInbk2160Chrem(), 80, '0'));
            } else {
                fiscINBKReq.setCHREM(StringUtils.rightPad("", 80, '0'));
            }
            fiscINBKReq.setTroutActno(getInbk2160().getInbk2160TroutActno());

            // 產生訊息押碼(MAC)
            getFeptxn().setFeptxnTxDate(getInbk2160().getInbk2160TxDate());
            getFeptxn().setFeptxnBkno(getInbk2160().getInbk2160Bkno());
            getFeptxn().setFeptxnStan(getInbk2160().getInbk2160Stan());
            getFeptxn().setFeptxnDesBkno(getInbk2160().getInbk2160DesBkno());
            getFeptxn().setFeptxnOriStan(getInbk2160().getInbk2160OriStan());
            getFeptxn().setFeptxnReqRc(getInbk2160().getInbk2160ReqRc());
            getFeptxn().setFeptxnTxAmtAct(getInbk2160().getInbk2160TxAmt());
            getFeptxn().setFeptxnReqDatetime(getInbk2160().getInbk2160TxDate() + getInbk2160().getInbk2160TxTime());

            encHelper = new ENCHelper(this.getFiscBusiness().getFeptxn(), this.getTxData());
            RefString mac = new RefString(fiscINBKReq.getMAC());
            rtnCode = encHelper.makeFiscMac(fiscINBKReq.getMessageType(), mac);
            fiscINBKReq.setMAC(mac.get());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            // Make Bit Map
            getFiscBusiness().setfisc(fiscINBKReq);
            rtnCode = getFiscBusiness().makeBitmap(fiscINBKReq.getMessageType(), fiscINBKReq.getProcessingCode(), MessageFlow.Request);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".preparePendHeader");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 組成送往財金電文
     *
     * @return
     * @throws Exception
     */
    private String prepareOriDate() throws Exception {
        try {
            String OriDate = StringUtils.repeat(" ", 195);
            StringBuilder ori = new StringBuilder(OriDate);
            ori.replace(0, 4, StringUtils.leftPad(getInbk2160().getInbk2160OriPcode(), 4, " "));
            ori.replace(4, 11, StringUtils.leftPad(getInbk2160().getInbk2160OriBkno() + "0000", 7, " "));
            ori.replace(11, 18, StringUtils.leftPad(getInbk2160().getInbk2160OriStan(), 7, " "));
            ori.replace(18, 32, StringUtils.leftPad(getInbk2160().getInbk2160OriTxDate() + getInbk2160().getInbk2160OriTxTime(), 14, "0"));
            if (StringUtils.isNotBlank(getInbk2160().getInbk2160OriTroutBkno7())) {
                ori.replace(32, 39, StringUtils.leftPad(getInbk2160().getInbk2160OriTroutBkno7(), 7, " "));
            } else {
                ori.replace(32, 39, StringUtils.leftPad(getInbk2160().getInbk2160TroutBkno() + "0000", 7, " "));
            }
            ori.replace(39, 47, StringUtils.leftPad(getInbk2160().getInbk2160OriIcSeqno(), 8, "0"));
            ori.replace(47, 51, StringUtils.leftPad(getInbk2160().getInbk2160OriIcdata(), 4, "0"));
            ori.replace(51, 55, StringUtils.leftPad(getInbk2160().getInbk2160OriRepRc(), 4, " "));
            if (StringUtils.isNotBlank(getInbk2160().getInbk2160RepRc())) {
                ori.replace(55, 59, StringUtils.leftPad(getInbk2160().getInbk2160RepRc(), 4, " "));
            } else {
                ori.replace(55, 59, StringUtils.leftPad("    ", 4, " "));
            }
            ori.replace(59, 66, StringUtils.leftPad(getInbk2160().getInbk2160OriTrinBkno7(), 7, " "));
            ori.replace(66, 82, StringUtils.leftPad(getInbk2160().getInbk2160OriTrinActno(), 7, " "));
            ori.replace(82, 86, StringUtils.rightPad(getInbk2160().getInbk2160OriAtmType(), 4, " "));
            ori.replace(86, 90, StringUtils.leftPad(getInbk2160().getInbk2160OriFeeCustpay().setScale(0, BigDecimal.ROUND_DOWN).toString(), 4, " "));
            if ("2541".equals(getInbk2160().getInbk2160OriPcode()) || "2542".equals(getInbk2160().getInbk2160OriPcode())
                    || "2543".equals(getInbk2160().getInbk2160OriPcode())) {
                ori.replace(90, 105, StringUtils.rightPad(getInbk2160().getInbk2160OriMerchantId(), 15, " "));
                ori.replace(105, 121, StringUtils.rightPad(getInbk2160().getInbk2160OriOrderNo(), 16, " "));
                ori.replace(121, 129, StringUtils.rightPad(getInbk2160().getInbk2160OriBarcode(), 8, " "));
                if (StringUtils.isNotBlank(getInbk2160().getInbk2160OriTxDate())) {
                    ori.replace(129, 143, StringUtils.leftPad(getInbk2160().getInbk2160OriTxnAmtCur().toString(), 14, " "));
                    ori.replace(143, 146, StringUtils.leftPad(getInbk2160().getInbk2160OriTxCur(), 3, " "));
                } else {
                    ori.replace(129, 143, StringUtils.repeat(" ", 14));
                    ori.replace(143, 146, StringUtils.repeat(" ", 3));
                }
            }
            return ori.toString();
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".preparePendHeader");
            sendEMS(getLogContext());
            return null;
        }
    }
}