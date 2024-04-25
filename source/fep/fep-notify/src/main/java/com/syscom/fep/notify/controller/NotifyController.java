package com.syscom.fep.notify.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.mybatis.model.Notifycontent;
import com.syscom.fep.mybatis.model.Notifyrequest;
import com.syscom.fep.mybatis.model.Notifyrule;
import com.syscom.fep.mybatis.model.Notifytemplate;
import com.syscom.fep.notify.common.handler.CustomerRuleHandler;
import com.syscom.fep.notify.dto.request.NotifyRequestContent;
import com.syscom.fep.notify.dto.request.NotifyRequestForm;
import com.syscom.fep.notify.dto.response.LogNotifyResponse;
import com.syscom.fep.notify.dto.response.NotifyResponse;
import com.syscom.fep.notify.exception.NotifyException;
import com.syscom.fep.notify.model.NotifyRuleSetExt;
import com.syscom.fep.notify.service.*;
import com.syscom.fep.notify.util.DateTimeConvertUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.syscom.fep.notify.cnst.NotifyConstant.*;
import static com.syscom.fep.notify.enums.NotifyStatusCode.*;
import static com.syscom.fep.notify.util.NotifyExpressionParse.expressionParse;

@RestController
public class NotifyController {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private NotifyTemplateService notifyTemplateService;

    @Autowired
    private NotifyRuleSetService notifyRuleSetService;

    @Autowired
    private NotifyRuleService notifyRuleService;

    @Autowired
    private SystemVarsService systemVarsService;

