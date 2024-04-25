package com.syscom.safeaa.utils;

import com.syscom.safeaa.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

@Component
@ConfigurationProperties(prefix = "safeaa.config") // 讀取前綴為 config 的內容
@RefreshScope
public class SyscomConfig {
	public String culture;
	public String batchQueue;
	public String applyTemplate;

	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}

	public String getBatchQueue() {
		return batchQueue;
	}

	public void setBatchQueue(String batchQueue) {
		this.batchQueue = batchQueue;
	}

	public String getApplyTemplate() {
		return applyTemplate;
	}

	public void setApplyTemplate(String applyTemplate) {
		this.applyTemplate = applyTemplate;
	}

	@PostConstruct
	public void print() {
		Field[] fields = SyscomConfig.class.getDeclaredFields();
		if (ArrayUtils.isNotEmpty(fields)) {
			int repeat = 2;
			// 列印配置檔內容
			StringBuilder sb = new StringBuilder();
			sb.append("SYSCOM SAFEAA Configuration:\r\n");
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
