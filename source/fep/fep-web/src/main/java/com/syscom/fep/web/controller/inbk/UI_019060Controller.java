package com.syscom.fep.web.controller.inbk;

import java.math.BigDecimal;
import java.util.*;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019060_FormDetail;
import com.syscom.fep.web.form.inbk.UI_019060_FormMain;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.syscom.fep.mybatis.model.Apibatch;
import com.syscom.fep.web.controller.BaseController;

/**
 * 查詢全繳API純代理交易結果
 *
 * @author  Kai
 */
@Controller
public class UI_019060Controller extends BaseController {
	private static final String ProgramName = UI_019060Controller.class.getSimpleName();

	@Autowired
	public InbkService inbkService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_019060_FormMain form = new UI_019060_FormMain();
		form.setBeginDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
		form.setEndDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/inbk/UI_019060/queryApibatch", produces = "application/json;charset=utf-8")
	public String getQueryAPIBATCH(@ModelAttribute UI_019060_FormMain form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		if(Integer.parseInt(form.getBeginDate().replace("-","")) > Integer.parseInt(form.getEndDate().replace("-",""))){
			this.showMessage(mode,MessageType.DANGER,"結束日期不可小於開始日期");
		}else{
			bindGridData(form,mode);
		}
		return Router.UI_019060.getView();
	}


	@PostMapping(value = "/inbk/UI_019060/queryApidtl", produces = "application/json;charset=utf-8")
	public String getQueryAPIDTL(@ModelAttribute UI_019060_FormDetail form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		this.bindGrid(form,mode);
		return Router.UI_019060_Detail.getView();
	}

	/**
	 依查詢條件查詢的主程式。
	 Bind 資料至 SyscomGridView 中
	 */
	public void bindGridData(UI_019060_FormMain form, ModelMap mode){
		try {
			PageInfo<Apibatch> apibatches = inbkService.queryApibatch(form.getBeginDate().replace("-", ""), form.getEndDate().replace("-", ""),form.getPageNum(),form.getPageSize());
			if(apibatches.getSize() == 0){
				this.showMessage(mode, MessageType.INFO,QueryNoData);
			}
			PageData<UI_019060_FormMain,Apibatch> pageData = new PageData<>(apibatches,form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		}catch (Exception ex){
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode,MessageType.DANGER,QueryFail);
		}
	}

	public void bindGrid(UI_019060_FormDetail form, ModelMap mode){
		try {
			String webType = WebConfiguration.getInstance().getWebType();
			BigDecimal totalHandlingFee = new BigDecimal("0");
			PageInfo<HashMap<String,Object>> pageInfo =  inbkService.queryApidtl(form.getApibatchTxDate().replace("/", ""),webType, form.getPageNum(),form.getPageSize());
			if(pageInfo.getSize() > 0){
				if(!"SSTQ".equals(webType)){
					HashMap<String,Object> summary = inbkService.getApibatchTotFee(form.getApibatchTxDate().replace("/", ""));
					if (MapUtils.isNotEmpty(summary)) {
						totalHandlingFee = DbHelper.getMapValue(summary, "APIBATCH_TOT_FEE", totalHandlingFee);
					}
					mode.addAttribute("totalHandlingFee", totalHandlingFee);
				}
			}else{
				this.showMessage(mode,MessageType.INFO,QueryNoData);
			}
			PageData<UI_019060_FormDetail,HashMap<String,Object>> pageData = new PageData<>(pageInfo,form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			mode.addAttribute("archivesDate", form.getApibatchTxDate());
			mode.addAttribute("totalAmount", form.getApibatchTotAmt());
			mode.addAttribute("totalNumber", form.getApibatchTotCnt());
		}catch (Exception ex){
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode,MessageType.DANGER,QueryFail);
		}
	}
}
