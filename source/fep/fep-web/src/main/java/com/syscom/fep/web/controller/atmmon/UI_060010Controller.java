package com.syscom.fep.web.controller.atmmon;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.atmmon.UI_060010_FormDetail;
import com.syscom.fep.web.form.atmmon.UI_060010_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ATM憑證版本維護
 *
 * @author bruce
 */
@Controller
public class UI_060010Controller extends BaseController {
    private static final int MAX_LOOPS = Integer.MAX_VALUE; // 2024-04-24 Richard add add for 【Unchecked Input for Loop Condition】

    @Autowired
    private AtmService atmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_060010_FormMain form = new UI_060010_FormMain();
        form.setUrl("/atmmon/UI_060010/queryClick");
//		form.setRbtnDelFlg("2");//預設全部
        try {
            //廠牌下拉選單
            //this.setVendor(mode);//20230322 Bruce 先註解 TODO
            this.bindGridData(form, mode);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 查詢
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060010/queryClick")
    public String queryClick(@ModelAttribute UI_060010_FormMain form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        this.bindGridData(form, mode);
        this.doKeepFormData(mode, form);
        return Router.UI_060010.getView();
    }

    /**
     * ATM代號超連結
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060010/resultGrdvRowCommand")
    public String resultGrdvRowCommand(@ModelAttribute UI_060010_FormDetail formDetail, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", formDetail.toString(), "]");
        this.doKeepFormData(mode, formDetail);
        try {
            formDetail.setAtmAtmTypeCodeTxt(formDetail.getAtmAtmType() + "-" + formDetail.getAtmAtmTypeTxt());
            formDetail.setAtmLocCodeTxt(formDetail.getAtmLoc() + "-" + formDetail.getAtmLocTxt());
//			form.setAtmBrNoMaTxt(atmService.getBRAlias(form.getAtmBrNoMa()));
            //this.setVendor(mode);//廠牌下拉選單20230322 Bruce 先註解 TODO
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_060010_Detail.getView();
    }

    /*
     * 檔案上傳
     */
    @PostMapping(value = "/atmmon/UI_060010/upload")
    public String upload(@RequestParam("file") MultipartFile file, @ModelAttribute UI_060010_FormMain form,
                         ModelMap mode) throws Exception {
//	public String upload(@ModelAttribute UI_060010_FormMain form,ModelMap mode) throws Exception {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        form.setUrl("/atmmon/UI_060010/queryClick");
        this.doKeepFormData(mode, form);
        int i = 0;//總共幾筆
        int succ = 0;//成功
        int noFind = 0;//查無此ATM
        int err = 0;//失敗 successes = new ArrayList<String>();
        Workbook wb = null;
        StringBuilder sb = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        try {
            if (file.isEmpty()) {
                this.bindGridData(form, mode);
                this.showMessage(mode, MessageType.DANGER, selectExcelError);
                return Router.UI_060010.getView();
            }
            String fileName = file.getOriginalFilename().split("\\.")[1];
            //if(!"csv".equals(fileName)) {
            if (!"XLS".equalsIgnoreCase(fileName) && !"XLSX".equalsIgnoreCase(fileName)) {
                this.bindGridData(form, mode);
                this.showMessage(mode, MessageType.DANGER, uploadExcelError);
                return Router.UI_060010.getView();
            }
            InputStream inputStream = file.getInputStream();
            if ("XLS".equalsIgnoreCase(fileName)) {
                wb = new HSSFWorkbook(inputStream);
            } else if ("XLSX".equalsIgnoreCase(fileName)) {
                wb = new XSSFWorkbook(inputStream);
            }
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(0);
            if (row == null) {
                this.bindGridData(form, mode);
                this.showMessage(mode, MessageType.DANGER, contentEmpty);
                return Router.UI_060010.getView();
            } else {
                int rownum = sheet.getPhysicalNumberOfRows();//資料共幾列
                // 2024-04-24 Richard add add start for 【Unchecked Input for Loop Condition】
                if (rownum > MAX_LOOPS) {
                    rownum = MAX_LOOPS;
                }
                // 2024-04-24 Richard add add end for 【Unchecked Input for Loop Condition】
                int colnum = row.getPhysicalNumberOfCells();//每一列共幾欄
                if (colnum != 2) {
                    this.bindGridData(form, mode);
                    this.showMessage(mode, MessageType.DANGER, excelContentError);
                    return Router.UI_060010.getView();
                } else {
                    Map<String, Object> map = null;
                    List<String> atmNo = new ArrayList<String>(); ;//記錄查無ATM的編號
                    List<String> atmNoError = new ArrayList<String>(); ;//記錄查無ATM的編號
                    Object cellValue = null;
                    //i=0, 標題行
                    for (i = 1; i < rownum; i++) {
                        row = sheet.getRow(i);//取得第幾列
                        map = new HashMap<String, Object>();
                        for (int j = 0; j < colnum; j++) {
                            Cell cell = row.getCell(j);
                            switch (cell.getCellType()) {
                                case NUMERIC:
                                    cellValue = cell.getNumericCellValue();
                                    break;
                                case STRING:
                                    cellValue = cell.getRichStringCellValue().getString();
                                    break;
                                default:
                                    cellValue = "";
                            }
                            /** 讀取 **/
                            if (j == 0) {
                                map.put("atmAtmNo", cellValue.toString());
                            } else {
                                String sv = cellValue.toString();
                                String atmFepConnection;
                                if ("Y".equalsIgnoreCase(sv)) {
                                    atmFepConnection = "1";
                                } else {
                                    atmFepConnection = "0";
                                }

                                map.put("atmFepConnection", atmFepConnection);
                                try {
                                    int find = atmService.updateAtmmstr(map);
                                    if (find == 0) {
                                        noFind++;
                                        atmNo.add(map.get("atmAtmNo").toString());
                                    } else {
                                        succ++;
                                    }
                                } catch (Exception e) {
                                    err++;
                                    atmNoError.add(map.get("atmAtmNo").toString());
                                }
                            }
                        }
                    }
                    sb = this.getAtmNoToStringBuilder(atmNo);
                    sbErr = this.getAtmNoToStringBuilder(atmNoError);
                }
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.bindGridData(form, mode);
            this.showMessage(mode, MessageType.DANGER, programError);
            return Router.UI_060010.getView();
        }
        form.setAtmAtmNoTxt("");
        this.bindGridData(form, mode);
        if (i > 0) {
            //減少標題行
            i--;
        }
        this.showMessageWithArgs(mode, MessageType.SUCCESS, atmmstrUpdateSuccess, i, succ, err, sbErr, noFind, sb);
//		this.showMessage(mode, MessageType.SUCCESS, atmmstrUpdateSuccess);
        return Router.UI_060010.getView();
    }

    /**
     * 將錯誤及查無ATM的編號串起來丟至前端畫面
     *
     * @param atmNo
     * @return
     */
    private StringBuilder getAtmNoToStringBuilder(List<String> atmNo) {
        StringBuilder sb = new StringBuilder();
        //將查無ATM編號串起來
        for (int n = 0; n < atmNo.size(); n++) {
            if (atmNo.size() == 1) {
                sb.append("[" + atmNo.get(n) + "]");//只有一筆的時候
            } else if (n == 0) {
                sb.append("[" + atmNo.get(n) + ",");//不止一筆的第一筆
            } else if (atmNo.size() - 1 == n) {
                sb.append(atmNo.get(n) + "]");//不止一筆的最後一筆
            } else {
                sb.append(atmNo.get(n) + ",");//不止一筆的過程
            }
        }
        return sb;
    }

    /**
     * 修改頁面憑證版本更新 20230324 Bruce add
     *
     * @param form
     * @param mode
     * @return
     */
    @PostMapping(value = "/atmmon/UI_060010/updateCertalias")
    public String updateDetail(@ModelAttribute UI_060010_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        form.setUrl("/atmmon/UI_060010/queryClick");
        this.doKeepFormData(mode, form);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        try {
            Map<String, Object> map = form.toMap();
            atmService.updateAtmmstr(map);
        } catch (Exception e) {
            this.showMessage(mode, MessageType.INFO, UpdateFail);
            return Router.UI_060010_Detail.getView();
        }
        this.showMessage(mode, MessageType.INFO, UpdateSuccess);
        return Router.UI_060010_Detail.getView();
    }

    /**
     * 資料查詢
     *
     * @param form
     * @param mode
     */
    private void bindGridData(UI_060010_FormMain form, ModelMap mode) {
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        //分頁
        try {
//			if(form.isCheckCtrlEmvB()) {
//				form.setCheckCtrlEmv("1");
//			}else {
//				form.setCheckCtrlEmv("0");
//			}
            Map<String, Object> argsMap = form.toMap();
            argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
            PageInfo<Map<String, Object>> pageInfo = atmService.getAtmBasicList(argsMap);
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                //欄位加工
                this.setFields(pageInfo.getList(), mode);
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }
            PageData<UI_060010_FormMain, Map<String, Object>> pageData = new PageData<UI_060010_FormMain, Map<String, Object>>(pageInfo, form);
            //this.setVendor(mode);//廠牌下拉選單 20230322 Bruce 先註解 TODO
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 24小時服務及連線層換KEY 轉換是或否
     *
     * @param code
     * @return
     */
    private String bitToYesNo(String code) {
        switch (code) {
            case "0": return "否";
            case "1": return "是";
            default: return "";
        }
    }

    /**
     * 取得保全中文
     *
     * @param guardCode
     * @param mode
     * @return
     * @throws Exception
     */
    private String getGuardName(String guardCode, ModelMap mode) throws Exception {
//		Guard guard = atmService.getGuardName(guardCode);
//		if (guard != null) {
//			return guard.getGuardNameS();
//		}
        return "";
    }

    /**
     * 廠牌下拉選單 20230322 先註解 TODO
     * @throws Exception
     */
//	private void setVendor(ModelMap mode) throws Exception {
//		List<Vendor> vendorList = atmService.qetAllVendor();
//		List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
//		selectOptionList.add(new SelectOption<String>(StringUtils.EMPTY, StringUtils.EMPTY));
//		for (int i = 0; i < vendorList.size(); i++) {
//			selectOptionList.add(new SelectOption<String>(vendorList.get(i).getVendorNameS(), vendorList.get(i).getVendorNo()));
//		}
//		WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
//	}

    /**
     * 畫面顯示欄位加工
     *
     * @param argsMap
     * @throws Exception
     */
    private void setFields(List<Map<String, Object>> argsMapList, ModelMap mode) throws Exception {
        for (Map<String, Object> map : argsMapList) {
//			map.put("ATM_BRNO_ST_TXT", atmService.getBRAlias(map.get("ATM_BRNO_ST").toString()));
            map.put("ATM_BRNO_ST_TXT", "");
            map.put("ATM_CUR_ST_TXT", this.getZoneCurName(map.get("ATM_CUR_ST").toString()));
            map.put("ATM_ZONE_TXT", this.getAtmZoneName(map.get("ATM_ZONE") == null ? "" : map.get("ATM_ZONE").toString()));
            map.put("ATM_BRNO_ST_ALIAS_TXT", map.get("ATM_BRNO_ST") == null ? "" : map.get("ATM_BRNO_ST").toString() + "-" + this.getAtmZoneName(
                    map.get("ATM_BRNO_ST_ALIAS") == null ? "" : map.get("ATM_BRNO_ST_ALIAS").toString()));
//			map.put("ATM_VENDOR_TXT", this.getAtmDevVendorName(map.get("ATM_VENDOR") == null ? "" : map.get("ATM_VENDOR").toString(), mode));
            map.put("ATM_VENDOR_TXT", "");
            map.put("ATM_ATMTYPE_TXT", this.getAtmTypeName(map.get("ATM_ATMTYPE") == null ? "" : map.get("ATM_ATMTYPE").toString()));
            map.put("ATM_AREA_TXT", this.getAtmAreaName(map.get("ATM_AREA") == null ? "" : map.get("ATM_AREA").toString()));
            map.put("ATM_CHANNEL_TYPE_TXT", this.getAtmChannelTypeName(map.get("ATM_CHANNEL_TYPE") == null ? "" : map.get("ATM_CHANNEL_TYPE").toString()));
            map.put("ATM_LOC_TXT", this.getAtmLocName(map.get("ATM_LOC") == null ? "" : map.get("ATM_LOC").toString()));
            //為了更新後帶回前端
            map.put("rbtnDelFlg", map.get("ATM_DELETE_FG"));
            map.put("checkCtrlEmv", map.get("ATM_EMV"));
            map.put("rbtnOS", map.get("ATM_OS"));
            map.put("vendor", map.get("ATM_VENDOR"));
            map.put("typeQuery", map.get("ATM_ATMTYPE"));
            map.put("insBrno", map.get("ATM_INS_BRNO"));
            if (StringUtil.isNotBlank(map.get("ATM_FEP_CONNECTION").toString())) {
                map.put("ATM_FEP_CONNECTIONTXT", map.get("ATM_FEP_CONNECTION").toString().equals("1") ? "是" : "否");
            }
        }
    }

    /**
     * 轉換國家中文
     *
     * @param atmZone
     */
    private String getAtmZoneName(String atmZone) {
        if (StringUtils.isBlank(atmZone)) {
            return "";
        } else {
            switch (atmZone) {
                case "HKG": return "香港";
                case "MAC": return "澳門";
                case "TWN": return "台灣";
                default: return atmZone;
            }
        }
    }

    /**
     * 將廠牌轉成中文
     * @param atmVendor
     * @return
     * @throws Exception
     */
//	private String getAtmDevVendorName(String atmVendor,ModelMap mode) throws Exception {
//		return atmService.queryVendorNameByPK(atmVendor);
//	}

    /**
     * 型態別轉中文
     *
     * @param atmType
     * @return
     */
    private String getAtmTypeName(String atmType) {
        switch (atmType) {
            case "0": return "提款機";
            case "1": return "台外幣提款機";
            case "2": return "存提款機";
            case "3": return "WebATM";
            default: return "";
        }
    }

    /**
     * 地區代碼轉中文
     *
     * @param atmArea
     * @return
     */
    private String getAtmAreaName(String atmArea) {
        switch (atmArea) {
            case "1": return "北";
            case "2": return "中";
            case "3": return "南";
            case "4": return "新竹";
            case "5": return "高雄";
            default: return "";
        }
    }

    /**
     * 通路轉中文
     *
     * @param atmChannelType
     * @return
     */
    private String getAtmChannelTypeName(String atmChannelType) {
        switch (atmChannelType) {
            case "0": return "分行";
            case "1": return "策略設置";
            case "2": return "業務配合";
            case "3": return "證券";
            case "4": return "萊爾富";
            default: return "";
        }
    }

    /**
     * 行內外別轉中文
     *
     * @param atm24Service
     * @return
     */
    private String getAtmLocName(String atmLoc) {
        if (StringUtils.isBlank(atmLoc)) {
            return "";
        } else {
            switch (atmLoc) {
                case "0": return "行內";
                case "1": return "證券自";
                case "2": return "行外778結帳";
                default: return atmLoc;
            }
        }
    }
}
