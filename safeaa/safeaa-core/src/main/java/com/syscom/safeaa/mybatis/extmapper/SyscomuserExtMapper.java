package com.syscom.safeaa.mybatis.extmapper;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.safeaa.mybatis.mapper.SyscomuserMapper;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomGroupVo;
import com.syscom.safeaa.mybatis.vo.SyscomUserQueryVo;
import com.syscom.safeaa.mybatis.vo.SyscomresourceInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomuserInfoVo;

@Resource
public interface SyscomuserExtMapper extends SyscomuserMapper {

	/**
	 * 根據條件分頁查詢使用者列表
	 *
	 * @param sysUser 使用者信息
	 * @return 使用者信息集合信息
	 */
	public List<Syscomuser> selectUserList(Syscomuser syscomuUser);

	/**
	 * 通過使用者名查詢使用者
	 *
	 * @param userName 使用者名
	 * @return 使用者對象信息
	 */
	public List<Syscomuser> selectUserByUserName(String userName);

	public List<Syscomuser> queryUserByNo(String logOnId);

	/**
	 * all user data and relative status data.
	 *
	 */
	List<Syscomuser> getAllUsers();

	/**
	 *
	 * 以使用者名稱查出使用者資料(含狀態資料)
	 * 1. 不判斷有效日期(含未生效及停用資料)
	 * 2. 以Like方式查詢
	 */
	List<Syscomuser> getUserDataByName(String userName);

	SyscomuserInfoVo getSyscomuserInfo(@Param("logOnId") String logOnId);

	List<SyscomresourceInfoVo> querySyscomresourceByLogOnId(@Param("logOnId") String logOnId);

	List<SyscomUserQueryVo> queryUsersBy(@Param("logonId") String logonId, @Param("userName") String userName, @Param("orderBy") String orderBy);

	/*
	 * 以使用者編號查詢所在群組信息
	 */
	List<SyscomGroupVo> queryUserAndGroup(@Param("userId") String userId);
}