package com.syscom.fep.frmcommon.ssl;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.IOUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.Builder;
import java.security.KeyStore.PasswordProtection;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Component
public class SslContextFactory {
    private final LogHelper logger = new LogHelper();
    private static final String PROTOCOL = "TLSv1.2";

    static {
        // System.setProperty("javax.net.debug", "ssl,handshake,record");
    }

    private SslContextFactory() {
    }

    public SSLEngine getSSLEngine(SslKeyTrust sslKeyTrust, boolean needClientAuth, boolean wantClientAuth, boolean useClientMode) throws Exception {
        if (verify(sslKeyTrust)) {
            SSLContext sslContext = this.getSSLContext(sslKeyTrust);
            if (sslContext != null) {
                SSLEngine sslEngine = sslContext.createSSLEngine();
                sslEngine.setNeedClientAuth(needClientAuth);
                sslEngine.setWantClientAuth(wantClientAuth);
                sslEngine.setUseClientMode(useClientMode);
                return sslEngine;
            }
        }
        return null;
    }

    public String getCertificate(SslKeyTrust sslKeyTrust) throws Exception {
        try (InputStream inPk = this.getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode());
             InputStream inTrust = this.getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
            String infoTrust = getCertificate(inTrust, FilenameUtils.getName(CleanPathUtil.cleanString(sslKeyTrust.getSslTrustPath())),
                    sslKeyTrust.getIndex(), sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType());
            if (StringUtils.isNotBlank(infoTrust)) {
                return infoTrust;
            }
            String infoPk = getCertificate(inPk, FilenameUtils.getName(CleanPathUtil.cleanString(sslKeyTrust.getSslKeyPath())),
                    sslKeyTrust.getIndex(), sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType());
            return infoPk;
        } catch (Exception e) {
            throw e;
        }
    }

    public String getCertificate(InputStream in, String filename, int index, String store, SslKeyTrustType type) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            List<CertificateInformation> list = getCertificateInformationList(in, filename, index, store, type);
            for (CertificateInformation information : list) {
                sb.append("========================================以下憑證序號[").append(index).append("]========================================\r\n");
                sb.append(information.toString());
                sb.append("========================================以上憑證序號[").append(index).append("]========================================\r\n");
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
        return sb.toString();
    }

    public List<CertificateInformation> getCertificateInformationList(SslKeyTrust sslKeyTrust) throws Exception {
        try (InputStream inPk = this.getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode());
             InputStream inTrust = this.getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
            List<CertificateInformation> infoTrustList = getCertificateInformationList(inTrust, FilenameUtils.getName(CleanPathUtil.cleanString(sslKeyTrust.getSslTrustPath())),
                    sslKeyTrust.getIndex(), sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType());
            if (CollectionUtils.isNotEmpty(infoTrustList)) {
                return infoTrustList;
            }
            List<CertificateInformation> infoPkList = getCertificateInformationList(inPk, FilenameUtils.getName(CleanPathUtil.cleanString(sslKeyTrust.getSslKeyPath())),
                    sslKeyTrust.getIndex(), sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType());
            return infoPkList;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<CertificateInformation> getCertificateInformationList(InputStream in, String filename, int index, String store, SslKeyTrustType type) throws Exception {
        List<CertificateInformation> list = new ArrayList<>();
        try {
            KeyStore ks = this.loadKeyStore(in, store, type);
            if (ks != null) {
                for (Enumeration<String> e = ks.aliases(); e.hasMoreElements(); ) {
                    String alias = e.nextElement();
                    list.add(new CertificateInformation(filename, alias, (X509Certificate) ks.getCertificate(alias)));
                }
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
        return list;
    }

    public X509Certificate[] getX509TrustCertificates(List<SslKeyTrust> sslKeyTrustList) throws Exception {
        List<X509Certificate> certificateList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = this.getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
                    KeyStore ts = this.loadKeyStore(in, sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType());
                    if (ts != null) {
                        for (Enumeration<String> e = ts.aliases(); e.hasMoreElements(); ) {
                            String alias = e.nextElement();
                            X509Certificate cert = (X509Certificate) ts.getCertificate(alias);
                            certificateList.add(cert);
                        }
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        X509Certificate[] certificates = new X509Certificate[certificateList.size()];
        certificateList.toArray(certificates);
        return certificates;
    }

    public List<X509KeyManager> getKeyManagerList(List<SslKeyTrust> sslKeyTrustList) throws Exception {
        List<X509KeyManager> x509KeyManagerList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = this.getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode())) {
                    KeyManager[] keyManagers = this.getKeyManagers(in, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType());
                    if (ArrayUtils.isNotEmpty(keyManagers)) {
                        for (KeyManager keyManager : keyManagers) {
                            if (keyManager instanceof X509KeyManager) {
                                x509KeyManagerList.add((X509KeyManager) keyManager);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        return x509KeyManagerList;
    }

    public List<X509TrustManager> getTrustManagerList(List<SslKeyTrust> sslKeyTrustList) throws Exception {
        List<X509TrustManager> x509TrustManagerList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = this.getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
                    TrustManager[] trustManagers = this.getTrustManagers(in, sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType());
                    if (ArrayUtils.isNotEmpty(trustManagers)) {
                        for (TrustManager trustManager : trustManagers) {
                            if (trustManager instanceof X509TrustManager) {
                                x509TrustManagerList.add((X509TrustManager) trustManager);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        return x509TrustManagerList;
    }

    private boolean verify(SslKeyTrust sslKeyTrust) {
        if (sslKeyTrust == null) {
            return false;
        } else if (StringUtils.isBlank(sslKeyTrust.getSslKeyPath()) && StringUtils.isBlank(sslKeyTrust.getSslTrustPath())) {
            return false;
        } else if (StringUtils.isNotBlank(sslKeyTrust.getSslKeyPath()) && sslKeyTrust.getSslKeySscode() == null) {
            return false;
        } else if (StringUtils.isNotBlank(sslKeyTrust.getSslTrustPath()) && sslKeyTrust.getSslTrustSscode() == null) {
            return false;
        }
        return true;
    }

    private SSLContext getSSLContext(SslKeyTrust sslKeyTrust) throws Exception {
        try (InputStream inPk = this.getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode());
             InputStream inTrust = this.getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
            return this.getSSLContext(inPk, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType(), inTrust, sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType());
        } catch (Exception e) {
            throw e;
        }
    }

    private SSLContext getSSLContext(InputStream pk, String pkStore, SslKeyTrustType pkType, InputStream trust, String trustStore, SslKeyTrustType trustType) throws Exception {
        try {
            // key manager factory
            KeyManager[] km = this.getKeyManagers(pk, pkStore, pkType);
            // trust manager factory
            TrustManager[] tm = this.getTrustManagers(trust, trustStore, trustType);
            // Initialize the SSLContext to work with our key managers.
            return this.getSslContext(km, tm);
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
    }

    public SSLContext getSslContext(KeyManager[] km, TrustManager[] tm) throws Exception {
        if (km != null || tm != null) {
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(km, tm, null);
            return sslContext;
        }
        return null;
    }

    public KeyManager[] getKeyManagers(InputStream pk, String pkStore, SslKeyTrustType pkType) throws Exception {
        // keystore
        KeyStore ks = this.loadKeyStore(pk, pkStore, pkType);
        if (ks != null) {
            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, pkStore.toCharArray());
            // key manager factory
            KeyManager[] km = kmf.getKeyManagers();
            return km;
        }
        return null;
    }

    public TrustManager[] getTrustManagers(InputStream trust, String trustStore, SslKeyTrustType trustType) throws Exception {
        // truststore
        KeyStore ts = this.loadKeyStore(trust, trustStore, trustType);
        if (ts != null) {
            // set up trust manager factory to use our trust store
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            // trust manager factory
            TrustManager[] tm = tmf.getTrustManagers();
            return tm;
        }
        return null;
    }

    public KeyStore loadKeyStore(InputStream in, String store, SslKeyTrustType type) throws Exception {
        if (in != null && store != null) {
            KeyStore ks = KeyStore.getInstance(type.name());
            ks.load(in, store.toCharArray());
            return ks;
        }
        return null;
    }


    public InputStream getInputStream(String ssl, String store) throws Exception {
        InputStream in = null;
        if (StringUtils.isNotBlank(ssl) && store != null) {
            in = IOUtil.openInputStream(ssl);
        }
        return in;
    }

    public SSLEngine getSSLEngine(List<SslKeyTrust> sslKeyTrustList, boolean needClientAuth, boolean useClientMode) throws Exception {
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            List<Builder> builderList = new ArrayList<>();
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = this.getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode())) {
                    KeyStore ks = this.loadKeyStore(in, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType());
                    if (ks != null) {
                        builderList.add(Builder.newInstance(ks, new PasswordProtection(sslKeyTrust.getSslKeySscode().toCharArray())));
                    }
                }
            }
            if (!builderList.isEmpty()) {
                ManagerFactoryParameters ksParams = new KeyStoreBuilderParameters(builderList);
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
                kmf.init(ksParams);
                SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
                sslContext.init(kmf.getKeyManagers(), null, null);
                SSLEngine sslEngine = sslContext.createSSLEngine();
                sslEngine.setNeedClientAuth(needClientAuth);
                sslEngine.setUseClientMode(useClientMode);
                return sslEngine;
            }
        }
        return null;
    }
}
