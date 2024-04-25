package com.syscom.fep.web.controller.dbmaintain;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.ref.RefLong;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Cbspend;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.dbmaintain.UI_070050_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.*;

/**
 * 跨行系統參數維護
 *
 * @author Joseph
 * @create 2022/05/23
 */
@Controller
public class UI_070050Controller extends BaseController {

    @Autowired
    private InbkService inbkService;

    @SuppressWarnings("unused")
    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_070050_Form form = new UI_070050_Form();
        form.setUrl("/dbmaintain/UI_070050/queryClick");
        form.setCbspendTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
        this.queryClick(form, mode);
    }

    @PostMapping(value = "/dbmaintain/UI_070050/queryClick")
    private String queryClick(@ModelAttribute UI_070050_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        RefLong count = new RefLong(0);
        BindGridData(form, mode, count);
        return Router.UI_070050.getView();
    }

    @PostMapping(value = "/dbmaintain/UI_070050/doAllModify")
    @ResponseBody
    public BaseResp<UI_070050_Form> updateClick(@RequestBody UI_070050_Form form, @ModelAttribute ModelMap mode) throws Exception {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        BaseResp<UI_070050_Form> response = new BaseResp<>();
        try {
            int count;
            String txdate = StringUtils.replace((form.getCbspendTxDate()), "-", StringUtils.EMPTY);
            String zone = form.getCbspendZone();
            PageInfo<HashMap<String, Object>> dt = null;
            dt = PageHelper
                    .startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0)
                    .doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                                inbkService.GetCBSPENDByTXDATE(txdate, form.getCbspendSuccessFlag(),
                                        form.getCbspendSubsys(), form.getCbspendZone(), form.getCbspendCbsTxCode());
                        }
                    });
            count = (int) dt.getTotal();
            String tbsdy = "";
            if (count == 0) {
                response.setMessage(MessageType.WARNING, "請先做查詢再執行");
                return response;
            }
