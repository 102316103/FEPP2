package com.syscom.safeaa.security.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.configuration.DataSourceSafeaaConstant;
import com.syscom.safeaa.constant.CommonConstants;
import com.syscom.safeaa.enums.EnumPasswordFormat;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserstatusExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomauditlog;
import com.syscom.safeaa.mybatis.model.Syscomserial;
import com.syscom.safeaa.mybatis.model.Syscomuserstatus;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllUsers;
import com.syscom.safeaa.mybatis.vo.SyscomUserQueryVo;
import com.syscom.safeaa.mybatis.vo.SyscomresourceInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomuserInfoVo;
import com.syscom.safeaa.security.MyPolicy;
import com.syscom.safeaa.utils.DbHelper;
import com.syscom.safeaa.utils.PolyfillUtil;
import com.syscom.safeaa.utils.RandomPassword;
import com.syscom.safeaa.utils.SecurityUtils;
import com.syscom.safeaa.utils.SyscomConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.syscom.safeaa.mybatis.extmapper.SyscomserialExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.security.User;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserImpl extends ApplicationBase implements User {

	private int ErrorMessagesCount;

	@Autowired
	private SyscomConfig safeSettings;
	
	@Autowired
    private SyscomuserExtMapper syscomuserMapper;
	
	@Autowired
	private SyscomuserstatusExtMapper syscomuserstatusMapper;
	
	@Autowired
	private SyscomserialExtMapper serialExtMapper;



	/**
	 * 根據條件分頁查詢角色資料
	 *
	 * @param
	 * @return 角色資料集合信息
	 */
	@Override
    public List<Syscomuser> selectUserList(Syscomuser syscomuUser){
		return syscomuserMapper.selectUserList(syscomuUser);
	}

	/**
	 * 通過使用者名查詢使用者
	 *
	 * @param userName 使用者名
	 * @return 使用者對象信息
	 */
	@Override
    public List<Syscomuser> selectUserByUserName(String userName) {
		return syscomuserMapper.selectUserByUserName(userName);
	}


	/**
	 * Create one new SyscomUser record and relative SyscomUserStatus record.
	 *
	 */
	@Override
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public int createUser(Syscomuser oDefUser, int passwordFormat, String passwordHintQuestion,
						   String passwordHintAnswer, String ssCode) {
		try{
			if (oDefUser.getLogonid() == null || "".equals(oDefUser.getLogonid())){
				// 登入帳號不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyLogOnId);
			}
			int rst = -1;
			Integer nextId = -1;
			Syscomserial syscomserial = serialExtMapper.selectByPrimaryKey(CommonConstants.SerialName);
			if(syscomserial==null) {
				syscomserial = new Syscomserial();
				syscomserial.setSerialname(CommonConstants.SerialName);
				syscomserial.setNumberformat("");
				syscomserial.setResetfield("");
				syscomserial.setInterval(0);
				syscomserial.setNextid(1L);	
				syscomserial.setMaxvalue(9000000L);
				syscomserial.setUpdateUser(oDefUser.getUpdateuserid());
				rst = serialExtMapper.insert(syscomserial);
				nextId = 1;
			}else {
				if(syscomserial.getNextid()!=null) {
					syscomserial.setNextid(syscomserial.getNextid() + 1);
					syscomserial.setNumberformat("");
					syscomserial.setInterval(0);
				}else {
					syscomserial.setNextid(1L);
				}
				
				rst = serialExtMapper.updateByPrimaryKeySelective(syscomserial);
				nextId = syscomserial.getNextid().intValue();
			}
			
			if(rst==1) {
				oDefUser.setUserid(nextId);
			}else {
				return -1;
			}
			
			if (syscomuserMapper.insertSelective(oDefUser) > 0){
				Syscomuserstatus mDefUserStatus = new Syscomuserstatus();
				mDefUserStatus.setUserid(oDefUser.getUserid());
				mDefUserStatus.setLogonip("");
				mDefUserStatus.setIslogon(DbHelper.toShort(false));
				mDefUserStatus.setLogonchangesscode(DbHelper.toShort(true));				
				mDefUserStatus.setIslogon(DbHelper.toShort(false));
				mDefUserStatus.setSscodeerrorcount(0);
				mDefUserStatus.setLastlogonip("");
				if (ssCode.length() <= 0){
					// 若傳入密碼為空白，則自動產生新密碼
					ssCode = RandomPassword.doGenerate(8,16);
				}
				mDefUserStatus.setSscodeeffectdate(oDefUser.getEffectdate());
				mDefUserStatus.setSscodeformat(passwordFormat);
				mDefUserStatus.setSscodehintquestion(passwordHintQuestion);
				mDefUserStatus.setSscodehintanswer(passwordHintAnswer);
				// SHA(5)
				EnumPasswordFormat enumPasswordFormat = EnumPasswordFormat.getEnumPasswordFormatByValue(5);
				if (passwordFormat != 5 ){
					EnumPasswordFormat.getEnumPasswordFormatByValue(passwordFormat);
				}

				String encryptSsCode =	SecurityUtils.encryptPassword(ssCode, enumPasswordFormat);
				mDefUserStatus.setSscode(encryptSsCode);
				mDefUserStatus.setLastsscodechangetime(oDefUser.getEffectdate());
				mDefUserStatus.setIslockout((short) 0);
				syscomuserstatusMapper.insertSelective(mDefUserStatus);
			}

			return 1;
		}catch (Exception ex){
			return -1;
		}
		
	}


	/**
	 *
	 * 修改一筆使用者資料
	 * 若UserId有值則直接Update，若沒有則判斷LogOnId是否有值
	 * 並以LogOnId取得UserId再以UpdateByPrimaryKey更新
	 */
	@Transactional(transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	@Override
	public int updateUser(Syscomuser sysUser) throws SafeaaException {
		try{
			if (sysUser.getUserid() != null && sysUser.getUserid() != 0){
				// TODO: 2021/8/30
				sysUser.setUpdateuserid(1);
				sysUser.setUpdatetime(new Date());

				int mResult = syscomuserMapper.updateByPrimaryKeySelective(sysUser);
				if (mResult <= 0){
					addError(safeSettings.culture,SAFEMessageId.UpdateNoRecord);
				}
				return mResult;
			}
			else{
				if (sysUser.getLogonid() != null && !"".equals(sysUser.getLogonid())){
					if (syscomuserMapper.queryUserByNo(sysUser.getLogonid()).size() > 0){
						sysUser.setUpdateuserid(currentUser.getUserid());
						sysUser.setUpdatetime(new Date());
						int mResult = syscomuserMapper.updateByPrimaryKeySelective(sysUser);
						if (mResult <= 0){
							addError(safeSettings.culture,SAFEMessageId.UpdateNoRecord);
						}
						return mResult;
					}
					else{
						addError(safeSettings.culture,SAFEMessageId.WithoutUserData);
						return -1;
					}
				}
				else{
					addError(safeSettings.culture,SAFEMessageId.EmptyLogOnId);
					return -1;
				}
			}
		}catch (Exception e){
			throw e;

		}
	}


	/**
	 *
	 * 以使用者序號刪除一筆使用者及其相關的狀態資料及成員資料
	 * 同時刪除狀態資料所以要包Transaction
	 */
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	@Override
	public boolean deleteUser(int userId) throws SafeaaException {
		try{
			Syscomuser syscomuser = new Syscomuser();
			syscomuser.setUserid(userId);
			Syscomuserstatus  syscomuserstatus= new Syscomuserstatus();
			syscomuserstatus.setUserid((userId));
			if (syscomuserstatusMapper.deleteByPrimaryKey(syscomuserstatus) >= 0 && syscomuserMapper.deleteByPrimaryKey(syscomuser) > 0 ){
				return true;
			}
			else{
				addError(safeSettings.culture, SAFEMessageId.DeletMasterFail);
				return false;
			}
		}catch (Exception ex){
			throw ex;
		}
	}

	/**
	 *
	 * 以登入帳號(LogOnId)查出該筆使用者的使用者序號
	 *
	 */
	@Override
	public List<Syscomuser> getUserIdByNo(String logOnId) throws SafeaaException {
		try{
			if (StringUtils.isBlank(logOnId)){
				addError(safeSettings.culture, SAFEMessageId.EmptyLogOnId); // 鐧誨叆甯寵櫉涓嶅緱絀虹櫧
				return null;
			}

			List<Syscomuser> mResult = syscomuserMapper.queryUserByNo(logOnId);
			if (mResult.size() <= 0){
				addError(safeSettings.culture, SAFEMessageId.QueryNoRecord);
			}
			return mResult;
		}catch (Exception e){
			throw e;
		}
	}

	/**
	 *
	 * 以使用者序號(UserId)查出一筆使用者資料
	 *
	 */
	public Syscomuser getUserById(Syscomuser sysUser) throws SafeaaException {
		try{
			if (sysUser.getUserid() == null || sysUser.getUserid() == 0){
				addError(safeSettings.culture, SAFEMessageId.LostUserId);
				return null;
			}


			Syscomuser result = new Syscomuser();
			result = syscomuserMapper.selectByPrimaryKey(sysUser.getUserid());
			if (result != null && result.equals(new Syscomuser())){
				addError(safeSettings.culture, SAFEMessageId.QueryNoRecord);
			}
			return result;
		}catch (Exception ex){
			throw ex;
		}
	}

	/**
	 * 以使用者序號(UserId)查出一筆使用者資料(含狀態資料)
	 *
	 */
	@Override
	public int getUserById(Syscomuser sysUser, Syscomuserstatus syscomuserstatus) throws SafeaaException {
		try{
			if (sysUser.getUserid() == null || sysUser.getUserid() == 0){
				addError(safeSettings.culture, SAFEMessageId.LostUserId);
				return -1;
			}

			Syscomuser result = new Syscomuser();
			result = syscomuserMapper.selectByPrimaryKey(sysUser.getUserid());
			if (result != null && !result.equals(new Syscomuser())){
				syscomuserstatus.setUserid(sysUser.getUserid());
				Syscomuserstatus SyscomuserstatusResult = new Syscomuserstatus();
				SyscomuserstatusResult = syscomuserstatusMapper.selectByPrimaryKey(syscomuserstatus.getUserid());
				if (SyscomuserstatusResult != null && !SyscomuserstatusResult.equals(new Syscomuserstatus())){
					return 1;
				}
				else{
					addError(safeSettings.culture, SAFEMessageId.QueryNoRecord);
					return 0;
				}
			}
			else{
				addError(safeSettings.culture, SAFEMessageId.QueryNoRecord);
				return -1;
			}
		}catch (Exception ex){
			throw ex;
		}
	}

	/**
	 * all user data and relative status data.
	 */
	@Override
	public List<SyscomQueryAllUsers> getAllUsers(String whereClause) throws SafeaaException{
		return syscomuserstatusMapper.syscomQueryAllUsers(null, null, null, null);
	}

	/**
	 * To get one user and relative status data by using UserId.
	 * 1. Do not check valid date from EffectDate and ExpireDate.
	 * 2. If It have no culture code pass then system configuration status data will return.
	 * 以使用者序號(UserId)查出一筆使用者資料(含狀態資料)
	 * 1. 不判斷有效日期(含未生效及停用資料)
	 */
	@Override
	public List<SyscomQueryAllUsers> getUserDataById(int userId) throws SafeaaException {
		try{
			if (userId == 0){
				// 使用者序號未傳入
				addError(safeSettings.culture, SAFEMessageId.LostUserId);
				return null;
			}else{
				return syscomuserstatusMapper.syscomQueryAllUsers(userId, null, null, null);
			}
		}catch (Exception ex){
			throw ex;
		}
	}

	/**
	 *
	 * 以使用者名稱查出使用者資料(含狀態資料)
	 * 1. 不判斷有效日期(含未生效及停用資料)
	 * 2. 以Like方式查詢
	 */
	@Override
	public List<SyscomQueryAllUsers> getUserDataByName(String userName) throws SafeaaException {
		try{
			if (StringUtils.isBlank(userName)){
				// 使用者名稱不得空白
				addError(safeSettings.culture, SAFEMessageId.EmptyUserName);
				return null;
			}else{
				return syscomuserstatusMapper.syscomQueryAllUsers(null, userName, null, null);
			}
		}catch (Exception ex){
			throw ex;
		}
	}

	/**
	 *
	 * 以登入帳號(LogOnId)查出一筆使用者資料(含狀態資料)
	 * 1. 不判斷有效日期(含未生效及停用資料)
	 *
	 */
	@Override
	public List<SyscomQueryAllUsers> getUserDataByNo(String logOnId) throws SafeaaException {
		try{
			if (StringUtils.isBlank(logOnId)){
				// 登入帳號不得空白
				addError(safeSettings.culture, SAFEMessageId.EmptyLogOnId);
				return null;
			}else{
				return syscomuserstatusMapper.syscomQueryAllUsers(null, null, logOnId.trim(), null);
			}
		}catch (Exception e){
			throw e;
		}
	}

	/**
	 * <param name="userMail">user no mail address</param>
	 * one user data and relative status data.
	 * 以使用者EMail查出使用者資料(含狀態資料)
	 * 1. 不判斷有效日期(含未生效及停用資料)
	 *
	 * @param userMail
	 */
	@Override
	public List<SyscomQueryAllUsers> getUserDataByMail(String userMail) throws SafeaaException {
		try{
			if (StringUtils.isBlank(userMail)){
				// 使用者Mail不得空白
				addError(safeSettings.culture, SAFEMessageId.EmptyUserMail);
				return null;
			}else{
				return syscomuserstatusMapper.syscomQueryAllUsers(null, null, null, userMail.trim());
			}
		}catch (Exception e){
			throw e;
		}
	}

	/**
	 * Create one new SyscomUserStatus record.
	 * The count of successful insert records,
	 * 建立一筆使用者狀態資料
	 *
	 * @param syscomuserstatus
	 */
	@Override
	public int createUserStatus(Syscomuserstatus syscomuserstatus) throws Exception {
		try{
			if (syscomuserstatus.getUserid() == null || syscomuserstatus.getUserid() == 0){
				// 使用者序號未傳入
				addError(safeSettings.culture, SAFEMessageId.LostUserId);
				return -1;
			}

//			// 取得該系統Default管理原則
//			mMyPolicy.init();
//			syscomuserstatus.setLogonip("");
//			syscomuserstatus.setIslogon(DbHelper.toShort(false));
//			syscomuserstatus.setLogonchangesscode(DbHelper.toShort(mMyPolicy.ismFirstLogOnChangePassword()));
//			syscomuserstatus.setSscodeerrorcount(0);
//			syscomuserstatus.setLastlogonip("");
//
//			if (syscomuserstatus.getSscode() == null){
//				// 若傳入密碼為空白，則自動產生新密碼
//				String newPassword = RandomPassword.doGenerate(mMyPolicy.getmMinPasswordLength(),mMyPolicy.getmMaxPasswordLength());
//				syscomuserstatus.setSscode(newPassword);
//			}
//
//			syscomuserstatus.setSscodeeffectdate(new Date());
//			syscomuserstatus.setSscode(authentication.encryptPassword(syscomuserstatus.getSscode(), EnumPasswordFormat.values()[syscomuserstatus.getSscodeformat().intValue()]));
//
//			int mResult = syscomuserstatusMapper.insert(syscomuserstatus);
//
//			if (mResult <= 0){
//				addError(safeSettings.culture, SAFEMessageId.InsertNoRecord);
//			}
//			return mResult;
			
			return 1;
		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * Create one new SyscomUserStatus record.
	 * The count of successful insert records,
	 * 修改一筆使用者狀態資料
	 *
	 * @param syscomuserstatus
	 */
	@Override
	public int updateUserStatus(Syscomuserstatus syscomuserstatus) throws SafeaaException {
		try{
			if (syscomuserstatus.getUserid() == null || syscomuserstatus.getUserid() == 0){
				// 使用者序號未傳入
				addError(safeSettings.culture, SAFEMessageId.LostUserId);
				return -1;
			}

			int mResult = syscomuserstatusMapper.updateByPrimaryKeySelective(syscomuserstatus);
			if (mResult <= 0){
				addError(safeSettings.culture, SAFEMessageId.UpdateNoRecord);
			}
			return mResult;

		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * Delete one designate SyscomUserStatus record.
	 * The count of successful deleted records,
	 * 以使用者序號刪除一筆使用者狀態資料
	 *
	 * @param userId
	 */
	@Override
	public int deleteUserStatus(int userId) throws SafeaaException {
		try{
			if (userId == 0){
				// 使用者序號未傳入
				addError(safeSettings.culture, SAFEMessageId.LostUserId);
				return -1;
			}

			Syscomuserstatus  syscomuserstatus= new Syscomuserstatus();
			syscomuserstatus.setUserid(userId);
			int mResult = syscomuserstatusMapper.deleteByPrimaryKey(syscomuserstatus);
			if (mResult <= 0){
				addError(safeSettings.culture, SAFEMessageId.DeleteNoRecord);
			}
			return mResult;

		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * To get SyscomUserStatus record by using UserId.
	 * Status data of designate user.
	 * 以使用者序號(UserId)查出該筆使用者的狀態資料
	 *
	 * @param syscomuserstatus
	 */
	@Override
	public Syscomuserstatus getUserStatusById(Syscomuserstatus syscomuserstatus) throws SafeaaException {
		try{
			if (syscomuserstatus.getUserid() == null || syscomuserstatus.getUserid() == 0){
				// 使用者序號未傳入
				addError(safeSettings.culture, SAFEMessageId.LostUserId);
				return null;
			}

			return syscomuserstatusMapper.selectByPrimaryKey(syscomuserstatus.getUserid());

		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * Force to unlock one user status record.
	 * 強迫帳戶解鎖
	 *
	 * @param userId
	 * @param updateUserId
	 */
	@Override
	public int forceAccountLockOff(int userId, int updateUserId) throws SafeaaException {
		try{
			if (userId == 0){
				// 使用者序號未傳入
				addError(safeSettings.culture, SAFEMessageId.LostUserId);
				return -1;
			}

			Syscomuserstatus syscomuserstatus = new Syscomuserstatus();
			syscomuserstatus.setUserid(userId);
			syscomuserstatus.setIslogon(DbHelper.toShort(false));
			syscomuserstatus.setIslockout(DbHelper.toShort(false));

			int mResult = syscomuserstatusMapper.updateByPrimaryKeySelective(syscomuserstatus);
			if (mResult <= 0){
				addError(safeSettings.culture, SAFEMessageId.UpdateNoRecord);
			}
			return mResult;

		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * Validate the new LogOnId of user.
	 *
	 * @param newLogOnId
	 * @Param new user logon Id
	 * <returns>
	 * True : LogOnId is valid, False : LogOnId is not valid
	 * </returns>
	 * Check rules are depending on system policy.
	 * 檢核新登入帳號
	 * 先取得該系統Default管理原則
	 */
	@Override
	public boolean validateNewLogOnId(String newLogOnId) throws Exception {
		try{
			if (StringUtils.isBlank(newLogOnId)){
				// 登入帳號不得空白
				addError(safeSettings.culture, SAFEMessageId.EmptyLogOnId);
				return false;
			}

//			// 取得該系統Default管理原則
//			mMyPolicy.init();
//
			ErrorMessagesCount = 0;
//			// 檢核LogOnId長度
//			if (mMyPolicy.getmMaxAccountLength() > 0 && mMyPolicy.getmMinAccountLength() > 0){
//				boolean checkLength = checkAccountLength(newLogOnId,mMyPolicy);
//			}
//
//			// 檢核LogOnId必須包含文字字元
//			if (mMyPolicy.ismAccountRequireAlphaNumeric()){
//				boolean checkAlphaNumeric = checkAccountAlphaNumeric(newLogOnId);
//			}

			if (ErrorMessagesCount > 0){
				return false;
			}else{
				return true;
			}
		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * 檢核LogOnId長度
	 * @param logOnId
	 * @return
	 */
	private boolean checkAccountLength(String logOnId, MyPolicy myPolicy) throws SafeaaException {
		if (logOnId.length() > myPolicy.getmMaxAccountLength()){
			addError(safeSettings.culture, SAFEMessageId.MaxAccountLength);
			ErrorMessagesCount += 1;
			return false;
		}else{
			if (logOnId.length() < myPolicy.getmMinAccountLength()){
				addError(safeSettings.culture, SAFEMessageId.MinAccountLength);
				ErrorMessagesCount += 1;
				return false;
			}else{
				return true;
			}
		}
	}

	/**
	 * 檢核LogOnId必須包含文數字字元
	 * @param logOnId
	 * @return
	 */
	private boolean checkAccountAlphaNumeric(String logOnId) throws SafeaaException {
		if (PolyfillUtil.isNumeric(logOnId)){
			addError(safeSettings.culture, SAFEMessageId.AccountRequireAlphaNumeric);
			ErrorMessagesCount += 1;
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Validate the new password of user.
	 * True : password is valid, False : password is not valid
	 *
	 * @param userId
	 * @param newPassword
	 * @param idno
	 * @param birthday
	 * @param logOnId
	 * @param passwordEncryption
	 * @return
	 * @throws Exception 檢核新密碼
	 *                   先取得該系統Default管理原則
	 */
	@Override
	public boolean validateNewPassword(int userId, String newPassword, String idno, String birthday, String logOnId, int passwordEncryption) throws Exception {
		try{
			if (StringUtils.isBlank(newPassword)){
				// 密碼不得空白
				addError(safeSettings.culture, SAFEMessageId.EmptyPassword);
				return false;
			}

//			// 取得該系統Default管理原則
//			mMyPolicy.init(userId);
//			
			ErrorMessagesCount = 0;
//			// 檢核密碼允許包含LogOnId
//			if (mMyPolicy.ismAllowLogOnId()){
//				checkPasswordAllowLogOnId(newPassword,logOnId);
//			}
//
//			// 檢核密碼允許重複字元
//			if (!DbHelper.toBoolean((short)mMyPolicy.getmAllowContinueCharacters())){
//				checkPasswordRepeatedCharacters(newPassword);
//			}
//
//			// 檢核密碼強制執行密碼歷程記錄
//			if (mMyPolicy.getmEnforcePasswordHistory() > 0){
//				checkPasswordHistory(userId, (authentication.encryptPassword(newPassword, EnumPasswordFormat.values()[passwordEncryption])).toString(),mMyPolicy);
//				checkPasswordHistory(userId, "", mMyPolicy);
//			}
//
//			// 檢核密碼長度
//			if (mMyPolicy.getmMaxPasswordLength() > 0 && mMyPolicy.getmMinPasswordLength() > 0){
//				checkPasswordLength(newPassword, mMyPolicy);
//			}
//
//			// 檢核密碼必須符合複雜性需求
//			if (mMyPolicy.ismPasswordComplexity()){
//				checkPasswordComplexity(newPassword);
//			}
//
//			// 檢核密碼必須包含文數字字元
//			if (mMyPolicy.ismAccountRequireAlphaNumeric()){
//				checkPasswordAlphaNumeric(newPassword, mMyPolicy);
//			}
//
//			// 檢核密碼必須包含特殊字元
//			if (mMyPolicy.ismRequireSpecialCharacters()){
//				checkPasswordSpecialCharacters(newPassword);
//			}
//
//			// 檢核密碼允許包含身分證號
//			if (!mMyPolicy.ismAllowIdNo()){
//				checkPasswordAllowIDNO(newPassword, idno);
//			}
//
//			// 檢核密碼允許包含出生日期
//			if (!mMyPolicy.ismAllowBirthday()){
//				checkPasswordAllowBirthday(newPassword, birthday);
//			}
//
//			// 檢核密碼允許連續字元數
//			if (mMyPolicy.getmAllowContinueCharacters() > 0){
//				checkPasswordAllowContinueCharacters(newPassword, mMyPolicy.getmAllowContinueCharacters());
//			}

			if (ErrorMessagesCount > 0){
				return false;
			}else{
				return true;
			}

		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * 檢核密碼允許包含LogOnId
	 * @param password
	 * @param logOnId
	 * @return
	 */
	private boolean checkPasswordAllowLogOnId(String password, String logOnId) throws SafeaaException {
		if (password.indexOf(logOnId,5) > 0){
			addError(safeSettings.culture, SAFEMessageId.AlllowLogOnId);
			ErrorMessagesCount += 1;
			return false;
		}
		return true;
	}

	/**
	 * 檢核密碼允許重複字元
	 * @param password
	 */
	private boolean checkPasswordRepeatedCharacters(String password) throws SafeaaException {
		String ch = password.substring(0,1);
		for (int i= 0;i<password.length();i++){
			if (ch.equals(password.substring(i,i+1))){
				addError(safeSettings.culture, SAFEMessageId.AllowRepeatedCharacters);
				ErrorMessagesCount += 1;
				return false;
			}
		}
		return true;
	}

	/**
	 * 檢核密碼強制執行密碼歷程記錄
	 * @param userId
	 * @param encryptPassword
	 */
	private boolean checkPasswordHistory(int userId, String encryptPassword, MyPolicy myPolicy) throws SafeaaException {
		try{
			List<Syscomauditlog> syscomauditlog = new ArrayList<Syscomauditlog>();

//			syscomauditlog = syscomAuditLogDaoImpl.queryChangePasswordLog(userId, myPolicy.getmEnforcePasswordHistory());

			for (Syscomauditlog item : syscomauditlog){
				if (encryptPassword.equals(item.getAudittarget().trim())){
					addError(safeSettings.culture, SAFEMessageId.EnforcePasswordHistory);
					ErrorMessagesCount += 1;
					return false;
				}
			}
			return true;
		}catch (Exception e){
			throw e;
		}
	}

	/**
	 * 檢核密碼長度
	 * @param password
	 * @return
	 */
	private boolean checkPasswordLength(String password, MyPolicy myPolicy) throws SafeaaException {
		if (password.length() > myPolicy.getmMaxPasswordLength()){
			addError(safeSettings.culture, SAFEMessageId.MaxPasswordLength);
			ErrorMessagesCount += 1;
			return false;
		}
		else{
			if (password.length() < myPolicy.getmMinPasswordLength()){
				addError(safeSettings.culture, SAFEMessageId.MinPasswordLength);
				ErrorMessagesCount += 1;
				return false;
			}else{
				return true;
			}
		}
	}

	/**
	 * 檢核密碼必須符合複雜性需求
	 * @param password
	 * @return
	 */
	private boolean checkPasswordComplexity(String password) throws SafeaaException {
		int i = 0;
		int kind = 0;

		// 指示指定字串中位於指定位置處的字元是否屬於小寫字母類別
		for (i=0;i<password.length()-1;i++){
			if (Character.isLowerCase(password.charAt(i))){
				kind += 1;
				break;
			}
		}

		// 指示指定字串中位於指定位置處的字元是否屬於大寫字母類別
		for (i=0;i<password.length()-1;i++){
			if (Character.isUpperCase(password.charAt(i))){
				kind += 1;
				break;
			}
		}

		// 指示指定字串中位於指定位置處的字元是否屬於十進位數字字類別
		for (i=0;i<password.length()-1;i++){
			if (Character.isDigit(password.charAt(i))){
				kind += 1;
				break;
			}
		}

		// 指示指定字串中位於指定位置處的字元是否屬於符號字元類別
		String regEx = "[ -_`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(password);
		if (m.find()){
			kind += 1;
		}

		if(kind >= 3){
			return true;
		}else{
			addError(safeSettings.culture, SAFEMessageId.PasswordComplexity);
			ErrorMessagesCount += 1;
			return false;
		}
	}

	/**
	 * 檢核密碼必須包含文數字字元
	 * @param password
	 * @return
	 */
	private boolean checkPasswordAlphaNumeric(String password, MyPolicy myPolicy) throws SafeaaException {
		String regEx = StringUtils.join("^(?=.*\\d)(?=.*[a-z]).{", myPolicy.getmMinPasswordLength(), ",",myPolicy.getmMaxPasswordLength(),"}$");
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(password);
		if (!m.find()){
			addError(safeSettings.culture, SAFEMessageId.PasswordRequireAlphaNumeric);
			ErrorMessagesCount += 1;
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 檢核密碼必須包含特殊字元
	 * @param password
	 * @return
	 */
	private boolean checkPasswordSpecialCharacters(String password) throws SafeaaException {
		String regEx = "!@#$%^&+=_";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(password);
		if (!m.find()){
			addError(safeSettings.culture, SAFEMessageId.RequireSpecialCharacters);
			ErrorMessagesCount += 1;
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 檢核密碼允許包含身分證號
	 * @param password
	 * @param idno
	 * @return
	 */
	private boolean checkPasswordAllowIDNO(String password, String idno) throws SafeaaException {
		if (password.indexOf(idno, 4) > 0){
			return false;
		}else{
			addError(safeSettings.culture, SAFEMessageId.AllowIDNO);
			ErrorMessagesCount += 1;
			return true;
		}
	}

	/**
	 * 檢核密碼允許包含出生日期
	 * @param password
	 * @param birthday
	 * @return
	 */
	private boolean checkPasswordAllowBirthday(String password, String birthday) throws SafeaaException {
		if (password.indexOf(birthday, 5) > 0){
			return false;
		}else{
			addError(safeSettings.culture, SAFEMessageId.AllowIDNO);
			ErrorMessagesCount += 1;
			return true;
		}
	}

	/**
	 * 檢查密碼連續字元是否超過限制次數
	 * @param password 密碼
	 * @param repeatCount 限制次數
	 * @return
	 */
	private boolean checkPasswordAllowContinueCharacters(String password, int repeatCount) {
		String sAlphas = "abcdefghijklmnopqrstuvwxyz";
		String sNumerics = "01234567890";

		password = password.toLowerCase();

		for(int i=0;i< 26-repeatCount;i++){
			String sFwdAlpha = sAlphas.substring(i,i+repeatCount);
			String sRevAlpha = reverseString(sFwdAlpha);
			if (password.indexOf(sFwdAlpha, 5) > -1
					|| password.indexOf(sRevAlpha, 5) > -1){
				return false;
			}
		}

		for(int i=0;i< 10-repeatCount;i++){
			String sFwdNum = sNumerics.substring(i,i+repeatCount);
			String sRevNum = reverseString(sFwdNum);
			if (password.indexOf(sFwdNum, 5) > -1
					|| password.indexOf(sRevNum, 5) > -1){
				return false;
			}
		}

		return true;
	}

	private String reverseString(String strToReverse) {
		String result = "";
		for (int i=0;i< strToReverse.length()-1;i++){
			result += strToReverse.charAt(strToReverse.length()-1-i);
		}
		return result;
	}

	@Override
	public List<SyscomUserQueryVo> queryUsersBy(String logonId, String userName,String sort) throws Exception {				
		try{
			List<SyscomUserQueryVo> list = syscomuserMapper.queryUsersBy(logonId,userName,sort);		
			return list;
		}catch (Exception e){
			throw e;
		}
		
	}

	@Override
	public SyscomuserInfoVo getSyscomuserInfo(String logOnId) throws SafeaaException {

		try{
			
			if (StringUtils.isBlank(logOnId)){
				// 登入帳號不得空白
				addError(safeSettings.culture, SAFEMessageId.EmptyLogOnId);
				return null;
			}

			SyscomuserInfoVo result = new SyscomuserInfoVo();
			result = syscomuserMapper.getSyscomuserInfo(logOnId);
			if (result != null && result.equals(new Syscomuser())){
				addError(safeSettings.culture, SAFEMessageId.QueryNoRecord);
			}
			return result;
		}catch (Exception ex){
			throw ex;
		}
	}
	
	@Override
	public List<SyscomresourceInfoVo> querySyscomresourceByLogOnId(String logOnId) throws SafeaaException {
		try{
			if (StringUtils.isBlank(logOnId)){
				// 登入帳號不得空白
				addError(safeSettings.culture, SAFEMessageId.EmptyLogOnId);
				return null;
			}
			List<SyscomresourceInfoVo> list = syscomuserMapper.querySyscomresourceByLogOnId(logOnId);		
			return list;
		}catch (Exception ex){
			throw ex;
		}
	}

}
