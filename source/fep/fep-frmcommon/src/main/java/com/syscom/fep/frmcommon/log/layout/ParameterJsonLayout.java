package com.syscom.fep.frmcommon.log.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;

public class ParameterJsonLayout extends JsonLayout {
	private List<Parameter> parameters = new ArrayList<>();

	public void addParameter(Parameter parameter) {
		this.parameters.add(parameter);
	}

	@Override
	protected void addCustomDataToJsonMap(Map<String, Object> map, ILoggingEvent event) {
		for (Parameter parameter : parameters) {
			map.put(parameter.getName(), parameter.getLayout());
		}
	}

	public static class Parameter {
		private String name;
		private String type;
		private String layout;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getLayout() {
			return layout;
		}

		public void setLayout(String layout) {
			this.layout = layout;
		}
	}
}
