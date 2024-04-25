package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.NpschkExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.server.common.batch.SckBatch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Han
 */
public class ImportNPSUNIT extends FEPBase implements Task {

    private String _programName = ImportNPSUNIT.class.getSimpleName(); //程式名稱
    private String batchLogPath = StringUtils.EMPTY;
    private String fileName = StringUtils.EMPTY;
    private String batchInputFile = StringUtils.EMPTY;
    private String log = StringUtils.EMPTY;
    private BatchJobLibrary job = null;
    private LogData _logData;
    private String _BatchInputFilePath;   //全名
    private String _batchInputPath;
    private String _batchInputFile;
    private BatchReturnCode batchRC;
    private String effDate;
    private int countAll = 0;
    private int countOK = 0;
    private NpsunitExtMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    private NpschkExtMapper npschkExtMapper = SpringBeanFactoryUtil.getBean(NpschkExtMapper.class);

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

            //檢查公用變數值
            if (StringUtils.isBlank(_batchInputPath)) {
                _logData.setRemark("參數BatchInputPath未設定");
                job.writeLog(_logData.getRemark());
                BatchJobLibrary.sendEMS(_logData);
                job.stopBatch();
                return BatchReturnCode.TableNotFound;
            }

            // 主流程
            if (doBusiness() == BatchReturnCode.Succeed) {
                job.writeLog(_programName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(_programName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
                return batchRC;
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

    private BatchReturnCode doBusiness() {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        FileInputStream fis = null;
        FileInputStream fisNew = null;
        try {
            //取檔
            SckBatch sckBatch = new SckBatch();
            String remoteFilePath="D:\\TCBFTP\\AP1T\\FEP\\payunt\\"+fileName;
            sckBatch.SimpleFileReceiver("10.0.5.250", 46464, _BatchInputFilePath, remoteFilePath);
            sckBatch.receiveSocketFile();
            //清檔
            job.writeLog("TruncateNPSUNIT");
            npsunitExtMapper.deleteNPSUNIT();

            //寫檔
            File baseDirectory = new File(CleanPathUtil.cleanString(_batchInputPath));
            fis = new FileInputStream(new File(baseDirectory, CleanPathUtil.cleanString(fileName)));
            if (fis == null) {
                batchRC = BatchReturnCode.FileNotFound;
                return batchRC;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            int count = 0;
            while (br.ready()) {
                count++;
                String singleline = br.readLine();

                if (!TranNPSUNIT(singleline)) {
                    if (Integer.parseInt(singleline) == countOK) {
                        job.writeLog("應計筆數:" + Integer.parseInt(singleline) + ",實際筆數為:" + countOK);
                        transactionManager.commit(txStatus);
                    } else {
                        if (countOK == 0) {
//                            String date = CalendarUtil.rocStringToADString14(singleline);
                            job.writeLog("日期為:" + singleline);
                        } else {
                            job.writeLog("資料筆數有問題:" + log + ",正常執行筆數為:" + countOK);
                            transactionManager.rollback(txStatus);
                            batchRC = BatchReturnCode.DBIOError;
                            return batchRC;
                        }
                    }
                }
            }
            if (fis != null) {
                safeClose(fis);
            }
            //修改副檔名
            File a = new File(_batchInputPath + fileName);
            File b = new File(_batchInputPath + fileName.substring(0, fileName.length() - 3) + "BAK");

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

    public void safeClose(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException ex) {
                job.writeLog(ex.getMessage());
            }
        }
    }

    private boolean TranNPSUNIT(String singleline) {
        boolean rtn = true;//是否正常跑完
        try {
            if (getWordCountRegex(singleline) > 262) {
                job.writeLog("資料長度錯誤(" + getWordCountRegex(singleline) + "), 應為206");
                return false;
            } else if (getWordCountRegex(singleline) == 6) {
                if (Integer.parseInt(singleline) == countOK) {
                    job.writeLog("最後一筆筆數為:" + Integer.parseInt(singleline));
                    log = "最後一筆筆數為:" + Integer.parseInt(singleline);
                    return false;
                } else {
                    return false;
                }
            } else {
                String[] array = singleline.split("	");
                Npsunit npsunit = new Npsunit();
                npsunit.setNpsunitNo(array[0]);
                npsunit.setNpsunitPaytype(array[3]);
                npsunit.setNpsunitFeeno(array[4]);
                npsunit.setNpsunitName(array[1]);
                npsunit.setNpsunitNameS(array[2]);
                npsunit.setNpsunitPayName(array[5]);
                npsunit.setNpsunitBkno(array[6]);
                npsunit.setNpsunitMonthlyFg(array[7]);
                npsunit.setNpsunitEffectdate(array[8]);
                npsunit.setNpsunitFee(new BigDecimal(array[9]));
                npsunit.setNpsunitRecvFee1(new BigDecimal(array[10]));
                npsunit.setNpsunitRecvFee2(new BigDecimal(array[11]));
                npsunit.setNpsunitRecvFee3(new BigDecimal(array[12]));
                npsunit.setNpsunitRecvFee4(new BigDecimal(array[13]));
                npsunit.setNpsunitOtherFee1(new BigDecimal(array[14]));
                npsunit.setNpsunitOtherFee2(new BigDecimal(array[15]));
                npsunit.setNpsunitOtherFee3(new BigDecimal(array[16]));
                npsunit.setNpsunitOtherFee4(new BigDecimal(array[17]));
                npsunit.setNpsunitOtherFee5(new BigDecimal(array[18]));
                npsunit.setNpsunitTrinBkno(array[19]);
                npsunit.setNpsunitPaytypeName(array[20]);
                npsunit.setNpsunitReFg(array[21]);
                npsunit.setNpsunitUnitNo(Short.valueOf(array[22]));
                npsunit.setNpsunitPayKind(Short.valueOf(array[23]));
                npsunit.setNpsunitUnit(Short.valueOf(array[24]));
                npsunit.setUpdateUserid(0);
                npsunit.setUpdateTime(new Date());
                if (npsunitExtMapper.insert(npsunit) < 1) {
                    return false;
                } else {
                    countOK += 1;
                    return true;
                }
            }
        } catch (
                Exception ex) {
            job.writeLog(ex.getMessage());
            log = ex.toString();
            rtn = false;
        }
        return rtn;
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

        if (job.getArguments().containsKey("FILEID")) {
            fileName = job.getArguments().get("FILEID");
        }
        //設定公用變數
        _batchInputPath = CMNConfig.getInstance().getBatchInputPath().trim();
//		_batchInputPath ="D:/TXTT/";

        _BatchInputFilePath = _batchInputPath + fileName;

    }
}
