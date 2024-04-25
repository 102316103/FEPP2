package com.syscom.fep.batch.task.cmn;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.CMNConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchivingLogFile implements Task {
    private String _programName = ArchivingLogFile.class.getSimpleName();
    private BatchJobLibrary _job = null;
    private String _BatchLogPath = StringUtils.EMPTY;
    private LogData _logData = null;
    // 批次相關參數
    private String sourceDir = StringUtils.EMPTY;// 備份的來源目錄
    private String targetDir = StringUtils.EMPTY;// 備份的目的目錄
    private Integer archiveDay = 0;// 壓縮幾天前的Log檔
    private Integer reserveDay = 0;// 保留幾天壓縮檔
    private Boolean callBatchJob = true;// 用來指出是否要與Batch Job Service互動

    /**
     * 將來源資料夾下的檔案在指定天數以前的檔壓縮備份至目的資料夾,並刪除目的資料夾保留天數之前的壓縮檔
     * 傳入參數:
     * SourceDir-來源目錄
     * TargetDir-目的目錄,可以加上日期格式化字元例如{0:yyyyMMdd}做為目錄名稱一部分
     * ReserveDay-保留天數
     *
     * @param args args
     */
    @Override
    public BatchReturnCode execute(String[] args) {
        File jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        try {
            // 初始化相關批次物件及拆解傳入參數
            initialBatch(args);
            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            _job.writeLog("------------------------------------------------------------------");
            _job.writeLog(_programName + "開始");
            // 回報批次平台開始工作
            if (callBatchJob)
                _job.startTask();
            // 執行批次主要工作
            Process();
            _job.writeLog(_programName + "正常結束!");
            // 回報批次平台結束工作
            if (callBatchJob)
                _job.endTask();
            // 將此執行結果寫至TWSLOG
            // _job.InsertTWSLog( //
            // 		FEPConfig.getInstance().getHostName(), //
            // 		Integer.valueOf(BatchResult.Successful.getValue()).byteValue(), //
            // 		_programName, //
            // 		jarFile.getName() //
            //);
            return BatchReturnCode.Succeed;
        } catch (Exception ex) {
            if (_job != null) {

                // 回報批次平台工作失敗,暫停後面流程
                if (callBatchJob) {
                    try {
                        _job.abortTask();
                    } catch (Exception e) {
                        _job.writeLog(_programName + "失敗!");
                    }
                }

                _job.writeLog(_programName + "失敗!");
                _job.writeLog(ex.toString());
                _logData.setProgramException(ex);
                // 不得直接呼叫FEPBase.SendEMS
                BatchJobLibrary.sendEMS(_logData);
            }
            // 將此執行結果寫至TWSLOG
//			_job.InsertTWSLog( //
//					FEPConfig.getInstance().getHostName(), //
//					Integer.valueOf(BatchResult.Failed.getValue()).byteValue(), //
//					_programName, //
//					jarFile.getName() //
//			);
            return BatchReturnCode.ProgramException;
        } finally {
            if (_job != null) {
                _job.writeLog(_programName + "結束!!");
                _job.writeLog("------------------------------------------------------------------");
                _job.dispose();
                _job = null;
            }
            if (_logData != null) {
                _logData = null;
            }
        }
    }

    private void initialBatch(String[] args) {
        _logData = new LogData();
        _logData.setChannel(FEPChannel.BATCH);
        _logData.setEj(0);
        _logData.setProgramName(_programName);
        // 2023-10-05 Richard add 如果沒有傳入BatchLogPath, 則需要從db中獲取設定值
        if (Arrays.stream(args).noneMatch(t -> StringUtils.startsWithIgnoreCase(t, "/BatchLogPath"))) {
            _BatchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        }
        if (StringUtils.isBlank(_BatchLogPath)) {
            _BatchLogPath = "/fep/logs";
        }
        // 初始化BatchJob物件,傳入工作執行參數
        _job = new BatchJobLibrary(this, args, _BatchLogPath);
        // 顯示help說明
        if (_job.getArguments().containsKey("?")) {
            DisplayUsage();
            return;
        }
        // 檢查目錄
        if (StringUtils.isBlank(_job.getArguments().get("BatchLogPath"))) {
            System.out.println("Batch Log目錄未設定，請修正");
            return;
        }
        // 拆解傳入的參數並存入變數
        if (_job.getArguments().containsKey("SourceDir")) {
            sourceDir = _job.getArguments().get("SourceDir");
        } else {
            System.out.println("必須傳入參數SourceDir");
            return;
        }
        if (_job.getArguments().containsKey("TargetDir")) {
            targetDir = _job.getArguments().get("TargetDir");
        } else {
            System.out.println("必須傳入參數TargetDir");
            return;
        }
        if (_job.getArguments().containsKey("ReserveDay")) {
            reserveDay = Integer.parseInt(_job.getArguments().get("ReserveDay"));
        }
        if (_job.getArguments().containsKey("ArchiveDay")) {
            archiveDay = Integer.parseInt(_job.getArguments().get("ArchiveDay"));
        } else {
            System.out.println("必須傳入參數ArchiveDay");
            return;
        }
        if (_job.getArguments().containsKey("CallBatchJob")) {
            callBatchJob = Boolean.parseBoolean(_job.getArguments().get("CallBatchJob"));
        }
    }

    private void Process() {
        if (ArchiveFolder()) {
            // 壓縮成功後刪除保留日期之前的壓縮檔
            if (reserveDay > 0) {
                _job.writeLog(String.format("Begin delete earlier than %s days archive file", reserveDay.toString()));
                DeleteArchiveFile();
            }
        }
    }

    private Boolean ArchiveFolder() {
        // Log檔目錄
        File sourceDirFile = new File(sourceDir);
        File[] paths = sourceDirFile.listFiles();
        // 壓縮幾天前Log檔日期
        LocalDateTime archiveDate = LocalDateTime.now().minusDays(archiveDay);
        String begindate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(archiveDate);

        for (File path : paths) {
            String fname = path.getName();
            // 不符合log目錄格式(YYYY-MM-DD) 且 大於archiveDay指定日期，不處理
            if (!checkName(fname) || fname.compareTo(begindate) > 0) {
                continue;
            }
            Path source = Paths.get(sourceDir, path.getName());
            Path dest = Paths.get(targetDir, path.getName() + ".tar.gz");
            try {
                // 壓縮檔案
                compressFolder(source.toString(), dest.toString());
                // 刪除Log檔目錄
//				path.delete(); // 只能刪除空資料夾
                FileUtils.forceDelete(path);
            } catch (IOException e) {
                _job.writeLog("Compress folder failed:" + e.getMessage());
//				e.printStackTrace();
            }
        }
        return true;
    }

    // private Boolean ArchiveFiles() {
    // Boolean bRes = false;
    // File f = new File(sourceDir);
    // FilenameFilter fileNameFilter = new FilenameFilter() {
    // @Override
    // public boolean accept(File dir, String name) {
    // if (name.lastIndexOf('.') > 0) {
    // int lastIndex = name.lastIndexOf('.');// get last index for '.' char
    // String str = name.substring(lastIndex);// get extension
    // if (str.equals(".txt"))
    // return true;// match path name extension
    // }
    // return false;
    // }
    // };
    // // 取得目錄下所有的.txt檔案
    // File[] paths = f.listFiles(fileNameFilter);

    // SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    // ArrayList<String> rarFileName = new ArrayList<>();// 存放欲產生的壓縮檔
    // ArrayList<String> rarFiles = new ArrayList<>();// 存放欲壓縮的檔案清單,以";"區隔,存放多個檔名
    // // 壓縮檔規則:將檔案之最後修改時間在指定日期之前的檔案,依主要檔名(不含日期)壓成一個檔
    // try {
    // // for each pathname in pathname array
    // for (File path : paths) {
    // // prints file and directory paths
    // long lastmodifydate = path.lastModified();
    // Date filedate = new Date();
    // filedate.setTime(lastmodifydate);
    // long lmodifyday = Long.valueOf(format.format(filedate)).longValue();//
    // 取得檔案的最後編輯日期
    // long lastArchiveday = getDate(archiveDay);// 取得指定壓縮天數的日期
    // // 判斷檔案最後編輯日期是否小於等於欲壓縮的天數日期
    // if (lmodifyday <= lastArchiveday) {
    // String name = path.getName();// 取得欲壓縮之檔名(主要檔名(不含日期)壓成一個檔)
    // Integer seq = name.indexOf("_");
    // if (seq > -1)
    // name = name.substring(0, 0 + seq);
    // else {
    // seq = name.indexOf(".");
    // name = name.substring(0, 0 + seq);
    // }
    // String desPath = targetDir + File.separator + format.format(filedate);//
    // 目的路徑= targetDir + \\ +
    // // 檔案的最後編輯日期(ex:
    // // d:\\feplog\backup\20220512)
    // File file = new File(desPath);// 此為壓縮檔目的地目錄
    // if (file.exists() == false) {
    // if (file.mkdirs()) {// 此為壓縮檔目的地目錄,目錄不存在時,建立目錄
    // _job.writeLog("無符合條件的檔案可供壓縮");
    // }
    // }

    // file = new File(desPath, name + ".zip");// 此為壓縮檔
    // if (rarFileName.contains(file.getPath()) == false) {
    // rarFileName.add(file.getPath());// 記錄壓縮檔名
    // rarFiles.add(path.getPath());// 記錄欲壓縮之清案清單
    // } else {
    // // 記錄欲壓縮之清案清單
    // for (int x = 0; x < rarFileName.size(); x = x + 1) {
    // if (rarFileName.get(x).equalsIgnoreCase(file.getPath())) {
    // String files = rarFiles.get(x);
    // files += ";" + path.getPath();
    // rarFiles.set(x, files);
    // }
    // }
    // }
    // }
    // }
    // if (rarFileName.size() == 0) {
    // _job.writeLog("無符合條件的檔案可供壓縮");
    // return true;
    // }
    // for (int x = 0; x < rarFileName.size(); x = x + 1) {
    // String rarName = rarFileName.get(x);
    // String[] rarfiles = rarFiles.get(x).split(";");
    // File[] aryFiles = new File[rarfiles.length];
    // int i = 0;
    // try {
    // for (String aa : rarfiles) {
    // File f1 = new File(aa);
    // aryFiles[i] = f1;
    // i = i + 1;
    // }
    // _job.writeLog(String.format("開始壓縮{0},來源檔案{1}", rarName, rarFiles.get(x)));
    // if (CompressionUtil.compressFiles2Zip(aryFiles, rarName))// 壓縮檔案
    // {
    // for (File f1 : aryFiles) {
    // if (f1.delete()) {// 刪除壓縮來源檔
    // _job.writeLog("刪除失敗");
    // }
    // }
    // }
    // _job.writeLog(String.format("壓縮完成{0}", rarName));
    // } catch (Exception ex) {
    // _job.writeLog(String.format("壓縮失敗{0},原因:{1}", rarName, ex.getMessage()));
    // }
    // }
    // bRes = true;
    // } catch (Exception ex) {
    // _job.writeLog(ex.getMessage());
    // }
    // return bRes;
    // }

    public long getDate(int day) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendar.DATE, -1 * day);
        date = calendar.getTime();
        long ldate = Long.valueOf(format.format(date)).longValue();
        return ldate;
    }

    public static Boolean checkName(String folder) {
        Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(folder);
        return matcher.find();
    }

    private void DeleteArchiveFile() {
        // 壓縮檔目錄
        File targetDirFile = new File(targetDir);
        File[] paths = targetDirFile.listFiles();
        // 保留壓縮檔日期
        LocalDateTime reserveDate = LocalDateTime.now().minusDays(reserveDay);
        String begindate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(reserveDate);

        try {
            for (File path : paths) {
                String fname = getFileNameWithoutExtension(path.getName());
                // 壓縮檔日期大於保留壓縮檔日期，不處理
                if (fname.compareTo(begindate) >= 0) {
                    continue;
                }
//				path.delete(); // 只能刪除空資料夾
                FileUtils.forceDelete(path);
                _job.writeLog(String.format("Delete archive file %s complete", path.getName()));
            }
        } catch (IOException e) {
            _job.writeLog("Compress folder failed:" + e.getMessage());
//			e.printStackTrace();
        }
    }

    public void compressFolder(String sourceFolder, String destinationTarGzFile) throws IOException {
        _job.writeLog(String.format("Begin compress folder %s", sourceFolder));

        try (FileOutputStream fos = new FileOutputStream(destinationTarGzFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos);) {
            addFolderToTar("", sourceFolder, tos);
        } catch (IOException e) {
            _job.writeLog("Compress folder failed:" + e.getMessage());
//			e.printStackTrace();
        }

        _job.writeLog(String.format("%s archive to %s completed", sourceFolder, destinationTarGzFile));
    }

    private void addFolderToTar(String parent, String folder, TarArchiveOutputStream tos) throws IOException {
        File dir = new File(folder);
        String[] files = dir.list();

        if (files == null) {
            return;
        }

        for (String file : files) {
            String path = folder + "/" + file;
            File f = new File(path);
            if (f.isDirectory()) {
                addFolderToTar(parent + f.getName() + "/", path, tos);
            } else {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));) {
                    TarArchiveEntry tarEntry = new TarArchiveEntry(path);
                    tarEntry.setName(parent + f.getName());
                    tarEntry.setSize(f.length());
                    tos.putArchiveEntry(tarEntry);
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = bis.read(buffer)) != -1) {
                        tos.write(buffer, 0, count);
                    }
                } catch (Exception e) {
                    _job.writeLog("addFolder to tar failed:" + e.getMessage());
//					e.printStackTrace();
                }
                tos.closeArchiveEntry();
            }
        }
    }

    private String getFileNameWithoutExtension(String fileName) {
        int pos = fileName.indexOf(".");
        if (pos > 0 && pos < (fileName.length() - 1))
            fileName = fileName.substring(0, pos);
        return fileName;

    }

    private void DisplayUsage() {
        System.out.println("USAGE:");
        System.out.println(" ArchivingLogFile Options");
        System.out.println();
        System.out.println(" Options:");
        System.out.println(" /? Display this help message.");
        System.out.println(" /SourceDir Required.");
        System.out.println(" /TargetDir Required");
        System.out.println(" /ReserveDay Optional.");
        System.out.println(" /ArchiveDay Required.");
        System.out.println(" /CallBatchJob Optional, true or false, default true");
        System.out.println(" /BatchLogPath Optional");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println(" ArchivingLogFile /SourceDir:G:\\FEP10\\Log/TargetDir:G:\\FEPLog\\Backup\\ /ArchiveDay:2 /ReserveDay:7");
    }
}
