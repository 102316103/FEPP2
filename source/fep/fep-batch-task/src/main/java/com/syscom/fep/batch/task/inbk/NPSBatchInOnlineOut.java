package com.syscom.fep.batch.task.inbk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import com.syscom.fep.server.common.batch.SckBatch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.net.ftp.FtpAdapterFactory;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.NpsbatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.server.common.handler.NBFEPHandler;
import com.syscom.fep.vo.enums.ATMZone;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_PAYDATA;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;

/**
 * @author Joseph
 */
public class NPSBatchInOnlineOut extends FEPBase implements Task {
    private LinkedBlockingQueue<Npsdtl> queueNpsdtl = new LinkedBlockingQueue<Npsdtl>();
    private Object tota = null;
    private Integer detailRow = 0;
    private Double detailtxAmt = 0.0;

    @Autowired
    private FtpAdapterFactory factory = SpringBeanFactoryUtil.getBean(FtpAdapterFactory.class);
    private Npsbatch defNPSBATCH = new Npsbatch();
    private String _programName = NPSBatchInOnlineOut.class.getSimpleName(); //程式名稱
    private String batchLogPath = StringUtils.EMPTY;
    private String batchInputFile = StringUtils.EMPTY;
    private String log = StringUtils.EMPTY;
    private BatchJobLibrary job = null;
    private LogData _logData;
    private String aaName;
    private String tbsdy;
    private String batchNo; //批號
    private String bunit; //委託單位
    private String payType; //繳費類別
    private String ftp;
    private String dir; //上傳下載目錄
    private String inputFileName;
    private String downloadFilePath;
    private String fileid;
    private String outputFileName;
    private String uploadFilePath;
    private Integer maxThreads;
    private String tbsdy2;
    private String ftpucode;
    private Integer threadNo;
    private String ftpscode;
    private Boolean strZoneDetail;
    private String[] arrayHeader = new String[5];
    private String[] arrayDetail = new String[20];
    private String[] arrayFoot = new String[6];
    private int countAll = 0;
    private int countOK = 0;
    private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);
    private NpsbatchExtMapper npsbatchExtMapper = SpringBeanFactoryUtil.getBean(NpsbatchExtMapper.class);
    private BigDecimal charge;
    private String brch;
    private String chargeFlag;

    @Override
    public BatchReturnCode execute(String[] args) {
        // 自動生成的方法存根
        try {

            //檢查Batch Log目錄參數
            batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
//			batchLogPath = "D:/TXTT/log/";

            if (StringUtils.isBlank(batchLogPath)) {
                LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
                return BatchReturnCode.Succeed;
            }

            // 1. 初始化相關批次物件及拆解傳入參數
            initialBatch(args);

            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();

            if (!CheckConfig()) {
                job.stopBatch();
                return BatchReturnCode.Succeed;
            }

            if (job.getArguments().containsKey("FORCERUN") && "Y".equals(job.getArguments().get("FORCERUN"))) {
                job.writeLog("參數FORCERUN=Y, 強制執行此批次!");
            } else {
                if (!(job.isBsDay(ATMZone.TWN.toString()))) {

                    job.writeLog("非營業日不執行批次，請停止批次作業!");
                    job.stopBatch();
                    return BatchReturnCode.Succeed;
                }
            }

            // 主流程
            if (doBusiness()) {
                job.writeLog(_programName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(_programName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
            }
            return BatchReturnCode.Succeed;
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            if (job != null) {
                //Send to System Event
                job.writeLog("Batch Log目錄未設定，請修正");
                System.out.println("Batch Log目錄未設定，請修正");
            } else {
                job.writeErrorLog(e, e.getMessage());
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
                // 通知批w作業管理系統工作失敗,暫停後面流程
                try {
                    job.abortTask();
                    logContext.setProgramException(e);
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
                } catch (Exception ex) {
                    logContext.setProgramException(ex);
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
                }
            }
            return BatchReturnCode.ProgramException;
        } finally {
            if (job != null) {
                job.writeLog(ProgramName + "結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.dispose();
                job = null;
            }
            if (logContext != null) {
                logContext = null;
            }
        }
    }

    private boolean CheckConfig() {
        try {
            _logData = new LogData();
            _logData.setChannel(FEPChannel.BATCH);
            _logData.setEj(0);
            _logData.setProgramName(_programName);
            _logData.setSubSys(SubSystem.INBK);
            _logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));

            //扣款日期，預設為系統日
            if (job.getArguments().containsKey("TBSDY")) {
                tbsdy = job.getArguments().get("TBSDY");
            } else {
                tbsdy = new SimpleDateFormat("yyyyMMdd").format(new Date());
            }

            //批號
            if (job.getArguments().containsKey("BATCHNO")) {
                batchNo = job.getArguments().get("BATCHNO");
            } else {
                job.writeLog("請設定參數-批號(BATCHNO)");
                return false;
            }

            //檔名
            if (job.getArguments().containsKey("FILEID")) {
                fileid = job.getArguments().get("FILEID");
            } else {
                job.writeLog("請設定參數-檔名(FILEID)");
                return false;
            }

            ftp = CMNConfig.getInstance().getSFTPServer();
            ftpucode = CMNConfig.getInstance().getSFTPUCode();
            ftpscode = CMNConfig.getInstance().getSFTPSCode();

            ftp = "10.3.101.3";
            ftpucode = "sftpuser";
            ftpscode = "!QAZ2wsx";
            //上下傳目錄
//            if (job.getArguments().containsKey("DIR")) {    //未指定時預設為系統時間前5分鐘，必須有值且為6碼(暫無使用)
//                dir = job.getArguments().get("DIR");
//            } else {
//                job.writeLog("請設定參數-FTP目錄(DIR)");
//                return false;
//            }

//            job.writeLog("傳入參數: TBSDY={" + tbsdy + "}, BUNIT={" + bunit + "}, PAYTYPE={" + payType + "}, BATCHNO={" + batchNo + "}, DIR={" + dir + "}");
            job.writeLog("傳入參數: TBSDY={" + tbsdy + "}, BUNIT={" + bunit + "}, PAYTYPE={" + payType + "}, BATCHNO={" + batchNo + "}");


            //檔案路徑和檔名
            inputFileName = fileid + tbsdy + batchNo + ".TXT"; //下載檔名
            downloadFilePath = CMNConfig.getInstance().getBatchInputPath() + inputFileName; //下載檔路徑+檔名
            outputFileName = fileid + tbsdy + batchNo + "RSP.TXT"; //回饋檔名
            uploadFilePath = CMNConfig.getInstance().getBatchOutputPath() + outputFileName; //回饋檔路徑+檔名

            //最大執行Thread數
            maxThreads = CMNConfig.getInstance().getSECMaxThreads();
        } catch (Exception e) {
            e.printStackTrace();
            job.writeLog(ProgramName + "-CheckConfig Exception=" + e.getMessage());
            job.writeLog(e.getMessage());
            _logData.setProgramException(e);
            sendEMS(_logData);
            return false;
        } finally {
            //dbSYSSTAT = null;
            if (job != null) {
                job.writeLog(ProgramName + "結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.dispose();
            }
        }
        return true;
    }

    private boolean doBusiness() {
        Boolean result = false;
        try {
            result = FEPDownload();
            if (!result) {
                return result;
            }

            //2.讀NPSBATCH
//            tbsdy2 = CalendarUtil.rocStringToADString14(tbsdy);
            tbsdy2 = tbsdy;
            defNPSBATCH.setNpsbatchTxDate(tbsdy2);
            defNPSBATCH.setNpsbatchBatchNo(batchNo);
            defNPSBATCH.setNpsbatchFileId(fileid);
            defNPSBATCH = npsbatchExtMapper.selectByPrimaryKey(fileid, tbsdy2, batchNo);
            if (defNPSBATCH == null) {
                if (defNPSBATCH.getNpsbatchOkCnt().equals(0) && defNPSBATCH.getNpsbatchTotCnt() > 0
                        && defNPSBATCH.getNpsbatchTotCnt().equals(defNPSBATCH.getNpsbatchFailCnt())) {
                    result = DeleteData();
                } else {
                    result = false;
                    job.writeLog("批號重覆");
                }
                if (!result) {
                    return false;
                }
            }

            //3.檢核整批轉即時扣款檔案內容、寫入NPSBATCH和NPSDTL
            if (result) {
                result = InsertAndCheckData();
            }

            //4.讀取NPSDTL產生ATMRequest電文，並以系統設定的Thread數執行
            if (result) {
                result = SubProcess();
            }

            //5.產生整批轉即時回饋檔
            if (result) {
                result = FeeBackSuccess();
            }

            //6.FEP上傳回饋檔
            File file = new File(uploadFilePath);
            if (file.exists()) {
                result = false;
                job.writeLog("FTP上傳回饋檔的檔案不存在");
            } else {
                SckBatch sckBatch = new SckBatch();
                String remoteSendFilePath = "D:\\TCBFTP\\AP1T\\PU\\" + outputFileName;
                sckBatch.FileUploadClient("10.0.5.250", 46464, uploadFilePath+outputFileName, remoteSendFilePath);
                sckBatch.uploadFile();
                job.writeLog("放檔成功");
//                FtpProperties ftpProperties = new FtpProperties();
//                ftpProperties.setHost(ftp);
//                ftpProperties.setPort(22);
//                ftpProperties.setPassword(ftpscode);
//                ftpProperties.setProtocol(FtpProtocol.SFTP);
//                ftpProperties.setUsername(ftpucode);
//                FtpAdapter ftpAdapter = factory.createFtpAdapter(ftpProperties);
//                String local = "D:/TXTT/" + inputFileName;
//			ftpAdapter.download(downloadFilePath,downloadFilePath);
//                ftpAdapter.upload(local, uploadFilePath, false);
            }

            return true;
        } catch (Exception e) {
            job.writeLog(e);
            System.out.println(e);
            return false;
        }
    }

    private boolean FeeBackSuccess() {
        int count = 0;
        String batNo = fileid + tbsdy + batchNo;
        Npsdtl qNPSDTL = new Npsdtl();
        StringBuilder sLine = null;
        try {
            qNPSDTL.setNpsdtlBatNo(batNo);
            List<Npsdtl> alld = npsdtlExtMapper.GetNPSDTLByBATNOforAll(batNo);
            if (alld == null) {
                job.writeLog("無明細資料，產生回饋檔失敗");
            } else {
                job.writeLog("開始產生回饋檔，檔名" + outputFileName);
            }
            File file = new File(uploadFilePath);
            if (file.exists()) {
                file.delete();
            }
            OutputStream os = new FileOutputStream(uploadFilePath);
            OutputStreamWriter writer = null;
            writer = new OutputStreamWriter(os, "Big5");
            /* 首筆 */
            sLine = new StringBuilder("");
            sLine.append("11");
            String date = defNPSBATCH.getNpsbatchTxDate();
            date = CalendarUtil.adStringToROCString(date);
            sLine.append(date);
            sLine.append(StringUtils.leftPad(defNPSBATCH.getNpsbatchBatchNo(), 13, "0"));
            sLine.append(StringUtils.leftPad(defNPSBATCH.getNpsbatchBranch(), 4, "0"));
            sLine.append(StringUtils.repeat(" ", 204));
            sLine.append(System.getProperty("line.separator"));
            writer.write(sLine.toString());
            writer.flush();
            sLine = null;

            /* 明細筆 */
            for (int i = 0; i < alld.size(); i++) {
                writer = new OutputStreamWriter(os, "Big5");
                sLine = new StringBuilder("");
                sLine.append("12");
                String detaildate = defNPSBATCH.getNpsbatchTxDate();
                detaildate = CalendarUtil.adStringToROCString(detaildate);
                sLine.append(detaildate);
                sLine.append(StringUtils.rightPad(defNPSBATCH.getNpsbatchBatchNo(), 13, " "));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlSeqNo().toString(), 10, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlAtmno(), 5, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlTerminalid(), 8, " "));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTxTime(), 6, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlBusinessUnit(), 8, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlPaytype(), 5, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlPayno(), 4, " "));
                Double txAmt = Double.valueOf(alld.get(i).getNpsdtlTxAmt().toString()) * 100;
                int txAmtt = txAmt.intValue();
                sLine.append(StringUtils.leftPad(String.valueOf(txAmtt), 11, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlIdno(), 11, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlReconSeq(), 16, " "));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTroutBkno7(), 7, "0"));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTroutActno(), 16, "0"));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTrinBkno(), 3, "0"));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTrinActno(), 16, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlAllowT1(), 1, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlDueDate(), 8, " "));
                sLine.append(StringUtils.rightPad("", 23, " "));
                sLine.append(StringUtils.rightPad("006" + alld.get(i).getNpsdtlStan(), 10, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlPcode(), 4, " "));
                if (alld.get(i).getNpsdtlTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
                        && alld.get(i).getNpsdtlTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    sLine.append("CB2W");
                } else {
                    sLine.append("AB3W");
                }
                String datetbsdy = CalendarUtil.adStringToROCString(alld.get(i).getNpsdtlTbsdy()); //西元轉民國
                sLine.append(StringUtils.leftPad(datetbsdy, 7, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlHostBrch(), 4, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlCbsRc(), 3, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlReplyCode(), 4, " "));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlHostCharge().toString(), 2, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlHostChargeFlag(), 1, " "));
                sLine.append(StringUtils.rightPad("", 11, " "));
                sLine.append(System.getProperty("line.separator"));
                writer.write(sLine.toString());
                writer.flush();
                sLine = null;
            }

            /* 尾筆 */
            writer = new OutputStreamWriter(os, "Big5");
            sLine = new StringBuilder("");
            sLine.append("19");
            String endDate = defNPSBATCH.getNpsbatchTxDate();
            endDate = CalendarUtil.adStringToROCString(endDate);
            sLine.append(endDate);
            sLine.append(StringUtils.rightPad(defNPSBATCH.getNpsbatchBatchNo(), 13, " "));
            sLine.append(StringUtils.leftPad(defNPSBATCH.getNpsbatchTotCnt().toString(), 6, "0"));
            Double tot = Double.valueOf(defNPSBATCH.getNpsbatchTotAmt().toString()) * 100;
            int tott = tot.intValue();
            sLine.append(StringUtils.leftPad(String.valueOf(tott), 14, "0"));
            sLine.append(StringUtils.leftPad(defNPSBATCH.getNpsbatchOkCnt().toString(), 6, "0"));
            Double ok = Double.valueOf(defNPSBATCH.getNpsbatchOkAmt().toString()) * 100;
            int okt = ok.intValue();
            sLine.append(StringUtils.leftPad(String.valueOf(okt), 14, "0"));
            sLine.append(StringUtils.leftPad(defNPSBATCH.getNpsbatchFailCnt().toString(), 6, "0"));
            Double fail = Double.valueOf(defNPSBATCH.getNpsbatchFailAmt().toString()) * 100;
            int failt = fail.intValue();
            sLine.append(StringUtils.leftPad(String.valueOf(failt), 14, "0"));
            sLine.append(StringUtils.repeat(" ", 148));
            sLine.append(System.getProperty("line.separator"));
            writer.write(sLine.toString());
            writer.flush();
            sLine = null;

            return true;
        } catch (Exception ex) {
            job.writeLog("刪檔發生例外錯誤:" + ex.getMessage());
            return false;
        }
    }

    private boolean SubProcess() {
        int count = 0;
        String batNo = fileid + tbsdy + batchNo;
        Npsdtl qNPSDTL = new Npsdtl();
        CountDownLatch latch = null;
        List<Threads> threadProcessorList = null;
        try {
            latch = new CountDownLatch(maxThreads);
            threadProcessorList = new ArrayList<>(maxThreads);
            for (int i = 0; i < maxThreads; i++) {
                threadNo = i;
                threadProcessorList.add(new Threads(batNo, threadNo, latch));
            }
            latch.await();
            qNPSDTL.setNpsdtlBatNo(batNo);
            Map<String, Object> result = npsdtlExtMapper.getNPSDTLTotalCNTAMT(qNPSDTL);
            if (result != null) {
                defNPSBATCH.setNpsbatchOkCnt(Integer.valueOf(result.get("OKCNT").toString()));
                defNPSBATCH.setNpsbatchOkAmt(new BigDecimal(result.get("OKAMT").toString()));
                defNPSBATCH.setNpsbatchFailCnt(Integer.valueOf(result.get("FAILCNT").toString()));
                defNPSBATCH.setNpsbatchFailAmt(new BigDecimal(result.get("FAILAMT").toString()));
                defNPSBATCH.setNpsbatchFeeCnt(Integer.valueOf(result.get("OKCNT").toString()));
                defNPSBATCH.setNpsbatchFeeAmt(new BigDecimal(result.get("OKFEE").toString()));
                defNPSBATCH.setNpsbatchDoTotCnt(detailRow);
                defNPSBATCH.setNpsbatchDoTotAmt(BigDecimal.valueOf(detailtxAmt));
                defNPSBATCH.setNpsbatchRspTime(FormatUtil.parseDataTime(Calendar.getInstance().toString(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
                defNPSBATCH.setNpsbatchResult("00");
            } else {
                job.writeLog("查不到NPSDTL的統計資料!!!(成功筆數金額、失敗筆數金額及手續費");
                sendEMS(_logData);
                defNPSBATCH.setNpsbatchOkCnt(0);
                defNPSBATCH.setNpsbatchOkAmt(BigDecimal.ZERO);
                defNPSBATCH.setNpsbatchFailCnt(0);
                defNPSBATCH.setNpsbatchFailAmt(BigDecimal.ZERO);
                defNPSBATCH.setNpsbatchFeeCnt(0);
                defNPSBATCH.setNpsbatchFeeAmt(BigDecimal.ZERO);
            }
            if (npsbatchExtMapper.updateByPrimaryKeySelective(defNPSBATCH) < 1) {
                job.writeLog("更新NPSBATCH失敗");
                sendEMS(_logData);
                return false;
            }
        } catch (Exception ex) {
            job.writeLog("刪檔發生例外錯誤:" + ex.getMessage());
            return false;
        }
        return true;
    }


    private class Threads extends Thread {
        private String batNo;
        private Integer threadNo;
        private CountDownLatch latch;
        private Object lock = new Object();

        public Threads(String batNo, Integer threadNo, CountDownLatch latch) {
            job.writeLog("ThreadProcessor-" + threadNo);
            this.batNo = batNo;
            this.threadNo = threadNo;
            this.latch = latch;
            start();
        }

        @Override
        public void run() {
            Npsdtl defNPSDTL = new Npsdtl();
            defNPSDTL.setNpsdtlBatNo(batchNo);
            defNPSDTL.setNpsdtlThreadNo(threadNo);
            List<Npsdtl> defNpsdtl = new ArrayList<>();
            defNpsdtl = npsdtlExtMapper.GetNPSDTLByBATNO(batNo, threadNo);
            if (defNpsdtl.size() > 0) {
                for (int j = 0; j <= defNpsdtl.size(); j++) {
                    MultiProcess(defNpsdtl.get(j));
                }
            }
            this.latch.countDown();
        }

    }


    private boolean InsertAndCheckData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        FileInputStream fis = null;
        String strZoneNo = null;
        Boolean result;
        Double strTxAmt = null;
        Double strTotalTxAmt = null;
        Integer strTotalTxRow = null;
        Npsdtl defNPSDTL = new Npsdtl();
        try {
//			File baseDirectory = new File(CleanPathUtil.cleanString(downloadFilePath));
//			fis = new FileInputStream(new File(baseDirectory, CleanPathUtil.cleanString(inputFileName)));
            File baseDirectory = new File(CleanPathUtil.cleanString("D:/TXTT/"));
            fis = new FileInputStream(new File(baseDirectory, CleanPathUtil.cleanString(inputFileName)));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "BIG5"));
            while (br.ready()) {
                String singleline = br.readLine();
                strZoneNo = singleline.substring(0, 2);
                switch (strZoneNo) {
                    case "01":
                        result = ParseHeader(singleline);
                        if (!result) {
                            return result;
                        }
                        break;
                    case "02":
                        result = ParseDetail(singleline, detailRow);
                        if (!result) {
                            return result;
                        }
                        //累計金額筆數
                        Double m = Double.valueOf(arrayDetail[7]);
                        strTxAmt = m / 100;
                        detailtxAmt += strTxAmt;
                        detailRow += 1;
                        strZoneDetail = true;
                        break;
                    case "09":
                        result = ParseFoot(singleline);
                        if (!result) {
                            return result;
                        }
                        strTotalTxAmt = Double.valueOf(arrayFoot[4]) / 100;
                        strTotalTxRow = Integer.valueOf(arrayFoot[3]);
                        break;
                }

                //檢查檔案內容
                result = CheckParseData(strZoneNo, detailRow);

                //檢查檔案內容-尾錄之交易總金額筆數
                if (strZoneNo.equals("09")) {
                    if (!strTxAmt.equals(strTotalTxAmt) && !detailRow.equals(strTotalTxRow)) {
                        defNPSBATCH.setNpsbatchResult("01");
                        job.writeLog("交易總金額或交易總筆數檢核不符");
                        npsbatchExtMapper.updateByPrimaryKeySelective(defNPSBATCH);
                        return false;
                    }
                }

                if (strZoneNo.equals("02")) {
                    defNPSDTL.setNpsdtlBatNo(fileid + tbsdy2 + batchNo);
                    defNPSDTL.setNpsdtlSeqNo(Integer.valueOf(arrayDetail[3]));
                    defNPSDTL.setNpsdtlTroutBkno(arrayDetail[13].substring(0, 3));
                    defNPSDTL.setNpsdtlTroutActno(arrayDetail[14]);
                    defNPSDTL.setNpsdtlTrinBkno(arrayDetail[15]);
                    defNPSDTL.setNpsdtlTrinActno(arrayDetail[16]);
                    defNPSDTL.setNpsdtlTxAmt(BigDecimal.valueOf(Double.valueOf(arrayDetail[10]) / 100));
                    defNPSDTL.setNpsdtlBusinessUnit(arrayDetail[7]);
                    defNPSDTL.setNpsdtlPaytype(arrayDetail[8]);
                    defNPSDTL.setNpsdtlPayno(arrayDetail[9]);
                    defNPSDTL.setNpsdtlIdno(StringUtils.rightPad(arrayDetail[11], 11, " "));
                    defNPSDTL.setNpsdtlReconSeq(arrayDetail[12]);
                    defNPSDTL.setNpsdtlTroutBkno7(arrayDetail[13]);
                    defNPSDTL.setNpsdtlAllowT1(arrayDetail[7]);
                    defNPSDTL.setNpsdtlTerminalid(arrayDetail[5]);
                    defNPSDTL.setNpsdtlAtmno(arrayDetail[4]);
                    defNPSDTL.setNpsdtlTxTime(arrayDetail[6]);
                    defNPSDTL.setNpsdtlBkno(SysStatus.getPropertyValue().getSysstatHbkno());
                    threadNo = detailRow % maxThreads;
                    defNPSDTL.setNpsdtlThreadNo(threadNo);
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[13].substring(0, 3))
                            && !SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[15])) {
                        defNPSDTL.setNpsdtlPcode("2261");
                        defNPSDTL.setNpsdtlTxCode("ED");
                    } else if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[13].substring(0, 3))
                            && SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[15])) {
                        defNPSDTL.setNpsdtlPcode("2262");
                        defNPSDTL.setNpsdtlTxCode("EW");
                    } else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[13].substring(0, 3))
                            && SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[15])) {
                        defNPSDTL.setNpsdtlPcode("2263");
                        defNPSDTL.setNpsdtlTxCode("EA");
                    } else if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[13].substring(0, 3))
                            && !SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[15])
                            && arrayDetail[13].substring(0, 3).equals(arrayDetail[15])) {
                        defNPSDTL.setNpsdtlPcode("2263");
                        defNPSDTL.setNpsdtlTxCode("EA");
                    } else if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[13].substring(0, 3))
                            && !SysStatus.getPropertyValue().getSysstatHbkno().equals(arrayDetail[15])
                            && !arrayDetail[13].substring(0, 3).equals(arrayDetail[15])) {
                        defNPSDTL.setNpsdtlPcode("2264");
                        defNPSDTL.setNpsdtlTxCode("ER");
                    }
                    try {
                        if (npsdtlExtMapper.insert(defNPSDTL) < 1) {
                            job.writeLog("NPSDTL寫入資料失敗");
                            transactionManager.rollback(txStatus);
                            return false;
                        }
                    } catch (Exception ex) {
                        job.writeLog("NPSDTL寫入資料發生例外錯誤:" + ex.getMessage());
                        transactionManager.rollback(txStatus);
                        return false;
                    }
                }

            }
            if (detailRow <= 0) {
                job.writeLog("無明細錄資料");
            }

            //新增NPBATCH
            defNPSBATCH.setNpsbatchTotAmt(BigDecimal.valueOf(strTotalTxAmt));
            defNPSBATCH.setNpsbatchTotCnt(strTotalTxRow);
            defNPSBATCH.setNpsbatchRcvTime(Calendar.getInstance().getTime());
            if (npsbatchExtMapper.updateByPrimaryKeySelective(defNPSBATCH) < 1) {
                job.writeLog(fileid + batchNo + "Update NPSBATCH失敗");
                transactionManager.rollback(txStatus);
                job.endTask();
            }
            transactionManager.commit(txStatus);
            defNPSDTL = null;
            return true;
        } catch (
                Exception ex) {
            job.writeLog("刪檔發生例外錯誤:" + ex.getMessage());
            transactionManager.rollback(txStatus);
            return false;
        }

    }

    private boolean CheckParseData(String strZoneNo, Integer detailRow) {
        String strTxDate = "";
        String strBatchNo = "";
        String strZone = null;
        try {
            switch (strZoneNo) {
                case "01":
                    strTxDate = arrayHeader[1];
                    strBatchNo = arrayHeader[2];
                    strZone = "首筆";
                    break;
                case "02":
                    strTxDate = arrayDetail[1];
                    strBatchNo = arrayDetail[2];
                    strZone = "明細";
                    break;
                case "09":
                    strTxDate = arrayFoot[1];
                    strBatchNo = arrayFoot[2];
                    strZone = "尾筆";
                    break;
            }

            String tbsdy3 = CalendarUtil.adStringToROCString(tbsdy);
            if (!strTxDate.equals(tbsdy3)) {
                job.writeLog(strZone + "之入扣帳日不同");
                return false;
            }
            if (!strBatchNo.equals(batchNo)) {
                job.writeLog(strZone + "之批號錯誤");
                return false;
            }
            //判斷是否為有效日期
            tbsdy2 = CalendarUtil.rocStringToADString(strTxDate);
            if (!CalendarUtil.validateDateTime(tbsdy2, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)) {
                job.writeLog(strZone + "之入扣帳日不為有效之日期");
                return false;
            }


        } catch (Exception ex) {
            job.writeLog("拆解文字檔內容之尾錄資料失敗" + ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean ParseFoot(String line) {
        try {
            arrayFoot[0] = line.substring(0, 2);
            arrayFoot[1] = line.substring(2, 9);
            arrayFoot[2] = line.substring(9, 22);
            arrayFoot[3] = line.substring(22, 26);
            arrayFoot[4] = line.substring(26, 42);
            arrayFoot[5] = line.substring(42, 180);
        } catch (Exception ex) {
            job.writeLog("拆解文字檔內容之尾錄資料失敗" + ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean MultiProcess(Npsdtl npsdtl) {
        String batNo;
        String seqNo;
        int Failcnt = 0;
        int doTOTcnt = 0;
        BigDecimal failamt = null;
        BigDecimal doTOTamt = null;
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String nb_rq = "";
        try {
            if (!npsdtl.getNpsdtlBatNo().substring(8, 16).equals(SysStatus.getPropertyValue().getSysstatTbsdyFisc())
                    && npsdtl.getNpsdtlAllowT1().equals("N")) {
                npsdtl.setNpsdtlResult("01");
                npsdtl.setNpsdtlErrMsg("財金已換日，不允許逾時交易");
                npsdtlExtMapper.updateByPrimaryKeySelective(npsdtl);
                Failcnt += 1;
                doTOTcnt += 1;
                failamt.add(npsdtl.getNpsdtlTxAmt());
                doTOTamt.add(npsdtl.getNpsdtlTxAmt());
                defNPSBATCH.setNpsbatchFailCnt(Failcnt);
                defNPSBATCH.setNpsbatchDoTotCnt(doTOTcnt);
                defNPSBATCH.setNpsbatchFailAmt(failamt);
                defNPSBATCH.setNpsbatchDoTotAmt(doTOTamt);
                npsbatchExtMapper.updateByPrimaryKeySelective(defNPSBATCH);
            }
            batNo = npsdtl.getNpsdtlBatNo();
            seqNo = String.valueOf(npsdtl.getNpsdtlSeqNo());
            if (rtnCode == CommonReturnCode.Normal) {
                nb_rq = prepareATMReq(npsdtl);
                if (nb_rq == null) {
                    rtnCode = CommonReturnCode.ParseTelegramError;
                }
            }
            if (rtnCode == CommonReturnCode.Normal) {
                String nbDataRes = "";
                NBFEPHandler mNbHandler = new NBFEPHandler();
                try {
                    if (npsdtl.getNpsdtlTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
                            && npsdtl.getNpsdtlTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                        aaName = "NBPYSelfIssue";
                    } else if (npsdtl.getNpsdtlTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                        aaName = "NBSelfIssueRequest";
                    } else {
                        aaName = "ANBPYOtherRequestA";
                    }
                    nbDataRes = mNbHandler.dispatch(nb_rq);
                    if (nbDataRes == null) {
                        rtnCode = CommonReturnCode.ProgramException;
                    }
                    brch = mNbHandler.getBrch();
                    charge = mNbHandler.getCharge();
                    chargeFlag = mNbHandler.getChargeFlag();
                    UpdateNPSDTL(npsdtl, nbDataRes, rtnCode);
                } catch (Exception e) {
                    job.writeLog("FISCHandlerDispatch [AA 2160] Fail!!!");
                    job.writeLog(e.getMessage());
                    return false;
                }
            }

        } catch (Exception ex) {
            job.writeLog();
            return false;
        }
        return true;
    }

    private Boolean UpdateNPSDTL(Npsdtl npsdtl, String Res, FEPReturnCode rtnCode) {
        SEND_NB_GeneralTrans_RS nbDataRes = deserializeFromXml(Res, SEND_NB_GeneralTrans_RS.class);
        try {
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
            npsdtl.setNpsdtlFee(nbDataRes.getBody().getRs().getSvcRs().getCUSTPAYFEE());

            if (nbDataRes.getBody().getRs().getSvcRs().getTXNTYPE().equals("RQ")) {
                npsdtl.setNpsdtlHostCharge(this.getCharge().intValue());
            } else {
                npsdtl.setNpsdtlHostCharge(0);
            }
            if (nbDataRes.getBody().getRs().getSvcRs().getTXNTYPE().equals("RQ")) {
                npsdtl.setNpsdtlHostBrch(this.getBrch());
            } else {
                npsdtl.setNpsdtlHostBrch(defNPSBATCH.getNpsbatchBranch());
            }
            npsdtl.setNpsdtlCbsRc(nbDataRes.getBody().getRs().getSvcRs().getTCBRTNCODE());
            npsdtl.setNpsdtlHostChargeFlag(this.getChargeFlag());
            npsdtl.setNpsdtlThreadNo(threadNo);
            if (npsdtlExtMapper.updateByPrimaryKey(npsdtl) < 1) {
                job.writeLog("更新NPSDTL失敗");
                sendEMS(_logData);
                return false;
            }

            return true;
        } catch (Exception ex) {
            job.writeLog();
        }
        return true;
    }

    private String prepareATMReq(Npsdtl npsdtl) {
        String batNo;
        String seqNo;
        String rtnMessage = "";
        try {
            batNo = npsdtl.getNpsdtlBatNo();
            seqNo = String.valueOf(npsdtl.getNpsdtlSeqNo());
            RCV_NB_GeneralTrans_RQ nnb = new RCV_NB_GeneralTrans_RQ();
            RCV_NB_GeneralTrans_RQ nb = newXml(nnb);
//            nbdata.setTxNbfepObject(new NBFEPGeneral());

//            NBFEPGeneral nbg = new NBFEPGeneral();

            nb.getBody().getRq().getHeader().setCLIENTTRACEID(FEPChannel.NAM + batNo + seqNo);
            nb.getBody().getRq().getHeader().setCHANNEL(FEPChannel.NAM.toString());
            nb.getBody().getRq().getHeader().setMSGID(npsdtl.getNpsdtlTxCode() + npsdtl.getNpsdtlPcode());
            nb.getBody().getRq().getHeader().setMSGKIND("G");
            nb.getBody().getRq().getHeader().setTXNID("");
            nb.getBody().getRq().getHeader().setBRANCHID("");
            nb.getBody().getRq().getHeader().setTERMID("");
            nb.getBody().getRq().getHeader().setCLIENTDT(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
            nb.getBody().getRq().getSvcRq().setINDATE(defNPSBATCH.getNpsbatchTxDate());
            nb.getBody().getRq().getSvcRq().setINTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            nb.getBody().getRq().getSvcRq().setIPADDR(" ");
            nb.getBody().getRq().getSvcRq().setTXNTYPE("RQ");
            nb.getBody().getRq().getSvcRq().setTERMINALID(npsdtl.getNpsdtlTerminalid());
            nb.getBody().getRq().getSvcRq().setTERMINAL_TYPE(" ");
            nb.getBody().getRq().getSvcRq().setTERMINAL_CHECKNO(" ");
            nb.getBody().getRq().getSvcRq().setFSCODE(npsdtl.getNpsdtlTxCode());
            nb.getBody().getRq().getSvcRq().setPCODE(npsdtl.getNpsdtlPcode());
            nb.getBody().getRq().getSvcRq().setBUSINESSTYPE("T");
            nb.getBody().getRq().getSvcRq().setTRANSAMT(npsdtl.getNpsdtlTxAmt());
            nb.getBody().getRq().getSvcRq().setTRANBRANCH("");
            nb.getBody().getRq().getSvcRq().setTRNSFROUTIDNO(npsdtl.getNpsdtlIdno());
            nb.getBody().getRq().getSvcRq().setTRNSFROUTBANK(npsdtl.getNpsdtlTroutBkno7());
            nb.getBody().getRq().getSvcRq().setTRNSFROUTACCNT(npsdtl.getNpsdtlTroutActno());
            nb.getBody().getRq().getSvcRq().setTRNSFRINBANK(npsdtl.getNpsdtlTrinBkno() + "0000");
            nb.getBody().getRq().getSvcRq().setTRNSFRINACCNT(npsdtl.getNpsdtlTrinActno());
            nb.getBody().getRq().getSvcRq().setFEEPAYMENTTYPE("");
            nb.getBody().getRq().getSvcRq().setCUSTPAYFEE(null);
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
            nb.getBody().getRq().getSvcRq().getPAYDATA().setNPBRANCH(defNPSBATCH.getNpsbatchBranch());
            nb.getBody().getRq().getSvcRq().getPAYDATA().setIDENTIFIER("");
            nb.getBody().getRq().getSvcRq().setTEXTMARK("");
//            nbdata.getTxNbfepObject().setRequest(nb);

            rtnMessage = XmlUtil.toXML(nb);
            return rtnMessage;
        } catch (Exception ex) {
        }
        return null;
    }

    private RCV_NB_GeneralTrans_RQ newXml(RCV_NB_GeneralTrans_RQ nb) {
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body rsbody = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq msg = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header header = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq body = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq();
        RCV_NB_GeneralTrans_RQ_Body_MsgRq_PAYDATA pay = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_PAYDATA();
        msg.setHeader(header);
        body.setPAYDATA(pay);
        msg.setSvcRq(body);
        rsbody.setRq(msg);
        nb.setBody(rsbody);
        return nb;
    }

    private boolean ParseDetail(String line, Integer detailRow) {
        try {
            arrayDetail[0] = line.substring(0, 2);
            arrayDetail[1] = line.substring(2, 9);
            arrayDetail[2] = line.substring(9, 22);
            arrayDetail[3] = line.substring(22, 32);
            arrayDetail[4] = line.substring(32, 37);
            arrayDetail[5] = line.substring(37, 45);
            arrayDetail[6] = line.substring(45, 51);
            arrayDetail[7] = line.substring(51, 59);
            arrayDetail[8] = line.substring(59, 64);
            arrayDetail[9] = line.substring(64, 68);
            arrayDetail[10] = line.substring(68, 79);
            arrayDetail[11] = line.substring(79, 90);
            arrayDetail[12] = line.substring(90, 106);
            arrayDetail[13] = line.substring(106, 113);
            arrayDetail[14] = line.substring(113, 129);
            arrayDetail[15] = line.substring(129, 132);
            arrayDetail[16] = line.substring(132, 148);
            arrayDetail[17] = line.substring(148, 149);
            arrayDetail[18] = line.substring(149, 157);
            arrayDetail[19] = line.substring(157, 180);
        } catch (Exception ex) {
            job.writeLog("拆解文字檔內容之明細資料第" + detailRow + "筆失敗" + ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean ParseHeader(String line) {
        try {
            arrayHeader[0] = line.substring(0, 2);
            arrayHeader[1] = line.substring(2, 9);
            arrayHeader[2] = line.substring(9, 22);
            arrayHeader[3] = line.substring(22, 26);
            arrayHeader[4] = line.substring(26, 180);
        } catch (Exception ex) {
            job.writeLog("拆解文字檔內容之尾錄資料失敗" + ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean DeleteData() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Npsbatch defNPSBATCH = new Npsbatch();
            defNPSBATCH.setNpsbatchBatchNo(batchNo);
            defNPSBATCH.setNpsbatchFileId(fileid);
            defNPSBATCH.setNpsbatchTxDate(tbsdy2);
            if (npsbatchExtMapper.deleteByPrimaryKey(defNPSBATCH) < 1) {
                job.writeLog("刪除NPSBATCH失敗");
                transactionManager.rollback(txStatus);
                return false;
            }

            Npsdtl defNPSDTL = new Npsdtl();
            defNPSDTL.setNpsdtlBatNo(fileid + tbsdy + batchNo);
            if (npsdtlExtMapper.deleteByBATNO(defNPSDTL.getNpsdtlBatNo()) < 1) {
                job.writeLog("刪除NPSDTL失敗");
                transactionManager.rollback(txStatus);
                return false;
            }
            transactionManager.commit(txStatus);
            return true;
        } catch (Exception ex) {
            job.writeLog("刪檔發生例外錯誤:" + ex.getMessage());
            transactionManager.rollback(txStatus);
            return false;
        }
    }

    private boolean FEPDownload() {
        try {
//            FtpProperties ftpProperties = new FtpProperties();
//            ftpProperties.setHost(ftp);
//            ftpProperties.setPort(22);
//            ftpProperties.setPassword(ftpscode);
//            ftpProperties.setProtocol(FtpProtocol.SFTP);
//            ftpProperties.setUsername(ftpucode);
//            FtpAdapter ftpAdapter = factory.createFtpAdapter(ftpProperties);
//            String local = "D:/TXTT/" + inputFileName;
//			ftpAdapter.download(downloadFilePath,downloadFilePath);
//            downloadFilePath = "/data/TEST202303081111129000200.TXT";
//            ftpAdapter.download(local, downloadFilePath);
            SckBatch sckBatch = new SckBatch();
            String remoteFilePath = "D:\\TCBFTP\\AP1T\\PU\\" + inputFileName;
            sckBatch.SimpleFileReceiver("10.0.5.250", 46464, batchLogPath + inputFileName, remoteFilePath);
            sckBatch.receiveSocketFile();
            job.writeLog("取檔成功");
        } catch (Exception ex) {
            job.writeLog(ex.getMessage());
            log = ex.toString();
            return false;
        }
        return true;
    }

    public static int getWordCountRegex(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        int length = s.length();
        return length;
    }

    /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private void initialBatch(String[] args) {
        // 0. 初始化logContext物件,傳入工作執行參數
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        // 1. 檢查Batch Log目錄參數
        batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(batchLogPath)) {
            System.out.println("Batch Log目錄未設定，請修正");
            return;
        }

        // 2. 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, batchLogPath);
        job.writeLog("ImportNPSUNIT start!");
    }

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
}
