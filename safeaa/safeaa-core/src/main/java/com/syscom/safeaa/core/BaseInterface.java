package com.syscom.safeaa.core;

import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.model.Syscomuserstatus;

public interface BaseInterface {

	public Integer getCurrentUserId();
	
	public void setCurrentUserId(Integer currentUserId);

	public Syscomuser getCurrentUser();

	public void setCurrentUser (Syscomuser currentUser);
	
	public Syscomuserstatus getmDefUserStatus();

	public void setmDefUserStatus(Syscomuserstatus mDefUserStatus);
}
