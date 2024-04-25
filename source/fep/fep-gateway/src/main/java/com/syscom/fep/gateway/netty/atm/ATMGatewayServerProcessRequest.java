package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.gateway.entity.AtmStatus;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import com.syscom.fep.gateway.entity.InitKeyStatus;
import com.syscom.fep.gateway.entity.SocketStatus;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServer;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.gateway.util.GatewayCommuHelper;
import com.syscom.fep.gateway.util.GatewayUtil;
import com.syscom.fep.invoker.FEPInvoker;
import com.syscom.fep.vo.communication.*;
import com.syscom.fep.vo.enums.RestfulResultCode;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.response.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.Calendar;

@StackTracePointCut(caller = SvrConst.SVR_ATM_GATEWAY)
public class ATMGatewayServerProcessRequest extends NettyTransmissionChannelProcessRequestServer<ATMGatewayServerConfiguration> {
    private String clientIP;
    private int clientPort;
    private String atmNo;
    private String atmZone;
    private boolean clearAll;
    private boolean needCheckMac;
    private boolean byPassIP;
    private boolean disableService;
    private InitKeyStatus initKey;
    private SocketStatus socketStatus;
    private boolean needUpdateAtmstat;
    private short encCheckErrorCount;
    private short atmFepConnection;
    private String atmstatApVersionN;

    @Autowired
    private GatewayCommuHelper commuHelper;
    @Autowired
    private ATMGatewayServerProcessRequestManager manager;
    @Autowired
    private GatewayUtil gatewayUtil;
    @Autowired
    private FEPInvoker invoker;

    private boolean hasCheckSecurity;
    private boolean hasCheckAtmmstrByAtmNo = false;
    private boolean hasUpdateAtmstatApVersionN = false;
    private String certAlias;

    @PostConstruct
    public void initATMGatewayServerProcessRequest() {
        this.logContext.setSubSys(SubSystem.GW);
        this.logContext.setChannel(FEPChannel.ATM);
        this.clearAll = false;
    }

