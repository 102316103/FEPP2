package com.syscom.fep.frmcommon.net.ftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.file.remote.ClientCallback;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 用於處理一般的sftp
 *
 * @author Richard
 */
public class SftpClient extends FtpAdapter {
    private LogHelper logger = new LogHelper();

    private SftpRemoteFileTemplate template;

    /**
     * 構建函式
     *
     * @param sftpProperties
     * @param beanFactory
     */
    public SftpClient(FtpProperties sftpProperties, BeanFactory beanFactory) {
        super(sftpProperties);
        // DefaultFtpSessionFactory
        DefaultSftpSessionFactory sftpSessionFactory = new DefaultSftpSessionFactory();
        sftpSessionFactory.setChannelConnectTimeout(Duration.of(sftpProperties.getConnectTimeout(), ChronoUnit.MILLIS));
        sftpSessionFactory.setTimeout(sftpProperties.getConnectTimeout());
        sftpSessionFactory.setHost(sftpProperties.getHost());
        sftpSessionFactory.setPort(sftpProperties.getPort());
        sftpSessionFactory.setUser(sftpProperties.getUsername());
        if (StringUtils.isNotBlank(sftpProperties.getSftpPrivateKey())) {
            sftpSessionFactory.setPrivateKey(new ClassPathResource(sftpProperties.getSftpPrivateKey()));
            sftpSessionFactory.setPrivateKeyPassphrase(sftpProperties.getSftpPrivateKeyPassphrase());
        } else {
            sftpSessionFactory.setPassword(sftpProperties.getPassword());
        }
        sftpSessionFactory.setAllowUnknownKeys(true);
        sftpSessionFactory.setSessionConfig(new Properties() {
            private static final long serialVersionUID = 1L;

            {
                put("StrictHostKeyChecking", "no");
            }
        });
        // CachingSessionFactory
        CachingSessionFactory<LsEntry> cachingSessionFactory = new CachingSessionFactory<>(sftpSessionFactory, sftpProperties.getPoolSize());
        cachingSessionFactory.setSessionWaitTimeout(sftpProperties.getSessionWaitTimeout());
        cachingSessionFactory.setPoolSize(ftpProperties.getPoolSize());
        // FtpRemoteFileTemplate
        template = new SftpRemoteFileTemplate(cachingSessionFactory);
        template.setCharset(sftpProperties.getControlEncoding());
        template.setBeanFactory(beanFactory);
        logger.info("SftpClient is ready, ", sftpProperties.toString());
    }

