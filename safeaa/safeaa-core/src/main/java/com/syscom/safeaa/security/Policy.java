package com.syscom.safeaa.security;


import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.core.BaseInterface;
import com.syscom.safeaa.mybatis.model.Syscompolicy;
import com.syscom.safeaa.mybatis.model.Syscompolicyculture;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllPolicysVo;

import java.util.List;

public interface Policy extends BaseInterface {

    /**
     * Create one new SyscomPolicy record.
     * The count of successful insert records,
     * if return -1 Then represent insert fail or parameter passing error.
     * 建立一筆管理原則資料
     * @param syscompolicy
     * @return
     */
    public int createPolicy(Syscompolicy syscompolicy) throws SafeaaException;

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
    public int updatePolicy(Syscompolicy syscompolicy) throws SafeaaException;

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
    public boolean deletePolicy(int policyId) throws SafeaaException;

    /**
     * To get PolicyId of policy by using PolicyNo.
     * PolicyId : -1 meaning the policyNo is not exist in SyscomPolicy.
     * 以管理原則代碼(PolicyNo)查出該筆管理原則的管理原則序號
     *
     * @param policyNo
     * @return
     * @throws SafeaaException
     */
    public List<Syscompolicy> getPolicyIdByNo(String policyNo) throws SafeaaException;

    /**
     * To get one policy record by using PolicyId.
     * if return -1 Then represent query fail or parameter passing error.
     * 以管理原則代碼(PolicyNo)查出該筆管理原則的管理原則序號
     *
     * @param syscompolicy
     * @return
     * @throws SafeaaException
     */
    public Syscompolicy getPolicyById(Syscompolicy syscompolicy) throws SafeaaException;

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
    public Syscompolicy getPolicyById(Syscompolicy syscompolicy, Syscompolicyculture syscompolicyculture) throws SafeaaException;

    /**
     * To get all policys and relative culture data by using culture code.
     * 1. Do not check valid date from EffectDate and ExpireDate.
     * 2. If It have no culture code pass then system configuration culture data will return.
     * 以傳入語系查出所有管理原則資料(含語系資料)
     * @param culture
     * @return
     * @throws SafeaaException
     */
    public List<SyscomQueryAllPolicysVo> getAllPolicys(String culture) throws SafeaaException;

    /**
     * To get one policy and relative culture data by using PolicyId.
     * 以管理原則序號(PolicyId)查出一筆管理原則資料(含語系資料)
     * @param culture
     * @param policyId
     * @return
     * @throws SafeaaException
     */
    public List<SyscomQueryAllPolicysVo> getPolicyDataById(String culture, Integer policyId) throws SafeaaException;

    /**
     * To get all policys and relative culture data by using policy name.
     * all policy data and relative culture data.
     * 以管理原則名稱查出管理原則資料(含語系資料)
     * 1. 不判斷有效日期(含未生效及停用資料)
     * 2. 若不傳入語系代碼，則回傳系統預設語系資料
     * 3. 以Like方式查詢
     * @param culture
     * @param policyName
     * @return
     * @throws SafeaaException
     */
    public List<SyscomQueryAllPolicysVo> getPolicyDataByName(String culture, String policyName) throws SafeaaException;

    /**
     *
     * @param culture
     * @param policyNo
     * @return
     * @throws SafeaaException
     */
    public List<SyscomQueryAllPolicysVo> getPolicyDataByNo(String culture, String policyNo) throws SafeaaException;

    /**
     * Create one new SyscomPolicyCulture record.
     * 建立一筆管理原則語系資料(傳入語系物件)
     * 語系若不傳入會以系統預設語系處理
     * @param syscompolicyculture
     * @return
     * @throws SafeaaException
     */
    public int addPolicyCulture(Syscompolicyculture syscompolicyculture) throws SafeaaException;

    /**
     * Create one new SyscomPolicyCulture record.
     * 建立一筆管理原則語系資料(傳入語系資料)
     * 語系若不傳入會以系統預設語系處理
     * @param policyNo
     * @param culture
     * @param policyName
     * @param remark
     * @return
     * @throws SafeaaException
     */
    public int addPolicyCulture(String policyNo, String culture, String policyName, String remark) throws SafeaaException;

    /**
     * Update one SyscomPolicyCulture record.
     * 修改一筆管理原則語系資料(傳入語系資料)
     * 語系若不傳入會以系統預設語系處理
     * @param syscompolicyculture
     * @return
     * @throws SafeaaException
     */
    public int updatePolicyCulture(Syscompolicyculture syscompolicyculture) throws SafeaaException;

    /**
     * Update one SyscomPolicyCulture record.
     * 修改一筆管理原則語系資料(傳入語系資料)
     * 1. 先取得PolicyId再更新資料
     * 2. 語系若不傳入會以系統預設語系處理
     * @param policyNo
     * @param culture
     * @param policyName
     * @param remark
     * @return
     * @throws SafeaaException
     */
    public int updatePolicyCulture(String policyNo, String culture, String policyName, String remark) throws SafeaaException;

    /**
     * Delete one designate SyscomPolicyCulture record.
     * 以管理原則序號刪除一筆管理原則語系資料(指定語系)
     * 語系若不傳入會以系統預設語系處理
     * @param policyId
     * @param culture
     * @return
     * @throws SafeaaException
     */
    public int removePolicyCulture(Integer policyId, String culture) throws SafeaaException;

    /**
     * To delete all SyscomPolicyCulture records by using PolicyId .
     * 以管理原則序號刪除該管理原則的所有語系資料
     * @param policyId
     * @return
     * @throws SafeaaException
     */
    public int removeAllPolicyCultures(Integer policyId) throws SafeaaException;

    /**
     * To get one SyscomPolicyCulture record by using PolicyId and culture code.
     * 以管理原則序號(PolicyId)及語系查出一筆管理原則語系資料
     * @param syscompolicyculture
     * @return
     * @throws SafeaaException
     */
    public Syscompolicyculture getPolicyCultureById(Syscompolicyculture syscompolicyculture) throws SafeaaException;

    /**
     * To get all SyscomPolicyCulture records by using PolicyId.
     * 以管理原則序號(PolicyId)查出該筆管理原則的所有語系資料
     * @param policyId
     * @return
     * @throws SafeaaException
     */
    public List<Syscompolicyculture> getPolicyCulturesById(Integer policyId) throws SafeaaException;
}
