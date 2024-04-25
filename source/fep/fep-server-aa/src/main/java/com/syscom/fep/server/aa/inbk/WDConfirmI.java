package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
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
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.host.App;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.FISCType;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 處理財金發動跨行提款確認電文
 *
 * @author bruce
 */
public class WDConfirmI extends INBKAABase {
    //	private Object tota = null;
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode1 = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    private boolean isExitProgram = false;

    /**
     * AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception
     */
    public WDConfirmI(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     */
    @Override
    public String processRequestData() {
        try {
            // 1.拆解並檢核財金電文
            _rtnCode = processRequest();
            if(_rtnCode != CommonReturnCode.Normal){
                return StringUtils.EMPTY;
            }

            // 2.商業邏輯檢核＆電文Body檢核
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = checkBusinessRule();
                if(isExitProgram){
                    return StringUtils.EMPTY;
                }
            }

            // 3.更新交易記錄 (FEPTXN & INTLTXN)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = updateTxData();
                if(_rtnCode != CommonReturnCode.Normal){
                    return StringUtils.EMPTY;
                }
            }

            // 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = processAPTOTSendToCBSASC();
            }

            //6.更新交易記錄 (查不到原始資料則不更新FEPTXN)
            _rtnCode = this.updateFEPTXN();

            //7.發送推播或簡訊或Email
            _rtnCode =sendToMailHunter();

