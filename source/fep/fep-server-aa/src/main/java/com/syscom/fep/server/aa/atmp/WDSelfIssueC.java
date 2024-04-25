package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

/**
 * @author vincent
 */
public class WDSelfIssueC extends ATMPAABase {
    private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    boolean isGoResponse = false;
    String AATxTYPE = "";
    String atmno;
    private String tita;


    public WDSelfIssueC(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 記錄FEPLOG內容
            tita = EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage());
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage("ASCII TITA:" + tita);
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);

            // 1. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();

            //2.	SendToCBS(if need): 
            if(!isGoResponse && rtnCode == FEPReturnCode.Normal){
            	// 提款確認電文, 如ATM送Con(-), 須組I002電文送往CBS主機
            	//ATM第二道為失敗電文且FEP紀錄CBS已記帳
				if ("SE".equals(getATMRequest().getMSGTYP()) && feptxn.getFeptxnAccType() == 1) {
					AATxTYPE = "2"; // 上CBS沖正
				}
				if (StringUtils.isNotBlank(AATxTYPE)) {
					String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid(); // AA = MSGCTL_TWCBSTXID的電文
					feptxn.setFeptxnCbsTxCode(AA);
					ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
					rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
				}
            }

            if (!isGoResponse) {
                // 3. UpdateTxData: 更新交易記錄(FEPTxn)
                this.updateTxData();
            }

            // 4. Response:組ATM回應電文 & 回 ATMMsgHandler
            rtnMessage = this.response();

            // 5. 交易通知 (if need)
            this.sendToMailHunter();

            // 6. 交易結束通知主機
            AATxTYPE = "";
            String AATxRs = "N";
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1(); // AA = MSGCTL_TWCBSTXID的電文
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE,AATxRs);
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }

        try {
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage("MessageToATM:"+rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }

        return rtnMessage;
    }

    /**
     * 1. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {
        // 1.1 取得原交易之 FEPTXN
        Feptxn tempFeptxn = getATMBusiness().checkConData();
        feptxn = tempFeptxn;
        getATMBusiness().setFeptxn(tempFeptxn);
        getTxData().setFeptxn(feptxn);
        if (getATMBusiness().getFeptxn() == null) {
            rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            isGoResponse = true;
            return; // GO TO 4 /* 組 ATM 回應電文 */
        }

        // 1.2 將ATM確認電文, 準備寫入原交易 FEPTXN欄位
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GO TO 3 /* 更新交易記錄 */
        }

        // 1.3 交易確認電文檢核 MAC
		String ATM_TITA_PICCMACD = this.getATMRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError;
		}
        String newMac = ATM_TITA_PICCMACD;
        this.logContext.setMessage("Begin checkAtmMac mac:" + newMac);
        logMessage(this.logContext);
        
        // CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
        atmno = feptxn.getFeptxnAtmno();
        
        String wkMAC = this.getTxData().getTxRequestMessage().substring(36, 742); // EBCDIC(36,742)
		rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno,wkMAC, newMac);
        this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
        logMessage(this.logContext);
    }

    /**
     * 3. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); // (RESPONSE)
        feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                getTxData().getTxChannel(), getTxData().getLogContext()));
        feptxn.setFeptxnAaRc(rtnCode.getValue());
        /* For報表, 寫入處理結果 */
        if (rtnCode == FEPReturnCode.Normal) {
            if(AATxTYPE.equals("2")){
                feptxn.setFeptxnTxrust("C"); /* Con(+), 處理結果=成功 */
            }else {
                feptxn.setFeptxnTxrust("A"); /* Con(-), 處理結果=成功 */
            }
            /* for 無卡提款(W2) */
            if("W2".equals(feptxn.getFeptxnTxCode())){
                try {
                    /* 更新 NWDTXN */
                    Nwdtxn nwdtxn = nwdtxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(),feptxn.getFeptxnEjfno());
                    nwdtxn.setNwdtxnConRc(feptxn.getFeptxnConRc());
                    nwdtxn.setNwdtxnTxrust(feptxn.getFeptxnTxrust());
                    nwdtxn.setNwdtxnTxDate(feptxn.getFeptxnTxDate());
                    nwdtxn.setNwdtxnEjfno(feptxn.getFeptxnEjfno());
                    int updCount = nwdtxnMapper.updateByPrimaryKeySelective(nwdtxn);
                    if (updCount <= 0) {
                        rtnCode = FEPReturnCode.FEPTXNUpdateNotFound;
                    }
                } catch (Exception e) {
                    rtnCode = FEPReturnCode.FEPTXNUpdateError;
                    getLogContext().setProgramException(e);
                    getLogContext().setProgramName(ProgramName + ".updateTxData");
                    sendEMS(getLogContext());
                }
                if (rtnCode != FEPReturnCode.Normal) {
                    feptxn.setFeptxnAaRc(rtnCode.getValue());
                }
            }

        }

        // 回寫 FEPTXN
        /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
        FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
        }

        if (rtnCode2 != FEPReturnCode.Normal) {
            // 回寫檔案 (FEPTxn) 發生錯誤
            this.feptxn.setFeptxnReplyCode("L013");
            sendEMS(getLogContext());
        }
    }

    /**
     * 4. Response:組ATM回應電文 & 回 ATMMsgHandler
     *
     * @return
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            // 組 Header
            ATMGeneralRequest atmReq = this.getATMRequest();
            RefString rfs = new RefString();
            ATM_FSN_HEAD2 atm_fsn_head2 = new ATM_FSN_HEAD2();
            atm_fsn_head2.setWSID(atmReq.getWSID());
            atm_fsn_head2.setRECFMT("1");
            atm_fsn_head2.setMSGCAT("F");
            atm_fsn_head2.setMSGTYP("PC");
            atm_fsn_head2.setTRANDATE(atmReq.getTRANDATE()); // 西元後兩碼+系統月日共六碼
            atm_fsn_head2.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
            atm_fsn_head2.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
            atm_fsn_head2.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
            // PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易
            // (FC1、P1)主機才有可能依據狀況要求吃卡
            atm_fsn_head2.setPRCRDACT("0");
            if (feptxn == null) {
                atm_fsn_head2.setRECFMT("0");
            }

            /* CALL ENC 取得MAC 資料 */
            ENCHelper atmEncHelper = new ENCHelper(this.getTxData());

            rfs.set("");
            rtnMessage = atm_fsn_head2.makeMessage();
			rtnCode = atmEncHelper.makeAtmMacP3(atmno,rtnMessage, rfs);
			if (rtnCode != FEPReturnCode.Normal) {
				atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
			} else {
				atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
			}
            this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            rtnMessage = atm_fsn_head2.makeMessage();
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 5. 交易通知 (if need)
     *
     * @return
     * @throws Exception
     */
    private void sendToMailHunter() {
        try {
            String noticeType = feptxn.getFeptxnNoticeType();
            if (StringUtils.isNotBlank(noticeType) && "A".equals(feptxn.getFeptxnTxrust())) {
                switch (noticeType) {
                    case "P": /* 送推播 */
                        getATMBusiness().preparePush(this.feptxn);
                        break;
                    case "M": /* 簡訊 */
                        getATMBusiness().prepareSms(this.feptxn);
                        break;
                    case "E": /* Email */
                        getATMBusiness().prepareMail(this.feptxn);
                        break;
                }
            }
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
        }
    }

}
