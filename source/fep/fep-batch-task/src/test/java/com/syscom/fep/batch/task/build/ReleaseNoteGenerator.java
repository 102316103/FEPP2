package com.syscom.fep.batch.task.build;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReleaseNoteGenerator {
    private static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();
    private static final List<String> excludes = Arrays.asList();

    @Test
    public void generate() {
        String base = AssemblyPropFileGenerator.class.getClassLoader().getResource(".").getPath();
        File target = new File(CleanPathUtil.cleanString(base)).getParentFile();
        File taskDirFile = new File(target, CleanPathUtil.cleanString("classes/com/syscom/fep/batch/task/"));
        File releaseNoteDirFile = new File(target.getParentFile().getParentFile().getParent(), "fep-release-note");
        List<File> list = listFiles(taskDirFile);
        for (File file : list) {
            String taskClassName = FilenameUtils.getBaseName(file.getPath());
            File releaseNoteFile = new File(releaseNoteDirFile, CleanPathUtil.cleanString(StringUtils.join("fep-batch-task-", taskClassName, ".txt")));
            if (releaseNoteFile.isFile() && releaseNoteFile.exists()) continue;
            if (releaseNoteFile.isDirectory()) releaseNoteFile.delete();
            try {
                FileUtils.write(releaseNoteFile,
                        StringUtils.join("# FEP Batch Task ", taskClassName, " Release Note", "\r\n"),
                        StandardCharsets.UTF_8, false);
            } catch (IOException e) {
                UnitTestLogger.warn(e, e.getMessage());
            }
        }
    }

    private List<File> listFiles(File file) {
        List<File> result = new ArrayList<>();
        File[] files = file.listFiles();
        if (ArrayUtils.isNotEmpty(files)) {
            for (File child : files) {
                if (excludes.stream().filter(t -> child.getName().startsWith(t)).findFirst().orElse(null) != null ||
                        child.getName().indexOf("$") > 0) {
                    continue;
                }
                result.addAll(listFiles(child));
            }
            return result;
        } else {
            return Arrays.asList(file);
        }
    }
}
