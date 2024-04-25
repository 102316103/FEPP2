package com.syscom.safeaa.security;

import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.core.BaseInterface;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.model.Syscomuserdeputy;
import com.syscom.safeaa.mybatis.vo.SyscomDeputyUserMatrixVo;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllUserDeputyVo;

import java.util.List;

/**
 * 使用者代理人
 * @author syscom
 */
public interface UserDeputy  extends BaseInterface {

    /**
     * Create one new SyscomUserDeputy record.
     * The count of successful insert records,
     * 增加使用者代理人資料(傳入使用者代理人物件)
     * @param syscomuserdeputy
     * @return
     * @throws SafeaaException
     */
    public int addUserDeputy(Syscomuserdeputy syscomuserdeputy) throws SafeaaException;

    /**
     * Delete one SyscomUserDeputy record.
     * The count of successful insert records,
     * if return -1 Then represent insert fail or parameter passing error.
     * 以角色序號,成員序號及代理人序號刪除一筆使用者代理人資料
     * @param userId
     * @param roleId
     * @param deputyUserId
     * @return
     * @throws SafeaaException
     */
    public int removeUserDeputy(int userId, int roleId, int deputyUserId) throws SafeaaException;

    /**
     * Delete SyscomUserDeputy records of one user.
     * The count of successful insert records,
     * if return -1 Then represent insert fail or parameter passing error.
     * 以使用者序號移除一個使用者的所有代理人資料
     * @param userId
     * @return
     * @throws SafeaaException
     */
    public int removeUserDeputysByUserId(int userId) throws SafeaaException;

    /**
     * Delete SyscomUserDeputy records of one role.
     * The count of successful insert records,
     * if return -1 Then represent insert fail or parameter passing error.
     * 以角色序號移除一個角色的所有使用者代理人資料
     * @param roleId
     * @return
     * @throws SafeaaException
     */
    public int removeUserDeputysByRoleId(int roleId) throws SafeaaException;

    /**
     * To get all selected SyscomUserDeputy records by using UserId.
     * 以使用者序號查出已加入至某使用者代理人的使用者清單
     * @param userId
     * @param orderby
     * @return
     * @throws SafeaaException
     */
    public List<SyscomQueryAllUserDeputyVo> getSelectedDeputyByUserId(int userId, String orderby) throws SafeaaException;

    /**
     * To get all unselected SyscomUserDeputy records by using UserId and RoleId.
     * 查出尚未加入至某使用者角色代理人的使用者清單
     * @param userId
     * @param roleId
     * @return
     * @throws SafeaaException
     */
    public List<Syscomuser> getUNSelectRolesByUserId(int userId, int roleId) throws SafeaaException;

    /**
     * To get one SyscomUserDeputy record by using UserId and culture code.
     * 以使用者序號查出已使用者代理人的代理狀態
     * @param userId
     * @param culture
     * @return
     * @throws SafeaaException
     */
    public List<SyscomDeputyUserMatrixVo> getDeputyMatrixByUserId(int userId, String culture) throws Exception;
}