            //8.判斷是否需傳送2160電文給財金
            if(StringUtils.isNotBlank(feptxn.getFeptxnSend2160()) && "Y".equals(feptxn.getFeptxnSend2160())){
                _rtnCode=insertINBK2160();
            }
            //9.FEP通知主機交易結束
            _rtnCode = SendToCBS();
        } catch (Exception e) {
            this._rtnCode = CommonReturnCode.ProgramException;
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(this.logContext);
        }
        return StringUtils.EMPTY;
    }
    private FEPReturnCode SendToCBS() throws Exception {
        FEPReturnCode rc2 = CommonReturnCode.Normal;
        /*沖轉主機帳務*/
        String AATxTYPE = "";
        String AATxRs = "N";
        String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1();
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
        rc2 = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE,AATxRs);
        return rc2;
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
     * 拆解並檢核財金電文
     *
     * @return
     */
    private FEPReturnCode processRequest() throws Exception {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            rtnCode = getFiscBusiness().checkHeader(getFiscCon(), true);
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().setFeptxn(null);
                getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
                return rtnCode;
            }

            getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
            getTxData().setFeptxn(getFiscBusiness().getFeptxn());

            this.logContext.setAtmNo(getFiscBusiness().getFeptxn().getFeptxnAtmno());
            this.logContext.setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
            this.logContext.setTrinBank(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
            this.logContext.setTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
            this.logContext.setTroutBank(getFiscBusiness().getFeptxn().getFeptxnTroutBkno());
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequest");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }


    /**
     * 商業邏輯檢核,電文Body檢核
     *
     * @return
     */
    private FEPReturnCode checkBusinessRule() throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        String wk_TX_DATE = "";

        try {
            //檢核 Mapping 欄位
            if (Integer.parseInt(this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 2)) < 90) {
                wk_TX_DATE = CalendarUtil.rocStringToADString("20110000" + this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6));
            } else {
                wk_TX_DATE = CalendarUtil.rocStringToADString("19110000" + this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6));
            }

            /* 9/9 修改, 改抓 FEPTXN_REQ_DATETIME欄位 */
            String aa=  (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0,6),7,"0"))) + this.getFiscCon().getTxnInitiateDateAndTime().substring(6,12);
            if(!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(aa) ||
                    (!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0,3))) ||
                    (StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))
                    ||(StringUtils.isNotBlank(this.getFiscCon().getTxAmt()) && !new DecimalFormat("0.00").format(getFiscBusiness().getFeptxn().getFeptxnTxAmt()).equals(new DecimalFormat("0.00").format(new BigDecimal(this.getFiscCon().getTxAmt()))))) {
                return FEPReturnCode.OriginalMessageDataError;
            }
            //檢核交易是否未完成
            if(!getFiscBusiness().getFeptxn().getFeptxnTxrust().equals("B")) {
                /* 10/20 修改, 財金錯誤代碼改為 ‘0101’ */
                return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
            }

            /* 9/22 修改 for CON 送2次 */
            if(!getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().equals(0)) {
                this.getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
                isExitProgram = true;
                return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
            }

            //檢核 MAC
            getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscCon().getResponseCode());
            /* 11/16 修改, 收到財金確認電文時間寫入FEPTXN */
            getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(),FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            rtnCode = encHelper.checkFiscMac(this.getFiscCon().getMessageType(), getFiscCon().getMAC());
            this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            if(rtnCode != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnConRc(null);
                return FEPReturnCode.ENCCheckMACError;//**訊息押碼錯誤
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 更新交易記錄 (FEPTXN)
     *
     * @return
     */
    private FEPReturnCode updateTxData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));/*AA Initial*/
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
                if (NormalRC.FISC_ATM_OK.equals(this.getFiscCon().getResponseCode())) { /*+CON*/
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); /*成功*/
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("C"); /*Accept-Reverse*/
                }
                getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); /*解除 PENDING */
            }

            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // 'F3  FISC CONFIRM
            getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
            getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());

            this.feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData")); // 2021-06-16 Richard add

            // 'add by maxine on 2012/03/05 for 換日後fiscBusiness.DBFEPTXN,以及txData.FEPTXN都應該使用實際交易日期,避免後面更新不到FEPTXN
            getTxData().setFeptxnDao(this.feptxnDao);
            getFiscBusiness().setFeptxnDao(this.feptxnDao);

            int i = this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn());
            if (i <= 0) {
                transactionManager.rollback(txStatus);
                _rtnCode = IOReturnCode.FEPTXNUpdateError;
                return _rtnCode;
            }

			/* 3/9 修改 for 無卡提款 */
            if (FISCPCode.PCode2510.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                    && "6071".equals(getFiscBusiness().getFeptxn().getFeptxnAtmType())) {
                /*須讀取 NWDTXN */
                Nwdtxn defNWDTXN = new Nwdtxn();
                NwdtxnMapper dbNWDTXN = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
                int iRes = 0;
                defNWDTXN.setNwdtxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
                defNWDTXN.setNwdtxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
                defNWDTXN.setNwdtxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
                defNWDTXN.setNwdtxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
                iRes = dbNWDTXN.updateByPrimaryKey(defNWDTXN);
                if (iRes <= 0) {
                    transactionManager.rollback(txStatus);
                    _rtnCode1 = IOReturnCode.UpdateFail;
                    return _rtnCode1;
                }
            }
            transactionManager.commit(txStatus);
        } catch (Exception ex) {
            // '若失敗則復原
            transactionManager.rollback(txStatus);
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 4. 	判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS)
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode processAPTOTSendToCBSASC() throws Exception {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                /*沖轉跨行代收付*/
                rtnCode = this.getFiscBusiness().processAptot(true);
                this.getLogContext().setProgramName(ProgramName);
                //*沖轉主機帳務*/
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
                    /* 沖轉主機帳務 */
                    String AATxTYPE = "2"; // 沖正
                    String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
                    ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
                    _rtnCode2 = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE);
                }
                logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(this.feptxn.getFeptxnConRc(), FEPChannel.FISC, logContext));
            }
        }
        return rtnCode;
    }

    /**
     * 6. 	更新交易記錄(FEPTXN) : if need
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTXN() throws Exception {
        if (getFiscBusiness().getFeptxn().getFeptxnAaRc().equals(FEPReturnCode.Normal.getValue())) {
            if(this._rtnCode != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode.getValue());
            }else if(this._rtnCode2 != FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode2.getValue());
            }
        }
        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
        if (this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn()) > 0) {
            return FEPReturnCode.Normal;
        }
        return FEPReturnCode.UpdateFail;
    }

    // ''' <summary>
    // ''' "拆解並檢核由財金發動的Request電文"
    // ''' </summary>
    // ''' <modify>
    // ''' <modifier>HusanYin</modifier>
    // ''' <reason>修正txData的feptxn與fiscbusiness同步問題</reason>
    // ''' <date>2010/12/07</date>
    // ''' </modify>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
//	private FEPReturnCode processRequest() {
//		FEPReturnCode rtnCode = CommonReturnCode.Normal;
//		try {
//			// 檢核Header
//			rtnCode = getFiscBusiness().checkHeader(getFiscCon(), true);
////			if (rtnCode == FEPReturnCode.MessageTypeError
////					|| rtnCode == FEPReturnCode.TraceNumberDuplicate
////					|| rtnCode == FEPReturnCode.OriginalMessageError
////					|| rtnCode == FEPReturnCode.STANError
////					|| rtnCode == FEPReturnCode.CheckBitMapError
////					|| rtnCode == FEPReturnCode.SenderIdError) {
//			if(rtnCode.toString().substring(0, 1) == "10") {
//				// 'modified by Maxine on 2011/09/19 for 查不到原始資料則不更新FEPTXN
//				//getFiscBusiness().setFeptxn(new FeptxnExt());
//				getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
//				return rtnCode;
//			}
//
//			getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
//			// 'for txdata can read originalFeptxn
//			getTxData().setFeptxn(getFiscBusiness().getFeptxn());
//
//			// '2015/07/29 Modify by Ruling for FISCRC=6101要寄Email需將值塞入LogContext.TroutActno
//			this.logContext.setAtmNo(getFiscBusiness().getFeptxn().getFeptxnAtmno());
//			this.logContext.setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
//			this.logContext.setTrinBank(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
//			this.logContext.setTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
//			this.logContext.setTroutBank(getFiscBusiness().getFeptxn().getFeptxnTroutBkno());
//		} catch (Exception e) {
//			this._rtnCode = CommonReturnCode.ProgramException;
//			this.logContext.setProgramException(e);
//			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequest"));
//			sendEMS(this.logContext);
//		}
//		return CommonReturnCode.Normal;
//	}

    // ''' <summary>
    // ''' "商業邏輯檢核,電文Body檢核"
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
//	private FEPReturnCode checkBusinessRule() {
//		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
//		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
//		String wk_TX_DATE = "";
//		try {
//			wk_TX_DATE = CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscCon().getTxnInitiateDateAndTime().substring(0, 6), 7, "0"));
//			// 'modified By Maxine on 2011/10/13 for 改抓 FEPTXN_REQ_DATETIME欄位
//			// 'If fiscBusiness.FepTxn.FEPTXN_TX_DATE.ToString() <> wk_TX_DATE OrElse _
//			// 'fiscBusiness.FepTxn.FEPTXN_TX_TIME <> fiscCon.TxnInitiateDateAndTime.Substring(6, 6) OrElse _
//			if (!wk_TX_DATE.equals(getFiscBusiness().getFeptxn().getFeptxnReqDatetime().substring(0, 8))
//					|| !getFiscBusiness().getFeptxn().getFeptxnReqDatetime().substring(8, 14).equals(getFiscCon().getTxnInitiateDateAndTime().substring(6, 12))
//					|| !getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(getFiscCon().getTxnDestinationInstituteId().substring(0, 3))
//					|| (StringUtils.isNotBlank(getFiscCon().getATMNO()) && !getFiscCon().getATMNO().equals(getFiscBusiness().getFeptxn().getFeptxnAtmno()))
//					|| (StringUtils.isNotBlank(getFiscCon().getTxAmt()) && MathUtil.compareTo(getFiscBusiness().getFeptxn().getFeptxnTxAmt(), getFiscCon().getTxAmt()) != 0)) {
//				return FISCReturnCode.OriginalMessageDataError;
//			}
//			// '檢核交易是否未完成
//			if (!"B".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
//				// '2014/10/21 Modify by Ruling 檢核電文欄位內容有誤，應由CardReturnCode.CheckFieldError改用FISCReturnCode.MessageFormatError(0101:訊息格式或內容編輯錯誤)
//				rtnCode = FISCReturnCode.MessageFormatError;
//				return rtnCode;
//			}
//			// '2015/09/22 Modify by Ruling for 為了避免送兩次Confrim電文加檢核FEPTXN_TRACE_EJFNO<>0
//			if (getFiscBusiness().getFeptxn().getFeptxnTraceEjfno() != 0) {
//				this.logContext.setRemark("已有Confirm電文, FEPTXN_TRACE_EJFNO=" + getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().toString());
//				logMessage(Level.INFO, getLogContext());
//				rtnCode = FISCReturnCode.MessageFormatError; // ' **相關欄位檢查錯誤
//				getFiscBusiness().setFeptxn(new FeptxnExt()); // ' 第2次Confirm則不更新FEPTXN
//				getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
//				return rtnCode;
//			}
//
//			getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
//			// '2017/11/17 Modify by Ruling for 收到財金確認電文時間寫入FEPTXN
//			getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
//			rtnCode = encHelper.checkFiscMac(getFiscCon().getMessageType(), getFiscCon().getMAC());
//			return rtnCode;
//		} catch (Exception ex) {
//			this._rtnCode = CommonReturnCode.ProgramException;
//			this.logContext.setProgramException(ex);
//			this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
//			sendEMS(this.logContext);
//		}
//		return rtnCode;
//	}

    // '''<summary>
    // ''' UpdateTxData部份
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
    // ''' <modify>
    // ''' <modifier>Husan</modifier>
    // ''' <reason>connie spec change
    // ''' 刪除欄位
    // ''' 1. FEPTXN_REQ_TIME
    // ''' 2. FEPTXN_REP_TIME
    // ''' 3. FEPTXN_CON_TIME
    // ''' </reason>
    // ''' <date>2010/11/05</date>
    // ''' <modifier>HusanYin</modifier>
    // ''' <reason>修正Const RC</reason>
    // ''' <date>2010/11/25</date>
    // ''' </modify>
//	private FEPReturnCode updateTxData() {
//		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
//		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
//		try {
//			getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));
//			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
//				if (NormalRC.FISC_ATM_OK.equals(getFiscCon().getResponseCode())) {
//					getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
//				} else {
//					getFiscBusiness().getFeptxn().setFeptxnTxrust("C"); // 'Accept-Reverse
//				}
//				getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
//			}
//
//			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // 'F3
//			// '2011/03/14 modified by Ruling for 修正被代理交易Confirm的ej number沒寫入FEPTXN_TRACE_EJNO的問題
//			getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
//			getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
//
//			this.feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData")); // 2021-06-16 Richard add
//
//			// 'add by maxine on 2012/03/05 for 換日後fiscBusiness.DBFEPTXN,以及txData.FEPTXN都應該使用實際交易日期,避免後面更新不到FEPTXN
//			getTxData().setFeptxnDao(this.feptxnDao);
//			getFiscBusiness().setFeptxnDao(this.feptxnDao);
//
//			int i = this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn());
//			if (i <= 0) {
//				transactionManager.rollback(txStatus);
//				_rtnCode = IOReturnCode.FEPTXNUpdateError;
//				return _rtnCode;
//			}
////			20220913 Bruce modify 依照Candy需求取消國際卡交易 start
////			if ("24".equals(getFiscCon().getProcessingCode().substring(0, 2))) {
////				Intltxn intltxn = new Intltxn();
////				IntltxnMapper intltxnMapper = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
////				int iRes = 0;
////				// 'spec change 20101130
////				intltxn.setIntltxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
////				intltxn.setIntltxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
////				intltxn = intltxnMapper.selectByPrimaryKey(intltxn.getIntltxnTxDate(), intltxn.getIntltxnEjfno());
////				if (intltxn != null) {
////					intltxn.setIntltxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
////					intltxn.setIntltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
////					iRes = intltxnMapper.updateByPrimaryKey(intltxn);
////					if (iRes <= 0) {
////						transactionManager.rollback(txStatus);
////						_rtnCode1 = IOReturnCode.UpdateFail;
////						return _rtnCode1;
////					}
////				}
////			}
////			20220913 Bruce modify 依照Candy需求取消國際卡交易 end
//			// '2018/02/12 Modify by Ruling for 跨行無卡提款
//			if (FISCPCode.PCode2510.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
//					&& "6071".equals(getFiscBusiness().getFeptxn().getFeptxnAtmType())) {
//				Nwdtxn defNWDTXN = new Nwdtxn();
//				NwdtxnMapper dbNWDTXN = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
//				int iRes = 0;
//				defNWDTXN.setNwdtxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
//				defNWDTXN.setNwdtxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
//				defNWDTXN.setNwdtxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
//				defNWDTXN.setNwdtxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
//				iRes = dbNWDTXN.updateByPrimaryKey(defNWDTXN);
//				if (iRes <= 0) {
//					transactionManager.rollback(txStatus);
//					_rtnCode1 = IOReturnCode.UpdateFail;
//					return _rtnCode1;
//				}
//			}
//
//			// '2019/07/23 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：被掃交易更新QRPTXN
////			20220913 Bruce modify 依照Candy需求取消豐錢包APP被掃交易 Start 
////			if (FISCPCode.PCode2541.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
////					&& "6".equals(getFiscBusiness().getFeptxn().getFeptxnAtmType().substring(0, 1))
////					&& ("B".equals(getFiscBusiness().getFeptxn().getFeptxnAtmType().substring(3, 4)) || "C".equals(getFiscBusiness().getFeptxn().getFeptxnAtmType().substring(3, 4)))) {
////				Qrptxn defQRPTXN = new Qrptxn();
////				QrptxnMapper dbQRPTXN = SpringBeanFactoryUtil.getBean(QrptxnMapper.class);
////				int iRes = 0;
////
////				defQRPTXN.setQrptxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
////				defQRPTXN.setQrptxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
////				defQRPTXN.setQrptxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
////				defQRPTXN.setQrptxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
////				iRes = dbQRPTXN.updateByPrimaryKey(defQRPTXN);
////				if (iRes <= 0) {
////					transactionManager.rollback(txStatus);
////					_rtnCode1 = IOReturnCode.UpdateFail;
////					return _rtnCode1;
////				}
////			}
////			20220913 Bruce modify 依照Candy需求取消豐錢包APP被掃交易 end 
////			20220913 Bruce modify 依照Candy需求取消 若交易失敗需更新原始交易,但授權完成若是失敗則不再更新預約授權資料, 因為已解圏 Start 
////			if (FISCPCode.PCode2542.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
////					&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
////				if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
////					getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("D");
////				} else {
////					getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A");
////				}
////
////				FeptxnMapper oridbFEPTXN = SpringBeanFactoryUtil.getBean(FeptxnMapper.class);
////				int iRes = 0;
////
////				// 'modified By Maxine on 2011/10/13 for 改用FEPTXN_REQ_DATETIME欄位
////				getFiscBusiness().getOriginalFEPTxn().setFeptxnReqDatetime(getFiscBusiness().getFeptxn().getFeptxnDueDate());
////				// 'fiscBusiness.OriginalFEPTxn.FEPTXN_TX_DATE = fiscBusiness.FepTxn.FEPTXN_DUE_DATE
////				getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
////				getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
////				iRes = oridbFEPTXN.updateByPrimaryKey(getFiscBusiness().getOriginalFEPTxn());
////				// 'iRes = oridbFEPTXN.UpdateFEPTXNByDateBknoStan(fiscBusiness.OriginalFEPTxn)
////				if (iRes <= 0) {
////					iRes = oridbFEPTXN.updateByPrimaryKey(getFiscBusiness().getOriginalFEPTxn());
////					if (iRes <= 0) {
////						transactionManager.rollback(txStatus);
////						_rtnCode1 = IOReturnCode.FEPTXNUpdateError;
////						return _rtnCode1;
////					}
////				}
////			}
////			20220913 Bruce modify 依照Candy需求取消 若交易失敗需更新原始交易,但授權完成若是失敗則不再更新預約授權資料, 因為已解圏 end 
//			transactionManager.commit(txStatus);
//		} catch (Exception ex) {
//			// '若失敗則復原
//			transactionManager.rollback(txStatus);
//			this.logContext.setProgramException(ex);
//			this.logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
//			sendEMS(this.logContext);
//			return CommonReturnCode.ProgramException;
//		}
//		return CommonReturnCode.Normal;
//	}

//	private FEPReturnCode processAPTOTSendToCBSASC() throws Exception {
//		HK hostHk = new HK(getTxData());
//		MO hostMo = new MO(getTxData());
//		T24 hostT24 = new T24(getTxData());
//		Credit hostCredit = new Credit(getTxData());
//		FEPReturnCode rtnCode = CommonReturnCode.Normal;
//
//		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
//			if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
//				rtnCode = getFiscBusiness().processAptot(true);
//				getLogContext().setProgramName(ProgramName);
//				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())
//						&& !"2542".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
//						&& !"2543".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
//					switch (getFiscBusiness().getFeptxn().getFeptxnZoneCode()) {
//						case ZoneCode.TWN:
//							getLogContext().setRemark("FEPTXN_CBS_TX_CODE=" + getFiscBusiness().getFeptxn().getFeptxnCbsTxCode());
//							logMessage(Level.DEBUG, getLogContext());
//							rtnCode = hostT24.sendToT24(getFiscBusiness().getFeptxn().getFeptxnCbsTxCode(), 2, true);
//							break;
//						case ZoneCode.HKG:
//							getLogContext().setRemark("FEPTXN_CBS_TX_CODE=" + getFiscBusiness().getFeptxn().getFeptxnCbsTxCode());
//							logMessage(Level.DEBUG, getLogContext());
//							// '2013/06/24 Modify by Ruling for 港澳NCB
//							if (CBSHostType.Unisys.equals(SysStatus.getPropertyValue().getSysstatCbsHkg())) {
//								rtnCode = hostHk.sendToCBSHK(getFiscBusiness().getFeptxn().getFeptxnCbsTxCode(), (byte) 0);
//							} else {
//								rtnCode = hostHk.sendToCBSHKT24(getFiscBusiness().getFeptxn().getFeptxnCbsTxCode(), (byte) 2, true);
//							}
//							break;
//						case ZoneCode.MAC:
//							getLogContext().setRemark("FEPTXN_CBS_TX_CODE=" + getFiscBusiness().getFeptxn().getFeptxnCbsTxCode());
//							logMessage(Level.DEBUG, getLogContext());
//							// '2013/06/24 Modify by Ruling for 港澳NCB
//							if (CBSHostType.Unisys.equals(SysStatus.getPropertyValue().getSysstatCbsMac())) {
//								rtnCode = hostMo.sendToCBSMO(getFiscBusiness().getFeptxn().getFeptxnCbsTxCode(), (byte) 0);
//							} else {
//								rtnCode = hostMo.sendToCBSMOT24(getFiscBusiness().getFeptxn().getFeptxnCbsTxCode(), (byte) 2, true);
//							}
//							break;
//					}
//				} else {
//					getLogContext().setProgramName(ProgramName);
//					TxHelper.getMessageFromFEPReturnCode(getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC, getLogContext());
//				}
//			} else {
////				20220913 Bruce modify 依照Candy需求取消 轉入交易入帳 for 跨行轉帳之CBS主機代號有二個, 第一個為轉出沖正代號, 第二個為轉入入帳代號, 所以入帳時需讀取第二個主機代號 Start
////				if (!getFiscBusiness().getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
////						&& getFiscBusiness().getFeptxn().getFeptxnTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
////						|| "2542".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
////						|| "2543".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
////					if (!"4".equals(getFiscBusiness().getFeptxn().getFeptxnPcode().substring(3, 4))) {
////						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), 1, true);
////					} else {
////						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(), 1, true);
////					}
////					if ("G".equals(getFiscBusiness().getFeptxn().getFeptxnTrinKind())) {
////						rtnCode = hostCredit.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(), (byte) 1);
////					}
////				}
////				if ("2523".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
////						&& "G".equals(getFiscBusiness().getFeptxn().getFeptxnTrinKind())) {
////					rtnCode = hostCredit.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(), (byte) 1);
////				}
////				20220913 Bruce modify 依照Candy需求取消 轉入交易入帳 for 跨行轉帳之CBS主機代號有二個, 第一個為轉出沖正代號, 第二個為轉入入帳代號, 所以入帳時需讀取第二個主機代號 end
//				// '2018/12/26 Modify by Ruling for 避免2470沖正交易，原交易結果已更新為D沖正，但因2450 Confirm發送MailHunter逾時後又將交易結果更新為A成功，將此段移至後面發簡訊及EMAIL
//				// '2014/10/24 Modify by Ruling for HK SMS:香港境外提款發簡訊功能
//				// 'If fiscBusiness.FepTxn.FEPTXN_ZONE_CODE = ZoneCode.HKG AndAlso (fiscBusiness.FepTxn.FEPTXN_PCODE = "2410" OrElse fiscBusiness.FepTxn.FEPTXN_PCODE = "2510") Then
//				// ' rtnCode = fiscBusiness.PrepareHKSMS()
//				// 'End If
//				//
//				// '2018/12/26 Modify by Ruling for 已在+CON內不需在判斷一次拿掉FEPTXN_CON_RC = NormalRC.FISC_ATM_OK 條件
//				// '2018/04/03 Modify by Ruling for 跨行無卡提款:往網銀推播通知服務
//				if (FISCPCode.PCode2510.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
//						&& "6071".equals(getFiscBusiness().getFeptxn().getFeptxnAtmType())) {
//					rtnCode = sendToNB();
//				}
//				// 'If fiscBusiness.FepTxn.FEPTXN_PCODE = CStr(FISCPCode.PCode2510) AndAlso
//				// ' fiscBusiness.FepTxn.FEPTXN_ATM_TYPE = "6071" AndAlso
//				// ' fiscBusiness.FepTxn.FEPTXN_CON_RC = NormalRC.FISC_ATM_OK Then
//				// ' sendToNB()
//				// 'End If
//				//
//				// '2018/12/26 Modify by Ruling for 避免2470沖正交易，原交易結果已更新為D沖正，但因2450 Confirm發送MailHunter逾時後又將交易結果更新為A成功，將此段移至後面發簡訊及EMAIL
//				// 'Fly 2018/03/12 修改 for 跨國提款, 發簡訊功能
//				// 'If fiscBusiness.FepTxn.FEPTXN_ZONE_CODE = "TWN" AndAlso fiscBusiness.FepTxn.FEPTXN_PCODE = CStr(FISCPCode.PCode2450) Then
//				// ' fiscBusiness.PrepareSMSMAIL()
//				// 'End If
//				//
//				// '2019/02/11 Modify by Ruling for 跨行轉帳小額交易手續費調降
////				20220913 Bruce modify 依照Candy需求取消 跨行轉帳小額交易手續費調降 Start
////				if (FISCPCode.PCode2524.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
////						&& SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())
////						&& StringUtils.isNotBlank(getFiscCon().getAcctSup())) {
////					getFiscBusiness().getFeptxn().setFeptxnAcctSup(getFiscCon().getAcctSup());
////				}
////				20220913 Bruce modify 依照Candy需求取消 跨行轉帳小額交易手續費調降 end
//			}
//		}
//		return rtnCode;
//	}

    // ''' <summary>
    // ''' CON(+)往網銀推播通知服務
    // ''' </summary>
    // ''' <remarks></remarks>
    // ''' <modify>
    // ''' <modifier>Ruling</modifier>
    // ''' <reason>跨行無卡提款</reason>
    // ''' <date>2018/02/12</date>
    // ''' </modify>
//	private FEPReturnCode sendToNB() {
//		FEPReturnCode rtnCode = CommonReturnCode.Normal;
//		// '2020/07/02 Modify by Ruling for 大戶APP無卡提款推播：移到下面要使用前才用NEW
//		Nwdtxn nwdtxn = new Nwdtxn();
//		NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
//		try {
//			nwdtxn.setNwdtxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
//			nwdtxn.setNwdtxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
//			Nwdtxn record = nwdtxnMapper.selectByPrimaryKey(nwdtxn.getNwdtxnTxDate(), nwdtxn.getNwdtxnEjfno());
//			if (record == null) {
//				getLogContext().setRemark("以交易日=" + nwdtxn.getNwdtxnTxDate() + ",EJFNO=" + nwdtxn.getNwdtxnEjfno() + "找不到預約資料,無法送網銀推播通知服務,");
//				logMessage(Level.INFO, getLogContext());
//			} else {
//				// '2020/07/02 Modify by Ruling for 大戶APP無卡提款推播：
//				// '2020/09/09 Modify by Ruling for 新增MDAWHO Channel Code：依預約Channel為MDAWHO，發送大戶APP WebService
////				20220913 Bruce Modify 依照Candy需求修改推播方式 無卡提款簡訊/EMAL/推播 Start
////				if (FEPChannel.MOBILBANK.name().equals(record.getNwdtxnRegChannel())) {
////					Ncnb hostNB = new Ncnb(getTxData());
////					rtnCode = hostNB.sendToNCNB("3", record.getNwdtxnRegDt());
////				} else if (FEPChannel.DAPP.name().equals(record.getNwdtxnRegChannel())
////						|| FEPChannel.MDAWHO.name().equals(record.getNwdtxnRegChannel())) {
////					Dapp hostDAPP = new Dapp(getTxData());
////					rtnCode = hostDAPP.sendToDAPP(DAPPAppMsg.Complete.getValue());
////				}
//				switch(getFiscBusiness().getFeptxn().getFeptxnNoticeType()) {
//				case "P": rtnCode = new BusinessBase().preparePush(getFiscBusiness().getFeptxn()); break; //送推播
//				case "M": rtnCode = new BusinessBase().prepareSms(getFiscBusiness().getFeptxn()); break; //簡訊
//				case "E": rtnCode = new BusinessBase().prepareMail(getFiscBusiness().getFeptxn()); break; //Email
//				}
////				20220913 Bruce Modify 依照Candy需求修改推播方式 無卡提款簡訊/EMAL/推播 end
//			}			
//		} catch (Exception ex) {
//			this.logContext.setProgramException(ex);
//			this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToNB"));
//			sendEMS(this.logContext);
//		}
//		return rtnCode;
//	}

    // ''' <summary>
    // ''' 更新FEPTXN
    // ''' </summary>
    // ''' <returns></returns>
    // ''' <remarks></remarks>
//	private FEPReturnCode updateFEPTXN() {
//		FEPReturnCode rtnCode = FEPReturnCode.Normal;
//		if (getFiscBusiness().getFeptxn().getFeptxnAaRc() == CommonReturnCode.Normal.getValue()) {
//			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
//		}
//		getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));
//		rtnCode = getFiscBusiness().updateTxData();
//		if (rtnCode != CommonReturnCode.Normal) {
//			return rtnCode;
//		}
//		return rtnCode;
//	}

    private FEPReturnCode sendToMailHunter() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            if (getFiscBusiness().getFeptxn() != null
                    && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
                    && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                switch(getFiscBusiness().getFeptxn().getFeptxnNoticeType()){
                    case "P" : /* 送推播 */
                        getFiscBusiness().preparePush(getFiscBusiness().getFeptxn());
                        break;
                    case "M": /* 簡訊 */
                        getFiscBusiness().prepareSms(getFiscBusiness().getFeptxn());
                        break;
                    case "E": /* Email */
                        getFiscBusiness().prepareMail(getFiscBusiness().getFeptxn());
                        break;
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
            return FEPReturnCode.Abnormal;
        }
    }
}