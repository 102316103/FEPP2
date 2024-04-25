package com.syscom.fep.frmcommon.util;

public class CommandLineUtil {

    private CommandLineUtil() {
    }

    public static boolean existArg(String[] args, String found) {
        try {
            for (int i = 0; i < args.length; i++) {
                if (found.equals(args[i])) {
                    return true;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw e;
        }
        return false;
    }

    public static String findArg(String[] args, String found) {
        try {
            for (int i = 0; i < args.length; i++) {
                if (found.equals(args[i])) {
                    if (i + 1 < args.length) {
                        return args[i + 1];
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw e;
        }
        return null;
    }
}
