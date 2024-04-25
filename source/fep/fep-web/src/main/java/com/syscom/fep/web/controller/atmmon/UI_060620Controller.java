package com.syscom.fep.web.controller.atmmon;

import java.sql.Clob;
import java.util.*;

import com.ibm.db2.jcc.am.SqlException;
import com.syscom.fep.mybatis.util.DB2Util;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.model.Alert;
import com.syscom.fep.mybatis.model.Msgkb;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.atmmon.UI_060620_B_Form;
import com.syscom.fep.web.form.atmmon.UI_060620_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 事件管理系統(EMS)日誌查詢
 *
 * @author Chenyu
 */
@Controller
public class UI_060620Controller extends BaseController {
	private static final String URL_DO_QUERY = "/atmmon/UI_060620/saveIntervalBtn_Click";

	@Autowired
	AtmService atmService;

	private PageInfo<HashMap<String, Object>> pageDate;

	private int index;

	@SuppressWarnings("unused")
	private String arNO = "";
	private String arERCODE = "";
	private String txExternalCode = "";
	private String arHOSTNAME = "";
	private String arDATE = "";
	private String arTIME = "";
	private String arSUBSYS = "";
	private String arSYS = "";
	private String arLEVEL = "";
	private String arERDESCRIPTION = "";
	private String arREMARK = "";
	private String txExSubCode = "";
	private HashMap<String, String> thisDate;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_060620_Form form = new UI_060620_Form();
		String time = "30";
		form.setTime(time);
		form.setDtTransactDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
		form.setTxTransactTimeBEG("00:00");
		form.setTxTransactTimeEND("23:59");
		form.setUrl(URL_DO_QUERY);
		this.saveIntervalBtn_Click(form, mode);
	}

	private PageInfo<HashMap<String, Object>> queryData(ModelMap mode, UI_060620_Form form) {
		String txTransactDate = "";
		String txDateTimeBeg = "";
		String txDateTimeEnd = "";
		String sLevel = "";
		String sAtmNo = "";
		String sIP = "";
		String sApplication = "";

		if (!"".equals(form.getDtTransactDate())) {
			txTransactDate = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
		} else {
			String tradingDate = StringUtils.replace(form.getDtTransactDate(), "-", StringUtils.EMPTY);
			form.setDtTransactDate(tradingDate);
			txTransactDate = form.getDtTransactDate();
		}
		if (!"".equals(form.getTxTransactTimeBEG())) {
			txDateTimeBeg = form.getTxTransactTimeBEG();
		} else {
			txDateTimeBeg = "00:00";
			form.setTxTransactTimeBEG("00:00");
		}
		if (!"".equals(form.getTxTransactTimeEND())) {
			txDateTimeEnd = form.getTxTransactTimeEND();
		} else {
			txDateTimeEnd = "23:59";
			form.setTxTransactTimeEND("23:59");
		}
		sAtmNo = form.getTxtATMNo();
		sIP = form.getTxtIP();

		sLevel = "";
		sApplication = form.getApplicationDdl();

		String subsys = form.getSubSystemDdl();

		PageInfo<HashMap<String, Object>> dt = getResultData(form, mode, subsys, txTransactDate, txDateTimeBeg, txDateTimeEnd, sLevel, sAtmNo, sIP, sApplication);
		WebUtil.putInAttribute(mode, AttributeName.PageData, dt);
		pageDate = dt;
		return dt;
	}

	private PageInfo<HashMap<String, Object>> getResultData(UI_060620_Form form, ModelMap mode, String AR_SUBSYS, String sDate, String sTimeBeg, String sTimeEnd, String sLevel, String sATMNo,
															String sIP, String sApplication) {

		try {
			String begDateTime = sDate + StringUtils.replace(sTimeBeg, ":", StringUtils.EMPTY) + "00";
			String endDateTime = sDate + StringUtils.replace(sTimeEnd, ":", StringUtils.EMPTY) + "00";
			PageInfo<HashMap<String, Object>> dt = atmService.queryAlertData(AR_SUBSYS, begDateTime, endDateTime, sLevel, sATMNo, sIP, sApplication, form.getPageNum(), form.getPageSize());

			int i = 0;
			if (dt.getList().size() > 0) {
				int tempVar = dt.getList().size();
				for (i = 0; i < tempVar; i++) {
					String APMessagexml =DB2Util.getClobValue(dt.getList().get(i).get("AR_MESSAGE"), StringUtils.EMPTY);
					APMessagexml = "<APMessage>" + APMessagexml + "</APMessage>";
					APMessagexml = APMessagexml.replace("<LogData>", "").replace("</LogData>", "");
					Element root = XmlUtil.load(APMessagexml);
					if (!dt.getList().get(i).containsKey("AR_ERDESCRIPTION")) {
						dt.getList().get(i).put("AR_ERDESCRIPTION", "");
					}
					dt.getList().get(i).put("EJ", XmlUtil.getChildElementValue(root, "EJ", StringUtils.EMPTY));
					dt.getList().get(i).put("MSGID", XmlUtil.getChildElementValue(root, "MessageId", StringUtils.EMPTY));
					dt.getList().get(i).put("Channel", XmlUtil.getChildElementValue(root, "Channel", StringUtils.EMPTY));
					dt.getList().get(i).put("ATMNO", XmlUtil.getChildElementValue(root, "ATMNo", StringUtils.EMPTY));
					dt.getList().get(i).put("STAN", XmlUtil.getChildElementValue(root, "STAN", StringUtils.EMPTY));
					dt.getList().get(i).put("SBK", XmlUtil.getChildElementValue(root, "Bkno", StringUtils.EMPTY));
					dt.getList().get(i).put("DBK", XmlUtil.getChildElementValue(root, "DesBkno", StringUtils.EMPTY));
					dt.getList().get(i).put("TxUser", XmlUtil.getChildElementValue(root, "TxUser", StringUtils.EMPTY));
				}
			}
			return dt;
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, "讀取資料有誤");
			return null;
		}

	}

	@SuppressWarnings("unused")
	private PageInfo<HashMap<String, Object>> bindGrid(ModelMap mode, Alert dt, String sortExpression, String direction, UI_060620_Form form) {
		try {
			if (dt == null) {
				String subsys = form.getSubSystemDdl();
				PageInfo<HashMap<String, Object>> gdbt = queryData(mode, form);
			}
			if (StringUtils.isNoneBlank(sortExpression)) {
				if ("ASC".equals(direction)) {
					direction = "ASC";
				} else {
					direction = "DESC";
				}
			}
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, "讀取資料有誤");
			return null;
		}
		return null;
	}

	// UpdatePanel設定
	@PostMapping(value = URL_DO_QUERY)
	protected String saveIntervalBtn_Click(@ModelAttribute UI_060620_Form form, ModelMap mode) {
		this.doKeepFormData(mode, form);
		String time = form.getTime();
		if ("30".equals(time)) {
			queryData(mode, form);
			updateDataPnl1_Tick(mode, form);
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		} else {
			form.setTime(time);
			queryData(mode, form);
			updateDataPnl1_Tick(mode, form);
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		}
		return Router.UI_060620.getView();
	}

	// 計時器發動的事件
	private void updateDataPnl1_Tick(ModelMap mode, UI_060620_Form form) {
		String time = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
		StringBuffer str = new StringBuffer(time);
		str.insert(4, "年");
		str.insert(7, "月");
		str.insert(10, "日");
		str.insert(13, "點");
		str.insert(16, "分");
		str.insert(19, "秒");
		mode.addAttribute("newtime", str);
		queryData(mode, form);
	}

	// Grid中第一列查詢按鈕
	@PostMapping(value = "/atmmon/UI_060620_A/inquiryDetail")
	public String doinquiremsg(@ModelAttribute UI_060620_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);

		try {
			String AR_NO = form.getArNo();
			String APMessage = "";
			APMessage = atmService.queryAR_Message(AR_NO);
			APMessage = "<APMessage>" + APMessage + "</APMessage>";
			APMessage = APMessage.replace("<LogData>", "").replace("</LogData>", "");
			Element root = XmlUtil.load(APMessage);
			List<HashMap<String, String>> list = new ArrayList<>();
			HashMap<String, String> hashMap = new HashMap<>();
			hashMap.put("name", "EJNumber");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "EJ", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "ATMNo");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "ATMNo", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "ATMSeq");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "ATMSeq", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "STAN");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "STAN", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "SBK");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "Bkno", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "DBK");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "DesBkno", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "FiscRC");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "FiscRC", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "TxChannel");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "Channel", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "TxProgram");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "ProgramName", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "TxUser");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "TxUser", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "TxMessage");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "Message", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "TxSource");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "TxSource", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "TxDesc");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "TxErrDesc", StringUtils.EMPTY));
			list.add(hashMap);
			hashMap = new HashMap<>();
			hashMap.put("name", "ExStack");
			hashMap.put("value", XmlUtil.getChildElementValue(root, "ExStack", StringUtils.EMPTY));
			list.add(hashMap);
			WebUtil.putInAttribute(mode, AttributeName.DetailMap, list);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_060620_A.getView();
	}

	// Grid中最後一列查詢按鈕
	@PostMapping(value = "/atmmon/UI_060620_B/inquiryDetail")
	public String doinQuireMsg(@ModelAttribute UI_060620_B_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		index = Integer.parseInt(form.getIndex());
		try {
			getAlertData(index);
			List<Msgkb> refBase = null;
			bindFormViewData(refBase, mode);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_060620_B.getView();
	}

	private List<Msgkb> getResultDatas() {
		return atmService.get_AlertDetailForUI060620B(arERCODE, txExternalCode, txExSubCode);
	}

	private void getAlertData(int index) throws Exception {
		HashMap<String, Object> hashMap = pageDate.getList().get(index);
		String apmessage = DB2Util.getClobValue(hashMap.get("AR_MESSAGE"), StringUtils.EMPTY);
		if (StringUtils.isNotBlank(apmessage)) {
			apmessage = "<EMS>" + apmessage + "</EMS>";
			apmessage = apmessage.replace("<LogData>", "").replace("</LogData>", "");
			Element root = XmlUtil.load(apmessage);
			String nTxExternalCode = XmlUtil.getChildElementValue(root, "TxExternalCode", StringUtils.EMPTY);
			if (StringUtils.isNotBlank(nTxExternalCode)) {
				txExternalCode = nTxExternalCode;
			}
			String nRemark = XmlUtil.getChildElementValue(root, "Remark", StringUtils.EMPTY);
			if (StringUtils.isNotBlank(nRemark)) {
				arREMARK = nRemark;
			}else {
				arREMARK = (String) hashMap.get("AR_ERCODE");
			}
			String nExSubCode = XmlUtil.getChildElementValue(root, "ExSubCode", StringUtils.EMPTY);
			if (StringUtils.isNotBlank(nExSubCode)) {
				txExSubCode = nExSubCode;
			}
		}
		arSYS = (String) hashMap.get("AR_SYS");
		arSUBSYS = (String) hashMap.get("AR_SUBSYS");
		arDATE = hashMap.get("AR_DATETIME") != null ? hashMap.get("AR_DATETIME").toString().substring(0, 10) : StringUtils.EMPTY;
		arTIME = hashMap.get("AR_DATETIME") != null ? hashMap.get("AR_DATETIME").toString().substring(11, 19) : StringUtils.EMPTY;
		arHOSTNAME = (String) hashMap.get("AR_HOSTNAME");
		arERCODE = (String) hashMap.get("AR_ERCODE");
		arLEVEL = (String) hashMap.get("AR_LEVEL");
		arERDESCRIPTION = (String) hashMap.get("AR_ERDESCRIPTION");
	}

	/**
	 * <modifier>Anna Lin</modifier>
	 * <reason>Spec. 修改</reason>
	 * <date>2011/7/25</date>
	 * <modifier>Anna Lin</modifier>
	 * <reason>MSGKB 新增欄位</reason>
	 * <date>2012/4/23</date>
	 */
	private List<Msgkb> bindFormViewData(List<Msgkb> dt, ModelMap mode) {

		try {
//			if (dt == null) {
			dt=getResultDatas();
//			}
			HashMap<String, String> hashMap = new HashMap<>();
			if (dt.size() == 0) {
				hashMap.put("lblErrCode", arERCODE);
				hashMap.put("lblSeverity", arLEVEL);
				hashMap.put("lblExternalCode", txExternalCode);
				hashMap.put("lblExSubCode", txExSubCode);
				hashMap.put("description", arERDESCRIPTION);
				hashMap.put("remark", arREMARK);
				hashMap.put("chbNotify", "0");
				hashMap.put("responsible", "");
				hashMap.put("notifyMail", "");
				hashMap.put("action", "");
			} else {
				hashMap.put("lblErrCode", dt.get(0).getErrorcode());
				hashMap.put("chbNotify", dt.get(0).getNotify().toString());
				hashMap.put("lblExternalCode", dt.get(0).getExternalcode());
				hashMap.put("lblExSubCode", dt.get(0).getExsubcode());
				hashMap.put("responsible", dt.get(0).getResponsible());
				hashMap.put("notifyMail", dt.get(0).getNotifymail());
				hashMap.put("action", dt.get(0).getAction());
				hashMap.put("lblSeverity", dt.get(0).getSeverity());
				// 訊息與說明欄一律以EMS的為主

				hashMap.put("description", dt.get(0).getDescription());
				hashMap.put("remark", dt.get(0).getRemark());
			}
			hashMap.put("lblDate", arDATE);
			hashMap.put("lblTime", arTIME);
			hashMap.put("lblHost", arHOSTNAME);
			hashMap.put("lblSys", arSYS);
			hashMap.put("lblSubSys", arSUBSYS);
			thisDate = hashMap;
			WebUtil.putInAttribute(mode, AttributeName.DetailMap, hashMap);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return dt;
	}



	@PostMapping(value = "/atmmon/UI_060620_B/updateDetail")
	@ResponseBody
	protected BaseResp<?> updateDetail(@RequestBody UI_060620_B_Form form) {
		BaseResp<?> response = new BaseResp<>();
		Msgkb defMSGKB = new Msgkb();
		int iRes = 0;

		try {
			String sERCode = thisDate.get("lblErrCode");
			String TxExternalCode = thisDate.get("lblExternalCode");
			String TxExSubCode = thisDate.get("lblExSubCode");
			@SuppressWarnings("unused")
			String AR_LEVEL = thisDate.get("lblSeverity");
			String bNotify = form.getChbNotify();

			String sAction = form.getAction();
			String sRemark = form.getRemark();
			String sResponsible = form.getResponsible();
			String sDescription = form.getDescription();
			String sEmail = form.getNotifyMail();

			// 若勾選自動通知,則電子郵件一欄必須有資料
			if (DbHelper.toBoolean(bNotify)) {
				if (StringUtils.isBlank(sEmail)) {
					response.setMessage(MessageType.INFO, "若勾選自動通知,則電子郵件一欄必須有資料");
					return response;
				}
			}

			defMSGKB.setErrorcode(sERCode);
			defMSGKB.setExternalcode(TxExternalCode);
			if (StringUtils.isNotBlank(TxExSubCode)) {
				defMSGKB.setExsubcode(TxExSubCode);
			}
			defMSGKB.setSeverity(arLEVEL);
			defMSGKB.setDescription(sDescription);
			defMSGKB.setRemark(sRemark);
			defMSGKB.setNotify(Short.parseShort(bNotify));
			defMSGKB.setAction(sAction);
			defMSGKB.setResponsible(sResponsible);
			defMSGKB.setNotifymail(sEmail);

			if (atmService.chkExistInMSGKB(sERCode, TxExternalCode, TxExSubCode)) {
				// update
				iRes = atmService.updateMSGKB(defMSGKB);
				if (iRes > 0) {
					response.setMessage(MessageType.SUCCESS, UpdateSuccess);
				} else {
					response.setMessage(MessageType.DANGER, UpdateFail);
				}
			} else {
				// insert
				iRes = atmService.insertMSGKB(defMSGKB);
				if (iRes > 0) {
					response.setMessage(MessageType.SUCCESS, InsertSuccess);
				} else {
					response.setMessage(MessageType.DANGER, InsertFail);
				}
			}

		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			response.setMessage(MessageType.DANGER, programError);
		}

		return response;
	}

	// 上一筆
	@PostMapping(value = "/atmmon/UI_060620_B/inquiryPrev")
	public String inquiryPrev(@ModelAttribute UI_060620_B_Form form, ModelMap mode) {
		try {
			if (index == 0) {
				WebUtil.putInAttribute(mode, AttributeName.DetailMap, thisDate);
				this.showMessage(mode, MessageType.INFO, FirstRecord);
			} else {
				index = index - 1;
				getAlertData(index);
				List<Msgkb> refBase = null;
				refBase = bindFormViewData(refBase, mode);
			}
			return Router.UI_060620_B.getView();
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
			return Router.UI_060620_B.getView();
		}
	}

	// 下一筆
	@PostMapping(value = "/atmmon/UI_060620_B/inquiryNext")
	public String inquiryNext(@ModelAttribute UI_060620_B_Form form, ModelMap mode) {
		try {
			if (index == pageDate.getList().size() - 1) {
				WebUtil.putInAttribute(mode, AttributeName.DetailMap, thisDate);
				this.showMessage(mode, MessageType.INFO, LastRecord);
			} else {
				index = index + 1;
				getAlertData(index);
				List<Msgkb> refBase = null;
				refBase = bindFormViewData(refBase, mode);
			}
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_060620_B.getView();
	}

}
