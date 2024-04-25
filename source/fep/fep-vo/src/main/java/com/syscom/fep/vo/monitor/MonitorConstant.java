package com.syscom.fep.vo.monitor;

public interface MonitorConstant {
    public static final String SERVICE_NAME_SYSTEM = "SYSTEM";
    public static final String SERVICE_NAME_DB = "DB";
    public static final String SERVICE_NAME_MQ = "MQ";
    public static final String SERVICE_NAME_NET_SERVER = "NET-SERVER";
    public static final String SERVICE_NAME_NET_CLIENT = "NET-CLIENT";
    public static final String SERVICE_NAME_PROCESS = "PROCESS";
    public static final String DB_NAME_FEP = "FEPDB";
    public static final String DB_NAME_EMS = "EMSDB";
    public static final String DB_NAME_ENC = "ENCDB";
    public static final String DB_NAME_ENCLOG = "ENCLOGDB";
    public static final String DB_NAME_FEPHIS = "FEPHIS";
    public static final String STATUS_UP = "UP";
    public static final String JSON_FIELD_COMPONENTS = "components";
    public static final String JSON_FIELD_STATUS = "status";
    public static final String JSON_FIELD_HOSTNAME = "hostname";
    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_IP = "ip";
    public static final String JSON_FIELD_USED = "used";
    public static final String JSON_FIELD_TOTAL = "total";
    public static final String JSON_FIELD_DISK = "disk";
    public static final short SYSCONF_VALUE_CMN = 9;
    public static final String SYSCONF_NAME_STOPNOTIFICATION = "StopNotification";
    public static final String SYSCONF_NAME_ENABLEAUTORESTART = "EnableAutoRestart";
    public static final String STATUS_NORMAL = "正常";
    public static final String STATUS_STOPPED = "停止";
    public static final String STATUS_UNKNOWN = "未知";
    public static final String NET_CLIENT_STATE_DISCONNECT = "DisConnect";
    public static final String NET_CLIENT_STATE_CONNECT = "Connect";
    public static final String NET_CLIENT_STATE_UNKNOWN = "Unknown";
    public static final String SUIP_RESP_FIELD_ID = "ID=";
    public static final String SUIP_RESP_FIELD_IPADDR = "IpADDR=";
    public static final String SUIP_RESP_FIELD_PORT = "port=";
    public static final String SUIP_RESP_FIELD_STATUS = "Status=";
    public static final String MONITOR_TYPE_SUIP_NET_CLIENT = "SUIP_NET_CLIENT";
}

