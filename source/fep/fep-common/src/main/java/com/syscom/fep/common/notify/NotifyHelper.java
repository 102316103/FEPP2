package com.syscom.fep.common.notify;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.delegate.AsynchronousListener;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpResultCode;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.notify.dto.request.NotifyRequestContent;
import com.syscom.fep.notify.dto.request.NotifyRequestForm;
import com.syscom.fep.notify.dto.response.NotifyResponse;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = NotifyHelperConstant.CONFIGURATION_PROPERTIES_PREFIX, name = {NotifyHelperConstant.CONFIGURATION_PROPERTIES_URL_SEND_NOTIFY, NotifyHelperConstant.CONFIGURATION_PROPERTIES_URL_LOG_NOTIFY})
public class NotifyHelper extends NotifyHelperConstant {
    @Autowired
    private NotifyHelperConfiguration configuration;
    @Autowired
    private ObjectMapper objectMapper;
    private HttpClient httpClient;
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    private final ExecutorService executor = Executors.newCachedThreadPool(new SimpleThreadFactory("NotifyHelper"));

    @PostConstruct
    public void initialization() {
        httpClient = new HttpClient(configuration.isRecordHttpLog());
    }

    @PreDestroy
    public void destroy() {
        logger.trace("NotifyHelper start to destroy...");
        try {
            this.executor.shutdown(); // 記得要關閉
            if (this.executor.awaitTermination(60, TimeUnit.SECONDS))
                logger.trace("NotifyHelper executor terminate all runnable successful");
            else
                logger.trace("NotifyHelper executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }

    /**
     * 依據指定的templateId送mail
     * <p>
     * 注意NotifyTemplate檔中Template欄位要有##Body##
     *
     * @param templateId 模板樣式編號
     * @param to         收件人地址
     * @param body       郵件內容
     * @param async      是否異步發送
     * @return
     * @throws Exception
     */
    public NotifyResponse sendSimpleMail(String templateId, String to, String body, boolean async) throws Exception {
        // parameter
        Map<String, String> paramVars = new HashMap<>();
        paramVars.put(NOTIFY_MESSAGE_CONTENT_BODY, body); // Mail內容
        return this.sendSimpleMail(templateId, to, paramVars, async);
    }

    /**
     * 依據指定的templateId送mail
     * <p>
     * 注意NotifyTemplate檔中Template欄位要有##Body##
     *
     * @param templateId 模板樣式編號
     * @param to         收件人地址
     * @param paramVars  郵件內容中需要替代的變數
     * @param async      是否異步發送
     * @return
     * @throws Exception
     */
    public NotifyResponse sendSimpleMail(String templateId, String to, Map<String, String> paramVars, boolean async) throws Exception {
        logger.info("start to sendSimpleMail,templateId:", templateId, ",to:", to, ",paramVars:", paramVars, ",async:", async);
        // parameter
        Map<String, String> parameter = new HashMap<>();
        parameter.put(NOTIFY_EMAIL_PARM_NAME, to); // 固定變數名稱, 表示接收通知的email address
        if (MapUtils.isNotEmpty(paramVars)) parameter.putAll(paramVars); // 加入自定義的變數
        // NotifyRequestContent
        NotifyRequestContent content = new NotifyRequestContent();
        content.setTemplateId(templateId); // 必填, 模板樣式編號
        content.setContentIndex(UUIDUtil.randomUUID()); // 必填, 訊息處理序列號, 為UUID, 作為辨別查詢處理的發送通知
        content.setParmVars(parameter); // 必填, 參數群組, 裡面為自定義參數(模板和規則詳細參數要用的)
        // NotifyRequestForm
        NotifyRequestForm form = new NotifyRequestForm();
        form.setContents(Collections.singletonList(content)); // 必填
        return this.sendNotify(form, async);
    }

    /**
     * 依據指定的templateId送簡訊
     *
     * @param templateId 模板樣式編號
     * @param phone      手機門號
     * @param paramVars  簡訊內容中需要替代的變數
     * @param async      是否異步發送
     * @return
     * @throws Exception
     */
    public NotifyResponse sendSimpleSMS(String templateId, String phone, Map<String, String> paramVars, boolean async) throws Exception {
        logger.info("start to sendSimpleSMS,templateId:", templateId, ",phone:", phone, ",paramVars:", paramVars, ",async:", async);
        // parameter
        Map<String, String> parameter = new HashMap<>();
        parameter.put(NOTIFY_PHONE_PARM_NAME, phone); // 固定變數名稱, 表示接收通知的手機門號
        if (MapUtils.isNotEmpty(paramVars)) parameter.putAll(paramVars); // 加入自定義的變數
        // NotifyRequestContent
        NotifyRequestContent content = new NotifyRequestContent();
        content.setTemplateId(templateId); // 必填, 模板樣式編號
        content.setContentIndex(UUIDUtil.randomUUID()); // 必填, 訊息處理序列號, 為UUID, 作為辨別查詢處理的發送通知
        content.setParmVars(parameter); // 必填, 參數群組, 裡面為自定義參數(模板和規則詳細參數要用的)
        // NotifyRequestForm
        NotifyRequestForm form = new NotifyRequestForm();
        form.setContents(Collections.singletonList(content)); // 必填
        return this.sendNotify(form, async);
    }

    /**
     * @param clientId 由交易序號+交易日期組成, 如沒有交易序號或交易日期則由訊息通知中心產生UUID
     * @param ej       交易序號, 非必填
     * @param txDate   交易日期, 非必填
     * @return
     * @throws Exception
     */
    public NotifyResponse logNotify(String clientId, Integer ej, String txDate) throws Exception {
        NotifyRequestForm form = new NotifyRequestForm();
        form.setClientId(clientId);
        if (ej != null)
            form.seteJNo(Integer.toString(ej));
        if (StringUtils.isNotBlank(txDate))
            form.settXDate(txDate);
        return this.logNotify(form);
    }

    /**
     * 請求傳送訊息通知
     *
     * @param form
     * @param async
     * @return
     * @throws Exception
     */
    public NotifyResponse sendNotify(final NotifyRequestForm form, final boolean async) throws Exception {
        return this.sendNotify(form, async, null);
    }

    /**
     * 請求傳送訊息通知
     *
     * @param form
     * @param async
     * @param listener
     * @return
     * @throws Exception
     */
    public NotifyResponse sendNotify(final NotifyRequestForm form, final boolean async, final AsynchronousListener<NotifyResponse> listener) throws Exception {
        if (async) {
            executor.execute(() -> {
                try {
                    NotifyResponse response = this.sendNotify(form);
                    if (listener != null) listener.callback(response);
                } catch (Exception e) {
                    throw ExceptionUtil.createRuntimeException(e);
                }
            });
            return null;
        } else {
            return this.sendNotify(form);
        }
    }

    /**
     * 請求傳送訊息通知
     *
     * @param form
     * @return
     * @throws Exception
     */
    public NotifyResponse sendNotify(NotifyRequestForm form) throws Exception {
        try {
            String request = objectMapper.writeValueAsString(form);
            String resultStr = httpClient.doPost(configuration.getUrl().getSendNotify(), MediaType.APPLICATION_JSON_UTF8, configuration.getTimeout() * 1000, request);
            return objectMapper.readValue(resultStr, NotifyResponse.class);
        } catch (Exception e) {
            throw handleException(configuration.getUrl().getSendNotify(), e);
        }
    }

    /**
     * 請求查詢訊息通知
     *
     * @param form
     * @param async
     * @return
     * @throws Exception
     */
    public NotifyResponse logNotify(final NotifyRequestForm form, final boolean async) throws Exception {
        return this.logNotify(form, async, null);
    }

    /**
     * 請求查詢訊息通知
     *
     * @param form
     * @param async
     * @param listener
     * @return
     * @throws Exception
     */
    public NotifyResponse logNotify(final NotifyRequestForm form, final boolean async, final AsynchronousListener<NotifyResponse> listener) throws Exception {
        if (async) {
            executor.execute(() -> {
                try {
                    NotifyResponse response = this.logNotify(form);
                    if (listener != null) listener.callback(response);
                } catch (Exception e) {
                    throw ExceptionUtil.createRuntimeException(e);
                }
            });
            return null;
        } else {
            return this.logNotify(form);
        }
    }

    /**
     * 請求查詢訊息通知
     *
     * @param form
     * @return
     * @throws Exception
     */
    public NotifyResponse logNotify(NotifyRequestForm form) throws Exception {
        try {
            String request = objectMapper.writeValueAsString(form);
            String resultStr = httpClient.doPost(configuration.getUrl().getLogNotify(), MediaType.APPLICATION_JSON_UTF8, configuration.getTimeout() * 1000, request);
            return objectMapper.readValue(resultStr, NotifyResponse.class);
        } catch (Exception e) {
            throw handleException(configuration.getUrl().getLogNotify(), e);
        }
    }

    /**
     * 處理異常
     *
     * @param url
     * @param e
     * @return
     */
    private Exception handleException(String url, Exception e) {
        if (e instanceof JacksonException) {
            return ExceptionUtil.createException(e, logger.error(e, "訪問中心服務「", url, "」解讀請求/回應訊息出現異常"));
        } else if (HttpResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
            return ExceptionUtil.createException(e, logger.error(e, "訪問通知中心服務「", url, "」被拒絕"));
        } else if (HttpResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
            return ExceptionUtil.createTimeoutException(e, logger.error(e, "等待通知中心服務「", url, "」回應超時"));
        }
        return ExceptionUtil.createTimeoutException(e, logger.error(e, "訪問通知中心服務「", url, "」發生異常"));
    }
}
