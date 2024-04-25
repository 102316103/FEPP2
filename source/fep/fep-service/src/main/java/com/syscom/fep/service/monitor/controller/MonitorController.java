package com.syscom.fep.service.monitor.controller;

import com.google.gson.Gson;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.SmsMapper;
import com.syscom.fep.mybatis.model.Sms;
import com.syscom.fep.service.monitor.job.MonitorSchedulerJobConfig;
import com.syscom.fep.service.monitor.vo.MonitorServerInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.List;

public class MonitorController {
    private LogHelper logger = LogHelperFactory.getServiceLogger();
    @Autowired
    private SmsMapper smsMapper;

    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
    }

    @RequestMapping(value = "/api/mon/SendMessage")
    @ResponseBody
    public String sendMessage(Sms sms) {
        putMDC();
        if (sms != null) {
            logger.info("Receive App Monitor Data = [", new Gson().toJson(sms), "]");
            String smsServiceName = sms.getSmsServicename();
            String smsServiceip = sms.getSmsServiceip();
            if (StringUtils.isBlank(smsServiceName) || StringUtils.isBlank(smsServiceip)) {
                logger.error("監控資料 skip, name or ip was empty, smsServiceName = [", smsServiceName, "], smsServiceip = [", smsServiceip, "]");
                return "COMPLETE";
            }
            try {
                Sms smsServices = smsMapper.selectByPrimaryKey(sms.getSmsServicename().toUpperCase(), smsServiceip);
                if (smsServices == null) {
                    smsServices = createSms(sms.getSmsServicename().toUpperCase(), smsServiceip, sms.getSmsHostname());
                    smsMapper.insert(smsServices);
                }
                // 視為停止
                if (sms.getSmsStarttime() == null || "0".equals(sms.getSmsServicestate())) {
                    if (sms.getSmsStoptime() != null) {
                        smsServices.setSmsStoptime(sms.getSmsStoptime());
                    } else {
                        smsServices.setSmsStoptime(Calendar.getInstance().getTime());
                    }
                    smsServices.setSmsStarttime(null);
                    smsServices.setSmsServicestate("0");
                } else {
                    if (sms.getSmsStarttime() != null) {
                        smsServices.setSmsStarttime(sms.getSmsStarttime());
                    } else {
                        smsServices.setSmsStarttime(Calendar.getInstance().getTime());
                    }
                    smsServices.setSmsStoptime(null);
                    smsServices.setSmsServicestate("1");
                }
                smsServices.setSmsHostname(sms.getSmsHostname());
                smsServices.setSmsRam(sms.getSmsRam());
                smsServices.setSmsCpu(sms.getSmsCpu());
                smsServices.setSmsThreads(sms.getSmsThreads());
                smsServices.setSmsUpdatetime(Calendar.getInstance().getTime());
                smsMapper.updateByPrimaryKey(smsServices);
            } catch (Exception e) {
                logger.exceptionMsg(e, e.getMessage());
                return "COMPLETE";
            } finally {
                boolean found = false;
                // config中有, 則更新hostname
                MonitorSchedulerJobConfig monitorSchedulerJobConfig = SpringBeanFactoryUtil.getBean(MonitorSchedulerJobConfig.class);
                List<MonitorServerInfo> serviceList = monitorSchedulerJobConfig.getServices();
                if (CollectionUtils.isNotEmpty(serviceList)) {
                    MonitorServerInfo[] services = new MonitorServerInfo[serviceList.size()];
                    monitorSchedulerJobConfig.getServices().toArray(services);
                    for (MonitorServerInfo service : services) {
                        if (service.getHostip().equalsIgnoreCase(smsServiceip) && service.getName().equalsIgnoreCase(sms.getSmsServicename())) {
                            service.setHostname(sms.getSmsHostname());
                            found = true;
                            break;
                        }
                    }
                }
                // config中沒有, 則增加到config中
                if (!found) {
                    MonitorServerInfo monitorServerInfo = new MonitorServerInfo();
                    monitorServerInfo.setName(sms.getSmsServicename().toUpperCase());
                    monitorServerInfo.setHostip(smsServiceip);
                    monitorServerInfo.setHostname(sms.getSmsHostname());
                    serviceList.add(monitorServerInfo);
                }
            }
            return "SUCCESS";
        } else {
            logger.warn("監控資料 EMPTY");
            return "COMPLETE";
        }
    }


    /*
     * 初始化SMS
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
}
