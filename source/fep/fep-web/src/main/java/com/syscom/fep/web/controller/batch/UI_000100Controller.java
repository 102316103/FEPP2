package com.syscom.fep.web.controller.batch;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.configurer.BatchBaseConfiguration;
import com.syscom.fep.batch.base.configurer.BatchBaseConfigurationHost;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Jobs;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.entity.batch.MaintainBatch;
import com.syscom.fep.web.form.batch.UI_000100_Detail_Form;
import com.syscom.fep.web.form.batch.UI_000100_Form;
import com.syscom.fep.web.form.batch.UI_000100_Main_Form;
import com.syscom.fep.web.form.batch.UI_000100_Task_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static javax.swing.SortOrder.ASCENDING;

/**
 * For System Monitor
 *
 * @author ZK
 */
@Controller
public class UI_000100Controller extends BaseController {
    private static final String URL_DO_QUERY = "/batch/UI_000100/queryClick";
    private static final Integer EMPTY_BATCH_ID = -1;
    private static final String DEFAULT_SORT_COLUMN = "BATCH_NAME";
    private static final int MAX_LOOPS = Integer.MAX_VALUE; // 2024-04-24 Richard add add for 【Unchecked Input for Loop Condition】
    @Autowired
    private BatchService obj;
    @Autowired
    private BatchBaseConfiguration batchBaseConfiguration;
    @Autowired
    private CommonService commonService;
    @Autowired
    private AtmService atmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        MaintainBatch maintainBatch = new MaintainBatch();
        WebUtil.putInSession(SessionKey.TemporaryRestoreData, maintainBatch);
        UI_000100_Form form = new UI_000100_Form();
        form.setUrl(URL_DO_QUERY);
        // 一載入就Query
        // Modify By Matt 2010/06/08
        this.queryClick(form, mode);
    }

    private void bindGrid(UI_000100_Form form, ModelMap mode, RefBase<PageInfo<HashMap<String, Object>>> dt, String sortExpression, String direction) {
        try {
            // if (dt == null) {
            dt = new RefBase<>(new PageInfo<>());
            dt.set(getResultData(form, mode));
            // }
            if (dt.get().getList().isEmpty()) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
            }
            WebUtil.putInAttribute(mode, AttributeName.PageData, dt.get());
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, QueryFail);
        }
    }

    private PageInfo<HashMap<String, Object>> getResultData(UI_000100_Form form, ModelMap mode) {
        PageInfo<HashMap<String, Object>> dt = null;
        try {
            dt = obj.getAllBatch(form.getBatchName(), WebConfiguration.getInstance().getSubsysList(), form.getPageNum(), form.getPageSize());
            return dt;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, QueryFail);
            return null;
        }
    }

    @PostMapping(value = URL_DO_QUERY, produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_000100_Form form, ModelMap mode) {
        this.infoMessage("查詢資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        MaintainBatch maintainBatch = this.getSessionData();
        maintainBatch.setBatchName(form.getBatchName());
        maintainBatch.getTasks().clear();
        this.bindGrid(form, mode, null, DEFAULT_SORT_COLUMN, ASCENDING.name());
        return Router.UI_000100.getView();
    }

    @PostMapping(value = "/batch/UI_000100/queryDetails", produces = "application/json;charset=utf-8")
    public String queryDetails(@ModelAttribute UI_000100_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        MaintainBatch maintainBatch = this.getSessionData();
        if (StringUtils.isBlank(form.getBatchId())) {
            // 2024-04-12 Richard modified 按下新增後也可以設定排程
            // maintainBatch.setDetail("insert");
            maintainBatch.setDetail("update");
            maintainBatch.setBatch(new Batch());
            maintainBatch.getBatch().setBatchBatchid(EMPTY_BATCH_ID);
            maintainBatch.getBatch().setBatchEnable((short) 1);
            maintainBatch.getBatch().setBatchSubsys((short) 0);
            maintainBatch.getBatch().setBatchCheckbusinessdate((short) 0);
            // 初始化通知方式
            this.resetNotify(maintainBatch.getBatch());
            // 初始化是否排程
            this.resetScheduleData(maintainBatch.getBatch());
        } else {
            maintainBatch.setDetail("update");
            maintainBatch.setBatch(obj.getBatchByID(Integer.parseInt(form.getBatchId())));
        }
        List<SelectOption<String>> options = new ArrayList<>();
        List<String> apHostNameList = batchBaseConfiguration.getHost().stream().map(BatchBaseConfigurationHost::getName).collect(Collectors.toList());
        options.add(new SelectOption<String>(" ", StringUtils.EMPTY));
        for (String apHostName : apHostNameList) {
            options.add(new SelectOption<String>(apHostName, apHostName));
        }
        mode.addAttribute("options", options);
        List<SelectOption<String>> zones = new ArrayList<>();
        List<String> zonesList = this.commonService.getAreaCode().stream().map(Zone::getZoneCode).collect(Collectors.toList());
        zones.add(new SelectOption<String>(" ", StringUtils.EMPTY));
        for (String zoneCode : zonesList) {
            zones.add(new SelectOption<String>(getZoneName(zoneCode), zoneCode));
        }
        mode.addAttribute("zones", zones);
        return Router.UI_000100_Detail.getView();
    }

    @PostMapping(value = "/batch/UI_000100/delClick", produces = "application/json;charset=utf-8")
    public String delClick(String[] delChecks, ModelMap mode) {
        this.infoMessage("刪除資料, 條件 = [", StringUtils.join(delChecks, ','), "]");
        UI_000100_Form form = new UI_000100_Form();
        form.setBatchName(this.getSessionData().getBatchName());
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        try {
            int iFaultCount = 0;
            StringBuilder sbFaultPK = new StringBuilder();
            int iRes = 0;
            if (delChecks != null) {
                for (String gr : delChecks) {
                    iRes = obj.deleteBatch(Integer.parseInt(gr));
                    if (iRes != 1) {
                        iFaultCount = iFaultCount + 1;
                        sbFaultPK.append(gr + ", ");
                    }
                }
            } else {
                this.bindGrid(form, mode, null, DEFAULT_SORT_COLUMN, ASCENDING.name());
                this.showMessage(mode, MessageType.WARNING, "請選擇批次");
                return Router.UI_000100.getView();
            }
            // 更新畫面
            this.bindGrid(form, mode, null, DEFAULT_SORT_COLUMN, ASCENDING.name());
            // 回應訊息
            if (iFaultCount == 0) {
                this.showMessage(mode, MessageType.INFO, DeleteSuccess);
            } else {
                this.showMessage(mode, MessageType.DANGER, DeleteFail);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_000100.getView();
    }

    @PostMapping(value = "/batch/UI_000100/details")
    @ResponseBody
    public UI_000100_Detail_Form details() {
        this.infoMessage("查看明細資料");
        MaintainBatch maintainBatch = this.getSessionData();
        maintainBatch.getTasks().clear(); // 在新增模式下按下放棄按鈕需要清空
        UI_000100_Detail_Form form = new UI_000100_Detail_Form();
        try {
            form.setDetail(maintainBatch.getDetail());
            form.setSubsys(obj.getSubsysAll());
            // 設置可啓動群組
            form.setStartGroup(this.bindGroupListBox());
            if ("update".equals(maintainBatch.getDetail())) {
                List<HashMap<String, String>> mList = new ArrayList<>();
                for (int i = 1; i < 13; i++) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("value", ((int) Math.pow(2, i - 1)) + StringUtils.EMPTY);
                    hashMap.put("name", i + "月");
                    mList.add(hashMap);
                }
                List<HashMap<String, String>> mdList = new ArrayList<>();
                for (int i = 1; i < 32; i++) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("value", i + StringUtils.EMPTY);
                    hashMap.put("name", i + "日");
                    mdList.add(hashMap);
                }
                if (maintainBatch.getBatch().getBatchScheduleStarttime() != null) {
                    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat time = new SimpleDateFormat("HH:mm");
                    form.setDate(date.format(maintainBatch.getBatch().getBatchScheduleStarttime()));
                    form.setTime(time.format(maintainBatch.getBatch().getBatchScheduleStarttime()));
                }
                form.setTaskList(obj.getTaskAll());
                form.setTasks(bindJobGrid());
                if (StringUtils.isNotBlank(maintainBatch.getBatch().getBatchScheduleType()) && maintainBatch.getBatch().getBatchScheduleType().equals("O")) {
                    form.setRadioType("mw");
                } else {
                    form.setRadioType("m");
                }
                form.setMdList(mdList);
                form.setmList(mList);
                form.setBatch(maintainBatch.getBatch());
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            form.setMessage(MessageType.DANGER, QueryFail);
        }
        return form;
    }

    private List<HashMap<String, String>> bindGroupListBox() throws Exception {
        List<SyscomroleAndCulture> roleList = commonService.getAllRoles();
        List<HashMap<String, String>> list = new ArrayList<>();
        for (SyscomroleAndCulture role : roleList) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", role.getRoleno() + "-" + role.getRolename());
            hashMap.put("value", Integer.toString(role.getRoleid()));
            list.add(hashMap);
        }
        return list;
    }

    @PostMapping(value = "/batch/UI_000100/saveClick")
    @ResponseBody
    public BaseResp<?> saveClick(@RequestBody UI_000100_Main_Form form) {
        this.infoMessage("變更存儲, 條件 = [", form.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        Batch batch1 = form.getBatch();
        int iRes = 0;
        String userId = WebUtil.getUser().getUserId();
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            if (checkAllBatchField(batch1, response, form)) {
                // 如果通知方式改為不通知, 則清除之前通知方式的設定
                if (batch1.getBatchNotifytype() == null || batch1.getBatchNotifytype() == (short) 0) {
                    this.resetNotify(batch1);
                }
                // 如果是否排程沒有勾選, 則清除排程相關所有的設定
                if (batch1.getBatchSchedule() == null || batch1.getBatchSchedule() == (short) 0) {
                    this.resetScheduleData(batch1);
                }
                if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                    batch1.setBatchBatchid(null); // 新增模式下, 這裡記得塞入null, 由DB創建PK值
                    iRes = obj.insertBatchJobTask(batch1, maintainBatch.getTasks());
                    if (iRes > 0) {
                        if (batch1.getBatchSchedule() == 1) {
                            obj.createScheduleTask(batch1.getBatchBatchid());
                        }
                        maintainBatch.setBatch(batch1);
                        response.setMessage(MessageType.INFO, InsertSuccess);
                    } else {
                        response.setMessage(MessageType.DANGER, InsertFail);
                    }
                } else {
                    iRes = obj.updateBatch(batch1, userId);
                    // 更新DB成功後通知批次服務建立此Batch的Task
                    if (batch1.getBatchSchedule() == 1) {
                        // 如果hostName有異動, 則要先通知原本的主機刪除排程中的Task
                        if (StringUtils.isBlank(maintainBatch.getBatch().getBatchExecuteHostName()) && StringUtils.isNotBlank(batch1.getBatchExecuteHostName())
                                || StringUtils.isNotBlank(maintainBatch.getBatch().getBatchExecuteHostName()) && !maintainBatch.getBatch().getBatchExecuteHostName().equals(batch1.getBatchExecuteHostName())) {
                            obj.deleteScheduleTask(maintainBatch.getBatch().getBatchExecuteHostName(), batch1.getBatchBatchid(), batch1.getBatchName());
                        }
                        obj.createScheduleTask(batch1.getBatchBatchid());
                    } else {
                        obj.deleteScheduleTask(maintainBatch.getBatch().getBatchExecuteHostName(), batch1.getBatchBatchid(), batch1.getBatchName());
                    }
                    if (iRes == 1) {
                        maintainBatch.setBatch(batch1);
                        response.setMessage(MessageType.INFO, UpdateSuccess);
                    } else {
                        response.setMessage(MessageType.DANGER, UpdateFail);
                    }
                }
            }
            return response;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
            return response;
        }
    }

    private boolean checkAllBatchField(Batch batch, BaseResp<?> response, UI_000100_Main_Form form) {
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            if (!"update".equals(maintainBatch.getDetail())) {
                batch.setBatchBatchid(null);
            }
            if (batch.getBatchSubsys() == 0) {
                response.setMessage(MessageType.DANGER, "未輸入系統別");
                return false;
            }
            if (batch.getBatchCheckbusinessdate() == 1 && StringUtils.isBlank(batch.getBatchZone())) {
                response.setMessage(MessageType.DANGER, "檢核營業日必須輸入地區別");
                return false;
            }
            batch.setBatchEditgroup(StringUtils.EMPTY);
            batch.setBatchStartgroup(form.getGroupChk());
            if ("update".equals(maintainBatch.getDetail())) {
                if (form.getJobId() == 0) {
                    batch.setBatchStartjobid(0);
                } else {
                    batch.setBatchStartjobid(form.getJobId());
                }
            } else {
                batch.setBatchStartjobid(0);
            }
            if ("update".equals(maintainBatch.getDetail()) && batch.getBatchSchedule() == 1) {
                if ("M".equals(batch.getBatchScheduleType()) && "mw".equals(form.getRadioType())) {
                    // 選每月的第幾週
                    batch.setBatchScheduleType("O");
                }
                if (StringUtils.isBlank(form.getDateTime())) {
                    response.setMessage(MessageType.DANGER, "未設置批次開始日期時間");
                    return false;
                }
                try {
                    batch.setBatchScheduleStarttime(FormatUtil.parseDataTime(form.getDateTime(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM));
                } catch (ParseException e) {
                    this.errorMessage(e, e.getMessage());
                    response.setMessage(MessageType.DANGER, "批次開始日期時間格式不正確");
                    return false;
                }
                switch (batch.getBatchScheduleType()) {
                    case "D": {
                        if (batch.getBatchScheduleRepetitioninterval() >= 1440) {
                            response.setMessage(MessageType.DANGER, "重覆時間必須小於1440分!");
                            return false;
                        }
                        if (batch.getBatchScheduleRepetitioninduration() >= 24) {
                            response.setMessage(MessageType.DANGER, "持續時間必須小於24小時!");
                            return false;
                        }
                        break;
                    }
                    case "W": {
                        if (form.getMwChk().length() == 1 && "0".equals(form.getMwChk())) {
                            form.setMwChk(StringUtils.EMPTY);
                        } else if (form.getMwChk().length() > 2 && "0,".equals(form.getMwChk().substring(0, 2))) {
                            form.setMwChk(form.getMwChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getMwChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何星期!");
                            return false;
                        }
                        batch.setBatchScheduleWeekdays(form.getMwChk());
                        break;
                    }
                    case "M": {
                        if (form.getmChk().length() == 1 && "0".equals(form.getmChk())) {
                            form.setmChk(StringUtils.EMPTY);
                        } else if (form.getmChk().length() > 2 && "0,".equals(form.getmChk().substring(0, 2))) {
                            form.setmChk(form.getmChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何月份!");
                            return false;
                        }
                        batch.setBatchScheduleMonths(form.getmChk());
                        if (form.getMdChk().length() == 1 && "0".equals(form.getMdChk())) {
                            form.setMdChk(StringUtils.EMPTY);
                        } else if (form.getMdChk().length() > 2 && "0,".equals(form.getMdChk().substring(0, 2))) {
                            form.setMdChk(form.getMdChk().substring(2));
                        }
                        if (StringUtils.isNotBlank(form.getMdChk())) {
                            batch.setBatchScheduleMonthdays(form.getMdChk());
                        } else {
                            response.setMessage(MessageType.DANGER, "尚未選取任何日期!");
                            return false;
                        }
                        break;
                    }
                    case "O": {
                        if (form.getmChk().length() == 1 && "0".equals(form.getmChk())) {
                            form.setmChk(StringUtils.EMPTY);
                        } else if (form.getmChk().length() > 2 && "0,".equals(form.getmChk().substring(0, 2))) {
                            form.setmChk(form.getmChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何月份!");
                            return false;
                        }
                        batch.setBatchScheduleMonths(form.getmChk());
                        if (form.getMwChk().length() == 1 && "0".equals(form.getMwChk())) {
                            form.setMwChk(StringUtils.EMPTY);
                        } else if (form.getMwChk().length() > 2 && "0,".equals(form.getMwChk().substring(0, 2))) {
                            form.setMwChk(form.getMwChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getMwChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何星期!");
                            return false;
                        }
                        batch.setBatchScheduleWeekdays(form.getMwChk());
                        if (form.getWmChk().length() == 1 && "0".equals(form.getWmChk())) {
                            form.setWmChk(StringUtils.EMPTY);
                        } else if (form.getWmChk().length() > 2 && "0,".equals(form.getWmChk().substring(0, 2))) {
                            form.setWmChk(form.getWmChk().substring(2));
                        }
                        if (StringUtils.isBlank(form.getWmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取哪一週!");
                            return false;
                        }
                        batch.setBatchScheduleWhickweeks(form.getWmChk());
                        break;
                    }
                    default: {
                        response.setMessage(MessageType.DANGER, "尚未選取排程方式");
                        return false;
                    }
                }
            }
            if (batch.getBatchNotifytype() != 0) {
                if (StringUtils.isBlank(batch.getBatchNotifymail())) {
                    response.setMessage(MessageType.DANGER, "通知Mail欄位必須有值");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
            return false;
        }
    }

    private List<HashMap<String, Object>> bindJobGrid() {
        MaintainBatch maintainBatch = this.getSessionData();
        if (Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
            return new ArrayList<>();
        }
        return obj.getJobTaskByBatchId(maintainBatch.getBatch().getBatchBatchid());
    }

    @PostMapping(value = "/batch/UI_000100/saveTaskClick")
    @ResponseBody
    public BaseResp<ArrayList<HashMap<String, Object>>> saveTaskClick(@RequestBody UI_000100_Task_Form form) {
        this.infoMessage("存儲批次Task, 條件 = [", form.toString(), "]");
        BaseResp<ArrayList<HashMap<String, Object>>> resp = new BaseResp<>();
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            // 在新增模式下
            if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                Task task = getTaskByTaskId(form.getTskId(), resp);
                if (task != null) {
                    int found = -1;
                    for (int i = 0; i < maintainBatch.getTasks().size(); i++) {
                        if (i == form.getJobId()) {
                            found = i;
                            break;
                        }
                    }
                    if (found != -1) {
                        maintainBatch.getTasks().set(found, task);
                    } else {
                        maintainBatch.getTasks().add(task);
                    }
                    resp.setMessage(MessageType.INFO, "存儲Task成功");
                }
                resp.setData(maintainBatch.makeGridData());
            }
            // 在編輯模式下
            else {
                Jobs job = new Jobs();
                if (checkAllJobField(job, form, resp)) {
                    job.setJobsBatchid(maintainBatch.getBatch().getBatchBatchid());
                    obj.updateJob(job, form.getTskId());
                    resp.setMessage(MessageType.INFO, "存儲Task成功");
                }
                resp.setData(new ArrayList<>(bindJobGrid()));
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, "存儲Task失敗");
        }
        return resp;
    }

    @PostMapping(value = "/batch/UI_000100/insertTaskClick")
    @ResponseBody
    public BaseResp<ArrayList<HashMap<String, Object>>> insertTaskClick(@RequestBody UI_000100_Task_Form form) {
        this.infoMessage("新增批次Task, 條件 = [", form.toString(), "]");
        BaseResp<ArrayList<HashMap<String, Object>>> resp = new BaseResp<>();
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            // 在新增模式下
            if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                Task task = getTaskByTaskId(form.getTskId(), resp);
                if (task != null) {
                    maintainBatch.getTasks().add(task);
                    resp.setMessage(MessageType.INFO, "新增Task成功");
                }
                resp.setData(maintainBatch.makeGridData());
            }
            // 在編輯模式下
            else {
                Jobs job = new Jobs();
                if (checkAllJobField(job, form, resp)) {
                    job.setJobsBatchid(maintainBatch.getBatch().getBatchBatchid());
                    obj.insertJobAndTask(job, form.getTskId());
                    // 2024-04-18 Richard modified 只有設定為排程才需要通知批次服務平台
                    if (maintainBatch.getBatch().getBatchSchedule() == 1) {
                        obj.createScheduleTask(maintainBatch.getBatch().getBatchBatchid());
                    }
                    resp.setMessage(MessageType.INFO, "新增Task成功");
                }
                resp.setData(new ArrayList<>(bindJobGrid()));
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, "新增Task失敗");
        }
        return resp;
    }

    private boolean checkAllJobField(Jobs job, UI_000100_Task_Form form, BaseResp<?> resp) {
        try {
            Task tsk = obj.getTaskById(form.getTskId());
            if (tsk == null) {
                resp.setMessage(MessageType.DANGER, "Task不存在");
                return false;
            }
            if (form.getSender() == 0) {
                // Add from EmptyDataTemplate
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
                job.setJobsDelay(0);
                job.setJobsSeq(1);
                job.setJobsStarttaskid(0);
            } else if (form.getSender() > 0) {
                // Add from FooterDataTemplate
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
                job.setJobsDelay(0);
                job.setJobsSeq(form.getSender() + 1);
                job.setJobsStarttaskid(0);
            } else {
                job.setJobsJobid(form.getJobId());
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
            }
            return true;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, "檢核Task失敗");
            return false;
        }
    }

    @PostMapping(value = "/batch/UI_000100/delTaskClick")
    @ResponseBody
    public BaseResp<ArrayList<HashMap<String, Object>>> delTaskClick(@RequestBody UI_000100_Task_Form form) {
        this.infoMessage("刪除批次Task, 條件 = [", form.toString(), "]");
        BaseResp<ArrayList<HashMap<String, Object>>> resp = new BaseResp<>();
        MaintainBatch maintainBatch = this.getSessionData();
        try {
            // 在新增模式下
            if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                if (!maintainBatch.getTasks().isEmpty()) {
                    maintainBatch.getTasks().remove(form.getJobId());
                }
                resp.setData(maintainBatch.makeGridData());
            }
            // 在編輯模式下
            else {
                obj.deleteJob(form.getJobId());
                resp.setData(new ArrayList<>(bindJobGrid()));
            }
            resp.setMessage(MessageType.INFO, "刪除Task成功");
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, "刪除Task失敗");
        }
        return resp;
    }

    @PostMapping(value = "/batch/UI_000100/changeTaskOrder")
    @ResponseBody
    public UI_000100_Task_Form changeTaskOrder(@RequestBody UI_000100_Task_Form form) {
        this.infoMessage("變更Task順序, 條件 = [", form.toString(), "]");
        try {
            if (form.getSender() == null) {
                form.setMessage(MessageType.DANGER, "批次Task清單不可以為空！");
                return form;
            }
            // 2024-04-24 Richard add start for 【Unchecked Input for Loop Condition】
            int nTask = form.getSender();
            if (nTask > MAX_LOOPS) {
                nTask = MAX_LOOPS;
            }
            // 2024-04-24 Richard add start for 【Unchecked Input for Loop Condition】
            String[] jobsSeqs = new String[nTask]; // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
            String[] jobsJobIDs = new String[nTask]; // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
            String[] taskIDs = new String[nTask]; // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
            String[] seqs = new String[nTask]; // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
            if (nTask > 0) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                seqs = form.getJobsSeq().split(",");
                jobsSeqs = form.getJobsSeq().split(",");
                jobsJobIDs = form.getJobsJobID().split(",");
                taskIDs = form.getTaskID().split(",");
            }
            ArrayList<String> tmp = new ArrayList<>();
            for (int i = 0; i < nTask; i++) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                tmp.add((i + 1) + StringUtils.EMPTY);
            }
            Arrays.sort(seqs);
            String[] strArr = null;
            strArr = tmp.toArray(new String[tmp.size()]);
            if (!Arrays.equals(strArr, seqs)) {
                form.setMessage(MessageType.DANGER, "順序必須由1開始且不能跳號也不能重覆！");
                return form;
            }
            MaintainBatch maintainBatch = this.getSessionData();
            // 在新增模式下
            if ("insert".equals(maintainBatch.getDetail()) || Objects.equals(maintainBatch.getBatch().getBatchBatchid(), EMPTY_BATCH_ID)) {
                List<Task> tasks = new ArrayList<>(nTask); // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                for (int i = 0; i < nTask; i++) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                    tasks.add(null);
                }
                for (int i = 0; i < nTask; i++) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                    Task task = this.getTaskByTaskId(Integer.parseInt(taskIDs[i]), form);
                    tasks.set(Integer.parseInt(jobsSeqs[i]) - 1, task);
                }
                tasks.removeIf(Objects::isNull); // 上面查詢到的Task有可能不存在, 則要移除掉null的task
                maintainBatch.getTasks().clear();
                maintainBatch.getTasks().addAll(tasks);
                form.setData(new ArrayList<>(maintainBatch.makeGridData()));
            }
            // 在編輯模式下
            else {
                int startJob = 0;
                for (int i = 0; i < nTask; i++) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                    int seq = Integer.parseInt(jobsSeqs[i]);
                    Jobs job = new Jobs();
                    job.setJobsJobid(Integer.parseInt(jobsJobIDs[i]));
                    job.setJobsSeq(seq);
                    Integer tskId = Integer.parseInt(taskIDs[i]);
                    obj.updateJob(job, tskId);
                    if (seq == 1) {
                        startJob = job.getJobsJobid();
                    }
                }
                Batch batch1 = new Batch();
                batch1.setBatchBatchid(maintainBatch.getBatch().getBatchBatchid());
                batch1.setBatchZone(null); // 避免原本的欄位被更新掉
                if (nTask == 0) { // 2024-04-24 Richard add modified for 【Unchecked Input for Loop Condition】
                    batch1.setBatchStartjobid(0);
                } else {
                    batch1.setBatchStartjobid(startJob);
                }
                obj.updateBatch(maintainBatch.getBatch(), WebUtil.getUser().getUserId());
                form.setData(new ArrayList<>(bindJobGrid()));
            }
            form.setMessage(MessageType.INFO, "變更Task順序成功");
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            form.setMessage(MessageType.DANGER, "變更Task順序失敗！");
        }
        return form;
    }

    /**
     * 初始化通知方式
     *
     * @param batch
     */
    private void resetNotify(Batch batch) {
        batch.setBatchNotifytype((short) 0);
        batch.setBatchNotifymail(StringUtils.EMPTY);
        batch.setBatchAction(StringUtils.EMPTY);
    }

    /**
     * 初始化是否排程
     *
     * @param batch
     */
    private void resetScheduleData(Batch batch) {
        batch.setBatchSchedule((short) 0);
        batch.setBatchScheduleType(null);
        batch.setBatchScheduleDayinterval((short) 0);
        batch.setBatchScheduleRepetitioninterval((short) 0);
        batch.setBatchScheduleRepetitioninduration((short) 0);
        batch.setBatchScheduleWeekinterval((short) 0);
        batch.setBatchScheduleMonths(StringUtils.EMPTY);
        batch.setBatchScheduleWeekdays(StringUtils.EMPTY);
        batch.setBatchScheduleWhickweeks(StringUtils.EMPTY);
        batch.setBatchScheduleMonthdays(StringUtils.EMPTY);
    }

    /**
     * 從Session獲取全局臨時變數
     *
     * @return
     */
    private MaintainBatch getSessionData() {
        return WebUtil.getFromSession(SessionKey.TemporaryRestoreData);
    }

    /**
     * 根據TaskId獲取Task
     *
     * @param taskId
     * @param resp
     * @return
     */
    private Task getTaskByTaskId(Integer taskId, BaseResp<?> resp) {
        Task task = null;
        try {
            task = obj.getTaskById(taskId);
            if (task == null) {
                resp.setMessage(MessageType.DANGER, "Task不存在");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, "查詢Task失敗");
        }
        return task;
    }
}
