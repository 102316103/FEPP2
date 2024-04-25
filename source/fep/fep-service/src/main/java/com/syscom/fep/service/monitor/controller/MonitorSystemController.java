package com.syscom.fep.service.monitor.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.monitor.MonitorData;
import com.syscom.fep.common.monitor.MonitorDataDisk;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.parse.GsonParser;
import com.syscom.fep.mybatis.mapper.SmsMapper;
import com.syscom.fep.mybatis.model.Sms;
import com.syscom.fep.service.monitor.svr.MonitorSchedulerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.List;

public class MonitorSystemController {
    private final LogHelper logger = LogHelperFactory.getServiceLogger();
    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private MonitorSchedulerService monitorSchedulerService;

    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
    }

    @RequestMapping(value = "/recv/mon/system", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String sendReceiveSystemMonitor(@RequestBody MonitorData system) {
        putMDC();
        if (system != null) {
            logger.info("Receive System Monitor Data = [", new Gson().toJson(system), "]");
            String smsServiceName = system.getSmsServicename();
            String smsServiceip = system.getSmsServiceip();
            if (StringUtils.isBlank(smsServiceName) || StringUtils.isBlank(smsServiceip)) {
                logger.error("系統監控數據 skip, name or ip was empty, smsServiceName = [", smsServiceName, "], smsServiceip = [", smsServiceip, "]");
                return "COMPLETE";
            }
            try {
                // 用來存儲磁盤信息
                String smsOthers = StringUtils.EMPTY;
                if (StringUtils.isNotBlank(system.getSmsOthers())) {
                    JSONArray disArray = new JSONArray();
                    try {
                        GsonParser<List<MonitorDataDisk>> parser = new GsonParser<>(new TypeToken<List<MonitorDataDisk>>() {}.getType());
                        List<MonitorDataDisk> monitorDataDiskList = parser.readIn(system.getSmsOthers());
                        if (CollectionUtils.isNotEmpty(monitorDataDiskList)) {
                            for (MonitorDataDisk monitorDataDisk : monitorDataDiskList) {
                                // 這裡要呼叫analyseDiskData方法, 需要判斷磁盤大小並送Alert郵件
                                disArray.put(monitorSchedulerService.analyseDiskData(monitorDataDisk));
                            }
                        }
                    } catch (Exception e) {
                        logger.exceptionMsg(e, "Parse System Disk Json Data with exception occur, ", e.getMessage());
                    }
                    if (!disArray.isEmpty()) {
                        smsOthers = disArray.toString();
                    }
                }
                Sms smsServices = smsMapper.selectByPrimaryKey(system.getSmsServicename().toUpperCase(), smsServiceip);
                boolean insert = false;
                if (smsServices == null) {
                    smsServices = new Sms();
                    insert = true;
                }
                BeanUtils.copyProperties(system, smsServices);
                smsServices.setSmsOthers(smsOthers);
                smsServices.setSmsUpdatetime(Calendar.getInstance().getTime());
                if (insert) {
                    smsMapper.insert(smsServices);
                } else {
                    smsMapper.updateByPrimaryKeyWithBLOBs(smsServices);
                }
            } catch (Exception e) {
                logger.exceptionMsg(e, e.getMessage());
                return "COMPLETE";
            }
            return "SUCCESS";
        } else {
            logger.warn("系統監控數據 EMPTY");
            return "COMPLETE";
        }
    }
}
