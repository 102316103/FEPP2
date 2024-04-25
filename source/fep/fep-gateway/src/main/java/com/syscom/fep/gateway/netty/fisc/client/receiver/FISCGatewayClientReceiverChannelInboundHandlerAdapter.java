package com.syscom.fep.gateway.netty.fisc.client.receiver;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelInboundHandlerAdapterClient;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClientProcessRequest;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;

@Sharable
public class FISCGatewayClientReceiverChannelInboundHandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapterClient<FISCGatewayClientReceiverConfiguration, FISCGatewayClientReceiverProcessRequest> {
    private static final int LL_LENGTH = 2; // 交易類電文長度欄位的長度

    /**
     * 拆解電文
     *
     * @param ctx
     * @param bytes
     * @return
     */
    @Override
    protected List<byte[]> disassembleTransmissionMessage(ChannelHandlerContext ctx, byte[] bytes) {
        LogData logData = new LogData();
        List<byte[]> disassembledMessages = new ArrayList<byte[]>();
        int offset = 0, remainder = bytes.length - offset, ll = 0;
        while (true) {
            // 計算剩餘電文長度
            remainder = bytes.length - offset;
            if (remainder <= 0)
                break;
            // RSM電文
            ll = 20;
            if (remainder >= ll && FISCGatewayClientProcessRequest.RSM_ID.equals(StringUtil.toHex(ArrayUtils.subarray(bytes, offset + 4, offset + 12)))) {
                if (offset + ll > bytes.length) {
                    break;
                }
                addDisassembledMessages(ctx, disassembledMessages, ArrayUtils.subarray(bytes, offset, offset + ll), logData,
                        StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Receiver, " disassemble the RSM data received From CBS"));
                offset += ll;
                continue;
            }
            // 交易類電文
            if (remainder > LL_LENGTH) {
                // 取出電文長度
                ll = Integer.parseInt(StringUtil.toHex(ArrayUtils.subarray(bytes, offset, offset + LL_LENGTH)), 16);
                if (offset + ll > bytes.length) {
                    break;
                }
                addDisassembledMessages(ctx, disassembledMessages, ArrayUtils.subarray(bytes, offset, offset + ll), logData,
                        StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Receiver, " disassemble the Transaction data received From CBS"));
                offset += ll;
                continue;
            }
            // 上述都不符合要求, 則直接添加返回
            break;
        }
        // 最後的一筆電文長度不夠
        if (remainder > 0) {
            addDisassembledMessages(ctx, disassembledMessages, ArrayUtils.subarray(bytes, offset, bytes.length), logData,
                    StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Receiver, " detected that there are also remaining data received From CBS"));
        }
        return disassembledMessages;
    }

    private void addDisassembledMessages(ChannelHandlerContext ctx, List<byte[]> disassembledMessages, byte[] bytes, LogData logData, String remark) {
        disassembledMessages.add(bytes);
        String message = StringUtil.toHex(bytes);
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.FISC);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".addDisassembledMessages"));
        logData.setMessage(message);
        logData.setRemark(remark);
        FEPBase.logMessage(Level.WARN, logData);
        NettyTransmissionUtil.warnMessage(ctx.channel(), remark, ", message = [", message, "]");
    }
}