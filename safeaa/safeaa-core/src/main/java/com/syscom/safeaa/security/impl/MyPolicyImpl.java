package com.syscom.safeaa.security.impl;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.enums.EnumGetValueType;
import com.syscom.safeaa.mybatis.extmapper.SyscompolicyExtMapper;
import com.syscom.safeaa.mybatis.model.SyscomQueryParentRolesByUserId;
import com.syscom.safeaa.mybatis.model.Syscompolicy;
import com.syscom.safeaa.security.MyPolicy;
import com.syscom.safeaa.utils.DbHelper;
import com.syscom.safeaa.utils.SyscomConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * @author syscom
 */
@Component
public class MyPolicyImpl extends ApplicationBase implements MyPolicy {

    @Autowired
    private SyscomConfig safeSettings;

    @Autowired
    private SyscompolicyExtMapper syscompolicyextMapper;

    private int mAccountLockoutDuration;
    private int mAccountLockoutThreshold;
    private int mMaxAccountLength;
    private int mMinAccountLength;
    private boolean mAccountRequireAlphaNumeric;
    private int mResetAccountLockoutCounterAfter;

    private boolean mAllowLogOnId;
    private boolean mAllowRepeatedCharacters;
    private boolean mAutoCreatePassword;
    private int mEnforcePasswordHistory;
    private int mMaxPasswordAge;
    private int mMaxPasswordLength;
    private int mMinPasswordAge;
    private int mMinPasswordLength;
    private boolean mPasswordComplexity;
    private int mPasswordEncryption;
    private boolean mPasswordRequireAlphaNumeric;
    private boolean mRequireSpecialCharacters;
    private boolean mFirstLogOnChangePassword;
    private int mNewPasswordValidPeriod;
    private boolean mAllowIdNo;
    private boolean mAllowBirthday;
    private int mAllowContinueCharacters;
    private boolean mAllowMultiLogOn;
    private int mPasswordAnswerThreshold;
    private int mPasswordExpireWarningDays;