    @Override
    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        super.setChannelHandlerContext(channelHandlerContext);
        if (StringUtils.isBlank(this.clientIP)) {
            this.clientIP = this.channelInformation.getRemoteIp();
        }
        if (this.clientPort == 0) {
            this.clientPort = this.channelInformation.getRemotePort();
        }
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state) {
        super.connStateChanged(channel, state);
        // SSL憑證Ok, 則從黑明單中移除, 將憑證alias更新到ATM主檔
        if (state == NettyTransmissionConnState.SSL_CERTIFICATE_ACCEPT) {
            // 從黑明單中移除
            ATMGatewayServerBlackListHandler blackListHandler = SpringBeanFactoryUtil.getBean(ATMGatewayServerBlackListHandler.class);
            blackListHandler.remove(this.clientIP);
            // 塞入certAlias, 並更新到ATM主檔
            ATMGatewayServerClientIpToCertNoHandler clientIpToCertNoHandler = SpringBeanFactoryUtil.getBean(ATMGatewayServerClientIpToCertNoHandler.class);
            this.certAlias = clientIpToCertNoHandler.getAndRemoveCertNo(this.clientIP);
            this.updateAtmmstr();
        }
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        boolean closeConnectionByManual = false;
        try {
            final String methodName = StringUtils.join(ProgramName, ".doProcess");
            Channel channel = ctx.channel();
            this.logContext.clear();
            String rcvData = StringUtil.toHex(bytes);
            LogHelperFactory.getTraceLogger().trace("ATMGW Recv Data:", rcvData);
            if ("00".equals(rcvData)) {
                // 忽略diebold送的00
                NettyTransmissionUtil.warnMessage(channel, "ignore message = [", rcvData, "]");
                return;
            }
            // keepalive功能, 前端沒交易時, 會發keepalive電文過來
            if (checkKeepAlive(channel, rcvData)) {
                return;
            }
            // 檢核不通過, 則終止當前連線
            if (StringUtils.isBlank(this.getAtmNo()) && !this.checkAtmmstrByAtmNo(ctx, rcvData)) {
                closeConnectionByManual = true;
                return;
            }
            // 2022-08-24 Richard modified start
            // GW不能直接連DB, 所以改為從透過FISCService取資料
            // this.ej = ejfnoGenerator.generate();
            // 2022-11-24 Richard mark 合庫，你幫我把atmgw取EJ功能拿掉, 改由atmService取就好 by Ashiang
            // this.ej = dbHelper.getEjfnoFromFEPATM(this.getTimeout());
            // 2022-08-24 Richard modified end
            // 先記一筆完整電文
            this.logContext.setEj(this.ej);
            this.logContext.setAtmNo(this.atmNo);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayIn);
            this.logContext.setProgramName(methodName);
            this.logContext.setRemark(StringUtils.join("ATMGW Receive From ATM(NO:", this.atmNo, ",IP:", this.clientIP, ",Port:", this.clientPort, "),Length:", bytes.length));
            this.logContext.setMessage(rcvData);
            this.logContext.setTxRquid(UUIDUtil.randomUUID(true));
            logMessage(this.logContext);
            // 要求checkmac,進行ENC相關檢核
            // 2022-08-23 Richard mark start
            // 這段合庫不會有, 點掉, 合庫版, ATMGW只檢查IP by Ashiang
            // if (this.needCheckMac) {
            // String replyData = StringUtils.EMPTY;
            // // 需要驗MAC但尚未檢核過就收到交易電文時,發送4A要求ATM送2A交易
            // if (rcvData.length() > 63 && !hasCheckSecurity) {
            // this.sendToATM(channel, "NOSECURITY",
            // MessageFormat.format(GatewayCodeConstant.ToATMNoSecurityData,
            // PolyfillUtil.toString(this.ej, "0000000000").substring(4)));
            // return;
            // }
            // if (this.initKey == InitKeyStatus.Initial) {
            // this.initKey = InitKeyStatus.Done;
            // this.sendToATM(channel, "CHANGEFST",
            // MessageFormat.format(GatewayCodeConstant.ToATMChangeFirst,
            // PolyfillUtil.toString(this.ej, "0000000000").substring(4)));
            // return;
            // }
            // RefString refReplyData = new RefString(replyData);
            // FEPReturnCode rtn = this.checkMacAndMakeMac(this.ej, rcvData, refReplyData);
            // replyData = refReplyData.get();
            // if (rtn == FEPReturnCode.Normal) {
            // this.sendToATM(channel, replyData.substring(20, 4 + 20), replyData);
            // } else {
            // this.encCheckErrorCount += 1;
            // // 檢核ENC KEY錯誤次數達5次則UpdateATMSTatus不允許連線(需reset)
            // if (this.encCheckErrorCount == 5) {
            // this.socketStatus = SocketStatus.Close;
            // closeConnectionByManual = true;
            // return;
            // }
            // if (StringUtils.isNotBlank(replyData)) {
            // this.sendToATM(channel, replyData.substring(20, 4 + 20), replyData);
            // } else {
            // closeConnectionByManual = true;
            // return;
            // }
            // }
            // return;
            // }
            // 2022-08-23 Richard mark end
            String atmSeq = StringUtils.EMPTY;
            if (bytes.length >= 35) {
                String messageId = StringUtils.EMPTY;
                if (bytes.length == 35) { // ChangeKey
                    messageId = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(22, 28)) + "-" + EbcdicConverter.fromHex(CCSID.English, rcvData.substring(64, 68));
                    atmSeq = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(52, 60));
                } else {
                    if (rcvData.length() >= 146) {
                        messageId = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(34, 40)) + "-" + EbcdicConverter.fromHex(CCSID.English, rcvData.substring(146, 150));
                        atmSeq = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(64, 72));
                    } else {
                        // 補摺
                        messageId = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(38, 42));
                    }
                }
                this.logContext.setAtmSeq(atmSeq);
                this.logContext.setProgramName(methodName);
                this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                this.logContext.setMessageId(messageId);
                // this.logContext.setAtmSeq(StringUtil.fromHex(rcvData.substring(124, 16 + 124)));
                this.logContext.setRemark(StringUtils.join("ATMGW Receive TxTele From ATM(NO:", this.atmNo, ",IP:", this.clientIP, ",Port:", this.clientPort, ")"));
                logMessage(this.logContext);
                if (!this.disableService) {
                    String rtnData = StringUtils.EMPTY;
                    if (configuration.isForwardTransmissionToFep()) {
                        try {
                            this.logContext.setMessage(rcvData);
                            ToFEPATMCommu request = new ToFEPATMCommu();
                            request.setAtmno(this.atmNo);
                            request.setEj(String.valueOf(this.ej));
                            request.setMessage(rcvData);
                            request.setSync(false); // FEP接收後進行異步處理
                            request.setTxRquid(this.logContext.getTxRquid());
                            rtnData = this.sendToATMServiceViaFEPInvoker(request, messageId);
                            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayOut);
                            this.logContext.setMessageFlowType(MessageFlow.Response);
                            this.logContext.setProgramName(methodName);
                            this.logContext.setMessageId(messageId);
                            this.logContext.setRemark(StringUtils.join("ATMGW Recv Msg From ATM Service, Timeout:", this.configuration.getTimeout()));
                            this.logContext.setMessage(rtnData);
                            logMessage(this.logContext);
                        } catch (Exception e) {
                            if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
                                this.logContext.setRemark("ATM Service is unreachable");
                            } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
                                this.logContext.setRemark("Invoke AA Timeout");
                            }
                            this.logContext.setProgramException(e);
                            this.logContext.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
                            sendEMS(this.logContext);
                        }
                        if (StringUtils.isNotBlank(rtnData) && !"E552".equals(rtnData)) {
                            try {
                                ToATMCommu toATMCommu = ToATMCommu.fromXML(rtnData, ToATMCommu.class);
                                rtnData = toATMCommu.getMessage();
                            } catch (Exception e) {
                                this.logContext.setRemark("deserialize ATMRestFulResponse xml string failed");
                                this.logContext.setProgramException(e);
                                this.logContext.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
                                sendEMS(this.logContext);
                            }
                        }
                    } else {
                        // 直接將收到的訊息返回給Client
                        rtnData = rcvData;
                    }
                    this.sendToATM(channel, this.getLogContext().getMessageId(), rtnData);
                } else {
                    // 暫停服務
                    String rtnData = this.makeAtmResponseFromRequest(rcvData, "0202");
                    this.logContext.setRemark(StringUtils.join("ATMGW Stop Service By ATM(NO:", this.atmNo, ",IP:", this.clientIP, ",Port:", this.clientPort, ")"));
                    logMessage(this.logContext);
                    if (StringUtils.isNotBlank(rtnData)) {
                        this.sendToATM(channel, this.logContext.getMessageId(), rtnData);
                    }
                }
            } else {
                return;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            sendEMS(this.logContext);
        } finally {
            if (closeConnectionByManual) {
                this.logContext.setRemark(StringUtils.join("Begin closeConnection by closeConnectionByManual:", closeConnectionByManual));
                this.logContext.setMessage("");
                this.logMessage(this.logContext);
                this.closeConnection();
            }
        }
    }

    /**
     * 通過配置檔決定丟訊息via Socket還是Restful
     *
     * @param request
     * @param messageId
     * @return
     */
    private String sendToATMServiceViaFEPInvoker(ToFEPATMCommu request, String messageId) {
        String rtnData = StringUtils.EMPTY;
        String tita = request.toString();
        int timeout = this.getTimeout();
        try {
            this.logContext.setRemark(StringUtils.join("Begin Send data to ATMService, timeout:", timeout));
            this.logContext.setMessage(tita);
            this.logMessage(this.logContext);
            rtnData = invoker.sendReceiveToFEPATM(request, timeout);
            this.logContext.setRemark(StringUtils.join("Send data to ATMService OK, timeout:", timeout));
            this.logContext.setMessage(tita);
            this.logMessage(this.logContext);
        } catch (Exception e) {
            this.logContext.setRemark(StringUtils.join("Send data to ATMService Failed, timeout:", timeout));
            this.logContext.setProgramName("ATMGateway.sendToATMServiceViaFEPInvoker");
            this.logContext.setProgramException(e);
            sendEMS(this.logContext);
        }
        return rtnData;
    }

    /**
     * keepalive功能, 前端沒交易時, 會發keepalive電文過來
     * <p>
     * 格式為LLKEEPALIVEREQYYYYMMDDHHMMSS1.0.0
     * <p>
     * LL是長度, YYYYMMDDHHMMSS是日期間
     * <p>
     * 收到這種電文, 直接回LLKEEPALIVERSPYYYYMMDDHHMMSS1.0.0即可
     *
     * @param channel
     * @param rcvData
     * @return
     * @throws DecoderException
     */
    private boolean checkKeepAlive(Channel channel, String rcvData) throws DecoderException {
        String plain = StringUtil.fromHex(rcvData);
        if (RegexUtil.find(GatewayCodeConstant.FromATMKeepAliveData, plain)) {
            // 不帶版本號的電文長度固定是0x1C
            final int lengthWithoutVersion = 0x1C;
            // 電文前兩位LL
            String LL = plain.substring(0, 2);
            // 轉為電文長度
            int length = Integer.parseInt(LL, 16);
            // 版本號
            String version = StringUtils.EMPTY;
            // 如果電文長度大於不帶版本號的電文長度, 則說明電文最後是帶版本號的
            if (length > lengthWithoutVersion) {
                // 取出版本號
                version = plain.substring(lengthWithoutVersion);
                // 取到版本號之後, 更新到ATMSTAT.ATMSTAT_AP_VERSION_N
                if (!this.hasUpdateAtmstatApVersionN && StringUtils.isNotBlank(version)) {
                    // 塞入版本號
                    this.atmstatApVersionN = version;
                    // 更新到ATMSTAT檔
                    this.updateAtmstat(AtmStatus.Connected);
                    // 僅第一次更新, 後面就不要再更新了
                    this.hasUpdateAtmstatApVersionN = true;
                }
            }
            // 組建回應電文
            String content = MessageFormat.format(GatewayCodeConstant.ToATMKeepAliveData,
                    StringUtils.join(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN), version));
            // 電文長度
            LL = NumberUtil.toHex(content.length() + 2, true, 2, "0");
            // 轉成Hex
            String data = StringUtil.toHex(StringUtils.join(LL, content));
            // 回應給ATM
            this.sendToATM(channel, "KeepAlive", data);
            return true;
        }
        return false;
    }

    /**
     * 依據ATMNO來進行判斷
     * <p>
     * ATMGW部分, 原先會根據連線的來源IP去讀取ATM檔來取得ATMNO及判斷是否可以連線,
     * 但因為壓測時只能從1臺機器模擬ATM發動交易, 這樣來源IP都會是同一個就無法壓測,
     * 所以請修改ATMGW程式, 改成從ATM電文header中抓ATMNO後再去讀ATM檔, 謝謝.
     * <p>
     * by Ashiang
     *
     * @param ctx
     * @param rcvData
     * @return 返回true, 表示檢核不同過, 終止當前連線
     */
    private boolean checkAtmmstrByAtmNo(ChannelHandlerContext ctx, String rcvData) {
        // if (!hasCheckAtmmstrByAtmNo && configuration.isCheckAtmmstrByAtmNo() && rcvData.length() >= 140) {
        if (configuration.isCheckAtmmstrByAtmNo()) {
            try {
                String atmNo = null;
                // 2023-08-30 這是永豐的邏輯
                // String brNo = StringUtil.fromHex(rcvData.substring(52, 6 + 52));
                // String wsNo = StringUtil.fromHex(rcvData.substring(58, 4 + 58));
                // atmNo = StringUtils.join(brNo, wsNo);
                // if (!hasCheckAtmmstrByAtmNo && rcvData.length() >= 50) {
                //     atmNo = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(126, 142));
                // } else if (rcvData.length() == 70 && "C3D2".equals(rcvData.substring(64, 68))) {
                //     atmNo = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(0, 16));
                // }
                if (rcvData.length() == 70 && "C3D2".equals(rcvData.substring(64, 68))) {
                    atmNo = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(0, 16));
                } else {
                    atmNo = EbcdicConverter.fromHex(CCSID.English, rcvData.substring(126, 142));
                }
                ToATMCommuAtmmstr atmmstr = this.commuHelper.getAtmmstrByAtmNo(this.logContext, atmNo, this.getTimeout());
                manager.checkAtmmstr(ctx, this.logContext, this.clientIP, this.clientPort, byPassIP, atmmstr);
            } catch (Exception e) {
                NettyTransmissionUtil.errorMessage(ctx.channel(), "Client rejected, ", e.getMessage());
                this.logContext.setProgramException(e);
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkAtmmstrByAtmNo"));
                sendEMS(this.logContext);
                return false;
            } finally {
                // 只檢核一次
                hasCheckAtmmstrByAtmNo = true;
            }
        }
        return true;
    }

    @Override
    public void closeConnection() {
        super.closeConnection();
        try {
            if (!this.byPassIP) {
                this.logContext.setRemark(StringUtils.join("ATM(NO:", this.atmNo, ",IP:", this.clientIP, ",Port:", this.clientPort, ")Disconnected"));
                this.logContext.setMessage(StringUtils.EMPTY);
                this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayOut);
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                this.logContext.setProgramException(null);
                sendEMS(this.logContext);
            }
            this.channelHandlerContext.close();
            if (StringUtils.isBlank(this.atmNo)) {
                return;
            }
            if (!this.clearAll) {
                manager.removeATMGatewayServerProcessRequest(channelHandlerContext, this.atmNo);
            }
            if (this.needUpdateAtmstat) {
                // 更新ATM狀態檔ATM已斷線
                this.updateAtmstat(AtmStatus.Disconnected);
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
            FEPBase.sendEMS(logContext);
        }
    }

    public void updateAtmmstr() {
        if (StringUtils.isBlank(this.atmNo))
            return;
        this.commuHelper.updateAtmmstr(this.logContext, this.atmNo, this.configuration.getHost(), String.valueOf(this.configuration.getPort()), this.certAlias, this.getTimeout());
    }

    public void updateAtmstat(AtmStatus atmStatus) {
        if (StringUtils.isBlank(this.atmNo))
            return;
        ToFEPATMCommuUpdateAtmstat request = new ToFEPATMCommuUpdateAtmstat();
        request.setAtmstatAtmno(this.atmNo);
        request.setAtmstatStatus((short) atmStatus.getValue());
        request.setAtmstatSocket((short) socketStatus.getValue());
        request.setAtmstatSec(encCheckErrorCount);
        request.setAtmstatInikey((short) initKey.getValue());
        request.setAtmAtmpIp(this.configuration.getHost());
        request.setAtmstatApVersionN(this.atmstatApVersionN);
        this.commuHelper.updateAtmstat(this.logContext, request, this.getTimeout());
    }

    public String makeAtmMessage(String msgId) {
        ATMGeneral obj = new ATMGeneral();
        String sRtn = StringUtils.EMPTY;
        try {
            // 2022-08-24 Richard mark start
            // GW不能直接連DB, 所以改為從透過ATMService取資料
            // Zone zone = FEPCache.getZoneList().stream().filter(t ->
            // t.getZoneCode().equals(this.atmZone)).findFirst().orElse(null);
            // 2022-08-24 Richard mark end
            ToGWCommuZone toGWCommuZone = commuHelper.getZoneByZoneCodeFromFEPATM(this.logContext, this.atmZone, this.getTimeout());
            // ben20221118 obj.getResponse().setTxcdO(msgId);
            // ben20221118
            // obj.getResponse().setDATE(CalendarUtil.adStringToROCString(StringUtils.leftPad(FormatUtil.dateTimeFormat(Calendar.getInstance(),
            // FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN), 8, '0')));
            // ben20221118
            // obj.getResponse().setTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(),
            // FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            // ben20221118 obj.getResponse().setAtmnoO(this.atmNo);
            // 2022-08-24 Richard mark start
            // GW不能直接連DB, 所以改為從透過ATMService取資料
            // obj.getResponse().setModeO(zone.getZoneCbsMode().toString());
            // obj.getResponse().setDdO(CalendarUtil.adStringToROCString(StringUtils.leftPad(zone.getZoneTbsdy(),
            // 8, '0')));
            // 2022-08-24 Richard mark end
            // ben20221118
            // obj.getResponse().setModeO(Short.toString(toGWCommuZone.getZoneCbsMode()));
            // ben20221118
            // obj.getResponse().setDdO(CalendarUtil.adStringToROCString(StringUtils.leftPad(toGWCommuZone.getZoneTbsdy(),
            // 8, '0')));
            // ben20221118 obj.getResponse().setDepmodeO("6");
            // ben20221118 obj.getResponse().setAtmseqO1(StringUtils.EMPTY);
            // ben20221118 obj.getResponse().setAtmseqO2(StringUtils.EMPTY);
            switch (msgId) {
                case "OPN":
                    OPNResponse opn = new OPNResponse();
                    sRtn = opn.makeMessageFromGeneral(obj);
                    break;
                case "CLS":
                    CLSResponse cls = new CLSResponse();
                    sRtn = cls.makeMessageFromGeneral(obj);
                    break;
                case "RBT":
                    RBTResponse rbt = new RBTResponse();
                    sRtn = rbt.makeMessageFromGeneral(obj);
                    break;
                case "SNS":
                    SNSResponse sns = new SNSResponse();
                    sRtn = sns.makeMessageFromGeneral(obj);
                    break;
                case "OEX":
                    OEXResponse oex = new OEXResponse();
                    sRtn = oex.makeMessageFromGeneral(obj);
                    break;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".makeAtmMessage"));
            sendEMS(this.logContext);
        }
        if (StringUtils.isNotBlank(sRtn)) {
            return this.addHeader("303031", sRtn);
        }
        return StringUtils.EMPTY;
    }

    public void sendToATM(String msgId, String data) {
        this.sendToATM(this.channelHandlerContext.channel(), msgId, data);
    }

    private void sendToATM(Channel channel, String msgId, String data) {
        final String methodName = StringUtils.join(ProgramName, ".sendToATM");
        LogHelperFactory.getTraceLogger().trace("Before Send Data to ATM:", data);
        NettyTransmissionUtil.sendHexMessage(this, this.configuration, channel, data);
        this.logContext.setMessageId(msgId);
        this.logContext.setRemark(StringUtils.join("ATMGW Send Data To ATM(NO:", this.atmNo, ",IP:", this.clientIP, ",Port:", this.clientPort, ")"));
        this.logContext.setMessage(data);
        this.logContext.setProgramName(methodName);
        this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayOut);
        this.logContext.setMessageFlowType(MessageFlow.Response);
        logMessage(this.logContext);
    }

    // 2022-08-23 Richard mark start
    // 這段合庫不會有, 點掉, 合庫版, ATMGW只檢查IP by Ashiang
    // private FEPReturnCode checkMacAndMakeMac(Integer ej, String rcvData,
    // RefString refReplyData) {
    // final String msgId = "ATMGW";
    // String xmt = PolyfillUtil.toString(ej, "0000000000").substring(4);
    // // 收到2A
    // if (RegexUtil.find(GatewayCodeConstant.FromATMMacDataPattern, rcvData)) {
    // this.hasCheckSecurity = true;
    // // 2011-04-08 by kyo for enchelper改介面for FEPLOG紀錄
    // ENCHelper enc = new ENCHelper(msgId, FEPChannel.ATM, SubSystem.ATMP, ej,
    // this.atmNo, StringUtils.EMPTY, null);
    // String mac = rcvData.substring(24, 16 + 24);
    // String rnd = rcvData.substring(40, 16 + 40);
    // FEPReturnCode rtn = enc.checkAtmSessionMac(this.atmNo, mac, rnd);
    // if (rtn == FEPReturnCode.Normal) {
    // // 檢查成功,產生一組新的MAC KEY送下去
    // RefString refNewMac = new RefString(StringUtils.EMPTY);
    // FEPReturnCode rtn2 = enc.makeAtmSessionMac(this.atmNo, refNewMac);
    // LogHelperFactory.getTraceLogger().trace("ATMGW Check Mac Successfully and Get
    // New MacKey:", refNewMac.get());
    // if (rtn2 == FEPReturnCode.Normal) {
    // refReplyData.set(MessageFormat.format(GatewayCodeConstant.ToATMCorrectMacData,
    // xmt, refNewMac.get()));
    // this.needCheckMac = false;
    // this.encCheckErrorCount = 0;
    // LogHelperFactory.getTraceLogger().trace(StringUtils.join("ATMGW Pass ATM
    // Security Check(NO:", this.atmNo, ",IP:", this.clientIP, ",Port:",
    // this.clientPort, ")"));
    // return FEPReturnCode.Normal;
    // }
    // return rtn2;
    // } else {
    // // 檢查失敗,也是要產生一組新的MAC KEY送下去
    // RefString refNewMac = new RefString(StringUtils.EMPTY);
    // FEPReturnCode rtn2 = enc.makeAtmSessionMac(this.atmNo, refNewMac);
    // LogHelperFactory.getTraceLogger().trace("ATMGW Check Mac Failed and Get New
    // MacKey:", refNewMac.get());
    // if (rtn2 == FEPReturnCode.Normal) {
    // refReplyData.set(MessageFormat.format(GatewayCodeConstant.ToATMWrongMacData,
    // xmt, refNewMac.get()));
    // }
    // return rtn;
    // }
    // }
    // // 收到ChangeFirst Response
    // else if (RegexUtil.find(GatewayCodeConstant.FromATMChangeFirstPattern,
    // rcvData)) {
    // this.initKey = InitKeyStatus.Done;
    // // 2011-04-08 by kyo for enchelper改介面for FEPLOG紀錄
    // ENCHelper enc = new ENCHelper(msgId, FEPChannel.ATM, SubSystem.ATMP, ej,
    // this.atmNo, StringUtils.EMPTY, null);
    // RefString refCdkey = new RefString(StringUtils.EMPTY);
    // RefString refMackey = new RefString(StringUtils.EMPTY);
    // FEPReturnCode rtn = enc.makeAtmSessionKey(this.atmNo, refCdkey, refMackey);
    // if (rtn == FEPReturnCode.Normal) {
    // refReplyData.set(MessageFormat.format(GatewayCodeConstant.ToATMChangeFirstKey,
    // xmt, StringUtils.join(refCdkey.get(), refMackey.get())));
    // } else {
    // this.initKey = InitKeyStatus.Initial;
    // }
    // return rtn;
    // }
    // // 收到ChangeKey Response
    // else if (RegexUtil.find(GatewayCodeConstant.FromATMChangeKeyPattern,
    // rcvData)) {
    // LogHelperFactory.getTraceLogger().trace("ATMGW Receive 1B0F Command");
    // // 2011-04-08 by kyo for enchelper改介面for FEPLOG紀錄
    // ENCHelper enc = new ENCHelper(msgId, FEPChannel.ATM, SubSystem.ATMP, ej,
    // this.atmNo, StringUtils.EMPTY, null);
    // RefString refNewMac = new RefString(StringUtils.EMPTY);
    // FEPReturnCode rtn = enc.makeAtmSessionMac(this.atmNo, refNewMac);
    // if (rtn == FEPReturnCode.Normal) {
    // refReplyData.set(MessageFormat.format(GatewayCodeConstant.ToATMNewMacKey,
    // xmt, refNewMac.get()));
    // this.initKey = InitKeyStatus.Done;
    // }
    // return rtn;
    // }
    // // ATM ENC Error: 3A
    // else if (RegexUtil.find(GatewayCodeConstant.FromATMENCErrorPattern, rcvData))
    // {
    // LogHelperFactory.getTraceLogger().trace("ATMGW Receive 3A0F Command");
    // return ENCReturnCode.ENCLibError;
    // }
    // return ENCReturnCode.ENCArgumentError;
    // }
    // 2022-08-23 Richard mark end

    private String makeAtmResponseFromRequest(String req, String errCode) {
        String tita = req.substring(24);
        StringBuilder sb = new StringBuilder();
        // 從tita搬欄位組tota
        sb.append(tita.substring(4, 6 + 4)); // TXCD
        sb.append(tita.substring(84, 16 + 84)); // DATE
        sb.append(StringUtil.toHex(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN))); // TIME
        sb.append(tita.substring(28, 10 + 28)); // ATMNO
        sb.append(tita.substring(64, 2 + 64)); // MODE
        sb.append(tita.substring(66, 16 + 66)); // DD
        sb.append(tita.substring(82, 2 + 82)); // DEPMODE
        sb.append(tita.substring(84, 16 + 84)); // ATMSEQ_O1
        sb.append(tita.substring(100, 16 + 100)); // ATMSEQ_O2
        sb.append(StringUtil.toHex(errCode));
        String body = sb.toString();
        return this.addHeader(req.substring(14, 6 + 14), body);
    }

    private String addHeader(String xmt, String body) {
        StringBuilder sb = new StringBuilder();
        // 第0 – 2 BYTE ==Hex(0F),Hex(0F),Hex(0F)
        sb.append("0F0F0F");
        // 第3 – 5 BYTE長度
        sb.append(StringUtils.leftPad(String.valueOf((body.length() / 2 + 12)), 6, '0'));
        // 第6 BYTE 本筆資料中共含有幾筆TITA/TOTA,固定塞01
        sb.append("01");
        // 第7 – 9 BYTE XMTNO 從零起編
        sb.append(xmt);
        // 第10 BYTE資料型態,0F 為DATA TITA,TOTA
        sb.append("0F");
        // 第 11 BYTE HEX 0F
        sb.append("0F");
        sb.append(body);
        return sb.toString();
    }

    private int getTimeout() {
        return gatewayUtil.getTimeout(this.configuration);
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }

    public String getAtmZone() {
        return atmZone;
    }

    public void setAtmZone(String atmZone) {
        this.atmZone = atmZone;
    }

    public boolean isClearAll() {
        return clearAll;
    }

    public void setClearAll(boolean clearAll) {
        this.clearAll = clearAll;
    }

    public boolean isNeedCheckMac() {
        return needCheckMac;
    }

    public void setNeedCheckMac(boolean needCheckMac) {
        this.needCheckMac = needCheckMac;
    }

    public boolean isByPassIP() {
        return byPassIP;
    }

    public void setByPassIP(boolean byPassIP) {
        this.byPassIP = byPassIP;
    }

    public boolean isDisableService() {
        return disableService;
    }

    public void setDisableService(boolean disableService) {
        this.disableService = disableService;
    }

    public InitKeyStatus getInitKey() {
        return initKey;
    }

    public void setInitKey(InitKeyStatus initKey) {
        this.initKey = initKey;
    }

    public SocketStatus getSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(SocketStatus socketStatus) {
        this.socketStatus = socketStatus;
    }

    public boolean isNeedUpdateAtmstat() {
        return needUpdateAtmstat;
    }

    public void setNeedUpdateAtmstat(boolean needUpdateAtmstat) {
        this.needUpdateAtmstat = needUpdateAtmstat;
    }

    public short getEncCheckErrorCount() {
        return encCheckErrorCount;
    }

    public void setEncCheckErrorCount(short encCheckErrorCount) {
        this.encCheckErrorCount = encCheckErrorCount;
    }

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    public short getAtmFepConnection() {
        return atmFepConnection;
    }

    public void setAtmFepConnection(short atmFepConnection) {
        this.atmFepConnection = atmFepConnection;
    }

    public String getAtmstatApVersionN() {
        return atmstatApVersionN;
    }

    public void setAtmstatApVersionN(String atmstatApVersionN) {
        this.atmstatApVersionN = atmstatApVersionN;
    }
}
