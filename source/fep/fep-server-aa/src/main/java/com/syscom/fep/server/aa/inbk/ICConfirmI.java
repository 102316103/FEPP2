package com.syscom.fep.server.aa.inbk;

//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
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
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.IctltxnExtMapper;
import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 處理財金發動晶片金融卡跨國確認電文
 * @author User	-> Ben (SA=Sarah)
 *
 */
public class ICConfirmI extends INBKAABase{
    private Ictltxn defICTLTXN = new Ictltxn();

    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    //private FEPReturnCode _rtnCode1 = CommonReturnCode.Normal;
    private FEPReturnCode rc = CommonReturnCode.Normal;
    private FEPReturnCode rc2 = CommonReturnCode.Normal;
    private boolean isExitProgram = false;
    
    /**
     AA的建構式,在這邊初始化及設定其他相關變數

     @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件


     */
    public ICConfirmI(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     */
    @Override
    public String processRequestData() {
        try {
        	//1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
        	rc = this.processRequest();
        	//程式結束
			if(isExitProgram) {
				return StringUtils.EMPTY;	
			}
        	if (CommonReturnCode.Normal.equals(rc)) {
                //2.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
        		rc = this.checkBusinessRule();
        		
        		//EXIT PROGRAM
    			if(isExitProgram) {
    				return StringUtils.EMPTY;	
    			}
        		
        		/*
        		//EXIT PROGRAM
        		if(FISCReturnCode.MessageFormatError.equals(rc)) {
        			getLogContext().setMessage(rc.toString());
					getLogContext().setRemark("格式發生錯誤!!");
					logMessage(Level.INFO, getLogContext());
        			return StringUtils.EMPTY;
        		}*/
        		
        		if (CommonReturnCode.Normal.equals(rc)) {
        			//3.UpdateTxData:更新交易記錄 (FEPTXN& ICTLTXN )
            		rc = this.updateTxData();
            		
            		//程式結束
            		if(isExitProgram) {
            			getLogContext().setMessage(rc.toString());
    					getLogContext().setRemark("寫入檔案發生錯誤!!");
    					logMessage(Level.INFO, getLogContext());
        				return StringUtils.EMPTY;
                    }
        		}
            }
            //4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS)
            _rtnCode = processAPTOTSendToCBS();

        	//6.更新交易記錄(FEPTXN) : if need
            _rtnCode = updateFEPTXN();

        	//7.判斷是否需傳送2160電文給財金
        	if(StringUtils.isNotBlank(feptxn.getFeptxnSend2160())
					&& "Y".equals(feptxn.getFeptxnSend2160())) {
				/* 寫入2160發送資料檔 */
				this.insertINBK2160();
			}
            //8.FEP通知主機交易結束
            _rtnCode = SendToCBS();
        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
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
    /**
     * 1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
     * @return
     */
    private FEPReturnCode processRequest() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            //檢核Header
        	//1.1 拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
            rtnCode = getFiscBusiness().checkHeader(getFiscCon(), true);
            if (rtnCode !=CommonReturnCode.Normal) {
                getFiscBusiness().setFeptxn(null);
                getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
                isExitProgram = true;
                return rtnCode;
            }

            getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
            getTxData().setFeptxn(getFiscBusiness().getFeptxn());

            //FISCRC=6101(跨行可用餘額小於零，不得交易)，要寄Email需將值塞入LogContext.TroutActno
            getLogContext().setAtmNo(getFiscBusiness().getFeptxn().getFeptxnAtmno());
            getLogContext().setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
            getLogContext().setTrinBank(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
            getLogContext().setTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
            getLogContext().setTroutBank(getFiscBusiness().getFeptxn().getFeptxnTroutBkno());
            
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".ProcessRequest");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 2.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
     * @return
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
        String wk_TX_DATE = null;

        try {
            //2.1 檢核 Mapping 欄位 
            if(Integer.parseInt(this.getFiscCon().getTxnInitiateDateAndTime().substring(0,2)) < 90) {
                wk_TX_DATE = CalendarUtil.rocStringToADString("20110000" + this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6));
            }else {
                wk_TX_DATE = CalendarUtil.rocStringToADString("19110000" + this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6));
            }
            
//			if (!wk_TX_DATE.equals(getFiscBusiness().getFeptxn().getFeptxnReqDatetime().substring(0, 8))
//					|| !getFiscBusiness().getFeptxn().getFeptxnReqDatetime().substring(8, 14)
//							.equals(getFiscCon().getTxnInitiateDateAndTime().substring(6, 12))
//					|| !getFiscBusiness().getFeptxn().getFeptxnDesBkno()
//							.equals(getFiscCon().getTxnDestinationInstituteId().substring(0, 3))
//					|| (StringUtils.isNotBlank(getFiscCon().getATMNO())
//							&& !getFiscCon().getATMNO().equals(getFiscBusiness().getFeptxn().getFeptxnAtmno()))
//					|| (StringUtils.isNotBlank(getFiscCon().getTxAmt()) && MathUtil
//							.compareTo(getFiscBusiness().getFeptxn().getFeptxnTxAmt(), getFiscCon().getTxAmt()) != 0)) {
//				return FISCReturnCode.OriginalMessageDataError;
//			}

            /* Confirm電文比對原交易 */
            String aa=  (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0,6),7,"0"))) + this.getFiscCon().getTxnInitiateDateAndTime().substring(6,12);
            if(!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(aa) ||
                    (!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0,3))) ||
                    (StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))
                    ||(StringUtils.isNotBlank(this.getFiscCon().getTxAmt()) && !new DecimalFormat("0.00").format(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct()).equals(new DecimalFormat("0.00").format(new BigDecimal(this.getFiscCon().getTxAmt()))))) {
                return FEPReturnCode.OriginalMessageDataError;
            }

			//2.2 檢核交易是否未完成
			if (!"B".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				//10/20 修改, 財金錯誤代碼改為 '0101' 
				rtnCode = FISCReturnCode.MessageFormatError;   //**相關欄位檢查錯誤
				return rtnCode;
			}

				/*9/22 修改 for CON 送2次 */
            if (getFiscBusiness().getFeptxn().getFeptxnTraceEjfno() != 0) {
                getLogContext().setRemark("已有Confirm電文, FEPTXN_TRACE_EJFNO=" + getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().toString());
                logMessage(Level.INFO, getLogContext());
                rtnCode = FISCReturnCode.MessageFormatError; // **相關欄位檢查錯誤
                getFiscBusiness().setFeptxn(null); //第2次Confirm則不更新FEPTXN
                getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
                isExitProgram = true;
                return rtnCode;
            }

            //2.3 檢核 MAC
            getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
			// '2017/11/17 Modify by Ruling for 收到財金確認電文時間寫入FEPTXN
			getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			rtnCode = encHelper.checkFiscMac(getFiscCon().getMessageType(), getFiscCon().getMAC());
            this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnConRc(null);
                return FEPReturnCode.ENCCheckMACError;//**訊息押碼錯誤

            }
			return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 3.UpdateTxData:更新交易記錄 (FEPTXN& ICTLTXN )
     * @return
     */
    private FEPReturnCode updateTxData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        IctltxnExtMapper dbICTLTXN = SpringBeanFactoryUtil.getBean(IctltxnExtMapper.class);
        int iRes = 0;
        try {
            getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));
            if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
                if (NormalRC.FISC_ATM_OK.equals(getFiscCon().getResponseCode())) {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("C"); //Accept-Reverse
                }
                getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
            }
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); //F3
            getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
            getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());

            //換日後fiscBusiness.DBFEPTXN 以及txData.FEPTXN都應該使用實際交易日期，避免後面更新不到FEPTXN
            //feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8),StringUtils.join(ProgramName, "updateTxData"));
            //getTxData().setFeptxnDao(feptxnDao);
            //getFiscBusiness().setFeptxnDao(feptxnDao);
            this.feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData")); // 2021-06-16 Richard add

            getTxData().setFeptxnDao(this.feptxnDao);
            getFiscBusiness().setFeptxnDao(this.feptxnDao);


            int i = feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getFeptxn());
            if (i <= 0) {
                transactionManager.rollback(txStatus);
                isExitProgram = true;
                return IOReturnCode.FEPTXNUpdateError;
            }
            defICTLTXN.setIctltxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
            defICTLTXN.setIctltxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
            defICTLTXN = dbICTLTXN.selectByPrimaryKey(defICTLTXN.getIctltxnTxDate(),defICTLTXN.getIctltxnEjfno());
            defICTLTXN.setIctltxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
            defICTLTXN.setIctltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
            //Fly 2019/02/13 for 晶片卡跨國提款/消費扣款沖正(2573/2549)
            defICTLTXN.setIctltxnTxFeeCr(getFiscBusiness().getFeptxn().getFeptxnTxFeeCr());
            defICTLTXN.setIctltxnActProfit(getFiscBusiness().getFeptxn().getFeptxnActProfit());
            iRes = dbICTLTXN.updateByPrimaryKeySelective(defICTLTXN);
            if (iRes <= 0) {
                transactionManager.rollback(txStatus);
                isExitProgram = true;
                return IOReturnCode.UpdateFail;
            }
            transactionManager.commit(txStatus);
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            //若失敗則復原
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS)
     * @return
     * @throws Exception
     */
    private FEPReturnCode processAPTOTSendToCBS() throws Exception {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            //+REP
            if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                //-CON
            	/*沖轉跨行代收付*/
                rc = getFiscBusiness().processICAptot(true);

                getLogContext().setProgramName(ProgramName);
                getLogContext().setRemark(StringUtils.join("FEPTXN_CBS_TX_CODE=",getFiscBusiness().getFeptxn().getFeptxnCbsTxCode()));
                logMessage(Level.DEBUG, getLogContext());

                /*沖轉主機帳務*/
                if (getTxData().getMsgCtl().getMsgctlCbsFlag() == 2) {//沖正
                	String TxType = "2";		//上CBS沖正
                    this.getTxData().setIctlTxn(defICTLTXN);
                    String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
        			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
        			rc2 = new CBS(hostAA, getTxData()).sendToCBS(TxType);
                }
                getLogContext().setProgramName(ProgramName);
                TxHelper.getMessageFromFEPReturnCode(getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC, getLogContext());
            }
        }
        return CommonReturnCode.Normal;
    } 
    
    /**
     * 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS)
     * @return
     * @throws Exception
     */
    private FEPReturnCode processAPTOTSendToCBS_old() throws Exception {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        T24 hostT24 = new T24(getTxData());

        if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
            //+REP
            if ( !NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                //-CON
            	/*沖轉跨行代收付*/
                rtnCode = getFiscBusiness().processICAptot(true);

                getLogContext().setProgramName(ProgramName);
                getLogContext().setRemark(StringUtils.join("FEPTXN_CBS_TX_CODE=",getFiscBusiness().getFeptxn().getFeptxnCbsTxCode()));
                logMessage(Level.DEBUG, getLogContext());
                
                /*沖轉主機帳務*/
                if (getTxData().getMsgCtl().getMsgctlCbsFlag() == 2) {//沖正
                    rc2 = hostT24.sendToT24(getFiscBusiness().getFeptxn().getFeptxnCbsTxCode(), 2, true);
                }

                getLogContext().setProgramName(ProgramName);
                TxHelper.getMessageFromFEPReturnCode(getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC, getLogContext());
            } else {
                //+CON
                if (getTxData().getMsgCtl().getMsgctlCbsFlag() == 1) {//入帳
                    rtnCode = hostT24.sendToT24(getFiscBusiness().getFeptxn().getFeptxnCbsTxCode(), 1, true);
                }

                //2018/12/26 Modify by Ruling for 避免2572沖正交易，原交易結果已更新為D沖正，但因2571 Confirm發送MailHunter逾時後又將交易結果更新為A成功，將此段移至後面發簡訊及EMAIL
                //'Fly 2018/03/12 修改 for 跨國提款, 發簡訊功能
                //If fiscBusiness.FepTxn.FEPTXN_ZONE_CODE = "TWN" AndAlso fiscBusiness.FepTxn.FEPTXN_PCODE = CStr(FISCPCode.PCode2571) Then
                //    fiscBusiness.PrepareSMSMAIL()
                //End If
            }
        }

        return rtnCode;

    }

    /**
     * 6. 更新交易記錄(FEPTXN) : if need
     * @return
     */
    private FEPReturnCode updateFEPTXN() throws Exception{
        //FEPReturnCode rtnCode = CommonReturnCode.Normal;
        if (getFiscBusiness().getFeptxn().getFeptxnAaRc() == CommonReturnCode.Normal.getValue()) {
            if (rc != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rc.getValue());
            }else if(rc2 != CommonReturnCode.Normal) {
            	getFiscBusiness().getFeptxn().setFeptxnAaRc(rc2.getValue());
            }
        }
        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
        if (this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn()) > 0) {
            return FEPReturnCode.Normal;
        }else{
            return FEPReturnCode.UpdateFail;
        }
    }

    /**
     CON(+)發送簡訊及EMAIL


     <modify>
     <modifier>Ruling</modifier>
     <reason>避免2572沖正交易，原交易結果已更新為D沖正，但因2571 Confirm發送MailHunter逾時後又將交易結果更新為A成功，將此段移至後面發簡訊及EMAIL</reason>
     <date>2018/12/26</date>
     </modify>
     */
    private void sendToMailHunter() {
        try {
            if (getFiscBusiness().getFeptxn() != null && FISCPCode.PCode2571.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
                getFiscBusiness().prepareSMSMAIL();
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToMailHunter");
            sendEMS(getLogContext());
        }
    }

    /**
     * 7.判斷是否需傳送2160電文給財金
     * @return
     */
    public FEPReturnCode insertINBK2160() {
    	FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			rtnCode = getFiscBusiness().prepareInbk2160();
			if (rtnCode != CommonReturnCode.Normal) {
				if (rtnCode != CommonReturnCode.Normal) {
					getLogContext().setMessage(rtnCode.toString());
					getLogContext().setRemark("寫入檔案(INBK2160)發生錯誤!!");
					logMessage(Level.INFO, getLogContext());
					return FEPReturnCode.INBK2160InsertError;
				} else {
					return FEPReturnCode.Normal;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".AddTXData");
			sendEMS(getLogContext());
		}
		return FEPReturnCode.Normal;
	}
}
