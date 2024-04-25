package com.syscom.fep.web.ldap;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.web.configurer.WebConfiguration;

import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class MySSLSocketFactory extends SSLSocketFactory {
    private static SSLSocketFactory socketFactory;
    private static MySSLSocketFactory instance = new MySSLSocketFactory();

    static {
        BufferedInputStream bis = null;
        InputStream fis = null;
        try {
        	String fileName = WebConfiguration.getInstance().getFileName();
            String filesscode = WebConfiguration.getInstance().getFilesscode();
            String certificateName = WebConfiguration.getInstance().getCertificateName();
            String fiddler = WebConfiguration.getInstance().getFiddler();
            
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null); // Make an empty store
            SslContextFactory sslContextFactory = SpringBeanFactoryUtil.getBean(SslContextFactory.class);
            fis = sslContextFactory.getInputStream(fileName, filesscode);
            bis = new BufferedInputStream(fis);
            CertificateFactory cf = CertificateFactory.getInstance(certificateName);
            while (bis.available() > 0) {
                Certificate cert = cf.generateCertificate(bis);
                trustStore.setCertificateEntry(fiddler + bis.available(), cert);
            }
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
                        @Override
                        public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            return true;
                        }
                    })
                    .loadKeyMaterial(trustStore, filesscode.toCharArray())
                    .build();
            socketFactory = sslcontext.getSocketFactory();
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().error(e, "MySSLSocketFactory init failed!!!");
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                	LogHelperFactory.getTraceLogger().error(e, "MySSLSocketFactory init failed!!!");
                }
            }
            if (fis != null) {
                try {
                	fis.close();
                } catch (IOException e) {
                	LogHelperFactory.getTraceLogger().error(e, "MySSLSocketFactory init failed!!!");
                }
            }
        }
    }

    public static SocketFactory getDefault() {
        return instance;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return socketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String string, int i, boolean bln) throws IOException {
        return socketFactory.createSocket(socket, string, i, bln);
    }

    @Override
    public Socket createSocket(String string, int i) throws IOException, UnknownHostException {
        return socketFactory.createSocket(string, i);
    }

    @Override
    public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException {
        return socketFactory.createSocket(string, i, ia, i1);
    }

    @Override
    public Socket createSocket(InetAddress ia, int i) throws IOException {
        return socketFactory.createSocket(ia, i);
    }

    @Override
    public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
        return socketFactory.createSocket(ia, i, ia1, i1);
    }

    @Override
    public Socket createSocket() throws IOException {
        return socketFactory.createSocket();
    }
}
