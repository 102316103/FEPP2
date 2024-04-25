package com.syscom.fep.enclib.vo;

public class ENCConfig {
	/**
	 * ENCLib發生RC95時重試次數
	 */
	public static int EncRetryCount;
	/**
	 * ENCLib發生RC95時重試間隔,單位毫秒
	 */
	public static int EncRetryInterval;
	/**
	 * ENCLib呼叫Suip的IP及Port,最少一組,最多組,中間用分號區隔
	 */
	public static String SuipAddress;
	/**
	 * ENCLib呼叫Suip的逾時時間,單位為秒
	 */
	public static int SuipTimeout;
}
