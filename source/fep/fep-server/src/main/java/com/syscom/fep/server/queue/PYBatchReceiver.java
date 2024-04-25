package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.jms.JmsMonitorController;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.PYBatchQueueConsumers;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsbatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.NBFEPHandler;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.PostConstruct;
import javax.jms.Message;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@StackTracePointCut(caller = SvrConst.SVR_NB)
public class PYBatchReceiver extends FEPBase implements JmsReceiver<String> {
    private static final String PROGRAM_NAME = PYBatchReceiver.class.getSimpleName();

    private BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
    private NpsbatchExtMapper npsbatchExtMapper = SpringBeanFactoryUtil.getBean(NpsbatchExtMapper.class);
    private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
    private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);


    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(PYBatchQueueConsumers.class).subscribe(this));
    }

    /**
     * 接收訊息
     *
     * @param destination
     * @param payload
     * @param message
     */
    @Override
    public void messageReceived(String destination, String payload, Message message) {
        InnerClass inner = new InnerClass();
        inner.message = payload;
        inner.maxThreads = CMNConfig.getInstance().getSECMaxThreads();
        Boolean result = false;
        LogData logData = new LogData();
        writLog(logData, "destination:(" + destination + "),payload:(" + payload + ")", "NPAYReceiver", ".messageReceived");
        try {
            //1.依各檔案REC長度切資料，檢核後寫入檔案
            if (!inner.message.substring(0, 8).trim().equals("AMPAYFL")) {
                inner.W_ERR_CODE = "K018";
                inner.W_ERR_MSG = "傳送檔名錯誤";
                ACKResponse(inner, logData);
            } else {
                inner.W_REC_LEN = inner.message.substring(21, 24);
                inner.W_REC_COUNT = inner.message.substring(24, 29);
                /* 以 inner.W_REC_LEN 長度為基準，切割 TITA 每筆資料，
                   共 inner.W_REC_COUNT 筆，從第1 筆~ inner.W_REC_COUNT
                   MQ 傳送檔案指示共有 29 bytes，從30 byte開始扣帳資料 */
                int a1 = 29 + Integer.valueOf(inner.W_REC_LEN) * Integer.valueOf(inner.W_REC_COUNT);
                writLog(logData, inner.toString(), "NPAYReceiver", ".messageReceived");
                if (inner.message.length() > a1) {
                    inner.W_ERR_CODE = "K005";
                    inner.W_ERR_MSG = "資料別錯誤";
                    ACKResponse(inner, logData);
                } else {
                    result = check(inner, logData);
                }
            }

            //2.逐筆讀取 <NPSDTL>,產生 ATM request電文, 並以系統設定的Thread數執行
            if (result) {
                result = SubProcess(inner, logData);
            }

            //5.產生整批轉即時回饋檔
            if (result) {
                result = FeeBackSuccess(inner, logData);
            }
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class InnerClass {
        private String W_ERR_CODE;
        private String W_ERR_MSG;
        private String W_REC_LEN;
        private BigDecimal W_TOT_TX_AMT = BigDecimal.ZERO;
        private String W_REC_COUNT;
        private int Failcnt = 0;
        private int doTOTcnt = 0;
        private BigDecimal failamt = BigDecimal.ZERO;
        private BigDecimal doTOTamt = BigDecimal.ZERO;
        private int okcnt = 0;
        private int feecnt = 0;
        private BigDecimal okamt = BigDecimal.ZERO;
        private BigDecimal feeamt = BigDecimal.ZERO;
        private Integer W_SEQ_NO = 0;
        private String message;
        private Npsbatch npsbatch = new Npsbatch();
        private Npsdtl npsdtl = new Npsdtl();
        private Zone zone;
        private Bsdays bsdays;
        private String fileid;
        private String txdate;
        private String batchNo;
        private Integer maxThreads;
        private Integer threadNo;
        private BigDecimal charge;
        private String brch;
        private String chargeFlag;

        public BigDecimal getCharge() {
            return charge;
        }

        public void setCharge(BigDecimal charge) {
            this.charge = charge;
        }

        public String getBrch() {
            return brch;
        }

        public void setBrch(String brch) {
            this.brch = brch;
        }

        public String getChargeFlag() {
            return chargeFlag;
        }

        public void setChargeFlag(String chargeFlag) {
            this.chargeFlag = chargeFlag;
        }

        @Override
        public String toString() {
            return "InnerClass{" +
                    "W_ERR_CODE='" + W_ERR_CODE + '\'' +
                    ", W_ERR_MSG='" + W_ERR_MSG + '\'' +
                    ", W_REC_LEN='" + W_REC_LEN + '\'' +
                    ", W_TOT_TX_AMT=" + W_TOT_TX_AMT +
                    ", W_REC_COUNT='" + W_REC_COUNT + '\'' +
                    '}';
        }
    }

    private boolean FeeBackSuccess(InnerClass inner, LogData logData) {
        String batNo = inner.fileid + inner.txdate + inner.batchNo;
        StringBuilder sLine = null;
        String queue = "";
        try {
            List<Npsdtl> alld = npsdtlExtMapper.GetNPSDTLByBATNOforAll(batNo);
            /*MQ檔案傳送指示*/
            queue = queue + StringUtils.rightPad(inner.npsbatch.getNpsbatchFileId(), 8, " ");
            queue = queue + StringUtils.leftPad(inner.npsbatch.getNpsbatchBatchNo(), 13, "0");
            queue = queue + "230";
            queue = queue + StringUtils.leftPad(inner.npsbatch.getNpsbatchTotCnt().toString(), 5, "0");

            /* BODY */
            /* 首筆 */
            queue = queue + "11";
            String date = inner.npsbatch.getNpsbatchTxDate();
            date = CalendarUtil.adStringToROCString(date);
            queue = queue + StringUtils.leftPad(date, 7, "0");
            queue = queue + StringUtils.rightPad(inner.npsbatch.getNpsbatchBatchNo(), 13, " ");
            queue = queue + StringUtils.rightPad(inner.npsbatch.getNpsbatchBranch(), 4, " ");
            queue = queue + StringUtils.rightPad("", 204, " ");

            /* 明細筆 */
            for (int i = 0; i < alld.size(); i++) {
                queue += "12";
                String detaildate = inner.npsbatch.getNpsbatchTxDate();
                detaildate = CalendarUtil.adStringToROCString(detaildate);
                queue = queue + StringUtils.leftPad(detaildate, 7, "0");
                queue = queue + StringUtils.rightPad(inner.npsbatch.getNpsbatchBatchNo(), 13, " ");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlSeqNo().toString(), 10, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlAtmno(), 5, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlTerminalid(), 8, " ");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTxTime(), 6, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlBusinessUnit(), 8, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlPaytype(), 5, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlPayno(), 4, " ");
                Double txAmt = Double.valueOf(alld.get(i).getNpsdtlTxAmt().toString()) * 100;
                int txAmtt = txAmt.intValue();
                queue = queue + StringUtils.leftPad(String.valueOf(txAmtt), 11, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlIdno(), 11, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlReconSeq(), 16, " ");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTroutBkno7(), 7, "0");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTroutActno(), 16, "0");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTrinBkno(), 3, "0");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTrinActno(), 16, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlAllowT1(), 1, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlDueDate(), 8, " ");
                queue = queue + StringUtils.rightPad("", 23, " ");
                queue = queue + StringUtils.rightPad("006" + alld.get(i).getNpsdtlStan(), 10, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlPcode(), 4, " ");
                if (alld.get(i).getNpsdtlTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
                        && alld.get(i).getNpsdtlTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    queue = queue + "CB2W";
                } else {
                    queue = queue + "AB3W";
                }
                String datetbsdy = CalendarUtil.adStringToROCString(alld.get(i).getNpsdtlTbsdy()); //西元轉民國
                queue = queue + StringUtils.leftPad(datetbsdy, 7, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlHostBrch(), 4, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlCbsRc(), 3, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlReplyCode(), 4, " ");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlHostCharge().toString(), 2, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlHostChargeFlag(), 1, " ");
                queue = queue + StringUtils.rightPad("", 11, " ");
            }

            /* 尾筆 */
            queue += "19";
            String endDate = inner.npsbatch.getNpsbatchTxDate();
            endDate = CalendarUtil.adStringToROCString(endDate);
            queue = queue + StringUtils.leftPad(endDate, 7, "0");
            queue = queue + StringUtils.rightPad(inner.npsbatch.getNpsbatchBatchNo(), 13, " ");
            queue = queue + StringUtils.leftPad(inner.npsbatch.getNpsbatchTotCnt().toString(), 6, "0");
            Double tot = Double.valueOf(inner.npsbatch.getNpsbatchTotAmt().toString()) * 100;
            int tott = tot.intValue();
            queue = queue + StringUtils.leftPad(String.valueOf(tott), 14, "0");
            queue = queue + StringUtils.leftPad(inner.npsbatch.getNpsbatchOkCnt().toString(), 6, "0");
            Double ok = Double.valueOf(inner.npsbatch.getNpsbatchOkAmt().toString()) * 100;
            int okt = ok.intValue();
            queue = queue + StringUtils.leftPad(String.valueOf(okt), 14, "0");
            queue = queue + StringUtils.leftPad(inner.npsbatch.getNpsbatchFailCnt().toString(), 6, "0");
            Double fail = Double.valueOf(inner.npsbatch.getNpsbatchFailAmt().toString()) * 100;
            int failt = fail.intValue();
            queue = queue + StringUtils.leftPad(String.valueOf(failt), 14, "0");
            queue = queue + StringUtils.repeat(" ", 148);

            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getPyBatchAck().getDestination(), queue, null, null);

            logData.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
            logData.setProgramFlowType(ProgramFlow.RESTFulIn);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(queue);
            logData.setRemark("NPAYService 送出Queue內容");
            this.logMessage(logData);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean SubProcess(InnerClass inner, LogData logData) {
        int count = 0;
        String batNo = inner.fileid + inner.txdate + inner.batchNo;
        Npsdtl qNPSDTL = new Npsdtl();
        CountDownLatch latch = null;
//        ExecutorService executor = null;
        List<Threads> threadProcessorList = null;
        try {
            latch = new CountDownLatch(inner.maxThreads);
//            executor= Executors.newFixedThreadPool(inner.maxThreads);
            threadProcessorList = new ArrayList<>(inner.maxThreads);
            for (int i = 0; i < inner.maxThreads; i++) {
                inner.threadNo = i;
                threadProcessorList.add(new Threads(batNo, inner.threadNo, latch, inner));
//                executor.execute(new Threads(batNo, threadNo, latch));
            }
            latch.await();
            inner.npsbatch.setNpsbatchRspTime(new Date());
//            inner.npsbatch.setinner.npsbatchRspTime(FormatUtil.parseDataTime(Calendar.getInstance().toString(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
            inner.npsbatch.setNpsbatchResult("00");
            writLog(logData, inner.toString(), "SubProcess done.", ".SubProcess");
            if (npsbatchExtMapper.updateByPrimaryKeySelective(inner.npsbatch) < 1) {
                logContext.setRemark("更新inner.npsbatch失敗");
                sendEMS(logContext);
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private class Threads extends Thread {
        private String batNo;
        private Integer threadNo;
        private CountDownLatch latch;

        private InnerClass inner;
        private Object lock = new Object();

        public Threads(String batNo, Integer threadNo, CountDownLatch latch, InnerClass inner) {
            this.batNo = batNo;
            this.threadNo = threadNo;
            this.latch = latch;
            this.inner = inner;
            start();
//            run();
        }

        @Override
        public void run() {
            Npsdtl defNPSDTL = new Npsdtl();
            defNPSDTL.setNpsdtlBatNo(inner.batchNo);
            defNPSDTL.setNpsdtlThreadNo(threadNo);
            List<Npsdtl> defNpsdtl = new ArrayList<>();
            defNpsdtl = npsdtlExtMapper.GetNPSDTLByBATNO(batNo, threadNo);
            if (defNpsdtl.size() > 0) {
                for (int j = 0; j < defNpsdtl.size(); j++) {
                    MultiProcess(defNpsdtl.get(j), inner);
                }
            }
            this.latch.countDown();
        }
    }

    private boolean MultiProcess(Npsdtl npsdtl, InnerClass inner) {
        String batNo;
        String seqNo;
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String nb_rq = "";
        try {
            if (!npsdtl.getNpsdtlBatNo().substring(8, 16).equals(SysStatus.getPropertyValue().getSysstatTbsdyFisc())
                    && npsdtl.getNpsdtlAllowT1().equals("N")) {
                npsdtl.setNpsdtlResult("01");
                npsdtl.setNpsdtlErrMsg("財金已換日，不允許逾時交易");
                npsdtlExtMapper.updateByPrimaryKeySelective(npsdtl);
                inner.Failcnt += 1;
                inner.doTOTcnt += 1;
                inner.failamt.add(npsdtl.getNpsdtlTxAmt());
                inner.doTOTamt.add(npsdtl.getNpsdtlTxAmt());
                inner.npsbatch.setNpsbatchFailCnt(inner.Failcnt);
                inner.npsbatch.setNpsbatchDoTotCnt(inner.doTOTcnt);
                inner.npsbatch.setNpsbatchFailAmt(inner.failamt);
                inner.npsbatch.setNpsbatchDoTotAmt(inner.doTOTamt);
                npsbatchExtMapper.updateByPrimaryKeySelective(inner.npsbatch);
            }
            batNo = npsdtl.getNpsdtlBatNo();
            seqNo = String.valueOf(npsdtl.getNpsdtlSeqNo());
            if (rtnCode == CommonReturnCode.Normal) {
                nb_rq = prepareATMReq(npsdtl, inner);
                if (nb_rq == null) {
                    rtnCode = CommonReturnCode.ParseTelegramError;
                }
            }
            if (rtnCode == CommonReturnCode.Normal) {
                String nbDataRes = "";
                NBFEPHandler mNbHandler = new NBFEPHandler();
                try {
//                    if (npsdtl.getNpsdtlTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
//                            && npsdtl.getNpsdtlTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//                        aaName = "NBPYSelfIssue";
//                    } else if (npsdtl.getNpsdtlTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//                        aaName = "NBPYSelfRequestA";
//                    } else {
//                        aaName = "NBPYOtherRequestA";
//                    }
                    nbDataRes = mNbHandler.dispatch(nb_rq);
                    if (nbDataRes == null) {
                        rtnCode = CommonReturnCode.ProgramException;
                    }
                    inner.brch = mNbHandler.getBrch();
                    inner.charge = mNbHandler.getCharge();
                    inner.chargeFlag = mNbHandler.getChargeFlag();
                    UpdateNPSDTL(npsdtl, nbDataRes, rtnCode, inner);

                } catch (Exception e) {
                    return false;
                }
            }

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private String prepareATMReq(Npsdtl npsdtl, InnerClass inner) {
        String batNo;
        String seqNo;
        String rtnmessage = "";
        try {
            batNo = npsdtl.getNpsdtlBatNo();
            seqNo = String.valueOf(npsdtl.getNpsdtlSeqNo());
            RCV_NB_GeneralTrans_RQ nnb = new RCV_NB_GeneralTrans_RQ();
            RCV_NB_GeneralTrans_RQ nb = newXml(nnb);
//            nbdata.setTxNbfepObject(new NBFEPGeneral());

//            NBFEPGeneral nbg = new NBFEPGeneral();

            nb.getBody().getRq().getHeader().setCLIENTTRACEID(FEPChannel.NAM + batNo + seqNo);
            nb.getBody().getRq().getHeader().setCHANNEL(FEPChannel.NAM.toString());
            nb.getBody().getRq().getHeader().setMSGID(npsdtl.getNpsdtlTxCode().trim() + npsdtl.getNpsdtlPcode());
            nb.getBody().getRq().getHeader().setMSGKIND("G");
            nb.getBody().getRq().getHeader().setTXNID("");
            nb.getBody().getRq().getHeader().setBRANCHID("");
            nb.getBody().getRq().getHeader().setTERMID("");
            nb.getBody().getRq().getHeader().setCLIENTDT(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
            nb.getBody().getRq().getSvcRq().setINDATE(inner.npsbatch.getNpsbatchTxDate());
            nb.getBody().getRq().getSvcRq().setINTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            nb.getBody().getRq().getSvcRq().setIPADDR(" ");
            nb.getBody().getRq().getSvcRq().setTXNTYPE("RQ");
            nb.getBody().getRq().getSvcRq().setTERMINALID(npsdtl.getNpsdtlTerminalid());
            nb.getBody().getRq().getSvcRq().setTERMINAL_TYPE(" ");
            nb.getBody().getRq().getSvcRq().setTERMINAL_CHECKNO(" ");
            nb.getBody().getRq().getSvcRq().setFSCODE(npsdtl.getNpsdtlTxCode().trim());
            nb.getBody().getRq().getSvcRq().setPCODE(npsdtl.getNpsdtlPcode());
            nb.getBody().getRq().getSvcRq().setBUSINESSTYPE("T");
            nb.getBody().getRq().getSvcRq().setTRANSAMT(npsdtl.getNpsdtlTxAmt().multiply(new BigDecimal("100")));
            nb.getBody().getRq().getSvcRq().setTRANBRANCH("");
            nb.getBody().getRq().getSvcRq().setTRNSFROUTIDNO(npsdtl.getNpsdtlIdno());
            nb.getBody().getRq().getSvcRq().setTRNSFROUTBANK(npsdtl.getNpsdtlTroutBkno7());
            nb.getBody().getRq().getSvcRq().setTRNSFROUTACCNT(npsdtl.getNpsdtlTroutActno());
            nb.getBody().getRq().getSvcRq().setTRNSFRINBANK(npsdtl.getNpsdtlTrinBkno() + "0000");
            nb.getBody().getRq().getSvcRq().setTRNSFRINACCNT(npsdtl.getNpsdtlTrinActno());
            nb.getBody().getRq().getSvcRq().setFEEPAYMENTTYPE("");
            nb.getBody().getRq().getSvcRq().setCUSTPAYFEE(null);
            nb.getBody().getRq().getSvcRq().setFISCFEE(null);
            nb.getBody().getRq().getSvcRq().setFAXFEE(null);
            nb.getBody().getRq().getSvcRq().setTRANSFEE(null);
            nb.getBody().getRq().getSvcRq().setOTHERBANKFEE(null);
            nb.getBody().getRq().getSvcRq().setTRNSFRINNOTE("");
            nb.getBody().getRq().getSvcRq().setTRNSFROUTNOTE("");
            nb.getBody().getRq().getSvcRq().setORITXSTAN("");
            nb.getBody().getRq().getSvcRq().setSSLTYPE("N");
            nb.getBody().getRq().getSvcRq().setLIMITTYPE("");
            nb.getBody().getRq().getSvcRq().setTRANSTYPEFLAG("");
            nb.getBody().getRq().getSvcRq().getPAYDATA().setNPOPID(npsdtl.getNpsdtlBusinessUnit());
            nb.getBody().getRq().getSvcRq().getPAYDATA().setNPPAYTYPE(npsdtl.getNpsdtlPaytype());
            nb.getBody().getRq().getSvcRq().getPAYDATA().setNPFEENO(npsdtl.getNpsdtlPayno());
            nb.getBody().getRq().getSvcRq().getPAYDATA().setNPID(npsdtl.getNpsdtlIdno());
            nb.getBody().getRq().getSvcRq().getPAYDATA().setNPPAYNO(npsdtl.getNpsdtlReconSeq());
            nb.getBody().getRq().getSvcRq().getPAYDATA().setNPPAYENDDATE(npsdtl.getNpsdtlDueDate());
            nb.getBody().getRq().getSvcRq().getPAYDATA().setNPBRANCH(inner.npsbatch.getNpsbatchBranch());
            nb.getBody().getRq().getSvcRq().getPAYDATA().setIDENTIFIER("");
            nb.getBody().getRq().getSvcRq().setTEXTMARK("");
//            nbdata.getTxNbfepObject().setRequest(nb);

            rtnmessage = XmlUtil.toXML(nb);
            return rtnmessage;
        } catch (Exception ex) {
        }
        return null;
    }

    private RCV_NB_GeneralTrans_RQ newXml(RCV_NB_GeneralTrans_RQ nb) {
        RCV_NB_GeneralTrans_RQ nba = new RCV_NB_GeneralTrans_RQ();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Header rsheader = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Header();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body rsbody = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq msg = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header header = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq body = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_PAYDATA pay = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_PAYDATA();
        msg.setHeader(header);
        body.setPAYDATA(pay);
        msg.setSvcRq(body);
        rsbody.setRq(msg);
        nb.setBody(rsbody);
        nb.setHeader(rsheader);
        return nb;
    }

    private Boolean UpdateNPSDTL(Npsdtl npsdtl, String Res, FEPReturnCode rtnCode, InnerClass inner) {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            SEND_NB_GeneralTrans_RS nbDataRes = deserializeFromXml(Res, SEND_NB_GeneralTrans_RS.class);

            if (nbDataRes.getBody().getRs().getHeader().getSEVERITY().equals("INFO")
                    && nbDataRes.getBody().getRs().getSvcRs().getHOSTACC_FLAG().equals("Y")) {
                npsdtl.setNpsdtlResult("00"); //成功
            } else {
                npsdtl.setNpsdtlResult("01"); //失敗
            }
            npsdtl.setNpsdtlTbsdy(nbDataRes.getBody().getRs().getSvcRs().getACCTDATE());
            npsdtl.setNpsdtlReplyCode(nbDataRes.getBody().getRs().getHeader().getSTATUSCODE());
            if (StringUtils.isBlank(npsdtl.getNpsdtlErrMsg())) {
                npsdtl.setNpsdtlErrMsg(nbDataRes.getBody().getRs().getHeader().getSTATUSDESC());
            }
            npsdtl.setNpsdtlEjfno(nbDataRes.getBody().getRs().getSvcRs().getFEP_EJNO());
            npsdtl.setNpsdtlTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
            npsdtl.setNpsdtlStan(nbDataRes.getBody().getRs().getSvcRs().getTXNSTAN());
            if (nbDataRes.getBody().getRs().getSvcRs().getCUSTPAYFEE() != null) {
                npsdtl.setNpsdtlFee(nbDataRes.getBody().getRs().getSvcRs().getCUSTPAYFEE());
            }

            if (nbDataRes.getBody().getRs().getSvcRs().getTXNTYPE().equals("RQ") && inner.charge != null) {
                npsdtl.setNpsdtlHostCharge(inner.charge.intValue());
            } else {
                npsdtl.setNpsdtlHostCharge(0);
            }
            if (nbDataRes.getBody().getRs().getSvcRs().getTXNTYPE().equals("RQ") && StringUtils.isNotBlank(inner.brch)) {
                npsdtl.setNpsdtlHostBrch(inner.brch);
            } else {
                npsdtl.setNpsdtlHostBrch(inner.npsbatch.getNpsbatchBranch());
            }

            npsdtl.setNpsdtlCbsRc(nbDataRes.getBody().getRs().getSvcRs().getTCBRTNCODE());
            if (StringUtils.isNotBlank(inner.chargeFlag)) {
                npsdtl.setNpsdtlHostChargeFlag(inner.chargeFlag);
            }
//            npsdtl.setNpsdtlThreadNo(threadNo);
            if (npsdtlExtMapper.updateByPrimaryKeySelective(npsdtl) < 1) {
                transactionManager.rollback(txStatus);
                logContext.setRemark("更新NPSDTL失敗");
                sendEMS(logContext);
                return false;
            } else {
                if (npsdtl.getNpsdtlResult().equals("00")) {
                    inner.okcnt += 1;
                    inner.feecnt += 1;
                    inner.doTOTcnt += 1;
                    inner.okamt = inner.okamt.add(npsdtl.getNpsdtlTxAmt());
                    inner.doTOTamt = inner.doTOTamt.add(npsdtl.getNpsdtlTxAmt());
                    inner.feeamt = inner.feeamt.add(npsdtl.getNpsdtlFee());
                    inner.npsbatch.setNpsbatchOkCnt(inner.okcnt);
                    inner.npsbatch.setNpsbatchFeeCnt(inner.feecnt);
                    inner.npsbatch.setNpsbatchDoTotCnt(inner.doTOTcnt);
                    inner.npsbatch.setNpsbatchOkAmt(inner.okamt);
                    inner.npsbatch.setNpsbatchDoTotAmt(inner.doTOTamt);
                    inner.npsbatch.setNpsbatchFeeAmt(inner.feeamt);
                } else {
                    inner.Failcnt += 1;
                    inner.doTOTcnt += 1;
                    inner.failamt = inner.failamt.add(npsdtl.getNpsdtlTxAmt());
                    inner.doTOTamt = inner.doTOTamt.add(npsdtl.getNpsdtlTxAmt());
                    inner.npsbatch.setNpsbatchFailCnt(inner.Failcnt);
                    inner.npsbatch.setNpsbatchDoTotCnt(inner.doTOTcnt);
                    inner.npsbatch.setNpsbatchFailAmt(inner.failamt);
                    inner.npsbatch.setNpsbatchDoTotAmt(inner.doTOTamt);
                }
                if (npsbatchExtMapper.updateByPrimaryKeySelective(inner.npsbatch) < 1) {
                    transactionManager.rollback(txStatus);
                    logContext.setRemark("更新inner.npsbatch失敗");
                    sendEMS(logContext);
                    return false;
                }
            }
            transactionManager.commit(txStatus);
            return true;
        } catch (Exception ex) {
        }
        return true;
    }

    private Boolean check(InnerClass inner, LogData logData) {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        int a2;
        try {
            for (int i = 0; i < Integer.valueOf(inner.W_REC_COUNT); i++) {
                a2 = 30 + i * Integer.valueOf(inner.W_REC_LEN) + 9;
                if (!inner.message.substring(a2 - 1, a2 + 12).equals(inner.message.substring(8, 21))) {
                    inner.W_ERR_MSG = "批號錯誤";
                    inner.W_ERR_CODE = "K003";
                    transactionManager.rollback(txStatus);
                    ACKResponse(inner, logData);
                    return false;
                } else {
                    int w = 30 + i * Integer.valueOf(inner.W_REC_LEN) + 1;
                    String bsdaysDate = CalendarUtil.rocStringToADString(inner.message.substring(w, w + 7));
                    inner.bsdays = bsdaysExtMapper.selectByPrimaryKey("TWN", bsdaysDate);
                    if (inner.bsdays == null || inner.bsdays.getBsdaysWorkday().equals(0)) {
                        inner.W_ERR_CODE = "K002";
                        inner.W_ERR_MSG = "日期錯誤";
                        transactionManager.rollback(txStatus);
                        ACKResponse(inner, logData);
                        return false;
                    } else {
                        int first2 = 30 + i * Integer.valueOf(inner.W_REC_LEN);
                        String checkfirst2 = inner.message.substring(first2 - 1, first2 + 1);
                        writLog(logData, "checkfirst2:" + checkfirst2,null, null);
                        switch (checkfirst2) {
                            case "01": /* 首筆 */
                                if (i > 0) {
                                    inner.W_ERR_MSG = "REC-TYPE ERROR";
                                    inner.W_ERR_CODE = "K001";
                                    transactionManager.rollback(txStatus);
                                    ACKResponse(inner, logData);
                                    return false;
                                }
                                if (StringUtils.isBlank(inner.message.substring(51, 55))) {
                                    inner.W_ERR_MSG = "主辦行錯誤";
                                    inner.W_ERR_CODE = "K004";
                                    transactionManager.rollback(txStatus);
                                    ACKResponse(inner, logData);
                                    return false;
                                }
                                inner.npsbatch = npsbatchExtMapper.selectByPrimaryKey(inner.message.substring(0, 8).trim(),
                                        CalendarUtil.rocStringToADString(inner.message.substring(31, 38)), inner.message.substring(38, 51));
                                if (inner.npsbatch != null) {
                                    if (StringUtils.isBlank(inner.npsbatch.getNpsbatchResult())) {
                                        inner.W_ERR_MSG = "該批資料已收妥,尚未執行完成";
                                        inner.W_ERR_CODE = "K008";
                                        transactionManager.rollback(txStatus);
                                        ACKResponse(inner, logData);
                                        return false;
                                    } else {
                                        if (inner.npsbatch.getNpsbatchResult().equals("01")) {
                                            inner.W_ERR_MSG = "該批資料檢核失敗，且已回覆";
                                            inner.W_ERR_CODE = "K020";
                                            transactionManager.rollback(txStatus);
                                            ACKResponse(inner, logData);
                                            return false;
                                        } else {
                                            inner.W_ERR_MSG = "該批已處理完成且下傳";
                                            inner.W_ERR_CODE = "K006";
                                            transactionManager.rollback(txStatus);
                                            ACKResponse(inner, logData);
                                            return false;
                                        }
                                    }
                                } else {
                                    insertNpsbatch(transactionManager, txStatus, inner, logData);
                                    txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
                                }
                                break;
                            case "02": /* 明細資料 */
                                inner.txdate = CalendarUtil.rocStringToADString(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 1, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 8));
                                inner.batchNo = inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 8, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 21);
                                inner.npsbatch = npsbatchExtMapper.selectByPrimaryKey(inner.message.substring(0, 8).trim(), inner.txdate, inner.batchNo);
                                if (inner.npsbatch == null) {
                                    if (StringUtils.isBlank(inner.npsbatch.getNpsbatchResult())) ;
                                    {
                                        inner.W_ERR_MSG = "明細批號錯誤";
                                        inner.W_ERR_CODE = "K003";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                } else {

                                    writLog(logData, "02 else", "", ".check");
                                    if (i == 0) {
                                        List<Npsdtl> Npsdtl = npsdtlExtMapper.GetNPSDTLByBATNOforAll(inner.fileid + inner.txdate + inner.batchNo);
                                        if (Npsdtl != null) {
                                            for (i = 0; i < Npsdtl.size(); i++) {
                                                inner.W_TOT_TX_AMT = inner.W_TOT_TX_AMT.add(Npsdtl.get(i).getNpsdtlTxAmt());
                                                inner.W_SEQ_NO += 1;
                                            }
                                        }
                                    }
                                    /* 檢核明細資料 */
                                    if (!"T".equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 36, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 37))
                                            || !"NAM".equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 41, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 44))
                                            || !inner.npsbatch.getNpsbatchBranch().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 37, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 41))) {
                                        inner.W_ERR_MSG = "端末資料錯誤";
                                        inner.W_ERR_CODE = "K010";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    if (!inner.npsbatch.getNpsbatchBranch().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 37, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 41))) {
                                        inner.W_ERR_MSG = "LTERM 分行代號錯誤";
                                        inner.W_ERR_CODE = "K011";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    if (StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 50, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 58))
                                            || StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 58, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 63))
                                            || StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 63, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 67))) {
                                        inner.W_ERR_MSG = "繳費代號資料錯誤";
                                        inner.W_ERR_CODE = "K012";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    if (!PolyfillUtil.isNumeric(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 67, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 78))
                                            || StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 67, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 78))) {
                                        inner.W_ERR_MSG = "繳費金額錯誤";
                                        inner.W_ERR_CODE = "K013";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    if (StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 78, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 89))) {
                                        inner.W_ERR_MSG = "ID-NO錯誤";
                                        inner.W_ERR_CODE = "K019";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    if (StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 112))
                                            || StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 112, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 128))) {
                                        inner.W_ERR_MSG = "轉出帳號錯誤";
                                        inner.W_ERR_CODE = "K014";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    if (StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 135))
                                            || StringUtils.isBlank(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 131, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 147))) {
                                        inner.W_ERR_MSG = "轉入行庫錯誤";
                                        inner.W_ERR_CODE = "K015";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    if (!String.valueOf(inner.W_SEQ_NO + 1).equals(String.valueOf(Integer.valueOf(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 21, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 31))))) {
                                        /*明細資料交易上傳序號不連續*/
                                        inner.W_ERR_MSG = "明細檔未依序";
                                        inner.W_ERR_CODE = "K009";
                                        transactionManager.rollback(txStatus);
                                        writLog(logData, inner.W_ERR_MSG, "case 02 error", ".check");
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    inner.W_SEQ_NO += 1;
                                    inner.W_TOT_TX_AMT = inner.W_TOT_TX_AMT.add(new BigDecimal(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 67, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 78)));
                                    writLog(logData, null, "ready to insertnpsdtl", ".check");
                                    InsertNPSDTL(i, transactionManager, txStatus, inner, logData);
                                }
                                break;
                            case "09":
                                inner.txdate = CalendarUtil.rocStringToADString(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 1, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 8));
                                inner.batchNo = inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 8, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 21);
                                inner.npsbatch = npsbatchExtMapper.selectByPrimaryKey(inner.message.substring(0, 8).trim(), inner.txdate, inner.batchNo);
                                if (inner.npsbatch == null) {
                                    inner.W_ERR_MSG = "尾筆批號錯誤";
                                    inner.W_ERR_CODE = "K003";
                                    transactionManager.rollback(txStatus);
                                    ACKResponse(inner, logData);
                                    return false;
                                } else {
                                    if (i == 0) {
                                        List<Npsdtl> Npsdtl = npsdtlExtMapper.GetNPSDTLByBATNOforAll(inner.fileid + inner.txdate + inner.batchNo);
                                        if (Npsdtl != null) {
                                            for (i = 0; i < Npsdtl.size(); i++) {
                                                inner.W_TOT_TX_AMT = inner.W_TOT_TX_AMT.add(Npsdtl.get(i).getNpsdtlTxAmt());
                                                inner.W_SEQ_NO += 1;
                                            }
                                        }
                                    }
                                    if (!inner.W_SEQ_NO.toString().equals(String.valueOf(Integer.valueOf(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 21, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 27))))) {
                                        inner.W_ERR_MSG = "總比數錯誤";
                                        inner.W_ERR_CODE = "K016";
                                        transactionManager.rollback(txStatus);
                                        ACKResponse(inner, logData);
                                        return false;
                                    }
                                    if (!inner.W_TOT_TX_AMT.toString().equals(String.format("%.0f", Double.valueOf(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 27, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 41))))) {
                                        inner.W_ERR_MSG = "總金額錯誤";
                                        inner.W_ERR_CODE = "K017";
                                        transactionManager.rollback(txStatus);
                                        ACKResponse(inner, logData);
                                        return false;
                                    } else {
                                        updateNpsbatch(i, transactionManager, txStatus, inner, logData);
                                    }
                                }
                                break;
                            default:
                                inner.W_ERR_MSG = "資料別錯誤";
                                inner.W_ERR_CODE = "K005";
                                transactionManager.rollback(txStatus);
                                ACKResponse(inner, logData);
                                return false;
                        }
                        if (StringUtils.isBlank(inner.W_ERR_CODE) && "09".equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) - 1, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 1))) {
                            inner.W_ERR_CODE = "0000";
                            ACKResponse(inner, logData);
                            //GO TO 2
                        }
                    }
                }
            }
            transactionManager.commit(txStatus);
            return true;
        } catch (
                Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void updateNpsbatch(Integer i, PlatformTransactionManager transactionManager, TransactionStatus txStatus, InnerClass inner, LogData logData) {
        try {
            inner.npsbatch.setNpsbatchTotAmt(new BigDecimal(Double.valueOf(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 27, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 41)) / 100));
            inner.npsbatch.setNpsbatchTotCnt(Integer.valueOf(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 21, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 27)));
            if (npsbatchExtMapper.updateByPrimaryKeySelective(inner.npsbatch) < 1) {
                inner.W_ERR_MSG = "UPDATE inner.npsbatch 檔案處理有誤";
                inner.W_ERR_CODE = "FXXX";
                transactionManager.rollback(txStatus);
                writLog(logData, inner.toString(), "updateNpsbatch done.", ".updateNpsbatch");
                ACKResponse(inner, logData);
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void InsertNPSDTL(Integer i, PlatformTransactionManager transactionManager, TransactionStatus txStatus, InnerClass inner, LogData logData) {
        Npsdtl defNpsdtl = new Npsdtl();
        try {
            defNpsdtl.setNpsdtlBatNo(inner.fileid + inner.txdate + inner.batchNo);
            defNpsdtl.setNpsdtlSeqNo(Integer.valueOf(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 21, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 31)));
            defNpsdtl.setNpsdtlTroutBkno(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 108));
            defNpsdtl.setNpsdtlTroutActno(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 112, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 128));
            defNpsdtl.setNpsdtlTrinBkno(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 131));
            defNpsdtl.setNpsdtlTrinActno(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 131, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 147));
            defNpsdtl.setNpsdtlTxAmt(BigDecimal.valueOf(Double.valueOf(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 67, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 78)) / 100));
            defNpsdtl.setNpsdtlBusinessUnit(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 50, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 58));
            defNpsdtl.setNpsdtlPaytype(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 58, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 63));
            defNpsdtl.setNpsdtlPayno(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 63, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 67));
            defNpsdtl.setNpsdtlIdno(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 78, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 89));
            defNpsdtl.setNpsdtlReconSeq(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 89, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 105));
            String TroutBkno7 = inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 112);
            TroutBkno7 = StringUtils.rightPad(TroutBkno7.trim(), 7, "0");
            defNpsdtl.setNpsdtlTroutBkno7(TroutBkno7);
            defNpsdtl.setNpsdtlDueDate(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 148, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 156));
            defNpsdtl.setNpsdtlAllowT1(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 147, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 148));
            defNpsdtl.setNpsdtlTerminalid(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 36, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 44));
            defNpsdtl.setNpsdtlAtmno(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 31, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 36));
            defNpsdtl.setNpsdtlTxTime(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 44, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 50));
            defNpsdtl.setNpsdtlBkno(SysStatus.getPropertyValue().getSysstatHbkno());
            defNpsdtl.setNpsdtlThreadNo(i % inner.maxThreads);
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 108))
                    && !SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 131))) {
                defNpsdtl.setNpsdtlPcode("2261");
                defNpsdtl.setNpsdtlTxCode("ED");
            } else if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 108))
                    && SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 131))) {
                defNpsdtl.setNpsdtlPcode("2262");
                defNpsdtl.setNpsdtlTxCode("EW");
            } else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 108))
                    && SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 131))) {
                defNpsdtl.setNpsdtlPcode("2263");
                defNpsdtl.setNpsdtlTxCode("EA");
            } else if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 108))
                    && !SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 131))
                    && (inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 108)).equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 131))) {
                defNpsdtl.setNpsdtlPcode("2263");
                defNpsdtl.setNpsdtlTxCode("EA");
            } else if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 108))
                    && !SysStatus.getPropertyValue().getSysstatHbkno().equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 131))
                    && !(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 105, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 108)).equals(inner.message.substring(30 + i * Integer.valueOf(inner.W_REC_LEN) + 128, 30 + i * Integer.valueOf(inner.W_REC_LEN) + 131))) {
                defNpsdtl.setNpsdtlPcode("2264");
                defNpsdtl.setNpsdtlTxCode("ER");
            }
            if (npsdtlExtMapper.insertSelective(defNpsdtl) < 1) {
                inner.W_ERR_MSG = "INSERT NPSDTL檔案處理有誤";
                inner.W_ERR_CODE = "FXXX";
                transactionManager.rollback(txStatus);
                ACKResponse(inner, logData);
                return;
            }
            writLog(logData, "", "npsdtlExtMapper insert done.", ".InsertNPSDTL");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertNpsbatch(PlatformTransactionManager transactionManager, TransactionStatus txStatus, InnerClass inner, LogData logData) {
        try {
            inner.fileid = inner.message.substring(0, 8);
            inner.txdate = CalendarUtil.rocStringToADString(inner.message.substring(31, 38));
            inner.batchNo = inner.message.substring(8, 21);
            inner.npsbatch = new Npsbatch();
            inner.npsbatch.setNpsbatchFileId(inner.fileid.trim());
            inner.npsbatch.setNpsbatchTxDate(inner.txdate);
            inner.npsbatch.setNpsbatchBatchNo(inner.batchNo);
            inner.npsbatch.setNpsbatchBranch(inner.message.substring(51, 55));
            inner.npsbatch.setNpsbatchRcvTime(new Date());
            if (npsbatchExtMapper.insertSelective(inner.npsbatch) < 1) {
                inner.W_ERR_MSG = " INSERT inner.npsbatch檔案處理有誤";
                inner.W_ERR_CODE = "FXXX";
                transactionManager.rollback(txStatus);
                ACKResponse(inner, logData);
                return;
            }
            transactionManager.commit(txStatus);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void ACKResponse(InnerClass inner, LogData logData) throws Exception {
        inner.zone = zoneExtMapper.selectByPrimaryKey("TWN");
        String REPLY_FILE_ID = StringUtils.rightPad(inner.message.substring(0, 8), 8, " ");
        String REPLY_BATCH_NO = StringUtils.leftPad(inner.message.substring(8, 21), 13, "0");
        String REPLY_REC_LEN = "100";
        String REPLY_REC_CNT = StringUtils.leftPad("1", 5, "0");
        String FILLER1 = "99";
        String date = CalendarUtil.adStringToROCString(inner.zone.getZoneTbsdy());
        String REPLY_KEY_1 = StringUtils.rightPad(date, 7, " ");
        String REPLY_KEY_2 = StringUtils.rightPad(inner.message.substring(8, 21), 13, " ");
        String ERR_CODE = StringUtils.rightPad(inner.W_ERR_CODE, 4, " ");
        String msg;
        if (StringUtils.isBlank(inner.W_ERR_MSG)) {
            msg = "";
        } else {
            msg = inner.W_ERR_MSG;
        }
        String ERR_MSG = StringUtils.rightPad(msg, 60, " ");
        String FILLER2 = StringUtils.rightPad("", 14, " ");
        String ack = REPLY_FILE_ID + REPLY_BATCH_NO + REPLY_REC_LEN + REPLY_REC_CNT + FILLER1 + REPLY_KEY_1 + REPLY_KEY_2
                + ERR_CODE + ERR_MSG + FILLER2;
        JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
        sender.sendQueue(configuration.getQueueNames().getPyBatchAck().getDestination(), ack, null, null);
        if (StringUtils.isNotBlank(inner.W_ERR_CODE)) {
            readNpsbatch(inner, logData);
        }
        writLog(logData, inner.toString(), "ACKResponse done.", ".ACKResponse");
    }

    public void readNpsbatch(InnerClass inner, LogData logData) {
        try {
            inner.npsbatch.setNpsbatchFileId(inner.message.substring(0, 8));
            inner.txdate = CalendarUtil.rocStringToADString(inner.message.substring(31, 38));
            inner.npsbatch.setNpsbatchTxDate(inner.txdate);
            inner.npsbatch.setNpsbatchBatchNo(inner.message.substring(38, 51));
            Npsbatch npsbatch1 = npsbatchExtMapper.selectByPrimaryKey(inner.npsbatch.getNpsbatchFileId(), inner.npsbatch.getNpsbatchTxDate(), inner.npsbatch.getNpsbatchBatchNo());
            if (npsbatch1 == null) {
                npsbatch1.setNpsbatchFileId(inner.message.substring(0, 8));
                npsbatch1.setNpsbatchTxDate(inner.txdate);
                npsbatch1.setNpsbatchBatchNo(inner.message.substring(8, 21));
                if ("01".equals(inner.message.substring(29, 31))) {
                    npsbatch1.setNpsbatchBranch(inner.message.substring(51, 55));
                }
                npsbatch1.setNpsbatchResult("01");
                npsbatch1.setNpsbatchErrMsg(inner.W_ERR_MSG);
                npsbatch1.setNpsbatchRspTime(new Date());
                npsbatchExtMapper.insert(npsbatch1);
                writLog(logData, "", "npsbatchExtMapper insert done.", ".readNpsbatch");
            } else {
                npsbatch1.setNpsbatchRspTime(new Date());
                if (!"0000".equals(inner.W_ERR_CODE) && !"K006".equals(inner.W_ERR_CODE)
                        && !"K008".equals(inner.W_ERR_CODE) && !"K020".equals(inner.W_ERR_CODE)) {
                    npsbatch1.setNpsbatchResult("01");
                    npsbatch1.setNpsbatchErrMsg(inner.W_ERR_MSG);
                }
                npsbatchExtMapper.updateByPrimaryKeySelective(npsbatch1);;
                writLog(logData, "", "npsbatchExtMapper update done.", ".readNpsbatch");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writLog(LogData logData, String msg, String mark, String progName){
        logData.setMessage(msg);
        logData.setEj(TxHelper.generateEj());
        logData.setProgramName(StringUtils.join(ProgramName, progName));
        logData.setProgramFlowType(ProgramFlow.RESTFulIn);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setRemark(mark);
        this.logMessage(logData);
    }
}
