package com.syscom.fep.web.controller.dbmaintain;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.entity.dbmaintain.MsgfileTmp;
import com.syscom.fep.web.form.dbmaintain.UI_070060_FormDetail;
import com.syscom.fep.web.form.dbmaintain.UI_070060_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 錯誤訊息定義資料維護
 * @author bruce
 *
 */
@Controller
public class UI_070060Controller extends BaseController{
	
	
	@Autowired
	private AtmService atmService;
	
	private String webType = WebConfiguration.getInstance().getWebType();
	
	private final String regx = "^[A-Za-z0-9+_.-]+@(.+)$";
	
	private final String ERRORCODE = "訊息代碼";

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_070060_FormMain form = new UI_070060_FormMain();
		Map<String, Object> argsMap = form.toMap();
		argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
		//'Fly 2018/02/14 SSTQ系統時取消新增/修改/刪除功能
		form.setWebType(this.webType);
		form.setUrl("/dbmaintain/UI_070060/bindGrid");
		this.bindGridData(form,argsMap,mode);
	}
	
	/**
	 * 查詢按鈕
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/dbmaintain/UI_070060/bindGrid")
	public String bindGrid(@ModelAttribute UI_070060_FormMain form, ModelMap mode) {
		Map<String, Object> argsMap = form.toMap();
		argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
		this.bindGridData(form, argsMap, mode);
		return Router.UI_070060.getView();
	}
	
	/**
	 * 新增按鈕
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/dbmaintain/UI_070060/insertClick")
	public String insertClick(@ModelAttribute UI_070060_FormDetail form, ModelMap mode) {
		this.doKeepFormData(mode, form);
		return Router.UI_070060_Detail.getView();
	}
	
	/**
	 * 儲存按鈕
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/dbmaintain/UI_070060/saveClick")
	public String saveClick(@ModelAttribute UI_070060_FormDetail form, ModelMap mode) {
		form.setUrl("/dbmaintain/UI_070060/bindGrid");
		String errorMsg = this.checkAllField(form);
		if(StringUtils.isBlank(errorMsg)) {
			try {
				if("E".equals(form.getActionType())) {
					atmService.updateMsgFile(this.setData(form));
					this.showMessage(mode, MessageType.INFO, UpdateSuccess);
				}else {
					boolean success = atmService.insertMsgFile(this.setData(form));
					if(success) {
						this.showMessage(mode, MessageType.INFO, InsertSuccess);
						//連續新增模式
						this.clearFormControl(form);
					}else {
						this.showMessage(mode, MessageType.DANGER, Multiple);
					}				
				}
			}catch(Exception e) {
				this.errorMessage(e, e.getMessage());
				this.showMessage(mode, MessageType.DANGER, programError);
			}			
		}else {
			this.showMessage(mode, MessageType.DANGER, programError);
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		}
		this.doKeepFormData(mode, form);
		return Router.UI_070060_Detail.getView();
	}
	
	/**
	 * 來源通道超連結
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/dbmaintain/UI_070060/bindGridDetail")
	public String bindGridDetail(@ModelAttribute UI_070060_FormDetail form, ModelMap mode) {
		//轉成布林值
		form.setMsgfileSendEmsB(DbHelper.toBoolean(form.getMsgfileSendEms()));
		form.setMsgfileRetainB(DbHelper.toBoolean(form.getMsgfileRetain()));
		form.setMsgfileAuthB(DbHelper.toBoolean(form.getMsgfileAuth()));
		form.setMsgfileWarningB(DbHelper.toBoolean(form.getMsgfileWarning()));
		form.setMsgfileNotifyB(DbHelper.toBoolean(form.getMsgfileNotify()));
		this.doKeepFormData(mode, form);
		return Router.UI_070060_Detail.getView();
	}
	
	/**
	 * 查詢
	 * @param argsMap
	 * @param mode
	 * @return
	 */
	private void bindGridData(UI_070060_FormMain form, Map<String, Object> argsMap, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);// 保存當前表單資料
		try {
			PageInfo<Msgfile> pageInfo = atmService.queryMsgFileByDef(argsMap);
			//將Msgfile改成使用MsgfileTmp
			PageInfo<MsgfileTmp> newPageInfo = this.changeObject(pageInfo);
			BeanUtils.copyProperties(pageInfo, newPageInfo, "list");
			if (newPageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				this.changeFieldContent(newPageInfo);
				this.showMessage(mode, MessageType.INFO, QuerySuccess);
			}
			PageData<UI_070060_FormMain, MsgfileTmp> pageData = new PageData<UI_070060_FormMain, MsgfileTmp>(newPageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}	
	
	/**
	 * 儲存時檢核欄位是否符合規格
	 * @param form
	 * @return
	 */
	private String checkAllField(UI_070060_FormDetail form) {
		if(StringUtils.isBlank(form.getMsgfileErrorcode())) {
			return this.ERRORCODE + HasData;
		}
		if(form.isMsgfileNotifyB()) {
			if(StringUtils.isBlank(form.getMsgfileResponsible())) {
				return PleaseInputEmail;
			}else {
				if(!Pattern.compile(this.regx).matcher(form.getMsgfileResponsible()).matches()) {
					return EmailError;
				}
			}
		}
		return "";
	}
	
	/**
	 * 因為要將物件欄位做二次加工，所以新增一個MsgfileTmp繼承Msgfile
	 * @param pageInfo
	 * @return
	 */
	private PageInfo<MsgfileTmp> changeObject(PageInfo<Msgfile> pageInfo) {
		List<Msgfile> msgfileList = pageInfo.getList();
		List<MsgfileTmp> msgfileTmpList = new ArrayList<>(msgfileList.size());
		for (Msgfile msgfile : msgfileList) {
			MsgfileTmp msgfileTmp = new MsgfileTmp(msgfile);
			msgfileTmpList.add(msgfileTmp);
		}
		return PageInfo.of(msgfileTmpList);
	}
	
	/**
	 * 改變特定欄位內容
	 * @param pageInfo
	 */
	private void changeFieldContent(PageInfo<MsgfileTmp> pageInfo ) {
		for(MsgfileTmp msgfile : pageInfo.getList()) {
			msgfile.setMsgfileChannelTxt(this.getChannelName(String.valueOf(msgfile.getMsgfileChannel())));//來源通道
			msgfile.setMsgfileSubsysTxt(this.getSubSystemName(String.valueOf(msgfile.getMsgfileSubsys())));//子系統
			msgfile.setMsgfileSendEmsTxt(this.getSendEmsToCh(String.valueOf(msgfile.getMsgfileSendEms())));//送事件監控
		}
	}
	
	/**
	 * 將送事件監控轉成"是"或"否"
	 * @param sendEms
	 * @return
	 */
	private String getSendEmsToCh(String sendEms) {
		switch (sendEms) {
		case "0": return "否";
		case "1": return "是";
		default:return "";
		}
	}
	
	/**
	 * 將子系統轉換成英文
	 * @param subsys
	 * @return
	 */
	private String getSubSystemName(String subsys) {
		if(StringUtils.isBlank(subsys)) {
			return "";
		}else {
			switch (subsys) {
			case "0": return "None";
			case "1": return "INBK";
			case "2": return "RM";
	        case "3": return "ATMP";
	        case "4": return "CARD";
	        case "5": return "HSM";
	        case "6": return "MON";
	        case "7": return "RECS";
	        case "8": return "GW";
	        case "9": return "CMN";
	        //'modified by ChenLi for Add subsystem -> SVCS
	        case "12":return "SVCS";
			default:return subsys;
			}			
		}
	}
	
	/**
	 * 將來源通道轉換成英文
	 * @param channel
	 * @return
	 */
	private String getChannelName(String channel) {
		switch (channel) {
		case "0": return"";
		case "1": return "ATM";
		case "2": return "FISC";
        case "3": return "WEBATM";
        case "4": return "SINOCARD";
        case "5": return "BRANCH";
        case "6": return "T24";
        //' 2010/8/17 modified by Daniel 改UI交易為FEP
        case "7": return "FEP";
        case "8": return "FCS";
        case "9": return "ATMMON";
        case "10":return "UATMP";
        case "11":return "FEDI";
        case "12":return "ETS";
        case "13":return "NETBANK";
        case "14":return "IVR";
        case "15":return "EPORTAL";
        case "16":return "SQL Server";
        case "17":return "Batch";
        case "18":return "CARDTP";
        case "19":return "GL";
        case "20":return "EBILL";
        case "21":return "PFS";
        case "22":return "MMAB2C";
        case "23":return "MOBILBANK";
        case "24":return "CSF3";
        //'modified by ChenLi on 2013/02/27 for 新增對應HSM的值
        case "25":return "HSM";
        //'modified by ChenLi on 2014/03/24 for 新增對應EasyDebit的值
        case "26":return "EasyDebit";
		default:return "";
		}
	}
	
	/**
	 * 將資料塞Msgfile
	 * @param form
	 * @return
	 * @throws ParseException 
	 */
	private Msgfile setData(UI_070060_FormDetail form) throws ParseException {
		MsgfileTmp msgfile = new MsgfileTmp();
		if("I".equals(form.getActionType())) {
			msgfile.setMsgfileSubsys(Integer.parseInt(form.getMsgfileSubsys()));
			msgfile.setMsgfileRetain((short) (form.isMsgfileRetainB() ? 1 : 0));
			msgfile.setMsgfileAuth((short) (form.isMsgfileAuthB() ? 1 : 0));
			msgfile.setMsgfileWarning((short) (form.isMsgfileWarningB() ? 1 : 0));
			msgfile.setMsgfileExternal(form.getMsgfileExternal());
			msgfile.setMsgfileFisc(form.getMsgfileFisc());
			msgfile.setMsgfileAtm(form.getMsgfileAtm());
			msgfile.setMsgfileUatmp(form.getMsgfileUatmp());
			msgfile.setMsgfileT24(form.getMsgfileT24());
			msgfile.setMsgfileCredit(form.getMsgfileCredit());
			msgfile.setMsgfileVisible((short)1);
		}
		//update及insert都要的欄位
		msgfile.setMsgfileChannel(Integer.parseInt(form.getMsgfileChannel()));
		msgfile.setMsgfileErrorcode(form.getMsgfileErrorcode());
		msgfile.setMsgfileSeverity(this.checkField(form.getMsgfileSeverity()));		
		msgfile.setMsgfileSendEms((short) (form.isMsgfileSendEmsB() ? 1 : 0));
		msgfile.setMsgfileShortmsg(this.checkField(form.getMsgfileShortmsg()));
		msgfile.setMsgfileMsgdscpt(this.checkField(form.getMsgfileMsgdscpt()));
		msgfile.setMsgfileAction(this.checkField(form.getMsgfileAction()));
		msgfile.setMsgfileResponsible(this.checkField(form.getMsgfileResponsible()));
		msgfile.setMsgfileNotify((short) (form.isMsgfileNotifyB() ? 1 : 0));
		msgfile.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));		
		return msgfile;
	}
	
	/**
	 * 如果欄位是空的就放空字串
	 * @param field
	 * @return
	 */
	private String checkField(String field) {
		if(StringUtils.isBlank(field)) {
			return "";
		}else {
			return field;
		}
	}
	
	/**
	 * 連續新增模式,清除單筆表單控制項內容
	 * @return
	 */
	private void clearFormControl(UI_070060_FormDetail form) {
		form.setActionType("I");
		form.setMsgfileSubsys("0");
		form.setMsgfileRetainB(false);
		form.setMsgfileAuthB(false);
		form.setMsgfileWarningB(false);
		form.setMsgfileSendEmsB(false);
		form.setMsgfileExternal("");
		form.setMsgfileErrorcode("");
		form.setMsgfileFisc("");
		form.setMsgfileAtm("");
		form.setMsgfileUatmp("");
		form.setMsgfileT24("");
		form.setMsgfileCredit("");
		form.setMsgfileNotifyB(false);
		form.setMsgfileShortmsg("");
		form.setMsgfileMsgdscpt("");
		form.setMsgfileAction("");
		form.setMsgfileResponsible("");
		form.setMsgfileSeverity("Info");
		form.setMsgfileChannel("1");
	}	
}
