package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelInboundHandlerAdapterServerException;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.NotSslRecordException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import javax.net.ssl.SSLHandshakeException;

@Sharable
public class ATMGatewayServerChannelInboundHandlerAdapterException extends NettyTransmissionChannelInboundHandlerAdapterServerException<ATMGatewayServerConfiguration, ATMGatewayServerProcessRequestManager, ATMGatewayServerProcessRequest> {

    public ATMGatewayServerChannelInboundHandlerAdapterException(ATMGatewayServerConfiguration configuration, ATMGatewayServerProcessRequestManager manager) {
        this.initialization(configuration, manager);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.putMDC(ctx);
        ATMGatewayServerProcessRequest processRequest = processRequestManager.getNettyChannelProcessRequest(ctx);
        processRequest.getLogContext().setProgramException(cause);
        processRequest.getLogContext().setProgramName(StringUtils.join(ProgramName, ".exceptionCaught"));
        // 憑證不正確
        boolean verifySslFailed = false;
        NotSslRecordException notSslRecordException = (NotSslRecordException) ExceptionUtil.find(cause, t -> t instanceof NotSslRecordException);
        if (notSslRecordException != null) {
            verifySslFailed = true;
            processRequest.getLogContext().setProgramException(null);
            processRequest.getLogContext().setReturnCode(FEPReturnCode.NOT_SSL_RECORD);
            processRequest.getLogContext().setRemark(
                    StringUtils.join("ATM未使用憑證建立連線, ", notSslRecordException.getMessage(),
                            ", ATM(NO:", processRequest.getAtmNo(),
                            ", IP:", processRequest.getClientIP(),
                            ", Port:", processRequest.getClientPort(),
                            ")"));
        } else {
            SSLHandshakeException sslHandshakeException = (SSLHandshakeException) ExceptionUtil.find(cause, t -> t instanceof SSLHandshakeException);
            if (sslHandshakeException != null) {
                verifySslFailed = true;
                FEPReturnCode rtnCode = FEPReturnCode.INVALID_CERTIFICATE;
                String remark = "ATMGW驗證憑證不正確";
                if (StringUtils.isNotBlank(sslHandshakeException.getMessage()) && "Received fatal alert: certificate_expired".equals(sslHandshakeException.getMessage())) {
                    rtnCode = FEPReturnCode.CERTIFICATE_EXPIRED;
                    remark = "ATMGW驗證憑證已過期";
                } else if (StringUtils.isNotBlank(sslHandshakeException.getMessage()) && "no cipher suites in common".equals(sslHandshakeException.getMessage())) {
                    rtnCode = FEPReturnCode.INVALID_CERTIFICATE_ALIAS;
                    remark = "ATMGW驗證憑證ALIAS不正確";
                } else if (StringUtils.isNotBlank(sslHandshakeException.getMessage()) && "Received fatal alert: unknown_ca".equals(sslHandshakeException.getMessage())) {
                    rtnCode = FEPReturnCode.CERTIFICATE_NOT_MATCH;
                    remark = "ATMGW驗證憑證與ATM不一致";
                }
                processRequest.getLogContext().setProgramException(null);
                processRequest.getLogContext().setReturnCode(rtnCode);
                processRequest.getLogContext().setRemark(
                        StringUtils.join(remark, ", ", sslHandshakeException.getMessage(),
                                ", ATM(NO:", processRequest.getAtmNo(),
                                ", IP:", processRequest.getClientIP(),
                                ", Port:", processRequest.getClientPort(),
                                ")"));
            }
        }
        // 這裡要清掉ReturnCode否則會一直列印出來
        if (verifySslFailed) {
            // 這裡要判斷一下是否有嘗試使用SYSCONF中的ATMCertNo和ATMCertNoOld都不是有效的憑證, 則才送EMS Alert
            ATMGatewayServerClientIpToCertNoHandler atmGatewayServerClientIpToCertNoHandler = SpringBeanFactoryUtil.getBean(ATMGatewayServerClientIpToCertNoHandler.class);
            // 這裡判斷一下是否於有try所有的CertNo
            if (atmGatewayServerClientIpToCertNoHandler.isTryoutAllCertNo(processRequest.getClientIP())) {
                FEPBase.sendEMS(processRequest.getLogContext());
                // 將ATM主檔中憑證Alias欄位更新為空白
                processRequest.setCertAlias(StringUtils.EMPTY);
                processRequest.updateAtmmstr();
            } else {
                LogHelperFactory.getTraceLogger().error(cause, processRequest.getLogContext().getRemark());
                FEPBase.logMessage(Level.WARN, processRequest.getLogContext());
            }
            processRequest.getLogContext().setReturnCode(null);
            // 設定這個Channel需要拒絕
            NettyTransmissionUtil.setChannelReject(ctx.channel(),true);
            // 更新失敗次數
            ATMGatewayServerBlackListHandler atmGatewayServerBlackListHandler = SpringBeanFactoryUtil.getBean(ATMGatewayServerBlackListHandler.class);
            atmGatewayServerBlackListHandler.incrementFailedTimes(processRequest.getClientIP());
        } else {
            FEPBase.sendEMS(processRequest.getLogContext());
        }
        NettyTransmissionUtil.errorMessage(ctx.channel(), cause, "Close the connection when an exception is raised");
        Channel channel = ctx.channel();
        notification.notifyConnStateChanged(channel.id().asLongText(), channel, NettyTransmissionConnState.CLIENT_DISCONNECTED_ON_EXCEPTION_OCCUR, cause);
        ctx.close();
    }
}