    @Override
    public void init() {
        initialValue();

        Syscompolicy syscompolicy = new Syscompolicy();
        List<Syscompolicy> list = syscompolicyextMapper.queryAllData();

        for (int i = 0; i < list.size() - 1; i++) {
            syscompolicy = list.get(i);
            String str = syscompolicy.getPolicyno().trim();
            if (StringUtils.equalsIgnoreCase("ACCOUNTLOCKOUTDURATION", str)) {
                mAccountLockoutDuration = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ACCOUNTLOCKOUTTHRESHOLD", str)) {
                mAccountLockoutThreshold = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("MAXACCOUNTLENGTH", str)) {
                mMaxAccountLength = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("MINACCOUNTLENGTH", str)) {
                mMinAccountLength = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ACCOUNTREQUIREALPHANUMERIC", str)) {
                mAccountRequireAlphaNumeric = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("RESETACCOUNTLOCKOUTCOUNTERAFTER", str)) {
                mResetAccountLockoutCounterAfter = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ALLOWLOGONID", str)) {
                mAllowLogOnId = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ALLOWREPEATEDCHARACTERS", str)) {
                mAllowRepeatedCharacters = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("AUTOCREATEPASSWORD", str)) {
                mAutoCreatePassword = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ENFORCEPASSWORDHISTORY", str)) {
                mEnforcePasswordHistory = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("MAXPASSWORDAGE", str)) {
                mMaxPasswordAge = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("MAXPASSWORDLENGTH", str)) {
                mMaxPasswordLength = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("MINPASSWORDAGE", str)) {
                mMinPasswordAge = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("MINPASSWORDLENGTH", str)) {
                mMinPasswordLength = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("PASSWORDCOMPLEXITY", str)) {
                mPasswordComplexity = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("PASSWORDENCRYPTION", str)) {
                mPasswordEncryption = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("PASSWORDREQUIREALPHANUMERIC", str)) {
                mPasswordRequireAlphaNumeric = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("REQUIRESPECIALCHARACTERS", str)) {
                mRequireSpecialCharacters = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("FIRSTLOGONCHANGEPASSWORD", str)) {
                mFirstLogOnChangePassword = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("NEWPASSWORDVALIDPERIOD", str)) {
                mNewPasswordValidPeriod = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ALLOWIDNO", str)) {
                mAllowIdNo = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ALLOWBIRTHDAY", str)) {
                mAllowBirthday = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ALLOWCONTINUECHARACTERS", str)) {
                mAllowContinueCharacters = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("ALLOWMULTILOGON", str)) {
                mAllowMultiLogOn = DbHelper.toBoolean(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("PASSWORDANSWERTHRESHOLD", str)) {
                mPasswordAnswerThreshold = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            } else if (StringUtils.equalsIgnoreCase("PASSWORDEXPIREWARNINGDAYS", str)) {
                mPasswordExpireWarningDays = Integer.parseInt(syscompolicy.getDefaultpolicyvalue().toString());
            }
        }
    }

    @Override
    public void init(Integer userId) {
        init();

        //判斷是否使用管理權限範本
        if (DbHelper.toBoolean(safeSettings.applyTemplate)) {

            List<SyscomQueryParentRolesByUserId> list = syscompolicyextMapper.queryPolicyByUserId(userId);

            for (SyscomQueryParentRolesByUserId item : list) {
                if (StringUtils.isNoneBlank(item.getPolicyValue())
                        && DbHelper.toBoolean(item.getIsGlobalScope().toString())) {
                    compareValue(item.getPolicyNo().trim().toUpperCase(Locale.ENGLISH), item.getPolicyValue().trim(), item.getGetValueType());
                }
            }

            for (SyscomQueryParentRolesByUserId item : list) {
                if (StringUtils.isNoneBlank(item.getPolicyValue())
                        && !DbHelper.toBoolean(item.getIsGlobalScope().toString())) {
                    compareValue(item.getPolicyNo().trim().toUpperCase(Locale.ENGLISH), item.getPolicyValue().trim(), item.getGetValueType());
                }
            }
        }
    }

    private void initialValue() {
        mAccountLockoutDuration = 3;
        mAccountLockoutThreshold = 3;
        mMaxAccountLength = 16;
        mMinAccountLength = 6;
        mAccountRequireAlphaNumeric = false;
        mResetAccountLockoutCounterAfter = 5;

        mAllowLogOnId = true;
        mAllowRepeatedCharacters = true;
        mAutoCreatePassword = false;
        mEnforcePasswordHistory = 1;
        mMaxPasswordAge = 90;
        mMaxPasswordLength = 16;
        mMinPasswordAge = 0;
        mMinPasswordLength = 8;
        mPasswordComplexity = false;
        mPasswordEncryption = 1;
        mPasswordRequireAlphaNumeric = false;
        mRequireSpecialCharacters = false;
        mFirstLogOnChangePassword = true;
        mNewPasswordValidPeriod = 30;
        mAllowIdNo = true;
        mAllowBirthday = true;
        mAllowContinueCharacters = 3;
        mAllowMultiLogOn = false;
        mPasswordAnswerThreshold = 5;
        mPasswordExpireWarningDays = 5;
    }

    private void compareValue(String policyNo, String templatePolicyValue, String getValueType) {
        EnumGetValueType valueType = EnumGetValueType.valueOf(getValueType);
        if (StringUtils.equalsIgnoreCase("ACCOUNTLOCKOUTDURATION", policyNo)) {
            // 帳戶鎖定期間, 取Maximum
            mAccountLockoutDuration = compareInt(mAccountLockoutDuration, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("ACCOUNTLOCKOUTTHRESHOLD", policyNo)) {
            // 帳戶鎖定登入失敗次數, 取Minimum
            mAccountLockoutThreshold = compareInt(mAccountLockoutThreshold, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("MAXACCOUNTLENGTH", policyNo)) {
            // 帳戶長度最大值, 取Minimum
            mMaxAccountLength = compareInt(mMaxAccountLength, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("MINACCOUNTLENGTH", policyNo)) {
            // 帳戶長度最小值, 取Maximum
            mMinAccountLength = compareInt(mMinAccountLength, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("ACCOUNTREQUIREALPHANUMERIC", policyNo)) {
            // 必須包含文字字元
            mAccountRequireAlphaNumeric = compareBool(mAccountRequireAlphaNumeric, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("RESETACCOUNTLOCKOUTCOUNTERAFTER", policyNo)) {
            // 重設帳戶鎖定計數器的時間, 取Maximum
            mResetAccountLockoutCounterAfter = compareInt(mResetAccountLockoutCounterAfter, templatePolicyValue,
                    valueType);
        } else if (StringUtils.equalsIgnoreCase("ALLOWLOGONID", policyNo)) {
            // 密碼允許包含登入帳號
            mAllowLogOnId = compareBool(mAllowLogOnId, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("ALLOWREPEATEDCHARACTERS", policyNo)) {
            // 密碼允許重複字元
            mAllowRepeatedCharacters = compareBool(mAllowRepeatedCharacters, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("AUTOCREATEPASSWORD", policyNo)) {
            // 系統自動產生密碼
            mAutoCreatePassword = compareBool(mAutoCreatePassword, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("ENFORCEPASSWORDHISTORY", policyNo)) {
            // 強制執行密碼歷程記錄, 取Maximum
            mEnforcePasswordHistory = compareInt(mEnforcePasswordHistory, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("MAXPASSWORDAGE", policyNo)) {
            // 密碼最長使用期限, 取Minimum
            mMaxPasswordAge = compareInt(mMaxPasswordAge, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("MAXPASSWORDLENGTH", policyNo)) {
            // 密碼長度最大值, 取Minimum
            mMaxPasswordLength = compareInt(mMaxPasswordLength, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("MINPASSWORDAGE", policyNo)) {
            // 密碼最短使用期限, 取Minimum
            mMinPasswordAge = compareInt(mMinPasswordAge, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("MINPASSWORDLENGTH", policyNo)) {
            // 密碼長度最小值, 取Maximum
            mMinPasswordLength = compareInt(mMinPasswordLength, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("PASSWORDCOMPLEXITY", policyNo)) {
            // 密碼必須符合複雜性需求
            mPasswordComplexity = compareBool(mPasswordComplexity, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("PASSWORDENCRYPTION", policyNo)) {
            // 密碼加密方式, 取Minimum
            mPasswordEncryption = compareInt(mPasswordEncryption, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("PASSWORDREQUIREALPHANUMERIC", policyNo)) {
            // 必須包含文字字元
            mPasswordRequireAlphaNumeric = compareBool(mPasswordRequireAlphaNumeric, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("REQUIRESPECIALCHARACTERS", policyNo)) {
            // 必須包含特殊字元
            mRequireSpecialCharacters = compareBool(mRequireSpecialCharacters, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("FIRSTLOGONCHANGEPASSWORD", policyNo)) {
            // 首次登入是否需變更密碼
            mFirstLogOnChangePassword = compareBool(mFirstLogOnChangePassword, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("NEWPASSWORDVALIDPERIOD", policyNo)) {
            // 新密碼啟動有效期限取, Minimum
            mNewPasswordValidPeriod = compareInt(mNewPasswordValidPeriod, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("ALLOWIDNO", policyNo)) {
            // 密碼允許包含身分證號
            mAllowIdNo = compareBool(mAllowIdNo, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("ALLOWBIRTHDAY", policyNo)) {
            // 密碼允許包含出生日期
            mAllowBirthday = compareBool(mAllowBirthday, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("ALLOWCONTINUECHARACTERS", policyNo)) {
            // 密碼允許連續字元數, Minimum
            mAllowContinueCharacters = compareInt(mAllowContinueCharacters, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("ALLOWMULTILOGON", policyNo)) {
            // 帳戶允許重複登入
            mAllowMultiLogOn = compareBool(mAllowMultiLogOn, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("PASSWORDANSWERTHRESHOLD", policyNo)) {
            // 帳戶鎖定密碼提示答案錯誤累積次數, 取Minimum
            mPasswordAnswerThreshold = compareInt(mPasswordAnswerThreshold, templatePolicyValue, valueType);
        } else if (StringUtils.equalsIgnoreCase("PASSWORDEXPIREWARNINGDAYS", policyNo)) {
            // 密碼過期警告日數, 取Minimum
            mPasswordExpireWarningDays = compareInt(mPasswordExpireWarningDays, templatePolicyValue, valueType);
        }
    }

    private int compareInt(int currentValue, String templatePolicyValue, EnumGetValueType valueType) {
        int tempValue = Integer.parseInt(templatePolicyValue);
        switch (valueType) {
            case Maximum:
                if (tempValue > currentValue) {
                    return tempValue;
                }
                return currentValue;
            case Minimum:
                if (tempValue < currentValue) {
                    return tempValue;
                }
                return currentValue;
            case Last:
                return Integer.parseInt(templatePolicyValue);
            default:
                return currentValue;
        }
    }

    private boolean compareBool(boolean currentValue, String templatePolicyValue, EnumGetValueType valueType) {
        switch (valueType) {
            case Maximum:
                if (DbHelper.toBoolean(templatePolicyValue)) {
                    return true;
                }
                return currentValue;
            case Minimum:
                if (!DbHelper.toBoolean(templatePolicyValue)) {
                    return false;
                }
                return currentValue;
            case Last:
                return DbHelper.toBoolean(templatePolicyValue);
            default:
                return currentValue;
        }
    }

    @Override
    public boolean ismAllowMultiLogOn() {
        return mAllowMultiLogOn;
    }

    @Override
    public void setmAllowMultiLogOn(boolean mAllowMultiLogOn) {
        this.mAllowMultiLogOn = mAllowMultiLogOn;
    }

    @Override
    public boolean ismAllowLogOnId() {
        return mAllowLogOnId;
    }

    @Override
    public void setmAllowLogOnId(boolean mAllowLogOnId) {
        this.mAllowLogOnId = mAllowLogOnId;
    }

    @Override
    public boolean ismAllowRepeatedCharacters() {
        return mAllowRepeatedCharacters;
    }

    @Override
    public void setmAllowRepeatedCharacters(boolean mAllowRepeatedCharacters) {
        this.mAllowRepeatedCharacters = mAllowRepeatedCharacters;
    }

    @Override
    public boolean ismAutoCreatePassword() {
        return mAutoCreatePassword;
    }

    @Override
    public void setmAutoCreatePassword(boolean mAutoCreatePassword) {
        this.mAutoCreatePassword = mAutoCreatePassword;
    }

    @Override
    public int getmEnforcePasswordHistory() {
        return mEnforcePasswordHistory;
    }

    @Override
    public void setmEnforcePasswordHistory(int mEnforcePasswordHistory) {
        this.mEnforcePasswordHistory = mEnforcePasswordHistory;
    }

    @Override
    public int getmMaxPasswordAge() {
        return mMaxPasswordAge;
    }

    @Override
    public void setmMaxPasswordAge(int mMaxPasswordAge) {
        this.mMaxPasswordAge = mMaxPasswordAge;
    }

    @Override
    public int getmMaxPasswordLength() {
        return mMaxPasswordLength;
    }

    @Override
    public void setmMaxPasswordLength(int mMaxPasswordLength) {
        this.mMaxPasswordLength = mMaxPasswordLength;
    }

    @Override
    public int getmMinPasswordAge() {
        return mMinPasswordAge;
    }

    @Override
    public void setmMinPasswordAge(int mMinPasswordAge) {
        this.mMinPasswordAge = mMinPasswordAge;
    }

    @Override
    public int getmMinPasswordLength() {
        return mMinPasswordLength;
    }

    @Override
    public void setmMinPasswordLength(int mMinPasswordLength) {
        this.mMinPasswordLength = mMinPasswordLength;
    }

    @Override
    public boolean ismPasswordComplexity() {
        return mPasswordComplexity;
    }

    @Override
    public void setmPasswordComplexity(boolean mPasswordComplexity) {
        this.mPasswordComplexity = mPasswordComplexity;
    }

    @Override
    public int getmPasswordEncryption() {
        return mPasswordEncryption;
    }

    @Override
    public void setmPasswordEncryption(int mPasswordEncryption) {
        this.mPasswordEncryption = mPasswordEncryption;
    }

    @Override
    public boolean ismPasswordRequireAlphaNumeric() {
        return mPasswordRequireAlphaNumeric;
    }

    @Override
    public void setmPasswordRequireAlphaNumeric(boolean mPasswordRequireAlphaNumeric) {
        this.mPasswordRequireAlphaNumeric = mPasswordRequireAlphaNumeric;
    }

    @Override
    public boolean ismRequireSpecialCharacters() {
        return mRequireSpecialCharacters;
    }

    @Override
    public void setmRequireSpecialCharacters(boolean mRequireSpecialCharacters) {
        this.mRequireSpecialCharacters = mRequireSpecialCharacters;
    }

    @Override
    public boolean ismFirstLogOnChangePassword() {
        return mFirstLogOnChangePassword;
    }

    @Override
    public void setmFirstLogOnChangePassword(boolean mFirstLogOnChangePassword) {
        this.mFirstLogOnChangePassword = mFirstLogOnChangePassword;
    }

    @Override
    public int getmNewPasswordValidPeriod() {
        return mNewPasswordValidPeriod;
    }

    @Override
    public void setmNewPasswordValidPeriod(int mNewPasswordValidPeriod) {
        this.mNewPasswordValidPeriod = mNewPasswordValidPeriod;
    }

    @Override
    public boolean ismAllowIdNo() {
        return mAllowIdNo;
    }

    @Override
    public void setmAllowIdNo(boolean mAllowIdNo) {
        this.mAllowIdNo = mAllowIdNo;
    }

    @Override
    public boolean ismAllowBirthday() {
        return mAllowBirthday;
    }

    @Override
    public void setmAllowBirthday(boolean mAllowBirthday) {
        this.mAllowBirthday = mAllowBirthday;
    }

    @Override
    public int getmAllowContinueCharacters() {
        return mAllowContinueCharacters;
    }

    @Override
    public void setmAllowContinueCharacters(int mAllowContinueCharacters) {
        this.mAllowContinueCharacters = mAllowContinueCharacters;
    }

    @Override
    public int getmPasswordAnswerThreshold() {
        return mPasswordAnswerThreshold;
    }

    @Override
    public void setmPasswordAnswerThreshold(int mPasswordAnswerThreshold) {
        this.mPasswordAnswerThreshold = mPasswordAnswerThreshold;
    }

    @Override
    public int getmPasswordExpireWarningDays() {
        return mPasswordExpireWarningDays;
    }

    @Override
    public void setmPasswordExpireWarningDays(int mPasswordExpireWarningDays) {
        this.mPasswordExpireWarningDays = mPasswordExpireWarningDays;
    }

    @Override
    public int getmMaxAccountLength() {
        return mMaxAccountLength;
    }

    @Override
    public void setmMaxAccountLength(int mMaxAccountLength) {
        this.mMaxAccountLength = mMaxAccountLength;
    }

    @Override
    public int getmMinAccountLength() {
        return mMinAccountLength;
    }

    @Override
    public void setmMinAccountLength(int mMinAccountLength) {
        this.mMinAccountLength = mMinAccountLength;
    }

    @Override
    public boolean ismAccountRequireAlphaNumeric() {
        return mAccountRequireAlphaNumeric;
    }

    @Override
    public void setmAccountRequireAlphaNumeric(boolean mAccountRequireAlphaNumeric) {
        this.mAccountRequireAlphaNumeric = mAccountRequireAlphaNumeric;
    }

    @Override
    public int getmAccountLockoutDuration() {
        return mAccountLockoutDuration;
    }

    @Override
    public void setmAccountLockoutDuration(int mAccountLockoutDuration) {
        this.mAccountLockoutDuration = mAccountLockoutDuration;
    }

    @Override
    public int getmAccountLockoutThreshold() {
        return mAccountLockoutThreshold;
    }

    @Override
    public void setmAccountLockoutThreshold(int mAccountLockoutThreshold) {
        this.mAccountLockoutThreshold = mAccountLockoutThreshold;
    }

    @Override
    public int getmResetAccountLockoutCounterAfter() {
        return mResetAccountLockoutCounterAfter;
    }

    @Override
    public void setmResetAccountLockoutCounterAfter(int mResetAccountLockoutCounterAfter) {
        this.mResetAccountLockoutCounterAfter = mResetAccountLockoutCounterAfter;
    }
}
