package com.syscom.fep.notify.service;

import static com.syscom.fep.notify.cnst.NotifyConstant.NOTIFY_MESSAGE_FAILURES;
import static com.syscom.fep.notify.cnst.NotifyConstant.NOTIFY_TYPE_EMAIL;
import static com.syscom.fep.notify.cnst.NotifyConstant.NOTIFY_TYPE_NOCLASSIFIED;
import static com.syscom.fep.notify.cnst.NotifyConstant.NOTIFY_TYPE_SMS;
import static com.syscom.fep.notify.cnst.NotifyConstant.UPDATE_USER;
import static com.syscom.fep.notify.enums.NotifyStatusCode.CLIENTID_DUPLICATION;
import static com.syscom.fep.notify.enums.NotifyStatusCode.NOTIFY_FAILURE;
import static com.syscom.fep.notify.enums.NotifyStatusCode.NOTIFY_FINISH;
import static com.syscom.fep.notify.enums.NotifyStatusCode.NOTIFY_PARTIAL_FAILURE;
import static com.syscom.fep.notify.enums.NotifyStatusCode.NOTIFY_PROCESSING;
import static com.syscom.fep.notify.enums.NotifyStatusCode.NOTIFY_RULES_UNLESS;
import static com.syscom.fep.notify.enums.NotifyStatusCode.SEND_FAILURE;
import static com.syscom.fep.notify.enums.NotifyStatusCode.SEND_FINISH;
import static com.syscom.fep.notify.enums.NotifyStatusCode.SYSTEM_ERROR;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.mybatis.ext.mapper.NotifycontentExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NotifyrequestExtMapper;
import com.syscom.fep.mybatis.mapper.NotifycontentMapper;
import com.syscom.fep.mybatis.mapper.NotifyrequestMapper;
import com.syscom.fep.mybatis.model.Notifycontent;
import com.syscom.fep.mybatis.model.Notifyrequest;
import com.syscom.fep.notify.common.handler.NotifyEmailHandler;
import com.syscom.fep.notify.common.handler.NotifyNoClassifiedHandler;
import com.syscom.fep.notify.common.handler.NotifySMSHandler;
import com.syscom.fep.notify.dto.request.NotifyRequestForm;
import com.syscom.fep.notify.dto.response.LogNotifyResponse;
import com.syscom.fep.notify.enums.NotifyStatusCode;
import com.syscom.fep.notify.exception.NotifyException;
import com.syscom.fep.notify.model.NotifyContentResponse;

@Service
@EnableAsync
public class NotifyService {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotifyEmailHandler notifyEmailHandler;

    @Autowired
    private NotifyNoClassifiedHandler notifyNoClassifiedHandler;
    @Autowired
    private NotifySMSHandler notifySMSHandler;

    @Autowired
    private NotifyrequestMapper notifyrequestMapper;

    @Autowired
    private NotifyrequestExtMapper notifyrequestExtMapper;

    @Autowired
    private NotifycontentMapper notifycontentMapper;

    @Autowired
    private NotifycontentExtMapper notifycontentExtMapper;

    @Async
    public void createNotify(NotifyRequestForm inputRequestForm, Notifyrequest notifyRequest, List<Notifycontent> notifyContents, Set<String> failures) throws NotifyException, JsonProcessingException {
        notifyRequest.setClientId(inputRequestForm.getClientId());
        notifyRequest.setRequestParms(objectMapper.writeValueAsString(inputRequestForm.getContents()));
        notifyRequest.setStatus(NOTIFY_PROCESSING.getCode());
        notifyRequest.setUpdateUserId(UPDATE_USER);
        notifyRequest.setUpdateTime(new Date());
        if (!Objects.isNull(inputRequestForm.getRuleSetId()) && !Strings.isBlank(inputRequestForm.getRuleSetId())) {
            notifyRequest.setRulesetId(Long.valueOf(inputRequestForm.getRuleSetId()));
        }
        try {
            notifyrequestMapper.insert(notifyRequest);
        } catch (DuplicateKeyException e) {
            logger.warn(CLIENTID_DUPLICATION.getDesc());
            throw new NotifyException(CLIENTID_DUPLICATION, inputRequestForm.getClientId(), CLIENTID_DUPLICATION.getDesc());
        } catch (Exception e) {
            logger.error(e);
            throw new NotifyException(e, SYSTEM_ERROR, inputRequestForm.getClientId());
        }
        // 發送通知
        for (Notifycontent notifyContent : notifyContents) {
            // 開始發送通知
            Gson gson = new Gson();
            Map contents = gson.fromJson(notifyContent.getContent(), Map.class);
            NotifyStatusCode sendStatus = send(contents);
            notifyContent.setStatus(sendStatus.getCode());
            if (sendStatus.equals(SEND_FAILURE)) {
                failures.add(String.valueOf(notifyContent.getContentIndex()));
            }
            // 將 notifyContent 轉成 DB notifyocntent
            Notifycontent notifycontentDb = new Notifycontent();
            notifycontentDb.setRequestId(notifyRequest.getRequestId());
            notifycontentDb.setRequestSeq(notifyContent.getRequestSeq());
            notifycontentDb.setTemplateId(notifyContent.getTemplateId());
            notifycontentDb.setContentIndex(notifyContent.getContentIndex());
            notifycontentDb.setContent(objectMapper.writeValueAsString(StringUtils.isBlank(notifyContent.getContent()) ? "" : notifyContent.getContent()));
            notifycontentDb.setStatus(notifyContent.getStatus());
            notifycontentDb.setMessage(sendStatus.getDesc());
            notifycontentDb.setUpdateUserId(UPDATE_USER);
            notifycontentDb.setUpdateTime(new Date());
            // 寫入DB notifyContent
            notifycontentMapper.insert(notifycontentDb);
        }

        if (failures.size() == 0) {
            notifyRequest.setStatus(NOTIFY_FINISH.getCode());
        }
        if (failures.size() > 0 && failures.size() == inputRequestForm.getContents().size()) {
            notifyRequest.setStatus(NOTIFY_FAILURE.getCode());
            notifyRequest.setFailures(objectMapper.writeValueAsString(failures));
            Map<String, Object> message = new HashMap<>();
            message.put(NOTIFY_MESSAGE_FAILURES, failures);
        }
        if (failures.size() > 0 && failures.size() < inputRequestForm.getContents().size()) {
            notifyRequest.setStatus(NOTIFY_PARTIAL_FAILURE.getCode());
            notifyRequest.setFailures(objectMapper.writeValueAsString(failures));
            Map<String, Object> message = new HashMap<>();
            message.put(NOTIFY_MESSAGE_FAILURES, failures);
        }

        notifyRequest.setUpdateTime(new Date());
        // update DB notifyRequest status
        notifyrequestMapper.updateByPrimaryKey(notifyRequest);
    }

