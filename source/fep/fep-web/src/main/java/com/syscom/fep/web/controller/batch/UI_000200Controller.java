package com.syscom.fep.web.controller.batch;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.batch.UI_000200_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.util.WebUtil;

/**
 * UI000200 批次程序維護
 *
 * @author xingyun_yang
 * @create 2022/1/13
 */
@Controller
public class UI_000200Controller extends BaseController {
	private static final String URL_DO_QUERY = "/batch/UI_000200/queryClick";

	@Autowired
	private BatchService batchService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_000200_Form form = new UI_000200_Form();
		form.setUrl(URL_DO_QUERY);
		this.queryClick(form, mode);
	}

	@PostMapping(value = URL_DO_QUERY)
	private String queryClick(@ModelAttribute UI_000200_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		bindGrid(form, mode, "ASC");
		return Router.UI_000200.getView();
	}

	/**
	 * 設定畫面Button Delete的Event。
	 * 透過CheckBox選取刪除者，請務必注意aspx中CheckBox要加入Javascript的DeleteChk_click，該function請參考aspx最下面的Code
	 */
	@RequestMapping(value = "/batch/UI_000200/btnDelete")
	@ResponseBody
	private BaseResp<UI_000200_Form> deleteClick(@RequestBody UI_000200_Form dForm) throws Exception {
		BaseResp<UI_000200_Form> response = new BaseResp<>();
		Task task = new Task();
		task.setTaskId(Integer.parseInt(dForm.getTask_Id()));
		batchService.deleteTask(task);
		response.setData(dForm);
		return response;
	}

	@PostMapping(value = "/batch/UI_000200/showDetail")
	private String showDetail(@ModelAttribute UI_000200_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		form.setBtnType(nullToEmptyStr(form.getBtnType()));
		form.setTask_Name(nullToEmptyStr(form.getTask_Name()));
		form.setTask_Command(nullToEmptyStr(form.getTask_Command()));
		form.setTask_Commandargs(nullToEmptyStr(form.getTask_Commandargs()));
		form.setTask_Id(nullToEmptyStr(form.getTask_Id()));
		form.setTask_Description(nullToEmptyStr(form.getTask_Description()));
		form.setTask_Timeout(nullToEmptyStr(form.getTask_Timeout()));
		if (!"insert".equals(form.getBtnType())) {
			mode.addAttribute("taskIdBoolean", "true");
			Task task = bindFormViewData(Integer.parseInt(form.getTask_Id()));
			form.setBtnType(nullToEmptyStr(form.getBtnType()));
			form.setTask_Name(nullToEmptyStr(task.getTaskName()));
			form.setTask_Command(nullToEmptyStr(task.getTaskCommand()));
			form.setTask_Commandargs(nullToEmptyStr(task.getTaskCommandargs()));
			form.setTask_Description(nullToEmptyStr(task.getTaskDescription()));
			form.setTask_Id(nullToEmptyStr(task.getTaskId()));
			form.setTask_Timeout(nullToEmptyStr(task.getTaskTimeout()));
		}
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return Router.UI_000200_Detail.getView();
	}

	@PostMapping(value = "/batch/UI_000200/saveClick")
	private String saveClick(@ModelAttribute UI_000200_Form form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("保存批次程式, 條件 = [", form.toString(), "]");
		Task task = new Task();
		// 事件編號
		Integer taskId;
		Integer iRes;
		if (checkAllField(task, form, redirectAttributes)) {
			if ("insert".equals(form.getBtnType())) {
				taskId = batchService.insertTask(task);
				if (taskId > 0) {
					this.showMessage(redirectAttributes, MessageType.INFO, InsertSuccess);
					return this.doRedirectForPrevPage(redirectAttributes, request);
				} else {
					this.showMessage(redirectAttributes, MessageType.INFO, InsertFail);
				}
			} else {
				iRes = batchService.updateTask(task);
				if (iRes == 1) {
					this.showMessage(redirectAttributes, MessageType.INFO, UpdateSuccess);
					return this.doRedirectForPrevPage(redirectAttributes, request);
				} else {
					this.showMessage(redirectAttributes, MessageType.INFO, UpdateFail);
				}
			}
		}
		if (!"insert".equals(form.getBtnType())) {
			redirectAttributes.addAttribute("taskIdBoolean", "true");
		}
		return this.doRedirectForCurrentPage(redirectAttributes, request);
	}

	/**
	 * 資料整理
	 */
	private void bindGrid(UI_000200_Form form, ModelMap mode, String direction) {
		PageInfo<Task> pageInfo = null;
		try {
			// 若沒輸入值則Query全部，若有則Query by Task ID
			if (StringUtils.isBlank(form.getTask_Name())) {
				pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
					@Override
					public void doSelect() {
							batchService.getTaskAll();
					}
				});
			} else {
				pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
					@Override
					public void doSelect() {
							batchService.getTaskByName(form.getTask_Name(), direction);
					}
				});
			}
			if (pageInfo.getList() == null || pageInfo.getList().size() == 0) {
				this.showMessage(mode, MessageType.WARNING, QueryNoData);
				return;
			} else {
				this.clearMessage(mode);
				PageData<UI_000200_Form, Task> pageData = new PageData<>(pageInfo, form);
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			}
		} catch (Exception exception) {
			this.errorMessage(exception, exception.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
	}

	private Task bindFormViewData(Integer pk) {
		return batchService.getTaskById(pk);
	}

	private boolean checkAllField(Task task, UI_000200_Form form, RedirectAttributes redirectAttributes) {
		String strTaskTIMEOUT;
		try {
			// 程序編號
			if (!"insert".equals(form.getBtnType())) {
				task.setTaskId(Integer.parseInt(form.getTask_Id()));
			}
			// 程序名稱
			task.setTaskCommand(form.getTask_Command());
			// 2022-02-16 Richard modified
			// 程序名稱為類名, 所以要判斷一下類是否存在
			boolean taskNotExist = false;
			BatchJobLibrary batchLib = new BatchJobLibrary();
			try {
				com.syscom.fep.batch.base.task.Task batchTaks = batchLib.getBatchTask(task.getTaskCommand());
				taskNotExist = (batchTaks == null);
			} catch (Throwable e) {
				LogHelperFactory.getGeneralLogger().warn(e, e.getMessage());
				taskNotExist = true;
			}
			if (taskNotExist) {
				this.showMessage(redirectAttributes, MessageType.DANGER, "指定的程序名稱不存在!");
				return false;
			}
			// 程序引數
			task.setTaskCommandargs(form.getTask_Commandargs());
			// 程序說明
			task.setTaskDescription(form.getTask_Description());
			// 程序簡稱
			task.setTaskName(form.getTask_Name());
			strTaskTIMEOUT = form.getTask_Timeout();
			// 逾時(Sec)
			task.setTaskTimeout(Integer.parseInt(strTaskTIMEOUT));
			return true;
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(redirectAttributes, MessageType.DANGER, programError);
			return false;
		}
	}
}
