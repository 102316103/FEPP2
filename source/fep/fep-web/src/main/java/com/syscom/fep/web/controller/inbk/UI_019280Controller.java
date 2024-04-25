package com.syscom.fep.web.controller.inbk;

import java.util.Calendar;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019280_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.web.controller.BaseController;

import javax.xml.crypto.Data;

/**
 * 查詢請求傳送滯留訊息-2280
 *
 * @author xingyun_yang
 * @create 2021/8/10
 */
@Controller
public class UI_019280Controller extends BaseController {
    @Autowired
    private InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_019280_Form form = new UI_019280_Form();
        // 營業日期
        form.setDatetime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/inbk/UI_019280/Confirm9280")
    public String doInquiryDetail(@ModelAttribute UI_019280_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            //查詢方式
            String way="3";
            switch (form.getRadioOption()) {
                case ORI:
                    way = "1";
                    break;
                case AGENCY:
                    way = "2";
                    break;
                case BOTH:
                    way = "3";
                    break;
                default:
                    break;
            }
            // SysStatus.getPropertyValue().getSysstatHbkno() = 807
            String sysstatHbkno= SysStatus.getPropertyValue().getSysstatHbkno();
            //營業日期
            String datetime =  StringUtils.replace(form.getDatetime(),"-", StringUtils.EMPTY);
            //交易時間起訖
            String stime = form.getStime();
            String etime = form.getEtime();
            //交易日期
            String datetimeo = form.getDatetimeo().replace("-", "");
            //財金STAN
            String bkno = form.getBkno();
            String stan = form.getStan();
            //財金交易代號(PCODE)
            String trad = form.getTrad();
            PageInfo<Feptxn> pageInfo =  inbkService.selectByRetention(way,sysstatHbkno,datetime,stime, etime, datetimeo, bkno, stan,trad,form.getPageNum(),form.getPageSize());
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_019280_Form, Feptxn> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019280.getView();
    }
}
