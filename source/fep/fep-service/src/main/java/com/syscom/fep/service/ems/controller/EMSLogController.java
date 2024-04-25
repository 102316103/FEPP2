package com.syscom.fep.service.ems.controller;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.log.LogMDC;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.service.ems.parser.EMSLogMessageParser;
import com.syscom.fep.service.ems.vo.EMSLogMessage;

/**
 * 接收其他服務透過logback api記錄的FEP Message
 * 
 * @author Richard
 *
 */
public class EMSLogController {
	private static final LogHelper SERVICELOGGER = LogHelperFactory.getServiceLogger();

	private EMSLogMessageParser parser;

	@PostConstruct
	public void registerParser() {
		parser = SpringBeanFactoryUtil.registerBean(EMSLogMessageParser.class);
	}

	@RequestMapping(value = "/api/EMS/SendMessage")
	@ResponseBody
	public String sendMessage(HttpServletRequest request) {
		LogMDC.put(Const.MDC_PGFILE, SvrConst.SVR_EMS);
		List<String> jsonStrList = new ArrayList<>();
		try (BufferedReader bufferedReader = request.getReader()) {
			String readLine;
			while ((readLine = bufferedReader.readLine()) != null) {
				jsonStrList.add(readLine);
			}
		} catch (Exception e) {
			SERVICELOGGER.exceptionMsg(e, e.getMessage());
		}
		if (!jsonStrList.isEmpty()) {
			List<EMSLogMessage> emsLogMessageList = new ArrayList<>();
			Gson gson = new Gson();
			for (String jsonStr : jsonStrList) {
				try {
					EMSLogMessage emsLogMessage = gson.fromJson(jsonStr, EMSLogMessage.class);
					SERVICELOGGER.debug("EMS Web service Message Type:", emsLogMessage.getMessageType(), ", MessageTarget=", emsLogMessage.getMessageTarget(), ",message=", emsLogMessage.toString());
					emsLogMessageList.add(emsLogMessage);
				} catch (Exception e) {
					SERVICELOGGER.exceptionMsg(e, "Parse JSON failed, json str = [", jsonStr, "]");
				}
			}
			try {
				parser.parseLog(emsLogMessageList);
			} catch (Exception e) {
				SERVICELOGGER.exceptionMsg(e, e.getMessage());
			}
		}
		return "OK";
	}
}
