package com.syscom.safeaa.security.impl;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserdeputyExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.model.Syscomuserdeputy;
import com.syscom.safeaa.mybatis.vo.SyscomDeputyUserMatrixVo;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllUserDeputyVo;
import com.syscom.safeaa.mybatis.vo.SyscomRoleMembersVo;
import com.syscom.safeaa.mybatis.vo.SyscomrolemembersAndCulture;
import com.syscom.safeaa.security.Role;
import com.syscom.safeaa.security.UserDeputy;
import com.syscom.safeaa.utils.DbHelper;
import com.syscom.safeaa.utils.SyscomConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserDeputyImpl extends ApplicationBase implements UserDeputy {

    @Autowired
    private SyscomConfig safeSettings;

    @Autowired
    private SyscomuserdeputyExtMapper syscomuserdeputyExtMapper;

    @Autowired
    private Role role;

    /**
     * Create one new SyscomUserDeputy record.
     * The count of successful insert records,
     * 增加使用者代理人資料(傳入使用者代理人物件)
     *
     * @param syscomuserdeputy
     * @return
     * @throws SafeaaException
     */
    @Override
    public int addUserDeputy(Syscomuserdeputy syscomuserdeputy) throws SafeaaException {
        if (!DbHelper.toBoolean(syscomuserdeputy.getRoleid().shortValue())){
            // 角色序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
            return -1;
        }
        if (!DbHelper.toBoolean(syscomuserdeputy.getUserid().shortValue())){
            // 成員序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
            return -1;
        }
        if (!DbHelper.toBoolean(syscomuserdeputy.getDeputyuserid().shortValue())){
            // 代理人序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostDeputyUserId);
            return -1;
        }

        try{
            int mResult = syscomuserdeputyExtMapper.insert(syscomuserdeputy);
            if (mResult <= 0){
                addError(safeSettings.getCulture(), SAFEMessageId.InsertMasterFail);
            }
            return mResult;
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * Delete one SyscomUserDeputy record.
     * The count of successful insert records,
     * if return -1 Then represent insert fail or parameter passing error.
     * 以角色序號,成員序號及代理人序號刪除一筆使用者代理人資料
     *
     * @param userId
     * @param roleId
     * @param deputyUserId
     * @return
     * @throws SafeaaException
     */
    @Override
    public int removeUserDeputy(int userId, int roleId, int deputyUserId) throws SafeaaException {
        if (userId == 0){
            // 角色序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostUserId);
            return -1;
        }
        if (roleId == 0){
            // 成員序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
            return -1;
        }
        if (deputyUserId == 0){
            // 代理人序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostDeputyUserId);
            return -1;
        }
        try{
            Syscomuserdeputy syscomuserdeputy = new Syscomuserdeputy();
            syscomuserdeputy.setUserid(userId);
            syscomuserdeputy.setRoleid(roleId);
            syscomuserdeputy.setDeputyuserid(deputyUserId);

            int mResult = syscomuserdeputyExtMapper.deleteByPrimaryKey(syscomuserdeputy);
            if (mResult <= 0){
                addError(safeSettings.getCulture(), SAFEMessageId.InsertMasterFail);
            }
            return mResult;
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * Delete SyscomUserDeputy records of one user.
     * The count of successful insert records,
     * if return -1 Then represent insert fail or parameter passing error.
     * 以使用者序號移除一個使用者的所有代理人資料
     *
     * @param userId
     * @return
     * @throws SafeaaException
     */
    @Override
    public int removeUserDeputysByUserId(int userId) throws SafeaaException {
        if (userId == 0){
            // 使用者序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostUserId);
            return -1;
        }
        try{
            return syscomuserdeputyExtMapper.deleteAllByUserId(userId, null);
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * Delete SyscomUserDeputy records of one role.
     * The count of successful insert records,
     * if return -1 Then represent insert fail or parameter passing error.
     * 以角色序號移除一個角色的所有使用者代理人資料
     *
     * @param roleId
     * @return
     * @throws SafeaaException
     */
    @Override
    public int removeUserDeputysByRoleId(int roleId) throws SafeaaException {
        if (roleId == 0){
            // 角色序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
            return -1;
        }
        try{
            return syscomuserdeputyExtMapper.deleteAllByUserId(null, roleId);
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * To get all selected SyscomUserDeputy records by using UserId.
     * 以使用者序號查出已加入至某使用者代理人的使用者清單
     *
     * @param userId
     * @param orderby
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<SyscomQueryAllUserDeputyVo> getSelectedDeputyByUserId(int userId, String orderby) throws SafeaaException {
        if (userId == 0){
            // 使用者序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostUserId);
            return null;
        }
        try{
            return syscomuserdeputyExtMapper.queryAllUserDeputy(userId,null, orderby);

        }catch (Exception e){
            throw e;
        }
    }

    /**
     * To get all unselected SyscomUserDeputy records by using UserId and RoleId.
     * 查出尚未加入至某使用者角色代理人的使用者清單
     *
     * @param userId
     * @param roleId
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<Syscomuser> getUNSelectRolesByUserId(int userId, int roleId) throws SafeaaException {
        if (userId == 0){
            // 角色序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostUserId);
            return null;
        }
        if (roleId == 0){
            // 成員序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
            return null;
        }

        return syscomuserdeputyExtMapper.queryUNSelectUserDeputyByUserId(userId, roleId);

    }

    /**
     * To get one SyscomUserDeputy record by using UserId and culture code.
     * 以使用者序號查出已使用者代理人的代理狀態
     *
     * @param userId
     * @param culture
     * @return
     * @throws SafeaaException
     */
    @Override
    public List<SyscomDeputyUserMatrixVo> getDeputyMatrixByUserId(int userId, String culture) throws Exception {
        if (userId == 0){
            // 角色序號未傳入
            addError(safeSettings.getCulture(), SAFEMessageId.LostUserId);
            return null;
        }
        if (StringUtils.isBlank(culture)){
            culture = safeSettings.culture;
        }
        List<SyscomDeputyUserMatrixVo> dt = new ArrayList<SyscomDeputyUserMatrixVo>();
        try{
            List<SyscomRoleMembersVo> dtParentRoles = syscomuserdeputyExtMapper.queryParentRolesByUserId(userId, culture);
            if (dtParentRoles.size() >0){

                SyscomDeputyUserMatrixVo row = new SyscomDeputyUserMatrixVo();

                for (SyscomRoleMembersVo syscomRoleMembersVo: dtParentRoles){
                	int roleId = syscomRoleMembersVo.getRoleid();
                    List<SyscomrolemembersAndCulture> dtRoleUsers = role.getRoleUsersByRoleId(roleId);
                    if (dtRoleUsers.size() > 0){
                        for(SyscomrolemembersAndCulture drowUser:dtRoleUsers){
                            int deputyUserId = drowUser.getUserid();
                            if (deputyUserId != userId){
                                row.setDeputyUserId(deputyUserId);
                                row.setDeputyLogOnId(drowUser.getLogonid());
                                row.setDeputyUserName(drowUser.getUsername());
                                row.setStatus(0L);
                                dt.add(row);
                            }
                        }
                    }
                }

                if (dt.size() >0){
                    // 設定First row
                    SyscomDeputyUserMatrixVo firstRow = new SyscomDeputyUserMatrixVo();
                    firstRow.setDeputyUserId(0);
                    firstRow.setDeputyLogOnId("DeputyLogOnId");
                    firstRow.setDeputyUserName("DeputyUserName");
                    firstRow.setStatus(0L);
                    dt.add(0,firstRow);

                    List<SyscomQueryAllUserDeputyVo> dtDeputyUsers = getSelectedDeputyByUserId(userId, null);
                    if(dtDeputyUsers.size() > 0){
                        for (SyscomQueryAllUserDeputyVo drow:dtDeputyUsers){
                            int deputyUserId = drow.getDeputyUserId();
                            for (int i=0;i<dt.size();i++){
                                if (dt.get(i).getDeputyUserId() == deputyUserId){
                                    dt.get(i).setStatus(1L);
                                    dt.get(i).setEffectDate(drow.getEffectdate());
                                    dt.get(i).setExpiredDate(drow.getExpireddate());
                                }
                            }
                        }
                    }
                }
            }
            return dt;

        }catch (Exception e){
            throw e;
        }
    }


}
