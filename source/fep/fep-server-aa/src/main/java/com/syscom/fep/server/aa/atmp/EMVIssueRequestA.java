package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.mapper.HotbinMapper;
import com.syscom.fep.mybatis.mapper.HotcardMapper;
import com.syscom.fep.mybatis.model.Hotbin;
import com.syscom.fep.mybatis.model.Hotcard;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.ATMAdapter;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_WW1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_WW1APN;

public class EMVIssueRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
    String rtnMessage = StringUtils.EMPTY;
    private Intltxn intlTxn = new Intltxn(); // 國際卡檔
    private Intltxn oriintlTxn = new Intltxn(); // 國際卡檔
    @SuppressWarnings("unused")
    private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);
    @SuppressWarnings("unused")
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
    private boolean needResponseMsg = true;

    // AA的建構式,在這邊初始化及設定其他相關變數
    // @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
    // 初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
    // ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
    // FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
    public EMVIssueRequestA(ATMData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() {
        String rtnMessage = "";
        try {
			getFiscBusiness().getFISCTxData().setFiscTeleType(FISCSubSystem.EMVIC);
            // 1. 交易記錄初始資料
            _rtnCode = getATMBusiness().PrepareFEPTXN_EMV();

            // 2. 新增交易記錄(FEPTXN)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = this.addTxData();
            }

            // 3. 商業邏輯檢核
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = checkBusinessRule();
            }

            // 4. 組送往 FISC 之 Request 電文並等待財金之 Response
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendEMVRequestToFISC(getATMRequest());
            }

            // 5. CheckResponseFromFISC:檢核回應電文是否正確
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().checkEMVResponseMessage();
            }

            if ((_rtnCode != CommonReturnCode.Normal) ||
                    (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))) { // -REP
            } else {
                //銀聯檢查BITMAP63
                if (StringUtils.isBlank(getFiscEMVICRes().getIcCheckdata())) {
                    logContext.setRemark("財金REP未包含BITMAP63(IC_CHECKDATA)!!");
                    logMessage(Level.INFO, logContext);
                }
                if (StringUtils.isBlank(getFiscEMVICRes().getIcCheckresult())) {
                    logContext.setRemark("財金REP未包含BITMAP60(IC_CHECKRESULT)!!");
                    logMessage(Level.INFO, logContext);
                }
            }

            // 6. Prepare國際卡交易(INTLTXN)記錄(if need)
            // 當財金回-response時，ORI_DATA的值為０，為避免電文內的日期轉換有誤，多加fisc reprc=4001的條件成立才組Prepare Intltxn
            if ((NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))
                    && StringUtils.isNotBlank(getFiscEMVICRes().getOriData())) {
                RefBase<Intltxn> intltxnRefBase = new RefBase<>(intlTxn);
                RefBase<Intltxn> oriintltxnRefBase = new RefBase<>(oriintlTxn);
                _rtnCode = getFiscBusiness().prepareIntltxnEMV(intltxnRefBase, oriintltxnRefBase, MessageFlow.Response);
                intlTxn = intltxnRefBase.get();
                oriintlTxn = oriintltxnRefBase.get();
            }

            // 7. ProcessAPTOT:更新跨行代收付
            if (_rtnCode == CommonReturnCode.Normal
                    && (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))
                    && DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
                _rtnCode = getFiscBusiness().processAptot(false);

            }

            // 8. SendToCBS: 代理提款-進帳務主機掛現金帳
            if (_rtnCode == CommonReturnCode.Normal
                    && (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))) {
                _rtnCode = sendToCBS();
                if (_rtnCode != FEPReturnCode.Normal) {
                    if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                        /* 沖回跨行代收付(APTOT) */
                        _rtnCode2 = getFiscBusiness().processAptot(true);
                    }
                }
            }

            // 9. label_END_OF_FUNC
            if(!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
                rtnMessage = this.labelEndOfFunc();
            }
            // 10. 組回應電文回給 ATM&組 CON 電文回財金
            _rtnCode2 = sendConfirm();

            // 11.新增國際卡交易(INTLTXN)記錄(if need)
            if ((NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))
                    && StringUtils.isNotBlank(getFiscEMVICRes().getOriData())) {
                intlTxn.setIntltxnTxrust(getFeptxn().getFeptxnTxrust());
                _rtnCode2 = getFiscBusiness().insertINTLTxn(intlTxn);
            }

        } catch (
                Exception ex) {
            rtnMessage = "";
            _rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "." + "processRequestData");
            sendEMS(getLogContext());
        }

        try {
            // 12.更新交易記錄(FEPTXN)
            this.updateFEPTXN();

            getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getATMtxData().getLogContext().setMessage(rtnMessage);
            getATMtxData().getLogContext().setProgramName(this.aaName);
            getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
            getLogContext().setProgramName(this.aaName);
            logMessage(Level.DEBUG, getLogContext());
        } catch (
                Exception ex) {
            rtnMessage = "";
            _rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "." + "processRequestData");
            sendEMS(getLogContext());
        }

        // 先組回應ATM 故最後return空字串
        return rtnMessage;
    }

    /**
     * 商業邏輯檢核
     *
     * @return
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtn = FEPReturnCode.Normal;
        try {
            rtn = checkRequestFromATM(getATMtxData());
            if (rtn != FEPReturnCode.Normal) {
                return rtn;
            }

            /* 檢核ATM MAC電文訊息押碼(MAC) */
            String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
            if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
    			return FEPReturnCode.ENCCheckMACError; // GO TO label_END_OF_FUNC /* 組回傳 ATM 電文 */
    		}

            rtn = new ENCHelper(getTxData())
    				.checkAtmMac(StringUtils.substring(getATMtxData().getTxRequestMessage(), 18, 375), ATM_REQ_PICCMACD); // EBCDIC(36,750)
            
            if (rtn != FEPReturnCode.Normal) {
                return rtn;  // GO TO label_END_OF_FUNC /* 組回傳 ATM 電文 */
            }
    		
            /* 檢核ATM電文 POSENTRYMODE */
            if (StringUtils.isBlank(getATMRequest().getPIPOSENT())) {
                logContext.setRemark("ATM上來的PIPOSENT值為空白");
                logMessage(Level.INFO, logContext);
                return FEPReturnCode.OtherCheckError;
            }

            return rtn;
        } catch (Exception ex) {
            // 異常時要Return ProgramException
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "." + "checkBusinessRule");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    // 檢核偽卡資料
//    private FEPReturnCode checkHotBin() {
//        String pan = "";
//        String bin = "";
//        @SuppressWarnings("unused")
//        Hotbin defHOTBIN = new Hotbin();
//        HotbinMapper dbHOTBIN = SpringBeanFactoryUtil.getBean(HotbinMapper.class);
//        @SuppressWarnings("unused")
//        Hotcard defHOTCARD = new Hotcard();
//        HotcardMapper dbHOTCARD = SpringBeanFactoryUtil.getBean(HotcardMapper.class);
//        try {
//            if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrk2())) {
//                if (!getFeptxn().getFeptxnTrk2().contains("=")) {
//                    return ATMReturnCode.Track2Error;
//                }
//                pan = getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")).trim();
//                if (pan.length() < 6) {
//                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
//                    logContext.setRemark("TRK2取=前的長度不能小於6位，值為=" + pan);
//                    logMessage(Level.INFO, logContext);
//                    return ATMReturnCode.Track2Error;
//                }
//
//                bin = pan.substring(0, 6);
//
//                // 檢核HOTCARD
//                if (dbHOTCARD.selectByPrimaryKey(pan) != null) {
//                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
//                    logContext.setRemark("在HOTCARD檔有找到資料，PAN_NO=" + pan);
//                    logMessage(Level.INFO, logContext);
//                    return ATMReturnCode.Track2Error;
//                }
//
//                // 檢核HOTBIN
//                if (dbHOTBIN.selectByPrimaryKey(bin) != null) {
//                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
//                    logContext.setRemark("在HOTBIN檔有找到資料，BIN_NO=" + bin);
//                    logMessage(Level.INFO, logContext);
//                    return ATMReturnCode.Track2Error;
//                }
//
//                // Fly 2017/11/27 增加防呆處理, 避免手續費記號不相符, 影響帳務
//                if (FISCPCode.PCode2620.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2622.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
//                    String CHARGEFG = "";
//                    BigDecimal ACFEE = new BigDecimal(0);
//                    RefBase<BigDecimal> bigDecimalRefBase = new RefBase<>(ACFEE);
//                    RefString chargefg = new RefString(CHARGEFG);
//                    FEPReturnCode rtn = getFiscBusiness().checkEUVISABIN(bin, bigDecimalRefBase, chargefg);
//                    CHARGEFG = chargefg.get();
//                    ACFEE = bigDecimalRefBase.get();
//                    if (rtn.getValue() != FEPReturnCode.Normal.getValue()) {
//                        return rtn;
//                    }
//                    //--ben-20220922-//if (("N".equals(CHARGEFG) && "Y".equals(getATMRequest().getACFEE()))
//                    //--ben-20220922-//		|| ("Y".equals(CHARGEFG) && !"Y".equals(getATMRequest().getACFEE()) && !"N".equals(getATMRequest().getACFEE()))) {
//                    //--ben-20220922-//	getLogContext().setRemark("ATM送來的手續費記號[" + getATMRequest().getACFEE() + "] 與FEP查詢的手續費記號[" + CHARGEFG + "] 不一致");
//                    //--ben-20220922-//	logMessage(getLogContext());
//                    //--ben-20220922-//	return ATMReturnCode.OtherCheckError;
//                    //--ben-20220922-//}
//                }
//
//            }
//            return FEPReturnCode.Normal;
//        } catch (Exception e) {
//            // 2014/10/06 Modify by Ruling for 前端的ErrorCode仍是Normal，要改用Return
//            // _rtnCode = CommonReturnCode.ProgramException
//            getLogContext().setProgramException(e);
//            getLogContext().setProgramName(ProgramName + ".CheckHotBin");
//            sendEMS(getLogContext());
//            // 2014/10/06 Modify by Ruling for 前端的ErrorCode仍是Normal，要改用Return
//            return CommonReturnCode.ProgramException;
//        }
//    }

    // 檢核EMV晶片卡拒絶磁條卡提款或預借現金交易
