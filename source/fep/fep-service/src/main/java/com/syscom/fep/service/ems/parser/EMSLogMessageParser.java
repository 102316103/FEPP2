package com.syscom.fep.service.ems.parser;

import com.syscom.fep.base.FEPBaseMethod;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.common.notify.NotifyHelperTemplateId;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.deslog.dao.DeslogDao;
import com.syscom.fep.mybatis.deslog.model.Deslog;
import com.syscom.fep.mybatis.ems.dao.FeplogDao;
import com.syscom.fep.mybatis.ems.ext.model.FeplogExt;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.mybatis.ext.mapper.AlertExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgctlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgkbExtMapper;
import com.syscom.fep.mybatis.model.Alert;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Msgkb;
import com.syscom.fep.service.ems.configurer.EMSServiceConfiguration;
import com.syscom.fep.service.ems.vo.*;
import com.syscom.fep.service.ems.vo.EMSLogMessage.MDC;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EMSLogMessageParser extends FEPBaseMethod {
    private static final LogHelper SERVICELOGGER = LogHelperFactory.getServiceLogger();
    @Autowired
    private AlertExtMapper alertExtMapper;
    @Autowired
    private MsgkbExtMapper msgkbExtMapper;
    @Autowired
    private MsgctlExtMapper msgctlExtMapper;
    // @Autowired
    // private MailSender mailSender;
    @Autowired
    private NotifyHelper notifyHelper;
    @Autowired
    private DeslogDao deslogDao;
    private EMSServiceConfiguration emsServiceConfiguration;
    private ConcurrentHashMap<String, EMSErrData> errDic = new ConcurrentHashMap<>();

    @PostConstruct
    public void initParser() {
        this.emsServiceConfiguration = SpringBeanFactoryUtil.registerBean(EMSServiceConfiguration.class);
    }

    public void parseLog(List<EMSLogMessage> emsLogMessageList) throws Exception {
        if (CollectionUtils.isNotEmpty(emsLogMessageList)) {
            List<EMSLogMessage> logFepMessageList = new ArrayList<>();
            List<EMSLogMessage> logHisMessageList = new ArrayList<>();
            List<EMSLogMessage> logDesMessageList = new ArrayList<>();
            List<EMSLogMessage> alertMessageList = new ArrayList<>();
            for (EMSLogMessage emsLogMessage : emsLogMessageList) {
                // 排除一些不需要寫入FEPLOG的message
                if (this.doExclude(emsLogMessage)) {
                    continue;
                }
                if (EMSLogMessageType.log.name().equals(emsLogMessage.getMessageType())) {
                    if (EMSLogMessageTarget.fep.name().equals(emsLogMessage.getMessageTarget())) {
                        logFepMessageList.add(emsLogMessage);
                    } else if (EMSLogMessageTarget.his.name().equals(emsLogMessage.getMessageTarget())) {
                        logHisMessageList.add(emsLogMessage);
                    } else if (EMSLogMessageTarget.des.name().equals(emsLogMessage.getMessageTarget())) {
                        logDesMessageList.add(emsLogMessage);
                    }
                } else if (EMSLogMessageType.alert.name().equals(emsLogMessage.getMessageType())) {
                    alertMessageList.add(emsLogMessage);
                }
            }
            this.parseLog(logFepMessageList, false);
            this.parseLog(logHisMessageList, true);
            this.parseDesLog(logDesMessageList);
            this.parseAlert(alertMessageList);
        }
    }

    /**
     * 排除一些不需要寫入FEPLOG的message
     *
     * @param emsLogMessage
     * @return
     */
    private boolean doExclude(EMSLogMessage emsLogMessage) {
        return CollectionUtils.isNotEmpty(emsServiceConfiguration.getExcludeMessageIdList()) &&
                emsServiceConfiguration.getExcludeMessageIdList().stream().filter(StringUtils::isBlank).count() != emsServiceConfiguration.getExcludeMessageIdList().size() &&
                emsServiceConfiguration.getExcludeMessageIdList().stream().filter(t -> t.equalsIgnoreCase(emsLogMessage.getMdc().getMessageID())).findFirst().orElse(null) != null;
    }

    private void parseLog(List<EMSLogMessage> emsLogMessageList, boolean isHistory) throws Exception {
        if (CollectionUtils.isEmpty(emsLogMessageList)) {
            return;
        }
        List<Feplog> recordList = new ArrayList<>();
        for (EMSLogMessage emsLogMessage : emsLogMessageList) {
            if (emsLogMessage.getMdc() == null) {
                continue;
            }
            MDC mdc = emsLogMessage.getMdc();
            if (StringUtils.isNotBlank(mdc.getTxDate()) && !"2".equals(mdc.getTxDate().substring(0, 1))) {
                mdc.setTxDate(CalendarUtil.rocStringToADString(mdc.getTxDate()));
            }
            if (StringUtils.isNotBlank(mdc.getChannel()) && FEPChannel.NETBANK.name().equals(mdc.getChannel().trim())) {
                if (StringUtils.isNotBlank(mdc.getTrinActno()) && mdc.getTrinActno().length() > 16) {
                    mdc.setTrinActno(mdc.getTrinActno().substring(0, 16));
                }
                if (StringUtils.isNotBlank(mdc.getTroutActno()) && mdc.getTroutActno().length() > 16) {
                    mdc.setTroutActno(mdc.getTroutActno().substring(0, 16));
                }
            }
            Feplog record = new FeplogExt();
            record.setAtmno(mdc.getATMNo());
            record.setAtmseq(mdc.getATMSeq());
            record.setBkno(mdc.getBkno());
            record.setChannel(mdc.getChannel());
            try {
                record.setEj(Long.parseLong(mdc.getEj()));
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            try {
                record.setLogdate(FormatUtil.parseDataTime(mdc.getLogDate(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            record.setMessageflow(mdc.getMessageFlow());
            record.setMessageid(mdc.getMessageID());
            record.setProgramflow(mdc.getProgramFlow());
            record.setProgramname(mdc.getProgramName());
            record.setRemark(emsLogMessage.getMessage());
            record.setStan(mdc.getStan());
            try {
                record.setSteps(Long.parseLong(mdc.getStep()));
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            record.setThreadid(emsLogMessage.getThread());
            record.setTrinactno(mdc.getTrinActno());
            record.setTrinbank(mdc.getTrinBank());
            record.setTroutactno(mdc.getTroutActno());
            record.setTroutbank(mdc.getTroutBank());
            record.setTxdate(mdc.getTxDate());
            record.setTxmessage(mdc.getTxMessage());
            record.setTxrquid(mdc.getTxRquid());
            record.setHostname(mdc.getHostname());
            recordList.add(record);
        }
        FeplogDao feplogDao = SpringBeanFactoryUtil.getBean("feplogDao");
        feplogDao.setTableNameSuffix(String.valueOf(CalendarUtil.getDayOfWeek(Calendar.getInstance())), StringUtils.join(ProgramName, ".parseLog"));
        // 批量新增到db中
        feplogDao.insertBatch(recordList, emsServiceConfiguration.getFlushStatementsTotal());
    }

    private void parseDesLog(List<EMSLogMessage> logDesMessageList) throws Exception {
        if (CollectionUtils.isEmpty(logDesMessageList)) {
            return;
        }
        List<Deslog> recordList = new ArrayList<>();
        for (EMSLogMessage emsLogMessage : logDesMessageList) {
            if (emsLogMessage.getMdc() == null) {
                continue;
            }
            MDC mdc = emsLogMessage.getMdc();
            Deslog record = new Deslog();
            record.setTableNameSuffix(String.valueOf(CalendarUtil.getDayOfWeek(Calendar.getInstance())));
            record.setDeslogCallobject(mdc.getCallObj());
            try {
                record.setDeslogEj(Integer.parseInt(mdc.getEj()));
            } catch (NumberFormatException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            record.setDeslogFunc(mdc.getFuncNo());
            record.setDeslogInputdata1(mdc.getInput1());
            record.setDeslogInputdata2(mdc.getInput2());
            record.setDeslogKeyid(mdc.getKeyId());
            record.setDeslogOutputdata1(mdc.getOutput1());
            record.setDeslogOutputdata2(mdc.getOutput2());
            record.setDeslogProgramflow(mdc.getProgramFlow());
            record.setDeslogProgramname(mdc.getProgramName());
            record.setDeslogRc(mdc.getRC());
            record.setDeslogRemark(emsLogMessage.getMessage());
            record.setDeslogSuipcommand(mdc.getSuipCommand());
            try {
                record.setDeslogUpdatetime(FormatUtil.parseDataTime(mdc.getLogDate(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
            } catch (ParseException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            record.setTxrquid(mdc.getTxRquid());
            record.setHostname(mdc.getHostname());
            recordList.add(record);
        }
        // 批量新增到db中
        deslogDao.insertBatch(recordList, emsServiceConfiguration.getFlushStatementsTotal());
    }

    private void parseAlert(List<EMSLogMessage> alertMessageList) throws Exception {
        if (CollectionUtils.isEmpty(alertMessageList)) {
            return;
        }
        for (EMSLogMessage emsLogMessage : alertMessageList) {
            if (emsLogMessage.getMdc() == null) {
                continue;
            }
            insertEMSAR(emsLogMessage);
        }
    }

    private void insertEMSAR(EMSLogMessage emsLogMessage) {
        try {
            Date ar_date = FormatUtil.parseDataTime(emsLogMessage.getMdc().getLogDate(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS);
            String erPkey = StringUtils.join(new String[] {emsLogMessage.getMdc().getHostname(), emsLogMessage.getMdc().getErrcode(), emsLogMessage.getMdc().getErrdescription()}, "#");
            // 該事件在3分鐘內出現的筆數
            EMSErrData lastErr = errDic.get(erPkey);
            if (lastErr != null) {
                lastErr.setEventData(emsLogMessage);
                lastErr.accumulateCount();
                // 之前有發生過此error, 判斷最後一筆的時間是否在3分鐘內
                if ((Calendar.getInstance().getTimeInMillis() - lastErr.getLastNotifyTime().getTimeInMillis()) < (long) emsServiceConfiguration.getRepeatInterval() * 60 * 1000) {
                    return;
                }
            }
            // 之前沒發生過此error, 從DB再判斷3分鐘內是否有發生
            else {
                lastErr = new EMSErrData();
                lastErr.setCount(1);
                lastErr.setEventData(emsLogMessage);
                lastErr.setLastNotifyTime(Calendar.getInstance());
                errDic.put(erPkey, lastErr);
                Alert alert = new Alert();
                alert.setArErcode(emsLogMessage.getMdc().getErrcode());
                alert.setArHostname(emsLogMessage.getMdc().getHostname());
                alert.setArErdescription(emsLogMessage.getMdc().getErrdescription());
                alert.setArDatetime(DateUtils.addMinutes(ar_date, -1 * emsServiceConfiguration.getRepeatInterval()));
                alert.setArSubsys(emsLogMessage.getMdc().getSubsys());
                int iRtn = alertExtMapper.getAlertCounts(alert);
                if (iRtn > 0) {
                    lastErr.accumulateCount(iRtn);
                    return;
                }
            }
            EMSApMessageData apData = this.parseAPMessage(emsLogMessage);
            Alert para = new Alert();
            para.setArDatetime(ar_date);
            para.setArHostname(emsLogMessage.getMdc().getHostname());
            para.setArHostip(emsLogMessage.getMdc().getHostip());
            para.setArLevel(emsLogMessage.getLevel());
            para.setArApplication(emsLogMessage.getMdc().getMessageGroup());
            para.setArLine(emsLogMessage.getMdc().getLinenumber());
            para.setArSys(emsLogMessage.getMdc().getSys());
            para.setArSubsys(emsLogMessage.getMdc().getSubsys());
            para.setArErcode(emsLogMessage.getMdc().getErrcode());
            para.setArErdescription(emsLogMessage.getMdc().getErrdescription());
            para.setArEj(apData.getEj());
            para.setArMessage(emsLogMessage.getMessage());
            para.setAtmno(apData.getAtmNo());
            alertExtMapper.insertSelective(para);
            if (apData.isNotification()) {
                this.sendMail(apData, lastErr);
            }
        } catch (SQLException e) {
            LogHelperFactory.getTraceLogger().info("EMS Get SQLException:", e.getMessage());
            SERVICELOGGER.error(e, "[2003]", "EMS Service Insert DB Fail. ", e.getMessage(), emsLogMessage.getMessage());
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().info("EMS Get Exception:", e.getMessage());
            SERVICELOGGER.error(e, "[2004]", "EMS Service Process Message Fail. ", e.getMessage(), emsLogMessage.getMessage());
        }
    }

    private EMSApMessageData parseAPMessage(EMSLogMessage emsLogMessage) throws Exception {
        String body = emsLogMessage.getMessage();
        Element root = XmlUtil.load("<EMS>" + body + "</EMS>");
        EMSApMessageData data = new EMSApMessageData();
        data.setTxErrDesc(XmlUtil.getChildElementValue(root, "TxErrDesc", StringUtils.EMPTY));
        data.setExSubCode(XmlUtil.getChildElementValue(root, "ExSubCode", StringUtils.EMPTY));
        data.setExStack(XmlUtil.getChildElementValue(root, "ExStack", StringUtils.EMPTY));
        data.setExSource(XmlUtil.getChildElementValue(root, "ExSource", StringUtils.EMPTY));
        data.setTxExternalCode(XmlUtil.getChildElementValue(root, "TxExternalCode", StringUtils.EMPTY));
        data.setTxPK(XmlUtil.getChildElementValue(root, "TxPK", StringUtils.EMPTY));
        data.setTxSource(XmlUtil.getChildElementValue(root, "TxSource", StringUtils.EMPTY));
        if (StringUtils.isNotBlank(data.getTxErrDesc())) {
            List<String> warningPattern = Arrays.asList(emsServiceConfiguration.getWarningPattern().split(";"));
            List<String> result = warningPattern.stream().filter(s -> data.getTxErrDesc().contains(s)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(result)) {
                data.setNotification(true);
                if (result.get(0).toUpperCase().indexOf("RC:") > -1) {
                    data.setRcString(this.getENCRC(result.get(0)));
                }
            }
        }
        Element logData = root.getChild("LogData");
        if (logData != null) {
            data.setEj(XmlUtil.getChildElementValue(logData, "EJ", StringUtils.EMPTY));
            data.setAtmNo(XmlUtil.getChildElementValue(logData, "ATMNo", StringUtils.EMPTY));
            data.setAtmSeq(XmlUtil.getChildElementValue(logData, "ATMSeq", StringUtils.EMPTY));
            data.setStan(XmlUtil.getChildElementValue(logData, "STAN", StringUtils.EMPTY));
            data.setPcode(XmlUtil.getChildElementValue(logData, "PCode", StringUtils.EMPTY));
            data.setMessageId(XmlUtil.getChildElementValue(logData, "MessageId", StringUtils.EMPTY));
            data.setProgramName(XmlUtil.getChildElementValue(logData, "ProgramName", StringUtils.EMPTY));
            data.setBkno(XmlUtil.getChildElementValue(logData, "Bkno", StringUtils.EMPTY));
            data.setDesBkno(XmlUtil.getChildElementValue(logData, "DesBkno", StringUtils.EMPTY));
            data.setTroutBank(XmlUtil.getChildElementValue(logData, "TroutBank", StringUtils.EMPTY));
            data.setTrinBank(XmlUtil.getChildElementValue(logData, "TrinBank", StringUtils.EMPTY));
            data.setNotification(XmlUtil.getChildElementValue(logData, "Notification", false));
            data.setNotifyMail(XmlUtil.getChildElementValue(logData, "Responsible", StringUtils.EMPTY));
            // 2024-01-02 Richard modified start
            // by Ashiang 請改成如果訊息有帶ReturnCode再去parse它, 並且parse如果有例外不要丟Exception出來, 改成一般訊息說明即可, 比如parse FEPReturnCode XXX Failed because…
            String returnCode = XmlUtil.getChildElementValue(logData, "ReturnCode", StringUtils.EMPTY);
            if (StringUtils.isNotBlank(returnCode)) {
                try {
                    data.setRtnCode(FEPReturnCode.parse(returnCode));
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn("parse FEPReturnCode \"", returnCode, "\" Failed, because ", e.getMessage());
                }
            }
            // 2024-01-02 Richard modified end
            data.setRemark(XmlUtil.getChildElementValue(logData, "Remark", StringUtils.EMPTY));
        }
        // Fly 2016/06/06 增加MSGID中文說明
        if (StringUtils.isNotBlank(data.getMessageId())) {
            Msgctl dtCTL = msgctlExtMapper.selectByPrimaryKey(data.getMessageId());
            if (dtCTL != null) {
                data.setMessageName(StringUtils.join(data.getMessageId(), "[", dtCTL.getMsgctlMsgName(), "]"));
            }
        }
        // 判斷要不要通知由MSGFILE及MSGKB只要有一設定為true就通知
        List<Msgkb> dtMsgKb = msgkbExtMapper.getMSGKB(emsLogMessage.getMdc().getErrcode(), data.getExSubCode());
        if (CollectionUtils.isNotEmpty(dtMsgKb)) {
            if (!data.isNotification()) {
                data.setNotification(DbHelper.toBoolean(dtMsgKb.get(0).getNotify()));
            }
            if (StringUtils.isBlank(data.getNotifyMail())) {
                data.setNotifyMail(dtMsgKb.get(0).getNotifymail());
            }
        }
        // 如為轉出/扣款行及匯款行<>807, 不需寄送EMAIL
        // 轉出/扣款行及匯款行=807, 才需寄送EMAIL
        if (data.getTxErrDesc().indexOf("代號:6101") > -1) {
            if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(data.getTroutBank())) {
                // Fly 2016/04/20 取值前先判斷是否有值
                if (StringUtils.isNotBlank(data.getPcode()) && "2".equals(data.getPcode().substring(0, 1))) {
                    data.setNotification(false);
                }
            }
        }
        // Fly 2016/04/08 SQL timeout 與RM的exception 需寄mail通知
        if (data.getExStack().indexOf("org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection") >= 0) {
            data.setNotification(true);
            data.setRcString("CannotGetJdbcConnectionException: Failed to obtain JDBC Connection");
        }
        if ("RM".equals(emsLogMessage.getMdc().getSubsys()) && data.getTxErrDesc().indexOf(Const.MESSAGE_ERR_EXCEPTION_OCCUR) > -1) {
            data.setNotification(true);
        }
        // Fly 2016/11/25 證券整批轉即時批次出現異常時需寄MAIL
        if (data.getTxSource().indexOf("BT010300") > -1 && data.getTxErrDesc().indexOf(Const.MESSAGE_ERR_EXCEPTION_OCCUR) > -1) {
            data.setNotification(true);
            data.setRcString("全國繳費整批轉即時交易失敗");
        }
        // 2023-05-15 Richard add ATMGW憑證不正確，送EMS，EMS收到後判斷ReturnCode送mail
        if ("ATMGW".equalsIgnoreCase(data.getTxSource()) &&
                (data.getRtnCode() == FEPReturnCode.INVALID_CERTIFICATE ||
                        data.getRtnCode() == FEPReturnCode.NOT_SSL_RECORD ||
                        data.getRtnCode() == FEPReturnCode.INVALID_CERTIFICATE_ALIAS ||
                        data.getRtnCode() == FEPReturnCode.CERTIFICATE_NOT_MATCH ||
                        data.getRtnCode() == FEPReturnCode.CERTIFICATE_EXPIRED)) {
            // data.setNotification(true);
            data.setRcString(data.getRemark());
        }
        return data;
    }

    private void sendMail(EMSApMessageData apMessage, EMSErrData lastErr) throws Exception {
        StringBuilder mailSubject = new StringBuilder();
        StringBuilder mailBody = new StringBuilder();
        // Mail對象優先順序,以MSGFILE訊息中的Responsible為主,如果沒有則看MSGKB有沒有定義此筆訊息的NotifyMail,如果也沒有就以EMS config的定義
        String[] mailTo = null;
        if (StringUtils.isNotBlank(apMessage.getNotifyMail())) {
            mailTo = apMessage.getNotifyMail().split(";");
        } else {
            mailTo = emsServiceConfiguration.getMailList();
        }
        // Fly 2016/12/13 (週二) 下午 03:24 永豐要求增加mail名單
        if (apMessage.getTxSource().indexOf("BT010300") > -1 && apMessage.getTxErrDesc().indexOf(Const.MESSAGE_ERR_EXCEPTION_OCCUR) > -1) {
            mailTo = emsServiceConfiguration.getMailListNps2262();
        }
        mailBody.append("發生時間：").append(lastErr.getEventData().getMdc().getLogDate()).append("\r\n");
        mailBody.append("發生主機：").append(lastErr.getEventData().getMdc().getHostname()).append("\r\n");
        mailBody.append("訊息代碼：").append(lastErr.getEventData().getMdc().getErrcode()).append("\r\n");
        mailBody.append("ATM代號：").append(apMessage.getAtmNo()).append("\r\n");
        mailBody.append("ATM序號：").append(apMessage.getAtmSeq()).append("\r\n");
        mailBody.append("EJ：").append(apMessage.getEj()).append("\r\n");
        mailBody.append("交易代號：").append(apMessage.getMessageName()).append("\r\n");
        // ChenLi, 2015/07/13, Mail通知內容增加顯示轉出行 And 轉入行
        mailBody.append("轉出行：").append(apMessage.getTroutBank()).append("\r\n");
        mailBody.append("轉入行：").append(apMessage.getTrinBank()).append("\r\n");
        // mailBody &= "銀行代號：" & bkno & vbCrLf
        mailBody.append("財金Stan：").append(apMessage.getBkno()).append("-").append(apMessage.getStan()).append("\r\n");
        // ChenLi, 2013/11/05, Mail通知內容增加顯示Source 銀行代號 與Destination銀行代號
        mailBody.append("Dest.  ID：").append(apMessage.getDesBkno()).append("\r\n");
        mailBody.append("Source ID：").append(apMessage.getBkno()).append("\r\n");
        mailBody.append("PCode：").append(apMessage.getPcode()).append("\r\n");
        mailBody.append("服務名稱：").append(apMessage.getTxSource()).append("\r\n");
        mailBody.append("函式名稱：").append(apMessage.getProgramName()).append("\r\n");
        if (StringUtils.isNotBlank(apMessage.getRcString())) {
            mailSubject.append("EMS異常事件監控系統警示通知-").append(apMessage.getRcString());
            mailBody.append("訊息內容：").append(apMessage.getRcString()).append("\r\n");
            if (StringUtils.isNotBlank(apMessage.getExStack())) {
                mailBody.append(apMessage.getExStack()).append("\r\n");
            }
            int c = apMessage.getTxErrDesc().indexOf(",INPUTSTR1");
            if (c > -1) {
                mailBody.append("參數內容：").append(apMessage.getTxErrDesc().substring(c + 1));
            }
        } else {
            mailSubject.append("EMS異常事件監控系統警示通知-");
            if (lastErr.getEventData().getMdc().getErrcode().compareToIgnoreCase("TimeoutException") == 0) {
                mailSubject.append(apMessage.getProgramName()).append(StringUtils.SPACE).append(lastErr.getEventData().getMdc().getErrcode());
            } else {
                mailSubject.append(apMessage.getTxErrDesc());
            }
            if (lastErr.getCount() > 1) {
                mailSubject.append(",此事件距離上次通知(")
                        .append(FormatUtil.dateTimeFormat(lastErr.getLastNotifyTime(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS))
                        .append(")共發生" + lastErr.getCount() + "筆");
            }
            String tmp = StringUtils.replace(mailSubject.toString(), "\r\n", ",");
            mailSubject.setLength(0);
            mailSubject.append(tmp);
            mailBody.append("訊息內容：").append(apMessage.getTxErrDesc()).append("\r\n");
            if (StringUtils.isNotBlank(apMessage.getExStack())) {
                mailBody.append(apMessage.getExStack()).append("\r\n");
            }
        }
        SERVICELOGGER.debug("Begin SendMail to [", StringUtils.join(mailTo, ','), "],Subject:", mailSubject);
        // 2014/10/01 Modify by Ruling for 調整寄件者由sysopa@sinopac.com改為hana.chang@sinopac.com
        // 2016/06/20 Fly 改設定在Config
        if (lastErr.getEventData().getLevel().equalsIgnoreCase("Error") || lastErr.getEventData().getLevel().equalsIgnoreCase("Fatal")) {
            // mailSender.sendSimpleEmail(
            //         emsServiceConfiguration.getMailSender(),
            //         mailTo,
            //         null,
            //         mailSubject.toString(),
            //         mailBody.toString(),
            //         EmailUtil.MailPriority.High);
            notifyHelper.sendSimpleMail(NotifyHelperTemplateId.EMS, StringUtils.join(mailTo, ','),
                    StringUtils.join(mailSubject.toString(), "\r\n", mailBody.toString()), true);
        } else {
            // mailSender.sendSimpleEmail(
            //         emsServiceConfiguration.getMailSender(),
            //         mailTo,
            //         null,
            //         mailSubject.toString(),
            //         mailBody.toString());
            notifyHelper.sendSimpleMail(NotifyHelperTemplateId.EMS, StringUtils.join(mailTo, ','),
                    StringUtils.join(mailSubject.toString(), "\r\n", mailBody.toString()), true);
        }
        lastErr.setLastNotifyTime(Calendar.getInstance());
        lastErr.setCount(0);
        SERVICELOGGER.debug("SendMail OK");
    }

    private String getENCRC(String rc) {
        String rcStr = StringUtils.EMPTY;
        switch (rc) {
            case "RC:10,":
                rcStr = "Source Key Parity Error";
                break;
            case "RC:11,":
                rcStr = "Destination Key Parity Error";
                break;
            case "RC:17,":
                rcStr = "HSM需要授權";
                break;
            case "RC:81,":
                rcStr = "參數檔異常";
                break;
            case "RC:82,":
                rcStr = "參數檔異常";
                break;
            case "RC:83,":
                rcStr = "ATM Key File Access Error";
                break;
            case "RC:84,":
                rcStr = "RM Key File Access Error";
                break;
            case "RC:94,":
                rcStr = "ENCLib Call HSMSUIP失敗";
                break;
            case "RC:95,":
                rcStr = "ENCLib Call HSMSUIP回應失敗";
                break;
            case "RC:97,":
                rcStr = "ENCDB處理異常";
                break;
            case "RC:98,":
                rcStr = "無法連接HSM";
                break;
            case "RC:99,":
                rcStr = "無法連接HSM SUIP";
                break;
            case "RC:998,":
                rcStr = "無法取得Suip Socket物件";
                break;
            case "RC:999,":
                rcStr = "ENCLib發生異常";
                break;
        }
        return StringUtils.join(rc, rcStr);
    }

    /**
     * 檢查是否有未寄出的訊息一次寄出
     */
    public void resetErrorCounter() {
        SERVICELOGGER.debug("ResetErrorCounter event");
        for (Map.Entry<String, EMSErrData> entry : errDic.entrySet()) {
            String key = entry.getKey();
            EMSErrData err = entry.getValue();
            SERVICELOGGER.debug("ResetErrorCounter Key:", key, ", Count:", err.getCount());
            if (err.getCount() > 0) {
                try {
                    EMSApMessageData apData = this.parseAPMessage(err.getEventData());
                    if (apData.isNotification()) {
                        this.sendMail(apData, err);
                    } else {
                        err.setCount(0);
                    }
                } catch (Exception e) {
                    SERVICELOGGER.error(e, e.getMessage());
                }
            }
        }
        errDic.entrySet().removeIf(t -> t.getValue().getCount() == 0);
    }
}
