package com.syscom.fep.jms.instance.ems;

import ch.qos.logback.ext.loggly.LogglyBatchAppender;
import ch.qos.logback.ext.loggly.io.IoUtils;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.instance.ems.sender.EmsQueueSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.JmsException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EmsQueueLogglyBatchAppender<E> extends LogglyBatchAppender<E> {
    private static final String PROGRAM_NAME = EmsQueueLogglyBatchAppender.class.getSimpleName();
    private LogHelper logger = LogHelperFactory.getTraceLogger();

    @Override
    public void start() {
        this.endpointUrl = StringUtils.EMPTY;
        super.start();
    }

    @Override
    protected void processLogEntries(InputStream in) {
        long nanosBefore = System.nanoTime();
        try {
            EmsQueueSender emsQueueSender = SpringBeanFactoryUtil.getBean(EmsQueueSender.class);
            if (emsQueueSender == null) return;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            long len = IoUtils.copy(in, out);
            sentBytes.addAndGet(len);
            out.flush();
            out.close();
            String message = ConvertUtil.toString(out.toByteArray(), StandardCharsets.UTF_8);
            emsQueueSender.sendEMS(message);
            sendSuccessCount.incrementAndGet();
        } catch (JmsException e) {
            logger.error(e, PROGRAM_NAME, ".processLogEntries with exception occur, ", e.getMessage());
            sendExceptionCount.incrementAndGet();
            addError(PROGRAM_NAME + " server-side exception: " + e.getErrorCode() + ": " + e.getMessage());
        } catch (Exception e) {
            logger.error(e, PROGRAM_NAME, ".processLogEntries with exception occur, ", e.getMessage());
            sendExceptionCount.incrementAndGet();
            addError(PROGRAM_NAME + " client-side exception", e);
        } finally {
            sendDurationInNanos.addAndGet(System.nanoTime() - nanosBefore);
        }
    }
}
