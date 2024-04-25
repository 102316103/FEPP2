package com.syscom.safeaa.utils;

import java.security.SecureRandom;
import java.util.Date;

/**
 * @author syscom
 * @date 2021/08/13
 */
public class RandomPassword {

    private static final char[] CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    /**
     * 生成密碼
     *
     * @param intPassLength 密碼長度
     * @return
     */
    public static String doGenerate(final int intPassLength) {
        int intTemp;
        String strSscode = "";
        SecureRandom rand = new SecureRandom();
        rand.setSeed((new Date()).getTime());
        for (int i = 0; i < intPassLength; i++) {
            intTemp = rand.nextInt(62);
            strSscode += Character.toString(CHARS[intTemp]);
        }
        return strSscode;
    }

    public static String doGenerate(final int minSscodeLength, final int maxSscodeLength) {
        SecureRandom rand = new SecureRandom();
        rand.setSeed((new Date()).getTime());
        int intPassLength = rand.nextInt(maxSscodeLength) % (maxSscodeLength - minSscodeLength + 1) + minSscodeLength;
        return doGenerate(intPassLength);
    }

}
