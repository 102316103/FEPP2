package com.syscom.safeaa.security;

/**
 * 
 * @author syscom
 *
 */
public interface Serial {

	/**
	 * 取得下一流水號
	 * @param serialName
	 * @return
	 */
	public Long getNextId(String serialName);
	
	/**
	 * 取得下一流水號
	 * @param serialName
	 * @param resetValue
	 * @return
	 */
	public Long getNextId(String serialName,String resetValue);
	
	/**
	 * 取得下一流水號
	 * @param serialName
	 * @param interval
	 * @return
	 */
	public Long getNextId(String serialName,Integer interval);
	
	/**
	 * 取得下一流水號
	 * @param serialName
	 * @param resetValue
	 * @param interval
	 * @return
	 */
	public Long getNextId(String serialName,String resetValue,Integer interval);
	
	/**
	 * 重置流水號
	 * @param serialName
	 * @throws Exception
	 */
	public void resetId(String serialName) throws Exception;
	
	/**
	 * 取得下一流水號并格式化
	 * @param serialName
	 * @return
	 */
	public String getNextIdWithFormat(String serialName);
}