//    private FEPReturnCode checkEMV() {
//        FEPReturnCode rtnCode = CommonReturnCode.Normal;
//
//        try {
//            if (FISCPCode.PCode2620.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2622.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
//                //--ben-20220922-//if ("N".equals(getATMRequest().getACFEE())){
//                // 客戶不同意收取處理費，交易拒絕
//                //--ben-20220922-//	getLogContext().setRemark("CheckEMV-客戶不同意收取處理費交易拒絕(E928)，ATMRequest.ACFEE=" + getATMRequest().getACFEE());
//                //--ben-20220922-//	logMessage(getLogContext());
//                //--ben-20220922-//	return ATMReturnCode.ACFeeReject;
//                //--ben-20220922-//}
//                //--ben-20220922-//else if ("Y".equals(getATMRequest().getACFEE())){
//                // 客戶已同意收取處理費
//                //--ben-20220922-//	rtnCode = checkACFEE();
//                //--ben-20220922-//	if (rtnCode != CommonReturnCode.Normal){
//                //--ben-20220922-//		return rtnCode;
//                //--ben-20220922-//	}
//                //--ben-20220922-//}
//            }
//            return rtnCode;
//        } catch (Exception ex) {
//            getLogContext().setProgramException(ex);
//            getLogContext().setProgramName(ProgramName);
//            sendEMS(getLogContext());
//            return CommonReturnCode.ProgramException;
//        }
//    }

    // 收取EMV晶片卡處理費
