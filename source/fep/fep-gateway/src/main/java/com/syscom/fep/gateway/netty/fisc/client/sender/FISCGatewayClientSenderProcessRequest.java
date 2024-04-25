package com.syscom.fep.gateway.netty.fisc.client.sender;

import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClientProcessRequest;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

public class FISCGatewayClientSenderProcessRequest extends FISCGatewayClientProcessRequest<FISCGatewayClientSenderConfiguration> {
	/**
	 * Sender Socket接收資料流程
	 * 
	 * @param ctx
	 * @param logData
	 * @param message
	 * @throws Exception
	 */
	@Override
	protected void doProcess(ChannelHandlerContext ctx, LogData logData, String message) {
		if (message.length() == 40 && RSM_ID.equals(message.substring(8, 16 + 8))) {
			logData.setStan(StringUtils.EMPTY);
			logData.setRemark(StringUtils.join(
					"[", configuration.getSocketType(),
					" IP:", configuration.getHost(),
					",Port:", configuration.getPort(), "] Recv RSM Message"));
			logData.setMessage(message);
			logData.setProgramFlowType(ProgramFlow.FCSGWIn);
			logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
			sendEMS(logData);
			// 980214 FISC新規定
			// 當參加單位端之Sender收到Duplicate ClientID之RSM(08/38)後，財金公司端會主動斷線。
			switch (message.substring(30, 2 + 30)) {
				// 判斷RSM電文的Return code欄位
				case "08":
					// ClientID重覆
					if ("38".equals(message.substring(38, 2 + 38))) {
						logData.setStan(StringUtils.EMPTY);
						logData.setRemark(StringUtils.join(
								"[", configuration.getSocketType(),
								" IP:", configuration.getHost(),
								",Port:", configuration.getPort(), "] ClientID重覆"));
						logData.setMessage(message);
						logData.setProgramFlowType(ProgramFlow.FCSGWIn);
						logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
						sendEMS(logData);
						logMessage(logData);
						clientIdRepeat = true;
					}
					break;
			}
		}
	}
}
