package com.syscom.fep.batch.task.inbk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.MerchantExtMapper;
import com.syscom.fep.server.common.batch.SckBatch;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.model.Merchant;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 */
public class ImportMERCHANT extends FEPBase implements Task {

    private boolean batchResult = false;
    private String batchLogPath = StringUtils.EMPTY;
    private String fileName = StringUtils.EMPTY;

    private String _batchInputPath = StringUtils.EMPTY;
    private String _batchInputFile = StringUtils.EMPTY;
    private String _batchLogPath = StringUtils.EMPTY;
    private BatchReturnCode batchRC;

    private BatchJobLibrary job = null;
    private String effDate = "";
    private int countOK = 0;

    private MerchantExtMapper merchantMapper = SpringBeanFactoryUtil.getBean(MerchantExtMapper.class);

    @Override
    public BatchReturnCode execute(String[] args) {
        try {
            batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();

            if (StringUtils.isBlank(batchLogPath)) {
                LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
                return BatchReturnCode.Succeed;
            }
            //初始化相關批次物件及拆解傳入參數
            this.initialBatch(args);
            job.writeLog("---------------------------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始!");
            //回報批次平台開始工作
            job.startTask();
            if (!this.checkConfig()) {
                job.stopBatch();
                return BatchReturnCode.Succeed;
            }
            if (StringUtils.isBlank(_batchInputPath) || StringUtils.isBlank(_batchInputFile)) {
                LogHelperFactory.getGeneralLogger().error("參數BatchInputPath未設定");
                batchRC = BatchReturnCode.TableNotFound;
                return batchRC;
            }
            //批次主要處理流程
            if (doBusiness() == BatchReturnCode.Succeed) {
                job.writeLog(ProgramName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
                return batchRC;
            }
            return BatchReturnCode.Succeed;
        } catch (Exception ex) {
            //通知批次作業管理系統工作失敗,暫停後面流程
            try {
                job.abortTask();
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog(ex.toString());
                job.writeLog("------------------------------------------------------------------");
            } catch (Exception e) {
                logContext.setProgramException(e);
                logContext.setProgramName(ProgramName);
                sendEMS(logContext);
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

    private BatchReturnCode doBusiness() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        FileInputStream fis = null;
        FileInputStream fisNew = null;
        try {
            //取檔
            SckBatch sckBatch = new SckBatch();
            String remoteFilePath="D:\\TCBFTP\\AP1T\\FEP\\SL\\"+_batchInputFile;
            sckBatch.SimpleFileReceiver("10.0.5.250", 46464, _batchInputPath+_batchInputFile, remoteFilePath);
            sckBatch.receiveSocketFile();

            //清檔
            job.writeLog("TruncateMERCHANT");
            merchantMapper.truncateMERCHANT();

            //寫檔
            File baseDirectory = new File(CleanPathUtil.cleanString(_batchInputPath));
            fis = new FileInputStream(new File(baseDirectory, CleanPathUtil.cleanString(_batchInputFile)));
            if(fis == null){
                batchRC = BatchReturnCode.FileNotFound;
                return batchRC;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "BIG5"));
            int count = 0;
            while (br.ready()) {
                count++;
                String singleline = br.readLine();

                if (!TranNPSUNIT(singleline)) {
                    job.writeLog("insert MERCHANT Fail");
                    transactionManager.rollback(txStatus);
                    batchRC=BatchReturnCode.DBIOError;
                    return batchRC;
                }
            }
            job.writeLog("MERCHANT Table一共新增:" +countOK +"筆數");
            transactionManager.commit(txStatus);

            if (fis != null) {
                safeClose(fis);
            }
            //修改副檔名
            File a = new File(_batchInputPath + _batchInputFile);
            File b = new File(_batchInputPath + _batchInputFile.substring(0, _batchInputFile.length() - 3) + "BAK");

            a.renameTo(b);
            batchRC = BatchReturnCode.Succeed;
            return batchRC;
        } catch (Exception e) {
            job.writeLog(e);
            System.out.println(e);
            transactionManager.rollback(txStatus);
            batchRC = BatchReturnCode.ProgramException;
            return batchRC;
        }
    }

    private boolean TranNPSUNIT(String singleline) {
        boolean rtn = true;//是否正常跑完
        try {
            if (getWordCountRegex(singleline) > 81) {
                job.writeLog("資料長度錯誤(" + getWordCountRegex(singleline) + "), 應為81");
                return false;
            } else {
                String[] array = singleline.split(";");
                Merchant merchant = new Merchant();
                merchant.setMerchantBank(array[0]);
                merchant.setMerchantId(array[1]);
                merchant.setMerchantName(array[2].trim());
                merchant.setMerchantAbbnm(array[3].trim());
                merchant.setMerchantMcc(array[4].trim());
                merchant.setUpdateUserid(0);
                merchant.setUpdateTime(new Date());
                if (merchantMapper.selectByPrimaryKey(merchant.getMerchantId()) == null) {
                    if(merchantMapper.insert(merchant)<1){
                        return false;
                    }else{
                        countOK +=1;
                        return true;
                    }
                } else {
                    if(merchantMapper.updateByPrimaryKey(merchant)<1){
                        return false;
                    }else{
                        countOK +=1;
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            job.writeLog(ex.getMessage());
            rtn = false;
        }
        return rtn;
    }

    /**
     * 初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private void initialBatch(String[] args) {
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        //檢查Batch Log目錄參數
        _batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();

        if (StringUtils.isBlank(_batchLogPath)) {
            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
            return;
        }

        //初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, _batchLogPath);
    }

    /**
     * 檢查是否為營業日
     *
     * @return
     * @throws Exception
     */
    private boolean checkConfig() throws Exception {
//        if (job.getArguments().containsKey("FORCERUN") && "Y".equals(job.getArguments().get("FORCERUN"))) {
//            //不管是否為營業日，強制執行。
//        } else {
//            if (!(job.isBsDay(ATMZone.TWN.toString()))) {
//
//                job.writeLog("非營業日不執行批次");
//                job.stopBatch();
//                return false;
//            }
//        }

        _batchInputPath = CMNConfig.getInstance().getBatchInputPath();
        if (job.getArguments().containsKey("FILEID")) {
            fileName = job.getArguments().get("FILEID");
        }
//		_batchInputPath ="D:/TXTT/";
        _batchInputFile = fileName;

        return true;
    }

    public void safeClose(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException ex) {
                job.writeLog(ex.getMessage());
            }
        }
    }


    public static int getWordCountRegex(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        int length = s.length();
        return length;
    }

}
