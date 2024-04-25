package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.ext.mapper.CardExtMapper;
import com.syscom.fep.mybatis.model.Card;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMCTxType;
import com.syscom.fep.vo.enums.ATMCardCombine;
import com.syscom.fep.vo.enums.ATMCardStatus;
import com.syscom.fep.vo.enums.ATMCashTxType;
import com.syscom.fep.vo.enums.ATMNCCardStatus;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.CBSTxType;
import com.syscom.fep.vo.enums.CreditTxType;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.T24;

/**
 * <summary>
 * 負責處理 ATM 送來的信用卡交易電文
 * B05: 金融信用卡帳務查詢
 * B15: 晶片金融信用卡帳務查詢
 * AIN: 永豐錢卡餘額查詢
 * AAC: 永豐錢卡調約定轉入帳號
 * GIQ: GIFT卡額度查詢
 * CAV: VISA卡預借現金
 * CAM: MASTERCARD卡預借現金
 * CAA: AE卡預借現金
 * CAJ: JCB卡預借現金
 * CWV: PLUS國際卡提款
 * CWM: CIRRUS國際卡提款
 * ACW: 永豐錢卡預借現金
 * ATF: 永豐錢卡自行轉帳(Trk3)
 * BFT: 永豐錢卡自行轉帳(Trk2)
 * PNC: 金融信用卡-信用卡密碼變更
 * PNM: 金融信用卡-新卡啟用
 * </summary>
 * <remarks>
 * AA程式撰寫原則:
 * AA的程式主要為控制交易流程,Main為AA程式的進入點,在Main中的程式為控制交易的過程該如何進行
 * 請不要在Main中去撰寫實際的處理細節,儘可能將交易過程中的每一個"步驟",以副程式的方式來撰寫,
 * 而這些步驟,如果可以共用的話,請將該步驟寫在相關的Business物件中(例如本FISCBusiness中的CheckHeader,PrepareFEPTXN,CheckRequestBody)
 * 如果該步驟只有該AA會用到的話,再寫至自己AA中的類別中(例如本AA中的PrepareForFISC)
 * </remarks>
 * <history>
 * <modify>
 * <modifier>Kyo</modifier>
 * <reason>AA design</reason>
 * <date>2009/12/07</date>
 * </modify>
 * </history>
 */
public class CashAdvance extends ATMPAABase {
    private CardExtMapper cardExtMapper = SpringBeanFactoryUtil.getBean(CardExtMapper.class);
    ATMTXCD txCode;
    String rtnMessage = "";
    FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    Boolean needUpdateFEPTXN = false;
    //  2010-06-02 by kyo for SPEC修改
    Boolean wOldCard = false; // 是否處理舊卡FLAG
    Card oldDeCard = new Card();
    //  modify on 2010-04-09 for 因為主機回應逾時是H003，但信用卡主機開卡逾時(ATM的PNM電文，信用卡的B01電文)不能回H003要回Y002
    final String OpenCardTimeout = "Y002";
    Credit hostCredit = new Credit(this.getTxData());

    //	<summary>
//	AA的建構式,在這邊初始化及設定好相關變數   
//	</summary>
//	<param name="txnData">AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件</param>
//	<remarks></remarks>
    public CashAdvance(ATMData txnData) throws Exception {
        super(txnData);
      //--ben-20220922-//this.txCode = ATMTXCD.parse(this.getATMRequest().getTXCD());
    }

