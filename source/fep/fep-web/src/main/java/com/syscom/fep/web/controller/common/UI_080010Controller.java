package com.syscom.fep.web.controller.common;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Fepuser;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.common.UI_080010_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomuserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
public class UI_080010Controller extends BaseController {

	@Autowired
	private CommonService commonService;

//	@Autowired
//	private AtmService atmService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_080010_Form form = new UI_080010_Form();
		form.setUrl("/common/UI_080010/doQuery");
		this.doInquiryMain(form, mode);
	}

	@PostMapping(value = "/common/UI_080010/doQuery")
	public String doInquiryMain(@ModelAttribute UI_080010_Form form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			// 首次按下查詢時預設的排序
			if (form.getSqlSortExpressionCount() == 0) {
				form.addSqlSortExpression("LOGONID", SQLSortExpression.SQLSortOrder.ASC);
			}
			PageInfo<HashMap<String, Object>> pageInfo = commonService.queryUsersBy(form.getLogonIdQ(), form.getUserNameQ(), form.getSqlSortExpression(), form.getPageNum(), form.getPageSize());
			PageData<UI_080010_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);
			if (pageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			}
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_080010.getView();
	}

	@PostMapping(value = "/common/UI_080010/inquiryDetail")
	public String inquiryDetail(@ModelAttribute UI_080010_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {

//			List<Bctl> bctlList = atmService.getAllBctlBrno();
//			List<SelectOption<String>> bronList = new ArrayList<>();
//			for (Bctl bctl : bctlList) {
//				bronList.add(new SelectOption<>(bctl.getBctlBrno() + "-" + bctl.getBctlAlias(), bctl.getBctlBrno()));
//			}
//			mode.addAttribute("bronList", bronList);

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Syscomuser user = commonService.queryUsersById(Integer.valueOf(form.getUserid()));
			if (user != null) {
				Fepuser fepUser = commonService.queryFepUserInfo(user.getEmpid().intValue());
				if (fepUser != null) {
					form.setLogonid(user.getLogonid());
					form.setEmployeeid(user.getEmployeeid());
					form.setUsername(user.getUsername());
					form.setFepUserBrno(fepUser.getFepuserBrno());
					form.setFepUserJob(fepUser.getFepuserJob());
					form.setFepUserTlrno(fepUser.getFepuserTlrno());
					form.setEmpid(user.getEmpid().intValue());
					form.setEffectdate(simpleDateFormat.format(user.getEffectdate()));
					form.setExpireddate(simpleDateFormat.format(user.getExpireddate()));
					form.setEmailaddress(user.getEmailaddress());
				} else {
					this.showMessage(mode, MessageType.INFO, QueryNoData);
				}
			} else {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			}
			

			WebUtil.putInAttribute(mode, AttributeName.Options, "modify");
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_080010_Detail.getView();
	}

	@PostMapping(value = "/common/UI_080010/modifyDetail")
	public String modifyDetail(@ModelAttribute UI_080010_Form form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("執行修改動作, 條件 = [", form.toString(), "]");
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Syscomuser syscomuser = new Syscomuser();
			syscomuser.setUserid(Integer.valueOf(form.getUserid()));
			syscomuser.setLogonid(form.getLogonid());
			syscomuser.setUsername(form.getUsername());
			syscomuser.setEmailaddress(form.getEmailaddress());
			syscomuser.setEmployeeid(form.getEmployeeid());
			syscomuser.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
			syscomuser.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
			syscomuser.setUpdatetime(Calendar.getInstance().getTime());
			syscomuser.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));

			Fepuser fepuser = new Fepuser();
			fepuser.setFepuserUserid(form.getEmpid());
			fepuser.setFepuserLogonid(form.getLogonid());
			fepuser.setFepuserName(form.getUsername());
