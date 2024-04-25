package com.syscom.fep.frmcommon.net.http;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpClientConfiguration {
    private HttpClientConfiguration() {
    }

    public static SimpleClientHttpRequestFactory createSimpleClientHttpRequestFactory(int timeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        return requestFactory;
    }

    public static HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(int timeout) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null,
                        (x509Certificates, authType) -> true).build();
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(connectionSocketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        return requestFactory;
    }

    public static HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(int timeout, InputStream ssl, String store, String type) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(StringUtils.isBlank(type) ? KeyStore.getDefaultType() : type);
        trustStore.load(ssl, store.toCharArray());
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(trustStore,
                        (x509Certificates, authType) -> true).build();
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(connectionSocketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }
}
