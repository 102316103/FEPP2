package com.syscom.fep.gateway.netty.fisc.ctrl;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayCmdAction;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 透過Restful接收訊息下指令給FISCGateway
 *
 * @author Richard
 */
public class FISCGatewayClientRestfulCtrl extends FISCGatewayClientCtrl {
    /**
     * 獲取GW的Monitor資料
     * <p>
     * curl -d "action=get" -X POST http://localhost:8301/recv/fisc/monitor
     *
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/fisc/monitor", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageMonitor(@RequestParam(value = "action", required = false, defaultValue = StringUtils.EMPTY) String action) {
        putMDC();
        switch (action) {
            case "get":
                return this.getMonitorData();
            default:
                return this.onMessageMonitor("get");
        }
    }

    /**
     * 操控GW
     * <p>
     * curl -d "mode=primary&action=start" -X POST http://localhost:8301/recv/fisc/operate
     * <p>
     * curl -d "mode=primary&action=stop" -X POST http://localhost:8301/recv/fisc/operate
     * <p>
     * curl -d "mode=secondary&action=start" -X POST http://localhost:8301/recv/fisc/operate
     * <p>
     * curl -d "mode=secondary&action=stop" -X POST http://localhost:8301/recv/fisc/operate
     * <p>
     * curl -d "action=check" -X POST http://localhost:8301/recv/fisc/operate
     *
     * @param mode
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/fisc/operate", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageOperateGateway(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "mode", required = false, defaultValue = "all") FISCGatewayMode mode,
            @RequestParam(value = "action", required = true) FISCGatewayCmdAction action) {
        putMDC();
        switch (action) {
            case start:
            case stop:
                return this.doOperateGateway(mode, action);
            case check:
                return this.checkStatus();
            default:
                return StringUtils.join("Incorrect parameter action = \"", action, "\"");
        }
    }

    /**
     * 停止FISCGW
     * <p>
     * curl -X POST http://localhost:8301/recv/fisc/terminate
     *
     * @return
     */
    @RequestMapping(value = "/recv/fisc/terminate", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageTerminate(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.clear();
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageTerminate"));
        logData.setRemark(StringUtils.join("Stop FISCGateway, operator:", operator));
        this.logMessage(logData);
        // 延時終止程序, 確保有回應OK
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            System.exit(0);
        }, 5, TimeUnit.SECONDS);
        return Const.REPLY_OK;
    }
}
