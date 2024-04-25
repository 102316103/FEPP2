package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.ssl.SslKeyTrust;
import com.syscom.fep.frmcommon.ssl.SslKeyTrustType;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.gateway.entity.AtmStatus;
import com.syscom.fep.gateway.netty.*;
import com.syscom.fep.gateway.netty.atm.ctrl.ATMGatewayServerRestfulCtrl;
import com.syscom.fep.gateway.util.GatewayCommuHelper;
import com.syscom.fep.gateway.util.GatewayUtil;
import com.syscom.fep.vo.communication.ToATMCommuAtmstatList;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.util.*;

@StackTracePointCut(caller = SvrConst.SVR_ATM_GATEWAY)
public class ATMGatewayServer extends
        NettyTransmissionServer<ATMGatewayServerConfiguration, ATMGatewayServerChannelInboundHandlerAdapter, ATMGatewayServerRuleIpFilter, ATMGatewayServerProcessRequestManager, ATMGatewayServerProcessRequest> {
    @Autowired
    private GatewayCommuHelper commuHelper;
    @Autowired
    private GatewayUtil gatewayUtil;
    @Autowired
    private SslContextFactory sslContextFactory;
    @Autowired
    private SpringConfigurationUtil springConfigurationUtil;
    private Integer sslNumber = new Integer(0);
    private final List<X509KeyManager> keyManagerList = Collections.synchronizedList(new ArrayList<>());
    private final List<X509TrustManager> trustManagerList = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> clientIpToKeyAliasMap = Collections.synchronizedMap(new HashMap<>());

    @Override
    protected void initData() {
        super.initData();
// 2022-08-24 Richard mark start
// GW不能直接連DB, 所以改為從透過ATMService取資料
//		this.configuration.setBacklog(CMNConfig.getInstance().getListenBacklog());
//		this.configuration.setKeepAliveTime(CMNConfig.getInstance().getKeepAliveTime() * 1000);
//		this.configuration.setKeepAliveInterval(CMNConfig.getInstance().getKeepAliveInterval() * 1000);
//		this.configuration.setTimeout(GWConfig.getInstance().getAATimeout());
// 2022-08-24 Richard mark end
// 2023-01-03 Richard mark start
// 這幾項配置改為在配置檔中設定
//        ToGWCommuConfig toGWCommuConfig =
//                dbHelper.getConfigFromFEPATM(ToFEPCommuConfig.ConfigType.CMN.getValue() + ToFEPCommuConfig.ConfigType.GW.getValue(),
//                        gatewayUtil.getTimeout(this.configuration));
//        this.configuration.setBacklog(toGWCommuConfig.getCmn().getListenBacklog());
//        this.configuration.setKeepAliveTime(toGWCommuConfig.getCmn().getKeepAliveTime() * 1000);
//        this.configuration.setKeepAliveInterval(toGWCommuConfig.getCmn().getKeepAliveInterval() * 1000);
//        this.configuration.setTimeout(toGWCommuConfig.getGw().getAaTimeout());
// 2023-01-03 Richard mark end
        this.setReestablishConnectionAfterTerminateConnection(true);
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state) {
        super.connStateChanged(channel, state);
        if (state == NettyTransmissionConnState.SERVER_BOUND) {
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.ATM);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setRemark(StringUtils.join("ATMGW Begin Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayIn);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
            // registerController
            SpringBeanFactoryUtil.registerController(ATMGatewayServerRestfulCtrl.class);
            // registerBean
            SpringBeanFactoryUtil.registerBean(ATMGatewayServerBlackListHandler.class);
            SpringBeanFactoryUtil.registerBean(ATMGatewayServerClientIpToCertNoHandler.class);
        } else if (state == NettyTransmissionConnState.SERVER_SHUTTING_DOWN) {
            requestManager.handleAllProcessRequest(processRequest -> {
                processRequest.setClearAll(true);
                processRequest.closeConnection();
            });
            requestManager.clearAllProcessRequest();
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setRemark(StringUtils.join("ATMGW Stop Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayIn);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
            // unregisterController
            SpringBeanFactoryUtil.unregisterController(ATMGatewayServerRestfulCtrl.class);
            // unregisterBean
            SpringBeanFactoryUtil.unregisterBean(ATMGatewayServerBlackListHandler.class);
            SpringBeanFactoryUtil.unregisterBean(ATMGatewayServerClientIpToCertNoHandler.class);
        }
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state);
    }

    @Override
    protected SslHandler getSslHandler(LogData logData, SocketChannel ch) throws Exception {
        String clientIp = ReflectUtil.envokeMethod(ch.remoteAddress().getAddress(), "getHostAddress", StringUtils.EMPTY);
        loadSsl(false, true);
        loadSslAlias(clientIp, ch);
        ATMGatewayServerClientIpToCertNoHandler handler = SpringBeanFactoryUtil.getBean(ATMGatewayServerClientIpToCertNoHandler.class);
        // 如果alias沒有取到, 則會丟異常拒絕所有ATM連線
        String alias = handler.getCertNo(logData, clientIp);
        return NettyTransmissionUtil.getSsHandler(keyManagerList, alias, trustManagerList, configuration.isSslNeedClientAuth(), false); // 服務端認證方式
    }

    public void loadSsl(boolean clear, boolean throwException) throws Exception {
        if (clear) {
            this.keyManagerList.clear();
            this.trustManagerList.clear();
        }
        try {
            if (this.keyManagerList.isEmpty())
                this.keyManagerList.addAll(sslContextFactory.getKeyManagerList(configuration.getSslConfigs()));
            if (this.trustManagerList.isEmpty())
                this.trustManagerList.addAll(sslContextFactory.getTrustManagerList(configuration.getSslConfigs()));
        } catch (Exception e) {
            if (throwException)
                throw e;
        }
    }

    private void loadSslAlias(String clientIp, SocketChannel ch) {
        if (this.keyManagerList.isEmpty()) {
            return;
        }
        // 2023-07-31 Richard modified 不需要從主檔抓ATM_CERTALIAS
        // String alias = this.clientIpToKeyAliasMap.get(clientIp);
        // ToATMCommuAtmmstr atmmstr = this.getAtmmstr(clientIp);
        // if (atmmstr != null) {
        //     this.clientIpToKeyAliasMap.put(clientIp, atmmstr.getAtmCertAlias());
        //     if (StringUtils.isBlank(alias) || !alias.equals(atmmstr.getAtmCertAlias())) {
        //         NettyTransmissionUtil.infoMessage(ch, "Cert Alias has been changed, atmIp = [", clientIp, "], from old alias = [", alias, "] to new alias = [", atmmstr.getAtmCertAlias(), "]");
        //     }
        // }
    }

    public SslKeyTrust sslKeySwitch(Integer index) throws Exception {
        if (CollectionUtils.isEmpty(configuration.getSslConfigs()))
            throw ExceptionUtil.createException("Cannot switch or change cause empty SSL Certificate list!!!");
        if (index == null) {
            synchronized (sslNumber) {
                sslNumber++;
                if (sslNumber == configuration.getSslConfigs().size())
                    sslNumber = 0;
                LogHelperFactory.getGeneralLogger().info("SSL Certificate has been switched, sslNumber = [", sslNumber, "]");
                return configuration.getSslConfigs().get(sslNumber);
            }
        } else if (index < 0 || index >= configuration.getSslConfigs().size()) {
            throw ExceptionUtil.createIllegalArgumentException("index must between 0 and ", configuration.getSslConfigs().size() - 1);
        } else {
            synchronized (sslNumber) {
                sslNumber = index;
                LogHelperFactory.getGeneralLogger().info("SSL Certificate has been switched, sslNumber = [", sslNumber, "]");
                SslKeyTrust sslKeyTrust = configuration.getSslConfigs().get(sslNumber);
                return sslKeyTrust;
            }
        }
    }

    public SslKeyTrust sslKeyRemove(Integer index) throws Exception {
        if (CollectionUtils.isEmpty(configuration.getSslConfigs()))
            throw ExceptionUtil.createException("Cannot remove cause empty SSL Certificate list!!!");
        if (index == null || index < 0 || index >= configuration.getSslConfigs().size()) {
            throw ExceptionUtil.createIllegalArgumentException("index must between 0 and ", configuration.getSslConfigs().size() - 1);
        } else {
            boolean removed = false;
            try {
                synchronized (sslNumber) {
                    // if (sslNumber.equals(index)) {
                    //      throw ExceptionUtil.createIllegalArgumentException("Cannot remove current SSL Certificate, sslNumber = [", sslNumber, "]");
                    // }
                    LogHelperFactory.getGeneralLogger().info("SSL Certificate has been removed, index = [", index, "]");
                    SslKeyTrust sslKeyTrust = configuration.getSslConfigs().remove(index.intValue());
                    configuration.setSslConfigsIndex();
                    sslNumber--;
                    if (sslNumber < 0) {
                        sslNumber = 0;
                    }
                    removed = true;
                    return sslKeyTrust;
                }
            } finally {
                try {
                    // 重新載入ssl
                    loadSsl(true, false);
                    // 記得更新配置檔
                    storeSslConfigs(removed);
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        }
    }

    public String sslKeyAdd(String filename, String sscode, SslKeyTrustType sslKeyTrustType, Boolean needClientAuth) throws Exception {
        if (StringUtils.isBlank(filename)) {
            throw ExceptionUtil.createIllegalArgumentException("Filename cannot be blank");
        } else {
            if (configuration.getSslConfigs() == null) {
                configuration.setSslConfigs(new ArrayList<>());
            }
            boolean added = false;
            try {
                SslKeyTrust sslKeyTrust = new SslKeyTrust();
                sslKeyTrust.setIndex(configuration.getSslConfigs().size());
                sslKeyTrust.setSslKeyPath(filename);
                sslKeyTrust.setSslKeySscode(sscode);
                sslKeyTrust.setSslKeyType(sslKeyTrustType);
                sslKeyTrust.setSslTrustPath(filename);
                sslKeyTrust.setSslTrustSscode(sscode);
                sslKeyTrust.setSslTrustType(sslKeyTrustType == null ? SslKeyTrustType.PKCS12 : sslKeyTrustType);
                configuration.setSslNeedClientAuth(needClientAuth == null || needClientAuth);
                String certificate = sslContextFactory.getCertificate(sslKeyTrust);
                if (StringUtils.isNotBlank(certificate)) {
                    configuration.getSslConfigs().add(sslKeyTrust);
                    LogHelperFactory.getGeneralLogger().info("SSL Certificate has been add, index = [", sslKeyTrust.getIndex(), "], total = [", configuration.getSslConfigs().size(), "]\r\n", certificate);
                    added = true;
                    return certificate;
                } else {
                    throw ExceptionUtil.createException("Cannot read SSL Certificate, filename = [", filename, "], maybe incorrect password.");
                }
            } finally {
                try {
                    // 重新載入ssl
                    loadSsl(true, false);
                    // 記得更新配置檔
                    storeSslConfigs(added);
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        }
    }

    public String sslAlias(String action, String atmIp, String alias, String alias2, String alias3) throws Exception {
        sslKeySwitch(2);
        return "";
    }

    public String sslAlias(String action, String atmIp, String alias) throws Exception {
        if ("set".equalsIgnoreCase(action) && (StringUtils.isBlank(atmIp) || StringUtils.isBlank(alias))) {
            throw ExceptionUtil.createIllegalArgumentException("Both AtmIp and Alias cannot be empty!!");
        } else if (("remove".equalsIgnoreCase(action) || "get".equalsIgnoreCase(action))
                && !this.clientIpToKeyAliasMap.containsKey(atmIp)) {
            throw ExceptionUtil.createIllegalArgumentException("ATM IP not exist, atmIp = \"", atmIp, "\"!!");
        }
        if ("set".equalsIgnoreCase(action)) {
            this.clientIpToKeyAliasMap.put(atmIp, alias);
            return StringUtils.join("alias \"", alias, "\" has been set for atmIp \"", atmIp, "\"\r\n");
        } else if ("remove".equalsIgnoreCase(action)) {
            String value = this.clientIpToKeyAliasMap.remove(atmIp);
            return StringUtils.join("alias \"", value, "\" has been removed for atmIp \"", atmIp, "\"\r\n");
        } else if ("get".equalsIgnoreCase(action)) {
            String value = this.clientIpToKeyAliasMap.get(atmIp);
            return StringUtils.join("alias \"", value, "\" was mapped for atmIp \"", atmIp, "\"\r\n");
        } else if ("list".equalsIgnoreCase(action)) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : this.clientIpToKeyAliasMap.entrySet()) {
                sb.append("atmIp:").append(entry.getKey()).append("\t\t").append("alias:").append(entry.getValue()).append("\r\n");
            }
            return sb.toString();
        }
        throw ExceptionUtil.createIllegalArgumentException("Invalid action, must be \"set\" or \"remove\" or \"get\"!!");
    }

    /**
     * 將ssl憑證設定更新到指定的配置檔中
     *
     * @param store
     * @throws Exception
     */
    private void storeSslConfigs(boolean store) throws Exception {
        if (store) {
            try {
                Resource resource = new ClassPathResource(CleanPathUtil.cleanString(Const.PROP_FILENAME_ATMGW_SSL));
                File file = resource.getFile();
                // 如果更新配置檔成功, 則刷新配置
                if (configuration.storeSslConfigs(file)) {
                    springConfigurationUtil.refreshManually();
                    super.printConfiguration();
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public ToATMCommuAtmstatList getAtmstatList(AtmStatus atmStatus) throws Exception {
        return this.commuHelper.getAtmstatList(this.logContext, atmStatus);
    }

    /**
     * 拒絕Client連入, 由子類覆寫
     *
     * @param ch
     * @return
     */
    @Override
    protected boolean channelRejected(SocketChannel ch) {
        String clientIp = ReflectUtil.envokeMethod(ch.remoteAddress().getAddress(), "getHostAddress", StringUtils.EMPTY);
        ATMGatewayServerBlackListHandler handler = SpringBeanFactoryUtil.getBean(ATMGatewayServerBlackListHandler.class);
        return handler.exist(clientIp);
    }

    /**
     * 有新的Channel時, 初始化動作
     *
     * @param ch
     */
    @Override
    protected void channelInitialization(SocketChannel ch) {
        // 取出ClientIp, 判斷是否在BypassCheckAtmIp中, 如果有的話, 則不要列印log
        String clientIp = ReflectUtil.envokeMethod(ch.remoteAddress().getAddress(), "getHostAddress", StringUtils.EMPTY);
        if (this.configuration.getBypassCheckAtmIp().indexOf(clientIp) > -1) {
            NettyTransmissionUtil.setChannelLoggingDisable(ch, true);
            NettyTransmissionUtil.infoMessage(ch, "[channelInitialization] Disabled all logging for ATM IP = [", clientIp, "]");
        } else {
            NettyTransmissionUtil.infoMessage(ch, "[channelInitialization] Get ATM IP = [", clientIp, "]");
        }
    }

    /**
     * 處理異常的類
     *
     * @return
     */
    @Override
    protected NettyTransmissionChannelInboundHandlerAdapterServerException<ATMGatewayServerConfiguration, ATMGatewayServerProcessRequestManager, ATMGatewayServerProcessRequest> getExceptionHandlerAdapter() {
        if (this.exceptionHandlerAdapter == null) {
            this.exceptionHandlerAdapter = new ATMGatewayServerChannelInboundHandlerAdapterException(this.configuration, this.requestManager);
        }
        return this.exceptionHandlerAdapter;
    }
}