    /**
     * 創建遠程目錄
     *
     * @param basePath 基礎目錄
     * @param folder   待創建目錄名稱
     * @return
     */
    @Override
    public boolean createRemotePath(String basePath, String folder) {
        boolean exists = this.isRemotePathExist(basePath, folder);
        if (exists) {
            logger.warn("No need create remote path cause remote path already exist, basePath = [", basePath, "], folder = [", folder, "]");
            return true;
        }
        try {
            final String remotePath = this.getRemotePath(basePath, folder);
            return template.executeWithClient(new ClientCallback<ChannelSftp, Boolean>() {
                @Override
                public Boolean doWithClient(ChannelSftp sftpClient) {
                    FtpAction ftpAction = FtpAction.NO_ACTION;
                    String[] directories = remotePath.split("/");
                    if (ArrayUtils.isNotEmpty(directories)) {
                        StringBuilder sb = new StringBuilder();
                        try {
                            for (String directory : directories) {
                                if (StringUtils.isBlank(directory)) {
                                    continue;
                                }
                                sb.append("/").append(directory);
                                try {
                                    ftpAction = FtpAction.CHANGE_DIRECTORY;
                                    sftpClient.cd(directory);
                                } catch (SftpException e) {
                                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                                        ftpAction = FtpAction.MAKE_DIRECTORY;
                                        sftpClient.mkdir(directory);
                                        ftpAction = FtpAction.CHANGE_DIRECTORY;
                                        sftpClient.cd(directory);
                                    } else {
                                        throw e;
                                    }
                                }
                            }
                        } catch (SftpException e) {
                            logger.error(e, ftpAction.getDescription(), " failed, directory = [", sb.toString(), "]");
                            return false;
                        } finally {
                            // 這裡要退回到主目錄下, 否則連續使用此方法會有問題
                            try {
                                sftpClient.cd("/");
                            } catch (SftpException e) {
                                logger.warn("Back to main directory failed, error message = [", e.getMessage(), "]");
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            logger.error(e, "Create Remote path failed!! basePath = [", basePath, "], folder = [", folder, "]");
            return false;
        }
    }

    /**
     * 下載檔案
     *
     * @param local  本地檔案路徑
     * @param remote 遠程檔案路徑
     * @return
     */
    @Override
    public boolean download(String local, String remote) {
        boolean exists = this.isRemotePathExist(remote, null);
        if (!exists) {
            logger.warn("Cannot download cause file not exist, remote = [", remote, "]");
            return false;
        }
        File file = new File(CleanPathUtil.cleanString(local));
        try {
            FileUtils.forceMkdirParent(file);
        } catch (IOException e) {
            logger.error(e, e.getMessage());
            return false;
        }
        try {
            String remotePath = this.getRemotePath(remote, null);
            return template.get(remotePath, in -> {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    IOUtils.copy(in, fos);
                } catch (Exception e) {
                    logger.error(e, "Download remote file successful but copy to local failed!!");
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error(e, "Download failed!! remote = [", remote, "]");
            return false;
        }
    }

    /**
     * 獲取檔案列表
     *
     * @param remote                  遠程目錄路徑
     * @param mask                    用於模糊檔案查詢, 例如GibCobolConverter*, 就會找出檔案名含有GibCobolConverter字符串的所有檔案, 不傳入此參數則取所有的檔案名包括目錄名
     * @param isExcludeDirectory可選參數, 是否不包括檔案夾
     * @return
     */
    @Override
    public List<String> getFileList(String remote, String mask, boolean... isExcludeDirectory) {
        boolean exists = this.isRemotePathExist(remote, null);
        if (!exists) {
            logger.warn("Cannot get file list cause remote path not exist, remote = [", remote, "]");
            return null;
        }
        try {
            String remotePath = this.getRemotePath(remote, null);
            LsEntry[] lsEntries = template.list(remotePath);
            if (ArrayUtils.isNotEmpty(lsEntries)) {
                List<String> list = new ArrayList<>();
                if (StringUtils.isNotBlank(mask)) {
                    int index = mask.indexOf("*");
                    if (index == -1) {
                        mask = StringUtils.join(mask, "*");
                        index = mask.indexOf("*");
                    }
                    String mask_ = mask.substring(0, index);
                    for (LsEntry lsEntry : lsEntries) {
                        if (!"*.*".equals(mask.trim()) && lsEntry.getFilename().length() >= mask_.length()) {
                            // 返回的path中有一些是一個或者多個.
                            // 要忽略掉
                            if (lsEntry.getFilename().matches("(\\.)+")) {
                                logger.warn("ignore invalid lsEntry = [", lsEntry.getFilename(), "]");
                                continue;
                            }
                            if (lsEntry.getFilename().substring(0, mask_.length()).equals(mask_)) {
                                if (ArrayUtils.isNotEmpty(isExcludeDirectory) && isExcludeDirectory[0] && lsEntry.getAttrs().isDir()) {
                                    continue;
                                }
                                list.add(lsEntry.getFilename());
                            }
                        }
                    }
                } else {
                    for (LsEntry lsEntry : lsEntries) {
                        list.add(lsEntry.getFilename());
                    }
                }
                return list;
            }
        } catch (Exception e) {
            logger.error(e, "Get file list file failed!! remote = [", remote, "], mask = [", mask, "]");
        }
        return null;
    }

    /**
     * 檢查遠端目錄下的目錄是否存在
     *
     * @param basePath 基礎目錄
     * @param folder   目錄名稱
     * @return
     */
    @Override
    public boolean isRemotePathExist(String basePath, String folder) {
        try {
            String remotePath = this.getRemotePath(basePath, folder);
            return template.exists(remotePath);
        } catch (Exception e) {
            logger.error(e, "Check path exist or not failed!! basePath = [", basePath, "], folder = [", folder, "]");
            return false;
        }
    }

    /**
     * 上傳檔案
     *
     * @param local                   待上傳本地檔案
     * @param remote                  上傳後遠程檔案
     * @param isRemoteExcludeFilename remote中是否不包含檔案名
     * @return
     */
    @Override
    public boolean upload(String local, String remote, boolean... isRemoteExcludeFilename) {
        boolean exists = this.isRemotePathExist(remote, null);
        if (exists) {
            logger.warn("Remote file exist and will be overwrite, remote = [", remote, "]");
        }
        File file = new File(CleanPathUtil.cleanString(local));
        if (!file.exists()) {
            logger.error("Cannot be upload cause local file not exist, local = [", local, "]");
            return false;
        }
        String remotePath = this.getRemotePath(remote, null);
        if (ArrayUtils.isEmpty(isRemoteExcludeFilename) || !isRemoteExcludeFilename[0]) {
            int index = remotePath.lastIndexOf("/");
            final String filename = remotePath.substring(index + 1, remotePath.length());
            remotePath = remotePath.substring(0, index);
            template.setFileNameGenerator(message -> {
                return filename;
            });
        }
        template.setRemoteDirectoryExpression(new LiteralExpression(remotePath));
        template.setAutoCreateDirectory(true);
        template.afterPropertiesSet();
        Message<File> message = MessageBuilder.withPayload(file).build();
        try {
            template.send(message, FileExistsMode.REPLACE);
        } catch (Exception e) {
            logger.error(e, "Upload failed!! local = [", local, "], remote = [", remote, "]");
            return false;
        } finally {
            template.setFileNameGenerator(null);
            template.setRemoteDirectoryExpression(new LiteralExpression("/"));
            template.setAutoCreateDirectory(false);
            template.afterPropertiesSet();
        }
        return true;
    }

    @Override
    protected String getRemotePath(String basePath, String folder) {
        String remotePath = super.getRemotePath(basePath, folder);
        if (StringUtils.isNotBlank(ftpProperties.getSftpRootDirectory())) {
            remotePath = StringUtils.join("/", ftpProperties.getSftpRootDirectory(), remotePath);
        }
        return remotePath;
    }

    /**
     * 刪除遠程檔案
     *
     * @param remote 遠程檔案所在路徑
     * @return
     */
    @Override
    public boolean delete(String remote) {
        boolean exists = this.isRemotePathExist(remote, null);
        if (!exists) {
            logger.warn("Cannot delete cause file not exist, remote = [", remote, "]");
            return false;
        }
        try {
            String remotePath = this.getRemotePath(remote, null);
            return template.remove(remotePath);
        } catch (Exception e) {
            logger.error(e, "Delete failed!! remote = [", remote, "]");
            return false;
        }
    }

    /**
     * 斷開連線
     *
     * @return
     */
    @Override
    public boolean disconnect() {
        return template.executeWithClient((ChannelSftp sftpClient) -> {
            logger.info("try to disconnect from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "]...");
            if (sftpClient.isConnected()) {
                sftpClient.disconnect();
            } else {
                logger.warn("Already disconnected from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "] successful");
            }
            try {
                Session session = sftpClient.getSession();
                if (session.isConnected()) {
                    session.disconnect();
                    logger.info("Disconnect from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "] successful");
                } else {
                    logger.warn("Already disconnected from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "] successful");
                }
                return true;
            } catch (JSchException e) {
                logger.error(e, "Disconnect failed from [", ftpProperties.getHost(), ":", ftpProperties.getPort(), "]");
            }
            return false;
        });
    }
}
