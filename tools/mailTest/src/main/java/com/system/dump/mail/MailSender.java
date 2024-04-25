package com.system.dump.mail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.syscom.fep.frmcommon.util.EmailUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.EmailUtil.MailPriority;

public class MailSender {

	public static void main(String[] args) {
		boolean boo = false;
		try {
			Properties properties = new Properties();
			BufferedReader bufferedReader = new BufferedReader(new FileReader("./mailTest.properties"));
			properties.load(bufferedReader);
			//連接到的MAIL伺服器位置
			String smtp = properties.getProperty("smtp");
			String port = properties.getProperty("port");
			//帳號
			String account = properties.getProperty("account");
			//密碼
			String sscode = properties.getProperty("sscode");
			//寄件者
			String from = properties.getProperty("from");
			//收件者
			String to = properties.getProperty("to");
			//副本
			String cc = properties.getProperty("cc");
			//子標題
			String subject = properties.getProperty("subject");
			//信件內容
			String body = properties.getProperty("body");
			System.out.println("smtp:"+smtp);
			System.out.println("port:"+port);
			System.out.println("account:"+account);
			System.out.println("sscode:"+sscode);
			System.out.println("from:"+from);
			System.out.println("to:"+to);
			System.out.println("cc:"+cc);
			System.out.println("subject:"+subject);
			System.out.println("body:"+body);
			
			boo = send(
					smtp,
					Integer.valueOf(port),
					account,
					sscode,
	                from, to, cc, subject, body, EmailUtil.MailPriority.High);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("result:" + String.valueOf(boo));
	}
	
    public static boolean send(String smtp, int port, String account, String sscode, String from, String to, String cc, String subject, String body) throws Exception {
        return send(smtp, port, account, sscode, from, to, cc, subject, body, MailPriority.Normal);
    }

    public static boolean send(String smtp, int port, String account, String sscode, String from, String to, String cc, String subject, String body, MailPriority mailPriority) throws Exception {
    	return send(smtp, port, account, sscode, from, ArrayUtils.toArray(to), StringUtils.isNotBlank(cc) ? ArrayUtils.toArray(cc) : null, subject, body, mailPriority);
    }

    public static boolean send(String smtp, int port, String account, String sscode, String from, String[] to, String[] cc, String subject, String body) throws Exception {
    	return send(smtp, port, account, sscode, from, to, cc, subject, body, MailPriority.Normal);
    }
	
    public static boolean send(String smtp, int port, String account, String sscode, String from, String[] to, String[] cc, String subject, String body, MailPriority mailPriority) throws Exception {
    	if (ArrayUtils.isEmpty(to) || Arrays.stream(to).filter(t -> StringUtils.isBlank(t)).count() == to.length) {
            return false;
        }
        try {
            JavaMailSenderImpl mailSender = createSender(smtp, port, account, sscode);
            System.out.println("mailSender:"+mailSender);
            if (mailSender == null) {
                return false;
            }else {
            	System.out.println("mailSender getUsername:"+mailSender.getUsername());
            }
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(from));
            for (String t : to) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(t));
            }
            if (ArrayUtils.isNotEmpty(cc)) {
                for (String c : cc) {
                    if (StringUtils.isNotBlank(c))
                        message.addRecipient(Message.RecipientType.CC, new InternetAddress(c));
                }
            }
            message.setSubject(subject);
            message.setText(body);
            if (mailPriority != null) {
                message.setHeader("X-Priority", String.valueOf(mailPriority.getValue()));
            }
            System.out.println("message context:"+message.getSubject());
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            String message = StringUtils.join("Send mail from [", from, "] to [", StringUtils.join(to, ","), "] and cc [", StringUtils.join(cc, ","), "] with subject [", subject, "] failed!!!");
            throw ExceptionUtil.createException(e, message);
        }
    }
    
    private static JavaMailSenderImpl createSender(String smtp, int port, String account, String sscode) {
        if (StringUtils.isBlank(smtp) || port == -1) {
            return null;
        }
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtp);
        mailSender.setPort(port);
        mailSender.setUsername(account);
        mailSender.setPassword(sscode);
        return mailSender;
    }

}
