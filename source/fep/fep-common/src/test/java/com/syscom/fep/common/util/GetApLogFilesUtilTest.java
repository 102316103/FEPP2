package com.syscom.fep.common.util;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import com.syscom.fep.common.util.GetApLogFilesUtil.ApLogType;

import java.io.*;

public class GetApLogFilesUtilTest {
    @Test
    public void test() throws Exception {
        String logDate = "2023-10-07";
        byte[] bytes = GetApLogFilesUtil.getApLogFiles(ApLogType.aplog, logDate, "/fep/logs", "/fep/waslogs", "/fep/logs/archives");
        if (bytes != null) {
            FileUtils.writeByteArrayToFile(new File("/fep/logs/backup", logDate + ".tar.gz"), bytes);
        } else {
            LogHelperFactory.getUnitTestLogger().error("length is 0");
        }
    }

    @Test
    public void test2() throws Exception {
        String logDate = "2023-10-30";
        byte[] bytes = GetApLogFilesUtil.getApLogFiles(ApLogType.aplog, logDate, "/fep/logs", "/fep/waslogs", "/fep/logs/archives");
        if (bytes != null) {
            FileUtils.writeByteArrayToFile(new File("/fep/logs/backup", logDate + ".tar.gz"), bytes);
        } else {
            LogHelperFactory.getUnitTestLogger().error("length is 0");
        }
    }

    @Test
    public void test3() {
        try {
            String logDate = "2023-10-32";
            byte[] bytes = GetApLogFilesUtil.getApLogFiles(ApLogType.aplog, logDate, "/fep/logs", "/fep/waslogs", "/fep/logs/archives");
            if (bytes != null) {
                FileUtils.writeByteArrayToFile(new File("/fep/logs/backup", logDate + ".tar.gz"), bytes);
            } else {
                LogHelperFactory.getUnitTestLogger().error("length is 0");
            }
        } catch (Exception e) {
            LogHelperFactory.getUnitTestLogger().error(e);
        }
    }

    @Test
    public void test4() throws Exception {
        String logDate = "2023-10-07";
        byte[] bytes = GetApLogFilesUtil.getApLogFiles(ApLogType.waslog, logDate, "/fep/logs", "/fep/waslogs", "/fep/waslogs/archives");
        if (bytes != null) {
            FileUtils.writeByteArrayToFile(new File("/fep/waslogs", logDate + "-waslog.tar.gz"), bytes);
        } else {
            LogHelperFactory.getUnitTestLogger().error("length is 0");
        }
    }

    @Test
    public void test5() throws Exception {
        String logDate = "2023-10-30";
        byte[] bytes = GetApLogFilesUtil.getApLogFiles(ApLogType.waslog, logDate, "/fep/logs", "/fep/waslogs", "/fep/waslogs/archives");
        if (bytes != null) {
            FileUtils.writeByteArrayToFile(new File("/fep/waslogs", logDate + "-waslog.tar.gz"), bytes);
        } else {
            LogHelperFactory.getUnitTestLogger().error("length is 0");
        }
    }

    @Test
    public void test6() {
        try {
            String logDate = "2023-10-32";
            byte[] bytes = GetApLogFilesUtil.getApLogFiles(ApLogType.waslog, logDate, "/fep/logs", "/fep/waslogs", "/fep/waslogs/archives");
            if (bytes != null) {
                FileUtils.writeByteArrayToFile(new File("/fep/waslogs", logDate + "-waslog.tar.gz"), bytes);
            } else {
                LogHelperFactory.getUnitTestLogger().error("length is 0");
            }
        } catch (Exception e) {
            LogHelperFactory.getUnitTestLogger().error(e);
        }
    }

    @Test
    public void compressFolder() throws IOException {
        String sourceFolder = "/fep/logs/2023-10-07";
        String destinationTarGzFile = "/fep/logs/archives/2023-10-07.tar.gz";
        LogHelperFactory.getUnitTestLogger().info(String.format("Begin compress folder %s", sourceFolder));
//        try (FileOutputStream fos = new FileOutputStream(destinationTarGzFile);
//             BufferedOutputStream bos = new BufferedOutputStream(fos);
//             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
//             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos);) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tos = new TarArchiveOutputStream(new GzipCompressorOutputStream(bos))) {
            addFolderToTar("", sourceFolder, tos);
        }
        bos.close();
        byte[] bytes = bos.toByteArray();
        FileUtils.writeByteArrayToFile(new File(destinationTarGzFile), bytes);
        LogHelperFactory.getUnitTestLogger().info(String.format("%s archive to %s completed", sourceFolder, destinationTarGzFile));
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

                    System.out.println("===========================================");
                    System.out.println("path = " + path);
                    System.out.println("name = " + parent + f.getName());
                    System.out.println("===========================================");

                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = bis.read(buffer)) != -1) {
                        tos.write(buffer, 0, count);
                    }
                }
                tos.closeArchiveEntry();
            }
        }
    }
}
