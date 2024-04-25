package com.syscom.fep.enclib.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

@Configuration
@RefreshScope
public class ENCConfiguration implements ApplicationContextAware {
	private static ApplicationContext applicationContext;

	@Value("${spring.fep.enclib.enable:true}")
	private boolean encLibEnable;
	@Value("${spring.fep.enclib.atmagent.enable:false}")
	private boolean encLibAtmAgentEnable;

	public static ENCConfiguration getInstance() {
		return applicationContext.getBean(ENCConfiguration.class);
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		if (applicationContext == null) {
			applicationContext = context;
		}
	}

	public boolean isEncLibEnable() {
		return encLibEnable;
	}

	public boolean isEncLibAtmAgentEnable() {
		return encLibAtmAgentEnable;
	}

	@PostConstruct
	public void print() {
		Field[] fields = ENCConfiguration.class.getDeclaredFields();
		if (ArrayUtils.isNotEmpty(fields)) {
			int repeat = 2;
			// 列印配置檔內容
			StringBuilder sb = new StringBuilder();
			sb.append("ENCLib Configuration:\r\n");
			for (Field field : fields) {
				ReflectionUtils.makeAccessible(field);
				Value annotation = field.getAnnotation(Value.class);
				sb.append(StringUtils.repeat(StringUtils.SPACE, repeat));
				if (annotation != null) {
					sb.append(annotation.value().substring(annotation.value().indexOf("${") + 2, annotation.value().contains(":") ? annotation.value().indexOf(":") : annotation.value().length() - 1));
				} else {
					ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
					if (annotation2 != null) {
						String prefix = annotation2.prefix();
						if (StringUtils.isNotBlank(prefix)) {
							sb.append(prefix).append(".");
						}
					}
					sb.append(field.getName());
				}
				sb.append(" = ").append(ReflectionUtils.getField(field, this)).append("\r\n");
			}
			LogHelperFactory.getGeneralLogger().info(sb.toString());
		}
	}
}
