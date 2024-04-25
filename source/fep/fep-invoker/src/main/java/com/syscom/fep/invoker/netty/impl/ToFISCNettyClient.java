package com.syscom.fep.invoker.netty.impl;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.invoker.netty.SimpleNettyClientShort;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.vo.communication.ToFEPFISCCommu;
import com.syscom.fep.vo.communication.ToFISCCommu;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 發送socket到FISC GW
 * </p>
 * 
 * <ul>
 * <li>
 * 送出的是{@code ToFISCCommu}對應的XML字串
 * </li>
 * <li>
 * 收到的是{@code ToFEPFISCCommu}對應的XML字串
 * </li>
 * </ul>
 * 
 * @author Richard
 *
 */
public class ToFISCNettyClient extends SimpleNettyClientShort<ToFISCNettyClientConfiguration, ToFEPFISCCommu, ToFISCCommu> {

	@Override
	protected ToFEPFISCCommu bytesToMessageIn(byte[] bytes) {
		try {
			String messageIn = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
			if (StringUtils.isNotBlank(messageIn)) {
				return ToFEPFISCCommu.fromXML(messageIn, ToFEPFISCCommu.class);
			}
		} catch (Exception e) {
			LogData logData = new LogData();
			logData.setProgramException(e);
			logData.setProgramName(ProgramName);
			sendEMS(logData);
		}
		return null;
	}

	@Override
	protected byte[] messageOutToBytes(ToFISCCommu messageOut) {
		return ConvertUtil.toBytes(messageOut.toString(), StandardCharsets.UTF_8);
	}

	/**
	 * @param channel
	 * @param state
	 */
	@Override
	public void connStateChanged(Channel channel, SimpleNettyConnState state) {
	}

	/**
	 * @param channel
	 * @param state
	 * @param t
	 */
	@Override
	public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {
	}
}
