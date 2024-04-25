package com.syscom.safeaa.security;

import java.util.List;

import com.syscom.safeaa.core.BaseInterface;
import com.syscom.safeaa.mybatis.model.Syscomroleresource;
import com.syscom.safeaa.mybatis.vo.SyscomSelectResourcesVo;

/**
 * 
 * @author syscom
 *
 */
public interface RoleResource extends BaseInterface {

	/**
	 * 新增RoleResource
	 * @param oRoleResource
	 * @return
	 * @throws Exception
	 */
	public int addRoleResource(Syscomroleresource oRoleResource) throws Exception;
	
	/**
	 * 新增RoleResource
	 * @param roleno
	 * @param resourceno
	 * @return
	 * @throws Exception
	 */
	public int addRoleResource(String roleno,String resourceno) throws Exception;
	
	/**
	 * 刪除RoleResource
	 * @param roleid
	 * @param resourceid
	 * @return
	 * @throws Exception
	 */
	public int removeRoleResource(Integer roleid,Integer resourceid) throws Exception;
	
	/**
	 * 刪除RoleResource
	 * @param roleid
	 * @return
	 * @throws Exception
	 */
	public int removeRoleResourcesByRoleId(Integer roleid) throws Exception;
	
	/**
	 * 刪除RoleResource
	 * @param resourceid
	 * @return
	 * @throws Exception
	 */
	public int removeRoleResourcesByResourceId(Integer resourceid) throws Exception;
	
	/**
	 * 查詢RoleResource
	 * @param roleid
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	public List<SyscomSelectResourcesVo> getSelectedResourcesByRoleId(Integer roleid,String culture) throws Exception;
	
	/**
	 * 查詢RoleResource
	 * @param roleid
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	public List<SyscomSelectResourcesVo> getUnselectResourcesByRoleId(Integer roleid,String culture) throws Exception;
	
	
	
}