//			fepuser.setFepuserBrno(form.getFepUserBrno());
			fepuser.setFepuserJob(form.getFepUserJob());
			fepuser.setFepuserTlrno(form.getFepUserTlrno());
			fepuser.setUpdateTime(Calendar.getInstance().getTime());
			fepuser.setUpdateUser(Integer.parseInt(WebUtil.getUser().getUserId()));

			boolean rst = commonService.updatetUser(syscomuser, fepuser);
			if (rst) {
				this.showMessage(redirectAttributes, MessageType.SUCCESS, UpdateSuccess);
			} else {
				this.showMessage(redirectAttributes, MessageType.DANGER, UpdateFail);
			}
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		// 修改完成返回上一頁
		return this.doRedirectForPrevPage(redirectAttributes, request);
	}

	@PostMapping(value = "/common/UI_080010/insertDetail")
	public String insertDetail(@ModelAttribute UI_080010_Form form, ModelMap mode) {
		this.infoMessage("進入新增添明細資料頁面, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			form.setEffectdate(sdf.format(cal.getTime()));
			cal.add(Calendar.YEAR, 2);
			form.setExpireddate(sdf.format(cal.getTime()));

//			List<Bctl> bctlList = atmService.getAllBctlBrno();
//			List<SelectOption<String>> bronList = new ArrayList<>();
//			for (Bctl bctl : bctlList) {
//				bronList.add(new SelectOption<>(bctl.getBctlBrno() + "-" + bctl.getBctlAlias(), bctl.getBctlBrno()));
//			}
//			mode.addAttribute("bronList", bronList);

			WebUtil.putInAttribute(mode, AttributeName.Options, "insert");
//		} catch (SafeaaException ex) {
//			this.showMessage(mode, MessageType.INFO, ex.getMessage());
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_080010_Add.getView();
	}

	@PostMapping(value = "/common/UI_080010/saveDetail")
	public String saveDetail(@ModelAttribute UI_080010_Form form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("執行保存動作, 條件 = [", form.toString(), "]");
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Syscomuser syscomuser = new Syscomuser();
			syscomuser.setLogonid(form.getLogonid());
			syscomuser.setUsername(form.getUsername());
			syscomuser.setEmailaddress(form.getEmailaddress());
			syscomuser.setEmployeeid(form.getEmployeeid());
			syscomuser.setEffectdate(simpleDateFormat.parse(form.getEffectdate()));
			syscomuser.setExpireddate(simpleDateFormat.parse(form.getExpireddate()));
			syscomuser.setUpdatetime(Calendar.getInstance().getTime());
			syscomuser.setUpdateuserid(Integer.parseInt(WebUtil.getUser().getUserId()));

			Fepuser fepuser = new Fepuser();
			fepuser.setFepuserLogonid(form.getLogonid());
			fepuser.setFepuserName(form.getUsername());
//			fepuser.setFepuserBrno(form.getFepUserBrno());
			fepuser.setFepuserJob(form.getFepUserJob());
			fepuser.setFepuserTlrno(form.getFepUserTlrno());
			fepuser.setFepuserLevel((short) 1);
			fepuser.setFepuserStatus((short) 0);
			fepuser.setUpdateTime(Calendar.getInstance().getTime());
			fepuser.setFepuserUserid(Integer.parseInt(WebUtil.getUser().getUserId()));

			SyscomuserInfoVo vo = commonService.getSyscomuserInfo(form.getLogonid());
			if (vo != null) {
				this.showMessage(redirectAttributes, MessageType.INFO, "帳號重複，不允許新增");
				return this.doRedirectForCurrentPage(redirectAttributes, request);
			}

			boolean rst = commonService.insertUser(syscomuser, fepuser);
			if (rst) {
				this.showMessage(redirectAttributes, MessageType.SUCCESS, InsertSuccess);
			} else {
				this.showMessage(redirectAttributes, MessageType.DANGER, InsertFail);
				return this.doRedirectForCurrentPage(redirectAttributes, request);
			}
		} catch (SafeaaException ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.INFO, programError);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
			return this.doRedirectForCurrentPage(redirectAttributes, request);
		}
		// 修改完成返回上一頁
		return this.doRedirectForPrevPage(redirectAttributes, request);
	}

	@PostMapping(value = "/common/UI_080010/deleteDetail")
	public String deleteDetail(@ModelAttribute UI_080010_Form form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("刪除主檔資料, 條件 = [", form.toString(), "]");
		try {
			boolean rst = commonService.deleteUserById(Integer.valueOf(form.getUserid()), form.getEmpid());
			if (rst) {
				this.showMessage(redirectAttributes, MessageType.SUCCESS, DeleteSuccess);
			} else {
				this.showMessage(redirectAttributes, MessageType.DANGER, DeleteFail);
			}
			this.showMessage(mode, MessageType.SUCCESS, DeleteSuccess);

		} catch (SafeaaException ex) {
			this.showMessage(mode, MessageType.INFO, ex.getMessage());
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return this.doRedirectForCurrentPage(redirectAttributes, request);
	}

	@PostMapping(value = "/common/UI_080010/deleteList")
	@ResponseBody
	public BaseResp<?> deleteList(@RequestBody List<UI_080010_Form> formList, ModelMap mode) {
		this.infoMessage("執行刪除動作, 條件 = [", formList.toString(), "]");
		BaseResp<?> response = new BaseResp<>();
		try {
			for (UI_080010_Form form : formList) {
				boolean rst = commonService.deleteUserById(Integer.parseInt(form.getUserid()), form.getEmpid());
				if (!rst) {
					response.setMessage(MessageType.DANGER, DeleteFail);
					return response;
				}
			}
			response.setMessage(MessageType.SUCCESS, DeleteSuccess);
		} catch (SafeaaException ex) {
			this.showMessage(mode, MessageType.INFO, ex.getMessage());
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			// show錯誤訊息到前台頁面
			response.setMessage(MessageType.DANGER, DeleteFail);
		}
		return response;
	}

	@PostMapping(value = "/common/UI_080010/unlock")
	public String unlock(@ModelAttribute UI_080010_Form form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("執行解鎖動作, 條件 = [", form.toString(), "]");
		try {
			boolean rst = commonService.unlockAccount(Integer.valueOf(form.getUserid()), form.getLogonid(), WebUtil.getRemoteClientIp(), WebUtil.getUser().getLoginId());
			if (rst) {
				this.showMessage(redirectAttributes, MessageType.SUCCESS, "解鎖成功");
			} else {
				this.showMessage(redirectAttributes, MessageType.DANGER, "解鎖失敗");
			}
		} catch (SafeaaException ex) {
			this.showMessage(mode, MessageType.INFO, ex.getMessage());
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return this.doRedirectForCurrentPage(redirectAttributes, request);
	}

	@PostMapping(value = "/common/UI_080010/restsscode")
	public String restSscode(@ModelAttribute UI_080010_Form form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("執行重置密碼動作, 條件 = [", form.toString(), "]");
		try {
			boolean rst = commonService.restPassword(Integer.valueOf(form.getUserid()), form.getLogonid(), WebUtil.getRemoteClientIp());
			if (rst) {
				this.showMessage(redirectAttributes, MessageType.SUCCESS, "重置密碼成功");
			} else {
				this.showMessage(redirectAttributes, MessageType.DANGER, "重置密碼失敗");
			}
		} catch (SafeaaException ex) {
			this.showMessage(mode, MessageType.INFO, ex.getMessage());
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return this.doRedirectForCurrentPage(redirectAttributes, request);
	}
}
