package com.syscom.fep.web.controller.inbk;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019050_Form;
import com.syscom.fep.web.form.inbk.UI_019050_FormDetail;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;

/**
 * 查詢全國繳費整批轉即時交易結果
 *
 * @author xingyun_yang
 * @create 2021/8/25
 */
@Controller
public class UI_019050Controller extends BaseController {

    @Autowired
    private InbkService inbkService;


    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_019050_Form form = new UI_019050_Form();
        // 營業日期
        form.setTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/inbk/UI_019050/doInquiryDetail")
    public String doInquiryDetail(@ModelAttribute UI_019050_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            // 傳送檔名
            String fileid = form.getFileid();
            //扣賬日期
            String txdate = form.getTxdate().replace("-", "");
            PageInfo<Npsbatch> pageInfo = inbkService.queryNPSBATCH(fileid,txdate,form.getPageNum(),form.getPageSize());
            PageData<UI_019050_Form, Npsbatch> pageData = new PageData<>(pageInfo, form);
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO,QueryNoData);
            } else {
            	  int tempVar = pageData.getList().size();
                  for (int i = 0; i < tempVar; i++) {
                	  Npsbatch data = pageData.getList().get(i);
                	  String npsbatchResult = data.getNpsbatchResult();
						if (StringUtils.isBlank(npsbatchResult)) { // 無值顯示”處理中”
							data.setNpsbatchResult("處理中");
						} else if ("00".equals(npsbatchResult)) { // “00”顯示”成功
							data.setNpsbatchResult("成功");
						} else { // “01”顯示”失敗”
							data.setNpsbatchResult("失敗");
						}
                  }
            }
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019050.getView();
    }

    @PostMapping(value = "/inbk/UI_019050/ShowDetail")
    public String ShowDetail(@ModelAttribute UI_019050_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
        	 // 傳送檔名
            String fileid = form.getFileid();
            //扣賬日期
            String txdate = form.getTxdate().replace("-", "").toString().trim();
            //批號
            String npsbatchBatchNo = form.getNpsbatchBatchNo().toString().trim();
            //batno 序號
            String batno = fileid+txdate+npsbatchBatchNo;
            mode.addAttribute("fileid",fileid);
            mode.addAttribute("txdate",txdate);
            mode.addAttribute("npsbatchBatchNo",npsbatchBatchNo);
            PageInfo<HashMap<String,Object>> pageInfo = inbkService.showDetail(batno,form.getPageNum(),form.getPageSize());
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_019050_FormDetail,HashMap<String,Object>> pageData = new PageData<>(pageInfo, form);
            List<HashMap<String,Object>> nbsdtlExtHashs= new ArrayList<>(pageData.getList().size());
            if (pageData.getList().size() > 0) {
                int tempVar = pageData.getList().size();
                for (int i = 0; i < tempVar; i++) {
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("NPSDTL_SEQ_NO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_SEQ_NO")));
                    hashMap.put("NPSDTL_TROUT_BKNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TROUT_BKNO")));
                    hashMap.put("NPSDTL_TROUT_ACTNO", nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TROUT_ACTNO")));
                    hashMap.put("NPSDTL_TRIN_BKNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TRIN_BKNO")));
                    hashMap.put("NPSDTL_TRIN_ACTNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TRIN_ACTNO")));
                    hashMap.put("NPSDTL_TX_AMT",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TX_AMT")));
                    hashMap.put("NPSDTL_BUSINESS_UNIT",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_BUSINESS_UNIT")));
                    hashMap.put("NPSDTL_PAYTYPE",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_PAYTYPE")));
                    hashMap.put("NPSDTL_PAYNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_PAYNO")));
                    hashMap.put("NPSDTL_RECON_SEQ",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_RECON_SEQ")));
                    hashMap.put("NPSDTL_STAN",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_STAN")));
                    hashMap.put("NPSDTL_EJFNO",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_EJFNO")));
                    hashMap.put("NPSDTL_TBSDY",nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_TBSDY")));
					HashMap<String, Object> data = pageData.getList().get(i);
					String npsdtlResult = Objects.toString(data.get("NPSDTL_RESULT"));
					if (StringUtils.isBlank(npsdtlResult)) { // 無值:處理中
						hashMap.put("NPSDTL_RESULT", "處理中");
					} else if ("00".equals(npsdtlResult)) { // 00:成功
						hashMap.put("NPSDTL_RESULT", "成功");
					} else { // 01:失敗
						hashMap.put("NPSDTL_RESULT", "失敗");
					}
                    hashMap.put("NPSDTL_REPLY_CODE", nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_REPLY_CODE")));
                    hashMap.put("NPSDTL_ERR_MSG", nullToEmptyStr(pageData.getList().get(i).get("NPSDTL_ERR_MSG")));
                    nbsdtlExtHashs.add(hashMap);
                }
            }
            pageData.setList(nbsdtlExtHashs);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019050_Detail.getView();
    }
}
