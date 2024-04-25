package com.syscom.fep.service.monitor.svr;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.monitor.MonitorDataCollector;
import com.syscom.fep.common.monitor.MonitorDataDisk;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.common.notify.NotifyHelperTemplateId;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.parse.GsonDateParser;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.deslog.configuration.DataSourceDeslogConstant;
import com.syscom.fep.mybatis.ems.configuration.DataSourceEmsConstant;
import com.syscom.fep.mybatis.enc.configuration.DataSourceEncConstant;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.his.configuration.DataSourceHisConstant;
import com.syscom.fep.mybatis.mapper.SmsMapper;
import com.syscom.fep.mybatis.model.Sms;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.service.monitor.controller.MonitorNetworkController;
import com.syscom.fep.service.monitor.job.MonitorSchedulerJobConfig;
import com.syscom.fep.service.monitor.vo.*;
import com.syscom.fep.vo.monitor.ClientNetworkStatus;
import com.syscom.fep.vo.monitor.IBMMQStatus;
import com.syscom.fep.vo.monitor.MonitorConstant;
import com.syscom.fep.vo.monitor.ServerNetworkStatus;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MonitorSchedulerService extends FEPBase implements MonitorConstant {
    private final LogHelper TRACELogger = LogHelperFactory.getTraceLogger();
    private final ExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory("AppMon"));
    @Autowired
    private FEPConfig fepConfig;
    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private SysconfExtMapper sysconfExtMapper;
    @Autowired
    private MonitorSchedulerJobConfig monitorSchedulerJobConfig;
    // @Autowired
    // private MailSender mailSender;
    @Autowired
    private NotifyHelper notifyHelper;
    private String APPNAME, HOSTIP, HOSTNAME, PROTOCOL, HOSTPORT, HOSTCONTEXTPATH, URL;
    private double ruleRiskRate;
    private int notifyCount;
    private final Map<Integer, Integer> interval = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, Long>> alert = new ConcurrentHashMap<>();
    private HttpClient httpClient;
    private final Map<String, Date> processNameToStopTimeMap = new ConcurrentHashMap<>();
    private final SuipHsmMonitorData suipHsmMonitorData = new SuipHsmMonitorData();

    @PostConstruct
    public void initialization() {
        APPNAME = fepConfig.getApplicationName();
        HOSTIP = this.monitorSchedulerJobConfig.getSystem().getHostip();
        HOSTNAME = this.monitorSchedulerJobConfig.getSystem().getHostname();
        PROTOCOL = this.monitorSchedulerJobConfig.getSystem().getProtocol();
        HOSTPORT = this.monitorSchedulerJobConfig.getSystem().getPort();
        HOSTCONTEXTPATH = this.monitorSchedulerJobConfig.getSystem().getContextPath();
        URL = StringUtils.join(PROTOCOL, "://", HOSTIP, ":", HOSTPORT, HOSTCONTEXTPATH);
        httpClient = new HttpClient(this.monitorSchedulerJobConfig.isRecordHttpLog());
        ruleRiskRate = this.monitorSchedulerJobConfig.getRuleDiskRate();
        getNotifyInterval();
    }

    @PreDestroy
    public void destroy() {
        TRACELogger.trace(ProgramName, " start to destroy...");
        try {
            this.executor.shutdown(); // 記得要關閉
            if (this.executor.awaitTermination(60, TimeUnit.SECONDS))
                TRACELogger.trace(ProgramName, " executor terminate all runnable successful");
            else
                TRACELogger.trace(ProgramName, " executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            TRACELogger.warn(e, e.getMessage());
        }
    }

    public void reloadMonitor() {
        LogMDC.put(Const.MDC_PGFILE, SvrConst.SVR_APPMON);
        try {
            TRACELogger.info(ProgramName, " reloadMonitor begin");
            // 初始化
            init();
            TRACELogger.info(ProgramName, " init finish");
            // 系統信息
            fetchSystemInfo();
            TRACELogger.info(ProgramName, " fetchSystemInfo finish");
            // DB信息
            fetchDbInfo();
            TRACELogger.info(ProgramName, " fetchDbInfo finish");
            // MQ信息
            fetchMqInfo();
            TRACELogger.info(ProgramName, " fetchMqInfo finish");
            // Process信息
            fetchProcess();
            TRACELogger.info(ProgramName, " fetchProcess finish");
            // 服務信息
            fetchServicesAlertInfo();
            TRACELogger.info(ProgramName, " fetchServicesAlertInfo finish");
            // suip的網絡狀態
            fetchLocalSuipHsmMonitorData();
            TRACELogger.info(ProgramName, " fetchLocalSuipConnection finish");
        } catch (Throwable t) {
            TRACELogger.error(t, ProgramName, " reloadMonitor with Exception occur, ", t.getMessage());
        } finally {
            TRACELogger.info(ProgramName, " reloadMonitor end");
        }
    }

    /*
     * 初始化
     */
    private void init() {
        // 初始化系統信息
        Sms systemSms = smsMapper.selectByPrimaryKey(SERVICE_NAME_SYSTEM, HOSTIP);
        if (systemSms == null) {
            systemSms = createSms(SERVICE_NAME_SYSTEM, HOSTIP, HOSTNAME);
            smsMapper.insert(systemSms);
        } else {
            if ("2".equals(systemSms.getSmsServicestate())) {
                systemSms.setSmsServicestate("0");
                systemSms.setSmsUpdatetime(Calendar.getInstance().getTime());
                smsMapper.updateByPrimaryKeySelective(systemSms);
            }
        }
        // 初始化DB信息
        Sms dbSms = smsMapper.selectByPrimaryKey(SERVICE_NAME_DB, HOSTIP);
        if (dbSms == null) {
            dbSms = createSms(SERVICE_NAME_DB, HOSTIP, HOSTNAME);
            smsMapper.insert(dbSms);
        } else {
            if ("2".equals(dbSms.getSmsServicestate())) {
                dbSms.setSmsServicestate("0");
                dbSms.setSmsUpdatetime(Calendar.getInstance().getTime());
                smsMapper.updateByPrimaryKeySelective(dbSms);
            }
        }
        MonitorServerInfo[] services = new MonitorServerInfo[this.monitorSchedulerJobConfig.getServices().size()];
        this.monitorSchedulerJobConfig.getServices().toArray(services);
        // 初始化服務信息
        for (MonitorServerInfo serverInfo : services) {
            Sms service = smsMapper.selectByPrimaryKey(serverInfo.getName(), serverInfo.getHostip());
            if (service == null) {
                if (StringUtils.isBlank(serverInfo.getHostname()))
                    serverInfo.setHostname(StringUtils.SPACE);
                service = createSms(serverInfo.getName(), serverInfo.getHostip(), serverInfo.getHostname());
                smsMapper.insert(service);
            } else {
                if ("2".equals(service.getSmsServicestate())) {
                    if (StringUtils.isNotBlank(serverInfo.getHostname()))
                        service.setSmsHostname(serverInfo.getHostname());
                    service.setSmsServicestate("0");
                    service.setSmsUpdatetime(Calendar.getInstance().getTime());
                    smsMapper.updateByPrimaryKeySelective(service);
                }
            }
        }
        // 初始化MQ訊息
        Sms mqservice = smsMapper.selectByPrimaryKey(SERVICE_NAME_MQ, HOSTIP);
        if (mqservice == null) {
            List<IBMMQStatus> ibmmqStatusList = getIBMMQStatusList();
            if (CollectionUtils.isNotEmpty(ibmmqStatusList)) {
                mqservice = createSms(SERVICE_NAME_MQ, HOSTIP, HOSTNAME);
                mqservice.setSmsOthers(new Gson().toJson(ibmmqStatusList));
                smsMapper.insert(mqservice);
            }
        }
        //是否停止提醒 true -停止提醒 / false -提醒
        Sysconf sysconf = sysconfExtMapper.selectByPrimaryKey(SYSCONF_VALUE_CMN, SYSCONF_NAME_STOPNOTIFICATION);
        if (sysconf != null) {
            this.monitorSchedulerJobConfig.setStopNotification(Boolean.parseBoolean(sysconf.getSysconfValue()));
        }
        // 是否啟用自動重啟
        sysconf = sysconfExtMapper.selectByPrimaryKey(SYSCONF_VALUE_CMN, SYSCONF_NAME_ENABLEAUTORESTART);
        if (sysconf != null) {
            this.monitorSchedulerJobConfig.setEnableAutoRestart(Boolean.parseBoolean(sysconf.getSysconfValue()));
        }
    }

    /*
     * 系統信息
     */
    private void fetchSystemInfo() {
        try {
            Sms sms = createSms(SERVICE_NAME_SYSTEM, HOSTIP, HOSTNAME);
            sms.setSmsServicestate("1");
            sms.setSmsCpu(fetchSystemCpuUsage());
            sms.setSmsRam(fetchSystemMemoryUsage());
            sms.setSmsOthers(fetchSystemHardDisk());
            sms.setSmsUpdatetime(Calendar.getInstance().getTime());
            smsMapper.updateByPrimaryKeySelective(sms);
        } catch (Throwable e) {
            TRACELogger.warn(ProgramName, " fetchSystemInfo with exception occur, ", e.getMessage());
        }
    }

    /*
     * 資料庫狀態---DB
     */
    private void fetchDbInfo() {
        String promQL = "/actuator/health";
        String jsonStr;
        try {
            jsonStr = httpClient.doGet(StringUtils.join(URL, promQL), false);
        } catch (Throwable e) {
            if ("503".equals(e.getMessage().substring(0, 3))) {
                String msg = e.getMessage();
                int index = msg.indexOf("{");
                jsonStr = msg.substring(index, msg.length() - 1);
            } else {
                TRACELogger.warn(ProgramName, " fetchDbInfo with exception occur, ", e.getMessage());
                return;
            }
        }
        JSONObject dbObject = new JSONObject();
        dbObject.put(DB_NAME_FEP, Boolean.FALSE.toString());
        dbObject.put(DB_NAME_EMS, Boolean.FALSE.toString());
        dbObject.put(DB_NAME_ENC, Boolean.FALSE.toString());
        dbObject.put(DB_NAME_ENCLOG, Boolean.FALSE.toString());
        dbObject.put(DB_NAME_FEPHIS, Boolean.FALSE.toString());
        JSONObject rootObject = new JSONObject(jsonStr);
        if (rootObject != null) {
            JSONObject componentsJSONObject = rootObject.getJSONObject(JSON_FIELD_COMPONENTS);
            if (componentsJSONObject != null) {
                JSONObject dbJSONObject = componentsJSONObject.getJSONObject(SERVICE_NAME_DB.toLowerCase());
                if (dbJSONObject != null) {
                    JSONObject compJSONObject = dbJSONObject.getJSONObject(JSON_FIELD_COMPONENTS);
                    if (compJSONObject != null) {
                        // FEPDB
                        if (compJSONObject.has(DataSourceConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject fepJSONObject = compJSONObject.getJSONObject(DataSourceConstant.BEAN_NAME_DATASOURCE);
                            if (fepJSONObject != null && STATUS_UP.equals(fepJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_FEP, Boolean.TRUE.toString());
                            }
                        }
                        // EMSDB
                        if (compJSONObject.has(DataSourceEmsConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject emsJSONObject = compJSONObject.getJSONObject(DataSourceEmsConstant.BEAN_NAME_DATASOURCE);
                            if (emsJSONObject != null && STATUS_UP.equals(emsJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_EMS, Boolean.TRUE.toString());
                            }
                        }
                        // ENCDB
                        if (compJSONObject.has(DataSourceEncConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject desJSONObject = compJSONObject.getJSONObject(DataSourceEncConstant.BEAN_NAME_DATASOURCE);
                            if (desJSONObject != null && STATUS_UP.equals(desJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_ENC, Boolean.TRUE.toString());
                            }
                        }
                        // DESLOGDB
                        if (compJSONObject.has(DataSourceDeslogConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject emsLogJSONObject = compJSONObject.getJSONObject(DataSourceDeslogConstant.BEAN_NAME_DATASOURCE);
                            if (emsLogJSONObject != null && STATUS_UP.equals(emsLogJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_ENCLOG, Boolean.TRUE.toString());
                            }
                        }
                        // FEPHIS
                        if (compJSONObject.has(DataSourceHisConstant.BEAN_NAME_DATASOURCE)) {
                            JSONObject emsLogJSONObject = compJSONObject.getJSONObject(DataSourceHisConstant.BEAN_NAME_DATASOURCE);
                            if (emsLogJSONObject != null && STATUS_UP.equals(emsLogJSONObject.getString(JSON_FIELD_STATUS))) {
                                dbObject.put(DB_NAME_FEPHIS, Boolean.TRUE.toString());
                            }
                        }
                    }
                }
            }
        }
        //是否提醒
        if (!this.monitorSchedulerJobConfig.isStopNotification()) {
            //是否觸發提醒FEPDB
            if (!Boolean.parseBoolean(dbObject.getString(DB_NAME_FEP))) {
                //已報價提醒過一次
                if (alert.containsKey(DB_NAME_FEP)) {
                    Map<Integer, Long> alertTimes = alert.get(DB_NAME_FEP);
                    //小於設定報警次數才會報警
                    if (alertTimes.size() < notifyCount) {
                        Long firstTime = alertTimes.get(1);
                        Calendar firstCal = Calendar.getInstance();
                        firstCal.setTimeInMillis(firstTime);
                        String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                        Long lastTime = alertTimes.get(alertTimes.size());
                        Long nowTime = Calendar.getInstance().getTimeInMillis();
                        //大於間隔時間才會報警
                        if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                            alertTimes.put(alertTimes.size() + 1, nowTime);
                            alert.put(DB_NAME_FEP, alertTimes);
                            String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_FEP, "!");
                            String body = StringUtils.join("發生時間:", firstTimeStr, "\n", msg);
                            String subject = StringUtils.join(APPNAME + "第", alertTimes.size(), "次警告-", msg);
                            String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                            sendAlertMail(subject, body, remark);
                        }
                    }
                } else {
                    //第一此報警
                    Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
                    alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
                    alert.put(DB_NAME_FEP, alertTimes);
                    String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_FEP, "!");
                    String body = StringUtils.join("發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
                    String subject = StringUtils.join(APPNAME + "第1次警告-", msg);
                    String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                    sendAlertMail(subject, body, remark);
                }
            } else {
                alert.remove(DB_NAME_FEP);
            }
            //否觸發提醒EMSDB
            if (!Boolean.parseBoolean(dbObject.getString(DB_NAME_EMS))) {
                //已報價提醒過一次
                if (alert.containsKey(DB_NAME_EMS)) {
                    Map<Integer, Long> alertTimes = alert.get(DB_NAME_EMS);
                    //小於設定報警次數才會報警
                    if (alertTimes.size() < notifyCount) {
                        Long firstTime = alertTimes.get(1);
                        Calendar firstCal = Calendar.getInstance();
                        firstCal.setTimeInMillis(firstTime);
                        String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                        Long lastTime = alertTimes.get(alertTimes.size());
                        Long nowTime = Calendar.getInstance().getTimeInMillis();
                        //大於間隔時間才會報警
                        if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                            alertTimes.put(alertTimes.size() + 1, nowTime);
                            alert.put(DB_NAME_EMS, alertTimes);
                            String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_EMS, "!");
                            String body = StringUtils.join("發生時間:", firstTimeStr, "\n", msg);
                            String subject = StringUtils.join(APPNAME + "第", alertTimes.size(), "次警告-", msg);
                            String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                            sendAlertMail(subject, body, remark);
                        }
                    }
                } else {
                    //第一此報警
                    Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
                    alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
                    alert.put(DB_NAME_EMS, alertTimes);
                    String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_EMS, "!");
                    String body = StringUtils.join("發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
                    String subject = StringUtils.join(APPNAME + "第1次警告-", msg);
                    String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                    sendAlertMail(subject, body, remark);
                }
            } else {
                alert.remove(DB_NAME_EMS);
            }
            //是否觸發提醒ENCDB
            if (!Boolean.parseBoolean(dbObject.getString(DB_NAME_ENC))) {
                //已報價提醒過一次
                if (alert.containsKey(DB_NAME_ENC)) {
                    Map<Integer, Long> alertTimes = alert.get(DB_NAME_ENC);
                    //小於設定報警次數才會報警
                    if (alertTimes.size() < notifyCount) {
                        Long firstTime = alertTimes.get(1);
                        Calendar firstCal = Calendar.getInstance();
                        firstCal.setTimeInMillis(firstTime);
                        String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                        Long lastTime = alertTimes.get(alertTimes.size());
                        Long nowTime = Calendar.getInstance().getTimeInMillis();
                        //大於間隔時間才會報警
                        if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                            alertTimes.put(alertTimes.size() + 1, nowTime);
                            alert.put(DB_NAME_ENC, alertTimes);
                            String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_ENC, "!");
                            String body = StringUtils.join("發生時間:", firstTimeStr, "\n", msg);
                            String subject = StringUtils.join(APPNAME + "第", alertTimes.size(), "次警告-", msg);
                            String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                            sendAlertMail(subject, body, remark);
                        }
                    }
                } else {
                    //第一此報警
                    Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
                    alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
                    alert.put(DB_NAME_ENC, alertTimes);
                    String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_ENC, "!");
                    String body = StringUtils.join("發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
                    String subject = StringUtils.join(APPNAME + "第1次警告-", msg);
                    String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                    sendAlertMail(subject, body, remark);
                }
            } else {
                alert.remove(DB_NAME_ENC);
            }
            //是否觸發提醒DESLOGDB
            if (!Boolean.parseBoolean(dbObject.getString(DB_NAME_ENCLOG))) {
                //已報價提醒過一次
                if (alert.containsKey(DB_NAME_ENCLOG)) {
                    Map<Integer, Long> alertTimes = alert.get(DB_NAME_ENCLOG);
                    //小於設定報警次數才會報警
                    if (alertTimes.size() < notifyCount) {
                        Long firstTime = alertTimes.get(1);
                        Calendar firstCal = Calendar.getInstance();
                        firstCal.setTimeInMillis(firstTime);
                        String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                        Long lastTime = alertTimes.get(alertTimes.size());
                        Long nowTime = Calendar.getInstance().getTimeInMillis();
                        //大於間隔時間才會報警
                        if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                            alertTimes.put(alertTimes.size() + 1, nowTime);
                            alert.put(DB_NAME_ENCLOG, alertTimes);
                            String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_ENCLOG, "!");
                            String body = StringUtils.join("發生時間:", firstTimeStr, "\n", msg);
                            String subject = StringUtils.join(APPNAME + "第", alertTimes.size(), "次警告-", msg);
                            String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                            sendAlertMail(subject, body, remark);
                        }
                    }
                } else {
                    //第一此報警
                    Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
                    alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
                    alert.put(DB_NAME_ENCLOG, alertTimes);
                    String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_ENCLOG, "!");
                    String body = StringUtils.join("發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
                    String subject = StringUtils.join(APPNAME + "第1次警告-", msg);
                    String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                    sendAlertMail(subject, body, remark);
                }
            } else {
                alert.remove(DB_NAME_ENCLOG);
            }
            //是否觸發提醒FEPHIS
            if (!Boolean.parseBoolean(dbObject.getString(DB_NAME_FEPHIS))) {
                //已報價提醒過一次
                if (alert.containsKey(DB_NAME_FEPHIS)) {
                    Map<Integer, Long> alertTimes = alert.get(DB_NAME_FEPHIS);
                    //小於設定報警次數才會報警
                    if (alertTimes.size() < notifyCount) {
                        Long firstTime = alertTimes.get(1);
                        Calendar firstCal = Calendar.getInstance();
                        firstCal.setTimeInMillis(firstTime);
                        String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                        Long lastTime = alertTimes.get(alertTimes.size());
                        Long nowTime = Calendar.getInstance().getTimeInMillis();
                        //大於間隔時間才會報警
                        if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                            alertTimes.put(alertTimes.size() + 1, nowTime);
                            alert.put(DB_NAME_FEPHIS, alertTimes);
                            String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_FEPHIS, "!");
                            String body = StringUtils.join("發生時間:", firstTimeStr, "\n", msg);
                            String subject = StringUtils.join(APPNAME + "第", alertTimes.size(), "次警告-", msg);
                            String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                            sendAlertMail(subject, body, remark);
                        }
                    }
                } else {
                    //第一此報警
                    Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
                    alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
                    alert.put(DB_NAME_FEPHIS, alertTimes);
                    String msg = StringUtils.join("主機[", HOSTNAME, ":", HOSTIP, "]無法連線至資料庫", DB_NAME_FEPHIS, "!");
                    String body = StringUtils.join("發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
                    String subject = StringUtils.join(APPNAME + "第1次警告-", msg);
                    String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                    sendAlertMail(subject, body, remark);
                }
            } else {
                alert.remove(DB_NAME_FEPHIS);
            }
        }
        Sms sms = createSms(SERVICE_NAME_DB, HOSTIP, HOSTNAME);
        sms.setSmsServicestate("1");
        sms.setSmsOthers(dbObject.toString());
        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
        smsMapper.updateByPrimaryKeySelective(sms);
    }

    /*
     * MQ信息
     */
    private void fetchMqInfo() {
        if (CollectionUtils.isEmpty(this.monitorSchedulerJobConfig.getMqs())) return;
        List<IBMMQStatus> ibmmqStatusList = new ArrayList<>();
        Sms sms = createSms(SERVICE_NAME_MQ, HOSTIP, HOSTNAME);
        // int openOptions = CMQC.MQOO_INQUIRE;
        int openOptions = CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_INQUIRE | CMQC.MQOO_FAIL_IF_QUIESCING; // 合庫取不到, 調整一下
        for (MonitorMQServerInfo serverInfo : this.monitorSchedulerJobConfig.getMqs()) {
            MQQueueManager queueManager = null;
            try {
                if (CollectionUtils.isNotEmpty(serverInfo.getQueueNames())) {
                    queueManager = JmsFactory.createMQQueueManager(
                            serverInfo.getHostip(),
                            Integer.parseInt(serverInfo.getPort()),
                            serverInfo.getQueueManagerName(),
                            serverInfo.getChannel(),
                            serverInfo.getUserID(),
                            serverInfo.getPassword());
                    for (MonitorMQNameInfo monitorMQNameInfo : serverInfo.getQueueNames()) {
                        IBMMQStatus status = new IBMMQStatus();
                        status.setServiceIP(serverInfo.getHostip());
                        status.setServiceHostName(serverInfo.getHostname());
                        status.setName(monitorMQNameInfo.getName());
                        status.setObjectType(monitorMQNameInfo.getType());
                        status.setStatus("0");
                        status.setQueueCount("0");
                        MQQueue queue = null;
                        try {
                            queue = queueManager.accessQueue(monitorMQNameInfo.getName(), openOptions);
                            status.setStatus("1");
                            status.setQueueCount(Integer.toString(queue.getCurrentDepth()));
                            // 如果Queue的數量超過設定值, 則需要發送mail
                            if (!this.monitorSchedulerJobConfig.isStopNotification() &&
                                    monitorMQNameInfo.getQueueMax() > 0 && queue.getCurrentDepth() > monitorMQNameInfo.getQueueMax()) {
                                this.sendMQAlertQueueMaxMail(serverInfo, monitorMQNameInfo, status);
                            }
                        } catch (Throwable e) {
                            TRACELogger.warn("accessQueue ip = [", serverInfo.getHostip(), "],",
                                    " port = [", serverInfo.getPort(), "],",
                                    " queueManagerName = [", serverInfo.getQueueManagerName(), "],",
                                    " channel = [", serverInfo.getChannel(), "],",
                                    " queueName = [", monitorMQNameInfo.getName(), "] with exception occur, ", e.getMessage());
                            if (!this.monitorSchedulerJobConfig.isStopNotification()) {
                                sendMQAlertMail(serverInfo, monitorMQNameInfo);
                            }
                        } finally {
                            if (queue != null) {
                                queue.close();
                            }
                            ibmmqStatusList.add(status);
                        }
                    }
                }
            } catch (Throwable e) {
                TRACELogger.warn("fetchMqInfo ip = [", serverInfo.getHostip(), "],",
                        " port = [", serverInfo.getPort(), "],",
                        " queueManagerName = [", serverInfo.getQueueManagerName(), "],",
                        " channel = [", serverInfo.getChannel(), "] with exception occur, ", e.getMessage());
                // 如果取queueManager出現異常, 還是需要將監控的資料加入, 只是狀態都是0
                for (MonitorMQNameInfo nameInfo : serverInfo.getQueueNames()) {
                    IBMMQStatus status = new IBMMQStatus();
                    status.setServiceIP(serverInfo.getHostip());
                    status.setServiceHostName(serverInfo.getHostname());
                    status.setName(nameInfo.getName());
                    status.setObjectType(nameInfo.getType());
                    status.setStatus("0");
                    status.setQueueCount("0");
                    ibmmqStatusList.add(status);
                }
                if (!this.monitorSchedulerJobConfig.isStopNotification()) {
                    for (MonitorMQNameInfo monitorMQNameInfo : serverInfo.getQueueNames()) {
                        sendMQAlertMail(serverInfo, monitorMQNameInfo);
                    }
                }
            } finally {
                if (queueManager != null) {
                    try {
                        queueManager.disconnect();
                    } catch (Throwable e) {
                        TRACELogger.warn(e, e.getMessage());
                    }
                }
            }
        }
        sms.setSmsServicestate(ibmmqStatusList.stream().filter(t -> "1".equals(t.getStatus())).count() == ibmmqStatusList.size() ? "1" : "0");
        sms.setSmsOthers(new Gson().toJson(ibmmqStatusList));
        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
        smsMapper.updateByPrimaryKeySelective(sms);
    }

    /**
     * MQ送Alert
     *
     * @param serverInfo
     * @param monitorMQNameInfo
     */
    private void sendMQAlertMail(MonitorMQServerInfo serverInfo, MonitorMQNameInfo monitorMQNameInfo) {
        String alertKey = StringUtils.join(
                Arrays.asList(
                        serverInfo.getHostname(),
                        serverInfo.getHostip(),
                        serverInfo.getPort(),
                        serverInfo.getQueueManagerName(),
                        serverInfo.getChannel(),
                        monitorMQNameInfo.getName()), '@');
        if (alert.containsKey(alertKey)) {
            Map<Integer, Long> alertTimes = alert.get(alertKey);
            // 小於設定報警次數才會報警
            if (alertTimes.size() < notifyCount) {
                Long firstTime = alertTimes.get(1);
                Calendar firstCal = Calendar.getInstance();
                firstCal.setTimeInMillis(firstTime);
                String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                Long lastTime = alertTimes.get(alertTimes.size());
                Long nowTime = Calendar.getInstance().getTimeInMillis();
                // 大於間隔時間才會報警
                if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                    alertTimes.put(alertTimes.size() + 1, nowTime);
                    alert.put(alertKey, alertTimes);
                    String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), ":", serverInfo.getPort(), ":", serverInfo.getQueueManagerName(), ":", serverInfo.getChannel(), "]上的MQ[", monitorMQNameInfo.getName(), "]無法訪問!");
                    String body = StringUtils.join("服務停止時間:", firstTimeStr, "\n", msg);
                    String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                    String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                    sendAlertMail(subject, body, remark);
                }
            }
        } else {
            Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
            alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
            alert.put(alertKey, alertTimes);
            String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), ":", serverInfo.getPort(), ":", serverInfo.getQueueManagerName(), ":", serverInfo.getChannel(), "]上的MQ[", monitorMQNameInfo.getName(), "]無法訪問!");
            String body = StringUtils.join("服務停止時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
            String subject = StringUtils.join(APPNAME + "服務異常第1次通知-", msg);
            String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
            sendAlertMail(subject, body, remark);
        }
    }

    /**
     * MQ送Alert, Queue數量超過限制
     *
     * @param serverInfo
     * @param monitorMQNameInfo
     * @param status
     */
    private void sendMQAlertQueueMaxMail(MonitorMQServerInfo serverInfo, MonitorMQNameInfo monitorMQNameInfo, IBMMQStatus status) {
        String alertKey = StringUtils.join(
                Arrays.asList(
                        serverInfo.getHostname(),
                        serverInfo.getHostip(),
                        serverInfo.getPort(),
                        serverInfo.getQueueManagerName(),
                        serverInfo.getChannel(),
                        monitorMQNameInfo.getName(),
                        "QueueMax"), '@');
        if (alert.containsKey(alertKey)) {
            Map<Integer, Long> alertTimes = alert.get(alertKey);
            // 小於設定報警次數才會報警
            if (alertTimes.size() < notifyCount) {
                Long firstTime = alertTimes.get(1);
                Calendar firstCal = Calendar.getInstance();
                firstCal.setTimeInMillis(firstTime);
                String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                Long lastTime = alertTimes.get(alertTimes.size());
                Long nowTime = Calendar.getInstance().getTimeInMillis();
                // 大於間隔時間才會報警
                if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                    alertTimes.put(alertTimes.size() + 1, nowTime);
                    alert.put(alertKey, alertTimes);
                    String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), ":", serverInfo.getPort(), ":", serverInfo.getQueueManagerName(), ":", serverInfo.getChannel(), "]上的MQ[", monitorMQNameInfo.getName(), "]訊息數量為[", status.getQueueCount(), "]已經超過[", monitorMQNameInfo.getQueueMax(), "]警示值!");
                    String body = StringUtils.join("發生時間:", firstTimeStr, "\n", msg);
                    String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                    String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                    sendAlertMail(subject, body, remark);
                }
            }
        } else {
            Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
            alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
            alert.put(alertKey, alertTimes);
            String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), ":", serverInfo.getPort(), ":", serverInfo.getQueueManagerName(), ":", serverInfo.getChannel(), "]上的MQ[", monitorMQNameInfo.getName(), "]訊息數量為[", status.getQueueCount(), "]已經超過[", monitorMQNameInfo.getQueueMax(), "]警示值!");
            String body = StringUtils.join("發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
            String subject = StringUtils.join(APPNAME + "服務異常第1次通知-", msg);
            String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
            sendAlertMail(subject, body, remark);
        }
    }

    /*
     * 服務信息
     */
    private void fetchServicesAlertInfo() {
        TRACELogger.info(ProgramName, " fetchServicesAlertInfo");
        MonitorServerInfo[] services = new MonitorServerInfo[this.monitorSchedulerJobConfig.getServices().size()];
        this.monitorSchedulerJobConfig.getServices().toArray(services);
        for (MonitorServerInfo serverInfo : services) {
            Sms smsServices = smsMapper.selectByPrimaryKey(serverInfo.getName(), serverInfo.getHostip());
            if (smsServices == null) {
                continue;
            }
            //NO SERVICE
            if (SERVICE_NAME_SYSTEM.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_DB.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_MQ.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_NET_CLIENT.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_NET_SERVER.equals(smsServices.getSmsServicename())
                    || SERVICE_NAME_PROCESS.equals(smsServices.getSmsServicename())) {
                continue;
            }
            //服務異常
            boolean isError = false;
            if ("0".equals(smsServices.getSmsServicestate())) {
                //SERVICE STOP
                isError = true;
            } else {
                TRACELogger.warn(smsServices.getSmsUpdatetime());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(smsServices.getSmsUpdatetime());
                Long lastTime = calendar.getTimeInMillis();
                TRACELogger.warn(lastTime);
                TRACELogger.warn(Calendar.getInstance().getTime());
                Long nowTime = Calendar.getInstance().getTimeInMillis();
                TRACELogger.warn(nowTime);
                //如果超過2分鐘無更新資料，判定為服務異常(n為監控的取樣間隔時間)
                if (nowTime - lastTime > (1000 * 60 * 2)) {
                    //SERVICE STOP
                    if (!smsServices.getSmsServicename().equalsIgnoreCase(APPNAME)) {
                        smsServices.setSmsServicestate("0");
                    }
                    smsServices.setSmsHostname(serverInfo.getHostname());
                    smsServices.setSmsStoptime(smsServices.getSmsUpdatetime());
                    smsServices.setSmsUpdatetime(Calendar.getInstance().getTime());
                    smsMapper.updateByPrimaryKey(smsServices);
                    isError = true;
                } else {
                    //SERVICE RUNNING
                }
            }
            if (isError) {
                //是否提醒
                if (!this.monitorSchedulerJobConfig.isStopNotification()) {
                    //是否觸發提醒  ---SmsServicestate--0 停止服務
                    //已報價提醒過一次
                    String alertKey = StringUtils.join(
                            Arrays.asList(
                                    serverInfo.getHostip(),
                                    serverInfo.getName()
                            ),
                            '@'
                    );
                    if (alert.containsKey(alertKey)) {
                        Map<Integer, Long> alertTimes = alert.get(alertKey);
                        //小於設定報警次數才會報警
                        if (alertTimes.size() < notifyCount) {
                            Long firstTime = alertTimes.get(1);
                            Calendar firstCal = Calendar.getInstance();
                            firstCal.setTimeInMillis(firstTime);
                            String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                            Long lastTime = alertTimes.get(alertTimes.size());
                            Long nowTime = Calendar.getInstance().getTimeInMillis();
                            //大於間隔時間才會報警
                            if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                                alertTimes.put(alertTimes.size() + 1, nowTime);
                                alert.put(alertKey, alertTimes);
                                String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), "]上的服務[", serverInfo.getName(), "]已停止!");
                                String body = StringUtils.join("服務停止時間:", firstTimeStr, "\n", msg);
                                String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                                String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                                sendAlertMail(subject, body, remark);
                            }
                        }
                    } else {
                        //第一此報警
                        Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
                        alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
                        alert.put(alertKey, alertTimes);
                        String msg = StringUtils.join("FEP在主機[", serverInfo.getHostname(), ":", serverInfo.getHostip(), "]上的服務[", serverInfo.getName(), "]已停止!");
                        String body = StringUtils.join("服務停止時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
                        String subject = StringUtils.join(APPNAME + "服務異常第1次通知-", msg);
                        String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                        sendAlertMail(subject, body, remark);
                    }
                }
                // 根據設定是否需要重啟服務
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    this.notifyMonitorServerLauncher(serverInfo);
                }
            } else {
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    MonitorServerLauncherInfo launcher =
                            this.monitorSchedulerJobConfig.getLaunchers().stream().filter(
                                    t -> t.getName().equalsIgnoreCase(serverInfo.getName()) && t.getHostip().equalsIgnoreCase(serverInfo.getHostip())).findFirst().orElse(null);
                    if (launcher != null) {
                        // 如果有啟動成功, 則將上一次重啟的日期時間設置為null
                        launcher.setLatestCmdStart(null);
                    }
                }
            }
        }
        TRACELogger.info(ProgramName, " fetchServicesAlertInfo");
    }

    /**
     * 系統狀態---CPU
     *
     * @return
     */
    private int fetchSystemCpuUsage() {
        int cpu = 0;
        try {
            cpu = MonitorDataCollector.fetchSystemCpuUsage(httpClient, URL).intValue();
        } catch (Throwable e) {
            TRACELogger.warn(ProgramName, " fetchSystemMemoryUsage with exception occur, ", e.getMessage());
        }
        return cpu;
    }

    /**
     * 系統狀態---RMA
     *
     * @return
     */
    private int fetchSystemMemoryUsage() {
        int memo = 0;
        try {
            memo = MonitorDataCollector.fetchSystemMemoryUsage().intValue();
        } catch (Throwable e) {
            TRACELogger.warn(ProgramName, " fetchSystemMemoryUsage with exception occur, ", e.getMessage());
        }
        return memo;
    }

    /**
     * 系統DISK USED
     *
     * @return
     */
    private String fetchSystemHardDisk() {
        String disk = "";
        try {
            JSONArray disArray = new JSONArray();
            List<MonitorDataDisk> monitorDataDiskList = MonitorDataCollector.fetchSystemHardDisk(HOSTNAME, HOSTIP);
            for (MonitorDataDisk monitorDataDisk : monitorDataDiskList) {
                disArray.put(this.analyseDiskData(monitorDataDisk));
            }
            disk = disArray.toString();
        } catch (Throwable e) {
            TRACELogger.warn(ProgramName, " fetchSystemHardDisk with exception occur, ", e.getMessage());
        }
        return disk;
    }

    /**
     * 分析處理磁盤信息
     *
     * @param monitorDataDisk
     * @return
     */
    public JSONObject analyseDiskData(MonitorDataDisk monitorDataDisk) {
        JSONObject disObject = new JSONObject();
        String diskName = monitorDataDisk.getName();
        int gb = (int) Math.pow(1024, 3);
        String diskTotal = FormatUtil.doubleFormat(monitorDataDisk.getTotal() / gb, "#,###G");
        String diskFree = FormatUtil.doubleFormat(monitorDataDisk.getFree() / gb, "#,###G");
        String diskUsed = FormatUtil.doubleFormat(monitorDataDisk.getUsed() / gb, "#,###G");
        double total = monitorDataDisk.getTotal();
        double used = monitorDataDisk.getUsed();
        double diskRate = used / total;
        double diskFreeRate = Double.parseDouble(FormatUtil.doubleFormat(monitorDataDisk.getFree() / total, "0.00"));
        String diskFreeRateStr = FormatUtil.doubleFormat(diskFreeRate, "0%");
        String ruleRiskRateStr = FormatUtil.doubleFormat(ruleRiskRate, "0%");
        disObject.put(JSON_FIELD_HOSTNAME, monitorDataDisk.getHostName());
        disObject.put(JSON_FIELD_NAME, diskName);
        disObject.put(JSON_FIELD_IP, monitorDataDisk.getIp());
        disObject.put(JSON_FIELD_USED, diskUsed);
        disObject.put(JSON_FIELD_TOTAL, diskTotal);
        disObject.put(JSON_FIELD_DISK, FormatUtil.doubleFormat(diskRate, "0%"));
        //是否提醒
        String alertKey = StringUtils.join(
                Arrays.asList(
                        monitorDataDisk.getIp(),
                        diskName
                ),
                '@'
        );
        if (!this.monitorSchedulerJobConfig.isStopNotification()) {
            //是否觸發提醒
            if (diskFreeRate < ruleRiskRate) {
                //已報價提醒過一次
                if (alert.containsKey(alertKey)) {
                    Map<Integer, Long> alertTimes = alert.get(alertKey);
                    //小於設定報警次數才會報警
                    if (alertTimes.size() < notifyCount) {
                        Long firstTime = alertTimes.get(1);
                        Calendar firstCal = Calendar.getInstance();
                        firstCal.setTimeInMillis(firstTime);
                        String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                        Long lastTime = alertTimes.get(alertTimes.size());
                        Long nowTime = Calendar.getInstance().getTimeInMillis();
                        //大於間隔時間才會報警
                        if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                            alertTimes.put(alertTimes.size() + 1, nowTime);
                            alert.put(alertKey, alertTimes);
                            String msg = StringUtils.join("主機[", monitorDataDisk.getHostName(), ":", monitorDataDisk.getIp(), "]磁碟", diskName, "可用空間目前剩餘", diskFreeRateStr, "(", diskFree, "),已低於", ruleRiskRateStr, "!");
                            String body = StringUtils.join("發生時間:", firstTimeStr, "\n", msg);
                            String subject = StringUtils.join(APPNAME + "第", alertTimes.size(), "次警告-", msg);
                            String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                            sendAlertMail(subject, body, remark);
                        }
                    }
                } else {
                    //第一此報警
                    Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
                    alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
                    alert.put(alertKey, alertTimes);
                    String msg = StringUtils.join("主機[", monitorDataDisk.getHostName(), ":", monitorDataDisk.getIp(), "]磁碟", diskName, "可用空間目前剩餘", diskFreeRateStr, "(", diskFree, "),已低於", ruleRiskRateStr, "!");
                    String body = StringUtils.join("發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
                    String subject = StringUtils.join(APPNAME + "第1次警告-", msg);
                    String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                    sendAlertMail(subject, body, remark);
                }
            } else {
                alert.remove(alertKey);
            }
        } else {
            alert.remove(alertKey);
        }
        return disObject;
    }

    /**
     * 建立SMS物件
     *
     * @param sname
     * @param ip
     * @param hname
     * @return
     */
    private Sms createSms(String sname, String ip, String hname) {
        Sms sms = new Sms();
        sms.setSmsServicename(sname);
        sms.setSmsServiceip(ip);
        sms.setSmsHostname(hname);
        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
        // 0-停止 1-正常
        sms.setSmsServicestate("0");
        sms.setSmsCpu(0);
        sms.setSmsCpuThreshold(0);
        sms.setSmsRam(0);
        sms.setSmsRamThreshold(0);
        sms.setSmsThreads(0);
        sms.setSmsThreadsActive(0);
        sms.setSmsThreadsThreshold(0);
        return sms;
    }

    /*
     * 發送提醒郵件
     */
    private void sendAlertMail(String subject, String body, String remark) {
        TRACELogger.info("sendAlertMail start");
        try {
            // mailSender.sendSimpleEmail(
            //         this.monitorSchedulerJobConfig.getMailSender(),
            //         this.monitorSchedulerJobConfig.getMailList(),
            //         ArrayUtils.toArray(StringUtils.EMPTY),
            //         subject,
            //         body,
            //         EmailUtil.MailPriority.High);
            notifyHelper.sendSimpleMail(NotifyHelperTemplateId.APP_MONITOR, StringUtils.join(this.monitorSchedulerJobConfig.getMailList(), ','),
                    StringUtils.join(subject, "\r\n", body), true);
        } catch (Throwable e) {
            TRACELogger.warn("sendAlertMail with exception occur, ", e.getMessage());
        }
    }

    /*
     * 獲取提醒次數&間隔時間配置
     */
    private void getNotifyInterval() {
        String notifyInterval = this.monitorSchedulerJobConfig.getNotifyInterval();
        if (StringUtils.isNotBlank(notifyInterval)) {
            String[] str = notifyInterval.split(",");
            if (ArrayUtils.isNotEmpty(str)) {
                notifyCount = str.length;
                for (int i = 0; i < notifyCount; i++) {
                    interval.put(i, Integer.valueOf(str[i]));
                }
            }
        }
    }

    private List<IBMMQStatus> getIBMMQStatusList() {
        List<IBMMQStatus> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(this.monitorSchedulerJobConfig.getMqs())) {
            for (MonitorMQServerInfo serverInfo : this.monitorSchedulerJobConfig.getMqs()) {
                if (CollectionUtils.isNotEmpty(serverInfo.getQueueNames())) {
                    for (MonitorMQNameInfo nameInfo : serverInfo.getQueueNames()) {
                        IBMMQStatus status = new IBMMQStatus();
                        status.setServiceIP(serverInfo.getHostip());
                        status.setServiceHostName(serverInfo.getHostname());
                        status.setName(nameInfo.getName());
                        status.setObjectType(nameInfo.getType());
                        status.setStatus("0");
                        status.setQueueCount("0");
                        list.add(status);
                    }
                }
            }
        }
        return list;
    }

    private void fetchProcess() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        List<OSProcess> processList = os.getProcesses();
        List<Sms> smsProcessList = new ArrayList<>();
        for (String processName : this.monitorSchedulerJobConfig.getProcessNameList()) {
            OSProcess osProcess = processList.stream().filter(t -> t.getName().equals(processName)).findFirst().orElse(null);
            if (osProcess != null) {
                Sms sms = new Sms();
                sms.setSmsServicename(processName);
                sms.setSmsHostname(HOSTNAME);
                sms.setSmsServiceip(HOSTIP);
                sms.setSmsServicestate("1");
                sms.setSmsStarttime(CalendarUtil.clone(osProcess.getStartTime()).getTime());
                sms.setSmsCpu((int) (10000L * (osProcess.getKernelTime() + osProcess.getUserTime()) / osProcess.getUpTime()));
                sms.setSmsRam((int) (osProcess.getResidentSetSize() / 1024));
                sms.setSmsThreads(osProcess.getThreadCount());
                smsProcessList.add(sms);
                processNameToStopTimeMap.remove(processName);
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    MonitorServerLauncherInfo launcher =
                            this.monitorSchedulerJobConfig.getLaunchers().stream().filter(t -> t.getName().equalsIgnoreCase(processName)).findFirst().orElse(null);
                    if (launcher != null) {
                        // 如果有啟動成功, 則將上一次重啟的日期時間設置為null
                        launcher.setLatestCmdStart(null);
                    }
                }
            } else {
                Date stopTime = processNameToStopTimeMap.get(processName);
                if (stopTime == null) {
                    stopTime = Calendar.getInstance().getTime();
                    processNameToStopTimeMap.put(processName, stopTime);
                }
                Sms sms = new Sms();
                sms.setSmsServicename(processName);
                sms.setSmsHostname(HOSTNAME);
                sms.setSmsServiceip(HOSTIP);
                sms.setSmsServicestate("0");
                sms.setSmsStoptime(stopTime);
                sms.setSmsCpu(0);
                sms.setSmsRam(0);
                sms.setSmsThreads(0);
                smsProcessList.add(sms);
                // sendAlertMail
                if (!this.monitorSchedulerJobConfig.isStopNotification()) {
                    String alertKey = StringUtils.join(
                            Arrays.asList(
                                    HOSTIP,
                                    processName
                            ),
                            '@'
                    );
                    if (alert.containsKey(alertKey)) {
                        Map<Integer, Long> alertTimes = alert.get(alertKey);
                        // 小於設定報警次數才會報警
                        if (alertTimes.size() < notifyCount) {
                            Long firstTime = alertTimes.get(1);
                            Calendar firstCal = Calendar.getInstance();
                            firstCal.setTimeInMillis(firstTime);
                            String firstTimeStr = FormatUtil.dateTimeFormat(firstCal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
                            Long lastTime = alertTimes.get(alertTimes.size());
                            Long nowTime = Calendar.getInstance().getTimeInMillis();
                            // 大於間隔時間才會報警
                            if (nowTime > (lastTime + interval.get(alertTimes.size()) * 60 * 1000)) {
                                alertTimes.put(alertTimes.size() + 1, nowTime);
                                alert.put(alertKey, alertTimes);
                                String msg = StringUtils.join("FEP在主機[", HOSTNAME, ":", HOSTIP, "]上的服務[", processName, "]已停止!");
                                String body = StringUtils.join("服務停止時間:", firstTimeStr, "\n", msg);
                                String subject = StringUtils.join(APPNAME + "服務異常第", alertTimes.size(), "次通知-", msg);
                                String remark = StringUtils.join(subject, ", 發生時間:", firstTimeStr);
                                sendAlertMail(subject, body, remark);
                            }
                        }
                    } else {
                        Map<Integer, Long> alertTimes = new HashMap<Integer, Long>();
                        alertTimes.put(1, Calendar.getInstance().getTimeInMillis());
                        alert.put(alertKey, alertTimes);
                        String msg = StringUtils.join("FEP在主機[", HOSTNAME, ":", HOSTIP, "]上的服務[", processName, "]已停止!");
                        String body = StringUtils.join("服務停止時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS), "\n", msg);
                        String subject = StringUtils.join(APPNAME + "服務異常第1次通知-", msg);
                        String remark = StringUtils.join(subject, ", 發生時間:", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
                        sendAlertMail(subject, body, remark);
                    }
                }
                // 根據設定是否需要重啟服務
                if (this.monitorSchedulerJobConfig.isEnableAutoRestart()) {
                    this.notifyMonitorProcessLauncher(processName);
                }
            }
        }
        Sms processSms = createSms(SERVICE_NAME_PROCESS, HOSTIP, HOSTNAME);
        processSms.setSmsServicestate(smsProcessList.stream().filter(t -> "1".equals(t.getSmsServicestate())).count() == smsProcessList.size() ? "1" : "0");
        try {
            // 注意這裡要用GsonDateParser產出json字串, 因為AIX下解讀Date字串有問題, 所以GsonDateParser中會特別處理Date類型的欄位, 轉為字串處理
            processSms.setSmsOthers(new GsonDateParser<List<Sms>>(new TypeToken<List<Sms>>() {}.getType()).writeOut(smsProcessList));
        } catch (Exception e) {
            TRACELogger.warn("parse smsProcessList to json string with exception occur, ", e.getMessage());
        }
        if (smsMapper.selectByPrimaryKey(SERVICE_NAME_PROCESS, HOSTIP) == null) {
            smsMapper.insert(processSms);
        } else {
            smsMapper.updateByPrimaryKeyWithBLOBs(processSms);
        }
    }

    private void notifyMonitorServerLauncher(MonitorServerInfo serverInfo) {
        if (CollectionUtils.isNotEmpty(this.monitorSchedulerJobConfig.getLaunchers())) {
            MonitorServerLauncherInfo launcher =
                    this.monitorSchedulerJobConfig.getLaunchers().stream().filter(
                            t -> t.getName().equalsIgnoreCase(serverInfo.getName()) && t.getHostip().equalsIgnoreCase(serverInfo.getHostip())).findFirst().orElse(null);
            if (launcher == null) {
                TRACELogger.warn("Cannot find launcher for name = [", serverInfo.getName(), "], hostIp = [", serverInfo.getHostip(), "]");
            } else if (launcher.getLatestCmdStart() != null) {
                TRACELogger.warn("Already restart name = [", serverInfo.getName(), "], hostIp = [", serverInfo.getHostip(), "] at [", FormatUtil.dateTimeInMillisFormat(launcher.getLatestCmdStart().getTime()), "]");
            } else {
                this.doLauncher(launcher);
            }
        }
    }

    private void notifyMonitorProcessLauncher(String processName) {
        if (CollectionUtils.isNotEmpty(this.monitorSchedulerJobConfig.getLaunchers())) {
            MonitorServerLauncherInfo launcher =
                    this.monitorSchedulerJobConfig.getLaunchers().stream().filter(t -> t.getName().equalsIgnoreCase(processName)).findFirst().orElse(null);
            if (launcher == null) {
                TRACELogger.warn("Cannot find launcher for processName = [", processName, "]");
            } else if (launcher.getLatestCmdStart() != null) {
                TRACELogger.warn("Already restart name = [", processName, "] at [", FormatUtil.dateTimeInMillisFormat(launcher.getLatestCmdStart().getTime()), "]");
            } else {
                this.doLauncher(launcher);
            }
        }
    }

    private void doLauncher(MonitorServerLauncherInfo launcher) {
        if (StringUtils.isNotBlank(launcher.getCmdStart())) {
            TRACELogger.info("Start to execute command = [", launcher.getCmdStart(), "]...");
            ProcessBuilder processBuilder = new ProcessBuilder().command(launcher.getCmdStart().split("\\s+"));
            processBuilder.redirectErrorStream(true);
            try {
                Process process = processBuilder.start();
                Consumer<String> consumer = launcher.isPrintInputStream() ? TRACELogger::debug : null;
                StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), consumer);
                executor.submit(streamGobbler);
                TRACELogger.info("Execute successful, command = [", launcher.getCmdStart(), "]");
            } catch (Throwable e) {
                TRACELogger.exceptionMsg(e, "Execute command = [", launcher.getCmdStart(), "] with exception occur, ", e.getMessage());
            }
        } else if (StringUtils.isNotBlank(launcher.getHttpStart())) {
            TRACELogger.info("Start to do http post, url = [", launcher.getHttpStart(), "]...");
            try {
                Map<String, String> args = new HashMap<>();
                args.put("operator", APPNAME.toUpperCase());
                String response = httpClient.doPost(launcher.getHttpStart(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
                if (Const.REPLY_OK.equals(response)) {
                    TRACELogger.info("Do http post successful, url = [", launcher.getHttpStart(), "]");
                } else {
                    TRACELogger.error("Do http post url = [", launcher.getHttpStart(), "] failed, ", response);
                }
            } catch (Throwable e) {
                TRACELogger.exceptionMsg(e, "Do http post url = [", launcher.getHttpStart(), "] with exception occur, ", e.getMessage());
            }
        }
    }

    /**
     * 獲取本機的suip和HSM的狀態
     */
    private void fetchLocalSuipHsmMonitorData() {
        TRACELogger.info("enter fetchLocalSuipHsmMonitorData");
        this.fetchSuipHsmMonitorData(this.monitorSchedulerJobConfig.getLocalSuip(), this.suipHsmMonitorData.localSuip, this.suipHsmMonitorData.localHsm);
        TRACELogger.info("exit fetchLocalSuipHsmMonitorData");
    }

    /**
     * 獲取遠程機器上的suip和HSM的狀態
     */
    public void fetchRemoteSuipHsmMonitorData() {
        TRACELogger.info("enter fetchRemoteSuipHsmMonitorData");
        this.fetchSuipHsmMonitorData(this.monitorSchedulerJobConfig.getRemoteSuip(), this.suipHsmMonitorData.remoteSuip, this.suipHsmMonitorData.remoteHsm);
        TRACELogger.info("exit fetchRemoteSuipHsmMonitorData");
    }

    /**
     * @param suipConnectionInfos
     * @param suipMonitorDataList suip的監控數據
     * @param hsmMonitorDataList  HSM的監控數據
     */
    private void fetchSuipHsmMonitorData(List<MonitorSuipConnectionInfo> suipConnectionInfos, List<ClientNetworkStatus> suipMonitorDataList, List<ServerNetworkStatus> hsmMonitorDataList) {
        // suip的監控數據
        List<ClientNetworkStatus> clientNetworkStatusList = new ArrayList<>();
        // HSM的監控數據
        List<ServerNetworkStatus> serverNetworkStatusList = new ArrayList<>();
        Set<String> filteredIp = new HashSet<>();
        int timeout = -1;
        String messageIn = null, body = null;
        String[] section = null, each = null;
        if (CollectionUtils.isNotEmpty(suipConnectionInfos)) {
            MonitorSuipConnectClientConfiguration configuration = SpringBeanFactoryUtil.registerBean(MonitorSuipConnectClientConfiguration.class);
            MonitorSuipConnectClient suipConnectClient = SpringBeanFactoryUtil.registerBean(MonitorSuipConnectClient.class);
            for (MonitorSuipConnectionInfo info : suipConnectionInfos) {
                timeout = info.getTimeout() < 0 ? CMNConfig.getInstance().getSuipTimeout() * 1000 : (int) info.getTimeout();
                // prepare data
                ClientNetworkStatus base = new ClientNetworkStatus();
                base.setType(MONITOR_TYPE_SUIP_NET_CLIENT); // 這裡一定要設定Type, 以便後面MonitorNetworkController在進行merge時有針對性的處理
                base.setIdentity(info.getName());
                base.setServiceHostName(StringUtils.isNotBlank(info.getHostname()) ? info.getHostname() : HOSTNAME);
                base.setServiceIP(StringUtils.isNotBlank(info.getHostip()) ? info.getHostip() : HOSTIP);
                base.setServiceName(info.getName());
                base.setLocalEndPoint(StringUtils.EMPTY);
                base.setRemoteEndPoint(StringUtils.EMPTY);
                base.setSocketCount("0");
                base.setState(NET_CLIENT_STATE_DISCONNECT);
                base.setServiceState("0");
                try {
                    // fetch via Socket
                    configuration.setHost(info.getHostip());
                    configuration.setPort(Integer.parseInt(info.getPort()));
                    // receive
                    messageIn = suipConnectClient.establishConnectionAndSendReceive(configuration, info.getCmd(), timeout);
                    if (StringUtils.isNotBlank(messageIn)) {
                        // substring(24, 2)=00代表回應成功
                        if ("00".equals(messageIn.substring(24, 24 + 2))) {
                            // 取substring 32開始至00前為止
                            // 轉成ASCII後結果如 ID=1,IpADDR=127.0.0.1,port=1500,Status=1;
                            body = StringUtil.fromHex(messageIn.substring(32, messageIn.indexOf("00", 32)));
                            TRACELogger.info("Parse response message from HSM succeed, body = [", body, "]");
                            // 1個suip可以連多台HSM, 所以;分隔不同台HSM, IpADDR及port則是該HSM的IP與Port, Status=1代表連線中, 0代表斷線
                            section = body.split(";");
                            if (ArrayUtils.isNotEmpty(section)) {
                                for (int i = 0; i < section.length; i++) {
                                    if (StringUtils.isNotBlank(section[i])) {
                                        each = section[i].split(",");
                                        if (ArrayUtils.isNotEmpty(each)) {
                                            ClientNetworkStatus clientNetworkStatus = (ClientNetworkStatus) BeanUtils.cloneBean(base);
                                            ServerNetworkStatus serverNetworkStatus = new ServerNetworkStatus();
                                            serverNetworkStatus.setServiceHostName(StringUtils.isNotBlank(info.getHostname()) ? info.getHostname() : HOSTNAME);
                                            serverNetworkStatus.setServiceState("0");
                                            serverNetworkStatus.setUpdateDateTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
                                            String remoteIp = null, remotePort = null;
                                            for (int j = 0; j < each.length; j++) {
                                                // ID
                                                if (StringUtils.startsWithIgnoreCase(each[j], SUIP_RESP_FIELD_ID)) {
                                                    clientNetworkStatus.setIdentity(StringUtils.join(clientNetworkStatus.getIdentity(), "-", each[j].substring(SUIP_RESP_FIELD_ID.length())));
                                                }
                                                // Remote IP
                                                else if (StringUtils.startsWithIgnoreCase(each[j], SUIP_RESP_FIELD_IPADDR)) {
                                                    remoteIp = each[j].substring(SUIP_RESP_FIELD_IPADDR.length());
                                                }
                                                // Remote Port
                                                else if (StringUtils.startsWithIgnoreCase(each[j], SUIP_RESP_FIELD_PORT)) {
                                                    remotePort = each[j].substring(SUIP_RESP_FIELD_PORT.length());
                                                }
                                                // Status
                                                else if (StringUtils.startsWithIgnoreCase(each[j], SUIP_RESP_FIELD_STATUS)) {
                                                    if ("1".equals(each[j].substring(SUIP_RESP_FIELD_STATUS.length()))) {
                                                        clientNetworkStatus.setState(NET_CLIENT_STATE_CONNECT);
                                                        clientNetworkStatus.setServiceState("1");
                                                        clientNetworkStatus.setSocketCount("1");
                                                        serverNetworkStatus.setServiceState("1");
                                                    }
                                                }
                                            }
                                            if (StringUtils.isNotBlank(remoteIp) && StringUtils.isNotBlank(remotePort)) {
                                                clientNetworkStatus.setRemoteEndPoint(StringUtils.join(remoteIp, ":", remotePort));
                                                serverNetworkStatus.setServiceIP(remoteIp);
                                                serverNetworkStatus.setServicePort(remotePort);
                                                serverNetworkStatus.setServiceName(StringUtils.join("HSM-", remoteIp));
                                            }
                                            clientNetworkStatusList.add(clientNetworkStatus);
                                            // 過濾掉重複的IP
                                            if (!filteredIp.contains(serverNetworkStatus.getServiceIP())) {
                                                filteredIp.add(serverNetworkStatus.getServiceIP());
                                                // 根據IP依據配置檔重新塞名字
                                                MonitorHSMInfo hsmInfo = this.monitorSchedulerJobConfig.getHsm().stream().filter(t -> t.getIp().equals(serverNetworkStatus.getServiceIP())).findFirst().orElse(null);
                                                if (hsmInfo != null) serverNetworkStatus.setServiceName(hsmInfo.getName());
                                                serverNetworkStatusList.add(serverNetworkStatus);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    TRACELogger.warn("fetchSuipHsmMonitorData suip = [", info.getHostip(), ":", info.getPort(), "], timeout = [", timeout, "] with exception occur, ", e.getMessage());
                    // 如果從suip取不到, 則添加base資料, 否則監控畫面上就缺少這筆資料
                    clientNetworkStatusList.add(base);
                }
            }
        }
        // 將suip監控的數據, 加入到List中
        suipMonitorDataList.clear();
        suipMonitorDataList.addAll(clientNetworkStatusList);
        // 將suip監控數據寫入SMS檔
        MonitorNetworkController controller = SpringBeanFactoryUtil.getBean(MonitorNetworkController.class, false);
        if (controller != null) {
            controller.sendReceiveNetClient(this.suipHsmMonitorData.getSuipMonitorClientNetworkStatus());
        }
        // 將HSM監控的數據, 加入到List中, 以供MonitorDumpJob使用
        hsmMonitorDataList.clear();
        hsmMonitorDataList.addAll(serverNetworkStatusList);
    }

    public List<ServerNetworkStatus> getHsmMonitorDataList() {
        return this.suipHsmMonitorData.getHsmMonitorServerNetworkStatus();
    }

    public void clearRemoteSuipHsmMonitorData() {
        this.suipHsmMonitorData.remoteSuip.clear();
        this.suipHsmMonitorData.remoteHsm.clear();
    }

    private class SuipHsmMonitorData {
        public final List<ClientNetworkStatus> localSuip = Collections.synchronizedList(new ArrayList<>());
        public final List<ClientNetworkStatus> remoteSuip = Collections.synchronizedList(new ArrayList<>());
        public final List<ServerNetworkStatus> localHsm = Collections.synchronizedList(new ArrayList<>());
        public final List<ServerNetworkStatus> remoteHsm = Collections.synchronizedList(new ArrayList<>());

        public List<ClientNetworkStatus> getSuipMonitorClientNetworkStatus() {
            List<ClientNetworkStatus> clientNetworkStatusList = new ArrayList<>();
            clientNetworkStatusList.addAll(localSuip);
            clientNetworkStatusList.addAll(remoteSuip);
            return clientNetworkStatusList;
        }

        public List<ServerNetworkStatus> getHsmMonitorServerNetworkStatus() {
            List<ServerNetworkStatus> serverNetworkStatusList = new ArrayList<>();
            serverNetworkStatusList.addAll(localHsm);
            serverNetworkStatusList.addAll(remoteHsm);
            return serverNetworkStatusList;
        }
    }
}
