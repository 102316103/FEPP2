package com.syscom.fep.web.controller.atmmon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SQLSortExpression.SQLSortOrder;
import com.syscom.fep.web.form.atmmon.UI_060550_FormDetail;
import com.syscom.fep.web.form.atmmon.UI_060550_FormMain;
import com.syscom.fep.web.form.atmmon.UI_060610_A_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.EmsService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 交易日誌(FEPTXN)查詢
 *
 * @author Richard
 */
@Controller
public class UI_060550Controller extends BaseController {
    @Autowired
    private AtmService atmService;
    @Autowired
    private EmsService emsSvr;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_060550_FormMain form = new UI_060550_FormMain();
        form.setFeptxnTbsdyFisc(this.getTbsdy(mode));
        form.setFeptxnExcludeTxCode("OEX;R3K");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    private String getTbsdy(ModelMap mode) {
        try {
            Sysstat sysstat = atmService.getStatus();
            if (sysstat != null) {
                String sysstatTbsdyFisc = sysstat.getSysstatTbsdyFisc();
                if (StringUtils.isNotBlank(sysstatTbsdyFisc)) {
                    return charDateToDate(sysstatTbsdyFisc, "-");
                }
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, "營業日期", DATA_INQUIRY_EXCEPTION_OCCUR);
        }
        return StringUtils.EMPTY;
    }

