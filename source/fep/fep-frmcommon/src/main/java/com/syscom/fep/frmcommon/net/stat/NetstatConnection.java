package com.syscom.fep.frmcommon.net.stat;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang.StringUtils;
import oshi.util.ParseUtil;

public class NetstatConnection implements NetstatConstant {
    private LogHelper logger = new LogHelper();
    private String protocol;
    private int receiveQueue;
    private int sendQueue;
    private String localAddress;
    private String foreignAddress;
    private NetstatTcpState state;
    private int pid;
    private String programName;

    public NetstatConnection(String[] fields) {
        try {
            this.protocol = fields[FIELD_PROTOCOL];
            this.receiveQueue = ParseUtil.parseIntOrDefault(fields[FIELD_RECEIVEQUEUE], 0);
            this.sendQueue = ParseUtil.parseIntOrDefault(fields[FIELD_SENDQUEUE], 0);
            this.localAddress = fields[FIELD_LOCALADDRESS];
            this.foreignAddress = fields[FIELD_FOREIGNADDRESS];
            this.state = StringUtils.isBlank(fields[FIELD_STATE]) ? NetstatTcpState.NONE : NetstatTcpState.valueOf(fields[FIELD_STATE]);
            this.pid = Integer.parseInt(fields[FIELD_PID_PROGRAM].substring(0, fields[FIELD_PID_PROGRAM].indexOf("/")));
            this.programName = fields[FIELD_PID_PROGRAM].substring(fields[FIELD_PID_PROGRAM].indexOf("/") + 1);
            if (this.programName.startsWith("./")) {
                this.programName = this.programName.substring(2);
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, "Parse failed, ", e.getMessage());
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public int getReceiveQueue() {
        return receiveQueue;
    }

    public int getSendQueue() {
        return sendQueue;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public String getForeignAddress() {
        return foreignAddress;
    }

    public NetstatTcpState getState() {
        return state;
    }

    public int getPid() {
        return pid;
    }

    public String getProgramName() {
        return programName;
    }

    @Override
    public String toString() {
        return "NetstatConnection{" +
                "logger=" + logger +
                ", protocol='" + protocol + '\'' +
                ", receiveQueue=" + receiveQueue +
                ", sendQueue=" + sendQueue +
                ", localAddress='" + localAddress + '\'' +
                ", foreignAddress='" + foreignAddress + '\'' +
                ", state=" + state +
                ", pid=" + pid +
                ", programName='" + programName + '\'' +
                '}';
    }
}
