package com.syscom.fep.web.controller.common;

import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.vo.monitor.MonitorConstant;
import com.syscom.fep.vo.monitor.ServiceMonitoring;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.MonitorService;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.Map;


/**
 * For System Monitor
 *
 * @author ChenYang
 */
@Controller
public class UI_080100Controller extends BaseController implements MonitorConstant {
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private SysconfExtMapper sysconfExtMapper;

    @PostMapping(value = "/common/UI_080100/inquiryMain")
    @ResponseBody
    public BaseResp<?> doMonitor() {
        this.infoMessage("開始查詢...");
        int reNewTime = WebConfiguration.getInstance().getReNewTime();
        BaseResp<ServiceMonitoring> response = new BaseResp<ServiceMonitoring>();
        try {
            ServiceMonitoring monitor = monitorService.getData();
            monitor.setReNewTime(reNewTime);
            response.setData(monitor);
            response.setMessage(MessageType.INFO, QuerySuccess);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, QueryFail);
        }
        return response;
    }

    @PostMapping(value = "/common/UI_080100/doStopNotification")
    @ResponseBody
    public BaseResp<?> doStopNotification(@RequestBody Map<String, Object> form) {
        return this.doOperation(form, "stopNotification", SYSCONF_NAME_STOPNOTIFICATION);
    }

    @PostMapping(value = "/common/UI_080100/doEnableAutoRestart")
    @ResponseBody
    public BaseResp<?> doEnableAutoRestart(@RequestBody Map<String, Object> form) {
        return this.doOperation(form, "enableAutoRestart", SYSCONF_NAME_ENABLEAUTORESTART);
    }

    private BaseResp<?> doOperation(Map<String, Object> form, String formKey, String sysconfName) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        response.setMessage(MessageType.INFO, UpdateSuccess);
        try {
            Sysconf sysconf = sysconfExtMapper.selectByPrimaryKey(SYSCONF_VALUE_CMN, sysconfName);
            if (sysconf != null) {
                // 與DB資料相同, 則不需要更新DB
                if (((String) form.get(formKey)).equalsIgnoreCase(sysconf.getSysconfValue())) {
                    this.warnMessage("The same value exists in Table, no need to Update, sysconfSubsysno = [", SYSCONF_VALUE_CMN, "], sysconfName = [", sysconfName, "], sysconfValue = [", sysconf.getSysconfValue(), "]");
                    return response;
                }
            } else {
                sysconf = new Sysconf();
            }
            sysconf.setSysconfSubsysno(SYSCONF_VALUE_CMN);
            sysconf.setSysconfName(sysconfName);
            sysconf.setSysconfValue((String) form.get(formKey));
            sysconf.setUpdateTime(Calendar.getInstance().getTime());
            sysconf.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            sysconf.setUpdateUser(sysconf.getUpdateUserid());
            sysconf.setLogAuditTrail(true);
            sysconfExtMapper.updateByPrimaryKeySelective(sysconf);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, UpdateFail);
        }
        return response;
    }
}
