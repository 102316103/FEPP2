package com.syscom.fep.server.common.notify;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.mail.MailSender;
import com.syscom.fep.common.sms.SmsConfiguration;
import com.syscom.fep.common.sms.SmsSender;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.EmailUtil.MailPriority;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.NotifylogMapper;
//import com.syscom.fep.mybatis.mapper.SmlparmMapper;
//import com.syscom.fep.mybatis.mapper.SmsmsgMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.vo.enums.CurrencyType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 這支程式不會再使用, 暫時先保留確保編譯OK
 */
@Deprecated
public class NotifyHelper extends FEPBase {
//    private Smsmsg _smsMsg;
    private NotifyData _notifyData;

    public NotifyHelper(NotifyData notifyData) {
        super();
        this.logContext.setProgramName(this.ProgramName);
        this._notifyData = notifyData;
    }

    public NotifyHelper(LogData logContext, NotifyData notifyData) {
        this.logContext = logContext;
        this.logContext.setProgramName(this.ProgramName);
        this._notifyData = notifyData;
    }

    public void send() throws Exception {
//        if (this._notifyData.getSmsmsg() == null) {
//            SmsmsgMapper mapper = SpringBeanFactoryUtil.getBean(SmsmsgMapper.class);
//            Smsmsg record = mapper.selectByPrimaryKey(this._notifyData.getTxDate(), this._notifyData.getEj());
//            if (record == null) {
//                this.logContext.setRemark("SMSMSG查無資料");
//                this.logMessage(this.logContext);
//                return;
//            }
//            this._smsMsg = record;
//        } else {
//            this._smsMsg = this._notifyData.getSmsmsg();
//            this._notifyData.setTxDate(this._smsMsg.getSmsmsgTxDate());
//            this._notifyData.setEj(this._smsMsg.getSmsmsgEjfno());
//        }
//        if (StringUtils.isNotBlank(this._smsMsg.getSmsmsgEmail()) && this._notifyData.getSmlSeqNoForMail() > 0) {
//            this.sendMail();
//        } else {
//            this.logContext.setRemark("無EMAIL欄位");
//            this.logMessage(this.logContext);
//        }
//        if (StringUtils.isNotBlank(this._smsMsg.getSmsmsgNumber()) && this._notifyData.getSmlSeqNoForSMS() > 0) {
//            this.sendSMS();
//        } else {
//            this.logContext.setRemark("無電話欄位");
//            this.logMessage(this.logContext);
//        }
//        if (StringUtils.isNotBlank(this._smsMsg.getSmsmsgIdno()) && this._notifyData.getSmlSeqNoForApp() > 0) {
//            this.sendApp();
//        }
//        this.updateSmsmsg();
    }

//    private FEPReturnCode sendSMS() {
//        SmlparmMapper smlparmMapper = SpringBeanFactoryUtil.getBean(SmlparmMapper.class);
//        Smlparm defSML = smlparmMapper.selectByPrimaryKey("S", this._notifyData.getSmlSeqNoForSMS());
//        if (defSML == null) {
//            this.logContext.setRemark("SMLPARM查無資料");
//            this.logMessage(this.logContext);
//            return FEPReturnCode.QueryNoData;
//        }
//        String msg = defSML.getSmlparmContent();
//        switch (defSML.getSmlparmSeqno()) {
//            case 1:
//                msg = StringUtils.replace(msg, "[PARM1]",
//                        new StringBuilder()
//                                .append(this._smsMsg.getSmsmsgTxDate().substring(4))
//                                .append(this._smsMsg.getSmsmsgTxTime().substring(0, 4))
//                                .insert(6, ":")
//                                .insert(4, " ")
//                                .insert(2, "/").toString());
//                if (CurrencyType.TWD.name().equals(this._smsMsg.getSmsmsgTxCurAct())) {
//                    msg = StringUtils.replace(msg, "[PARM2]",
//                            StringUtils.join("新台幣", FormatUtil.decimalFormat(this._smsMsg.getSmsmsgTxAmtAct(), "#,##0")));
//                } else {
//                    List<Curcd> curcdList = FEPCache.getCurcdList();
//                    Curcd curcd = curcdList.stream().filter(t -> t.getCurcdAlpha3().equals(this._smsMsg.getSmsmsgTxCurAct())).findFirst().orElse(null);
//                    String curName = curcd != null ? curcd.getCurcdCurName() : StringUtils.EMPTY;
//                    msg = StringUtils.replace(msg, "[PARM2]",
//                            StringUtils.join(curName, FormatUtil.decimalFormat(this._smsMsg.getSmsmsgTxAmtAct(), "#,##0")));
//                }
//                break;
//            case 2:
//                break;
//            case 3:
//                msg = StringUtils.replace(msg, "[PARM1]",
//                        new StringBuilder()
//                                .append(this._smsMsg.getSmsmsgTxDate())
//                                .append(this._smsMsg.getSmsmsgTxTime().substring(0, 4))
//                                .insert(10, ":")
//                                .insert(8, " ")
//                                .insert(6, "/")
//                                .insert(4, "/").toString());
//                msg = StringUtils.replace(msg, "[PARM2]", FormatUtil.decimalFormat(this._smsMsg.getSmsmsgTxAmt(), "#,##0"));
//                break;
//            default:
//                //根據外部傳入的參數值替換body內容
//                if (MapUtils.isNotEmpty(this._notifyData.getParameterData())) {
//                    for (Map.Entry<String, String> entry : this._notifyData.getParameterData().entrySet()) {
//                        msg = StringUtils.replace(msg, entry.getKey(), entry.getValue());
//                    }
//                }
//                break;
//        }
//        // 合庫改為call 三竹簡訊API
//        // 這裡先取出SmsConfiguration, 塞入SYSCONF中的設定
//        SmsConfiguration smsConfiguration = SpringBeanFactoryUtil.getBean(SmsConfiguration.class);
//        smsConfiguration.setDomain(CMNConfig.getInstance().getSMSDomain());
//        smsConfiguration.setUsername(CMNConfig.getInstance().getSMSUserName());
//        smsConfiguration.setSscode(CMNConfig.getInstance().getSMSSscode());
//        // SmsSender
//        SmsSender smsSender = SpringBeanFactoryUtil.getBean(SmsSender.class);
//        try {
//            smsSender.send(this._smsMsg.getSmsmsgNumber(), msg);
//            this.logContext.setRemark("Send SMS OK");
//            this.logMessage(this.logContext);
//        } catch (Exception e) {
//            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendSMS"));
//            this.logContext.setRemark(StringUtils.join("Send SMS Failed, ", e.getMessage()));
//            this.logContext.setProgramException(e);
//            sendEMS(this.logContext);
//        }
//
//        Notifylog notifyLog = new Notifylog();
//        notifyLog.setNotifylogTxDate(this._notifyData.getTxDate());
//        notifyLog.setNotifylogEj((long) this._notifyData.getEj());
//        notifyLog.setNotifylogType(defSML.getSmlparmType());
//        notifyLog.setNotifylogIdno(StringUtils.EMPTY);
//        notifyLog.setNotifylogProj(defSML.getSmlparmProj());
//        notifyLog.setNotifylogPriority(defSML.getSmlparmPriority());
//        notifyLog.setNotifylogFromname(StringUtils.EMPTY);
//        notifyLog.setNotifylogFromemail(StringUtils.EMPTY);
//        notifyLog.setNotifylogTomail(StringUtils.EMPTY);
//        notifyLog.setNotifylogNumber(this._smsMsg.getSmsmsgNumber());
//        notifyLog.setNotifylogPgcode(defSML.getSmlparmPgcode());
//        notifyLog.setNotifylogChannel(defSML.getSmlparmChannel());
//        notifyLog.setNotifylogSubject(StringUtils.EMPTY);
//        notifyLog.setNotifylogContent(msg);
//        notifyLog.setNotifylogSendtime(Calendar.getInstance().getTime());
//        this.insertLog(notifyLog);
//        return FEPReturnCode.Normal;
//    }
//
//    private FEPReturnCode sendMail() throws Exception {
//        SmlparmMapper smlparmMapper = SpringBeanFactoryUtil.getBean(SmlparmMapper.class);
//        Smlparm defSML = smlparmMapper.selectByPrimaryKey("M", this._notifyData.getSmlSeqNoForMail());
//        if (defSML == null) {
//            this.logContext.setRemark("SMLPARM查無資料");
//            this.logMessage(this.logContext);
//            return FEPReturnCode.QueryNoData;
//        }
//        String mailBody = defSML.getSmlparmContent();
//        String subject = defSML.getSmlparmSubject();
//        // by 不同param 變更mail body內容
//        switch (defSML.getSmlparmSeqno()) {
//            case 1:
//                // txdate
//                String txdate = new StringBuilder()
//                        .append(this._smsMsg.getSmsmsgTxDate())
//                        .insert(6, "月")
//                        .insert(4, "年")
//                        .append("日")
//                        .toString();
//                // actno
//                String actno = new StringBuilder()
//                        .append(this._smsMsg.getSmsmsgTroutActno().substring(2 + 0, 2 + 8))
//                        .append("***")
//                        .append(this._smsMsg.getSmsmsgTroutActno().substring(2 + 11, 2 + 11 + 2))
//                        .append("*")
//                        .insert(13, "-")
//                        .insert(6, "-")
//                        .insert(3, "-")
//                        .toString();
//                // time
//                String time = new StringBuilder()
//                        .append(this._smsMsg.getSmsmsgTxDate())
//                        .append(this._smsMsg.getSmsmsgTxTime())
//                        .insert(12, ":")
//                        .insert(10, ":")
//                        .insert(8, " ")
//                        .insert(6, "/")
//                        .insert(4, "/")
//                        .toString();
//                // brno
//                String brno = this._smsMsg.getSmsmsgBrno();
//
//                // Fly 2020/07/30 改抓SYSSTAT
//                AllbankExtMapper allbankExtMapper = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
//                Allbank defBank = allbankExtMapper.selectByPrimaryKey(SysStatus.getPropertyValue().getSysstatHbkno(), this._smsMsg.getSmsmsgBrno());
//                if (defBank != null) {
//                    brno = defBank.getAllbankFullname().trim();
//                }
//                mailBody = StringUtils.replace(mailBody, "[PARM1]", txdate);
//                mailBody = StringUtils.replace(mailBody, "[PARM2]", brno);
//                mailBody = StringUtils.replace(mailBody, "[PARM3]", actno);
//                mailBody = StringUtils.replace(mailBody, "[PARM4]", this._smsMsg.getSmsmsgTxCur());
//                mailBody = StringUtils.replace(mailBody, "[PARM5]", FormatUtil.decimalFormat(this._smsMsg.getSmsmsgTxAmt(), "#,##0"));
//                mailBody = StringUtils.replace(mailBody, "[PARM6]", FormatUtil.decimalFormat(this._smsMsg.getSmsmsgTxAmtAct(), "#,##0"));
//                mailBody = StringUtils.replace(mailBody, "[PARM7]", time);
//                // 2021/01/27 Modify by Ruling for 多幣DEBIT卡：幣別原為新台幣改用傳參數的方式
//                if (CurrencyType.TWD.name().equals(this._smsMsg.getSmsmsgTxCurAct())) {
//                    mailBody = StringUtils.replace(mailBody, "[PARM8]", "新台幣");
//                } else {
//                    mailBody = StringUtils.replace(mailBody, "[PARM8]", this._smsMsg.getSmsmsgTxCurAct());
//                }
//                break;
//            case 2:
//                break;
//            case 3:
//            case 4:
//                String actno2 = StringUtils.join(
//                        this._smsMsg.getSmsmsgTroutActno().substring(2 + 0, 2 + 8),
//                        "***",
//                        this._smsMsg.getSmsmsgTroutActno().substring(2 + 11, 2 + 11 + 2),
//                        "*"
//                );
//                String time2 = new StringBuilder()
//                        .append(this._smsMsg.getSmsmsgTxDate())
//                        .append(this._smsMsg.getSmsmsgTxTime())
//                        .insert(12, ":")
//                        .insert(10, ":")
//                        .insert(8, " ")
//                        .insert(6, "/")
//                        .insert(4, "/")
//                        .toString();
//                mailBody = StringUtils.replace(mailBody, "[PARM1]", actno2);
//                mailBody = StringUtils.replace(mailBody, "[PARM2]", time2);
//                mailBody = StringUtils.replace(mailBody, "[PARM3]", FormatUtil.decimalFormat(this._smsMsg.getSmsmsgTxAmtAct(), "#,##0"));
//                break;
//            case 5:
//                String txdate1 = new StringBuilder()
//                        .append(this._smsMsg.getSmsmsgTxDate())
//                        .insert(6, "月")
//                        .insert(4, "年")
//                        .append("日")
//                        .toString();
//                String actno1 = new StringBuilder()
//                        .append(this._smsMsg.getSmsmsgTroutActno().substring(2 + 0, 2 + 8))
//                        .append("***")
//                        .append(this._smsMsg.getSmsmsgTroutActno().substring(2 + 11, 2 + 11 + 2))
//                        .append("*")
//                        .insert(13, "-")
//                        .insert(6, "-")
//                        .insert(3, "-")
//                        .toString();
//                String time1 = new StringBuilder()
//                        .append(this._smsMsg.getSmsmsgTxDate())
//                        .append(this._smsMsg.getSmsmsgTxTime())
//                        .insert(12, ":")
//                        .insert(10, ":")
//                        .insert(8, " ")
//                        .insert(6, "/")
//                        .insert(4, "/")
//                        .toString();
//                String brno1 = this._smsMsg.getSmsmsgBrno();
//
//                AllbankExtMapper allbankExtMapper1 = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
//                Allbank defBank1 = allbankExtMapper1.selectByPrimaryKey(SysStatus.getPropertyValue().getSysstatHbkno(), this._smsMsg.getSmsmsgBrno());
//                if (defBank1 != null) {
//                    brno1 = defBank1.getAllbankFullname().trim();
//                }
//                mailBody = StringUtils.replace(mailBody, "[PARM1]", txdate1);
//                mailBody = StringUtils.replace(mailBody, "[PARM2]", brno1);
//                mailBody = StringUtils.replace(mailBody, "[PARM3]", actno1);
//                mailBody = StringUtils.replace(mailBody, "[PARM4]", this._smsMsg.getSmsmsgTxCur());
//                mailBody = StringUtils.replace(mailBody, "[PARM5]", FormatUtil.decimalFormat(this._smsMsg.getSmsmsgTxAmt(), "#,##0"));
//                mailBody = StringUtils.replace(mailBody, "[PARM6]", time1);
//                break;
//            default:
//                // 根據外部傳入的參數值替換body內容
//                if (MapUtils.isNotEmpty(this._notifyData.getParameterData())) {
//                    for (Map.Entry<String, String> entry : this._notifyData.getParameterData().entrySet()) {
//                        mailBody = StringUtils.replace(mailBody, entry.getKey(), entry.getValue());
//                    }
//                }
//                break;
//        }
//
//        // 合庫改為Java Email元件直接發送 (同步)
//        MailSender mailSender = SpringBeanFactoryUtil.getBean(MailSender.class);
//        try {
//            mailSender.sendSimpleEmail(
//                    defSML.getSmlparmFromemail(),
//                    this._smsMsg.getSmsmsgEmail(),
//                    null,
//                    subject,
//                    mailBody,
//                    MailPriority.fromSmlparmPriority(defSML.getSmlparmPriority()));
//            this.logContext.setRemark("Send Mail OK");
//            this.logMessage(this.logContext);
//        } catch (Exception e) {
//            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendMail"));
//            this.logContext.setRemark(StringUtils.join("Send Mail Failed, ", e.getMessage()));
//            this.logContext.setProgramException(e);
//            sendEMS(this.logContext);
//        }
//
//        Notifylog notifyLog = new Notifylog();
//        notifyLog.setNotifylogTxDate(this._notifyData.getTxDate());
//        notifyLog.setNotifylogEj((long) this._notifyData.getEj());
//        notifyLog.setNotifylogType(defSML.getSmlparmType());
//        notifyLog.setNotifylogIdno(StringUtils.EMPTY);
//        notifyLog.setNotifylogProj(defSML.getSmlparmProj());
//        notifyLog.setNotifylogPriority(defSML.getSmlparmPriority());
//        notifyLog.setNotifylogFromname(defSML.getSmlparmFromname());
//        notifyLog.setNotifylogFromemail(defSML.getSmlparmFromemail());
//        notifyLog.setNotifylogTomail(this._smsMsg.getSmsmsgEmail());
//        notifyLog.setNotifylogNumber(StringUtils.EMPTY);
//        notifyLog.setNotifylogPgcode(defSML.getSmlparmPgcode());
//        notifyLog.setNotifylogChannel(defSML.getSmlparmChannel());
//        notifyLog.setNotifylogSubject(subject);
//        notifyLog.setNotifylogContent(mailBody);
//        notifyLog.setNotifylogSendtime(Calendar.getInstance().getTime());
//        this.insertLog(notifyLog);
//        return FEPReturnCode.Normal;
//    }
//
//    /**
//     * For 推播
//     *
//     * @return
//     */
//    private FEPReturnCode sendApp() {
//        SmlparmMapper smlparmMapper = SpringBeanFactoryUtil.getBean(SmlparmMapper.class);
//        Smlparm defSML = smlparmMapper.selectByPrimaryKey("P", this._notifyData.getSmlSeqNoForApp());
//        if (defSML == null) {
//            this.logContext.setRemark("SMLPARM查無資料");
//            this.logMessage(this.logContext);
//            return FEPReturnCode.QueryNoData;
//        }
//        if (StringUtils.isBlank(this._smsMsg.getSmsmsgIdno()))
//        {
//            this.logContext.setRemark("IDNO 無值,不需送推播");
//            this.logMessage(this.logContext);
//            return FEPReturnCode.Normal;
//        }
//        String msg = defSML.getSmlparmContent();
//        // 置換參數內容
//        if (MapUtils.isNotEmpty(this._notifyData.getParameterData())) {
//            for (Map.Entry<String, String> entry : this._notifyData.getParameterData().entrySet()) {
//                msg = StringUtils.replace(msg, entry.getKey(), entry.getValue());
//            }
//        }
//
//        // TODO 合庫改為送IBM MQ
//
//        this.logContext.setRemark("Send to MQ OK");
//        this.logMessage(this.logContext);
//        Notifylog notifyLog = new Notifylog();
//        notifyLog.setNotifylogTxDate(this._notifyData.getTxDate());
//        notifyLog.setNotifylogEj((long) this._notifyData.getEj());
//        notifyLog.setNotifylogType(defSML.getSmlparmType());
//        notifyLog.setNotifylogIdno(this._smsMsg.getSmsmsgIdno());
//        notifyLog.setNotifylogProj(defSML.getSmlparmProj());
//        notifyLog.setNotifylogPriority(defSML.getSmlparmPriority());
//        notifyLog.setNotifylogFromname(StringUtils.EMPTY);
//        notifyLog.setNotifylogFromemail(StringUtils.EMPTY);
//        notifyLog.setNotifylogTomail(StringUtils.EMPTY);
//        notifyLog.setNotifylogNumber(StringUtils.EMPTY);
//        notifyLog.setNotifylogPgcode(defSML.getSmlparmPgcode());
//        notifyLog.setNotifylogChannel(defSML.getSmlparmChannel());
//        notifyLog.setNotifylogSubject(StringUtils.EMPTY);
//        notifyLog.setNotifylogContent(msg);
//        notifyLog.setNotifylogSendtime(Calendar.getInstance().getTime());
//        this.insertLog(notifyLog);
//        return FEPReturnCode.Normal;
//    }
//
//    private void insertLog(Notifylog notifylog) {
//        try {
//            NotifylogMapper mapper = SpringBeanFactoryUtil.getBean(NotifylogMapper.class);
//            mapper.insertSelective(notifylog);
//        } catch (Exception e) {
//            this.logContext.setRemark(StringUtils.join("寫入通知記錄檔失敗:", e.getMessage()));
//            this.logContext.setProgramException(e);
//            sendEMS(this.logContext);
//        }
//    }
//
//    private void updateSmsmsg() {
//        try {
//            Smsmsg def = new Smsmsg();
//            def.setSmsmsgTxDate(this._smsMsg.getSmsmsgTxDate());
//            def.setSmsmsgEjfno(this._smsMsg.getSmsmsgEjfno());
//            def.setSmsmsgSend("Y");
//            if (StringUtils.isNotBlank(this._smsMsg.getSmsmsgSendType())) {
//                def.setSmsmsgSendType(this._smsMsg.getSmsmsgSendType());
//            }
//            SmsmsgExtMapper mapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
//            mapper.updateSmsmsgSendByPrimaryKey(def);
//        } catch (Exception e) {
//            this.logContext.setRemark(StringUtils.join("更新SMSMSG失敗:", e.getMessage()));
//            this.logContext.setProgramException(e);
//            sendEMS(this.logContext);
//        }
//    }
}
