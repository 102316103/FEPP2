package com.syscom.fep.web.controller.inbk;

import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019010_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 查詢或變更系統狀態
 *
 * @author Joseph
 * @create 2022/05/11
 */
@Controller
public class UI_019010Controller extends BaseController {
    @Autowired
    private InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_019010_Form form = new UI_019010_Form();
        form.setSysstat(new Sysstat());
        BindData(form, mode);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_019010/confirm")
    public String confirmClick(@ModelAttribute UI_019010_Form form, ModelMap mode) throws Exception {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        int iRes = 0;
        Sysstat defSYSSTAT = inbkService.getStatus();
        Sysstat dtSYSSTAT = inbkService.getStatus();
        LogData logContext = new LogData();
        //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat Start
        //Fcrmstat defFCRMSTAT = inbkService.getFCRMSTAT();
        //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat end
        this.doKeepFormData(mode, form);

        try {
            getLogContext().setProgramName("UI_019010");
            defSYSSTAT.setSysstatHbkno(dtSYSSTAT.getSysstatHbkno());
            //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat Start
            //List<List<Object>> aryNeedSendEMS = prepareNeedSendEMSList(form, dtSYSSTAT, defFCRMSTAT);
            //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat end
            // AOCT
            if (dtSYSSTAT.getSysstatAoct1000().equals(form.getSysstat().getSysstatAoct1000())) {
                defSYSSTAT.setSysstatAoct1100(form.getSysstat().getSysstatAoct1100());
                defSYSSTAT.setSysstatAoct1200(form.getSysstat().getSysstatAoct1200());
                defSYSSTAT.setSysstatAoct1300(form.getSysstat().getSysstatAoct1300());
                defSYSSTAT.setSysstatAoct1400(form.getSysstat().getSysstatAoct1400());
            } else {
                defSYSSTAT.setSysstatAoct1000(form.getSysstat().getSysstatAoct1000());
                defSYSSTAT.setSysstatAoct1100(form.getSysstat().getSysstatAoct1000());
                defSYSSTAT.setSysstatAoct1200(form.getSysstat().getSysstatAoct1000());
                defSYSSTAT.setSysstatAoct1300(form.getSysstat().getSysstatAoct1000());
                defSYSSTAT.setSysstatAoct1400(form.getSysstat().getSysstatAoct1000());
            }
            // MBACT
            if (dtSYSSTAT.getSysstatMbact1000().equals(form.getSysstat().getSysstatMbact1000())) {
                defSYSSTAT.setSysstatMbact1100(form.getSysstat().getSysstatMbact1100());
                defSYSSTAT.setSysstatMbact1200(form.getSysstat().getSysstatMbact1200());
                defSYSSTAT.setSysstatMbact1300(form.getSysstat().getSysstatMbact1300());
                defSYSSTAT.setSysstatMbact1400(form.getSysstat().getSysstatMbact1400());
            } else {
                defSYSSTAT.setSysstatMbact1000(form.getSysstat().getSysstatMbact1000());
                defSYSSTAT.setSysstatMbact1100(form.getSysstat().getSysstatMbact1000());
                defSYSSTAT.setSysstatMbact1200(form.getSysstat().getSysstatMbact1000());
                defSYSSTAT.setSysstatMbact1300(form.getSysstat().getSysstatMbact1000());
                defSYSSTAT.setSysstatMbact1400(form.getSysstat().getSysstatMbact1000());
            }
            // SYSSTAT_MBACT_2000～SYSSTAT_MBACT_2200
            defSYSSTAT.setSysstatMbact2000(form.getSysstat().getSysstatMbact2000());
            defSYSSTAT.setSysstatMbact2200(form.getSysstat().getSysstatMbact2200());
            // SYSSTAT_MBACT_2500～SYSSTAT_MBACT_2560
            if (dtSYSSTAT.getSysstatMbact2500().equals(form.getSysstat().getSysstatMbact2500())) {
                defSYSSTAT.setSysstatMbact2510(form.getSysstat().getSysstatMbact2510());
                defSYSSTAT.setSysstatMbact2520(form.getSysstat().getSysstatMbact2520());
                defSYSSTAT.setSysstatMbact2530(form.getSysstat().getSysstatMbact2530());
                defSYSSTAT.setSysstatMbact2540(form.getSysstat().getSysstatMbact2540());
                defSYSSTAT.setSysstatMbact2550(form.getSysstat().getSysstatMbact2550());
                defSYSSTAT.setSysstatMbact2560(form.getSysstat().getSysstatMbact2560());
                defSYSSTAT.setSysstatMbact2570(form.getSysstat().getSysstatMbact2570());
            } else {
                defSYSSTAT.setSysstatMbact2500(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2510(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2520(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2530(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2540(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2550(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2560(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2570(form.getSysstat().getSysstatMbact2500());
            }
            // SYSSTAT_MBACT_7100
            defSYSSTAT.setSysstatMbact7100(form.getSysstat().getSysstatMbact7100());
            // SYSSTAT_MBACT_7300
            defSYSSTAT.setSysstatMbact7300(form.getSysstat().getSysstatMbact7300());
            //2022/09/12 Bruce modify 依照Candy需求取消Fcrmstat相關 Start
            //if (defFCRMSTAT != null) {
             //   iRes = inbkService.UpdateFCRMSTAT(defFCRMSTAT);
            //}
            //2022/09/12 Bruce modify 依照Candy需求取消Fcrmstat相關 end
            iRes = inbkService.UpdateSYSSTAT(defSYSSTAT);
            if (iRes > 0) {
                FISC ofiscbusiness = new FISC();
                ofiscbusiness.setLogContext(logContext);
                FEPReturnCode rtnCode = CommonReturnCode.Normal;

                if ("4".equals(defSYSSTAT.getSysstatAoct1000()) || "4".equals(defSYSSTAT.getSysstatAoct1100()) ||
                        "D".equals(defSYSSTAT.getSysstatAoct1000()) || "D".equals(defSYSSTAT.getSysstatAoct1100())) {
                    rtnCode = ofiscbusiness.updateEAINETForCheckInOut("N");
                } else if ("1".equals(defSYSSTAT.getSysstatMbact1000()) || "1".equals(defSYSSTAT.getSysstatMbact1100())) {
                    if (!"3".equals(defSYSSTAT.getSysstatAoct1000()) && !"4".equals(defSYSSTAT.getSysstatAoct1000()) && !"5".equals(defSYSSTAT.getSysstatAoct1000())
                            && !"7".equals(defSYSSTAT.getSysstatAoct1000()) && !"9".equals(defSYSSTAT.getSysstatAoct1000()) && !"C".equals(defSYSSTAT.getSysstatAoct1000())
                            && !"D".equals(defSYSSTAT.getSysstatAoct1000()) && !"3".equals(defSYSSTAT.getSysstatAoct1100()) && !"4".equals(defSYSSTAT.getSysstatAoct1100())
                            && !"5".equals(defSYSSTAT.getSysstatAoct1100())
                            && !"7".equals(defSYSSTAT.getSysstatAoct1100()) && !"9".equals(defSYSSTAT.getSysstatAoct1100()) && !"C".equals(defSYSSTAT.getSysstatAoct1100())
                            && !"D".equals(defSYSSTAT.getSysstatAoct1100())) {
                        rtnCode = ofiscbusiness.updateEAINETForCheckInOut("Y");

                    }
                }
                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark("更新FEP成功，更新EAINET失敗");
                    getLogContext().setReturnCode(rtnCode);
                    inbkService.inbkLogMessage(Level.INFO, logContext);
                }
                //2022/09/12 Bruce modify 依照Candy需求取消Fcrmstat相關 Start
                //sendOKEMS(aryNeedSendEMS);
                //2022/09/12 Bruce modify 依照Candy需求取消Fcrmstat相關 end
                this.showMessage(mode, MessageType.INFO, UpdateSuccess);
                BindData(form, mode);
            } else {
                this.showMessage(mode, MessageType.WARNING, UpdateFail);
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            FEPBase.sendEMS(getLogContext());
            //20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 Start
            this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
            //this.showMessage(mode, MessageType.DANGER, ex.getMessage());
			//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 end
        }
        return Router.UI_019010.getView();
    }

    // DB相關
    private void BindData(UI_019010_Form form, ModelMap mode) {
        this.doKeepFormData(mode, form);

        try {
            Sysstat dtSysstat = inbkService.getStatus();
            //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat Start
            //Fcrmstat defFcrmstat = inbkService.getFCRMSTAT();
            //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat end
            if (dtSysstat != null) {
                form.setSysstat(dtSysstat); // 將查詢的結果塞入到form中
                form.getSysstat().setSysstatSoct(dtSysstat.getSysstatSoct() + "-" + getSoctName(dtSysstat.getSysstatSoct()));
                form.getSysstat().setSysstatMboct(dtSysstat.getSysstatSoct() + "-" + getSoctName(dtSysstat.getSysstatSoct()));
                form.getSysstat().setSysstatAoct2000(dtSysstat.getSysstatAoct2000() + "-" + getAoctName(dtSysstat.getSysstatAoct2000()));
                form.getSysstat().setSysstatAoct2200(dtSysstat.getSysstatAoct2200() + "-" + getAoctName(dtSysstat.getSysstatAoct2200()));
                form.getSysstat().setSysstatAoct2500(dtSysstat.getSysstatAoct2500() + "-" + getAoctName(dtSysstat.getSysstatAoct2500()));
                form.getSysstat().setSysstatAoct2510(dtSysstat.getSysstatAoct2510() + "-" + getAoctName(dtSysstat.getSysstatAoct2510()));
                form.getSysstat().setSysstatAoct2520(dtSysstat.getSysstatAoct2520() + "-" + getAoctName(dtSysstat.getSysstatAoct2520()));
                form.getSysstat().setSysstatAoct2530(dtSysstat.getSysstatAoct2530() + "-" + getAoctName(dtSysstat.getSysstatAoct2530()));
                form.getSysstat().setSysstatAoct2540(dtSysstat.getSysstatAoct2540() + "-" + getAoctName(dtSysstat.getSysstatAoct2540()));
                form.getSysstat().setSysstatAoct2550(dtSysstat.getSysstatAoct2550() + "-" + getAoctName(dtSysstat.getSysstatAoct2550()));
                form.getSysstat().setSysstatAoct2560(dtSysstat.getSysstatAoct2560() + "-" + getAoctName(dtSysstat.getSysstatAoct2560()));
                form.getSysstat().setSysstatAoct2570(dtSysstat.getSysstatAoct2570() + "-" + getAoctName(dtSysstat.getSysstatAoct2570()));
                form.getSysstat().setSysstatAoct7100(dtSysstat.getSysstatAoct7100() + "-" + getAoctName(dtSysstat.getSysstatAoct7100()));
                form.getSysstat().setSysstatAoct7300(dtSysstat.getSysstatAoct7300() + "-" + getAoctName(dtSysstat.getSysstatAoct7300()));

                // 自行
//                form.setIntra(DbHelper.toBoolean(dtSysstat.getSysstatIntra()));
//                form.setIwdI(DbHelper.toBoolean(dtSysstat.getSysstatIwdI()));
//                form.setIftI(DbHelper.toBoolean(dtSysstat.getSysstatIftI()));
//                form.setAdmI(DbHelper.toBoolean(dtSysstat.getSysstatAdmI()));
//                form.setFwdI(DbHelper.toBoolean(dtSysstat.getSysstatFwdI()));
//                form.setIpyI(DbHelper.toBoolean(dtSysstat.getSysstatIpyI()));
//                form.setIccdpI(DbHelper.toBoolean(dtSysstat.getSysstatIccdpI()));
//                form.setEtxI(DbHelper.toBoolean(dtSysstat.getSysstatEtxI()));
//                form.setCaI(DbHelper.toBoolean(dtSysstat.getSysstatCaI()));
//                form.setCaaI(DbHelper.toBoolean(dtSysstat.getSysstatCaaI()));
//                form.setAig(DbHelper.toBoolean(dtSysstat.getSysstatAig()));
//                form.setHkIssue(DbHelper.toBoolean(dtSysstat.getSysstatHkIssue()));
//                form.setMoIssue(DbHelper.toBoolean(dtSysstat.getSysstatMoIssue()));
//                form.setHkFiscmb(DbHelper.toBoolean(dtSysstat.getSysstatHkFiscmb()));
//                form.setMoFiscmb(DbHelper.toBoolean(dtSysstat.getSysstatMoFiscmb()));
//                form.setHkPlus(DbHelper.toBoolean(dtSysstat.getSysstatHkPlus()));
//                form.setMoPlus(DbHelper.toBoolean(dtSysstat.getSysstatMoPlus()));
                // 代理行
//                form.setAgent(DbHelper.toBoolean(dtSysstat.getSysstatAgent()));
//                form.setIwdA(DbHelper.toBoolean(dtSysstat.getSysstatIwdA()));
//                form.setIftA(DbHelper.toBoolean(dtSysstat.getSysstatIftA()));
//                form.setIpyA(DbHelper.toBoolean(dtSysstat.getSysstatIpyA()));
//                form.setIccdpA(DbHelper.toBoolean(dtSysstat.getSysstatIccdpA()));
//                form.setEtxA(DbHelper.toBoolean(dtSysstat.getSysstatEtxA()));
//                form.setT2525A(DbHelper.toBoolean(dtSysstat.getSysstat2525A()));
//                form.setCpuA(DbHelper.toBoolean(dtSysstat.getSysstatCpuA()));
//                form.setCafA(DbHelper.toBoolean(dtSysstat.getSysstatCafA()));
//                form.setCavA(DbHelper.toBoolean(dtSysstat.getSysstatCavA()));
//                form.setCamA(DbHelper.toBoolean(dtSysstat.getSysstatCamA()));
//                form.setCajA(DbHelper.toBoolean(dtSysstat.getSysstatCajA()));
//                form.setCauA(DbHelper.toBoolean(dtSysstat.getSysstatCauA()));
//                form.setCwvA(DbHelper.toBoolean(dtSysstat.getSysstatCwvA()));
//                form.setCwmA(DbHelper.toBoolean(dtSysstat.getSysstatCwmA()));
                // 原存
//                form.setIssue(DbHelper.toBoolean(dtSysstat.getSysstatIssue()));
//                form.setIwdF(DbHelper.toBoolean(dtSysstat.getSysstatIwdF()));
//                form.setIftF(DbHelper.toBoolean(dtSysstat.getSysstatIftF()));
//                form.setIpyF(DbHelper.toBoolean(dtSysstat.getSysstatIpyF()));
//                form.setIccdpF(DbHelper.toBoolean(dtSysstat.getSysstatIccdpF()));
//                form.setCdpF(DbHelper.toBoolean(dtSysstat.getSysstatCdpF()));
//                form.setEtxF(DbHelper.toBoolean(dtSysstat.getSysstatEtxF()));
//                form.setT2525F(DbHelper.toBoolean(dtSysstat.getSysstat2525F()));
//                form.setCpuF(DbHelper.toBoolean(dtSysstat.getSysstatCpuF()));
//                form.setGpcadF(DbHelper.toBoolean(dtSysstat.getSysstatGpcadF()));
//                form.setCauF(DbHelper.toBoolean(dtSysstat.getSysstatCauF()));
//                form.setGpcwdF(DbHelper.toBoolean(dtSysstat.getSysstatGpcwdF()));
                // 其他通道
                form.setCbs(DbHelper.toBoolean(dtSysstat.getSysstatCbs()));
//                form.setFedi(DbHelper.toBoolean(dtSysstat.getSysstatFedi()));
//                form.setNb(DbHelper.toBoolean(dtSysstat.getSysstatNb()));
//                form.setWebatm(DbHelper.toBoolean(dtSysstat.getSysstatWebatm()));
//                form.setAscChannel(DbHelper.toBoolean(dtSysstat.getSysstatAscChannel()));
//                form.setAsc(DbHelper.toBoolean(dtSysstat.getSysstatAsc()));
//                form.setAscmd(DbHelper.toBoolean(dtSysstat.getSysstatAscmd()));
//                form.setGcard(DbHelper.toBoolean(dtSysstat.getSysstatGcard()));
//                form.setSps(DbHelper.toBoolean(dtSysstat.getSysstatSps()));
//                form.setAscmac(DbHelper.toBoolean(dtSysstat.getSysstatAscmac()));
//                form.setSpsmac(DbHelper.toBoolean(dtSysstat.getSysstatSpsmac()));
            }
            WebUtil.putInAttribute(mode, AttributeName.Options, form);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage()); // 列印異常的log
            //20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 Start
            //this.showMessage(mode, MessageType.DANGER, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
            //20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 Start
        }
    }

//    private List<List<Object>> prepareNeedSendEMSList(UI_019010_Form form, Sysstat dtSYSSTAT, Fcrmstat defFCRMSTAT) {
//        List<List<Object>> aryNeedSendEMS = new ArrayList<>();
//        List<Object> itemNeedSendEMS = new ArrayList<>();
//        if (dtSYSSTAT.getSysstatMbact1000().equals(form.getSysstat().getSysstatMbact1000())) {
//            if (!dtSYSSTAT.getSysstatMbact1100().equals(form.getSysstat().getSysstatMbact1100())) {
//                itemNeedSendEMS.add("匯款類子系統(1100)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact1100())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact1200().equals(form.getSysstat().getSysstatMbact1200())) {
//                itemNeedSendEMS.add("代收款項類子系統(1200)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact1200())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact1300().equals(form.getSysstat().getSysstatMbact1300())) {
//                itemNeedSendEMS.add("代繳代發類子系統(1300)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact1300())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact1400().equals(form.getSysstat().getSysstatMbact1400())) {
//                itemNeedSendEMS.add("一般通信類子系統(1400)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact1400())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//        } else {
//            if ("2".equals(dtSYSSTAT.getSysstatMbact1000())) {
//                itemNeedSendEMS.add("通匯各類子系統(1000)");
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            } else {
//                itemNeedSendEMS.add("通匯各類子系統(1000)");
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact1100().equals(form.getSysstat().getSysstatMbact1000())) {
//                itemNeedSendEMS.add("匯款類子系統(1100)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact1100())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact1200().equals(form.getSysstat().getSysstatMbact1000())) {
//                itemNeedSendEMS.add("代收款項類子系統(1200)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact1200())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact1300().equals(form.getSysstat().getSysstatMbact1000())) {
//                itemNeedSendEMS.add("代繳代發類子系統(1300)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact1300())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact1400().equals(form.getSysstat().getSysstatMbact1000())) {
//                itemNeedSendEMS.add("一般通信類子系統(1400)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact1400())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//        }
//        if (!dtSYSSTAT.getSysstatMbact2000().equals(form.getSysstat().getSysstatMbact2000())) {
//            itemNeedSendEMS.add("CD/ATM 共用系統提款作業(2000)");
//            if ("2".equals(dtSYSSTAT.getSysstatMbact2000())) {
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//            } else {
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//            }
//            aryNeedSendEMS.add(itemNeedSendEMS);
//        }
//        if (!dtSYSSTAT.getSysstatMbact2200().equals(form.getSysstat().getSysstatMbact2200())) {
//            itemNeedSendEMS.add("CD/ATM 共用系統轉帳作業(2200)");
//            if ("2".equals(dtSYSSTAT.getSysstatMbact2200())) {
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//            } else {
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//            }
//            aryNeedSendEMS.add(itemNeedSendEMS);
//        }
//        if (dtSYSSTAT.getSysstatMbact2500().equals(form.getSysstat().getSysstatMbact2500())) {
//            if (!dtSYSSTAT.getSysstatMbact2510().equals(form.getSysstat().getSysstatMbact2510())) {
//                itemNeedSendEMS.add("晶片卡提款作業(2510)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2510())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2520().equals(form.getSysstat().getSysstatMbact2520())) {
//                itemNeedSendEMS.add("晶片卡提款作業(2520)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2520())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2530().equals(form.getSysstat().getSysstatMbact2530())) {
//                itemNeedSendEMS.add("晶片卡繳款作業(2530)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2530())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2540().equals(form.getSysstat().getSysstatMbact2540())) {
//                itemNeedSendEMS.add("晶片卡消費扣款作業(2540)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2540())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2550().equals(form.getSysstat().getSysstatMbact2550())) {
//                itemNeedSendEMS.add("晶片卡預先授權作業(2550)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2550())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2560().equals(form.getSysstat().getSysstatMbact2560())) {
//                itemNeedSendEMS.add("晶片卡全國繳費作業(2560)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2560())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2570().equals(form.getSysstat().getSysstatMbact2570())) {
//                itemNeedSendEMS.add("晶片卡跨國提款作業(2570)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2570())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//        } else {
//            if ("2".equals(dtSYSSTAT.getSysstatMbact2500())) {
//                itemNeedSendEMS.add("晶片卡共用系統(2500)");
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            } else {
//                itemNeedSendEMS.add("晶片卡共用系統(2500)");
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2510().equals(form.getSysstat().getSysstatMbact2500())) {
//                itemNeedSendEMS.add("晶片卡提款作業(2510)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2510())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2520().equals(form.getSysstat().getSysstatMbact2500())) {
//                itemNeedSendEMS.add("晶片卡轉帳作業(2520)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2520())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2530().equals(form.getSysstat().getSysstatMbact2500())) {
//                itemNeedSendEMS.add("晶片卡繳款作業(2530)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2530())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2540().equals(form.getSysstat().getSysstatMbact2500())) {
//                itemNeedSendEMS.add("晶片卡消費扣款作業(2540)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2540())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2550().equals(form.getSysstat().getSysstatMbact2500())) {
//                itemNeedSendEMS.add("晶片卡預先授權作業(2550)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2550())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2560().equals(form.getSysstat().getSysstatMbact2500())) {
//                itemNeedSendEMS.add("晶片卡全國繳費作業(2560)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2560())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//            if (!dtSYSSTAT.getSysstatMbact2570().equals(form.getSysstat().getSysstatMbact2500())) {
//                itemNeedSendEMS.add("晶片卡跨國提款作業(2570)");
//                if ("2".equals(dtSYSSTAT.getSysstatMbact2570())) {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//                } else {
//                    itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//                }
//                aryNeedSendEMS.add(itemNeedSendEMS);
//            }
//        }
//        if (!dtSYSSTAT.getSysstatMbact7100().equals(form.getSysstat().getSysstatMbact7100())) {
//            itemNeedSendEMS.add("轉帳退款類交易(7100)");
//            if ("2".equals(dtSYSSTAT.getSysstatMbact7100())) {
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//            } else {
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//            }
//            aryNeedSendEMS.add(itemNeedSendEMS);
//        }
//        if (!dtSYSSTAT.getSysstatMbact7300().equals(form.getSysstat().getSysstatMbact7300())) {
//            itemNeedSendEMS.add("FXML跨行付款交易(7300)");
//            if ("2".equals(dtSYSSTAT.getSysstatMbact7300())) {
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckIn);
//            } else {
//                itemNeedSendEMS.add(FISCReturnCode.MBankExceptionalCheckOut);
//            }
//            aryNeedSendEMS.add(itemNeedSendEMS);
//        }
//        return aryNeedSendEMS;
//    }

//    private void sendOKEMS(List<List<Object>> aryNeedSendEMS) { // 這個方法沒有回傳值
//        LogData logContext = new LogData();
//        logContext.setChannel(FEPChannel.FEP);
//        logContext.setSubSys(SubSystem.INBK);
//        logContext.setProgramName("UI_019010");
//        logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
//        logContext.setMessage("UI019010");
//        logContext.setMessageGroup("1");
//        logContext.setTxUser(WebUtil.getUser().getUserId());
//        for (List<Object> itemNeedSendEMS : aryNeedSendEMS) {
//            logContext.setMessageParm13(itemNeedSendEMS.get(0).toString());
//            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode((FEPReturnCode) itemNeedSendEMS.get(1), logContext));
//            inbkService.inbkLogMessage(Level.INFO, logContext);
//        }
//    }
}