//   20230322 Bruce 先註解掉 TODO
//            Zone zonee = new Zone();
//            zonee = atmservice.getDataByZonee(zone);
//            if (zonee == null) {
//                response.setMessage(MessageType.WARNING, "查不到ZONE檔的自行營業日");
//                return response;
//            } else {
//                tbsdy = zonee.getZoneTbsdy();
//            }
            Integer fepCnt = 0;
            String isWeb = WebConfiguration.getInstance().getIsFEPWebOrRMWeb();
            String RemoteMQServiceFlag = WebConfiguration.getInstance().getRemoteMQServiceFlag();
            String serverIP = WebUtil.getServerHostIp();
            if ("FEPWeb".equals(isWeb)) {
                qList(txdate, zone, tbsdy, "", fepCnt, serverIP);
                if ("ON".equals(RemoteMQServiceFlag)) {
                    serverIP = WebConfiguration.getInstance().getRemoteMQServiceIP();
                    qList(txdate, zone, tbsdy, "2", fepCnt, serverIP);
                }
            } else {
                qList(txdate, zone, tbsdy, "2", fepCnt, serverIP);
                if ("ON".equals(RemoteMQServiceFlag)) {
                    serverIP = WebConfiguration.getInstance().getRemoteMQServiceIP();
                    qList(txdate, zone, tbsdy, "", fepCnt, serverIP);
                }
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
        return response;
    }

    private Boolean SendToFEPMQService(List<String> qList, String serverIP) {
        try {
            String qData = "=";
            int i = 0;
            for (i = 0; i <= qList.size() - 1; i++) {
                if (i == qList.size() - 1) {
                    qData = qData + qList.get(i);
                } else {
                    qData = qData + qList.get(i) + "|";
                }
            }
            String port = WebConfiguration.getInstance().getMQServicePort();
//            String isWeb = WebConfiguration.getInstance().getIsFEPWebOrRMWeb();
            String requestURL = WebConfiguration.getInstance().getMqServiceRequest();
            String url = "http://" + serverIP + ":" + port + requestURL + "/AddCBSTimeoutRerunMessage";
            logContext.setMessage("UI070050");
            logContext.setRemark("SendTo [" + serverIP + "] MQService After GetRequestStream, 寫入CBSPEND Queue清單 [" + qData + "]");
            inbkService.inbkLogMessage(Level.INFO, logContext);
            HttpClient httpClient = new HttpClient();
            String request = httpClient.doPost(url, MediaType.APPLICATION_FORM_URLENCODED, qData);
            logContext.setRemark("SendTo [" + serverIP + "] MQService After GetRequestStream=" + request);
            inbkService.inbkLogMessage(Level.INFO, logContext);

            return true;
        } catch (Exception e) {
            logContext.setMessage("UI070050");
            logContext.setRemark("SendTo [" + serverIP + "] MQService 發生Exception:" + e);
        }
        return null;

    }

    private Boolean qList(String txdate, String zone, String tbsdy, String subsys, Integer processCnt, String serverIP) {
        try {
            logContext.setMessage("UI070050");
            List<Map<String, Object>> dt = null;
            List<String> ejList = new LinkedList<>();
            dt = inbkService.SelectResendCNT(txdate, zone, tbsdy, subsys);
            if (null != dt && dt.size() > 0) {
                for (Map<String, Object> drv : dt) {
                    try {

                        Integer ej = ((Integer) drv.get("CBSPEND_EJFNO")).intValue();
                        BigDecimal ejno = BigDecimal.valueOf(ej);
                        if (inbkService.UpdateResendCNT(drv.get("CBSPEND_TX_DATE").toString(), ejno, zone, tbsdy, subsys) > 0) {
                            processCnt += 1;
                            ejList.add(drv.get("CBSPEND_TX_DATE").toString() + ":" + drv.get("CBSPEND_EJFNO").toString());
                            if (ejList.size() >= 100) {
                                SendToFEPMQService(ejList, serverIP);
                                ejList.clear();
                            }
                        } else {
                            logContext.setRemark("全部修改-更新不到EJ=[" + drv.get("CBSPEND_EJFNO").toString() + "]");
                            inbkService.inbkLogMessage(Level.ERROR, logContext);
                        }
                    } catch (Exception e) {
                        logContext.setRemark("全部修改-EJ=[" + drv.get("CBSPEND_EJFNO").toString() + "]更新Queue發生錯誤：" + e);
                        inbkService.inbkLogMessage(Level.ERROR, logContext);
                    }
                }
                if (ejList.size() > 0) {
                    SendToFEPMQService(ejList, serverIP);
                    ejList.clear();
                }
            } else {
                logContext.setRemark("查無可修改重送次數為0的資料");
                inbkService.inbkLogMessage(Level.ERROR, logContext);
            }
        } catch (Exception e) {
            logContext.setRemark("SendToFEPMQService 發生Exception:" + e);
            inbkService.inbkLogMessage(Level.ERROR, logContext);
            this.errorMessage(e, e.getMessage());
        }
        return false;
    }

    @PostMapping(value = "/dbmaintain/UI_070050/doModify")
    @ResponseBody
    public BaseResp<UI_070050_Form> doModify(@RequestBody UI_070050_Form form, @ModelAttribute ModelMap mode) {
        this.infoMessage("執行修改動作, 條件 = [", form.toString(), "]");
        BaseResp<UI_070050_Form> response = new BaseResp<>();
        try {
            Short subsys = form.getCbspendSubsys();
//            Zone dtZone = new Zone();
//            dtZone = atmservice.GetDataByZonee(Zone);
            String tbsdy = StringUtils.replace(form.getCbspendTbsdy(), "/", StringUtils.EMPTY);
            if (subsys != 2) {
                if (form.getCbspendResendCnt() != (short) 0) {
                    response.setMessage(MessageType.INFO, "重送次數只能輸入0");
                    return response;
                }
//                } else if (z < Integer.parseInt(dtZone.getZoneTbsdy())) {
//                    si = (short) 5;
//                    form.setCbspendResendCnt(si);
//                    response.setMessage(MessageType.INFO, "自行營業日已轉換，不得重送");
//                    return response;
//                }
            }
            String date = StringUtils.replace(form.getCbspendTxDate(), "-", StringUtils.EMPTY);
            Cbspend cbspend = new Cbspend();
            cbspend.setCbspendTxDate(date);
            cbspend.setCbspendEjfno(form.getCbspendEjfno());
            cbspend.setCbspendResendCnt(form.getCbspendResendCnt());
            cbspend.setUpdateUserid(WebUtil.getFepuser().getFepuserUserid());
            int iRes = inbkService.UpdateCBSPEND(cbspend);
            boolean sendQueueFlag = false;
            if (iRes == 1) {
                boolean btntmp = false;
                boolean txtt = false;
                form.setBtntmp(btntmp);
                form.setTxtt(txtt);
                if (subsys != 2) {
                    String qBody = cbspend.getCbspendTxDate() + ":" + cbspend.getCbspendEjfno();
                    try {
                        logContext.setMessage("UI_070050");
                        logContext.setRemark("單筆-[" + qBody + "]送Queue成功");
                        inbkService.inbkLogMessage(Level.INFO, logContext);
                        sendQueueFlag = true;
                    } catch (Exception exception) {
                        sendQueueFlag = false;
                        logContext.setMessage("UI_070050");
                        logContext.setRemark("[" + qBody + "]送Queue發生錯誤：" + exception);
                        inbkService.inbkLogMessage(Level.INFO, logContext);
                    }
                    if (sendQueueFlag == false) {
                        short sa = 5;
                        cbspend.setCbspendResendCnt(sa);
                        int IRes = inbkService.UpdateCBSPEND(cbspend);
                        if (IRes == 1) {
                            form.setCbspendResendCnt(sa);
                            btntmp = true;
                            txtt = true;
                            response.setMessage(MessageType.INFO, UpdateFail + "(送Queue發生錯誤)");
                        } else {
                            response.setMessage(MessageType.INFO, UpdateFail + "(送Queue發生錯誤且還原重送次數=5失敗)");
                        }
                    } else {
                        response.setMessage(MessageType.INFO, UpdateSuccess);
                    }
                } else {
                    response.setMessage(MessageType.INFO, UpdateSuccess);
                }
            } else {
                response.setMessage(MessageType.INFO, UpdateFail);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, UpdateFail);
        }
        return response;
    }

    public void BindGridData(UI_070050_Form form, ModelMap mode, RefLong count) {
        PageInfo<HashMap<String, Object>> dt = null;
        try {
            if (StringUtils.isNotBlank(form.getCbspendCbsTxCode())) {
                if (!"A".equals(form.getCbspendCbsTxCode().substring(0, 1))) {
                    this.showMessage(mode, MessageType.WARNING, "主機交易代號-只能輸入A類交易");
                }
            }
            String txdate = StringUtils.replace((form.getCbspendTxDate()), "-", StringUtils.EMPTY);
            BigDecimal summary = inbkService.getCbspendSummary(txdate, form.getCbspendSuccessFlag(),
                    form.getCbspendSubsys(), form.getCbspendZone(), form.getCbspendCbsTxCode());
            dt = PageHelper
                    .startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0)
                    .doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                                inbkService.GetCBSPENDByTXDATE(txdate, form.getCbspendSuccessFlag(),
                                        form.getCbspendSubsys(), form.getCbspendZone(), form.getCbspendCbsTxCode());
                        }
                    });
            List<HashMap<String, Object>> dataList = dt.getList();
//            Cbspend cbspend = new Cbspend();
            count.set(dt.getTotal());
            form.setTotalCNT(dt.getTotal());
            form.setTotalAMT(summary);
            if (null != dataList && dataList.size() > 0) {
                for (Map<String, Object> drv : dataList) {
                    drv.put("btntmp", false);
                    drv.put("txtt", false);
                    int txt = ((BigDecimal) drv.get("CBSPEND_RESEND_CNT")).intValue();
                    String lbl = drv.get("CBSPEND_SUCCESS_FLAG").toString();
                    if (txt >= 5 && "0".equals(lbl)) {
                        drv.put("btntmp", true);
                        drv.put("txtt", true);
                    }
                }
                PageData<UI_070050_Form, HashMap<String, Object>> pageData = new PageData<>(dt, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            }
        } catch (Exception exception) {
            this.errorMessage(exception, exception.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
