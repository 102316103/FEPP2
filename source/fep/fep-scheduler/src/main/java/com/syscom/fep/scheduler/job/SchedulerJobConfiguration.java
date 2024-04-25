package com.syscom.fep.scheduler.job;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = SchedulerJobConstant.CONFIGURATION_PROPERTIES_PREFIX)
@RefreshScope
public class SchedulerJobConfiguration implements SchedulerJobConstant {
    private List<SchedulerJobRegister> register;
    private String threadPoolClass = "org.quartz.simpl.SimpleThreadPool";
    private int threadPoolCount = 10;
    private int threadPoolPriority = 5;

    public void setRegister(List<SchedulerJobRegister> register) {
        this.register = register;
    }

    public void setThreadPoolClass(String threadPoolClass) {
        this.threadPoolClass = threadPoolClass;
    }

    public void setThreadPoolCount(int threadPoolCount) {
        this.threadPoolCount = threadPoolCount;
    }

    public void setThreadPoolPriority(int threadPoolPriority) {
        this.threadPoolPriority = threadPoolPriority;
    }

    protected void registerBean() {
        if (CollectionUtils.isNotEmpty(register)) {
            for (SchedulerJobRegister jobRegister : register) {
                try {
                    SpringBeanFactoryUtil.registerBean(jobRegister.getConfigClassName());
                    SpringBeanFactoryUtil.registerBean(jobRegister.getClassName());
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn("Cannot register bean = [", jobRegister, "], maybe it is needless for this application");
                }
            }
        }
    }

    @Bean(name = SCHEDULER_JOB_FACTORY_NAME)
    @Primary
    public JobFactory schedulerJobFactory() {
        return new AdaptableJobFactory() {
            @Override
            protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
                JobDetail jobDetail = bundle.getJobDetail();
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                if (jobDataMap.getBooleanValue(JOB_DATA_MAP_KEY_IS_SIMPLE)) {
                    SchedulerSimpleJob job = (SchedulerSimpleJob) super.createJobInstance(bundle);
                    SchedulerJobConfig config = (SchedulerJobConfig) jobDataMap.get(JOB_DATA_MAP_KEY_JOB_CONFIG);
                    job.setConfig(config);
                    return job;
                }
                return SpringBeanFactoryUtil.getBean(jobDetail.getJobClass());
            }
        };
    }

    @Bean(name = SCHEDULER_JOB_FACTORY_SCHEDULER_NAME)
    @Primary
    public SchedulerFactoryBean schedulerJobFactoryBean(@Qualifier(SCHEDULER_JOB_FACTORY_NAME) JobFactory factory) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setBeanName(SCHEDULER_JOB_FACTORY_SCHEDULER_NAME);
        schedulerFactoryBean.setJobFactory(factory);
        schedulerFactoryBean.setSchedulerName(SCHEDULER_NAME);
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", SCHEDULER_NAME);
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        prop.put("org.quartz.threadPool.class", threadPoolClass);
        prop.put("org.quartz.threadPool.threadCount", Integer.toString(threadPoolCount));
        prop.put("org.quartz.threadPool.threadPriority", Integer.toString(threadPoolPriority));
        schedulerFactoryBean.setQuartzProperties(prop);
        return schedulerFactoryBean;
    }

    @PostConstruct
    public void print() {
        ConfigurationProperties annotation2 = SchedulerJobConfiguration.class.getAnnotation(ConfigurationProperties.class);
        if (annotation2 != null) {
            String prefix = annotation2.prefix();
            Field[] fields = SchedulerJobConfiguration.class.getDeclaredFields();
            if (ArrayUtils.isNotEmpty(fields)) {
                int repeat = 2;
                // 列印配置檔內容
                StringBuilder sb = new StringBuilder();
                sb.append("Scheduler Job Configuration:\r\n");
                for (Field field : fields) {
                    if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    ReflectionUtils.makeAccessible(field);
                    Object object = ReflectionUtils.getField(field, this);
                    String prefix2 = prefix == null ? StringUtils.EMPTY : StringUtils.join(prefix, ".");
                    if (object instanceof List) {
                        prefix2 = StringUtils.join(prefix2, field.getName());
                        List<?> list = (List<?>) object;
                        int i = 0;
                        for (Object e : list) {
                            print(e, StringUtils.join(prefix2, "[", i++, "]"), sb);
                        }
                    } else {
                        sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                                .append(prefix2)
                                .append(field.getName())
                                .append(" = ").append(object).append("\r\n");
                    }
                }
                LogHelperFactory.getGeneralLogger().info(sb.toString());
                return;
            }
        }
        LogHelperFactory.getGeneralLogger().info(this.getClass().getSimpleName(), ":\r\n", ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    private void print(Object object, String prefix, StringBuilder sb) {
        int repeat = 2;
        if ("java.lang".equals(object.getClass().getPackage().getName())) {
            sb.append(StringUtils.repeat(StringUtils.SPACE, 2))
                    .append(prefix)
                    .append(" = ")
                    .append(object)
                    .append("\r\n");
        } else {
            Field[] fields = object.getClass().getDeclaredFields();
            if (ArrayUtils.isNotEmpty(fields)) {
                for (Field field : fields) {
                    ReflectionUtils.makeAccessible(field);
                    sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                            .append(prefix).append(".")
                            .append(field.getName()).append(" = ")
                            .append(ReflectionUtils.getField(field, object))
                            .append("\r\n");
                }
            }
        }
    }
}
