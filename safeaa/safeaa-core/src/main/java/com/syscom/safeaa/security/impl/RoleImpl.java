package com.syscom.safeaa.security.impl;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.configuration.DataSourceSafeaaConstant;
import com.syscom.safeaa.constant.CommonConstants;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.vo.SyscomroleResourceVo;
import com.syscom.safeaa.security.Role;
import com.syscom.safeaa.utils.SyscomConfig;
import com.syscom.safeaa.mybatis.extmapper.SyscomroleExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomrolecultureExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomrolegroupExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomrolemembersExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomserialExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserExtMapper;

import com.syscom.safeaa.mybatis.model.Syscomrole;
import com.syscom.safeaa.mybatis.model.Syscomroleculture;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.model.Syscomserial;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;
import com.syscom.safeaa.mybatis.vo.SyscomroleInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomrolemembersAndCulture;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleImpl extends ApplicationBase implements Role {
	
	private int mCurrentUserId;
	
	@Autowired
	private SyscomroleExtMapper mDBRole;
	
	@Autowired
	private SyscomrolecultureExtMapper mDBRoleCulture;
	
	@Autowired
	private SyscomrolemembersExtMapper mDBRoleMembers;
	
	@Autowired
	private SyscomrolegroupExtMapper mDBRoleGroups;
	
	@Autowired
	private SyscomuserExtMapper userMapper;
	
	@Autowired
	private SyscomConfig safeSettings;
	
	@Autowired
	private SyscomserialExtMapper serialExtMapper;

	public  RoleImpl() {}
	
	
	public  RoleImpl(int userId) {
		this.mCurrentUserId = userId;
	}
	
	@Override
	public int createRole(Syscomrole defRole) throws SafeaaException {
		try {
			if ( null == defRole.getRoleno()) {
				// 角色代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo); 
			}

			int rst = -1;
			int nextId = -1;
			Syscomserial syscomserial = serialExtMapper.selectByPrimaryKey(CommonConstants.SerialName);
			if(syscomserial==null) {
				syscomserial = new Syscomserial();
				syscomserial.setSerialname(CommonConstants.SerialName);
				syscomserial.setNumberformat("");
				syscomserial.setResetfield("");
				syscomserial.setInterval(0);
				syscomserial.setNextid(1L);	
				syscomserial.setMaxvalue(9000000L);
				syscomserial.setUpdateUser(defRole.getUpdateuserid());
				rst = serialExtMapper.insert(syscomserial);
				nextId = 1;
			}else {
				if(syscomserial.getNextid()!=null) {
					syscomserial.setNextid(syscomserial.getNextid() + 1);
					syscomserial.setNumberformat("");
					syscomserial.setInterval(0);
				}else {
					syscomserial.setNextid(1L);
				}
				
				rst = serialExtMapper.updateByPrimaryKeySelective(syscomserial);
				nextId = syscomserial.getNextid().intValue();
			}
			
			if(rst==1) {
				defRole.setRoleid(nextId);
			}else {
				return -1;
			}
			return this.mDBRole.insertSelective(defRole);
			
		}catch(Exception e) {
			e.printStackTrace();
			throw new SafeaaException(safeSettings.getCulture(), SAFEMessageId.InsertMasterFail,e );
		}
	}

	@Override
	public int updateRole(Syscomrole defRole) throws SafeaaException {
		try {
			if ( defRole.getRoleid() >0) {
				return mDBRole.updateByPrimaryKeySelective(defRole);
			}else {
				if (StringUtils.isEmpty(defRole.getRoleno())) {
					Integer mRoleId = mDBRole.queryRoleIdByNo(defRole.getRoleno());
					if (mRoleId != null && mRoleId > 0) {
						defRole.setRoleid(mRoleId);
						defRole.setUpdateuserid(mCurrentUserId);
						//TODO datatime
						return mDBRole.updateByPrimaryKeySelective(defRole);
					}
					addError(safeSettings.getCulture(), SAFEMessageId.WithoutRoleData);
				}
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo);
			}
			
		}catch(Exception e) {
			//TODO exception message
			addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
			e.printStackTrace();
			
		}
		return 0;
	}

	@Override
	@Transactional(rollbackFor=SafeaaException.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public boolean deleteRole(int roleId) throws Exception {
		try {
			Syscomrole defRole = new Syscomrole();
			defRole.setRoleid(roleId);			
			
			//待處理
			
			int rst4 =  mDBRoleGroups.deleteAllByRoleId(roleId);
			
			int rst3 = mDBRoleMembers.deleteAllByRoleId(roleId);
			
			int rst2 = mDBRoleCulture.deleteAllByRoleId(roleId);
			
			int rst1 = mDBRole.deleteByPrimaryKey(defRole);
			
			if (rst1>=0 && rst2>=0 && rst3>=0 && rst4>=0) {
				return true;
			}
			
			//刪除主檔失敗
			addError(safeSettings.getCulture(), SAFEMessageId.DeletMasterFail);
		}catch(SafeaaException safeaaException) {
			throw safeaaException;	
		}catch(Exception e) {
			e.printStackTrace();
			//查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return false;
	}

	@Override
	public Integer getRoleIdByNo(String roleNo) throws SafeaaException {
		if (StringUtils.isEmpty(roleNo)) {
			this.addError("", SAFEMessageId.EmptyRoleNo); //錕斤拷色錕斤拷錕絘錕斤拷錕矯空幫拷
		}
		
		try {
			Integer mResult = mDBRole.queryRoleIdByNo(roleNo);
			//if (mResult == null || mResult < 0) {
				//this.addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			//}
			
			return mResult;
		// }catch(SafeaaException ae) {
		// 	throw ae;
	    }catch(Exception e) {
	    	this.addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
		}
		return -1;
	}

	@Override
	public Syscomrole getRoleById(Syscomrole defRole) throws Exception {
		if ( defRole.getRoleid() <=0) {
			this.addError("", SAFEMessageId.LostRoleId); //瑙掕壊搴忚櫉鏈偝鍏�
		}
		
		try {
			Syscomrole dbRole = mDBRole.selectByPrimaryKey(defRole.getRoleid());
			if ( null== dbRole || dbRole.getRoleid() < 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			}
			
			return dbRole;
		}catch(SafeaaException ae) {
			throw ae;
	    }catch(Exception e) {
	    	addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
		}
		return null;
	}

	@Override
	public SyscomroleAndCulture getRoleById(Syscomrole defRole, Syscomroleculture culture) throws Exception {
		if ( defRole.getRoleid() <=0) {
			//角色序號未傳入
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId); 
		}

		try {
			Syscomrole dbRole = mDBRole.selectByPrimaryKey(defRole.getRoleid());
			if ( null== dbRole || dbRole.getRoleid() < 0) {
				this.addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			}
			
			culture.setRoleid(dbRole.getRoleid());
			Syscomroleculture dbCulture = mDBRoleCulture.selectByPrimaryKey(dbRole.getRoleid(), culture.getCulture());
			if (dbCulture == null || StringUtils.isEmpty(dbCulture.getRolename())) {
				this.addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			}
			
			SyscomroleAndCulture roleCulture = new SyscomroleAndCulture(dbRole, dbCulture);
			
			return roleCulture;
		}catch(SafeaaException ae) {
			throw ae;
	    }catch(Exception e) {
	    	this.addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
		}
		return null;
	}

	@Override
	public List<SyscomroleAndCulture> getAllRoles(String culture) throws Exception {
		if ( null == culture || StringUtils.isEmpty(culture)) {
			culture = safeSettings.getCulture();
		}
		try {
			return mDBRole.queryAllRoles(new SyscomroleAndCulture(culture));
			
	    }catch(Exception e) {
	    	e.printStackTrace();
	    	addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
		}
		return null;
	}

	@Override
	public SyscomroleAndCulture getRoleDataById(int roleId, String culture) throws Exception {
		if ( roleId <=0) {
			//角色序號未傳入
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId); 
		}
		
		if ( null == culture || StringUtils.isEmpty(culture)) {
			culture = safeSettings.getCulture();
		}
		try {

			List<SyscomroleAndCulture> roleCultureList = mDBRole.queryAllRoles(new SyscomroleAndCulture (roleId, culture));
			if ( null == roleCultureList ) {
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			}
			return roleCultureList.get(0);
		}catch(SafeaaException ae) {
			throw ae;
	    }catch(Exception e) {
	    	//TODO need to define new Errormsg 
	    	addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
		}
		return null;
	}

	@Override
	public List<SyscomroleAndCulture> getRoleDataByName(String roleName, String culture) throws Exception {
		if ( StringUtils.isBlank(roleName)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleName); //瑙掕壊鍚嶇ū涓嶅緱絀虹櫧
		}
		
		if ( null == culture || StringUtils.isEmpty(culture)) {
			culture = safeSettings.getCulture();
		}
		try {

			return mDBRole.queryAllRoles(new SyscomroleAndCulture (roleName, culture));
	    }catch(Exception e) {
	    	//TODO need to define new Errormsg 
	    	addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
		}
		return null;
	}

	@Override
	public SyscomroleAndCulture getRoleDataByNo(String roleNo, String culture) throws Exception {
		if ( StringUtils.isBlank(roleNo)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo); //瑙掕壊浠ｇ⒓涓嶅緱絀虹櫧
		}
		
		if ( null == culture || StringUtils.isEmpty(culture)) {
			culture = safeSettings.getCulture();
		}
		try {
			SyscomroleAndCulture roleCulture = new SyscomroleAndCulture (culture);
			roleCulture.setRoleno(roleNo);
			List<SyscomroleAndCulture> result = mDBRole.queryAllRoles(roleCulture);
			if ( null !=result) {
				return result.get(0);
			}
			return null;
	    }catch(Exception e) {
	    	//TODO need to define new Errormsg 
	    	addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
		}
		return null;
	}

	@Override
	public int addRoleCulture(Syscomroleculture oDefRoleCulture) throws SafeaaException {
		
		if (oDefRoleCulture.getRoleid() <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
			return -1;
		}
		
		if (StringUtils.isBlank(oDefRoleCulture.getCulture())) {
			//TODO culture
			oDefRoleCulture.setCulture(safeSettings.getCulture());
		}
		
		try {
			return mDBRoleCulture.insert(oDefRoleCulture);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError("",  SAFEMessageId.InsertCultureFail);
		}
		return -1;
	}

	@Override
	public int addRoleCulture(String roleNo, String culture, String roleName, String remark) throws SafeaaException {
		
		if (StringUtils.isBlank(roleNo)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo);
		}
		
		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture();
		}		
		try {
			Integer roleId = mDBRole.queryRoleIdByNo(roleNo);
			if (roleId == null || roleId <=0) {
				addError(safeSettings.getCulture(),  SAFEMessageId.WithoutRoleData);
			}
			
			Syscomroleculture oDefRoleCulture = new Syscomroleculture();
			oDefRoleCulture.setRoleid(roleId);
			oDefRoleCulture.setCulture(culture);
			oDefRoleCulture.setRolename(roleName);
			oDefRoleCulture.setRemark(remark);
			
			return mDBRoleCulture.insert(oDefRoleCulture);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return -1;
	}

	@Override
	public int updateRoleCulture(Syscomroleculture oDefRoleCulture) throws Exception {
		if (oDefRoleCulture.getRoleid() <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
			return -1;
		}
		
		if (StringUtils.isBlank(oDefRoleCulture.getCulture())) {
			oDefRoleCulture.setCulture(safeSettings.getCulture());
		}
		
		try {
			return mDBRoleCulture.updateByPrimaryKey(oDefRoleCulture);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return -1;
	}

	@Override
	public int updateRoleCulture(String roleNo, String culture, String roleName, String remark) throws SafeaaException {
		if (StringUtils.isBlank(roleNo)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo);
		}
		
		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture();
		}		
		try {
			Integer roleId = mDBRole.queryRoleIdByNo(roleNo);
			if (roleId == null || roleId <=0) {
				addError(safeSettings.getCulture(),  SAFEMessageId.WithoutRoleData);
			}
			
			Syscomroleculture oDefRoleCulture = new Syscomroleculture();
			oDefRoleCulture.setRoleid(roleId);
			oDefRoleCulture.setCulture(culture);
			oDefRoleCulture.setRolename(roleName);
			oDefRoleCulture.setRemark(remark);
			
			return mDBRoleCulture.updateByPrimaryKeySelective(oDefRoleCulture);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return -1;
	}

	@Override
	public int removeRoleCulture(int roleId, String culture) throws Exception {
		if ( roleId <=0) {
			this.addError("", SAFEMessageId.LostRoleId);
			return -1;
		}
		
		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture();
		}
		
		try {
			Syscomroleculture syscomroleculture = new Syscomroleculture();
			syscomroleculture.setRoleid(roleId);
			syscomroleculture.setCulture(culture);
			return mDBRoleCulture.deleteByPrimaryKey(syscomroleculture);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return -1;
	}

	@Override
	public int removeAllRoleCultures(int roleId) throws Exception {
		if ( roleId <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
			return -1;
		}
		
		try {
			return mDBRoleCulture.deleteAllByRoleId(roleId);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return -1;
	}

	@Override
	public Syscomroleculture getRoleCultureById(Syscomroleculture oDefRoleCulture) throws Exception {
		if (oDefRoleCulture.getRoleid() <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		
		if (StringUtils.isBlank(oDefRoleCulture.getCulture())) {
			oDefRoleCulture.setCulture(safeSettings.getCulture());
		}
		
		try {
			return mDBRoleCulture.selectByPrimaryKey(oDefRoleCulture.getRoleid(), oDefRoleCulture.getCulture() );
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.QueryNoRecord);
		}
		return null;
	}

	@Override
	public List<Syscomroleculture> getRoleCulturesById(int roleId) throws Exception {
		if ( roleId <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		
		try {
			return mDBRoleCulture.queryAllByRoleId(roleId);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return null;
	}

	@Override
	public int addRoleMembers(Syscomrolemembers oDefRoleMembers) throws Exception {
		if (oDefRoleMembers.getRoleid() <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		
		if (oDefRoleMembers.getChildid() <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostChildId);
		}		
		if (StringUtils.isBlank(oDefRoleMembers.getChildtype())) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType);
		}
		
		try {
			//TODO If CheckChildId(oDefRoleMembers.ChildId, oDefRoleMembers.ChildType) Then
			return mDBRoleMembers.insert(oDefRoleMembers);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.QueryNoRecord);
		}
		return -1;
	}

	@Override
	public int addRoleMembers(String roleNo, String memberNo, String childType, int locationNo, Date effectDate,
			Date expiredDate) throws SafeaaException{
		if (StringUtils.isBlank(roleNo)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo);
		}
		
		if (StringUtils.isBlank(memberNo)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildNo);
		}
		if (StringUtils.isBlank(childType)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType);
		}
		
		try {
			Integer roleId = mDBRole.queryRoleIdByNo(roleNo);
			if (roleId == null || roleId <=0) {
				addError(safeSettings.getCulture(),  SAFEMessageId.WithoutRoleData);
			}
			
			Syscomrolemembers mDefRoleMembers = new Syscomrolemembers();
			mDefRoleMembers.setRoleid(roleId);
			//TODO ,query childid
			//mDefRoleMembers.setChildid(childId);
			mDefRoleMembers.setChildtype(childType);
			
			mDefRoleMembers.setEffectdate(effectDate);
			mDefRoleMembers.setExpireddate(expiredDate);
			
			return mDBRoleMembers.insert(mDefRoleMembers);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertMemberFail);
		}
		return -1;

	}

	@Override
	public int updateRoleMembes(Syscomrolemembers oDefRoleMembers) throws Exception {
		if (oDefRoleMembers.getRoleid() <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		
		if (oDefRoleMembers.getChildid() <=0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostChildId);
		}		
		if (StringUtils.isBlank(oDefRoleMembers.getChildtype())) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType);
		}
		
		try {
			return mDBRoleMembers.updateByPrimaryKeySelective(oDefRoleMembers);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.QueryNoRecord);
		}
		return -1;
	}

	@Override
	public int updateRoleMembes(String roleNo, String memberNo, String childType, 
			Date effectDate, Date expiredDate) throws SafeaaException{
		if (StringUtils.isBlank(roleNo)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo);
		}
		
		if (StringUtils.isBlank(memberNo)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildNo);
		}
		if (StringUtils.isBlank(childType)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType);
		}
		
		try {
			Integer roleId = mDBRole.queryRoleIdByNo(roleNo);
			if (roleId == null || roleId <=0) {
				addError("",  SAFEMessageId.WithoutRoleData);
			}
			
			Syscomrolemembers mDefRoleMembers = new Syscomrolemembers();
			mDefRoleMembers.setRoleid(roleId);
			//TODO ,query childid
			//mDefRoleMembers.setChildid(childId);
			mDefRoleMembers.setChildtype(childType);
			
			mDefRoleMembers.setEffectdate(effectDate);
			mDefRoleMembers.setExpireddate(expiredDate);
			
			return mDBRoleMembers.updateByPrimaryKeySelective(mDefRoleMembers);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertMemberFail);
		}
		return -1;

	}

	@Override
	public int removeRoleMembers(int roleId, int childId, String childType) throws Exception {
		if ( roleId <=0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		if ( childId <=0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostChildId);
		}
		
		if (StringUtils.isBlank(childType)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType);
		}
		
		try {
			Syscomrolemembers syscomrolemembers = new Syscomrolemembers();
			syscomrolemembers.setRoleid(roleId);
			syscomrolemembers.setChildid(childId);
			syscomrolemembers.setChildtype(childType);
			return mDBRoleMembers.deleteByPrimaryKey(syscomrolemembers);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return -1;	
	}

	@Override
	public int removeAllRoleMembers(int roleId) throws Exception {
		if ( roleId <=0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		
		try {
			return mDBRoleMembers.deleteAllByRoleId(roleId);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return -1;		
	}

	@Override
	public int removeAllRoleUsers(int roleId) throws Exception {
		if ( roleId <=0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		
		try {
			//TODO , mResult = mDBRoleMembers.DeleteAllUsersByRoleId(roleId), no such code
			return mDBRoleMembers.deleteAllByRoleId(roleId);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return -1;	
	}

	@Override
	public List<SyscomrolemembersAndCulture> getUNSelectMembersById(int roleId, String culture) throws Exception {
		if ( roleId <=0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture();
		}		
		try {

			return mDBRoleMembers.queryUNSelectMembers(roleId, culture);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return null;	
	}

	@Override
	public List<SyscomrolemembersAndCulture> getSelectedMembersById(int roleId, String culture) throws SafeaaException {
		if ( roleId <=0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture();
		}	
		try {

			return mDBRoleMembers.querySelectedByRoleId(roleId, culture);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return null;	
	}

	@Override
	public List<SyscomrolemembersAndCulture> getParentRolesByLogOnId(String logOnId, String culture) throws Exception {
		if (StringUtils.isBlank(logOnId)) {
			addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo);
		}
		
		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture();
		}
		
		try {
			
			List<Syscomuser> user = userMapper.queryUserByNo(logOnId);
			if (user == null || user.size() ==0 || user.get(0).getUserid() <=0) {
				addError(safeSettings.getCulture(),  SAFEMessageId.WithoutUserData);
			}
			
			int userId = user.get(0).getUserid();
			
			return mDBRoleMembers.queryParentRolesByUserId(userId, culture);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return null;			
	}

	public List<SyscomrolemembersAndCulture> getRoleUsersByRoleId(final int roleId) throws Exception{
		if ( roleId <=0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}		
		
		try {
			
			return mDBRoleMembers.queryRoleUsersByRoleId(roleId);
			
		} catch (Exception e) {
			e.printStackTrace();
			addError(safeSettings.getCulture(),  SAFEMessageId.InsertCultureFail);
		}
		return null;			
	}
	
	@Override	
	public List<SyscomroleInfoVo> getSyscomroleInfoVoAll() throws Exception {
		try {			
			return mDBRole.getSyscomroleInfoVoAll();			
		}catch(Exception e) {
			e.printStackTrace();
			//查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}	
		return null;
	}

	@Override
	public List<SyscomroleResourceVo> queryMenuListByRoles(String[] roles)throws Exception{
		try {
			return mDBRole.queryMenuListByRoles(roles);
		}catch(Exception e) {
			e.printStackTrace();
			//查詢失敗
			//addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return null;
	}
}
