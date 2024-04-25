package com.syscom.safeaa.security.impl;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.configuration.DataSourceSafeaaConstant;
import com.syscom.safeaa.constant.CommonConstants;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.extmapper.SyscompolicyExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscompolicycultureExtMapper;
import com.syscom.safeaa.mybatis.model.Syscompolicy;
import com.syscom.safeaa.mybatis.model.Syscompolicyculture;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllPolicysVo;
import com.syscom.safeaa.security.Serial;
import com.syscom.safeaa.security.Policy;
import com.syscom.safeaa.utils.SyscomConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
public class PolicyImpl extends ApplicationBase implements Policy {

    private int mCurrentUserId;

    @Autowired
    private Serial serialService;

    @Autowired
    private SyscomConfig SAFESettings;

    @Autowired
    private SyscompolicyExtMapper syscompolicyMapper;

//    @Autowired
//    private SyscomConfig safeSettings;

    @Autowired
    private SyscompolicycultureExtMapper syscompolicycultureExtMapper;

    public PolicyImpl() {
    }

    // 1. SAFE will update data column UpateUserId of relative tables when pass in userId.
    // 2. SAFE will insert SyscomAuditLog and SyscomAuditTrail when pass in userId.
    public PolicyImpl(int userId) {
        mCurrentUserId = userId;
    }


