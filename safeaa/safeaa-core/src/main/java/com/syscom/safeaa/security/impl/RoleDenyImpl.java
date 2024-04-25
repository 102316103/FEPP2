package com.syscom.safeaa.security.impl;

import java.util.List;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.constant.CommonConstants;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.extmapper.SyscomroledenyExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomroledeny;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.vo.SyscomrolegroupAndCulture;
import com.syscom.safeaa.security.RoleDeny;
import com.syscom.safeaa.utils.SyscomConfig;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleDenyImpl extends ApplicationBase implements RoleDeny{

	@Autowired
	private SyscomConfig safeSettings;
	
	@Autowired
	private SyscomroledenyExtMapper mDBRoleDenyMapper;
	
	@Override
	public int addRoleDeny(Syscomroledeny oDefRoleDeny) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addRoleDeny(String roleNo, String childNo, String childType) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean checkChildId(int childId, String childType) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkChildNo(String childNo, String childType) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int removeRoleDeny(int roleId, int childId, String childType) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int removeRoleDenysByRoleId(int roleId) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int removeRoleDenysByChildId(int childId, String childType) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SyscomrolegroupAndCulture> getSelectedRoleDenyByRoleId(int roleId, String culture) throws Exception {
		if ( roleId <= 0 ) {
			addError("", SAFEMessageId.LostRoleId);
		}

		if (StringUtils.isBlank(culture)) {
			culture = "zh-TW";
		}
		try {
			return mDBRoleDenyMapper.querySelectedRoleDenyByRoleId(roleId, CommonConstants.ChildTypeAll, culture);
		}catch(Exception e) {
			e.printStackTrace();
			addError("", SAFEMessageId.LogOnFail);
		}
		return null; 
			
	}

	@Override
	public List<SyscomrolegroupAndCulture> getUNSelectRoleDenyByRoleId(int roleId, String culture) throws Exception {
		if ( roleId <= 0 ) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}

		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture();
		}
		try {
			return mDBRoleDenyMapper.queryUNSelectRoleDenyByRoleId(roleId, CommonConstants.ChildTypeAll, culture);
		}catch(Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(), SAFEMessageId.LogOnFail);
		}
		return null; 
	}

	@Override
	public List<Syscomrolemembers> getDenyGroupsByUserId(int userId) throws Exception {
		if ( userId <= 0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}

		try {
			return mDBRoleDenyMapper.queryDenyGroupsByUserId(userId);
			
		}catch(Exception e) {
			e.printStackTrace();
			//TODO, define new messageId
			this.addError(safeSettings.getCulture(), SAFEMessageId.LogOnFail);
		}
		return null;
		
	}

}
