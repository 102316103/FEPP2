package com.syscom.safeaa.security.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.syscom.safeaa.configuration.DataSourceSafeaaConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.enums.EnumAuditType;
import com.syscom.safeaa.enums.EnumPasswordFormat;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.extmapper.SyscomauditlogExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserstatusExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomauditlog;
import com.syscom.safeaa.mybatis.model.Syscomuserstatus;
import com.syscom.safeaa.mybatis.vo.SyscomuserInfoVo;
import com.syscom.safeaa.security.Authentication;
import com.syscom.safeaa.security.MyPolicy;
import com.syscom.safeaa.security.User;
import com.syscom.safeaa.utils.DbHelper;
import com.syscom.safeaa.utils.SecurityUtils;
import com.syscom.safeaa.utils.SyscomConfig;

/**
 * @author syscom
 */
@Component
public class AuthenticationImpl extends ApplicationBase implements Authentication {

    private String mLogonIp = "";

    @Autowired
    private SyscomConfig safeSettings;

    @Autowired
    private SyscomuserstatusExtMapper userstatusExtMapper;

    @Autowired
    private SyscomauditlogExtMapper auditlogExtMapper;

    @Autowired
    private SyscomuserExtMapper userExtMapper;

    @Autowired
    private User userImpl;

