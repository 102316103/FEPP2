package com.syscom.fep.server.common.adapter;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Calendar;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.vo.communication.ToSendQueryAccountCommu;
import com.syscom.fep.vo.enums.RestfulResultCode;

/**
 * 負責傳送至手機轉帳API
 *
 * @author Jaime
 */
public class MobileAdapter extends AdapterBase {

	public MobileAdapter(MessageBase txData) {
		this.txData = txData;
	}

	/**
	 * 送給手機的data
	 */
	private ToSendQueryAccountCommu toSendQueryAccountCommu;

	private MessageBase txData;

	/**
	 * 財金STAN
	 */
	private String stan;

	@Override
	public FEPReturnCode sendReceive() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		InputStream fis = null;
		BufferedInputStream bis = null;
		try {

			this.txData.getLogContext().setMessage(toSendQueryAccountCommu.toString());
			this.txData.getLogContext().setStan(this.stan);
			this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
			this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, "sendMsgToFISC"));
			this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
			this.txData.getLogContext().setRemark(StringUtils.join("MobileAdapter before Send TO Mobile Url:", toSendQueryAccountCommu.getRestfulUrl(), ",Timeout:", this.timeout));
			logMessage(this.txData.getLogContext());

			String url = CMNConfig.getInstance().getMobileQueryUrl() + "/QueryAccountInfo";
			toSendQueryAccountCommu.setRestfulUrl(url);
			toSendQueryAccountCommu.setTimeout((60 + 5) * 1000);

//        	ToSendQueryAccountRestfulClient toFiscClient = new ToSendQueryAccountRestfulClient(toSendQueryAccountCommu.getRestfulUrl());
//            String resultMessage = toFiscClient.sendReceive((ToSendQueryAccountCommu) toSendQueryAccountCommu, this.timeout);

            KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null);//Make an empty store
	        SslContextFactory sslContextFactory = SpringBeanFactoryUtil.getBean(SslContextFactory.class);
            fis = sslContextFactory.getInputStream("TCBCA_IndR.cer", KeyStore.getDefaultType());
//	        InputStream fis = new FileInputStream("./TCBCA_IndR.cer");
	        bis = new BufferedInputStream(fis);

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
	        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
	                sslcontext,
	                new String[]{"TLSv1.2"},
	                null,
	                NoopHostnameVerifier.INSTANCE);
	        CloseableHttpClient client = HttpClients.custom()
	                .setSSLSocketFactory(sslConnectionSocketFactory)
	                .build();

		    HttpPost httpPost = new HttpPost(url);
//			    HttpPost httpPost = new HttpPost("http://www.example.com");

//		    String json = "{\"Idno\":\"" + toSendQueryAccountCommu.getIdNo() + "\",\"MobilePhone\":\""+ toSendQueryAccountCommu.getMobilePhone() + "\",\"BankCode\":\""+ toSendQueryAccountCommu.getBankCode().trim() + "\"}";
		    StringEntity entity = new StringEntity("{\"Idno\":\"" + toSendQueryAccountCommu.getIdNo() + "\",\"MobilePhone\":\""+ toSendQueryAccountCommu.getMobilePhone() + "\",\"BankCode\":\""+ toSendQueryAccountCommu.getBankCode().trim() + "\"}");
		    httpPost.setEntity(entity);
		    httpPost.setHeader("Accept", "application/json");
		    httpPost.setHeader("Content-type", "application/json");
		    httpPost.setHeader("X-SourceId", "ATM");
		    httpPost.setHeader("X-TxnDttm", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));

		    CloseableHttpResponse response = client.execute(httpPost);
		    try {
                HttpEntity httpentity = response.getEntity();
//                System.out.println(response.getStatusLine());
//                System.out.println("response ContentType:"+httpentity.getContentType());
//                System.out.println("response Content:"+IOUtils.toString(httpentity.getContent()));
//                System.out.println("response AllHeaders:"+response.getAllHeaders());
    			this.txData.getLogContext().setMessage(toSendQueryAccountCommu.getRestfulUrl() + ",response AllHeaders:"+response.getAllHeaders());
    			this.txData.getLogContext().setStan(this.stan);
    			this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, "sendMsgToFISC"));
    			this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
    			this.txData.getLogContext().setMessageFlowType(MessageFlow.Response);
    			this.txData.getLogContext().setRemark(StringUtils.join("MobileAdapter Receive Msg :", IOUtils.toString(httpentity.getContent())));
    			logMessage(this.txData.getLogContext());

            } finally {
            	client.close();
                response.close();
            }

			return rtnCode;
		} catch (Exception e) {
			if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
				rtnCode = FEPReturnCode.FISCGWATMSendError;
			} else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
				rtnCode = FEPReturnCode.FISCTimeout;
			} else {
				rtnCode = CommonReturnCode.ProgramException;
			}
			this.txData.getLogContext().setProgramException(e);
			this.txData.getLogContext().setReturnCode(rtnCode);
			sendEMS(this.txData.getLogContext());
			return rtnCode;
		}finally {
			if(fis != null) {
				try {
					fis.close();
				}catch (Exception e) {
					this.txData.getLogContext().setProgramException(e);
					this.txData.getLogContext().setReturnCode(rtnCode);
				}
			}
			if(bis != null) {
				try {
					bis.close();
				}catch (Exception e) {
					this.txData.getLogContext().setProgramException(e);
					this.txData.getLogContext().setReturnCode(rtnCode);
				}
			}
        }
	}

	public ToSendQueryAccountCommu getToSendQueryAccountCommu() {
		return toSendQueryAccountCommu;
	}

	public void setToSendQueryAccountCommu(ToSendQueryAccountCommu toSendQueryAccountCommu) {
		this.toSendQueryAccountCommu = toSendQueryAccountCommu;
	}

	public MessageBase getTxData() {
		return txData;
	}

	public void setTxData(MessageBase txData) {
		this.txData = txData;
	}

}
