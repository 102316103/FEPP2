import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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
import org.apache.http.util.EntityUtils;

import com.syscom.fep.frmcommon.util.FormatUtil;



public class WebApiTest {
	public static void main(String[] args) {
		//系統中 連接外部手機轉帳API 測試連線的功能
		try {
			FileReader fr = new FileReader("./WebApiTestData.txt");
			BufferedReader br = new BufferedReader(fr);
			
			while (br.ready()) {
				String value = br.readLine();
				if(StringUtils.isNotBlank(value)) {
					String[] array = value.split(",");
					char ch=',';
					long count = value.chars().filter(c -> c == ch).count();
//					System.out.println("count: " + count);
					if(count == 6) {
						int ix = array.length;
						String url = array[0];
						String port = array[1];
						String suffix = array[2];
						String idno = array[3];
						String MobilePhone = array[4];
						String BankCode = array[5];
						String tlstype = array[6];
//						System.out.println("getIdNo: " + url);
//						System.out.println("getMobilePhone: " + port);
//						System.out.println("getBankCode: " + idno);
//						System.out.println("getTimeout: " + MobilePhone);
						
						//憑證檔
				        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
				        trustStore.load(null);//Make an empty store
				        InputStream fis = new FileInputStream("./TCBCA_IndR.cer");
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
				                //"changeit".toCharArray() 這個參數是這個憑證檔密碼的意思,合庫的憑證檔沒有密碼, 但使用憑證檔會必須使用預設密碼changeit
				                .loadKeyMaterial(trustStore, "changeit".toCharArray())
				                .build();
				        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
				                sslcontext,
				                new String[]{tlstype},
				                null,
				                NoopHostnameVerifier.INSTANCE);
				        CloseableHttpClient client = HttpClients.custom()
				                .setSSLSocketFactory(sslConnectionSocketFactory)
				                .build();
				      //憑證檔結束
			        	
				        // 串外部的API
					    HttpPost httpPost = new HttpPost(url+":"+port+suffix);
//						    HttpPost httpPost = new HttpPost("http://www.example.com");

					    //塞 API要的參數  IDNO是身分證  BankCode是銀行代號
					    String json = "{\"Idno\":\"" + idno + "\",\"MobilePhone\":\""+ MobilePhone + "\",\"BankCode\":\""+ BankCode.trim() + "\"}";
					    StringEntity entity = new StringEntity(json);
					    httpPost.setEntity(entity);
					    httpPost.setHeader("Accept", "application/json");
					    httpPost.setHeader("Content-type", "application/json");
					    httpPost.setHeader("X-SourceId", "ATM");
					    httpPost.setHeader("X-TxnDttm", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
					    
				        System.out.println("url: " + url+":"+port+suffix);
				        System.out.println("data: " + json);
				        System.out.println("tlstype: " + tlstype);
				        

					    CloseableHttpResponse response = client.execute(httpPost);
					    try {
			                HttpEntity httpentity = response.getEntity();
			                System.out.println(response.getStatusLine());
			                System.out.println("response ContentType:"+httpentity.getContentType());
			                System.out.println("response Content:"+IOUtils.toString(httpentity.getContent()));
			                System.out.println("response AllHeaders:"+response.getAllHeaders());
			            } finally {
			            	client.close();
			                response.close();
			            }
					}
					
				}
			}
			
			
			
			//在下程式碼是在本機可以測試用
			
//			String url = "http://127.0.0.1";
//			String port = "8081";
//			String suffix = "/atm/recvv";
////			
//			String idno = "AT00000001";
//			String MobilePhone = "0912555444";
//			String BankCode = "";
//			
//	        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
//	        trustStore.load(null);//Make an empty store
//	        InputStream fis = new FileInputStream("C:\\Users\\geas\\Desktop\\WebApiTest\\TCBCA_IndR.cer");
//	        BufferedInputStream bis = new BufferedInputStream(fis);
//
//	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//
//	        while (bis.available() > 0) {
//	            Certificate cert = cf.generateCertificate(bis);
//	            trustStore.setCertificateEntry("fiddler"+bis.available(), cert);
//	        }
//	        
//	        SSLContext sslcontext = SSLContexts.custom()
//	                .loadTrustMaterial(new TrustStrategy() {
//	                	@Override
//	                    public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) {
//	                        return true;
//	                    }
//	                })
//	                .loadKeyMaterial(trustStore, "changeit".toCharArray())
//	                .build();
////	        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
////	                sslcontext,
////	                new String[]{tlstype},
////	                null,
////	                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
//	        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
//	                sslcontext,
//	                new String[]{"TLSv1.2"},
//	                null,
//	                NoopHostnameVerifier.INSTANCE);
////	        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
////	        		SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),NoopHostnameVerifier.INSTANCE);
//	        CloseableHttpClient client = HttpClients.custom()
//	                .setSSLSocketFactory(sslConnectionSocketFactory)
//	                .build();
//        	
//		    HttpPost httpPost = new HttpPost(url+":"+port+suffix);
////			    HttpPost httpPost = new HttpPost("http://www.example.com");
//
//		    String json = "{\"Idno\":\"" + idno + "\",\"MobilePhone\":\""+ MobilePhone + "\",\"BankCode\":\""+ BankCode.trim() + "\"}";
//		    StringEntity entity = new StringEntity(json);
//		    httpPost.setEntity(entity);
//		    httpPost.setHeader("Accept", "application/json");
//		    httpPost.setHeader("Content-type", "application/json");
//		    httpPost.setHeader("X-SourceId", "ATM");
//		    httpPost.setHeader("X-TxnDttm", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
//		    
//	        System.out.println("url: " + url+":"+port+suffix);
//	        System.out.println("data: " + json);
//	        System.out.println("tlstype: " + "TLSv1.2");
//	        
//
//		    CloseableHttpResponse response = client.execute(httpPost);
////			    assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
//		    try {
//                HttpEntity httpentity = response.getEntity();
//                System.out.println(response.getStatusLine());
////                System.out.println(IOUtils.toString(entity.getContent()));
//                EntityUtils.consume(httpentity);
//            } finally {
//            	client.close();
//                response.close();
//            }
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
//			System.out.println("exception: " + e.getMessage());
		} finally {
			System.out.println("finally");
			System.exit(0);
		}	

	}
}
