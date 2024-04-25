package com.syscom.fep.frmcommon.log.appender;

import ch.qos.logback.ext.loggly.LogglyBatchAppender;
import ch.qos.logback.ext.loggly.io.IoUtils;
import com.syscom.fep.frmcommon.log.LogHelper;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyLogglyBatchAppender<E> extends LogglyBatchAppender<E> {
    private static final String PROGRAM_NAME = MyLogglyBatchAppender.class.getSimpleName();
    private LogHelper logger = new LogHelper();

    /* Store Connection Read Timeout */
    private int connReadTimeoutSeconds = 1;

    @Override
    protected void processLogEntries(InputStream in) {
        long nanosBefore = System.nanoTime();
        try {

            HttpURLConnection conn = getHttpConnection(new URL(endpointUrl));
            /* Set connection Read Timeout */
            conn.setReadTimeout(connReadTimeoutSeconds * 1000);
            BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());

            long len = IoUtils.copy(in, out);
            sentBytes.addAndGet(len);

            out.flush();
            out.close();

            int responseCode = conn.getResponseCode();
            String response = super.readResponseBody(conn.getInputStream());
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_ACCEPTED:
                    sendSuccessCount.incrementAndGet();
                    break;
                default:
                    sendExceptionCount.incrementAndGet();
                    addError(PROGRAM_NAME + " server-side exception: " + responseCode + ": " + response);
            }
            // force url connection recycling
            try {
                conn.getInputStream().close();
                conn.disconnect();
            } catch (Exception e) {
                // swallow exception
                LogHelper.getAdditionalLogger().warn(e, e.getMessage());
            }
        } catch (Exception e) {
            logger.error(e, PROGRAM_NAME, ".processLogEntries with exception occur, ", e.getMessage());
            sendExceptionCount.incrementAndGet();
            addError(PROGRAM_NAME + " client-side exception", e);
        } finally {
            sendDurationInNanos.addAndGet(System.nanoTime() - nanosBefore);
        }
    }

    /**
     * set method for Logback to allow Connection Read Timeout to be exposed
     */
    public void setConnReadTimeoutSeconds(int connReadTimeoutSeconds) {
        this.connReadTimeoutSeconds = connReadTimeoutSeconds;
    }
}
