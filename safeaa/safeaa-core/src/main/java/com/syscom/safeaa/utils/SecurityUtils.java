package com.syscom.safeaa.utils;

import com.syscom.safeaa.log.LogHelperFactory;
import org.springframework.util.DigestUtils;

import com.syscom.safeaa.enums.EnumPasswordFormat;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

	public static String encryptPassword(String password,EnumPasswordFormat passwordEncryption) throws Exception {

		try {
			switch (passwordEncryption) {
				//case MD5 :
					//return DigestUtils.md5DigestAsHex(password.getBytes());
				case SHA :
					return SHA256(password);
				case TEXT:
					return password;
			default:
				return null;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private static String SHA(final String strText, final String strType) {
		// 返回值
		String strResult = null;

		// 是否有效字符串
		if (strText != null && strText.length() > 0) {
			try {
				// SHA 加密開始
				// 創建加密對象 並傳入加密類型
				MessageDigest messageDigest = MessageDigest.getInstance(strType);
				// 傳入要加密的字符串
				messageDigest.update(strText.getBytes("UTF-8"));

				// 得到 byte類型結果
				byte byteBuffer[] = messageDigest.digest();

				// 將byte轉換為String
				StringBuffer strHexString = new StringBuffer();
				// 遍歷byte buffer
				for (int i = 0; i < byteBuffer.length; i++) {
					String hex = Integer.toHexString(0xff & byteBuffer[i]);
					if (hex.length() == 1) {
						strHexString.append('0');
					}
					strHexString.append(hex);
				}
				// 得到返回結果
				strResult = strHexString.toString();
			} catch (NoSuchAlgorithmException e) {
				LogHelperFactory.getTraceLogger().exceptionMsg(e, e.getMessage());
			}catch (UnsupportedEncodingException e) {
				LogHelperFactory.getTraceLogger().exceptionMsg(e, e.getMessage());
			}
		}

		return strResult;
	}

	/**
	 * 傳入文本內容，返回
	 * @param strText
	 * @return
	 */
	public static String SHA256(final String strText) {
		return SHA(strText, "SHA-256");
	}
}
