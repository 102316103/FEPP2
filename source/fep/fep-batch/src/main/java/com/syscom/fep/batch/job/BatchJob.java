package com.syscom.fep.batch.job;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.batch.policy.BatchJobPolicy;
import com.syscom.fep.batch.policy.BatchJobPolicyConclusion;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;

import java.util.Calendar;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public abstract class BatchJob implements Job {
    protected static final LogHelper logger = LogHelperFactory.getGeneralLogger();

    /**
     * 執行任務
     *
     * @param context
     */
    protected abstract void executeJob(JobExecutionContext context, BatchJobParameter parameter) throws Exception;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        BatchJobParameter parameter = (BatchJobParameter) jobDataMap.get(BatchJobParameter.getJobDataMapKey());
        logger.info(this.getClass().getSimpleName(), parameter.getLogContent(), "start to execute...");
        try {
            parameter.setNextQuartzFireDateTime(context.getNextFireTime());
            parameter.setNextExecutedDateTime(CalendarUtil.clone(context.getNextFireTime()));
            BatchJobPolicyConclusion conclusion = null;
            if (StringUtils.isNotBlank(parameter.getPolicyClassname())) {
                try {
                    Class<BatchJobPolicy> policyClass = (Class<BatchJobPolicy>) Class.forName(parameter.getPolicyClassname());
                    BatchJobPolicy policy = SpringBeanFactoryUtil.getBean(policyClass);
                    if (policy != null) {
                        conclusion = policy.execute(context, parameter);
                        if (conclusion != null && conclusion.isSkip()) {
                            logger.info(this.getClass().getSimpleName(), parameter.getLogContent(), "skip to execute...");
                            return;
                        }
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                } finally {
                    // 必要時根據policy校正更新一些資訊
                    updateByPolicy(parameter, conclusion);
                }
            }
            // 塞入最近一次執行時間
            parameter.setLatestExecutedDateTime(Calendar.getInstance());
            // 必要時根據policy校正更新一些資訊
            updateByPolicy(parameter, conclusion);
            try {
                this.executeJob(context, parameter);
            } catch (Exception e) {
                logger.exceptionMsg(e, e.getMessage());
            } finally {
                logger.info(this.getClass().getSimpleName(), parameter.getLogContent(), "execute finished, next execute time = [", FormatUtil.dateTimeFormat(parameter.getNextExecutedDateTime().getTime()),"]");
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
        } finally {
            jobDataMap.put(BatchJobParameter.getJobDataMapKey(), parameter); // 上面改變了param的值, 所以這裡一定要再put一次, 否則不會寫入db
        }
    }

    /**
     * 必要時根據policy校正更新一些資訊
     *
     * @param parameter
     * @param conclusion
     */
    private void updateByPolicy(BatchJobParameter parameter, BatchJobPolicyConclusion conclusion) {
        if (conclusion != null) {
            // 最近一次執行時間
            if (conclusion.getLatestExecutedDateTime() != null) {
                parameter.setLatestExecutedDateTime(conclusion.getLatestExecutedDateTime());
            }
            // 預計下一次執行時間
            if (conclusion.getNextExecutedDateTime() != null) {
                parameter.setNextExecutedDateTime(conclusion.getNextExecutedDateTime());
            }
        }
    }
}