    private NotifyStatusCode send(Map<String, String> content) {
        try {
            if (NOTIFY_TYPE_EMAIL.equals(content.get("Type"))) {
                notifyEmailHandler.send(content);
            }
            if (NOTIFY_TYPE_SMS.equals(content.get("Type"))) {
                notifySMSHandler.send(content);
            }
            if (NOTIFY_TYPE_NOCLASSIFIED.equals(content.get("Type"))) {
                notifyNoClassifiedHandler.send(content);
            }

            return SEND_FINISH;
        } catch (Exception e) {
            logger.error(SEND_FAILURE.getDesc());

            return SEND_FAILURE;
        }
    }

    public LogNotifyResponse logNotify(NotifyRequestForm inputRequestForm, Notifyrequest notifyRequest) throws NotifyException, JsonProcessingException {
        LogNotifyResponse logNotifyResponse = new LogNotifyResponse();

        notifyRequest.setClientId(inputRequestForm.getClientId());
        notifyRequest.setRequestParms(objectMapper.writeValueAsString(inputRequestForm.getContents()));
        notifyRequest.setStatus(NOTIFY_PROCESSING.getCode());
        notifyRequest.setUpdateUserId(UPDATE_USER);
        notifyRequest.setUpdateTime(new Date());
        if (!Objects.isNull(inputRequestForm.getRuleSetId()) && !Strings.isBlank(inputRequestForm.getRuleSetId())) {
            notifyRequest.setRulesetId(Long.valueOf(inputRequestForm.getRuleSetId()));
        }
        try {
            //拿到NOTIFYREQUEST的REQUESTID後去查
            Notifyrequest notifyrequest = notifyrequestExtMapper.byClientId(notifyRequest.getClientId());
            if(notifyrequest.getStatus() == NOTIFY_PROCESSING.getCode()){//還在處理中就直接回傳
                throw new NotifyException(NOTIFY_PROCESSING, inputRequestForm.getClientId(), NOTIFY_PROCESSING.getDesc());
            }
            logNotifyResponse.setRequestId(String.valueOf(notifyrequest.getRequestId()));//通知請求ID
            logNotifyResponse.setStatus(notifyrequest.getStatus());//狀態

            List<Notifycontent> notifyContents = notifycontentExtMapper.getNotifycontentsByRequestId(notifyrequest.getRequestId().toString());
            if (notifyContents.size() == 0) {
                logger.info("檢查前端傳送的變數，查詢條件無符合資料。");
                Map<String, Object> msg = new HashMap<>();
                msg.put("訊息", "查詢NotifyContent條件無符合資料");       // 沒有查到通知請求記錄檔
                throw new NotifyException(NOTIFY_RULES_UNLESS, inputRequestForm.getClientId(), msg);
            }

            NotifyContentResponse notifyContentResponse = new NotifyContentResponse();
            List<NotifyContentResponse> notifyContentResponses = new ArrayList<>();
            for(Notifycontent notifycontent : notifyContents){
                notifyContentResponse.setContentIndex(notifycontent.getContentIndex());
                notifyContentResponse.setContentStatus(notifycontent.getStatus());
                notifyContentResponse.setMessage(notifycontent.getMessage());
                notifyContentResponses.add(notifyContentResponse);
            }
            logNotifyResponse.setClientId(inputRequestForm.getClientId());
            logNotifyResponse.seteJNo(inputRequestForm.geteJNo());
            logNotifyResponse.settXDate(inputRequestForm.gettXDate());
            logNotifyResponse.setNotifyContentResponses(notifyContentResponses);//NotifyContent

        } catch (DuplicateKeyException e) {
            logger.warn(CLIENTID_DUPLICATION.getDesc());
            throw new NotifyException(CLIENTID_DUPLICATION, inputRequestForm.getClientId(), CLIENTID_DUPLICATION.getDesc());
        } catch (NotifyException e) {
            logger.error(e);
            throw e;
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }

        return logNotifyResponse;
    }


}