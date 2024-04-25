package com.syscom.fep.web.configurer;

import javax.servlet.http.HttpServletRequest;

import com.syscom.fep.web.base.WebConst;
import com.syscom.fep.web.entity.AttributeName;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.util.WebUtil;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionControllerAdvice {

	@ExceptionHandler(Exception.class)
	public Object globalException(HttpServletRequest request, HandlerMethod handlerMethod, Throwable t) {
		LogHelperFactory.getTraceLogger().error(t, t.getMessage());
		if (WebUtil.isAjax(request, handlerMethod)) {
			BaseResp<String> response = new BaseResp<>();
			response.setAjaxErr(true);
			response.setMessage(MessageType.DANGER, ExceptionUtil.getStackTrace(t));
			if (WebConst.RESPONSE_TYPE_BLOB.equals(request.getHeader(WebConst.REQUEST_HEADER_KEY_RESPONSE_TYPE))) {
				return ResponseEntity.ok(response);
			} else {
				return response;
			}
		} else {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName(Router.PAGE_500.getView());
			WebUtil.putInAttribute(modelAndView.getModelMap(), AttributeName.ExecStackTrace, ExceptionUtil.getStackTrace(t));
			return modelAndView;
		}
	}
}
