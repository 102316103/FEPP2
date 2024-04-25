package com.syscom.fep.web.controller.dbmaintain;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Inbkparm;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.batch.UI_000200_Form;
import com.syscom.fep.web.form.dbmaintain.UI_070030_Detail_Form;
import com.syscom.fep.web.form.dbmaintain.UI_070030_Form;
import com.syscom.fep.web.form.inbk.UI_019060_FormDetail;
import com.syscom.fep.web.form.inbk.UI_019060_FormMain;
import com.syscom.fep.web.form.inbk.UI_019120_Form;
import com.syscom.fep.web.form.inbk.UI_019141_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
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

/**
 * 跨行系統參數維護
 *
 * @author Joseph
 * @create 2022/05/23
 */
@Controller
public class UI_070030Controller extends BaseController {

	@Autowired
	private InbkService inbkService;
	@SuppressWarnings("unused")
	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_070030_Form form = new UI_070030_Form();
		form.setUrl("/dbmaintain/UI_070030/queryClick");
		this.queryClick(form, mode);
	}

	@PostMapping(value = "/dbmaintain/UI_070030/queryClick")
	private String queryClick(@ModelAttribute UI_070030_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		
		try {
			BindGridData(form, mode);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_070030.getView();
	}

	@PostMapping(value = "/dbmaintain/UI_070030/btnDelete")
	@ResponseBody
	public BaseResp<UI_070030_Form> btnDelete(@RequestBody List<UI_070030_Form> list) {
		this.infoMessage("執行刪除動作, 條件 = [", list.toString(), "]");
		BaseResp<UI_070030_Form> response = new BaseResp<>();
		try {
			for (UI_070030_Form form : list) {
				Inbkparm inbkparm = new Inbkparm();
				inbkparm.setInbkparmApid(form.getInbkparmApid());
				inbkparm.setInbkparmPcode(form.getInbkparmPcode());
				inbkparm.setInbkparmAcqFlag(form.getInbkparmAcqFlag());
				inbkparm.setInbkparmEffectDate(form.getInbkparmEffectDate());
				inbkparm.setInbkparmCur(form.getInbkparmCur());
				inbkparm.setInbkparmRangeFrom(form.getInbkparmRangeFrom());
				inbkService.deleteINBKPARM(inbkparm);
			}
			response.setMessage(MessageType.INFO, DeleteSuccess);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			// show錯誤訊息到前台頁面
			response.setMessage(MessageType.DANGER, DeleteFail);
		}
		return response;
	}
	@PostMapping(value = "/dbmaintain/UI_070030/showDetail")
	private String showDetail(@ModelAttribute UI_070030_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		form.setBtnType(nullToEmptyStr(form.getBtnType()));
		form.setINBKPARM_APID(nullToEmptyStr(form.getINBKPARM_APID()));
		form.setINBKPARM_PCODE(nullToEmptyStr(form.getINBKPARM_PCODE()));
		form.setINBKPARM_ACQ(form.getINBKPARM_ACQ_FLAG().toString());
		form.setINBKPARM_EFFECT_DATE(nullToEmptyStr(form.getINBKPARM_EFFECT_DATE()));
		form.setINBKPARM_CUR(nullToEmptyStr(form.getINBKPARM_CUR()));
		form.setINBKPARM_RANGE_FROM(form.getINBKPARM_RANGE_FROM());
		form.setINBKPARM_RANGE_TO(form.getINBKPARM_RANGE_TO());
		form.setINBKPARM_FEE_TYPE(form.getINBKPARM_FEE_TYPE());
		form.setINBKPARM_FEE_MBR_DR(form.getINBKPARM_FEE_MBR_DR());
		form.setINBKPARM_FEE_MBR_CR(form.getINBKPARM_FEE_MBR_CR());
		form.setINBKPARM_FEE_ASS_DR(form.getINBKPARM_FEE_ASS_DR());
		form.setINBKPARM_FEE_ASS_CR(form.getINBKPARM_FEE_ASS_CR());
		form.setINBKPARM_FEE_CUSTPAY(form.getINBKPARM_FEE_CUSTPAY());
		form.setINBKPARM_PRNCRDB(nullToEmptyStr(form.getINBKPARM_PRNCRDB()));
		form.setINBKPARM_FEE_MIN(form.getINBKPARM_FEE_MIN());
		if (!"insert".equals(form.getBtnType())) {
			String inbkparmacq = form.getINBKPARM_ACQ_FLAG().toString();
			Inbkparm inbkparm = bindFormViewData(form.getINBKPARM_APID(),inbkparmacq,form.getINBKPARM_CUR(),form.getINBKPARM_EFFECT_DATE(),form.getINBKPARM_RANGE_FROM(),form.getINBKPARM_PCODE());
			form.setINBKPARM_APID(nullToEmptyStr(inbkparm.getInbkparmApid()));
			form.setINBKPARM_PCODE(nullToEmptyStr(inbkparm.getInbkparmPcode()));
			form.setINBKPARM_ACQ(nullToEmptyStr(inbkparm.getInbkparmAcqFlag()));
			inbkparm.setInbkparmEffectDate(inbkparm.getInbkparmEffectDate().substring(0,4) + "-" +inbkparm.getInbkparmEffectDate().substring(4,6)+ "-" +inbkparm.getInbkparmEffectDate().substring(6,8));
			form.setINBKPARM_EFFECT_DATE(nullToEmptyStr(inbkparm.getInbkparmEffectDate()));
			form.setINBKPARM_CUR(nullToEmptyStr(inbkparm.getInbkparmCur()));
			form.setINBKPARM_RANGE_FROM(inbkparm.getInbkparmRangeFrom());
			form.setINBKPARM_RANGE_TO(inbkparm.getInbkparmRangeTo());
			form.setINBKPARM_FEE_TYPE(inbkparm.getInbkparmFeeType());
			form.setINBKPARM_FEE_MBR_DR(inbkparm.getInbkparmFeeMbrDr());
			form.setINBKPARM_FEE_MBR_CR(inbkparm.getInbkparmFeeMbrCr());
			form.setINBKPARM_FEE_ASS_DR(inbkparm.getInbkparmFeeAssDr());
			form.setINBKPARM_FEE_ASS_CR(inbkparm.getInbkparmFeeAssDr());
			form.setINBKPARM_FEE_CUSTPAY(inbkparm.getInbkparmFeeCustpay());
			form.setINBKPARM_PRNCRDB(nullToEmptyStr(inbkparm.getInbkparmPrncrdb()));
			form.setINBKPARM_FEE_MIN(inbkparm.getInbkparmFeeMin());
		}
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return Router.UI_070030_Detail.getView();
	}

	private Inbkparm bindFormViewData(String APID, String a, String INBKPARM_CUR, String INBKPARM_EFFECT_DATE,
			BigDecimal INBKPARM_RANGE_FROM, String pcode) {
		return inbkService.getInbkparmByPK(APID,a,INBKPARM_CUR,INBKPARM_EFFECT_DATE,INBKPARM_RANGE_FROM,pcode);
	}
	@PostMapping(value = "/dbmaintain/UI_070030/saveClick")
	private String saveClick(@ModelAttribute UI_070030_Detail_Form dform, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("參數檔案維護, 條件= [", dform.toString(), "]");
		Inbkparm inbkparm = new Inbkparm();
		Integer iRes;
		if (checkAllField(inbkparm, dform, redirectAttributes)) {
			if ("insert".equals(dform.getBtnType())) {
				String INBKPARM_EFFECT_DATE = StringUtils.replace(dform.getINBKPARM_EFFECT_DATE(), "-", StringUtils.EMPTY);
				inbkparm.setInbkparmEffectDate(INBKPARM_EFFECT_DATE);
				iRes = inbkService.insertINBKPARM(inbkparm);
				if (iRes == 1) {
					this.showMessage(redirectAttributes, MessageType.INFO, InsertSuccess);
					return this.doRedirectForPrevPage(redirectAttributes, request);
				} else {
					this.showMessage(redirectAttributes, MessageType.INFO, InsertFail);
				}
			} else {
				String INBKPARM_EFFECT_DATE = StringUtils.replace(dform.getINBKPARM_EFFECT_DATE(), "-", StringUtils.EMPTY);
				inbkparm.setInbkparmEffectDate(INBKPARM_EFFECT_DATE);
				iRes = inbkService.updateINBKPARM(inbkparm);
				if (iRes == 1) {
					this.showMessage(redirectAttributes, MessageType.INFO, UpdateSuccess);
					return this.doRedirectForPrevPage(redirectAttributes, request);
				} else {
					this.showMessage(redirectAttributes, MessageType.INFO, UpdateFail);
				}
			}
		}
		return this.doRedirectForCurrentPage(redirectAttributes, request);
	}
	private boolean checkAllField(Inbkparm inbkparm, UI_070030_Detail_Form dform, RedirectAttributes redirectAttributes) {
		try {
			inbkparm.setInbkparmApid(dform.getINBKPARM_APID());
			if("".equals(dform.getINBKPARM_PCODE())) {
				inbkparm.setInbkparmPcode(dform.getINBKPARM_PCODE());
			}
			else {
				inbkparm.setInbkparmPcode(dform.getINBKPARM_PCODE());
			}
	
			if (dform.getINBKPARM_PCODE().equals(dform.getINBKPARM_APID())) {
				this.showMessage(redirectAttributes, MessageType.WARNING, "財金 PCODE等於財金APID, 則不需輸入財金PCODE");
				return false;
			}
			inbkparm.setInbkparmAcqFlag(dform.getINBKPARM_ACQ_FLAG());
			inbkparm.setInbkparmEffectDate(dform.getINBKPARM_EFFECT_DATE());
			inbkparm.setInbkparmCur(dform.getINBKPARM_CUR());
			inbkparm.setInbkparmRangeFrom(dform.getINBKPARM_RANGE_FROM());
			if(StringUtils.isBlank(dform.getINBKPARM_RANGE_TO().toString())){
				inbkparm.setInbkparmRangeTo(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmRangeTo(dform.getINBKPARM_RANGE_TO());
			}
			inbkparm.setInbkparmFeeType(dform.getINBKPARM_FEE_TYPE());
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_MBR_DR().toString())){
				inbkparm.setInbkparmFeeMbrDr(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeMbrDr(dform.getINBKPARM_FEE_MBR_DR());
			}
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_MBR_CR().toString())){
				inbkparm.setInbkparmFeeMbrCr(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeMbrCr(dform.getINBKPARM_FEE_MBR_CR());
			}
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_ASS_CR().toString())){
				inbkparm.setInbkparmFeeAssCr(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeAssCr(dform.getINBKPARM_FEE_ASS_CR());
			}
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_ASS_DR().toString())){
				inbkparm.setInbkparmFeeAssDr(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeAssDr(dform.getINBKPARM_FEE_ASS_DR());
			}
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_CUSTPAY().toString())){
				inbkparm.setInbkparmFeeCustpay(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeCustpay(dform.getINBKPARM_FEE_CUSTPAY());
			}
			inbkparm.setInbkparmPrncrdb(dform.getINBKPARM_PRNCRDB());
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_MIN().toString())){
				inbkparm.setInbkparmFeeMin(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeMin(dform.getINBKPARM_FEE_MIN());
			}
			return true;
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(redirectAttributes, MessageType.WARNING, programError);
			return false;
		}
	}

	public void BindGridData(UI_070030_Form form, ModelMap mode) {
		PageInfo<Inbkparm> pageInfo = null;
		try {
			String APID = form.getINBKPARM_APID();
			String INBKPARM_ACQ_FLAG= form.getINBKPARM_ACQ_FLAG().toString();
			String INBKPARM_CUR = form.getINBKPARM_CUR();
			String INBKPARM_EFFECT_DATE = form.getINBKPARM_EFFECT_DATE();
			//String INBKPARM_RANGE_FROM = form.getINBKPARM_RANGE_FROM().toString();
		   
			if (StringUtils.isBlank(form.getINBKPARM_APID())
					&& "N".equals(form.getINBKPARM_ACQ_FLAG().toString())
					&& StringUtils.isBlank(form.getINBKPARM_EFFECT_DATE())
					&& StringUtils.isBlank(form.getINBKPARM_CUR())
					&& form.getINBKPARM_RANGE_FROM()==null) {
				pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
						form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
							@Override
							public void doSelect() {
									inbkService.getInbkparmAll();
							}
						});
			} else {
				pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
						form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
							@Override
							public void doSelect() {
                                    inbkService.getINBKPARMByPK(APID, INBKPARM_ACQ_FLAG, INBKPARM_CUR,
                                            INBKPARM_EFFECT_DATE, form.getINBKPARM_RANGE_FROM());
							}
						});
			}
			if (pageInfo.getList() == null || pageInfo.getList().size() == 0) {
				this.showMessage(mode, MessageType.WARNING, QueryNoData);
				return;
			} else {
				this.clearMessage(mode);
				PageData<UI_070030_Form, Inbkparm> pageData = new PageData<>(pageInfo, form);
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			}

		} catch (Exception exception) {
			this.errorMessage(exception, exception.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
}
