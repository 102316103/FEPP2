package com.syscom.fep.invoker;

import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.invoker.netty.impl.*;
import com.syscom.fep.vo.communication.BaseCommu;
import com.syscom.fep.vo.communication.ToFEPFISCCommu;
import com.syscom.fep.vo.communication.ToFISCCommu;
import com.syscom.fep.vo.enums.ClientType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class SimpleNettyClientFactory {
    /**
     * 建立NettyClient發送訊息
     *
     * @param <Request>
     * @param clientType
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public <Request> String sendReceive(ClientType clientType, Request request, int timeout) throws Exception {
        try {
            switch (clientType) {
                case TO_FISC_GW:
                    return sendReceiveToFISCGW((ToFISCCommu) request, timeout);
                case TO_FEP_FISC:
                    return sendReceiveToFEPFISC((BaseCommu) request, timeout);
                case TO_FEP_ATM:
                    return sendReceiveToFEPATM((BaseCommu) request, timeout);
                default:
                    throw ExceptionUtil.createIllegalArgumentException("Unsupported ClientType = [", clientType.name(), "]");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 丟訊息給FISC GW並sync/wait接收回應
     *
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    private String sendReceiveToFISCGW(ToFISCCommu request, int timeout) throws Exception {
        // 改為短連接
        ToFISCNettyClientConfiguration source = SpringBeanFactoryUtil.registerBean(ToFISCNettyClientConfiguration.class);
        // 這裡要new一個configuration, 將不同的host和port塞入
        ToFISCNettyClientConfiguration target = new ToFISCNettyClientConfiguration();
        BeanUtils.copyProperties(source, target);
        target.setHost(request.getHost());
        target.setPort(request.getPort());
        target.printConfiguration();
        ToFISCNettyClient toFISCNettyClient = SpringBeanFactoryUtil.registerBean(ToFISCNettyClient.class);
        ToFEPFISCCommu toFEPFISCCommu = toFISCNettyClient.establishConnectionAndSendReceive(target, request, timeout);
        return toFEPFISCCommu == null ? StringUtils.EMPTY : toFEPFISCCommu.getMessage();
    }

    /**
     * 丟訊息給FEP FISC並sync/wait接收回應
     *
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    private String sendReceiveToFEPFISC(BaseCommu request, int timeout) throws Exception {
        // 改為短連接
        SpringBeanFactoryUtil.registerBean(ToFEPFISCNettyClientConfiguration.class);
        ToFEPFISCNettyClient toFEPFISCNettyClient = SpringBeanFactoryUtil.registerBean(ToFEPFISCNettyClient.class);
        return toFEPFISCNettyClient.establishConnectionAndSendReceive(request, timeout);
    }

    /**
     * 丟訊息給FEP ATM並sync/wait接收回應
     *
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    private String sendReceiveToFEPATM(BaseCommu request, int timeout) throws Exception {
        // 改為短連接
        SpringBeanFactoryUtil.registerBean(ToFEPATMNettyClientConfiguration.class);
        ToFEPATMNettyClient toFEPATMNettyClient = SpringBeanFactoryUtil.registerBean(ToFEPATMNettyClient.class);
        return toFEPATMNettyClient.establishConnectionAndSendReceive(request, timeout);
    }
}
