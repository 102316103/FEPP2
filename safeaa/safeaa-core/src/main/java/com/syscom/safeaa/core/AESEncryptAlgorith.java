package com.syscom.safeaa.core;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;

public class AESEncryptAlgorith implements EncryptAlgorithm {
	// read from properties
	private static final String KEYS = "aes-key";  
	
	private byte[] secretKey;

	public void init() {
		secretKey =  Arrays.copyOf( DigestUtils.sha1(KEYS) , 16);
	}
	
	@Override
	public String encrypt(final Object plaintext) {
//		if (null ==  plaintext) {
//			return null;
//		}
//
//		try {
//			Cipher cipher = Cipher.getInstance("AES");
//			cipher.init(Cipher.ENCRYPT_MODE,  new SecretKeySpec(secretKey, "AES"));
//
//			return Base64.encodeBase64String(cipher.doFinal(StringUtils.getBytesUtf8(String.valueOf(plaintext))));
//
//		}catch(NoSuchAlgorithmException| NoSuchPaddingException |InvalidKeyException  | IllegalBlockSizeException | BadPaddingException e) {
//
//		}
		return null;
	}

	@Override
	public Object decrypt(final String ciphertext) {
//		if (null == ciphertext ) {
//			return null;
//		}
//		try {
//			Cipher cipher = Cipher.getInstance("AES");
//			cipher.init(Cipher.DECRYPT_MODE,  new SecretKeySpec(secretKey, "AES"));
//
//			byte[] result = cipher.doFinal(Base64.decodeBase64(ciphertext));
//			return new String (result, StandardCharsets.UTF_8 );
//		}catch (Exception e) {
//		}
		return null;
	}
	
}