//    private FEPReturnCode checkACFEE() {
//        SysconfExtMapper dbSYSCONF = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
//        FEPReturnCode rtnCode = CommonReturnCode.Normal;
//        Sysconf defSYSCONF = dbSYSCONF.selectByPrimaryKey((short) 1, "EMVACFeeFlag");
//        try {
//            if (defSYSCONF == null) {
//                rtnCode = ATMReturnCode.OtherCheckError;
//                getLogContext().setRemark("CheckACFEE-在SYSCONF檔找不到EMVACFeeFlag的值");
//                logMessage(getLogContext());
//                return rtnCode;
//            } else {
//                if ("Y".equals(defSYSCONF.getSysconfValue())) {
//                    getFeptxn().setFeptxnRsCode("Y");
//                    getFeptxn().setFeptxnFeeCustpay(BigDecimal.valueOf(INBKConfig.getInstance().getEMVAccessFee()));
//                } else {
//                    rtnCode = ATMReturnCode.OtherCheckError;
//                    getLogContext().setRemark("CheckACFEE-在SYSCONF檔的EMVACFeeFlag其值不等於Y");
//                    logMessage(getLogContext());
//                    return rtnCode;
//                }
//            }
//            return rtnCode;
//
//        } catch (Exception ex) {
//            getLogContext().setProgramException(ex);
//            getLogContext().setProgramName(ProgramName);
//            sendEMS(getLogContext());
//            return CommonReturnCode.ProgramException;
//        }
//    }

    // EMV檢核限額