    /**
     * Create one new SyscomPolicy record.
     * The count of successful insert records,
     * if return -1 Then represent insert fail or parameter passing error.
     * 建立一筆管理原則資料
     * @param syscompolicy
     * @return
     */
    @Transactional(transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
    @Override
    public int createPolicy(Syscompolicy syscompolicy) throws SafeaaException {
        // 管理原則代碼不得空白
        if (syscompolicy.getPolicyno() == null || "".equals(syscompolicy.getPolicyno())){
            addError(SAFESettings.getCulture(), SAFEMessageId.EmptyPolicyNo);
        }
        try{
            syscompolicy.setUpdateuserid(mCurrentUserId);
            // 若未傳入管理原則序號則自取序號
            if (syscompolicy.getPolicyid() == null || syscompolicy.getPolicyid() == 0){
                Long nextId = serialService.getNextId(CommonConstants.SerialName);
                syscompolicy.setPolicyid(nextId.intValue());
            }

            int mResult = syscompolicyMapper.insertSelective(syscompolicy);
            if (mResult <= 0){
                addError(SAFESettings.getCulture(), SAFEMessageId.InsertMasterFail);
            }
            return mResult;
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * Modify one SyscomPolicy record.
     * The count of successful updated records,
     * if return -1 Then represent update fail or parameter passing error.
     * If PolicyId has no value, Then find PolicyId by PolicyNo first.<br/>
     * If PolicyId has value, Then update table directly.
     * 修改一筆管理原則資料
     * 若PolicyId有值則直接Update，若沒有則判斷PolicyNo是否有值
     * 並以PolicyNo取得PolicyId再以UpdateByPrimaryKey更新
     * @param syscompolicy
     * @return
     * @throws SafeaaException
     */
    @Transactional(transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
    @Override
    public int updatePolicy(Syscompolicy syscompolicy) throws SafeaaException {
        try{
            if (syscompolicy.getPolicyid() != null && syscompolicy.getPolicyid() != 0){
                syscompolicy.setUpdateuserid(mCurrentUserId);
                syscompolicy.setUpdatetime(new Date());
                int mResult = syscompolicyMapper.updateByPrimaryKeySelective(syscompolicy);
                if (mResult <= 0){
                    addError(SAFESettings.getCulture(), SAFEMessageId.UpdateNoRecord);
                }
                return mResult;
            }
            else{
                if (syscompolicy.getPolicyid() == null || syscompolicy.getPolicyid() == 0){

                    List<Syscompolicy> list = syscompolicyMapper.queryPolicyByNo(syscompolicy.getPolicyno());
                    if (list.get(0).getPolicyid() > 0){
                        syscompolicy.setUpdateuserid(mCurrentUserId);
                        syscompolicy.setUpdatetime(new Date());
                        int mResult = syscompolicyMapper.updateByPrimaryKeySelective(syscompolicy);
                        if (mResult <= 0){
                            addError(SAFESettings.getCulture(), SAFEMessageId.UpdateNoRecord);
                        }
                        return mResult;
                    }else{
                        // 無此管理原則資料
                        addError(SAFESettings.getCulture(), SAFEMessageId.WithoutPolicyData);
                        return -1;
                    }
                }
                else {
                    // 管理原則代碼不得空白
                    addError(SAFESettings.getCulture(), SAFEMessageId.EmptyPolicyNo);
                    return -1;
                }
            }

        }catch (Exception e){
            throw e;
        }
    }

    /**
     * Delete one SyscomPolicy record and its relative culture data and member data.
     * policy sequence no
     * True:delete successful False: delete fail
     * Update db need transaction process because delete culture data simulate.
     * 以管理原則序號刪除一筆管理原則及其相關的語系資料及成員資料
     * ͬ同時刪除語系資料所以要包Transaction
     *
     * @param policyId
     * @return
     * @throws SafeaaException
     */
    @Transactional(transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
    @Override
    public boolean deletePolicy(int policyId) throws SafeaaException {
        Syscompolicy syscompolicy = new Syscompolicy();
        syscompolicy.setPolicyid(policyId);
        if (syscompolicyMapper.deleteByPrimaryKey(syscompolicy) > 0){
            return true;
        }
        else{
            addError(SAFESettings.getCulture(), SAFEMessageId.DeletMasterFail);
            return false;
        }
    }

    /**
     * To get one policy record by using PolicyId.
     * if return -1 Then represent query fail or parameter passing error.
     * 以管理原則代碼(PolicyNo)查出該筆管理原則的管理原則序號
     *
     * @param policyNo
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<Syscompolicy> getPolicyIdByNo(String policyNo) throws SafeaaException {
        try{
            if (StringUtils.isBlank(policyNo)){
                // 管理原則序號未傳入
                addError(SAFESettings.getCulture(), SAFEMessageId.EmptyPolicyNo);
                return null;
            }

            List<Syscompolicy> list = syscompolicyMapper.queryPolicyByNo(policyNo);
            //Syscompolicy syscompolicy = new Syscompolicy();
            if (list != null && list.size() > 0){
                return list;
            }else{
                addError(SAFESettings.getCulture(), SAFEMessageId.QueryNoRecord);
                return null;
            }

        }catch (Exception e){
            throw e;

        }
    }

    /**
     * To get one policy record by using PolicyId.
     * if return -1 Then represent query fail or parameter passing error.
     * 以管理原則代碼(PolicyNo)查出該筆管理原則的管理原則序號
     *
     * @param syscompolicy
     * @return
     * @throws SafeaaException
     */
    @Override
    public Syscompolicy getPolicyById(Syscompolicy syscompolicy) throws SafeaaException {
        try{
            if (syscompolicy.getPolicyid() == null || syscompolicy.getPolicyid() == 0){
                // 管理原則代碼不得空白
                addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
                return null;
            }

            Syscompolicy policy = syscompolicyMapper.selectByPrimaryKey(syscompolicy.getPolicyid());
            if (policy != null && !policy.equals(new Syscompolicy())){
                return syscompolicy;
            }else{
                addError(SAFESettings.getCulture(), SAFEMessageId.QueryNoRecord);
                return null;
            }

        }catch (Exception e){
            throw e;

        }
    }

    /**
     * To get one policy record by using PolicyId.
     * if return -1 Then represent query fail or parameter passing error.
     * 以管理原則序號(PolicyId)查出一筆管理原則資料
     *
     * @param syscompolicy
     * @param syscompolicyculture
     * @return
     * @throws SafeaaException
     */
    @Override
    public Syscompolicy getPolicyById(Syscompolicy syscompolicy, Syscompolicyculture syscompolicyculture) throws SafeaaException {
        try{
            if (syscompolicy.getPolicyid() == null || syscompolicy.getPolicyid() == 0){
                // 管理原則序號未傳入
                addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
                return null;
            }

            if (StringUtils.isBlank(syscompolicyculture.getCulture())){
                syscompolicyculture.setCulture(SAFESettings.getCulture());
                return null;
            }

            Syscompolicy policy = syscompolicyMapper.selectByPrimaryKey(syscompolicy.getPolicyid());
            if (policy != null && !policy.equals(new Syscompolicy())){
                return syscompolicy;
            }else{
                addError(SAFESettings.getCulture(), SAFEMessageId.QueryNoRecord);
                return null;
            }

        }catch (Exception e){
            throw e;

        }
    }

    /**
     * To get all policys and relative culture data by using culture code.
     * 1. Do not check valid date from EffectDate and ExpireDate.
     * 2. If It have no culture code pass then system configuration culture data will return.
     * 以傳入語系查出所有管理原則資料(含語系資料)
     *
     * @param culture
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<SyscomQueryAllPolicysVo> getAllPolicys(String culture) throws SafeaaException {

        if (StringUtils.isBlank(culture)){
            culture = SAFESettings.culture;
        }

        return syscompolicyMapper.queryAllPolicys(culture, null, null, null);
    }

    /**
     * To get one policy and relative culture data by using PolicyId.
     * 以管理原則序號(PolicyId)查出一筆管理原則資料(含語系資料)
     *
     * @param culture
     * @param policyId
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<SyscomQueryAllPolicysVo> getPolicyDataById(String culture, Integer policyId) throws SafeaaException {
        if (policyId == null || policyId ==0){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return null;
        }

        if (StringUtils.isBlank(culture)){
            culture = SAFESettings.culture;
        }
        return syscompolicyMapper.queryAllPolicys(culture, policyId, null, null);
    }

    /**
     * To get all policys and relative culture data by using policy name.
     * all policy data and relative culture data.
     * 以管理原則名稱查出管理原則資料(含語系資料)
     * 1. 不判斷有效日期(含未生效及停用資料)
     * 2. 若不傳入語系代碼，則回傳系統預設語系資料
     * 3. 以Like方式查詢
     *
     * @param culture
     * @param policyName
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<SyscomQueryAllPolicysVo> getPolicyDataByName(String culture, String policyName) throws SafeaaException {
        if (StringUtils.isBlank(policyName)){
            // 管理原則名稱不得空白
            addError(SAFESettings.getCulture(), SAFEMessageId.EmptyPolicyName);
            return null;
        }
        if (StringUtils.isBlank(culture)){
            culture = SAFESettings.culture;
        }
        return syscompolicyMapper.queryAllPolicys(culture, null, policyName, null);
    }

    /**
     * To get one policy and relative culture data by using PolicyNo.
     *  以管理原則代碼(PolicyNo)查出一筆管理原則資料(含語系資料)
     * @param culture
     * @param policyNo
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<SyscomQueryAllPolicysVo> getPolicyDataByNo(String culture, String policyNo) throws SafeaaException {
        if (StringUtils.isBlank(policyNo)){
            // 管理原則代碼不得空白
            addError(SAFESettings.getCulture(), SAFEMessageId.EmptyPolicyNo);
            return null;
        }
        if (StringUtils.isBlank(culture)){
            culture = SAFESettings.culture;
        }
        return syscompolicyMapper.queryAllPolicys(culture, null, null, policyNo);
    }

    /**
     * Create one new SyscomPolicyCulture record.
     * 建立一筆管理原則語系資料(傳入語系物件)
     * 語系若不傳入會以系統預設語系處理
     *
     * @param syscompolicyculture
     * @return
     * @throws SafeaaException
     */
    @Override
    public int addPolicyCulture(Syscompolicyculture syscompolicyculture) throws SafeaaException {
        if (syscompolicyculture.getPolicyid() == null){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return -1;
        }
        if (StringUtils.isBlank(syscompolicyculture.getCulture())){
            syscompolicyculture.setCulture(SAFESettings.culture);
        }
        try{
            int mResult = syscompolicycultureExtMapper.insertSelective(syscompolicyculture);
            if (mResult <= 0){
                addError(SAFESettings.getCulture(), SAFEMessageId.InsertCultureFail);
            }
            return mResult;

        }catch (Exception e){
            throw e;
        }
    }

    /**
     * Create one new SyscomPolicyCulture record.
     * 建立一筆管理原則語系資料(傳入語系資料)
     * 語系若不傳入會以系統預設語系處理
     *
     * @param policyNo
     * @param culture
     * @param policyName
     * @param remark
     * @return
     * @throws SafeaaException
     */
    @Override
    public int addPolicyCulture(String policyNo, String culture, String policyName, String remark) throws SafeaaException {
        if (StringUtils.isBlank(policyNo)){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return -1;
        }
        if (StringUtils.isBlank(culture)){
            culture = SAFESettings.getCulture();
        }

        List<Syscompolicy> list = syscompolicyMapper.queryPolicyByNo(policyNo);
        Syscompolicyculture syscompolicyculture = new Syscompolicyculture();
        if (list.size() > 0){
            syscompolicyculture.setPolicyid(list.get(0).getPolicyid());
            syscompolicyculture.setCulture(culture);
            syscompolicyculture.setPolicyname(policyName);
            syscompolicyculture.setRemark(remark);
            int mResult = syscompolicycultureExtMapper.insertSelective(syscompolicyculture);
            if (mResult <= 0){
                addError(SAFESettings.getCulture(), SAFEMessageId.InsertCultureFail);
            }
            return mResult;
        }
        else{
            // 無此管理原則資料
            addError(SAFESettings.getCulture(), SAFEMessageId.WithoutPolicyData);
            return -1;
        }
    }

    /**
     * Update one SyscomPolicyCulture record.
     * 修改一筆管理原則語系資料(傳入語系資料)
     * 語系若不傳入會以系統預設語系處理
     *
     * @param syscompolicyculture
     * @return
     * @throws SafeaaException
     */
    @Override
    public int updatePolicyCulture(Syscompolicyculture syscompolicyculture) throws SafeaaException {
        if (syscompolicyculture.getPolicyid() == null){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return -1;
        }
        if (StringUtils.isBlank(syscompolicyculture.getCulture())){
            syscompolicyculture.setCulture(SAFESettings.culture);
        }
        try{
            int mResult = syscompolicycultureExtMapper.updateByPrimaryKeySelective(syscompolicyculture);
            if (mResult <= 0){
                addError(SAFESettings.getCulture(), SAFEMessageId.UpdateNoRecord);
            }
            return mResult;

        }catch (Exception e){
            throw e;
        }
    }

    /**
     * Update one SyscomPolicyCulture record.
     * 修改一筆管理原則語系資料(傳入語系資料)
     * 1. 先取得PolicyId再更新資料
     * 2. 語系若不傳入會以系統預設語系處理
     *
     * @param policyNo
     * @param culture
     * @param policyName
     * @param remark
     * @return
     * @throws SafeaaException
     */
    @Override
    public int updatePolicyCulture(String policyNo, String culture, String policyName, String remark) throws SafeaaException {
        if (StringUtils.isBlank(policyNo)){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return -1;
        }
        if (StringUtils.isBlank(culture)){
            culture = SAFESettings.getCulture();
        }

        List<Syscompolicy> list = syscompolicyMapper.queryPolicyByNo(policyNo);
        Syscompolicyculture syscompolicyculture = new Syscompolicyculture();
        if (list.size() > 0){
            syscompolicyculture.setPolicyid(list.get(0).getPolicyid());
            syscompolicyculture.setCulture(culture);
            syscompolicyculture.setPolicyname(policyName);
            syscompolicyculture.setRemark(remark);
            int mResult = syscompolicycultureExtMapper.updateByPrimaryKeySelective(syscompolicyculture);
            if (mResult <= 0){
                addError(SAFESettings.getCulture(), SAFEMessageId.InsertCultureFail);
            }
            return mResult;
        }
        else{
            // 無此管理原則資料
            addError(SAFESettings.getCulture(), SAFEMessageId.WithoutPolicyData);
            return -1;
        }
    }

    /**
     * Delete one designate SyscomPolicyCulture record.
     * 以管理原則序號刪除一筆管理原則語系資料(指定語系)
     * 語系若不傳入會以系統預設語系處理
     *
     * @param policyId
     * @param culture
     * @return
     * @throws SafeaaException
     */
    @Override
    public int removePolicyCulture(Integer policyId, String culture) throws SafeaaException {
        if (policyId == null){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return -1;
        }
        if (StringUtils.isBlank(culture)){
            culture = SAFESettings.getCulture();
        }

        try{
            Syscompolicyculture syscompolicyculture = new Syscompolicyculture();
            syscompolicyculture.setPolicyid(policyId);
            syscompolicyculture.setCulture(culture);

            int mResult = syscompolicycultureExtMapper.deleteByPrimaryKey(syscompolicyculture);
            if (mResult <= 0){
                addError(SAFESettings.getCulture(), SAFEMessageId.DeleteNoRecord);
            }
            return mResult;
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * To delete all SyscomPolicyCulture records by using PolicyId .
     * 以管理原則序號刪除該管理原則的所有語系資料
     *
     * @param policyId
     * @return
     * @throws SafeaaException
     */
    @Override
    public int removeAllPolicyCultures(Integer policyId) throws SafeaaException {
        if (policyId == null){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return -1;
        }

        try{

            int mResult = syscompolicycultureExtMapper.deleteAllByPolicyId(policyId);
            if (mResult <= 0){
                addError(SAFESettings.getCulture(), SAFEMessageId.DeleteNoRecord);
            }
            return mResult;
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * To get one SyscomPolicyCulture record by using PolicyId and culture code.
     * 以管理原則序號(PolicyId)及語系查出一筆管理原則語系資料
     *
     * @param syscompolicyculture
     * @return
     * @throws SafeaaException
     */
    @Override
    public Syscompolicyculture getPolicyCultureById(Syscompolicyculture syscompolicyculture) throws SafeaaException {
        if (syscompolicyculture.getPolicyid() == null){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return null;
        }
        if (StringUtils.isBlank(syscompolicyculture.getCulture())){
            syscompolicyculture.setCulture(SAFESettings.culture);
        }
        try{
            Syscompolicyculture mResult = syscompolicycultureExtMapper.selectByPrimaryKey(syscompolicyculture.getPolicyid(),syscompolicyculture.getCulture());
            if (mResult == null){
                addError(SAFESettings.getCulture(), SAFEMessageId.QueryNoRecord);
            }
            return mResult;

        }catch (Exception e){
            throw e;
        }
    }

    /**
     * To get all SyscomPolicyCulture records by using PolicyId.
     * 以管理原則序號(PolicyId)查出該筆管理原則的所有語系資料
     *
     * @param policyId
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<Syscompolicyculture> getPolicyCulturesById(Integer policyId) throws SafeaaException {
        if (policyId == null){
            // 管理原則序號未傳入
            addError(SAFESettings.getCulture(), SAFEMessageId.LostPolicyId);
            return null;
        }
        try{
            List<Syscompolicyculture> list = syscompolicycultureExtMapper.queryAllByPolicyId(policyId);
            return list;
        }catch (Exception e){
            throw e;
        }
    }


}
