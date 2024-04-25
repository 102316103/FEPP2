package com.syscom.safeaa.security.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.mybatis.extmapper.SyscomroleExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomroleresourceExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomroleresource;
import com.syscom.safeaa.mybatis.vo.SyscomSelectResourcesVo;
import com.syscom.safeaa.security.RoleResource;
import com.syscom.safeaa.utils.SyscomConfig;

/**
 * 
 * @author syscom
 *
 */
@Component
public class RoleResourceImpl extends ApplicationBase implements RoleResource {

	@Autowired
	private SyscomConfig safeSettings;
	
	@Autowired
	private SyscomroleExtMapper roleMapper;
	
	@Autowired
	private SyscomroleresourceExtMapper roleresourceMapper;
	
	@Override
	public int addRoleResource(Syscomroleresource oRoleResource) throws Exception{
		try {
			if (oRoleResource.getRoleid()==null) {
				//角色序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId); 
				return -1;
			}
			
			if (oRoleResource.getResourceid()==null) {
				//資源功能權限序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return -1;
			}
			
			return roleresourceMapper.insert(oRoleResource);
			
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public int addRoleResource(String roleno, String resourceno) throws Exception{
		try {
			if (StringUtils.isBlank(roleno)) {
				//角色代碼不得空白
				this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo);
				return -1;
			}
			
			if (StringUtils.isBlank(resourceno)) {
				//資源功能權限代碼不得空白
				this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyResourceNo);
				return -1;
			}
			Integer roleid = roleMapper.queryRoleIdByNo(roleno);
			if(roleid!=null && roleid>0) {
				Syscomroleresource oRoleResource = new Syscomroleresource();
				oRoleResource.setRoleid(roleid);
				oRoleResource.setResourceid(new Integer(resourceno));
				oRoleResource.setSafedefinefunctionlist("");
				oRoleResource.setUserdefinefunctionlist("");
				oRoleResource.setUpdatetime(new Date());
				oRoleResource.setUpdateuserid(getCurrentUserId());
				return roleresourceMapper.insert(oRoleResource);
			}
			
			//無此角色代碼
			this.addError(safeSettings.getCulture(), SAFEMessageId.WithoutRoleData);
			return -1;
			
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public int removeRoleResource(Integer roleid, Integer resourceid) throws Exception{
		try {
			if (roleid ==null) {
				//角色序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId); 
				return -1;
			}
			
			if (resourceid==null) {
				//資源功能權限序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return -1;
			}

			Syscomroleresource syscomroleresource = new Syscomroleresource();
			syscomroleresource.setRoleid(roleid);
			syscomroleresource.setResourceid(resourceid);
			return roleresourceMapper.deleteByPrimaryKey(syscomroleresource);
			
		}catch (Exception e) {
			throw e;
		}
	}

	@Override
	public int removeRoleResourcesByRoleId(Integer roleid) throws Exception{
		try {
			if (roleid ==null) {
				//角色序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId); 
				return -1;
			}
			
			return roleresourceMapper.deleteAllByRoleId(roleid);

		}catch (Exception e) {
			throw e;
		}
	}

	@Override
	public int removeRoleResourcesByResourceId(Integer resourceid) throws Exception{
		try {
			if (resourceid==null) {
				//資源功能權限序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return -1;
			}
			return roleresourceMapper.deleteAllByResourceId(resourceid);

		}catch (Exception e) {
			throw e;
		}
	}

	@Override
	public List<SyscomSelectResourcesVo> getSelectedResourcesByRoleId(Integer roleid, String culture) throws Exception{
		try {
			if (roleid ==null) {
				//角色序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId); 
				return null;
			}
			
			if (StringUtils.isBlank(culture)) {
				culture =safeSettings.getCulture();
			}
			
			return roleresourceMapper.querySelectedResourcesByRoleId(roleid,culture);

		}catch (Exception e) {
			throw e;
		}

	}

	@Override
	public List<SyscomSelectResourcesVo> getUnselectResourcesByRoleId(Integer roleid, String culture) throws Exception{
		try {
			if (roleid ==null) {
				//角色序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId); 
				return null;
			}
			
			if (StringUtils.isBlank(culture)) {
				culture =safeSettings.getCulture();
			}
			
			return roleresourceMapper.queryUNSelectResourcesByRoleId(roleid,culture);

		}catch (Exception e) {
			throw e;
		}

	}

}
