package com.syscom.safeaa.security;

import com.syscom.safeaa.core.BaseInterface;
import com.syscom.safeaa.enums.EnumPasswordFormat;

/**
 * 
 * @author syscom
 *
 */
public interface Authentication extends BaseInterface{

	/**
	 * 
	 * 登錄檢核
	 * @param logOnId
	 * @param password
	 * @param logOnIp
	 * @param needChangePassowrd           
	 * @return 
	 * @throws Exception
	 *
	 */
	public boolean checkLogOn(String logOnId, String password, String logOnIp, boolean needChangePassowrd)
			throws Exception;

	/**
	 * 
	 * 登出
	 * @param logOnId
	 * @param logOnIp        
	 * @return 
	 * @throws Exception
	 *
	 */
	public boolean logOff(Integer userId,String logOnId, String logOnIp) throws Exception;

	/**
	 * 
	 * 修改密碼
	 * @param userId
	 * @param logOnId
	 * @param oldPassword
	 * @param newPassword
	 * @param logOnIp        
	 * @return 
	 * @throws Exception
	 *
	 */
	public boolean changePassword(Integer userId,String logOnId, String oldPassword, String newPassword, String logOnIp)
			throws Exception;

	/**
	 * 
	 * 重置密碼
	 * @param userId  
	 * @param logOnId
	 * @param logOnIp
	 * @param newPassword        
	 * @return 
	 * @throws Exception
	 *
	 */
	public boolean restPassword(Integer userId,String logOnId, String logOnIp, String newPassword) throws Exception;

	/**
	 * 
	 * 重置密碼
	 * @param userId 
	 * @param logOnId
	 * @param logOnIp
	 * @param logOnIdReset
	 * @param newPassword        
	 * @return 
	 * @throws Exception
	 *
	 */
	public boolean restPassword(Integer userId,String logOnId, String logOnIp, String logOnIdReset, String newPassword)
			throws Exception;

	/**
	 * 
	 * 解鎖賬號
	 * @param userId
	 * @param logOnId
	 * @param logOnIp
	 * @param logOnIdUnlocker     
	 * @return 
	 * @throws Exception
	 *
	 */
	public boolean unlockAccount(Integer userId, String logOnId, String logOnIp, String logOnIdUnlocker) throws Exception;

	/**
	 * 
	 * 密碼加密
	 * @param password
	 * @param passwordEncryption 
	 * @return 
	 * @throws Exception
	 *
	 */
	public String encryptPassword(String password, EnumPasswordFormat passwordEncryption) throws Exception;


}
