package com.syscom.fep.common.util;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 獲取AP日誌檔案
 *
 * @author Richard
 */
public class GetApLogFilesUtil {
    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();
    private static final String EXTENSION_TAR_GZ = ".tar.gz";

    private GetApLogFilesUtil() {}

    public static enum ApLogType {
        aplog, waslog;
    }

    /**
     * 獲取AP日誌tar檔, 並轉成byte[]
     *
     * @param logType
     * @param logDate
     * @param fepLogPath
     * @param fepWasLogPath
     * @param fepLogArchivesPath
     * @return
     * @throws Exception
     */
    public static byte[] getApLogFiles(ApLogType logType, String logDate, String fepLogPath, String fepWasLogPath, String fepLogArchivesPath) throws Exception {
        try {
            if (logType == ApLogType.aplog) {
                // 資料夾為/fep/logs
                File directory = new File(CleanPathUtil.cleanString(fepLogPath), CleanPathUtil.cleanString(logDate));
                // 如果/fep/logs/yyyy-MM-dd資料夾不存在, 就取/fep/logs/archives/yyyy-MM-dd.tar.gz檔案
                if (!directory.exists()) {
                    logger.warn("Directory not exist, directory = [", directory.getAbsolutePath(), "]");
                    // 取到/fep/logs/archives/yyyy-MM-dd.tar.gz檔案
                    File file = new File(CleanPathUtil.cleanString(fepLogArchivesPath), CleanPathUtil.cleanString(StringUtils.join(logDate, EXTENSION_TAR_GZ)));
                    return readCompressedFile(file);
                } else {
                    // 將/fep/logs/yyyy-MM-dd資料夾下所有的檔案壓縮成tar檔
                    return compressDirectory(directory);
                }
            } else if (logType == ApLogType.waslog) {
                // 資料夾為/fep/waslogs
                File directory = new File(CleanPathUtil.cleanString(fepWasLogPath), CleanPathUtil.cleanString(logDate));
                // 如果/fep/waslogs/yyyy-MM-dd資料夾不存在, 就取/fep/waslogs/yyyy-MM-dd-waslog.tar.gz檔案
                if (!directory.exists()) {
                    logger.warn("Directory not exist, directory = [", directory.getAbsolutePath(), "]");
                    // 取到/fep/waslogs/yyyy-MM-dd-waslog.tar.gz檔案
                    File file = new File(CleanPathUtil.cleanString(fepWasLogPath), CleanPathUtil.cleanString(StringUtils.join(logDate, "-waslog", EXTENSION_TAR_GZ)));
                    return readCompressedFile(file);
                } else {
                    // 將/fep/waslogs/yyyy-MM-dd資料夾下所有的檔案壓縮成tar檔
                    return compressDirectory(directory);
                }
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, "Get AP Log Files with exception occur, logType:", logType, ",logDate:", logDate, ",fepLogPath:", fepLogPath, ",fepWasLogPath:", fepWasLogPath, ",fepLogArchivesPath:", fepLogArchivesPath);
            throw e;
        }
        return null;
    }

    /**
     * 將指定的資料夾和資料夾下所有的子資料夾或者檔案, 存入壓縮檔中
     *
     * @param directory
     * @return
     * @throws Exception
     */
    private static byte[] compressDirectory(File directory) throws Exception {
        if (directory.isDirectory()) {
            logger.info("Start to Compress, Directory = [", directory.getAbsolutePath(), "]");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (TarArchiveOutputStream tos = new TarArchiveOutputStream(new GzipCompressorOutputStream(bos))) {
                compressDirectory(tos, directory.getParentFile().getAbsolutePath(), directory);
                logger.info("Compress Successful, Directory = [", directory.getAbsolutePath(), "]");
            } catch (IOException e) {
                throw ExceptionUtil.createException(e, "Compress Failed, Directory = [", directory.getAbsolutePath(), "]");
            }
            bos.close(); // 這裡一定要close
            return bos.toByteArray();
        } else {
            throw ExceptionUtil.createException("[", directory.getAbsolutePath(), "] is not directory!!!");
        }
    }

    /**
     * 將指定的資料夾和資料夾下所有的子資料夾或者檔案, 存入壓縮檔中
     *
     * @param tos
     * @param parentPath
     * @param directory
     * @throws IOException
     */
    private static void compressDirectory(TarArchiveOutputStream tos, String parentPath, File directory) throws IOException {
        File[] children = directory.listFiles();
        if (children == null) {
            logger.warn("[", directory.getAbsolutePath(), "] is empty!!!");
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                compressDirectory(tos, parentPath, child);
            } else {
                try {
                    TarArchiveEntry tarEntry = new TarArchiveEntry(child.getAbsolutePath());
                    tarEntry.setName(StringUtils.substring(child.getAbsolutePath(), parentPath.length()));
                    tarEntry.setSize(child.length());
                    tos.putArchiveEntry(tarEntry);
                    IOUtils.copy(child, tos);
                } catch (Exception e) {
                    logger.exceptionMsg(e, "Tar failed, directory = [", directory.getAbsolutePath(), "], file = [", child.getAbsolutePath(), "]");
                } finally {
                    tos.closeArchiveEntry();
                }
            }
        }
    }

    /**
     * 讀取檔案並存入byte[]
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static byte[] readCompressedFile(File file) throws Exception {
        logger.info("Start to Read Compressed File, file = [", file.getAbsolutePath(), "]");
        try {
            byte[] bytes = FileUtils.readFileToByteArray(file);
            logger.info("Read Compressed File Successful, file = [", file.getAbsolutePath(), "], length = [", bytes.length, "]");
            return bytes;
        } catch (IOException e) {
            throw ExceptionUtil.createException(e, "Read Compressed File Failed, file = [", file.getAbsolutePath(), "]");
        }
    }
}
