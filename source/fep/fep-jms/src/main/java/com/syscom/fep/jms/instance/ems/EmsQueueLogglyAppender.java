package com.syscom.fep.jms.instance.ems;

import ch.qos.logback.ext.loggly.LogglyAppender;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.instance.ems.sender.EmsQueueSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.JmsException;

public class EmsQueueLogglyAppender<E> extends LogglyAppender<E> {
    private static final String PROGRAM_NAME = EmsQueueLogglyAppender.class.getSimpleName();
    private LogHelper logger = LogHelperFactory.getTraceLogger();

    @Override
    public void start() {
        this.endpointUrl = StringUtils.EMPTY;
        super.start();
    }

    @Override
    protected void append(E eventObject) {
        String msg = this.layout.doLayout(eventObject);
        postToLoggly(msg);
    }

    private void postToLoggly(final String event) {
        try {
            EmsQueueSender emsQueueSender = SpringBeanFactoryUtil.getBean(EmsQueueSender.class);
            if (emsQueueSender == null) return;
            emsQueueSender.sendEMS(event);
        } catch (JmsException e) {
            logger.error(e, PROGRAM_NAME, ".postToLoggly with exception occur, ", e.getMessage());
            addError(PROGRAM_NAME + " server-side exception: " + e.getErrorCode() + ": " + e.getMessage());
        } catch (Exception e) {
            logger.error(e, PROGRAM_NAME, ".postToLoggly with exception occur, ", e.getMessage());
            addError(PROGRAM_NAME + " client-side exception", e);
        }
    }
}
