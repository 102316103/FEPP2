package com.syscom.fep.web.build;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class RemoveVersion {
    private static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();

    @Test
    public void remove() {
        String base = RemoveVersion.class.getClassLoader().getResource(".").getPath();
        File target = new File(CleanPathUtil.cleanString(base)).getParentFile();
        File lib = new File(target, CleanPathUtil.cleanString("/fep-web/WEB-INF/lib"));
        FileFilter filefilter = new RegexFileFilter("^fep-(.*)-1.0.0.jar", IOCase.INSENSITIVE);
        File[] files = lib.listFiles(filefilter);
        if (ArrayUtils.isEmpty(files)) UnitTestLogger.error("fep jar files empty!!!");
        if (files != null) {
            for (File sourceFile : files) {
                File targetFile = new File(CleanPathUtil.cleanString(StringUtils.replace(sourceFile.getAbsolutePath(), "-1.0.0", StringUtils.EMPTY)));
                try {
                    Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    UnitTestLogger.info(sourceFile.getName(), " rename to ", targetFile.getName(), " successful!!!");
                } catch (IOException e) {
                    UnitTestLogger.error(e, sourceFile.getName(), " rename to ", targetFile.getName(), " failed!!!");
                }
            }
        }
    }
}
