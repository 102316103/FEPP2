package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.StringUtils;

public class PathUtil {
    private PathUtil() {
    }

    /**
     * 修正Stored Relative Path Traversal
     *
     * @param path
     * @return
     */
    public static String removeTraversal(String path) {
        if (StringUtils.isNotBlank(path)) {
            return path.replace("..", StringUtils.EMPTY)
                    .replace("/", StringUtils.EMPTY)
                    .replace("\\", StringUtils.EMPTY)
                    .replace("'", StringUtils.EMPTY);
        }
        return path;
    }
}