    //	"AA進入點主程式"
//	<summary>
//	程式進入點
//	</summary>
//	<returns>Response電文</returns>
//	<remarks></remarks>
//	<history>
//	<modify>
//	<modifier>Kyo</modifier>
//	<reason>對應海外卡在台灣跨區提款的問題，修改先送電文回給ATM</reason>
//	<date>2010/06/10</date>
//	</modify>
//	</history>
    @Override
    public String processRequestData() throws Exception {
        try {
//          1.準備FEP交易記錄檔
            this._rtnCode = this.getATMBusiness().prepareFEPTXN();

//          2.新增FEP交易記錄檔
            if (this._rtnCode == CommonReturnCode.Normal) {
                this._rtnCode = this.getATMBusiness().addTXData();
            }

//          3.商業邏輯檢核
            if (this._rtnCode == CommonReturnCode.Normal) {
                this.needUpdateFEPTXN = true;
                this._rtnCode = this.checkBusinessRule();
            }

//          4.帳務主機處理
            if (this._rtnCode == CommonReturnCode.Normal) {
                this._rtnCode = this.sendToHost();
            }

//          5.更新FEP交易記錄檔
            this.updateFEPTxn();
//          2010-05-05 modify by kyo for 與永豐決議Phase I將 PNM 送至 ATMP:已經於sendtohost送至ATMP 故移除邏輯
        } catch (Exception ex) {
//          2010-04-23 modified by kyo for 防止程式中發生例外但沒CATCH到時，會回ATM正常的這種異常情形
            this._rtnCode = CommonReturnCode.ProgramException;
//          2011-07-05 by kyo for exception不需要call GetRCFromErrorCode，避免送兩次EMS
            this.getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            this.logContext.setProgramException(ex);
            sendEMS(this.logContext);
        } finally {
            if (StringUtils.isBlank(this.getTxData().getTxResponseMessage())) {
                this.rtnMessage = this.prepareATMResponseData();
            } else {
                this.rtnMessage = this.getTxData().getTxResponseMessage();
            }
            this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            this.getTxData().getLogContext().setMessage(this.rtnMessage);
            this.getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            this.logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(this._rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }

    //	"本支AA共用之sub routine"
//	
//	 <summary>
//	 ATM電文檢核邏輯
//	 </summary>
//	 <returns></returns>
//	 <remarks></remarks>
//	 <history>
//	   <modify>
//	     <modifier>Kyo</modifier>
//	     <reason>SPEC新增:PNM須執行變更密碼</reason>
//	     <date>2010/05/26</date>
//	   </modify>
//	   <modify>
//	     <modifier>Kyo</modifier>
//	     <reason>SPEC新增:PNM邏輯變更，以及調整程式</reason>
//	     <date>2010/06/02</date>
//	   </modify>
//	   <modify>
//	     <modifier>Kyo</modifier>
//	     <reason>for spec modify: 7/28 永豐通知修正 使用enclib來確認密碼</reason>
//	     <date>2010/07/28</date>
//	   </modify>
//	   <modify>
//	     <modifier>Kyo</modifier>
//	     <reason>1.for spec modify:7/29 修改 for PNM offset邏輯修改
//	             2.for spec added:補上存入卡片序號</reason>
//	     <date>2010/07/29</date>
//	   </modify>
//	   <modify>
//	     <modifier>Kyo</modifier>
//	     <reason>for spec修改:COMBO卡交易檢核卡片狀態改至3.4執行 且新增PNM邏輯檢核卡檔</reason>
//	     <date>2010/07/30</date>
//	   </modify>
//	 </history>
    private FEPReturnCode checkBusinessRule() {
        ENCHelper encHelper = new ENCHelper(this.getATMBusiness().getFeptxn(), this.getTxData());
        RefString returnData = new RefString(StringUtils.EMPTY);

//	      3.1 CheckHeader
        this._rtnCode = this.getATMBusiness().checkHeader();
        if (this._rtnCode != CommonReturnCode.Normal) {
            return this._rtnCode;
        }

//	      3.2 CheckBody
        this._rtnCode = this.getATMBusiness().checkBody();
        if (this._rtnCode != CommonReturnCode.Normal) {
            return this._rtnCode;
        }

//	      3.3 更新 ATM 狀態  
        this._rtnCode = this.getATMBusiness().updateATMStatus();
        if (this._rtnCode != CommonReturnCode.Normal) {
//	          更新ATM狀態失敗,更新交易記錄
            return this._rtnCode;
        }

        try {
//	          2010-07-30 by kyo for spec修改:COMBO卡交易檢核卡片狀態改至3.4執行 且新增PNM邏輯檢核卡檔
//	          2011-05-05 by kyo for 開卡流程新電文
//	          3.4 COMBO卡交易檢核卡片狀態
            if (this.txCode == ATMTXCD.PNC || this.txCode == ATMTXCD.B05 || this.txCode == ATMTXCD.B15
                    || this.txCode == ATMTXCD.PNM || this.txCode == ATMTXCD.PNX) {
                this._rtnCode = this.getATMBusiness().checkCardStatus();
            }
//	          2019/07/04 Modify by Ruling for 避免找不到卡檔，PNX將信用卡效期寫入FEPTXN時發生程式例外
            if (this._rtnCode != CommonReturnCode.Normal) {
                return this._rtnCode;
            }

//	          3.5 開卡交易取得舊卡 OFFSET
//	          '2010-06-02 by kyo for SPEC修改邏輯
//	          '2011-05-05 by kyo for 開卡流程新電文
//	          '2014-09-18 by Ruling for COMBO優化：借用FEPTXN_NOTICE_ID將信用卡效期放入
            if (this.txCode == ATMTXCD.PNM || this.txCode == ATMTXCD.PNX) {
//	              '2020/03/12 Modify by Ruling CheckCardStatus已讀新卡資料，多讀一次CARD檔拿掉此段邏輯
                this.getATMBusiness().getFeptxn().setFeptxnNoticeId(this.getATMBusiness().getCard().getCardCrEndMmyy());
//	              '/* 1/28 修改 */ 加上條件ATMBusiness.Card.CARD_CARD_SEQ > 1
//	              '2010-05-26 by kyo for SPEC更新:取新卡資料的邏輯修正，從TRK3取資料
                if (this.getATMBusiness().getCard().getCardStatus() == (short) ATMCardStatus.Create.getValue()
                        && this.getATMBusiness().getCard().getCardCardSeq() > 1) {
                    this.wOldCard = true;
//	                  '取得舊卡資料
//	                  '2020/03/12 Modify by Ruling 改用ICMARK讀舊卡資料
                    oldDeCard.setCardActno(this.getATMBusiness().getFeptxn().getFeptxnMajorActno());
                    oldDeCard.setCardCardSeq(this.getATMBusiness().getFeptxn().getFeptxnCardSeq());
                    oldDeCard.setCardCombine((short) ATMCardCombine.Extending.getValue());
                    oldDeCard.setCardStatus((short) ATMCardStatus.Start.getValue());
//	                  '2010-05-26 by kyo for SPEC修改:不需要判斷status這個條件
                    if (cardExtMapper.getOldCardByMaxCardSeq(oldDeCard) != null) {
//	                      '/* 3/22  PNX不需回寫舊卡 Offset */
                        if (this.txCode == ATMTXCD.PNM) {
                            if (StringUtils.isBlank(oldDeCard.getCardOut().trim())) {
                                this.getATMBusiness().getCard().setCardOffset(0);
                            } else {
                                if (oldDeCard.getCardOut().matches("[+-]?\\d*(\\.\\d+)?")) {
                                    this.getATMBusiness().getCard()
											.setCardOffset(Integer.parseInt(StringUtil.toHex(oldDeCard.getCardOut())));// 'HEX值轉成
                                    // ASCII
                                }
                            }
                        }
                    }
                }
            }
            if (this._rtnCode != CommonReturnCode.Normal) {
                return this._rtnCode;
            }

//	          '3.6 檢核磁條密碼變更
//	          '2010-05-26 by kyo for SPEC新增:PNM須執行變更密碼
            if (this.txCode == ATMTXCD.PNM) {
                String wOffset = "";
//	             '2010-07-29 by kyo for spec modify:7/29 修改 for PNM */
                if (!wOldCard) {
//	                  '無舊卡
                    wOffset = this.getATMBusiness().getFeptxn().getFeptxnTrk3().substring(45, 45 + 4);
                } else {
//	                  '有舊卡
                    wOffset = StringUtils.leftPad(this.getATMBusiness().getCard().getCardOffset().toString(), 4, '0');
                }
//	              ''2010-07-29 by kyo for spec added:補上存入卡片序號
//	              '2010-08-30 by kyo for spec修改:/* 8/30 修改 */ Card_Status=2 才驗密
                if (this.getATMBusiness().getCard().getCardStatus() == (byte) ATMCardStatus.Create.getValue()) {
                    encHelper = new ENCHelper(this.getATMBusiness().getFeptxn(), this.getTxData());
//	                  '2010-07-29 by kyo for enclib 修改介面
                  //--ben-20220922-//this._rtnCode = encHelper.changePassword(this.getATMRequest().getFPB(),
                  //--ben-20220922-//this.getATMRequest().getFpbNew(), wOffset, returnData);
                    if (this._rtnCode != CommonReturnCode.Normal) {
                        this._rtnCode = this.getATMBusiness().checkPWErrCnt(1); // '密碼可錯誤次數-1
                    } else {
//	                      '取得 Offset  Increment
                        this.getATMBusiness().getFeptxn().setFeptxnIncre(returnData.get());
                        this._rtnCode = this.getATMBusiness().checkPWErrCnt(2);// '回覆密碼錯誤次數
                    }
                }
            }
//	          '/* 3/22 新增電文 PNX 或 (PNM且 新卡狀態=3) */
            if (this.txCode == ATMTXCD.PNX || (this.txCode == ATMTXCD.PNM
                    && this.getATMBusiness().getCard().getCardStatus() == (short) ATMCardStatus.Receive.getValue())) {
            	//--ben-20220922-//this._rtnCode = encHelper.checkPassword(this.getATMRequest().getFpbNew(),
            	//--ben-20220922-//this.getATMBusiness().getFeptxn().getFeptxnTrk3().substring(45, 45 + 4), returnData);
                if (this._rtnCode == FEPReturnCode.Normal) {
                    this.getATMBusiness().getFeptxn()
                            .setFeptxnIncre(this.getATMBusiness().getFeptxn().getFeptxnTrk3().substring(45, 45 + 4));
                } else {
                    if (StringUtils.isBlank(returnData.get().trim())) {
                        this._rtnCode = FEPReturnCode.ENCCheckPasswordError;
                    } else {
                        this.getATMBusiness().getFeptxn().setFeptxnIncre(returnData.get());
                        this._rtnCode = FEPReturnCode.Normal;
                    }
                }
            }
            if (this._rtnCode != CommonReturnCode.Normal) {
                return this._rtnCode;
            }

//	          '3.7 晶片卡交易檢核MAC及TAC
//	          '2013/07/24 Modify by Ruling for 修正ATM電文的MAC為空白時不Call DES
            if (!StringUtils.isBlank(this.getTxData().getMsgCtl().getMsgctlReqmacType().toString())) {
            	//--ben-20220922-//if (StringUtils.isBlank(this.getATMRequest().getMAC())) {
            	//--ben-20220922-//    this.logContext.setRemark("CheckBusinessRule-ATM電文的MAC為空白");
            	//--ben-20220922-//    logMessage(Level.INFO, this.logContext);
            	//--ben-20220922-//    return ENCReturnCode.ENCCheckMACError;
            	//--ben-20220922-//}
            	//--ben-20220922-//this._rtnCode = encHelper.checkATMMAC(this.getATMRequest().getMAC());
                if (this._rtnCode != CommonReturnCode.Normal) {
//	                  '檢核ATM電文失敗，更新交易記錄
                    return this._rtnCode;
                }
            }

//	         '2010-01-20 by ed 檢核ACW的BIN為null的情形
//	         '3.8	錢卡交易檢核銀行代號及BIN
            if (this.txCode == ATMTXCD.ACW || this.txCode == ATMTXCD.ATF || this.txCode == ATMTXCD.BFT) {
                if (!this.getATMBusiness().getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue()
                        .getSysstatHbkno())) {
                    this._rtnCode = FEPReturnCode.OtherCheckError;
                    return this._rtnCode;
                } else {
                    if (this.getTxData().getBin() == null) {
                        this._rtnCode = FEPReturnCode.OtherCheckError;
                        return this._rtnCode;
                    }
                }

//	             'add by maxine for  3/20 修改 for永豐錢卡不得轉入 GIFT 卡*/
                if (this.txCode == ATMTXCD.BFT) {
                    this._rtnCode = this.getATMBusiness().checkTrfGCard();
                    return this._rtnCode;
                }

            }

//	         '2016/12/22 Modify by Ruling for EAV、EAM、EQC、EQP、EWV、EWM預借現金自行交易
//	         '3.9	預借現金交易檢核信用卡BIN
            if (this.txCode == ATMTXCD.CAV || this.txCode == ATMTXCD.CAM || this.txCode == ATMTXCD.CAA
                    || this.txCode == ATMTXCD.CAJ || this.txCode == ATMTXCD.EAV || this.txCode == ATMTXCD.EAM
                    || this.txCode == ATMTXCD.EQP || this.txCode == ATMTXCD.EQC || this.txCode == ATMTXCD.EWV
                    || this.txCode == ATMTXCD.EWM) {

                if (this.txCode == ATMTXCD.EQP || this.txCode == ATMTXCD.EQC || this.txCode == ATMTXCD.EWV
                        || this.txCode == ATMTXCD.EWM) {
                    this._rtnCode = FISCReturnCode.StopServiceByInternal;
                    this.logContext.setRemark("永豐信用卡只提供預借現金,無餘額查詢交易(EQC、EQP)及國際卡提款(EWV、EWM)");
                    logMessage(Level.INFO, this.logContext);
                    return this._rtnCode;
                }

                if (this.getTxData().getBin() == null) {
                    this._rtnCode = ATMReturnCode.OtherCheckError;
                    this.logContext.setRemark("BIN檔中找不到資料");
                    logMessage(Level.INFO, this.logContext);
                    return this._rtnCode;
                }

                if (!this.getATMBusiness().getFeptxn().getFeptxnTroutKind().equals("C")) {
                    this._rtnCode = FISCReturnCode.StopServiceByInternal;
                    this.logContext.setRemark(
                            "轉出類別必須為信用卡,FEPTXN_TROUT_KIND=" + this.getATMBusiness().getFeptxn().getFeptxnTroutKind());
                    logMessage(Level.INFO, this.logContext);
                    return this._rtnCode;
                }

// 2024-03-06 Richard modified for SYSSTATE 調整
//                if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAsc())) {
//                    this._rtnCode = FISCReturnCode.StopServiceByInternal;
//                    this.logContext.setRemark("信用卡主機暫停服務");
//                    logMessage(Level.INFO, this.logContext);
//                    return this._rtnCode;
//                }
            }

//	          'EMV晶片卡交易寫入POS ENTRY MODE
            if (this.txCode == ATMTXCD.EAV || this.txCode == ATMTXCD.EAM) {
            	//--ben-20220922-//if (StringUtils.isBlank(this.getATMRequest().getPOSENTRYMOD())) {
            	//--ben-20220922-//    this._rtnCode = ATMReturnCode.OtherCheckError;
            	//--ben-20220922-//    this.logContext.setRemark("POS ENTRY MODE 為NULL或空白");
            	//--ben-20220922-//    logMessage(Level.INFO, this.logContext);
            	//--ben-20220922-//    return this._rtnCode;
            	//--ben-20220922-//}

            	//--ben-20220922-//if (DbHelper.toBoolean(this.getATMBusiness().getATMMSTR().getAtmEmv())
            	//--ben-20220922-//        && "2901".equals(this.getATMRequest().getPOSENTRYMOD())) {
            	//--ben-20220922-//    this._rtnCode = ATMReturnCode.OtherCheckError;
            	//--ben-20220922-//    this.logContext.setRemark("已升級EMV機台, POSENTRYMOD不能送 2901");
            	//--ben-20220922-//    logMessage(Level.INFO, this.logContext);
            	//--ben-20220922-//    return this._rtnCode;
            	//--ben-20220922-//}

            	//--ben-20220922-//this.getATMBusiness().getFeptxn().setFeptxnRemark(this.getATMRequest().getPOSENTRYMOD());
            	//--ben-20220922-//this.logContext.setRemark("POS ENTRY MODE=" + this.getATMRequest().getPOSENTRYMOD());
                logMessage(Level.INFO, this.logContext);
            }

        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
        return this._rtnCode;
    }

    //     <summary>
//     ATM電文檢核邏輯
//     </summary>
//     <returns></returns>
//     <remarks></remarks>
//     <history>
//       <modify>
//         <modifier>Kyo</modifier>
//         <reason>BugReport(001B0388):永豐人員(張毓真)建議：查詢類交易(如：IIQ、IQ2)可以不記帳。</reason>
//         <date>2010/05/04</date>
//       </modify>
//       <modify>
//         <modifier>Ed</modifier>
//         <reason>配合spec修改程式</reason>
//         <date>2010/11/12</date>
//       </modify>
//     </history>
    private FEPReturnCode sendToHost() {
        FEPReturnCode rtn = FEPReturnCode.Abnormal;
        T24 hostT24 = new T24(this.getTxData());
        try {
//          /* 11/11 修改 */
//          /* 永豐錢卡自行轉帳交易 */
            if (ATMTXCD.ATF.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())
                    || ATMTXCD.BFT.toString().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
                /* 組T24主機電文, 驗證轉帳交易 */
                this._rtnCode = hostT24.sendToT24(this.getTxData().getMsgCtl().getMsgctlTwcbstxid(),
                        (byte) CBSTxType.Accounting.getValue(), false);
                if (this._rtnCode == CommonReturnCode.Normal) {
                    /* 組信用卡電文-授權 */
                    this._rtnCode = hostCredit.sendToCredit(this.getTxData().getMsgCtl().getMsgctlAsctxid(),
                            (byte) CreditTxType.Accounting.getValue());
                    if (this._rtnCode == CommonReturnCode.Normal) {
                        /* 組上T24主機電文入帳 */
                        this._rtnCode = hostT24.sendToT24(this.getTxData().getMsgCtl().getMsgctlTwcbstxid(),
                                (byte) CBSTxType.Accounting.getValue(), true);
                        if (this._rtnCode == CommonReturnCode.Normal) {
                            /* T24主機 Rep(+) , 寫入 ATM 清算資料 */
                            if (DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
                                this._rtnCode = this.getATMBusiness().insertATMC(ATMCTxType.Accounting.getValue());
                            }
                            /* 更新 ATM 鈔匣資料 */
                            if (this._rtnCode == CommonReturnCode.Normal
                                    && DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
                                this._rtnCode = this.getATMBusiness()
                                        .updateATMCash(ATMCashTxType.Accounting.getValue());
                            }
                        } else {
                            /* T24主機 Rep(-), 組信用卡沖正電文 */
                            rtn = hostCredit.sendToCredit(this.getTxData().getMsgCtl().getMsgctlAsctxid(),
                                    (byte) CreditTxType.EC.getValue());
                            /* 組 Rep(-) 回給 ATM */
                            if (rtn != FEPReturnCode.Normal)
                                this._rtnCode = rtn;
                        }
                    }
                }
            }
//          2020/01/30 Modifby by Ruling for 線上換發Debit卡：線上換發Debit卡PNX開卡流程，不送信用卡B01電文
//          /* 組信用卡電文*/
            if (this.getATMBusiness().getFeptxn().getFeptxnTxCode().equals(ATMTXCD.PNX.toString())) {
                if (BINPROD.Debit.equals(
                        TxHelper.getCardType(this.getATMBusiness().getCard().getCardType().byteValue()).getCrdrmark())
                        && DbHelper.toBoolean(
                        TxHelper.getCardType(this.getATMBusiness().getCard().getCardType().byteValue())
                                .getFlowonlinedebit())) {
                    this.logContext.setRemark("線上換發Debit卡不送B01信用卡電文");
                    logMessage(Level.INFO, this.logContext);
                } else {
                    this._rtnCode = hostCredit.sendToCredit(this.getTxData().getMsgCtl().getMsgctlAsctxid(),
                            (byte) CreditTxType.Accounting.getValue());
                }
            } else {
                this._rtnCode = hostCredit.sendToCredit(this.getTxData().getMsgCtl().getMsgctlAsctxid(),
                        (byte) CreditTxType.Accounting.getValue());
            }

            /* 組T24電文 */
            if (this._rtnCode == CommonReturnCode.Normal
                    && !StringUtils.isBlank(this.getTxData().getMsgCtl().getMsgctlTwcbstxid().trim())) {
                /* 組上T24主機電文-代理行現金 */
                this._rtnCode = hostT24.sendToT24(this.getTxData().getMsgCtl().getMsgctlTwcbstxid(),
                        (byte) CBSTxType.Accounting.getValue(), true);
                if (this._rtnCode == CommonReturnCode.Normal) {
                    /* T24主機 Rep(+), 寫入 ATM 清算資料 */
                    if (DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
                        this._rtnCode = this.getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
                    }
                    /* 更新 ATM 鈔匣資料 */
                    if (this._rtnCode == CommonReturnCode.Normal
                            && DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
                        this._rtnCode = this.getATMBusiness().updateATMCash(ATMCashTxType.Accounting.getValue());
                    }
                } else {
                    /* T24 主機 Rep(-), 送信用卡主機沖正授權 */
                    rtn = hostCredit.sendToCredit(this.getTxData().getMsgCtl().getMsgctlAsctxid(),
                            (byte) CreditTxType.EC.getValue());
                    /* 組 Rep(-) 回給 ATM */
                    if (rtn != FEPReturnCode.Normal)
                        this._rtnCode = rtn;
                }
            }
            return this._rtnCode;
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    //	    <summary>
//	     Update FEPTXN
//	     </summary>
//	     <remarks></remarks>
//	     <history>
//	      <modify>
//	         <modifier>Kyo</modifier>
//	         <reason>移掉查舊卡的STATUS判斷邏輯,與取新卡由TRK3來取查詢條件</reason>
//	         <date>2010/05/26</date>
//	       </modify>
//	      <modify>
//	         <modifier>Kyo</modifier>
//	         <reason>調整PNM開卡邏輯</reason>
//	         <date>2010/06/02</date>
//	       </modify>
//	      <modify>
//	         <modifier>Kyo</modifier>
//	         <reason>修改報表FEPTXN_TXRUST的搬移邏輯</reason>
//	         <date>2010/06/03</date>
//	       </modify>
//	     </history>
    private void updateFEPTxn() {
//	    2014/05/30 Modify by Ruling for COMBO優化
        Credit hostCreditB02 = new Credit(this.getTxData());
        String aspStop = "";
//	    2010-05-05 modify by kyo for 與永豐決議Phase I將 PNM 送至 ATMP 移除邏輯
//	    2017/08/04 Modify by Ruling for 註銷舊卡-關閉無卡提款服務
        Boolean isOldCardNCApply = false; // 舊卡是否有申請無卡提款服務
        Boolean isOldCardNCApply_B = false; // 企業戶舊卡是否有申請無卡提款服務
        this.getATMBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.AML_Response);
        this.getATMBusiness().getFeptxn().setFeptxnPending((short) 0);
        if (StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnReplyCode())) {
            // 2010-11-25 by kyo for normal不需Call Txhelper
            if (this._rtnCode == FEPReturnCode.Normal) {
                this.getATMBusiness().getFeptxn().setFeptxnReplyCode(NormalRC.ATM_OK);
            } else {
//	            2010-08-11 by kyo for 明祥通知若有修改程式GetRCFromErrorCoe要使用4個參數的版本
                this.getATMBusiness().getFeptxn()
                        .setFeptxnReplyCode(TxHelper.getRCFromErrorCode(this._rtnCode.toString(), FEPChannel.FEP,
                                this.getTxData().getTxChannel(), this.getTxData().getLogContext()));
            }
        }

        this.getATMBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode.getValue());

//	    Modify by Kyo Lai on 2010-04-01 For報表, 寫入處理結果
        if (this._rtnCode == CommonReturnCode.Normal) {
//	        2010-06-03 by kyo for spec修改
            if (this.getATMBusiness().getFeptxn().getFeptxnWay() == 3) {
                this.getATMBusiness().getFeptxn().setFeptxnTxrust("B"); // 處理結果=Pending
            } else {
                this.getATMBusiness().getFeptxn().setFeptxnTxrust("A");// 處理結果=成功
            }
        } else {
            this.getATMBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
        }

        // Modify by Kyo Lai on 2010-03-11 for updateTxData的_rtnCode不可蓋掉之前的_rtnCode
        if (this.needUpdateFEPTXN) {
            FEPReturnCode _rtnCode = CommonReturnCode.Normal;
            _rtnCode = this.getATMBusiness().updateTxData();
            if (_rtnCode != CommonReturnCode.Normal) {
                this.getATMBusiness().getFeptxn().setFeptxnReplyCode("L013"); // 回寫檔案(FEPTxn)發生錯誤
            }
        }

        try {
            // 2010-05-06 modified by kyo for spec修改邏輯:開卡成功, 將卡片狀態改為啟用
            // 2010-06-02 modified by kyo for spec修改邏輯:開卡成功後舊卡欄位與新卡欄位更新
            // 2011-05-05 by kyo for 開卡流程新電文
            if ((this.getATMBusiness().getFeptxn().getFeptxnTxCode().equals(ATMTXCD.PNM.toString())
                    || this.getATMBusiness().getFeptxn().getFeptxnTxCode().equals(ATMTXCD.PNX.toString()))
                    && this._rtnCode.equals(CommonReturnCode.Normal)) {
                if (wOldCard) {
                    // 2014/05/30 Modify by Ruling for COMBO優化
                    if (StringUtils.isNotBlank(oldDeCard.getCardCreditno())) {
                        if (!oldDeCard.getCardCreditno().equals(this.getATMBusiness().getCard().getCardCreditno())) {
                            aspStop = "10"; // 新卡信用卡號與舊卡信用卡號不同
                        } else {
                            aspStop = "20"; // 新卡信用卡號與舊卡信用卡號相同
                        }
                        this.getATMBusiness().getFeptxn().setFeptxnNoticeId(aspStop);
                        this._rtnCode = hostCreditB02.sendToCreditB02(oldDeCard.getCardCreditno(),
                                oldDeCard.getCardCardSeq().toString(), aspStop);
                        this.logContext.setRemark("舊卡有信用卡號送B02(信用卡停卡)電文給信用卡之結果, _rtnCode=" + this._rtnCode.toString());
                        this.logMessage(this.logContext);
                    }

                    oldDeCard.setCardNbcd(oldDeCard.getCardStatus());
                    oldDeCard.setCardStatus((short) ATMCardStatus.Cancel.getValue());
                    // 2014/08/05 Modify by Ruling for COMBO優化
                    oldDeCard.setCardCancel((short) 4); // '註銷理由不補發

                    // 2012-01-03 modified by KK for 舊卡寫入註銷日期
                    oldDeCard.setCardLosDate(this.getATMBusiness().getFeptxn().getFeptxnTxDate());
                    oldDeCard.setCardLosTime(this.getATMBusiness().getFeptxn().getFeptxnTxTime());
                    this.getATMBusiness().getFeptxn().setFeptxnTxCode("AP1");

                    oldDeCard.setCardOut("");
                    this.getATMBusiness().getCard().setCardOffset(0);

                    // 2017/08/04 Modify by Ruling for 註銷舊卡-關閉無卡提款服務
                    // 關閉舊卡無卡提款服務
                    if (oldDeCard.getCardNcstatus() == ATMNCCardStatus.Apply.getValue()) {
                        oldDeCard.setCardNcstatus((short) ATMNCCardStatus.Close.getValue()); // 註銷
                        oldDeCard.setCardNccloseDate(this.getATMBusiness().getFeptxn().getFeptxnTxDate());
                        oldDeCard.setCardNccloseTime(this.getATMBusiness().getFeptxn().getFeptxnTxTime());
                        isOldCardNCApply = true;
                        this.logContext.setRemark("關閉舊卡無卡提款服務");
                        logMessage(Level.INFO, this.logContext);
                    }

                    // 2019/01/29 Modify by Ruling for 企業戶無卡提款：註銷舊卡-關閉企業戶無卡提款服務
                    // 關閉企業戶舊卡無卡提款服務
                    if (oldDeCard.getCardNcstatusB() == ATMNCCardStatus.Apply.getValue()) {
                        oldDeCard.setCardNcstatusB((short) ATMNCCardStatus.Close.getValue());// 註銷
                        oldDeCard.setCardNccloseBDate(this.getATMBusiness().getFeptxn().getFeptxnTxDate());
                        oldDeCard.setCardNccloseBTime(this.getATMBusiness().getFeptxn().getFeptxnTxTime());
                        isOldCardNCApply_B = true;
                        this.logContext.setRemark("關閉企業戶舊卡無卡提款服務");
                        logMessage(Level.INFO, this.logContext);
                    }

                    // 舊卡更新狀態
                    if (cardExtMapper.updateByPrimaryKey(oldDeCard) <= 0) {
                        this._rtnCode = FEPReturnCode.CARDUpdateError;
                    } else {
                        // add by Maxine on 2011/08/29 for P1.5, 寫入卡片異動資料
                        this._rtnCode = this.getATMBusiness().prepareCARDTXN(oldDeCard);
                        // 2020/04/20 Modify by Ruling for 新增Log紀錄
                        this.logContext.setRemark("註銷舊卡狀態");
                        logMessage(Level.INFO, this.logContext);

                        // 2017/08/04 Modify by Ruling for 註銷舊卡-關閉無卡提款服務
                        // 關閉舊卡無卡提款服務，再多寫入一筆交易代號為NCS的資料至卡片異動檔
                        if (isOldCardNCApply) {
                            this.getATMBusiness().getFeptxn().setFeptxnTxCode("NCS");
                            this._rtnCode = this.getATMBusiness().prepareNCCARDTXN(oldDeCard);
                        }

                        // 2019/01/29 Modify by Ruling for 企業戶無卡提款：註銷舊卡-關閉企業戶無卡提款服務
                        // 關閉企業戶舊卡無卡提款服務，再多寫入一筆交易代號為NWA的資料至卡片異動檔
                        if (isOldCardNCApply_B) {
                            this.getATMBusiness().getFeptxn().setFeptxnTxCode("NWA");
                            this.getATMBusiness().getFeptxn().setFeptxnNoticeId("5");// '舊卡要給5註銷
                            this._rtnCode = this.getATMBusiness().prepareNCCARDTXN(oldDeCard);
                        }
                    }
                }
                this.getATMBusiness().getCard().setCardNbcd(this.getATMBusiness().getCard().getCardStatus());
                this.getATMBusiness().getCard().setCardStatus((short) ATMCardStatus.Start.getValue());

//	            '2012-01-03 modified by KK for 新卡寫入啟用日期
                this.getATMBusiness().getCard().setCardLtxDate(this.getATMBusiness().getFeptxn().getFeptxnTxDate());
                this.getATMBusiness().getCard().setCardLtxTime(this.getATMBusiness().getFeptxn().getFeptxnTxTime());
//	                '2014/11/03 modified by Ruling for 新卡寫入卡片首次啟用日期/時間
                this.getATMBusiness().getCard().setCardActiveDate(this.getATMBusiness().getFeptxn().getFeptxnTxDate());
                this.getATMBusiness().getCard().setCardActiveTime(this.getATMBusiness().getFeptxn().getFeptxnTxTime());
                this.getATMBusiness().getFeptxn().setFeptxnTxCode("PNX");// ' 寫入 CardTxn

//	            '2020/01/30 Modifby by Ruling for 線上換發Debit卡：線上換發Debit卡PNX開卡流程，因不送信用卡B01電文一律將COMBINE改為「6:客戶啟用二卡合一功能」
                if (BINPROD.Debit.equals(
                        TxHelper.getCardType(this.getATMBusiness().getCard().getCardType().byteValue()).getCrdrmark())
                        && DbHelper.toBoolean(
                        TxHelper.getCardType(this.getATMBusiness().getCard().getCardType().byteValue())
                                .getFlowonlinedebit())) {
                    this.getATMBusiness().getCard().setCardCombine((short) ATMCardCombine.CombineStart.getValue());
                } else {
                    if (!this.getATMBusiness().getFeptxn().getFeptxnReplyCode().equals(OpenCardTimeout)) {
                        this.getATMBusiness().getCard().setCardCombine((short) ATMCardCombine.CombineStart.getValue());
                    }
                }
//
//	            '2017/11/09 Modify by Ruling for 註銷舊卡-新卡開啟時，如有申請無卡提款服務，一併開啟無卡提款服務
//	            '開啟新卡無卡提款服務
                if (isOldCardNCApply) {
                    this.getATMBusiness().getCard().setCardAuth(oldDeCard.getCardAuth());
                    this.getATMBusiness().getCard().setCardNckeykind(oldDeCard.getCardNckeykind());
                    this.getATMBusiness().getCard().setCardNcstatus((short) ATMNCCardStatus.Apply.getValue());
                    this.getATMBusiness().getCard().setCardErrCnt(oldDeCard.getCardErrCnt());
                    this.getATMBusiness().getCard().setCardNcactiveDate(oldDeCard.getCardNcactiveDate());
                    this.getATMBusiness().getCard().setCardNcactiveTime(oldDeCard.getCardNcactiveTime());
                    this.logContext.setRemark("開啟新卡無卡提款服務");
                    logMessage(Level.INFO, this.logContext);
                }

//	            '2019/01/29 Modify by Ruling for 企業戶無卡提款：企業戶註銷舊卡-新卡開啟時，如有申請無卡提款服務，一併開啟無卡提款服務
                if (isOldCardNCApply_B) {
                    this.getATMBusiness().getCard().setCardAuth(oldDeCard.getCardAuth());
                    this.getATMBusiness().getCard().setCardNckeykind(oldDeCard.getCardNckeykind());
                    this.getATMBusiness().getCard().setCardNcstatusB((short) ATMNCCardStatus.Apply.getValue());
                    this.getATMBusiness().getCard().setCardErrCnt(oldDeCard.getCardErrCnt());
                    this.getATMBusiness().getCard().setCardNcactiveBDate(oldDeCard.getCardNcactiveBDate());
                    this.getATMBusiness().getCard().setCardNcactiveBTime(oldDeCard.getCardNcactiveBTime());
                    this.logContext.setRemark("企業戶開啟新卡無卡提款服務");
                    logMessage(Level.INFO, this.logContext);
                }

//	            '新卡更新狀態
                if (this.cardExtMapper.updateByPrimaryKey(this.getATMBusiness().getCard()) <= 0) {
                    this._rtnCode = FEPReturnCode.CARDUpdateError;
                } else {
//	                'add by Maxine on 2011/08/29 for P1.5, 寫入卡片異動資料
                    this._rtnCode = this.getATMBusiness().prepareCARDTXN(this.getATMBusiness().getCard());

//	                '2017/11/09 Modify by Ruling for 註銷舊卡-新卡開啟時，如有申請無卡提款服務，一併開啟無卡提款服務
//	                '開啟新卡無卡提款服務，再多寫入一筆交易代號為NCS的資料至卡片異動檔
                    if (isOldCardNCApply) {
                        this.getATMBusiness().getFeptxn().setFeptxnTxCode("NCS");
                        this._rtnCode = this.getATMBusiness().prepareNCCARDTXN(this.getATMBusiness().getCard());
                    }

//	                '2019/01/29 Modify by Ruling for 企業戶無卡提款：企業戶註銷舊卡-新卡開啟時，如有申請無卡提款服務，一併開啟無卡提款服務
//	                '企業戶開啟新卡無卡提款服務，再多寫入一筆交易代號為NWA的資料至卡片異動檔
                    if (isOldCardNCApply_B) {
                        this.getATMBusiness().getFeptxn().setFeptxnTxCode("NWA");
                        this.getATMBusiness().getFeptxn().setFeptxnNoticeId("1");// '新卡要給1啟用
                        this._rtnCode = this.getATMBusiness().prepareNCCARDTXN(this.getATMBusiness().getCard());
                    }
                }
            }
        } catch (Exception ex) {
            this._rtnCode = CommonReturnCode.ProgramException;
            this.logContext.setProgramException(ex);
            sendEMS(this.logContext);
        }

    }

    //	     <summary>
//	     處理PrepareATMResponseData的部分複雜電文
//	     這裏只處理CashAdvance的電文的欄位值
//	     </summary>
//	     <remarks></remarks>
//	     <history>
//	       <modify>
//	         <modifier>Kyo</modifier>
//	         <reason>降低程式碼複雜度</reason>
//	         <date>2010/2/24</date>
//	       </modify>
//	       <modify>
//	         <modifier>Kyo</modifier>
//	         <reason>spec modify: for P2整理</reason>
//	         <date>2010/10/04</date>
//	       </modify>
//	     </history>
    
    	//ben20221118      
    	public String prepareATMResponseData() throws Exception {
    		return "";
    	}
}
