package com.syscom.safeaa.core;


/**
 * AES encrypt algorithm
 *
 */

public interface EncryptAlgorithm {
	/**
	 * Encode
	 * 
	 * @param plaintext
	 * @return ciphertext
	 */
	String encrypt(Object plaintext);
	
	/**
	 * Decode
	 * 
	 * @param ciphertext  ciphertext
	 * @return plaintext
	 */
	Object decrypt(String ciphertext);
	
}