    @Autowired
    private MyPolicy myPolicyImpl;

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
    public boolean checkLogOn(String logOnId, String password, String logOnIp, boolean needChangePassowrd) throws Exception {
        try {

            if (StringUtils.isBlank(logOnId)) {
                // 登入帳號不得空白
                addError(safeSettings.getCulture(), SAFEMessageId.EmptyLogOnId);
                return false;
            }

            if (!getCurrentUserData(null, logOnId, EnumAuditType.LogOn.getValue(), logOnIp)) {
                // 無使用者資料
                addError(safeSettings.getCulture(), SAFEMessageId.WithoutUserData);
                return false;
            }

            // 檢核帳號是否已停用或逾時
            if (!checkUserValid()) {
                return false;
            }

            // 取得該User相關原則
            myPolicyImpl.init(currentUser.getUserid());

            // Check 是否重複登入(視AllowMultiLogOn是否允許)
            // if(!myPolicyImpl.ismAllowMultiLogOn() && !checkReLogon()) {
            // return false;
            // }

            // 檢核該帳戶是否已被鎖定
            if (!checkAccountLock()) {
                return false;
            }

            // 檢核計數器是否需重設為 0 次
            if (checkResetLockoutThreshold()) {
                mDefUserStatus.setSscodeerrorcount(0);
            }

            // Check 密碼
            if (!checkPassword(password)) {
                return false;
            } else {
                // 檢核密碼最後啟動日期
                if (!checkLastActivePasswordDate()) {
                    return false;
                } else {

                    addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), logOnIp, true, "");

                    mDefUserStatus.setLastlogontime(mDefUserStatus.getLogontime());
                    mDefUserStatus.setLastlogonip(mDefUserStatus.getLogonip());
                    mDefUserStatus.setLogontime(new Date());
                    mDefUserStatus.setLogonip(logOnIp);
                    mDefUserStatus.setIslogon(DbHelper.toShort(true));
                    if (mDefUserStatus.getLogonchangesscode() > 0) {
                        mDefUserStatus.setLogonchangesscode((short) 1);
                    } else {
                        if (checkNeedChangePassword()) {
                            mDefUserStatus.setLogonchangesscode((short) 1);
                        } else {
                            mDefUserStatus.setLogonchangesscode((short) 0);
                        }
                    }
                    mDefUserStatus.setSscodeerrorcount(0);
                    mDefUserStatus.setFailedpswansattemptcount(0);
                    mDefUserStatus.setIslockout(DbHelper.toShort(false));
                    userstatusExtMapper.updateByPrimaryKey(mDefUserStatus);
                    return true;
                }

            }

        } catch (Exception e) {
            throw e;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
    public boolean logOff(Integer userId, String logOnId, String logOnIp) throws Exception {
        try {
            if (StringUtils.isBlank(logOnId)) {
                // 登入帳號不得空白
                addError(safeSettings.getCulture(), SAFEMessageId.EmptyLogOnId);
                return false;
            }

            if (!getCurrentUserData(userId, logOnId, EnumAuditType.LogOff.getValue(), logOnIp)) {
                // 無使用者資料
                addError(safeSettings.getCulture(), SAFEMessageId.WithoutUserData);
                return false;
            }

            mLogonIp = logOnIp;
            addLog(currentUser.getUserid(), EnumAuditType.LogOff.getValue(), mLogonIp, true, "");

            mDefUserStatus.setIslockout(DbHelper.toShort(false));
            mDefUserStatus.setLastlockouttime(new Date());
            int rst = userstatusExtMapper.updateByPrimaryKey(mDefUserStatus);
            if (rst > 0) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            throw e;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
    public boolean changePassword(Integer userId, String logOnId, String oldPassword, String newPassword, String logOnIp) throws Exception {
        try {

            mLogonIp = logOnIp;

            if (oldPassword.equals(newPassword)) {
                // 新舊密碼不得相同
                addError(safeSettings.getCulture(), SAFEMessageId.TheSamePassword);
                return false;
            }

            if (StringUtils.isBlank(newPassword)) {
                // 新密碼不得空白
                addError(safeSettings.getCulture(), SAFEMessageId.NewPasswordEmpty);
                return false;
            }

            if (StringUtils.isBlank(logOnId)) {
                // 登入帳號不得空白
                addError(safeSettings.getCulture(), SAFEMessageId.EmptyLogOnId);
                return false;
            }

            if (!getCurrentUserData(userId, logOnId, EnumAuditType.ChangePassword.getValue(), logOnIp)) {
                // 無使用者資料
                addError(safeSettings.getCulture(), SAFEMessageId.WithoutUserData);
                return false;
            }

            // 檢核帳號是否已停用或逾時
            if (!checkUserValid()) {
                return false;
            }

            // Check Old password
            String encry = encryptPassword(oldPassword, EnumPasswordFormat.values()[mDefUserStatus.getSscodeformat().intValue() - 1]);
            if (mDefUserStatus.getSscode().equals(encry)) {
                // 檢核新密碼是否符合系統密碼原則
                if (userImpl.validateNewPassword(currentUser.getUserid(), newPassword, currentUser.getIdno(), currentUser.getBirthday(), currentUser.getLogonid(),
                        mDefUserStatus.getSscodeformat().intValue())) {
                    mDefUserStatus.setLastsscodechangetime(new Date());
                    mDefUserStatus.setSscodeerrorcount(0);
                    mDefUserStatus.setFailedpswansattemptcount(0);
                    // LogOnChangePassword必須是False, 因為這是ChangePassword
                    mDefUserStatus.setLogonchangesscode((short) 0);
                    mDefUserStatus.setSscode(encryptPassword(newPassword, EnumPasswordFormat.values()[mDefUserStatus.getSscodeformat().intValue() - 1]));

                    int rst = userstatusExtMapper.updateByPrimaryKey(mDefUserStatus);
                    if (rst > 0) {
                        addLog(userId, EnumAuditType.ChangePassword.getValue(), mLogonIp, true, mDefUserStatus.getSscode());
                        return true;
                    } else {
                        addLog(userId, EnumAuditType.ChangePassword.getValue(), mLogonIp, false, "");
                        return false;
                    }
                } else {
                    // 新密碼不符合系統密碼原則
                    addError(safeSettings.getCulture(), SAFEMessageId.NewPasswordAgainstPolicy);
                    addLog(userId, EnumAuditType.ChangePassword.getValue(), mLogonIp, false, SAFEMessageId.NewPasswordAgainstPolicy.toString());
                    return false;
                }
            } else {
                // 舊密碼錯誤
                addError(safeSettings.getCulture(), SAFEMessageId.WrongOldPassword);
                addLog(userId, EnumAuditType.ChangePassword.getValue(), mLogonIp, false, SAFEMessageId.WrongOldPassword.toString());
                return false;
            }

        } catch (Exception e) {
            throw e;
        }

    }

    @Override
    public boolean restPassword(Integer userId, String logOnId, String logOnIp, String newPassword) throws Exception {
        return restPassword(userId, logOnId, logOnIp, logOnId, newPassword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
    public boolean restPassword(Integer userId, String logOnId, String logOnIp, String logOnIdReset, String newPassword) throws Exception {
        if (userId == null) {
            // 使用者序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostUserId);
            return false;
        }

        if (StringUtils.isBlank(logOnId)) {
            // 登入帳號不得空白
            addError(safeSettings.getCulture(), SAFEMessageId.EmptyLogOnId);
            return false;
        }

        try {

            Syscomuserstatus userStatus = new Syscomuserstatus();
            userStatus.setUserid(userId);
            userStatus.setLastsscodechangetime(new Date());
            userStatus.setSscodeerrorcount(0);
            userStatus.setFailedpswansattemptcount(0);
            userStatus.setLogonchangesscode(DbHelper.toShort(true));
            userStatus.setSscodeeffectdate(new Date());
            // SHA(5)
            userStatus.setSscode(encryptPassword(newPassword, EnumPasswordFormat.values()[4]));

            int rst = userstatusExtMapper.updateByPrimaryKeySelective(userStatus);
            if (rst > 0) {
                return true;
            } else {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
        } catch (Exception e) {

            throw e;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
    public boolean unlockAccount(Integer userId, String logOnId, String logOnIp, String logOnIdUnlocker) throws Exception {

        if (userId == null) {
            // 使用者序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostUserId);
            return false;
        }

        if (StringUtils.isBlank(logOnId)) {
            // 登入帳號不得空白
            addError(safeSettings.getCulture(), SAFEMessageId.EmptyLogOnId);
            return false;
        }

        try {

            Syscomuserstatus userStatus = new Syscomuserstatus();
            // Update UserStatus
            userStatus.setUserid(userId);
            userStatus.setSscodeerrorcount(0);
            userStatus.setFailedpswansattemptcount(0);
            userStatus.setSscodeeffectdate(new Date());
            userStatus.setIslockout(DbHelper.toShort(false));

            int rst = userstatusExtMapper.updateByPrimaryKeySelective(userStatus);
            if (rst > 0) {
                return true;
            } else {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String encryptPassword(String ssCode, EnumPasswordFormat enumPasswordFormat) throws Exception {
        return SecurityUtils.encryptPassword(ssCode, enumPasswordFormat);
    }

    /**
     * 檢核帳號是否已停用或逾時
     *
     * @return
     * @throws Exception
     */
    private boolean checkUserValid() throws Exception {
        try {
            if (currentUser.getEffectdate().after(new Date())) {
                // 該登入帳號尚未啟用
                addError(safeSettings.getCulture(), SAFEMessageId.LogOnIdNotEffect);
                addLog(currentUser.getUserid(), EnumAuditType.ResetPassword.getValue(), mLogonIp, false, SAFEMessageId.LogOnIdNotEffect.toString());
                return false;
            }

            if (currentUser.getExpireddate().before(new Date())) {
                // 該登入帳號已停用
                addError(safeSettings.getCulture(), SAFEMessageId.LogOnIdExpired);
                addLog(currentUser.getUserid(), EnumAuditType.ResetPassword.getValue(), mLogonIp, false, SAFEMessageId.LogOnIdExpired.toString());
                return false;
            }

            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Check 是否重複登入(視AllowMultiLogOn是否允許)
     *
     * @return
     * @throws Exception
     */
    private boolean checkReLogon() throws Exception {
        try {
            if (DbHelper.toBoolean(mDefUserStatus.getIslogon().toString())) {
                // 重複登入
                addError(safeSettings.getCulture(), SAFEMessageId.UserAlreadyLogOn);
                addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), mLogonIp, false, SAFEMessageId.UserAlreadyLogOn.toString());

                return false;
            }
        } catch (Exception e) {
            throw e;
        }
        return true;
    }

    /**
     * 檢核該帳戶是否已被鎖定
     *
     * @return 傳回True代表無鎖定或已解除鎖定
     * @throws Exception
     */
    private boolean checkAccountLock() throws Exception {
        try {
            if (DbHelper.toBoolean((short) mDefUserStatus.getIslockout().intValue())) {
                if (myPolicyImpl.getmAccountLockoutDuration() == 0) {
                    // 登入帳號尚在鎖定期間
                    addError(safeSettings.getCulture(), SAFEMessageId.AccountLockoutDuration);
                    addLog(currentUser.getUserid(), EnumAuditType.ResetPassword.getValue(), mLogonIp, false, SAFEMessageId.AccountLockoutDuration.toString());
                    return false;
                } else {
                    if (mDefUserStatus.getLastlockouttime() != null) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(mDefUserStatus.getLastlockouttime());
                        c.add(Calendar.MINUTE, myPolicyImpl.getmAccountLockoutDuration());
                        Date d = c.getTime();
                        if ((new Date()).after(d)) {
                            mDefUserStatus.setIslockout(DbHelper.toShort(false));
                            mDefUserStatus.setSscodeerrorcount(0);
                            return true;
                        } else {
                            // 登入帳號尚在鎖定期間
                            addError(safeSettings.getCulture(), SAFEMessageId.AccountLockoutDuration);
                            addLog(currentUser.getUserid(), EnumAuditType.ResetPassword.getValue(), mLogonIp, false, SAFEMessageId.AccountLockoutDuration.toString());
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
            } else {
                return true;
            }

        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * 檢核計數器是否需重設為 0 次
     *
     * @return
     * @throws Exception
     */
    private boolean checkResetLockoutThreshold() throws Exception {
        try {
            if (mDefUserStatus.getSscodeerrorcount() > 0) {
                if (mDefUserStatus.getFailedpswattempttime() != null) {
                    // 系統可以自動解除鎖定，判斷是否已超過帳戶鎖定期間
                    // 上次登入系統失敗時間 + 重設帳戶鎖定計數器的時間 < Now => 計數器自動重設為 0 次
                    int counterAfter = 5;
                    Calendar c = Calendar.getInstance();
                    c.setTime(mDefUserStatus.getFailedpswattempttime());
                    c.add(Calendar.MINUTE, counterAfter);
                    Date attempttime = c.getTime();
                    if (attempttime.before(new Date())) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * Check 密碼
     *
     * @param password
     * @return
     * @throws Exception
     */
    private boolean checkPassword(String password) throws Exception {

        // int passwordEncryption = mMyPolicy.getmPasswordEncryption();
        // int accountLockoutThreshold = mMyPolicy.getmAccountLockoutThreshold();
        //
        String encry = encryptPassword(password, EnumPasswordFormat.values()[mDefUserStatus.getSscodeformat().intValue() - 1]);
        // if (passwordEncryption==-1) {
        // //密碼加密方式不得空白
        // addError(safeSettings.getCulture(), SAFEMessageId.PasswordEncryption);
        // return false;
        // }
        //
        if (mDefUserStatus.getSscode().equals(encry)) {
            return true;
        } else {
            try {
                // 登入帳號或密碼錯誤
                addError(safeSettings.getCulture(), SAFEMessageId.WrongLogOnIdPassword);
                addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), mLogonIp, false, SAFEMessageId.WrongLogOnIdPassword.toString());

                mDefUserStatus.setSscodeerrorcount(mDefUserStatus.getSscodeerrorcount() + 1);
                mDefUserStatus.setFailedpswattempttime(new Date());
                // if(mDefUserStatus.getSscodeerrorcount()>=accountLockoutThreshold) {
                // mDefUserStatus.setIslockout((long)DbHelper.toShort(true));
                // mDefUserStatus.setLastlockouttime(new Date());
                // }

                int rst = userstatusExtMapper.updateByPrimaryKey(mDefUserStatus);
                if (rst > 0) {
                    return false;
                }
            } catch (Exception e) {
                throw e;
            }
            return false;
        }
    }

    private boolean checkPasswordHint(String passwordHintAnswer) throws Exception {

        // int passwordAnswerThreshold = mMyPolicy.getmPasswordAnswerThreshold();
        //
        // if (StringUtils.isBlank(passwordHintAnswer)) {
        // //密碼提示答案錯誤
        // addError(safeSettings.getCulture(), SAFEMessageId.WrongPasswordHintAnswer);
        // return false;
        // }
        //
        // if(passwordHintAnswer.equals(passwordHintAnswer)) {
        // return true;
        // }else {
        // try {
        // //密碼提示答案錯誤
        // addError(safeSettings.getCulture(), SAFEMessageId.WrongPasswordHintAnswer);
        // addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), mLogonIp, false, SAFEMessageId.WrongPasswordHintAnswer.toString());
        //
        // mDefUserStatus.setFailedpswansattemptcount(mDefUserStatus.getFailedpswansattemptcount()+1);
        // mDefUserStatus.setFailedpswattempttime(new Date());
        // if(mDefUserStatus.getFailedpswansattemptcount()>=passwordAnswerThreshold) {
        // //已超過密碼提示答案錯誤次數，該帳戶已被鎖定
        // addError(safeSettings.getCulture(), SAFEMessageId.PasswordAnswerThreshold);
        // addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), mLogonIp, false, SAFEMessageId.PasswordAnswerThreshold.toString());
        //
        // mDefUserStatus.setIslockout((long)DbHelper.toShort(true));
        // mDefUserStatus.setLastlockouttime(new Date());
        // }
        // int rst = userstatusExtMapper.updateByPrimaryKey(mDefUserStatus);
        // if(rst>0) {
        // return false;
        // }
        // } catch (Exception e) {
        // throw e;
        // }
        // return false;
        // }

        return false;
    }

    /**
     * 檢核密碼最後啟動日期(密碼生效日期+新密碼啟動有效期限)
     *
     * @return
     * @throws Exception
     */
    private boolean checkLastActivePasswordDate() throws Exception {
        if (mDefUserStatus.getSscodeeffectdate() != null) {
            int period = 30;
            Calendar c = Calendar.getInstance();
            c.setTime(mDefUserStatus.getSscodeeffectdate());
            c.add(Calendar.DATE, period);
            Date lastPasswordActiveDate = c.getTime();
            if (mDefUserStatus.getLogonchangesscode() > 0 && lastPasswordActiveDate.before(new Date())) {
                // 密碼最後啟動日期已過
                addError(safeSettings.getCulture(), SAFEMessageId.NewPasswordValidPeriod);
                addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), mLogonIp, false, SAFEMessageId.NewPasswordValidPeriod.toString());
                return false;
            }
            return true;
        } else {
            // 密碼生效日期錯誤
            addError(safeSettings.getCulture(), SAFEMessageId.PasswordEffectDateInvalid);
            addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), mLogonIp, false, SAFEMessageId.PasswordEffectDateInvalid.toString());
            return false;
        }

    }

    /**
     * 檢核密碼最後有效日期(上次變更密碼日期+密碼最長使用期限)
     *
     * @return
     * @throws Exception
     */
    private boolean checkNeedChangePassword() throws Exception {

        // if(mDefUserStatus.getLastsscodechangetime()!=null) {
        // int passwordAge = mMyPolicy.getmMaxPasswordAge();
        // //先計算密碼最後有效日期
        // Calendar cExpiredDate = Calendar.getInstance();
        // cExpiredDate.setTime(mDefUserStatus.getLastsscodechangetime());
        // cExpiredDate.add(Calendar.DATE, passwordAge);
        // Date passwordExpiredDate = cExpiredDate.getTime();
        //
        // if(passwordExpiredDate.before(new Date())) {
        // //密碼最後有效日期已過(須變更密碼)
        // addError(safeSettings.getCulture(), SAFEMessageId.MaxPasswordAge);
        // return true;
        // }else {
        //
        // int warningDays = mMyPolicy.getmPasswordExpireWarningDays();
        // Calendar cWarningDays = Calendar.getInstance();
        // cWarningDays.setTime(mDefUserStatus.getLastsscodechangetime());
        // cWarningDays.add(Calendar.DATE, warningDays);
        // Date warningDate = cWarningDays.getTime();
        //
        // if(passwordExpiredDate.before(warningDate)) {
        // //密碼最後有效日期 < Today + 密碼過期警告日數 = > 須做警告
        // addError(safeSettings.getCulture(), SAFEMessageId.PasswordWillExpire);
        // return false;
        // }
        // }
        // return false;
        // } else {
        // //密碼生效日期錯誤
        // addError(safeSettings.getCulture(), SAFEMessageId.LastPasswordChangeTimeInvalid);
        // addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), mLogonIp, false, SAFEMessageId.LastPasswordChangeTimeInvalid.toString());
        // return true;
        // }
        return false;

    }

    /**
     * 取得User & UserStatus
     *
     * @param logOnId
     * @param auditType
     * @param logOnIp
     * @return
     * @throws Exception
     */
    private boolean getCurrentUserData(Integer userId, String logOnId, int auditType, String logOnIp) throws Exception {

        try {
            SyscomuserInfoVo info = userExtMapper.getSyscomuserInfo(logOnId);
            if (info != null) {
                currentUser = userExtMapper.selectByPrimaryKey(info.getUserid());
                mDefUserStatus = userstatusExtMapper.selectByPrimaryKey(info.getUserid());
                if (mDefUserStatus != null) {
                    return true;
                } else {
                    // 無使用者狀態資料
                    addError(safeSettings.getCulture(), SAFEMessageId.WithoutUserStatusData);
                    addLog(currentUser.getUserid(), EnumAuditType.LogOn.getValue(), logOnIp, false, SAFEMessageId.WithoutUserStatusData.toString());
                    return false;
                }
            } else {
                // 無使用者資料
                addError(safeSettings.getCulture(), SAFEMessageId.WithoutUserData);
                addLog(-1, EnumAuditType.LogOn.getValue(), logOnIp, false, SAFEMessageId.WithoutUserData.toString());
                return false;
            }
        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * 記錄日志
     *
     * @param userid
     * @param audittype
     * @param logonip
     * @param flag
     * @param audittarget
     */
    private void addLog(Integer userid, int audittype, String logonip, boolean flag, String audittarget) {
        String logId = UUID.randomUUID().toString();
        Syscomauditlog auditlog = new Syscomauditlog();
        auditlog.setLogid(logId);
        auditlog.setUserid(userid);
        auditlog.setAudittype(audittype);
        auditlog.setUpdatetime(new Date());
        auditlog.setUpdatelogonip(logonip);
        if (flag) {
            auditlog.setResult((short) 1);
        } else {
            auditlog.setResult((short) 0);
        }
        auditlog.setAudittarget(audittarget);
        auditlogExtMapper.insert(auditlog);
    }
}
