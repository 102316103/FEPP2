package com.syscom.fep.web.service;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.web.base.FEPWebBase;

public class BaseService extends FEPWebBase {

	protected FEPChannel getFEPChannel() {
		return FEPChannel.FEP;
	}

	protected SubSystem getSubSystem() {
		return SubSystem.FEPMonitor;
	}

	protected String getInnerMessage(Throwable t) {
		return t.getMessage();
	}

	protected void sendEMS(Object... msg) {
		sendEMS(Level.ERROR, null, msg);
	}

	protected void sendEMS(Throwable t, Object... msg) {
		sendEMS(Level.ERROR, t, msg);
	}

	protected void sendEMS(Level level, Throwable t, Object... msg) {
		LogData logData = new LogData();
		logData.setChannel(this.getFEPChannel());
		logData.setSubSys(this.getSubSystem());
		if (t != null) {
			logData.setProgramException(t);
		}
		if (ArrayUtils.isNotEmpty(msg)) {
			logData.setMessage(StringUtils.join(msg));
		}
		FEPBase.sendEMS(logData);
	}
}