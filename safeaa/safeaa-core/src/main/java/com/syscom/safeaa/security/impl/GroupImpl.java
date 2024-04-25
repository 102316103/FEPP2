package com.syscom.safeaa.security.impl;

import java.util.List;

import com.syscom.safeaa.configuration.DataSourceSafeaaConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.constant.CommonConstants;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupcultureExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupmembersExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomrolegroupExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomserialExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomgroup;
import com.syscom.safeaa.mybatis.model.Syscomgroupculture;
import com.syscom.safeaa.mybatis.model.Syscomgroupmembers;
import com.syscom.safeaa.mybatis.model.Syscomserial;
import com.syscom.safeaa.mybatis.vo.SyscomgroupAndCulture;
import com.syscom.safeaa.mybatis.vo.SyscomgroupInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;
import com.syscom.safeaa.security.Group;
import com.syscom.safeaa.utils.SyscomConfig;

@Component
public class GroupImpl extends ApplicationBase implements Group {

	@Autowired
	private SyscomConfig safeSettings;

	@Autowired
	SyscomgroupExtMapper groupExtMapper;

	@Autowired
	private SyscomrolegroupExtMapper rolegroupExtMapper;

	@Autowired
	SyscomgroupmembersExtMapper groupmembersExtMapper;

	@Autowired
	SyscomgroupcultureExtMapper groupcultureExtMapper;

	@Autowired
	private SyscomserialExtMapper serialExtMapper;

