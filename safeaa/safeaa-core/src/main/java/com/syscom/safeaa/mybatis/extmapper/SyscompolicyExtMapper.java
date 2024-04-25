package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscompolicyMapper;
import com.syscom.safeaa.mybatis.model.SyscomQueryParentRolesByUserId;
import com.syscom.safeaa.mybatis.model.Syscompolicy;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllPolicysVo;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscompolicyExtMapper extends SyscompolicyMapper {
	/**
	 *
	 * add by qin
	 */
	List<Syscompolicy> queryAllData();

	/**
	 *
	 * add by qin
	 */
	List<SyscomQueryParentRolesByUserId> queryPolicyByUserId(Integer userid);

	/**
	 *
	 * add by qin
	 */
	List<Syscompolicy> queryPolicyByNo(String policyNo);

	/**
	 * queryAllPolicys
	 * add by qin
	 */
	List<SyscomQueryAllPolicysVo> queryAllPolicys(@Param("culture") String culture,
			@Param("policyId") Integer policyId,
			@Param("policyName") String policyName,
			@Param("policyNo") String policyNo);

}