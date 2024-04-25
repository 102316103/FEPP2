package com.syscom.fep.server.common.business.cbsbusiness;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.mybatis.ext.mapper.CbspendExtMapper;
import com.syscom.fep.mybatis.model.Cbspend;
import com.syscom.fep.server.common.adapter.IMSAdapter;
import com.syscom.fep.server.common.business.BusinessBase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Date;

public class CBS extends BusinessBase {
    private Object tita = null;

    private ACBSAction action;

    private MessageBase txData;

    //private FeptxnExtMapper feptxnExtMapper = SpringBeanFactoryUtil.getBean(FeptxnExtMapper.class);

    private CbspendExtMapper cbspendExtMapper = SpringBeanFactoryUtil.getBean(CbspendExtMapper.class);

    /**
     * @param action (CBS電文AA)
     * @param txData
     */
    public CBS(ACBSAction action, MessageBase txData) {
        this.action = action;
        this.txData = txData;
        this.feptxn = txData.getFeptxn();
        this.feptxnDao = txData.getFeptxnDao();
        this.logContext = txData.getLogContext();
        this.ej = txData.getEj();
    }

    /**
     * Bruce add 組送CBS主機原存交易電文
     *
     * @param txType FEP電子日誌序號  0:查詢, 1:入扣帳, 2:沖正, 3:授權, 4:解圏, 5.註記(自行提款用)
     * @return
     * @throws Exception
     */
    public FEPReturnCode sendToCBS(String txType, String TxRs) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.HostResponseTimeout;
        // 檢核CBS主機連線狀態
        if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatCbs())) {
            if (this.feptxn.getFeptxnFiscFlag() == 1) {/*跨行 */
                /* 原存行交易 */
                if (StringUtils.isEmpty(this.feptxn.getFeptxnTxCode())) {
                    // 收信單位主機未在跨行作業運作狀態ReceiverBankOperationStop
                    return FEPReturnCode.ReceiverBankOperationStop;
                } else {/* 代理行交易 */
                    /*發信單位該項跨行業務停止SenderBankServiceStop*/
                    return FEPReturnCode.SenderBankServiceStop;
                }
            }
        }

        IMSAdapter adapter = new IMSAdapter(this.txData);
        // 組CBS 原存交易Request電文
        rtnCode = action.getCbsTita(txType);
        tita = action.getoTita();

        //本地測試暫時註解，正式環境需open
        if (!EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAIACC00") &&
                !EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAIINQ00")) {
            RefString cbsmac = new RefString("");
            rtnCode = this.makeCBSMac(cbsmac);
            if (rtnCode != FEPReturnCode.Normal) {
                this.logContext.setRemark("make cbsmac EBCDIC:" + cbsmac.get());
                logMessage(this.logContext);
                return rtnCode;
            }

            this.logContext.setRemark("make cbsmac EBCDIC:" + cbsmac.get());
            logMessage(this.logContext);
            /* 更新Tita 電文第 116~120(ASCII) 位置值 */
            StringBuilder resultBuilder = new StringBuilder(this.action.getTitaString());
            resultBuilder.replace(232, 240, cbsmac.substring(0, 8));
            this.action.setTitaToString(resultBuilder.toString());
            this.logContext.setMessage("after makeCBSMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }
        //-----------------------------


        String processType = this.getImsPropertiesValue(tita, ImsMethodName.PROCESS_TYPE.getValue());
        switch (txType) {
            case "0": /*查詢，紀錄手續費優惠次數*/
                if (processType.equals("CHK")) {
                    this.feptxn.setFeptxnMsgflow("N1"); /* CBS CR REQUEST */
                } else {
                    this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                }
                break;
            case "1": /*入扣帳*/
                if (processType.equals("ACCT")) {
                    this.feptxn.setFeptxnMsgflow("H1"); /*CBS Req */
                } else {
                    this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                }
                break;
            case "2":
            case "5":   /*沖正, 註記*/
                this.feptxn.setFeptxnMsgflow("X1"); /*CBS EC Req*/
                break;
            case "4":  /*沖正手續費優惠次數*/
                this.feptxn.setFeptxnMsgflow("R1"); /*CBS 沖正手續費優惠次數*/
                break;
            case "3": /*授權*/
                this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                break;
            case "6":  /*確認*/
                this.feptxn.setFeptxnMsgflow("H1"); /*CBS Req */
                break;
        }
        if (rtnCode == FEPReturnCode.Normal) {
            if (StringUtils.isBlank(TxRs)) {
                //交易上送主機才需更新FEPTXN
                this.feptxn.setFeptxnCbsTimeout((short) 1); /* CBS逾時 FLAG */
                this.feptxn.setFeptxnAccType((short) 4);// 未明
                this.feptxn.setFeptxnTxrust("S");
                rtnCode = FEPReturnCode.HostResponseTimeout;
                if (this.feptxnDao.updateByPrimaryKeySelective(this.feptxn) <= 0) {// 回寫失敗
                    this.getLogContext().setRemark("回寫(FEPTXN)發生錯誤");
                    this.logMessage(this.getLogContext());
                    return FEPReturnCode.UpdateFail;
                }
                adapter.setMessageToIMS(this.action.getTitaString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("MessageToIMS:" + adapter.getMessageToIMS());
                this.logContext.setRemark("Get CBS Text");
                this.logContext.setProgramFlowType(ProgramFlow.CBSIn);
                logMessage(Level.INFO, logContext);

                //新增test log
                adapter.setASCIIMessageToIMS(this.action.getASCIItitaToString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("ASCII MessageToIMS:" + adapter.getASCIIMessageToIMS());
                this.logContext.setRemark("ASCII CBS LOG");
                this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                logMessage(Level.INFO, logContext);

                rtnCode = adapter.sendReceive();
            } else if (TxRs.equals("N")) {
                adapter.setTxRs("N");
                adapter.setTimeout(0);
                this.feptxn.setFeptxnMsgflow("E");
                this.feptxn.setFeptxnCbsTimeout((short) 0);// CBS 逾時 FLAG
                if (this.feptxnDao.updateByPrimaryKeySelective(this.feptxn) <= 0) {// 回寫失敗
                    this.getLogContext().setRemark("回寫(FEPTXN)發生錯誤");
                    this.logMessage(this.getLogContext());
                    return FEPReturnCode.UpdateFail;
                }
                adapter.setMessageToIMS(this.action.getTitaString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("EndMessageToIMS:" + adapter.getMessageToIMS());
                this.logContext.setRemark("Get CBS Text");
                this.logContext.setProgramFlowType(ProgramFlow.CBSIn);
                logMessage(Level.INFO, logContext);

                //新增test log
                adapter.setASCIIMessageToIMS(this.action.getASCIItitaToString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("ASCII EndMessageToIMS:" + adapter.getASCIIMessageToIMS());
                this.logContext.setRemark("ASCII CBS LOG");
                this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                logMessage(Level.INFO, logContext);

                rtnCode = adapter.sendReceive();
                return FEPReturnCode.Normal;
            }
            //4. 	設定TIMER等待CBS主機回應訊息
            if (rtnCode == FEPReturnCode.HostResponseTimeout
                    || rtnCode == FEPReturnCode.ProgramException
                    || rtnCode == FEPReturnCode.CBSResponseError) {
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("GetMessageFromIMS Error");
                this.logContext.setRemark("SendCBSPend");
                this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                logMessage(Level.INFO, logContext);
                this.cbsPendProcess(txType);
                return rtnCode;
            }

            this.logContext.setProgramName("CBS.SendToCBS");
            this.logContext.setProgramFlowType(ProgramFlow.CBSOut);
            this.logContext.setMessage("MessageFromIMS:" + adapter.getMessageFromIMS());
            this.logContext.setRemark("After Send to CBS, ReturnCode:" + rtnCode);
            logMessage(Level.INFO, logContext);
            if (StringUtils.isNotBlank(adapter.getMessageFromIMS())) {
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setProgramFlowType(ProgramFlow.CBSOut);
                String ASCIIFromIMS = EbcdicConverter.fromHex(CCSID.English, adapter.getMessageFromIMS().substring(0, 192)) + adapter.getMessageFromIMS().substring(192, 200) + EbcdicConverter.fromHex(CCSID.English, adapter.getMessageFromIMS().substring(200));
                this.logContext.setMessage("ASCII MessageFromIMS:" + ASCIIFromIMS);
                this.logContext.setRemark("After Send to CBS, ReturnCode:" + rtnCode);
                logMessage(Level.INFO, logContext);
            }
            //5. 	收到CBS主機回應電文
            if (StringUtils.isNotBlank(adapter.getMessageFromIMS())) {//正常狀態
                rtnCode = this.action.processCbsTota(adapter.getMessageFromIMS(), txType);
                this.logContext.setMessage("after processCbsTota RC:" + rtnCode.toString());
                logMessage(this.logContext);
                //本地測試暫時註解，正式環境需open
                if (!EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAIACC00") &&
                        !EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAIINQ00")) {
                    String IMS_MAC = this.getImsPropertiesValue(this.action.getTota(), ImsMethodName.IMS_MAC.getValue());
                    this.logContext.setMessage("Tota IMS_MAC:" + IMS_MAC);
                    logMessage(this.logContext);
                    if (StringUtils.isBlank(IMS_MAC)) {
                        //寫EMS CheckCBSMacErr，繼續執行
                        this.logContext.setRemark("IMS_MAC is null or empty, ReturnCode:" + FEPReturnCode.ENCCheckMACError);
                        sendEMS(getLogContext());
                    } else {
                        this.logContext.setMessage("Before checkCBSMac RC:" + adapter.getMessageFromIMS().substring(192, 200));
                        logMessage(this.logContext);
                        FEPReturnCode rtnCode2 = this.checkCBSMac(adapter.getMessageFromIMS().substring(0, 136), adapter.getMessageFromIMS().substring(192, 200));

                        this.logContext.setMessage("after checkCBSMac RC:" + rtnCode2.toString());
                        logMessage(this.logContext);

                        if (rtnCode2 != FEPReturnCode.Normal) {
                            //寫EMS CheckCBSMacErr，繼續執行
                            this.logContext.setRemark("After checkCBSMac, ReturnCode:" + rtnCode2);
                            sendEMS(getLogContext());
                            rtnCode = rtnCode2;
                        }
                    }
                }
                //-----------------------------

                /* 依取得的主機帳務日，更換本行營業日 */
                String IMSBUSINESS_DATE = this.getImsPropertiesValue(this.action.getTota(), ImsMethodName.IMSBUSINESS_DATE.getValue());
                
                this.logContext.setMessage("IMSBUSINESS_DATE:" + IMSBUSINESS_DATE);
                logMessage(this.logContext);
                
                if(rtnCode ==  FEPReturnCode.Normal && StringUtils.isNotBlank(IMSBUSINESS_DATE)) {//IMSBUSINESS_DATE
                	IMSBUSINESS_DATE = CalendarUtil.rocStringToADString(IMSBUSINESS_DATE);
                	this.logContext.setMessage("AFTER TO ADSTRING IMSBUSINESS_DATE:" + IMSBUSINESS_DATE);
                    logMessage(this.logContext);
                	
                	rtnCode = ChangeZoneDay(IMSBUSINESS_DATE);
                }
                /* 變更交易記錄 */
                this.feptxn.setFeptxnMsgflow(this.feptxn.getFeptxnMsgflow().substring(0, 1) + "2");
                this.feptxn.setFeptxnCbsTimeout((short) 0);// CBS 逾時 FLAG
            }
        }
        return rtnCode;
    }

    private FEPReturnCode checkCBSMac(String TOTA, String IMS_MAC) {
        FEPReturnCode rtnCode = FEPReturnCode.ENCCheckMACError;
        try {

            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(txData);

            String inputData1 = remainderToF0(TOTA);

            //oldmac
            String oldMAC = IMS_MAC;

            // 3.呼叫
            RefString mac = new RefString(null);
            rtnCode = encHelper.makeCbsMacNew(inputData1, mac);
            String newMAC = mac.get();
            this.logContext.setRemark("checkCBSMac newmac:" + newMAC + ", oldmac:" + oldMAC);
            logMessage(this.logContext);
            if (oldMAC.equals(newMAC))
                rtnCode = FEPReturnCode.Normal;
            else
                rtnCode = FEPReturnCode.ENCCheckMACError;
        } catch (Exception e) {
            this.logContext.setRemark("checkCBSMac newmac:" + "失敗");
            logMessage(this.logContext);
            return rtnCode = FEPReturnCode.ENCCheckMACError;
        }
        return rtnCode;
    }

    /**
     * 把字串補足至 8 的倍數 ，不足以F0補足
     *
     * @return
     */
    private String remainderToF0(String inputData) {
        String rtnStr = "";
        int instr = inputData.length();
        instr = instr / 2;
        if (instr % 8 != 0) {
            int remainder = instr % 8;
            remainder = 8 - remainder;
            rtnStr = StringUtils.rightPad(rtnStr, remainder, '0');
            rtnStr = EbcdicConverter.toHex(CCSID.English, rtnStr.length(), rtnStr);
        }

        return inputData + rtnStr;
    }

    private String TrimSpace(String data) {
        String result = "";

        for (int i = 0; i < data.length(); i += 2) {

            String tmp = data.substring(i, i + 2);
            //System.out.println(tmp);
            if (i == 0) {
                result += tmp;
            } else {
                if (tmp.equals("40")) {
                    if (!result.substring(result.length() - 2, result.length()).equals("40")) {
                        result += tmp;
                    }
                } else {
                    result += tmp;
                }
            }
        }
        return result;
    }

    /**
     * 產生主機的MAC資料
     *
     * @param cbsmac
     * @return
     */
    private FEPReturnCode makeCBSMac(RefString cbsmac) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(txData);
            String inputData = this.action.getTitaString().substring(0, 166);

            String inputData1 = inputData;
            inputData1 = remainderToF0(inputData1);
            // 3.呼叫
            RefString mac = new RefString(null);
            rtnCode = encHelper.makeCbsMacNew(inputData1, mac);
            // 4.若rtnCode=normal, 則cbsmac = mac
            if (rtnCode == FEPReturnCode.Normal) {
                cbsmac.set(mac.get());
            }
            this.logContext.setRemark("after makeCBSMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        } catch (Exception e) {
//			rtnCode = handleException(e, "makeCBSMac");
        }
        return rtnCode;
    }

    public FEPReturnCode sendToCBS(String txType) throws Exception {
        return sendToCBS(txType, "");
    }

    /**
     * 判斷及執行CBSPend處理
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode cbsPendProcess(String txType) throws Exception {
        if ("1".equals(txType) || "2".equals(txType)) {//入扣帳
            /* 入扣帳電文 TimeOut時,  才寫入 CBSPEND */
            Cbspend cbspend = new Cbspend();
            cbspend.setCbspendTxDate(this.feptxn.getFeptxnTxDate());
            cbspend.setCbspendZone("TWN");
            cbspend.setCbspendTxTime(this.feptxn.getFeptxnTxTime());
            cbspend.setCbspendEjfno(this.feptxn.getFeptxnEjfno());
            cbspend.setCbspendCbsTxCode(this.txData.getMsgCtl().getMsgctlTwcbstxid());
            if (txType.equals("1")) {
                cbspend.setCbspendReverseFlag((short) 0);
            } else {
                cbspend.setCbspendReverseFlag((short) 1);
            }
            cbspend.setCbspendSubsys(this.feptxn.getFeptxnSubsys());
            cbspend.setCbspendTbsdy(this.feptxn.getFeptxnTbsdy());
            cbspend.setCbspendTbsdyFisc(this.feptxn.getFeptxnTbsdyFisc());
            cbspend.setCbspendSuccessFlag((short) 0);//必須重送
            cbspend.setCbspendResendCnt((short) 0);
            cbspend.setCbspendAccType((short) 4);
            if (this.feptxn.getFeptxnTxAmtAct().compareTo(BigDecimal.ZERO) != 0) {
                cbspend.setCbspendTxAmt(this.feptxn.getFeptxnTxAmtAct());
            } else {
                cbspend.setCbspendTxAmt(this.feptxn.getFeptxnTxAmt());
            }
            if (this.feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                cbspend.setCbspendActno(this.feptxn.getFeptxnTroutActno());
                cbspend.setCbspendIbBkno(this.feptxn.getFeptxnTrinBkno());
                cbspend.setCbspendIbActno(this.feptxn.getFeptxnTrinActno());
            } else if (this.feptxn.getFeptxnTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                cbspend.setCbspendActno(this.feptxn.getFeptxnTrinActno());
                cbspend.setCbspendIbBkno(this.feptxn.getFeptxnTroutBkno());
                cbspend.setCbspendIbActno(this.feptxn.getFeptxnTroutActno());
            } else {
                cbspend.setCbspendActno(this.feptxn.getFeptxnAtmno());
                cbspend.setCbspendIbBkno(this.feptxn.getFeptxnTroutBkno());
                cbspend.setCbspendIbActno(this.feptxn.getFeptxnTroutActno());
            }
            cbspend.setCbspendPcode(this.feptxn.getFeptxnPcode());
            cbspend.setCbspendTita(this.action.getTitaString());
            /* 2023/5/2 修改 for  CBS帳務分類 */
            cbspend.setCbspendBkno(this.feptxn.getFeptxnBkno());
            cbspend.setCbspendStan(this.feptxn.getFeptxnStan());

            if (DbHelper.toBoolean(this.getFeptxn().getFeptxnFiscFlag())) {
                if (this.feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    if (this.feptxn.getFeptxnPcode().equals("2543") || this.feptxn.getFeptxnPcode().equals("2556")) {
                        cbspend.setCbspendCbsKind("A2"); /* 入帳交易(A2) */
                    } else {
                        cbspend.setCbspendCbsKind("A1"); /* 扣帳交易(A1) */
                    }
                } else if (this.feptxn.getFeptxnTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    cbspend.setCbspendCbsKind("A2"); /* 入帳交易(A2) */
                }
            }

            /* 外圍 */
            if (StringUtils.isNotBlank(this.feptxn.getFeptxnChannel())) {
                if (this.feptxn.getFeptxnChannel().substring(0, 2).equals("NB")
                        || this.feptxn.getFeptxnChannel().substring(0, 1).equals("M")
                        || this.feptxn.getFeptxnChannel().equals("EDI")
                        || this.feptxn.getFeptxnChannel().equals("EOI")
                        || this.feptxn.getFeptxnChannel().equals("HCA")
                        || this.feptxn.getFeptxnChannel().equals("VO")) {
                    if (DbHelper.toBoolean(this.getFeptxn().getFeptxnFiscFlag())) {
                        cbspend.setCbspendCbsKind("A1");
                    } else {
                        cbspend.setCbspendCbsKind("A3");
                    }
                }
                if (this.feptxn.getFeptxnChannel().equals("NAM")) { /*整批轉即時*/
                    cbspend.setCbspendCbsKind("A1");
                }
                if (this.feptxn.getFeptxnChannel().equals("ATM")
                        || this.feptxn.getFeptxnChannel().equals("EAT")
                        || this.feptxn.getFeptxnChannel().equals("POS")) {
                    if (txType.equals("2")) { //沖正
                        cbspend.setCbspendCbsKind("A1");
                    } else {
                        cbspend.setCbspendCbsKind("A3");
                    }
                }
            }

            cbspend.setUpdateTime(new Date());
            cbspend.setUpdateUserid(0);
            //寫入 CBSPEND
            if (cbspendExtMapper.insertSelective(cbspend) < 1) {// 寫入CBSPEND失敗
                this.getLogContext().setRemark("CBSPendProcess-Insert CBSPEND Error");
                this.getLogContext().setReturnCode(FEPReturnCode.CBSPENDInsertError);
                logMessage(Level.INFO, logContext);
                return FEPReturnCode.Normal;
            }
            //寫入Queue 暫時關閉
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            String message = this.feptxn.getFeptxnTxDate() + ":" + this.feptxn.getFeptxnEjfno();
            sender.sendQueue(configuration.getQueueNames().getCbspend().getDestination(), message, null, null);
        }
        return FEPReturnCode.Normal;
    }
}
