package com.syscom.fep.ws.client;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.ws.client.entity.WsClientType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public abstract class WsGatewayClient<WsRequest, WsResponse, MessageOut, MessageIn> extends WebServiceGatewaySupport implements WsClient<WsRequest, WsResponse, MessageOut, MessageIn> {
    private final String uri;

    public WsGatewayClient(String uri) {
        this.uri = uri;
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(this.getWsdlPackage());
        this.setDefaultUri(this.getURI());
        this.setMarshaller(marshaller);
        this.setUnmarshaller(marshaller);
    }

    /**
     * 發送請求電文, 接收回應電文
     *
     * @param messageOut
     * @return
     * @throws Exception
     */
    @Override
    @SuppressWarnings("unchecked")
    public MessageIn sendReceive(MessageOut messageOut) throws Exception {
        boolean isLogEnable = this.isLogEnable();
        WsClientType wsClientType = this.getWsClientType();
        String uri = this.getURI();
        try {
            if (isLogEnable) logHelper.info("[", wsClientType, "][", uri, "]", Const.MESSAGE_OUT, this.outToString(messageOut));
            WsRequest request = this.createWsRequest(messageOut);
            WsResponse response = (WsResponse) this.getWebServiceTemplate().marshalSendAndReceive(uri, request, new SoapActionCallback(this.getSoapActionCallbackURI()));
            MessageIn messageIn = this.transferToMessageIn(response);
            if (isLogEnable) logHelper.info("[", wsClientType, "][", uri, "]", Const.MESSAGE_IN, this.inToString(messageIn));
            return messageIn;
        } catch (Exception e) {
            logHelper.exceptionMsg(e, "[", wsClientType, "][", uri, "]sendReceive failed, message = [", e.getMessage(), "]");
            throw e;
        }
    }

    /**
     * 取得服務URL
     *
     * @return
     */
    protected String getURI() {
        return this.uri;
    }

    /**
     * 取得SoapActionCallBackURI, 從WSDL中獲取
     *
     * @return
     */
    protected abstract String getSoapActionCallbackURI();

    /**
     * 產出的Client端程式所在的package
     *
     * @return
     */
    protected abstract String getWsdlPackage();
}
