package com.syscom.fep.frmcommon.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    public static final String EXP_SQL_INJECTION = "(\\s*([\\0\\b\\'\\\"\\n\\r\\t\\%\\_\\\\]*\\s*(((select\\s*.+\\s*from\\s*.+)|(insert\\s*.+\\s*into\\s*.+)|(update\\s*.+\\s*set\\s*.+)|(delete\\s*.+\\s*from\\s*.+)|(drop\\s*.+)|(truncate\\s*.+)|(alter\\s*.+)|(exec\\s*.+)|(\\s*(all|any|not|and|between|in|like|or|some|contains|containsall|containskey)\\s*.+[\\=\\>\\<=\\!\\~]+.+)|(let\\s+.+[\\=]\\s*.*)|(begin\\s*.*\\s*end)|(\\s*[\\/\\*]+\\s*.*\\s*[\\*\\/]+)|(\\s*(\\-\\-)\\s*.*\\s+)|(\\s*(contains|containsall|containskey)\\s+.*)))(\\s*[\\;]\\s*)*)+)";

    private RegexUtil() {
    }

    public static boolean find(String regex, CharSequence input) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        return m.find();
    }
}
