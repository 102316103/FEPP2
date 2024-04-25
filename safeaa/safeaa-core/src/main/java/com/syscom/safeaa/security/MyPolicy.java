package com.syscom.safeaa.security;

import com.syscom.safeaa.core.BaseInterface;



/**
 *
 * @author syscom
 */
public interface MyPolicy
{
    /**
     *
     */
    public void init();

    /**
     *
     * @param userId
     */
    public void init(Integer userId);

    /**
     *
     * @return
     */
    public boolean ismAllowMultiLogOn();

    /**
     *
     * @param mAllowMultiLogOn
     */
    public void setmAllowMultiLogOn(boolean mAllowMultiLogOn);

    public boolean ismAllowLogOnId();

    public void setmAllowLogOnId(boolean mAllowLogOnId);

    public boolean ismAllowRepeatedCharacters();

    public void setmAllowRepeatedCharacters(boolean mAllowRepeatedCharacters);

    public boolean ismAutoCreatePassword();

    public void setmAutoCreatePassword(boolean mAutoCreatePassword);

    public int getmEnforcePasswordHistory();

    public void setmEnforcePasswordHistory(int mEnforcePasswordHistory);

    public int getmMaxPasswordAge();

    public void setmMaxPasswordAge(int mMaxPasswordAge);

    public int getmMaxPasswordLength();

    public void setmMaxPasswordLength(int mMaxPasswordLength);

    public int getmMinPasswordAge();

    public void setmMinPasswordAge(int mMinPasswordAge) ;

    public int getmMinPasswordLength();

    public void setmMinPasswordLength(int mMinPasswordLength);

    public boolean ismPasswordComplexity();

    public void setmPasswordComplexity(boolean mPasswordComplexity);

    public int getmPasswordEncryption();

    public void setmPasswordEncryption(int mPasswordEncryption);

    public boolean ismPasswordRequireAlphaNumeric();

    public void setmPasswordRequireAlphaNumeric(boolean mPasswordRequireAlphaNumeric);

    public boolean ismRequireSpecialCharacters();

    public void setmRequireSpecialCharacters(boolean mRequireSpecialCharacters);

    public boolean ismFirstLogOnChangePassword();

    public void setmFirstLogOnChangePassword(boolean mFirstLogOnChangePassword);

    public int getmNewPasswordValidPeriod();

    public void setmNewPasswordValidPeriod(int mNewPasswordValidPeriod);

    public boolean ismAllowIdNo();

    public void setmAllowIdNo(boolean mAllowIdNo);

    public boolean ismAllowBirthday();

    public void setmAllowBirthday(boolean mAllowBirthday);

    public int getmAllowContinueCharacters();

    public void setmAllowContinueCharacters(int mAllowContinueCharacters);

    public int getmPasswordAnswerThreshold() ;

    public void setmPasswordAnswerThreshold(int mPasswordAnswerThreshold);

    public int getmPasswordExpireWarningDays();

    public void setmPasswordExpireWarningDays(int mPasswordExpireWarningDays);

    public int getmMaxAccountLength();

    public void setmMaxAccountLength(int mMaxAccountLength);

    public int getmMinAccountLength();

    public void setmMinAccountLength(int mMinAccountLength);

    public boolean ismAccountRequireAlphaNumeric();

    public void setmAccountRequireAlphaNumeric(boolean mAccountRequireAlphaNumeric);

    public int getmAccountLockoutDuration();

    public void setmAccountLockoutDuration(int mAccountLockoutDuration);

    public int getmAccountLockoutThreshold();

    public void setmAccountLockoutThreshold(int mAccountLockoutThreshold);

    public int getmResetAccountLockoutCounterAfter();

    public void setmResetAccountLockoutCounterAfter(int mResetAccountLockoutCounterAfter);
}