	@Override
	@Transactional(rollbackFor = Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public int createGroup(Syscomgroup oDefGroup) throws SafeaaException {
		try {

			if (oDefGroup == null || StringUtils.isBlank(oDefGroup.getGroupno())) {
				// 功能群組代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyGroupNo);
			}
			int rst = -1;
			Integer nextId = -1;
			Syscomserial syscomserial = serialExtMapper.selectByPrimaryKey(CommonConstants.SerialName);
			if (syscomserial == null) {
				syscomserial = new Syscomserial();
				syscomserial.setSerialname(CommonConstants.SerialName);
				syscomserial.setNumberformat("");
				syscomserial.setResetfield("");
				syscomserial.setInterval(0);
				syscomserial.setNextid(1L);
				syscomserial.setMaxvalue(9000000L);
				syscomserial.setUpdateUser(oDefGroup.getUpdateuserid());
				rst = serialExtMapper.insert(syscomserial);
				nextId = 1;
			} else {
				if (syscomserial.getNextid() != null) {
					syscomserial.setNextid(syscomserial.getNextid() + 1);
					syscomserial.setNumberformat("");
					syscomserial.setInterval(0);
				} else {
					syscomserial.setNextid(1L);
				}

				rst = serialExtMapper.updateByPrimaryKeySelective(syscomserial);
				nextId = syscomserial.getNextid().intValue();
			}

			if (rst == 1) {
				oDefGroup.setGroupid(nextId);
			} else {
				return -1;
			}

			if (oDefGroup.getLocationno() == null) {
				Integer locationno = groupExtMapper.getMaxLocationno();
				if (locationno == null) {
					locationno = 0;
				}
				oDefGroup.setLocationno(locationno + 1);
			}

			return this.groupExtMapper.insert(oDefGroup);

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return -1;
	}

	@Override
	public int updateGroup(Syscomgroup oDefGroup) throws SafeaaException {
		try {
			if (oDefGroup != null && oDefGroup.getGroupid() != null) {
				// oDefGroup.setUpdateuserid(getCurrentUserId());
				// oDefGroup.setUpdatetime(new Date());
				int mResult = groupExtMapper.updateByPrimaryKeySelective(oDefGroup);
				if (mResult <= 0) {
					// 資料不存在無法修改
					addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
				}
				return mResult;
			} else {

				if (StringUtils.isNotBlank(oDefGroup.getGroupno())) {
					Integer groupid = groupExtMapper.queryGroupIdByNo(oDefGroup.getGroupno());
					if (groupid != null) {
						oDefGroup.setGroupid(groupid);
						// oDefGroup.setUpdateuserid(getCurrentUserId());
						// oDefGroup.setUpdatetime(new Date());

						int mResult = groupExtMapper.updateByPrimaryKeySelective(oDefGroup);
						if (mResult <= 0) {
							// 資料不存在無法修改
							this.addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
						}
						return mResult;
					} else {
						// 無此功能群組資料
						this.addError(safeSettings.getCulture(), SAFEMessageId.WithoutGroupData);
					}
				} else {
					// 功能群組代碼不得空白
					this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyGroupNo);
				}
			}

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return -1;
	}

	@Override
	@Transactional(rollbackFor = SafeaaException.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public boolean deleteGroup(Integer groupId) throws SafeaaException {
		try {

			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			int rst1 = rolegroupExtMapper.deleteAllByGroupId(groupId, CommonConstants.ChildTypeGroup); // 2024-01-03 Richard modified

			int rst2 = groupmembersExtMapper.deleteAllByGroupId(groupId);

			int rst3 = groupcultureExtMapper.deleteAllByGroupId(groupId);

			int rst4 = groupExtMapper.deleteByPrimaryKey(new Syscomgroup() {
				private static final long serialVersionUID = 1L;
				{
					this.setGroupid(groupId);
				}
			});

			if (rst1 >= 0 && rst2 >= 0 && rst3 >= 0 && rst4 > 0) {
				return true;
			}
			// 刪除主檔失敗
			addError(safeSettings.getCulture(), SAFEMessageId.DeletMasterFail);

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return false;
	}

	@Override
	public Integer getGroupIdByNo(final String groupNo) throws Exception {
		try {
			if (StringUtils.isBlank(groupNo)) {
				// 功能群組代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyGroupNo);
			}

			Integer groupId = groupExtMapper.queryGroupIdByNo(groupNo);
			if (groupId == null) {
				// 查無資料
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			}
			return groupId;
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return -1;
	}

	@Override
	public Syscomgroup getGroupById(final Syscomgroup oDefGroup) throws Exception {
		try {

			if (oDefGroup == null || oDefGroup.getGroupid() == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			Syscomgroup syscomgroup = groupExtMapper.selectByPrimaryKey(oDefGroup.getGroupid());
			if (syscomgroup == null) {
				// 查無資料
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			}

			return syscomgroup;

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return null;
	}

	@Override
	public SyscomgroupAndCulture getGroupById(Syscomgroup oDefGroup, Syscomgroupculture oDefGroupCulture) throws Exception {
		try {
			if (oDefGroup == null || oDefGroup.getGroupid() == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (oDefGroupCulture == null || StringUtils.isBlank(oDefGroupCulture.getCulture())) {
				oDefGroupCulture.setCulture(safeSettings.getCulture());
			}

			Syscomgroup syscomgroup = groupExtMapper.selectByPrimaryKey(oDefGroup.getGroupid());
			if (syscomgroup != null) {
				Syscomgroupculture syscomroupculture = groupcultureExtMapper.selectByPrimaryKey(oDefGroup.getGroupid(), oDefGroupCulture.getCulture());
				if (syscomroupculture != null) {
					SyscomgroupAndCulture sc = new SyscomgroupAndCulture();
					// 待處理

					return sc;
				} else {
					// 查無資料
					addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
				}
			} else {
				// 查無資料
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			}

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}

		return null;
	}

	@Override
	public List<SyscomgroupAndCulture> getAllGroups(final boolean includeRoot, String culture) throws Exception {
		try {
			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}
			if (includeRoot) {
				return groupExtMapper.queryAllGroups(new SyscomgroupAndCulture(culture));
			} else {
				// 待處理
				return groupExtMapper.queryAllExcludeGroups("SYSCOM", culture);
			}

		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}

		return null;
	}

	@Override
	public SyscomgroupAndCulture getGroupDataById(final Integer groupId, String culture) throws Exception {

		try {
			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}
			SyscomgroupAndCulture sc = new SyscomgroupAndCulture();
			sc.setGroupid(groupId);
			sc.setCulture(culture);
			List<SyscomgroupAndCulture> result = groupExtMapper.queryAllGroups(sc);
			if (result == null || result.size() == 0) {
				// 查無資料
				addError(culture, SAFEMessageId.QueryNoRecord);
			}
			return result.get(0);
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return null;
	}

	@Override
	public List<SyscomgroupAndCulture> getGroupDataByName(final String groupName, String culture) throws Exception {

		try {
			if (StringUtils.isBlank(groupName)) {
				// 功能群組名稱不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyGroupName);
			}
			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			SyscomgroupAndCulture groupCulture = new SyscomgroupAndCulture();
			groupCulture.setGroupname(groupName);
			groupCulture.setCulture(culture);
			List<SyscomgroupAndCulture> rstList = groupExtMapper.queryAllGroups(groupCulture);

			return rstList;
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}

		return null;

	}

	@Override
	public List<SyscomgroupAndCulture> getGroupDataByNo(final String groupNo, String culture) throws Exception {

		try {

			// if (StringUtils.isBlank(groupNo)) {
			// //功能群組代碼不得空白
			// addError(safeSettings.getCulture(), SAFEMessageId.EmptyGroupNo);
			// }

			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			SyscomgroupAndCulture groupCulture = new SyscomgroupAndCulture();
			groupCulture.setGroupno(groupNo);
			groupCulture.setCulture(culture);

			List<SyscomgroupAndCulture> result = groupExtMapper.queryAllGroups(groupCulture);
			if (result == null || result.size() == 0) {
				// 查無資料
				addError(culture, SAFEMessageId.QueryNoRecord);
			}

			return result;

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return null;
	}

	@Override
	public int removeGroupMembers(final Integer groupId, final Integer childId, String childType) throws Exception {

		try {

			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}
			if (childId == null) {
				// 下層序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostChildId);
			}
			if (StringUtils.isBlank(childType)) {
				// 成員類別不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType);
			}

			Syscomgroupmembers syscomgroupmembers = new Syscomgroupmembers();
			syscomgroupmembers.setGroupid(groupId);
			syscomgroupmembers.setChildid(childId);
			syscomgroupmembers.setChildtype(childType);
			return groupmembersExtMapper.deleteByPrimaryKey(syscomgroupmembers);

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 刪除失敗
			addError(safeSettings.getCulture(), SAFEMessageId.DeleteFail);
		}
		return -1;
	}

	@Override
	public int removeAllGroupMembers(final Integer groupId) throws SafeaaException {
		try {
			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			return groupmembersExtMapper.deleteAllByGroupId(groupId);
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 刪除失敗
			addError(safeSettings.getCulture(), SAFEMessageId.DeleteFail);
		}

		return -1;
	}

	@Override
	public List<SyscomgroupmembersAndGroupLevel> getUNSelectedMembersById(Integer groupId, String culture) throws SafeaaException {

		try {
			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}
			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			return groupmembersExtMapper.queryUNSelectMembers(groupId, culture);
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return null;
	}

	@Override
	public List<SyscomgroupmembersAndGroupLevel> getSelectedMembersById(Integer groupId, String culture) throws SafeaaException {

		try {
			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			return groupmembersExtMapper.querySelectedByGroupId(groupId, culture);
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}

		return null;
	}

	@Override
	public List<SyscomgroupmembersAndGroupLevel> getNestedMembersById(Integer groupId, String culture) throws SafeaaException {

		try {

			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			return groupmembersExtMapper.queryNestedMembersByGroupId(groupId, culture);
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}

		return null;
	}

	@Override
	public int addGroupCulture(Syscomgroupculture oDefGroupCulture) throws Exception {

		try {
			if (oDefGroupCulture == null || oDefGroupCulture.getGroupid() == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (StringUtils.isBlank(oDefGroupCulture.getCulture())) {
				oDefGroupCulture.setCulture(safeSettings.getCulture());
			}
			return groupcultureExtMapper.insert(oDefGroupCulture);
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 新增失敗
			addError(safeSettings.getCulture(), SAFEMessageId.InsertCultureFail);
		}

		return -1;
	}

	@Override
	public int addGroupCulture(String groupNo, String culture, String groupName, String remark) throws SafeaaException {

		try {
			if (StringUtils.isBlank(groupNo)) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyGroupNo);
			}

			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			Integer groupId = groupExtMapper.queryGroupIdByNo(groupNo);
			if (groupId == null) {
				addError(safeSettings.getCulture(), SAFEMessageId.WithoutGroupData);
			}

			Syscomgroupculture groupCulture = new Syscomgroupculture();
			groupCulture.setGroupid(groupId);
			groupCulture.setCulture(culture);
			groupCulture.setGroupname(groupName);
			groupCulture.setRemark(remark);
			return groupcultureExtMapper.insert(groupCulture);

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 新增失敗
			addError(safeSettings.getCulture(), SAFEMessageId.InsertFail);
		}
		return -1;
	}

	@Override
	public int updateGroupCulture(Syscomgroupculture oDefGroupCulture) throws SafeaaException {

		try {

			if (oDefGroupCulture == null || oDefGroupCulture.getGroupid() == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (StringUtils.isBlank(oDefGroupCulture.getCulture())) {
				oDefGroupCulture.setCulture(safeSettings.getCulture());
			}

			int result = groupcultureExtMapper.updateByPrimaryKeySelective(oDefGroupCulture);
			if (result == 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
			}

			return result;

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 資料不存在無法修改
			addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
		}
		return -1;
	}

	@Override
	public int updateGroupCulture(String groupNo, String culture, String groupName, String remark) throws Exception {

		try {
			if (StringUtils.isBlank(groupNo)) {
				// 功能群組代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyGroupNo);
			}

			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			Integer groupId = groupExtMapper.queryGroupIdByNo(groupNo);
			if (groupId == null || groupId <= 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.WithoutGroupData);
			}

			Syscomgroupculture groupCulture = new Syscomgroupculture();
			groupCulture.setGroupid(groupId);
			groupCulture.setCulture(culture);
			groupCulture.setGroupname(groupName);
			groupCulture.setRemark(remark);
			return groupcultureExtMapper.updateByPrimaryKey(groupCulture);

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 資料不存在無法修改
			addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
		}
		return -1;
	}

	@Override
	public int removeGroupCulture(Integer groupId, String culture) throws Exception {

		try {

			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			Syscomgroupculture syscomgroupculture = new Syscomgroupculture();
			syscomgroupculture.setGroupid(groupId);
			syscomgroupculture.setCulture(culture);
			int result = groupcultureExtMapper.deleteByPrimaryKey(syscomgroupculture);
			if (result == 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.DeleteNoRecord);
			}
			return result;

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 刪除語系資料失敗
			addError(safeSettings.getCulture(), SAFEMessageId.DeleteCultureFail);
		}
		return -1;
	}

	@Override
	public int removeAllGroupCultures(Integer groupId) throws Exception {

		try {
			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			int result = groupcultureExtMapper.deleteAllByGroupId(groupId);
			if (result == 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.DeleteNoRecord);
			}

			return result;
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 刪除語系資料失敗
			addError(safeSettings.getCulture(), SAFEMessageId.DeleteCultureFail);
		}
		return -1;
	}

	@Override
	public Syscomgroupculture getGroupCultureById(Syscomgroupculture oDefGroupCulture) throws Exception {

		try {

			if (oDefGroupCulture == null || oDefGroupCulture.getGroupid() == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (StringUtils.isBlank(oDefGroupCulture.getCulture())) {
				oDefGroupCulture.setCulture(safeSettings.getCulture());
			}

			return groupcultureExtMapper.selectByPrimaryKey(oDefGroupCulture.getGroupid(), oDefGroupCulture.getCulture());

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return null;
	}

	@Override
	public List<Syscomgroupculture> getGroupCulturesById(Integer groupId) throws Exception {
		try {
			if (groupId == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}
			return groupcultureExtMapper.queryAllByGroupId(groupId);

		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return null;
	}

	@Override
	public int addGroupMembers(Syscomgroupmembers oDefGroupMembers) throws Exception {

		try {
			if (oDefGroupMembers == null || oDefGroupMembers.getGroupid() == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (oDefGroupMembers.getChildid() == null) {
				// 下層序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostChildId);

			}
			if (StringUtils.isBlank(oDefGroupMembers.getChildtype())) {
				// 成員類別不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType);
			}

			if (oDefGroupMembers.getLocationno() == null) {
				Integer locationno = groupmembersExtMapper.getMaxLocationno(oDefGroupMembers.getGroupid());
				if (locationno == null) {
					locationno = 0;
				}
				oDefGroupMembers.setLocationno(locationno + 1);
			}

			int result = groupmembersExtMapper.insert(oDefGroupMembers);
			if (result < 0) {
				// 新增成員資料失敗
				addError(safeSettings.getCulture(), SAFEMessageId.InsertMemberFail);
			}

			return result;
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 新增成員資料失敗
			addError(safeSettings.getCulture(), SAFEMessageId.InsertMemberFail);
		}
		return -1;
	}

	@Override
	public int updateGroupMembers(Syscomgroupmembers oDefGroupMembers) throws Exception {

		try {

			if (oDefGroupMembers == null || oDefGroupMembers.getGroupid() == null) {
				// 功能群組序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId);
			}

			if (oDefGroupMembers.getChildid() <= 0) {
				// 下層序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostChildId);

			}
			if (StringUtils.isBlank(oDefGroupMembers.getChildtype())) {
				// 成員類別不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType);
			}

			int result = groupmembersExtMapper.updateByPrimaryKey(oDefGroupMembers);
			if (result < 0) {
				// 資料不存在無法修改
				addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
			}
			return result;
		} catch (SafeaaException safeaaException) {
			throw safeaaException;
		} catch (Exception e) {
			e.printStackTrace();
			// 資料不存在無法修改
			addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
		}
		return -1;
	}

	@Override
	public int addGroupMembers(String groupNo, String memberNo, String childType, int locationNo,
			java.sql.Date effectDate, java.sql.Date expiredDate) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateGroupMembers(String groupNo, String memberNo, String childType, int locationNo,
			java.sql.Date effectDate, java.sql.Date expiredDate) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SyscomgroupInfoVo> getSyscomgroupInfoVoAll() throws Exception {
		try {

			return groupExtMapper.getSyscomgroupInfoVoAll();
		} catch (Exception e) {
			e.printStackTrace();
			// 查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return null;
	}

	// @Override
	// public int addGroupMembers(String groupNo, String memberNo, String childType, long locationNo, Date effectDate,Date expiredDate) throws Exception{
	// if (StringUtils.isBlank(groupNo)) {
	// addError("", SAFEMessageId.EmptyGroupNo);
	// }
	//
	// if (StringUtils.isBlank(memberNo)) {
	// addError("", SAFEMessageId.EmptyChildNo);
	// }
	// if (StringUtils.isBlank(childType)) {
	// addError("", SAFEMessageId.EmptyChildType);
	// }
	//
	// try {
	//
	// long groupId = groupExtMapper.queryGroupIdByNo(groupNo);
	// if (groupId <=0) {
	// addError("", SAFEMessageId.WithoutGroupData);
	// }
	// Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
	// oDefGroupMembers.setGroupid(groupId);
	// //oDefGroupMembers.setChildid();
	// oDefGroupMembers.setLocationno(locationNo);
	// oDefGroupMembers.setEffectdate(effectDate);
	// oDefGroupMembers.setExpireddate(expiredDate);
	// int result = mDBGroupMember.insert(oDefGroupMembers);
	// if (result < 0 ) {
	// addError("", SAFEMessageId.InsertMemberFail);
	// }
	//
	// }catch(Exception e) {
	// e.printStackTrace();
	// //TODO ,define new message
	// addError("", SAFEMessageId.InsertMemberFail);
	// }
	// return -1;
	// }

	// @Override
	// public int updateGroupMembers(String groupNo, String memberNo, String childType, long locationNo, Date effectDate,
	// Date expiredDate) throws Exception {
	//
	// if (StringUtils.isBlank(groupNo)) {
	// addError("", SAFEMessageId.EmptyGroupNo);
	// }
	//
	// if (StringUtils.isBlank(memberNo)) {
	// addError("", SAFEMessageId.EmptyChildNo);
	// }
	//
	// if (StringUtils.isBlank(childType)) {
	// addError("", SAFEMessageId.EmptyChildType);
	// }
	//
	// try {
	//
	// Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
	// oDefGroupMembers.setGroupid(1L);
	// oDefGroupMembers.setLocationno(locationNo);
	// oDefGroupMembers.setEffectdate(effectDate);
	// oDefGroupMembers.setExpireddate(expiredDate);
	//
	// int result = mDBGroupMember.updateByPrimaryKey(oDefGroupMembers);
	// if (result < 0 ) {
	// addError("", SAFEMessageId.UpdateNoRecord);
	// }
	//
	// }catch(Exception e) {
	// e.printStackTrace();
	// //TODO ,define new message
	// addError("", SAFEMessageId.UpdateNoRecord);
	// }
	// return -1;
	// }

}
