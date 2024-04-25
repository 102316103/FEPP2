package com.system.dump.ldapmain;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContexts;

public class MySSLSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory socketFactory;

    public MySSLSocketFactory() {
        try {
	        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null);//Make an empty store
	        InputStream fis = new FileInputStream("./fepd.cer");
	        BufferedInputStream bis = new BufferedInputStream(fis);

	        CertificateFactory cf = CertificateFactory.getInstance("X.509");

	        while (bis.available() > 0) {
	            Certificate cert = cf.generateCertificate(bis);
	            trustStore.setCertificateEntry("fiddler"+bis.available(), cert);
	        }
	        
	        SSLContext sslcontext = SSLContexts.custom()
	                .loadTrustMaterial(new TrustStrategy() {
	                	@Override
	                    public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) {
	                        return true;
	                    }
	                })
	                .loadKeyMaterial(trustStore, "changeit".toCharArray())
	                .build();       
            socketFactory = sslcontext.getSocketFactory();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            /* handle exception */
        }
    }

    public static SocketFactory getDefault() {
        return new MySSLSocketFactory();
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