//    private FEPReturnCode checkEMVMLimit() {
//        FEPReturnCode rtnCode = CommonReturnCode.Normal;
//        @SuppressWarnings("unused")
//        String trk2SCode = "";
//
//        try {
//            // Fly 2016/04/07 應永豐要求取消銀聯卡(2600)月限額
//            if (FISCPCode.PCode2622.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2620.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2632.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2630.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
//                String serviceCode = StringUtils.rightPad(getATMBusiness().getFeptxn().getFeptxnTrk2(), 40, " ")
//                        .substring(getATMBusiness().getFeptxn().getFeptxnTrk2().indexOf("=") + 5, getATMBusiness().getFeptxn().getFeptxnTrk2().indexOf("=") + 6);
//                if (!DbHelper.toBoolean(getATMBusiness().getATMMSTR().getAtmEmv()) && ("2".equals(serviceCode) || "6".equals(serviceCode))) {
//                    getLogContext().setRemark("CheckEMVMLimit(EMV檢核限額)-需檢核  ATM_EMV:[" + getATMBusiness().getATMMSTR().getAtmEmv() + "] serviceCode:[" + serviceCode + "]");
//                    logMessage(getLogContext());
//                    rtnCode = getFiscBusiness().checkEMVMLimit();
//                    if (rtnCode != CommonReturnCode.Normal) {
//                        return rtnCode;
//                    }
//                } else {
//                    getLogContext().setRemark("CheckEMVMLimit(EMV檢核限額)-不需檢核  ATM_EMV:[" + getATMBusiness().getATMMSTR().getAtmEmv() + "] serviceCode:[" + serviceCode + "]");
//                    logMessage(getLogContext());
//                    return rtnCode;
//                }
//            }
//            return rtnCode;
//        } catch (Exception ex) {
//            getLogContext().setProgramException(ex);
//            getLogContext().setProgramName(ProgramName);
//            sendEMS(getLogContext());
//            return CommonReturnCode.ProgramException;
//        }
//    }

    /**
     * 代理提款-進帳務主機掛現金帳
     *
     * @return
     */
    private FEPReturnCode sendToCBS() {
        try {
            if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlCbsFlag())) {
                /* 進主機入扣帳/手續費 */
                String AATxTYPE = "1"; // 上CBS入扣帳
                String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
                _rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
                tota = hostAA.getTota();
                return _rtnCode;
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
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
            ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
            RefString rfs = new RefString();
            if (_rtnCode != FEPReturnCode.Normal) {
                ATM_FAA_WW1APC atm_faa_ww1apc = new ATM_FAA_WW1APC();
                // 組Header(OUTPUT-1)
                atm_faa_ww1apc.setWSID(atmReq.getWSID());
                atm_faa_ww1apc.setRECFMT("1");
                atm_faa_ww1apc.setMSGCAT("F");
                atm_faa_ww1apc.setMSGTYP("PC"); // - response
                atm_faa_ww1apc.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                atm_faa_ww1apc.setTRANTIME(systemTime.substring(8,14)); // 系統時間
                atm_faa_ww1apc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                atm_faa_ww1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
                // 財金回應代碼: 4305,4403,4412,4413 要留置卡片
                String repRc = feptxn.getFeptxnRepRc();
                if ("4305".equals(repRc) || "4403".equals(repRc) || "4412".equals(repRc) || "4413".equals(repRc)) {
                    atm_faa_ww1apc.setPRCRDACT("2"); // 留置卡片
                } else {
                    atm_faa_ww1apc.setPRCRDACT("4"); // 不處理
                }

                // 組D0(OUTPUT-2)畫面顯示(Display message)
                // 組 D0(004)
                atm_faa_ww1apc.setDATATYPE("D0");
                atm_faa_ww1apc.setDATALEN("004");
                atm_faa_ww1apc.setACKNOW("0");
                // 以CBS_RC取得轉換後的PBMDPO編號
                String pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
                        getTxData().getLogContext());
                // 其他未列入的代碼，一律回 226
                if (StringUtils.isBlank(pageNo)) {
                    pageNo = "226"; // 交易不能處理
                }
                atm_faa_ww1apc.setPAGENO(pageNo);

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                atm_faa_ww1apc.setPTYPE("S0");
                atm_faa_ww1apc.setPLEN("161");
                atm_faa_ww1apc.setPBMPNO("010000"); // FPC
                // 日期格式 :YYYYMMDD(不用轉換)
                atm_faa_ww1apc.setPDATE(feptxn.getFeptxnTxDateAtm());
                // 時間格式：HHMMSS 轉為HH:MM:SS
                atm_faa_ww1apc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
                atm_faa_ww1apc.setPTID(feptxn.getFeptxnAtmno());
                // 交易金額(格式：$$$,$$$,$$9)
                atm_faa_ww1apc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
                // 代理銀行別
                atm_faa_ww1apc.setPATXBKNO(feptxn.getFeptxnBkno());
                // 跨行交易序號
                atm_faa_ww1apc.setPSTAN(feptxn.getFeptxnStan());
                atm_faa_ww1apc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                        getTxData().getLogContext()));
                if (tota != null) {
                    //交易種類
                    atm_faa_ww1apc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                    //國際卡卡號(明細表顯示內容)
                    atm_faa_ww1apc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.EMVCARD.getValue()));
                    //轉出行
                    atm_faa_ww1apc.setPOTXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue()));
                }
                rfs.set("");
                rtnMessage = atm_faa_ww1apc.makeMessage();
                _rtnCode = atmEncHelper.makeAtmMac(rtnMessage, rfs);
                if (_rtnCode != FEPReturnCode.Normal) {
                    atm_faa_ww1apc.setMACCODE(""); /* 訊息押碼 */
                } else {
                    atm_faa_ww1apc.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                //VISA組織ARPC
                if(StringUtils.isNotBlank(getFiscEMVICRes().getIcCheckresult())){
                    atm_faa_ww1apc.setARPC(getFiscEMVICRes().getIcCheckresult());
                }
                rtnMessage = atm_faa_ww1apc.makeMessage();
            } else {
                ATM_FAA_WW1APN atm_faa_ww1apn = new ATM_FAA_WW1APN();
                // 組Header(OUTPUT-1)
                atm_faa_ww1apn.setWSID(atmReq.getWSID());
                atm_faa_ww1apn.setRECFMT("1");
                atm_faa_ww1apn.setMSGCAT("F");
                atm_faa_ww1apn.setMSGTYP("PN"); // + response
                atm_faa_ww1apn.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
                atm_faa_ww1apn.setTRANTIME(systemTime.substring(8,14)); // 系統時間
                atm_faa_ww1apn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
                atm_faa_ww1apn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
                // 財金回應代碼: 4305,4403,4412,4413 要留置卡片
                String repRc = feptxn.getFeptxnRepRc();
                if ("4305".equals(repRc) || "4403".equals(repRc) || "4412".equals(repRc) || "4413".equals(repRc)) {
                    atm_faa_ww1apn.setPRCRDACT("2"); // 留置卡片
                } else {
                    atm_faa_ww1apn.setPRCRDACT("4"); // 不處理
                }

                // 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
                atm_faa_ww1apn.setPTYPE("S0");
                atm_faa_ww1apn.setPLEN("161");
                atm_faa_ww1apn.setPBMPNO("000010"); // FPN
                // 日期格式 :YYYYMMDD(不用轉換)
                atm_faa_ww1apn.setPDATE(feptxn.getFeptxnTxDateAtm());
                // 時間格式：HHMMSS 轉為HH:MM:SS
                atm_faa_ww1apn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
                atm_faa_ww1apn.setPTID(feptxn.getFeptxnAtmno());
                // 交易金額(格式：$$$,$$$,$$9)
                atm_faa_ww1apn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
                // 代理銀行別
                atm_faa_ww1apn.setPATXBKNO(feptxn.getFeptxnBkno());
                // 跨行交易序號
                atm_faa_ww1apn.setPSTAN(feptxn.getFeptxnStan());

                atm_faa_ww1apn.setPRCCODE(feptxn.getFeptxnCbsRc());

                if (tota != null) {
                    //交易種類
                    atm_faa_ww1apn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                    //國際卡卡號(明細表顯示內容)
                    atm_faa_ww1apn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.EMVCARD.getValue()));
                    //轉出行
                    atm_faa_ww1apn.setPOTXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue()));
                }
                rfs.set("");
                rtnMessage = atm_faa_ww1apn.makeMessage();
                _rtnCode = atmEncHelper.makeAtmMac(rtnMessage, rfs);
                if (_rtnCode != FEPReturnCode.Normal) {
                    atm_faa_ww1apn.setMACCODE(""); /* 訊息押碼 */
                } else {
                    atm_faa_ww1apn.setMACCODE(rfs.get()); /* 訊息押碼 */
                }
                //VISA組織ARPC
                if(StringUtils.isNotBlank(getFiscEMVICRes().getIcCheckresult())){
                    atm_faa_ww1apn.setARPC(getFiscEMVICRes().getIcCheckresult());
                }
                rtnMessage = atm_faa_ww1apn.makeMessage();
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 組回應電文回給 ATM&組 CON 電文回財金
     *
     * @return
     */
    private FEPReturnCode sendConfirm() {
        String feptxnRepRc = feptxn.getFeptxnRepRc();
        if ((_rtnCode == FEPReturnCode.Normal) && ("4001".equals(feptxnRepRc) || "4007".equals(feptxnRepRc))) {
            feptxn.setFeptxnReplyCode("    "); // 4個SPACES
            feptxn.setFeptxnTxrust("B"); /* 成功 */
            feptxn.setFeptxnPending((short)2);
            feptxn.setFeptxnConRc("4001"); /* +CON */
            if(!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                _rtnCode2 = getFiscBusiness().sendConfirmToFISCEMV();
            }
        } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
            if ("4001".equals(feptxnRepRc) || "4007".equals(feptxnRepRc)) { /* +REP */
                if(!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())){
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()),
                            FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
                    feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
                    if(!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())){
                        feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()),
                                FEPChannel.FEP, FEPChannel.FISC, getTxData().getLogContext())); /* +CON */
                        _rtnCode2 = getFiscBusiness().sendConfirmToFISCEMV();
                    }
                }else{
                    feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FEP,
                            getTxData().getTxChannel(), getTxData().getLogContext()));
                }
            } else { /* -REP */
                feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
                        getTxData().getTxChannel(), getTxData().getLogContext()));
            }
        } else {
            /* FEPReturnCode <> Normal */
            /* 2020/3/6 修改，主機有回應錯誤時，修改交易結果 */
            feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */

            if (StringUtils.isBlank(feptxn.getFeptxnRepRc())) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()),
                        FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
            }
        }

        feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
        _rtnCode =updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
        return _rtnCode;
    }

    // 更新交易記錄檔
    private FEPReturnCode updateFEPTXN() {
        FEPReturnCode rtnCode = null;

        if (_rtnCode != CommonReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        } else if (_rtnCode2 != CommonReturnCode.Normal) {
            getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
        } else {
            getFeptxn().setFeptxnAaRc(CommonReturnCode.Normal.getValue());
        }

        getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /* AA Close */

        rtnCode = this.updateFeptxn();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    // 組回應電文
//    private FEPReturnCode sendToATM() {
//        ATMAdapter oatmAdapter = new ATMAdapter(getATMtxData());
//        @SuppressWarnings("unused")
//        FEPReturnCode rtncode = null;
//        try {
//            // 先送給ATM主機
//            //--ben-20220922-//oatmAdapter.setAtmNo(getATMRequest().getBRNO() + getATMRequest().getWSNO());
//            if (StringUtils.isBlank(getATMtxData().getTxResponseMessage()) && needResponseMsg) {
//                rtnMessage = prepareATMResponseData();
//            } else {
//                rtnMessage = getATMtxData().getTxResponseMessage();
//            }
//
//            if (FEPChannel.ATM.toString().equals(getFeptxn().getFeptxnChannel())
//                    || FEPChannel.WEBATM.toString().equals(getFeptxn().getFeptxnChannel())) {
//                if (StringUtils.isNotBlank(rtnMessage.trim())) {
//                    oatmAdapter.setMessageToATM(rtnMessage);
//                    rtncode = oatmAdapter.sendReceive();
//                } else {
//                    getLogContext().setRemark("ATM組出來的回應電文為空字串");
//                    getLogContext().setProgramName(ProgramName);
//                    logMessage(Level.DEBUG, getLogContext());
//                }
//                // 因為先送給ATM了所以要將回應字串清成空字串
//                rtnMessage = "";
//            }
//            return _rtnCode;
//        } catch (Exception ex) {
//            getLogContext().setProgramException(ex);
//            getLogContext().setProgramName(ProgramName + "." + "sendToATM");
//            sendEMS(getLogContext());
//            return FEPReturnCode.ProgramException;
//        }
//    }

    /**
     * AddTxData: 新增交易記錄(FEPTxn)
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode addTxData() throws Exception {
        try {
            // 新增交易記錄(FEPTxn) Returning FEPReturnCode
            /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".addTxData"));
            int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
            if (insertCount <= 0) { // 新增失敗
                return FEPReturnCode.FEPTXNInsertError;
            }
        } catch (Exception ex) { // 新增失敗
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            return FEPReturnCode.FEPTXNInsertError;
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 更新feptxn
     *
     * @return
     */
    private FEPReturnCode updateFeptxn() {
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFeptxn"));
            int updCount = feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
            if (updCount < 1) {
                return FEPReturnCode.FEPTXNUpdateError;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
            return FEPReturnCode.FEPTXNUpdateError;
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 時間格式：HHmmss 轉為HH:mm:ss
     *
     * @param time
     * @return
     */
    private String formatTime(String time) {
        if (StringUtils.isBlank(time)) {
            return time;
        } else if (time.length() != 6) {
            time = StringUtils.leftPad(StringUtils.right(time, 6), 6, '0');
        }
        StringBuilder sb = new StringBuilder();
        boolean addColon = true;
        for (int i = 0; i < time.length(); i++) {
            if (addColon) {
                sb.append(':');
            }
            sb.append(time.charAt(i));
            addColon = !addColon;
        }
        return sb.substring(1);
    }
}
