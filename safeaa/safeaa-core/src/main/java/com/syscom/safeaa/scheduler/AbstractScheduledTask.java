package com.syscom.safeaa.scheduler;

import com.syscom.safeaa.log.LogHelper;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractScheduledTask {
    protected String taskName;
    private final LogHelper logger = new LogHelper();
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledFuture = null;

    public AbstractScheduledTask(String taskName) {
        this.taskName = taskName;
        this.executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(taskName));
    }

    public abstract void execute();

    public void schedule(long delay, TimeUnit unit) {
        this.cancel();
        logger.debug("[", this.taskName, "]ScheduledTask is scheduled, delay = [", delay, "], unit = [", unit, "]");
        scheduledFuture = executor.schedule(() -> {
            execute();
        }, delay, unit);
    }

    public void scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit) {
        this.cancel();
        logger.debug("[", this.taskName, "]ScheduledTask is scheduled at fixed rate, initialDelay = [", initialDelay, "], period = [", period, "], unit = [", unit, "]");
        scheduledFuture = executor.scheduleAtFixedRate(() -> {
            execute();
        }, initialDelay, period, unit);
    }

    public void scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit) {
        this.cancel();
        logger.debug("[", this.taskName, "]ScheduledTask is scheduled with fixed rate, initialDelay = [", initialDelay, "], delay = [", delay, "], unit = [", unit, "]");
        scheduledFuture = executor.scheduleWithFixedDelay(() -> {
            execute();
        }, initialDelay, delay, unit);
    }

    public void cancel() {
        if (scheduledFuture != null) {
            try {
                scheduledFuture.cancel(false);
            } catch (Exception e) {
                logger.warn(e, e.getMessage());
            } finally {
                scheduledFuture = null;
                logger.debug("[", this.taskName, "]ScheduledTask is cancelled");
            }
        }
    }

    @PreDestroy
    public void destroy() {
        logger.trace(this.taskName, " start to destroy...");
        try {
            this.executor.shutdown(); // 記得要關閉
            if (this.executor.awaitTermination(60, TimeUnit.SECONDS))
                logger.trace(this.taskName, " executor terminate all runnable successful");
            else
                logger.trace(this.taskName, " executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }
}