    @PostMapping(value = "/atmmon/UI_060550/inquiryMain")
    public String doInquiryMain(@ModelAttribute UI_060550_FormMain form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            if (this.doValidateForm(form, mode)) {
                // 首次按下查詢時預設的排序
                if (form.getSqlSortExpressionCount() == 0) {
                    form.addSqlSortExpression("FEPTXN_TX_DATE,FEPTXN_TX_TIME", SQLSortOrder.ASC);
                }
                // 轉成map對象供最後mybatis查詢資料使用
                Map<String, Object> argsMap = form.toMap();
                String feptxnTbsdyFisc = form.getFeptxnTbsdyFisc();
                if (StringUtils.isNotBlank(feptxnTbsdyFisc)) {
                    feptxnTbsdyFisc = StringUtils.replace(feptxnTbsdyFisc, "-", StringUtils.EMPTY);
                } else {
                    Calendar now = Calendar.getInstance();
                    feptxnTbsdyFisc = FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                    form.setFeptxnTbsdyFisc(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
                }
                String feptxnTxTimeBegin = form.getFeptxnTxTimeBegin();
                if (StringUtils.isNotBlank(feptxnTxTimeBegin)) {
                    feptxnTxTimeBegin = StringUtils.replace(feptxnTxTimeBegin, ":", StringUtils.EMPTY);
                } else {
                    feptxnTxTimeBegin = "000000";
                    form.setFeptxnTxTimeBegin("00:00:00");
                }
                String feptxnTxTimeEnd = form.getFeptxnTxTimeEnd();
                if (StringUtils.isNotBlank(feptxnTxTimeEnd)) {
                    feptxnTxTimeEnd = StringUtils.replace(feptxnTxTimeEnd, ":", StringUtils.EMPTY);
                } else {
                    feptxnTxTimeEnd = "235959";
                    form.setFeptxnTxTimeEnd("23:59:59");
                }
                String feptxnTxDate = form.getFeptxnTxDate();
                if (StringUtils.isNotBlank(feptxnTxDate)) {
                    feptxnTxDate = StringUtils.replace(feptxnTxDate, "-", StringUtils.EMPTY);
                }
                BigDecimal feptxnTxAmt = null;
                if (StringUtils.isNotBlank(form.getFeptxnTxAmt())) {
                    feptxnTxAmt = new BigDecimal(form.getFeptxnTxAmt());
                } else {
                    feptxnTxAmt = new BigDecimal("-1");
                }
                String fiscFlag = null;
                if (form.isCheckTrin() && !form.isCheckTrout()) {
                    fiscFlag = "0"; // 只查自行
                } else if (!form.isCheckTrin() && form.isCheckTrout()) {
                    fiscFlag = "1"; // 只查跨行
                } else {
                    fiscFlag = "2"; // 不限制
                }
                List<Long> feptxnEjfnoList = new ArrayList<>();
                if (StringUtils.isNotBlank(form.getFeptxnEjfno())) {
                    if (form.getFeptxnEjfno().contains(",")) {
                        String[] feptxnEjfnos = StringUtils.split(form.getFeptxnEjfno(), ",");
                        for (String feptxnEjfno : feptxnEjfnos) {
                            feptxnEjfnoList.add(Long.parseLong(feptxnEjfno));
                        }
                    } else {
                        feptxnEjfnoList.add(Long.parseLong(form.getFeptxnEjfno()));
                    }
                }
                String feptxnChannelEjfno = null;
                if (StringUtils.isNotBlank(form.getFeptxnChannelEjfno())) {
                    feptxnChannelEjfno = form.getFeptxnChannelEjfno();
                }
                List<String> feptxnExcludeTxCodeList = new ArrayList<>();
                if (form.getFeptxnExcludeTxCode().contains(";")) {
                    String[] feptxnExcludeTxCodes = StringUtils.split(form.getFeptxnExcludeTxCode(), ";");
                    for (String feptxnExcludeTxCode : feptxnExcludeTxCodes) {
                        feptxnExcludeTxCodeList.add(feptxnExcludeTxCode);
                    }
                } else {
                    feptxnExcludeTxCodeList.add(form.getFeptxnExcludeTxCode());
                }
                // 覆蓋或者增加map對象中的值
                argsMap.put("feptxnTbsdyFisc", feptxnTbsdyFisc);
                argsMap.put("feptxnTxTimeBegin", feptxnTxTimeBegin);
                argsMap.put("feptxnTxTimeEnd", feptxnTxTimeEnd);
                argsMap.put("feptxnTxDate", feptxnTxDate);
                argsMap.put("feptxnTxAmt", feptxnTxAmt);
                argsMap.put("fiscFlag", fiscFlag);
                argsMap.put("feptxnEjfno", feptxnEjfnoList);
                argsMap.put("feptxnChannelEjfno", feptxnChannelEjfno);
                argsMap.put("feptxnExcludeTxCode", feptxnExcludeTxCodeList);
                argsMap.put("tableNameSuffix", feptxnTbsdyFisc.substring(6, 8));
                argsMap.put("sqlSortExpression", form.getSqlSortExpression());
                // logSqlCommandText 2024/02/21 Richard add
                List<String> logSqlCommandText = new ArrayList<>();
                logSqlCommandText.add("營業日期=" + feptxnTbsdyFisc);
                logSqlCommandText.add("交易時間起訖=" + feptxnTxTimeBegin + "~" + feptxnTxTimeEnd);
                if (StringUtils.isNotBlank(form.getFeptxnTxDate()))
                    logSqlCommandText.add("交易日期=" + form.getFeptxnTxDate());
                if (StringUtils.isNotBlank(form.getFeptxnTxCurAct()))
                    logSqlCommandText.add("提領幣別=" + form.getFeptxnTxCurAct());
                if (StringUtils.isNotBlank(form.getFeptxnTxAmt()))
                    logSqlCommandText.add("交易金額=" + form.getFeptxnTxAmt());
                if (StringUtils.isNotBlank(form.getFeptxnTroutBkno()) || StringUtils.isNotBlank(form.getFeptxnTroutActno()))
                    logSqlCommandText.add("扣款帳號=" + form.getFeptxnTroutBkno() + form.getFeptxnTroutActno());
                if (StringUtils.isNotBlank(form.getFeptxnTrinBkno()) || StringUtils.isNotBlank(form.getFeptxnTrinActno()))
                    logSqlCommandText.add("轉入帳號=" + form.getFeptxnTrinBkno() + form.getFeptxnTrinActno());
                if ("0".equals(fiscFlag)) {
                    logSqlCommandText.add("自行or跨行=自行");
                } else if ("1".equals(fiscFlag)) {
                    logSqlCommandText.add("自行or跨行=跨行");
                }
                if (StringUtils.isNotBlank(form.getFeptxnExcludeTxCode()))
                    logSqlCommandText.add("排除交易別=" + form.getFeptxnExcludeTxCode());
                if (StringUtils.isNotBlank(form.getFeptxnTxrust()))
                    logSqlCommandText.add("交易結果=" + form.getFeptxnTxrust());
                if (StringUtils.isNotBlank(form.getFeptxnAtmno()))
                    logSqlCommandText.add("ATM代號=" + form.getFeptxnAtmno());
                if (StringUtils.isNotBlank(form.getFeptxnCbsRrn()))
                    logSqlCommandText.add("發卡區T24序號=" + form.getFeptxnCbsRrn());
                // if (StringUtils.isNotBlank(form.getFeptxnVirCbsRrn()))
                //     logSqlCommandText.add("交易區T24序號=" + form.getFeptxnVirCbsRrn());
                if (StringUtils.isNotBlank(form.getFeptxnAtmSeqno()))
                    logSqlCommandText.add("ATM交易序號=" + form.getFeptxnAtmSeqno());
                if (StringUtils.isNotBlank(form.getFeptxnConAtmSeqno2()))
                    logSqlCommandText.add("ATM CON交易序號=" + form.getFeptxnConAtmSeqno2());
                if (StringUtils.isNotBlank(form.getFeptxnEjfno()))
                    logSqlCommandText.add("EJ序號=" + form.getFeptxnEjfno());
                if (StringUtils.isNotBlank(form.getFeptxnTraceEjfno()))
                    logSqlCommandText.add("CON EJ序號=" + form.getFeptxnTraceEjfno());
                if (StringUtils.isNotBlank(form.getFeptxnChannelEjfno()))
                    logSqlCommandText.add("ClientTraceId" + form.getFeptxnChannelEjfno());
                if (StringUtils.isNotBlank(form.getFeptxnTxCode()))
                    logSqlCommandText.add("ATM交易代號=" + form.getFeptxnTxCode());
                if (StringUtils.isNotBlank(form.getFeptxnBkno()) || StringUtils.isNotBlank(form.getFeptxnStan()))
                    logSqlCommandText.add("財金 STAN=" + form.getFeptxnBkno() + form.getFeptxnStan());
                if (StringUtils.isNotBlank(form.getFeptxnPcode()))
                    logSqlCommandText.add("財金交易代號(PCODE)=" + form.getFeptxnPcode());
                if (StringUtils.isNotBlank(form.getFeptxnAccType()))
                    logSqlCommandText.add("記帳類別=" + form.getFeptxnAccType());
                if (StringUtils.isNotBlank(form.getFeptxnTroutBkno()) || StringUtils.isNotBlank(form.getFeptxnMajorActno()))
                    logSqlCommandText.add("卡片帳號=" + form.getFeptxnTroutBkno() + form.getFeptxnMajorActno());
                if (StringUtils.isNotBlank(form.getFeptxnMsgid()))
                    logSqlCommandText.add("訊息代號=" + form.getFeptxnMsgid());
                sqlCommandText.set(logSqlCommandText);
                PageInfo<Feptxn> pageInfo = atmService.getFeptxn(argsMap);
                if (pageInfo.getSize() == 0) {
                    this.showMessage(mode, MessageType.INFO, QueryNoData);
                }
                PageData<UI_060550_FormMain, Feptxn> pageData = new PageData<UI_060550_FormMain, Feptxn>(pageInfo, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
                mode.addAttribute("totalCount", pageInfo.getTotal());
                // 查詢總金額
                Map<String, Object> summary = atmService.getFeptxnSummary(argsMap);
                BigDecimal sumOfFeptxnTxAmt = new BigDecimal("0");
                if (MapUtils.isNotEmpty(summary)) {
                    sumOfFeptxnTxAmt = DbHelper.getMapValue(summary, "FEPTXN_TX_AMT", sumOfFeptxnTxAmt);
                }
                mode.addAttribute("sumOfFeptxnTxAmt", sumOfFeptxnTxAmt);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060550.getView();
    }

    /**
     * 檢核表單
     *
     * @param form
     * @param mode
     * @return
     */
    private boolean doValidateForm(UI_060550_FormMain form, ModelMap mode) {
        int cnt = 0;
        if (StringUtils.isNotBlank(form.getFeptxnEjfno())) {
            char[] chars = form.getFeptxnEjfno().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (!Character.isDigit(chars[i]) && chars[i] != ',') {
                    this.showMessage(mode, MessageType.DANGER, EJFNOComma);
                    return false;
                }
            }
        }
        if (!form.isCheckTrin() && !form.isCheckTrout()) {
            form.setCheckTrin(true);
            form.setCheckTrout(true);
        }
        if ((form.isCheckTrin() && !form.isCheckTrout())
                || (!form.isCheckTrin() && form.isCheckTrout())
                || (form.isCheckTrin() && form.isCheckTrout())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTbsdyFisc())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxDate())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxrust())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnAtmno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnCbsRrn())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnVirCbsRrn())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTroutBkno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTroutActno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTrinBkno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTrinActno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnEjfno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxCode())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnTxAmt())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnBkno())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnStan())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnPcode())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnAccType())) {
            cnt += 1;
        }
        if (StringUtils.isNotBlank(form.getFeptxnMsgid())) {
            cnt += 1;
        }
        if (cnt < 2) {
            this.showMessage(mode, MessageType.DANGER, QueryConditionCnt);
            return false;
        }
        return true;
    }

    @PostMapping(value = "/atmmon/UI_060550/inquiryDetail")
    public String doInquiryDetail(@ModelAttribute UI_060550_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        // RM
        if (form.getFeptxnSubsys() != null && form.getFeptxnSubsys() == '2') {
            // TODO
            this.showMessage(mode, MessageType.INFO, "FEPTXN_SUBSYS == '2', 功能尚未實作!!!");
            return Router.UI_060550.getView();
        } else {
            return doInquiryDetailForOtherSubSys(form, mode);
        }
    }

    /**
     * @param form
     * @param mode
     * @return
     */
    private String doInquiryDetailForOtherSubSys(UI_060550_FormDetail form, ModelMap mode) {
        try {
            String tableNameSuffix = StringUtils.EMPTY;
            if (StringUtils.isNotBlank(form.getFeptxnTbsdyFisc()) && form.getFeptxnTbsdyFisc().length() == 8) {
                tableNameSuffix = form.getFeptxnTbsdyFisc().substring(6, 8);
            }
            Map<String, Object> detail = atmService.getFeptxnIntltxn(tableNameSuffix, form.getFeptxnEjfno(), form.getFeptxnTxDate());
            // 2010-10-14 by kyo for 跨區交易時營業日會使用卡片地區營業日
            if (MapUtils.isEmpty(detail)) {
                // 取出查詢主頁資料時候的表單資料
                UI_060550_FormMain formMain = (UI_060550_FormMain) WebUtil.getUser().getPrevPageForm();
                String feptxnTbsdyFisc = formMain.getFeptxnTbsdyFisc();
                if (StringUtils.isNotBlank(feptxnTbsdyFisc)) {
                    feptxnTbsdyFisc = StringUtils.replace(feptxnTbsdyFisc, "-", StringUtils.EMPTY);
                } else {
                    Calendar now = Calendar.getInstance();
                    feptxnTbsdyFisc = FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                    formMain.setFeptxnTbsdyFisc(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
                }
                if (StringUtils.isNotBlank(feptxnTbsdyFisc) && feptxnTbsdyFisc.length() == 8) {
                    tableNameSuffix = feptxnTbsdyFisc.substring(6, 8);
                }
                detail = atmService.getFeptxnIntltxn(tableNameSuffix, form.getFeptxnEjfno(), form.getFeptxnTxDate());
            }
            if (MapUtils.isEmpty(detail)) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            // 應該只會有一筆資料
            WebUtil.putInAttribute(mode, AttributeName.DetailMap, detail);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060550_Detail.getView();
    }

    @PostMapping(value = "/atmmon/UI_060550/inquiryFeplogList")
    public String doInquiryFeplog(@ModelAttribute UI_060610_A_FormMain form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        if (form.getFeptxnSubsys() != null && form.getFeptxnSubsys() == '2') {
            // TODO
            this.showMessage(mode, MessageType.INFO, "FEPTXN_SUBSYS == '2', 功能尚未實作!!!");
            return Router.UI_060550_Detail.getView();
        } else {
            return doInquiryFeplogForOtherSubSys(form, mode);
        }
    }

    private String doInquiryFeplogForOtherSubSys(UI_060610_A_FormMain form, ModelMap mode) {
        this.changeTitle(mode, Router.UI_060610_A.getName());
        try {
            List<Long> ejfnoList = new ArrayList<>();
            if (form.getFeptxnEjfno() != null) {
                ejfnoList.add(form.getFeptxnEjfno());
            }
            if (form.getEjfnO1() != null) {
                ejfnoList.add(form.getEjfnO1());
            }
            if (form.getEjfnO2() != null) {
                ejfnoList.add(form.getEjfnO2());
            }
            if (form.getEjfnO3() != null) {
                ejfnoList.add(form.getEjfnO3());
            }
            if (form.getEjfnO4() != null) {
                ejfnoList.add(form.getEjfnO4());
            }
            if (form.getEjfnO5() != null) {
                ejfnoList.add(form.getEjfnO5());
            }
            PageInfo<Feplog> pageInfo = emsSvr.getFeplog_UI060550(ejfnoList, form.getFeptxnTraceEjfno(), form.getFeptxnTxDate(), form.getPageNum(), form.getPageSize());
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_060610_A_FormMain, Feplog> pageData = new PageData<UI_060610_A_FormMain, Feplog>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060610_A.getView();
    }
}
