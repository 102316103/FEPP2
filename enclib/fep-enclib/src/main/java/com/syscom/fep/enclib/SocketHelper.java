package com.syscom.fep.enclib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.enclib.enums.ENCProgramFlow;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCConfig;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;

public class SocketHelper {
	private static final String ProgramName = StringUtils.join(SocketHelper.class.getSimpleName(), ".");
	private static final LogHelper TRACELogger = LogHelperFactory.getTraceLogger();
	// 2013-10-03 Modify by Ruling for ENCLib整合FEP和MRM
	private static final int suipTimeout = ENCConfig.SuipTimeout * 1000; // 傳送至suip timeout時間
	private Socket clientSck;
	private String remoteIp;
	private int remotePort;
	private ENCLogData logData;
	private String hsipReturn;
	private boolean keepConnectionOpen;
	private SuipData suipData;

	public SocketHelper(String remoteIP, int remotePort, ENCLogData logData, SuipData suipdata) {
		this.remoteIp = remoteIP;
		this.remotePort = remotePort;
		this.logData = logData;
		this.suipData = suipdata;
	}

	public boolean isKeepConnectionOpen() {
		return keepConnectionOpen;
	}

	public void setKeepConnectionOpen(boolean keepConnectionOpen) {
		this.keepConnectionOpen = keepConnectionOpen;
	}

	public ENCRC connect() {
		final String methodName = "connect";
		ENCRC rc = ENCRC.ENCLibError;
		try {
			TRACELogger.trace("ENCLib try to build connect with Suip on IP:", this.remoteIp, " Port:", this.remotePort);
			this.clientSck = new Socket(this.remoteIp, this.remotePort);
			this.clientSck.setSoTimeout(suipTimeout);
			if (clientSck.isConnected()) {
				TRACELogger.trace("ENCLib Connect Suip OK on IP:", this.remoteIp, " Port:", this.remotePort);
				rc = ENCRC.Normal;
			} else {
				rc = ENCRC.ConnectSuipError;
				String errorMessage = StringUtils.join("ENCLib Connect Failed on Suip IP:", this.remoteIp, " Port:", this.remotePort);
				TRACELogger.trace(errorMessage);
				throw ExceptionUtil.createException(errorMessage);
			}
		} catch (Exception e) {
			rc = ENCRC.ConnectSuipError;
			this.logData.setProgramName(StringUtils.join(ProgramName, methodName));
			this.logData.setRemark(StringUtils.join("ENCLib Connect Failed on Suip IP:", this.remoteIp, " Port:", this.remotePort, " with exception message, ", e.getMessage()));
			this.logData.setProgramException(e);
			ENCSendEMS.sendEMS(this.logData);
			this.closeConnection();
		}
		return rc;
	}

	public ENCRC sendReceive(String sendString, RefString rtnString) {
		ENCRC rc = ENCRC.ENCLibError;
		rtnString.set(StringUtils.EMPTY);
		this.hsipReturn = StringUtils.EMPTY;
		try {
			if (this.clientSck != null && this.clientSck.isConnected()) {
				// 這裡要把十六進制字串轉為字節數組, 長度會減半
				byte[] bytes = ConvertUtil.hexToBytes(sendString);
				// send
				OutputStream out = this.clientSck.getOutputStream();
				long currentTimeMillis = System.currentTimeMillis();
				out.write(bytes);
				out.flush();
				TRACELogger.info("[", this.getClass().getSimpleName(), "] send message on [", this.remoteIp, ":", this.remotePort, "] via socket ", Const.MESSAGE_OUT, sendString);
				this.log("sendReceive", ENCProgramFlow.ENCSocketOut, sendString,
						"ENCLib send data OK on IP=", this.remoteIp, ",Port=", this.remotePort, ",total byte=", bytes.length, ",timeout=", suipTimeout);
				// recv
				byte[] rcv = new byte[2048];
				InputStream in = this.clientSck.getInputStream();
				int rtn = in.read(rcv, 0, rcv.length);
				long elapsedMilliseconds = System.currentTimeMillis() - currentTimeMillis;
				if (rtn > 0) {
					this.hsipReturn = ConvertUtil.toHex(rcv, 0, rtn);
					TRACELogger.info("[", this.getClass().getSimpleName(), "] receive on [", this.remoteIp, ":", this.remotePort, "] via socket ", Const.MESSAGE_IN, this.hsipReturn);
					this.log("sendReceive", ENCProgramFlow.ENCSocketIn, this.hsipReturn, "ENCLib Receive ", rtn, " bytes on IP=", this.remoteIp, ",Port=", this.remotePort, ",Elapsed:",
							elapsedMilliseconds);
					rc = ENCRC.Normal;
					rtnString.set(this.hsipReturn);
				} else {
					rc = ENCRC.ReceiveError;
					throw ExceptionUtil.createTimeoutException("ENCLib receive timeout on IP=", this.remoteIp, ",Port=", this.remotePort);
				}
			} else {
				rc = ENCRC.SendError;
				throw ExceptionUtil.createException("ENCLib send fail on IP=", this.remoteIp, ",Port=", this.remotePort);
			}
		} catch (SocketTimeoutException e) {
			this.logData.setProgramException(e);
			ENCSendEMS.sendEMS(this.logData);
		} catch (IOException e) {
			rc = ENCRC.ReceiveError;
			this.logData.setProgramException(e);
			ENCSendEMS.sendEMS(this.logData);
		} catch (TimeoutException e) {
			// timeout 會retry所以這裏不送EMS
			this.log("sendReceive", ENCProgramFlow.ENCIn, sendString, "ENCLib receive timeout on IP=", this.remoteIp, ",Port=", this.remotePort, ",total byte=", e.getMessage());
			rc = ENCRC.ReceiveError;
		} catch (Exception e) {
			this.logData.setProgramException(e);
			ENCSendEMS.sendEMS(this.logData);
		} finally {
			if (!this.keepConnectionOpen) {
				this.closeConnection(); // 短連接
			}
		}
		return rc;
	}

	private void closeConnection() {
		try {
			if (this.clientSck != null) {
				if (this.clientSck.isConnected()) {
					this.clientSck.close();
				}
				this.clientSck = null;
			}
			TRACELogger.trace("ENCLib Socket Closed on IP:", this.remoteIp, " Port:", this.remotePort);
		} catch (Exception e) {
			this.logData.setProgramException(e);
			ENCSendEMS.sendEMS(this.logData);
		}
	}

	private void log(String methodName, ENCProgramFlow progFlow, String msg, Object... remarks) {
		this.logData.setProgramName(StringUtils.join(ProgramName, methodName));
		this.logData.setProgramFlowType(progFlow);
		this.logData.setMessage(msg);
		this.logData.setRemark(StringUtils.join(StringUtils.join(remarks), ",objectid=", this.hashCode()));
		ENCLib.writeLog(Level.INFO, this.logData, this.suipData.getFunctionNo(), this.suipData.getKeyIdentity(), this.suipData.getInputData1(), this.suipData.getInputData2(),
				StringUtils.EMPTY, StringUtils.EMPTY, msg, StringUtils.EMPTY, StringUtils.EMPTY);
	}
}
