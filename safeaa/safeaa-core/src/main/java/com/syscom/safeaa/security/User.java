package com.syscom.safeaa.security;

import java.util.List;

import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.core.BaseInterface;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.model.Syscomuserstatus;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllUsers;
import com.syscom.safeaa.mybatis.vo.SyscomUserQueryVo;
import com.syscom.safeaa.mybatis.vo.SyscomresourceInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomuserInfoVo;


/**
 * 使用者表 資料層
 * 
 * @author syscom
 */
public interface User extends BaseInterface
{

    /**
     * 根據條件分頁查詢角色資料
     * 
     * @param role 角色信息
     * @return 角色資料集合信息
     */
    public List<Syscomuser> selectUserList(Syscomuser sysUser);

    /**
     * 通過使用者名查詢使用者
     * 
     * @param userName 使用者名
     * @return 使用者對象信息
     */
    public List<Syscomuser> selectUserByUserName(String userName);


    /**
     * Create one new SyscomUser record and relative SyscomUserStatus record.
     *
     */
    public int createUser(Syscomuser sysUser,int passwordFormat,String passwordHintQuestion,
                           String passwordHintAnswer,String password);

    /**
     *
     * 修改一筆使用者資料
     * 若UserId有值則直接Update，若沒有則判斷LogOnId是否有值
     * 並以LogOnId取得UserId再以UpdateByPrimaryKey更新
     */
    public int updateUser(Syscomuser sysUser) throws SafeaaException;

    /**
     *
     * 以使用者序號刪除一筆使用者及其相關的狀態資料及成員資料
     * 同時刪除狀態資料所以要包Transaction
     */
    public boolean deleteUser(int userId) throws SafeaaException;

    /**
     *
     * 以登入帳號(LogOnId)查出該筆使用者的使用者序號
     *
     */
    public List<Syscomuser> getUserIdByNo(String logOnId) throws SafeaaException;
    
    /**
    *
    * 以登入帳號(LogOnId)查出該筆使用者信息
    *
    */
    public SyscomuserInfoVo getSyscomuserInfo(String logOnId) throws SafeaaException;

    /**
     *
     * 以使用者序號(UserId)查出一筆使用者資料
     *
     */
    public Syscomuser getUserById(Syscomuser sysUser) throws SafeaaException;

    /**
     * 以使用者序號(UserId)查出一筆使用者資料(含狀態資料)
     *
     */
    public int getUserById(Syscomuser sysUser, Syscomuserstatus syscomuserstatus) throws SafeaaException;

    /**
     * all user data and relative status data.
     *
     */
    public List<SyscomQueryAllUsers> getAllUsers(String whereClause) throws SafeaaException;

    /**
     * To get one user and relative status data by using UserId.
     * 1. Do not check valid date from EffectDate and ExpireDate.
     * 2. If It have no culture code pass then system configuration status data will return.
     * 以使用者序號(UserId)查出一筆使用者資料(含狀態資料)
     * 1. 不判斷有效日期(含未生效及停用資料)
     */
    public List<SyscomQueryAllUsers> getUserDataById(int userId) throws SafeaaException;

    /**
     *
     * 以使用者名稱查出使用者資料(含狀態資料)
     * 1. 不判斷有效日期(含未生效及停用資料)
     * 2. 以Like方式查詢
     */
    public List<SyscomQueryAllUsers> getUserDataByName(String userName) throws SafeaaException;

    /**
     *
     * 以登入帳號(LogOnId)查出一筆使用者資料(含狀態資料)
     * 1. 不判斷有效日期(含未生效及停用資料)
     *
     */
    public List<SyscomQueryAllUsers> getUserDataByNo(String logOnId) throws SafeaaException;

    /**
     * <param name="userMail">user no mail address</param>
     * one user data and relative status data.
     * 以使用者EMail查出使用者資料(含狀態資料)
     * 1. 不判斷有效日期(含未生效及停用資料)
     */
    public List<SyscomQueryAllUsers> getUserDataByMail(String userMail) throws SafeaaException;

    /**
     * Create one new SyscomUserStatus record.
     * The count of successful insert records,
     * 建立一筆使用者狀態資料
     */
    public int createUserStatus(Syscomuserstatus syscomuserstatus) throws Exception;

    /**
     * Create one new SyscomUserStatus record.
     * The count of successful updated records,
     * 修改一筆使用者狀態資料
     */
    public int updateUserStatus(Syscomuserstatus syscomuserstatus) throws SafeaaException;

    /**
     * Delete one designate SyscomUserStatus record.
     * The count of successful deleted records,
     * 以使用者序號刪除一筆使用者狀態資料
     */
    public int deleteUserStatus(int userId) throws SafeaaException;

    /**
     *
     * To get SyscomUserStatus record by using UserId.
     *  Status data of designate user.
     *  以使用者序號(UserId)查出該筆使用者的狀態資料
     */
    public Syscomuserstatus getUserStatusById(Syscomuserstatus syscomuserstatus) throws SafeaaException;

    /**
     *
     * Force to unlock one user status record.
     * 強迫帳戶解鎖
     *
     */
    public int forceAccountLockOff(int userId,int updateUserId) throws SafeaaException;

    /**
     *
     * Validate the new LogOnId of user.
     * @Param new user logon Id
     * <returns>
     *     True : LogOnId is valid, False : LogOnId is not valid
     * </returns>
     * Check rules are depending on system policy.
     * 檢核新登入帳號
     * 先取得該系統Default管理原則
     */
    public boolean validateNewLogOnId(String newLogOnId) throws Exception;


    /**
     *
     * Validate the new password of user.
     * True : password is valid, False : password is not valid
     * @param userId
     * @param newPassword
     * @param idno
     * @param birthday
     * @param logOnId
     * @param passwordEncryption
     * @return
     * @throws Exception
     * 檢核新密碼
     * 先取得該系統Default管理原則
     */
    public boolean validateNewPassword(int userId, String newPassword, String idno, String birthday, String logOnId, int passwordEncryption) throws Exception;
        
    public List<SyscomUserQueryVo> queryUsersBy (String userId,String userName,String sort) throws Exception;
    
    public List<SyscomresourceInfoVo> querySyscomresourceByLogOnId(String logOnId) throws Exception;

}
