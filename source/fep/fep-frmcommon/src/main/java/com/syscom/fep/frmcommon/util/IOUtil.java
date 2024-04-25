package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;

public class IOUtil {
    private static LogHelper logger = new LogHelper();

    private IOUtil() {
    }

    public static InputStream openInputStream(String path, boolean... log) throws Exception {
        boolean logEnable = ArrayUtils.isNotEmpty(log) ? log[0] : true;
        InputStream in = null;
        if (StringUtils.isNotBlank(path)) {
            ClassPathResource classPathResource = new ClassPathResource(path);
            if (logEnable) logger.info("try to load in classpath = [", classPathResource.getPath(), "]...");
            try {
                in = classPathResource.getInputStream();
                if (logEnable) logger.info("load in classpath = [", classPathResource.getPath(), "] successful");
            } catch (Exception e) {
                if (logEnable) logger.warn("load in classpath = [", classPathResource.getPath(), "] failed with exception occur, ", e.getMessage());
            }
            if (in == null) {
                File file = new File(CleanPathUtil.cleanString(path));
                if (logEnable) logger.info("try to load out file system = [", file.getAbsolutePath(), "]...");
                try {
                    in = FileUtils.openInputStream(file);
                    if (logEnable) logger.info("load out file system = [", file.getAbsolutePath(), "] successful");
                } catch (Exception e) {
                    if (logEnable) logger.error(e, "load out file system = [", file.getAbsolutePath(), "] failed with exception occur, ", e.getMessage());
                    throw e;
                }
            }
        }
        if (in == null) {
            if (logEnable) logger.error("cannot load path = [", path, "]");
            throw ExceptionUtil.createException("cannot load path = [", path, "]");
        }
        return in;
    }
}