    @Autowired
    private CustomerRuleHandler customerRuleHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "sendNotify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NotifyResponse sendNotify(@RequestBody NotifyRequestForm inputRequestForm) throws NotifyException {
        // 檢查前端傳送的變數和通知模版
        List<Notifycontent> notifyContents = new ArrayList<>();
        Set<String> failures = new HashSet<>();
        try {
            checkParameter(inputRequestForm, notifyContents, failures);
        } catch (RuntimeException e) {
            throw new NotifyException(e, SYSTEM_ERROR, inputRequestForm.getClientId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (notifyContents.size() == 0) {
            logger.info("檢查前端傳送的變數和通知模版，沒有任何 template 符合發送通知。");
            Map<String, Object> msg = new HashMap<>();
            msg.put(NOTIFY_MESSAGE_FAILURES, failures);       // 有問題的TemplateId
            throw new NotifyException(NOTIFY_RULES_UNLESS, inputRequestForm.getClientId(), msg);
        }
        //檢查ID,如果沒有則生成一個ClientId
        if(StringUtils.isBlank(inputRequestForm.getClientId())){
            if(StringUtils.isBlank(inputRequestForm.geteJNo())){
                // 生成一個15位的隨機數
                Random random = new Random();
                long random15DigitNumber = (long) (Math.pow(10, 14) + random.nextDouble() * Math.pow(10, 14));
                inputRequestForm.seteJNo(Long.toString(random15DigitNumber));
            }
            if(StringUtils.isBlank(inputRequestForm.gettXDate())){
                inputRequestForm.settXDate(DateTimeConvertUtil.nowDateTimeString2());
            }
            UUID uniqueUUID = generateUniqueUUID(LocalDateTime.parse(inputRequestForm.gettXDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), inputRequestForm.geteJNo());
            inputRequestForm.setClientId(uniqueUUID.toString());
        }

        NotifyResponse notifyResponse = new NotifyResponse();
        notifyResponse.setClientId(inputRequestForm.getClientId());
        notifyResponse.seteJNo(inputRequestForm.geteJNo());
        notifyResponse.settXDate(inputRequestForm.gettXDate());
        notifyResponse.setCode(NOTIFY_PROCESSING.getCode());
        if (failures.size() > 0 && failures.size() == inputRequestForm.getContents().size()) {
            Map<String, Object> message = new HashMap<>();
            message.put(NOTIFY_MESSAGE_FAILURES, failures);
            notifyResponse.setCode(NOTIFY_FAILURE.getCode());
            notifyResponse.setMessage(message);
        }
        if (failures.size() > 0 && failures.size() < inputRequestForm.getContents().size()) {
            Map<String, Object> message = new HashMap<>();
            message.put(NOTIFY_MESSAGE_FAILURES, failures);
            notifyResponse.setMessage(message);
        }

        try {
            // 開始發送通知
            Notifyrequest notifyRequest = new Notifyrequest();
            notifyService.createNotify(inputRequestForm, notifyRequest, notifyContents, failures);       // 發送通知
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new NotifyException(e, SYSTEM_ERROR, inputRequestForm.getClientId());
        }

        return notifyResponse;
    }

    /**
     * 檢查前端傳送的變數和通知模版
     *
     * @param inputRequestForm
     * @param notifyContents
     * @param failures
     * @throws NotifyException
     */
    private void checkParameter(NotifyRequestForm inputRequestForm, List<Notifycontent> notifyContents, Set<String> failures) throws NotifyException, JsonProcessingException {
        List<NotifyRequestContent> requestContents = inputRequestForm.getContents();    //  前端傳送變數 Contents，
        String ruleSetId = inputRequestForm.getRuleSetId();
        int seq = 0;

        // 若 RuleSet 不存在將中止作業.並回應 Code 100 錯誤
        if (Strings.isNotBlank(ruleSetId)) {
            try {
                notifyRuleSetService.getNotifyRuleSetById(Long.valueOf(ruleSetId));
            } catch (NotifyException e) {
                e.setClientId(inputRequestForm.getClientId());
                throw e;
            }
        }

        // 檢查 template 必要傳參
        for (NotifyRequestContent requestContent : requestContents) {
            // 取得 template (cache)
            Notifytemplate notifyTemplate = notifyTemplateService.getNotifyTemplateById(requestContent.getTemplateId());
            if (Objects.isNull(notifyTemplate)) {
                logger.warn("clientId: " + inputRequestForm.getClientId() + ", 沒有 templateId:" + requestContent.getTemplateId() + " 的模版");
                failures.add(String.valueOf(requestContent.getContentIndex()));
                continue;// 中止這個模版後面的檢查。檢查下一個模版
            }

            Map<String, String> parmVars = requestContent.getParmVars();
            String type = notifyTemplate.getType();
            if (NOTIFY_TYPE_EMAIL.equals(type) && Objects.isNull(parmVars.get(NOTIFY_EMAIL_PARM_NAME))) {
                logger.info("clientId: " + inputRequestForm.getClientId() + ", type 為 M 時, ##Email## 為必要變數!");
                failures.add(String.valueOf(requestContent.getContentIndex()));
                continue;   // 中止這個模版後面的檢查。檢查下一個模版
            }
            if (NOTIFY_TYPE_SMS.equals(type) && Objects.isNull(parmVars.get(NOTIFY_PHONE_PARM_NAME))) {
                logger.info("clientId: " + inputRequestForm.getClientId() + ", type 為 S 時, ##Phone## 為必要變數!");
                failures.add(String.valueOf(requestContent.getContentIndex()));
                continue;   // 中止這個模版後面的檢查。繼續檢查下一個模版
            }

            if (Strings.isNotBlank(ruleSetId)) {
                // 這則通知須通過檢查 Rule 後,才可發送。若沒通過 Rule 檢查。中止後面的檢查。繼續檢查下一個模版
                try {
                    checkRule(ruleSetId, parmVars, failures, requestContent);
                } catch (NotifyException e) {
                    // 若沒通過 Rule, 將檢查下一個模版. 明祥表示只要有 Template 符合條件仍需發送通知
                    logger.warn("clientId: " + inputRequestForm.getClientId() + ", RuleSet expression Error!");
                    failures.add(String.valueOf(requestContent.getContentIndex()));
                    continue;
                } catch (SpelParseException e) {
                    logger.warn("clientId: " + inputRequestForm.getClientId() + ",spel expression Error!");
                    e.printStackTrace();
                    failures.add(String.valueOf(requestContent.getContentIndex()));
                    continue;
                } catch (RuntimeException e) {
                    logger.error("clientId: " + inputRequestForm.getClientId() + ",RuleSet expression Error!");
                    e.printStackTrace();
                    failures.add(String.valueOf(requestContent.getContentIndex()));
                    continue;
                }
            }

            // 將組合通知內容
            String body = parser(parmVars, notifyTemplate.getTemplate());
            if (body.lastIndexOf(INDEPENDEN_VAR_SYMBOL) > -1 || body.lastIndexOf(SYSTEM_VAR_SYMBOL) > -1) {
                logger.warn("前端傳送變數和系統變數無法滿足模版。");
                logger.warn(body);
                failures.add(String.valueOf(requestContent.getContentIndex()));
                continue;   // 中止這個模版後面的檢查。檢查下一個模版
            }

            Map<String, String> content = new HashMap<>();
            content.put(NOTIFY_MESSAGE_CONTENT_TYPE, type);
            content.put(NOTIFY_MESSAGE_CONTENT_SUBJECT, notifyTemplate.getSubject());
            content.put(NOTIFY_MESSAGE_CONTENT_BODY, body);
            content.put(NOTIFY_MESSAGE_CONTENT_CLIENTID, inputRequestForm.getClientId());
            parmVars.forEach((k, v) -> {
                content.put(k, v);
            });

            Notifycontent notifyContent = new Notifycontent();
            notifyContent.setTemplateId(requestContent.getTemplateId());
            notifyContent.setContentIndex(requestContent.getContentIndex());
            notifyContent.setRequestSeq(++seq);
            notifyContent.setContent(objectMapper.writeValueAsString(content));
            notifyContents.add(notifyContent);
        }
    }

    private void checkRule(String ruleSetId, Map<String, String> parmVars, Set<String> failures, NotifyRequestContent requestContent) throws NotifyException {
        // 前端傳送變數 ruleSetId 不為空時, 從 Cache 取出 ruleSet 的 運算式。
        NotifyRuleSetExt notifyRuleSetExt = notifyRuleSetService.getNotifyRuleSetById(Long.valueOf(ruleSetId));
        String expressionOrg = notifyRuleSetExt.getExpression();
        String customerComponent = notifyRuleSetExt.getCustomerComponent();
        // 當 customComponent 有值時，不用檢查 expression
        if (Strings.isNotBlank(customerComponent)) {
            // todo : 僅訂出 customerComponent 殻，還沒有實例
            try {
                if (!customerRuleHandler.proccess(customerComponent, requestContent)) {
                    // todo : 尚須確認目前作法是任一模版 NotifyRules 不過。即中止這請求，回應異常。
                    throw new NotifyException();
                    //failures.add(String.valueOf(templateParmVars.getTemplateId()));
                }
            } catch (Exception e) {
                logger.warn("customerComponent:" + customerComponent + " 沒有通過檢查");
                throw new NotifyException(SYSTEM_ERROR, e.getMessage());
            }
        } else if (Strings.isNotBlank(expressionOrg)) {
            String ruleExpression = notifyRuleSetExt.getRuleExpression();
            Map<String, String> expressionVars = new HashMap<>();
            getExpressParms(ruleExpression, parmVars, expressionVars);

            String expression = parser(expressionVars, expressionOrg);
            if (expression.lastIndexOf(INDEPENDEN_VAR_SYMBOL) > -1 || expression.lastIndexOf(SYSTEM_VAR_SYMBOL) > -1) {
                // 模版 NotifyRules 不過。
                String errorMsg = String.format("前端傳送變數和系統變數無法滿足 RuleSet expression。 RuleSetId:%s， Expression:%s", ruleSetId, expression);
                logger.warn(errorMsg);
                throw new NotifyException();
            }

            // 解析組合後的 RuleSet expression 是否符合發送通知條件
            ExpressionParser parser = new SpelExpressionParser();
            boolean evaluate = parser.parseExpression(expression).getValue(Boolean.class);
            if (!evaluate) {
                String errorMsg = String.format("無法滿足發送通知的條件，TemplateId:%s， RuleSetId:%s， Expression:%s", requestContent.getTemplateId(), ruleSetId, expression);
                logger.info(errorMsg);
                throw new NotifyException(NOTIFY_RULES_UNLESS, errorMsg);
            }
        } else {
            logger.warn("RuleSetId:", ruleSetId, ", 沒有定義 customComponent 也沒有定義 notifyexpression!");
        }
    }

    /**
     * 當變數為 Number 時，要去除千分位符號.當變數為 Date 時，要將 - 置換成 /
     *
     * @param expression
     * @param parmVars
     * @param expressionVars
     * @throws NotifyException
     */
    private void getExpressParms(String expression, Map<String, String> parmVars, Map<String, String> expressionVars) throws NotifyException {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        char[] chars = expression.toCharArray();
        int pos = chars.length;
        String v = "";
        v += chars[--pos];
        boolean checkFirst = false;
        boolean checkSecond = false;
        List<String> list = new ArrayList<>();
        for (int i = pos; 0 < i; ) {
            checkFirst = (Character.isDigit(chars[i])) ? true : false;
            checkSecond = (Character.isDigit(chars[--i])) ? true : false;
            if (checkFirst == checkSecond) {
                v = chars[i] + v;
            } else {
                if (pattern.matcher(v).matches()) list.add(v);
                v = "" + chars[i];
            }
        }
        if (pattern.matcher(v).matches()) {
            list.add(v);
        }

        for (String ruleId : list) {
            Notifyrule rule = notifyRuleService.getNotifyRuleById(Long.valueOf(ruleId));
            String parmName = rule.getParmVarName();
            String parmValue = parmVars.get(parmName);
            if ("NUMBER".equals(rule.getParmValueType())) {
                // 當變數為 Number 時，要去除千分位符號
                parmValue = parmValue.replaceAll(",", "");
            }
            if ("DATE".equals(rule.getParmValueType())) {
                // 當變數為 Date 時，要將 - 置換成 /
                parmValue = DateTimeConvertUtil.dateTimeString(parmValue);
            }
            expressionVars.put(parmName, parmValue);
        }
    }


    private String parser(Map<String, String> vars, String expressionOrg) {
        AtomicReference<String> ref = new AtomicReference<>(expressionOrg);
        Set<String> independentVars = new HashSet<>();        // 前端傳送變數
        Set<String> systemVars = new HashSet<>();             // 系統參數
        expressionParse(independentVars, INDEPENDEN_VAR_SYMBOL, ref.get());    // 解析字串，將內容有標記的前端傳送變數放到 independentVars
        expressionParse(systemVars, SYSTEM_VAR_SYMBOL, ref.get());             // 解析字串，將內容有標記的系統變數放到 systemVars

        independentVars.forEach((v) -> {
            // 置換內容標記前端傳送變數
            String var = v.replace(INDEPENDEN_VAR_SYMBOL, "");
            if (Objects.isNull(vars.get(var))) return;
            ref.updateAndGet(s -> s.replaceAll(v, String.valueOf(vars.get(var))));

        });
        systemVars.forEach((v) -> {
            // 置換內容標記系統變數
            if ("#@SYSDATE#@".equals(v)) {
                // 系統變數不是從資料庫取得
                ref.updateAndGet(s -> s.replaceAll(v, String.valueOf(DateTimeConvertUtil.nowDateString())));
            }
            if ("#@SYSDATETIME#@".equals(v)) {
                // 系統變數不是從資料庫取得
                ref.updateAndGet(s -> s.replaceAll(v, String.valueOf(DateTimeConvertUtil.nowDateTimeString())));
            }
            if (!Objects.isNull(systemVarsService.getSysStatCache(v))) {
                ref.updateAndGet(s -> s.replaceAll(v, String.valueOf(systemVarsService.getSysStatCache(v))));
            }
        });

        return ref.get();
    }

    @RequestMapping(value = "logNotify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LogNotifyResponse logNotify(@RequestBody NotifyRequestForm inputRequestForm) throws NotifyException, JsonProcessingException {
        if(StringUtils.isBlank(inputRequestForm.gettXDate())){
            inputRequestForm.settXDate(DateTimeConvertUtil.nowDateTimeString2());
        }
        //如果clientID不是empty就拿id去查，如果沒有clientid檢查交易序號跟交易時間有沒有帶入，有就轉換成clientid去查，沒有就throw new NotifyException();
        if (StringUtils.isBlank(inputRequestForm.getClientId())) {
            UUID uniqueUUID = generateUniqueUUID(LocalDateTime.parse(inputRequestForm.gettXDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), inputRequestForm.geteJNo());
            inputRequestForm.setClientId(uniqueUUID.toString());
        }else if((StringUtils.isBlank(inputRequestForm.geteJNo()) || StringUtils.isBlank(inputRequestForm.gettXDate())) &&
                StringUtils.isBlank(inputRequestForm.getClientId())){
            throw new NotifyException(SYSTEM_ERROR, "");
        }
        // 查詢
        Notifyrequest notifyRequest = new Notifyrequest();
        LogNotifyResponse logNotifyResponse = notifyService.logNotify(inputRequestForm, notifyRequest);

        return logNotifyResponse;
    }

    private static UUID generateUniqueUUID(LocalDateTime transactionDate, String transactionId) {
        // 將交易日期轉換為納秒數,且固定時區
        long timestamp = transactionDate.toInstant(ZoneOffset.UTC).toEpochMilli() * 1000000;

        // 將交易ID轉換為長整型
        long idAsLong = Long.parseLong(transactionId);

        // 使用UUID的構造方法將時間戳和交易ID組合成UUID
        return new UUID(timestamp, idAsLong);
    }

}
