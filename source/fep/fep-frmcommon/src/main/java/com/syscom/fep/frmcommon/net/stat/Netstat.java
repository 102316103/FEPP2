package com.syscom.fep.frmcommon.net.stat;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

import java.util.ArrayList;
import java.util.List;

public class Netstat {
    private Netstat() {
    }

    public static List<NetstatConnection> queryTcpNetstat(String processName, int port) {
        StringBuilder cmd = new StringBuilder();
        if (StringUtils.isNotBlank(processName)) {
            cmd.append(" | grep ").append(processName);
        }
        if (port > 0) {
            cmd.append(" | grep ").append(port);
        }
        return queryTcpNetstat(cmd.toString());
    }

    public static List<NetstatConnection> queryTcpNetstat(String args) {
        List<NetstatConnection> connections = new ArrayList<>();
        List<String> activeConnections = ExecutingCommand.runNative(new String[] {"netstat", "-antp", args});
        String[] fields = null;
        for (String activeConnection : activeConnections) {
            if (activeConnection.startsWith("tcp")) {
                fields = ParseUtil.whitespaces.split(activeConnection);
                if (ArrayUtils.isNotEmpty(fields)) {
                    connections.add(new NetstatConnection(fields));
                }
            }
        }
        return connections;
    }
}
