package com.syscom.safeaa.security.impl;

import java.math.BigDecimal;
import java.util.Date;
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
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupmembersExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomresourceExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomresourcecultureExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomrolegroupExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomserialExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomresource;
import com.syscom.safeaa.mybatis.vo.SyscomresourceAndCulture;
import com.syscom.safeaa.mybatis.model.Syscomresourceculture;
import com.syscom.safeaa.mybatis.model.Syscomserial;
import com.syscom.safeaa.security.Resource;
import com.syscom.safeaa.utils.SyscomConfig;

/**
 * 
 * @author syscom
 *
 */
@Component
public class ResourceImpl extends ApplicationBase implements Resource {

	@Autowired
	private SyscomConfig safeSettings;

	@Autowired
	private SyscomresourceExtMapper resourceMapper;

	@Autowired
	private SyscomresourcecultureExtMapper resourcecultureMapper;

	@Autowired
	private SyscomrolegroupExtMapper rolegroupMapper;

	@Autowired
	private SyscomserialExtMapper serialExtMapper;
	
	@Autowired
	private SyscomgroupmembersExtMapper groupmembersExtMapper;

	@Override
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public int createResource(Syscomresource oDefResource) throws Exception {
		try {

			if (StringUtils.isBlank(oDefResource.getResourceno())) {
				// 資源代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyResourceNo);
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
				syscomserial.setUpdateUser(oDefResource.getUpdateuserid());
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
				oDefResource.setResourceid(nextId);
			}else {
				return -1;
			}

			return resourceMapper.insert(oDefResource);

		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public int updateResource(Syscomresource oDefResource) throws Exception {
		try {
			Integer resourceid = oDefResource.getResourceid();
			if (resourceid != null) {
				oDefResource.setUpdateuserid(getCurrentUserId());
				oDefResource.setUpdatetime(new Date());
				int mResult = resourceMapper.updateByPrimaryKeySelective(oDefResource);
				if (mResult <= 0) {
					addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
				}
				
				return mResult;
			} else {
				if (StringUtils.isNotBlank(oDefResource.getResourceno())) {
					resourceid = resourceMapper.queryResourceIdByNo(oDefResource.getResourceno());
					if (resourceid != null) {
						oDefResource.setResourceid(resourceid);
						oDefResource.setUpdateuserid(getCurrentUserId());
						oDefResource.setUpdatetime(new Date());
						//Syscomresource oBefore = resourceMapper.queryByPrimaryKey(resourceid);
						int mResult = resourceMapper.updateByPrimaryKeySelective(oDefResource);
						if (mResult <= 0) {
							addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
						}
						//insertAuditTrail(getCurrentUserId(), "update", "SYSCOMRESOURCE", Long.toString(resourceid), oBefore, oDefResource);
						return mResult;
					} else {
						// 無此資源資料
						addError(safeSettings.getCulture(), SAFEMessageId.WithoutResourceData);
						return -1;
					}
				} else {
					// 資源代碼不得空白
					addError(safeSettings.getCulture(), SAFEMessageId.EmptyResourceNo);
					return -1;
				}
			}

		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public boolean deleteResource(Integer resourceid) throws SafeaaException {
		try {
			
			Syscomresource syscomresource = new Syscomresource();
			syscomresource.setResourceid(resourceid);

			int rst1 = groupmembersExtMapper.deleteAllByResourceId(resourceid);
			int rst2 = resourcecultureMapper.deleteAllByResourceId(resourceid);
			int rst3 = resourceMapper.deleteByPrimaryKey(syscomresource);

			if (rst1>=0 && rst2>=0 && rst3>=0) {
				return true;
			}

		} catch (Exception e) {
			throw new SafeaaException(safeSettings.culture, SAFEMessageId.DeletMasterFail, e);
		}
		return false;
	}

	@Override
	public Integer getResourceIdByNo(String resourceNo) throws SafeaaException {
		try {
			if (StringUtils.isBlank(resourceNo)) {
				// 資源代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyResourceNo);
			}

			Integer resourceid = resourceMapper.queryResourceIdByNo(resourceNo);
			if (resourceid == null) {
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
			}

			return resourceid;

		}catch(SafeaaException safeaaException) {
			throw safeaaException;
		}catch(Exception e) {
			e.printStackTrace();
			//查詢失敗
			addError(safeSettings.getCulture(), SAFEMessageId.QueryFail);
		}
		return -1;

	}

	@Override
	public Syscomresource getResourceById(Integer resourceid) throws Exception {

		try {
			if (resourceid == null) {
				addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
				return null;
			}

			Syscomresource syscomresource = resourceMapper.selectByPrimaryKey(resourceid);
			if (syscomresource != null) {
				return syscomresource;
			} else {
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
				return null;
			}

		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public int getResourceById(Syscomresource oDefResource, Syscomresourceculture oDefResourceCulture)
			throws Exception {
		Syscomresource rDefResource = oDefResource;
		Syscomresourceculture rDefResourceCulture = oDefResourceCulture;

		try {
			if (oDefResource.getResourceid() == null) {
				// 資源序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return -1;
			}
			if (StringUtils.isBlank(oDefResourceCulture.getCulture())) {
				oDefResourceCulture.setCulture(safeSettings.getCulture());
			}

			rDefResource = resourceMapper.selectByPrimaryKey(oDefResource.getResourceid());
			if (rDefResource != null) {
				rDefResourceCulture = resourcecultureMapper.selectByPrimaryKey(oDefResourceCulture.getResourceid(),
						oDefResourceCulture.getCulture());
				if (rDefResourceCulture != null) {
					return 1;
				} else {
					addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
					return 0;
				}

			} else {
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
				return 0;
			}

		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	public List<SyscomresourceAndCulture> getAllResources(String culture) throws Exception{
		try {
			if (StringUtils.isBlank(culture)) {
				culture =safeSettings.getCulture();
			}
			
			SyscomresourceAndCulture sc = new SyscomresourceAndCulture();
			sc.setCulture(culture);			
			List<SyscomresourceAndCulture> list = resourceMapper.queryAllResources(sc);
			return list;
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public List<SyscomresourceAndCulture> getResourceDataById(Integer resourceId,String culture) throws Exception{
		try {
			if (resourceId == null) {
				// 資源序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return null;
			}
			if (StringUtils.isBlank(culture)) {
				culture =safeSettings.getCulture();
			}
			
			SyscomresourceAndCulture sc = new SyscomresourceAndCulture();
			sc.setResourceid(resourceId);
			sc.setCulture(culture);
			
			List<SyscomresourceAndCulture> list = resourceMapper.queryAllResources(sc);
			return list;
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public List<SyscomresourceAndCulture> getResourceDataByName(String resourceName,String culture) throws Exception{
		try {
			if (StringUtils.isBlank(resourceName)) {
				//資源名稱不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyResourceName);
				return null;
			}
			if (StringUtils.isBlank(culture)) {
				culture =safeSettings.getCulture();
			}
			
			SyscomresourceAndCulture sc = new SyscomresourceAndCulture();
			sc.setResourcename(resourceName);
			sc.setCulture(culture);
			List<SyscomresourceAndCulture> list = resourceMapper.queryAllResources(sc);
			return list;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public List<SyscomresourceAndCulture> getResourceDataByNo(String resourceNo,String culture) throws Exception{
		try {
			if (StringUtils.isBlank(resourceNo)) {
				// 資源代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyResourceNo);
				return null;
			}
			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}
			SyscomresourceAndCulture sc = new SyscomresourceAndCulture();
			sc.setResourceno(resourceNo);
			sc.setCulture(culture);
			List<SyscomresourceAndCulture> list = resourceMapper.queryAllResources(sc);
			return list;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public int addResourceCulture(Syscomresourceculture oDefResourceCulture) throws Exception {
		try {
			if (oDefResourceCulture.getResourceid() == null) {
				// 資源序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return -1;
			}
			if (StringUtils.isBlank(oDefResourceCulture.getCulture())) {
				oDefResourceCulture.setCulture(safeSettings.getCulture());
			}

			int rst = resourcecultureMapper.insertSelective(oDefResourceCulture);
			if (rst <= 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.InsertCultureFail);			
			}
			return rst;
		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	@Transactional(rollbackFor=Exception.class, transactionManager = DataSourceSafeaaConstant.BEAN_NAME_TRANSACTION_MANAGER)
	public int addResourceCulture(String resourceNo, String culture, String resourceName, String remark)
			throws Exception {
		try {
			if (StringUtils.isBlank(resourceNo)) {
				// 資源代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyResourceNo);
				return -1;
			}
			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			Integer resourceid = resourceMapper.queryResourceIdByNo(resourceNo);
			if (resourceid != null) {
				Syscomresourceculture oDefResourceCulture = new Syscomresourceculture();
				oDefResourceCulture.setResourceid(resourceid);
				oDefResourceCulture.setCulture(culture);
				oDefResourceCulture.setRemark(remark);
				oDefResourceCulture.setResourcename(resourceName);

				int rst = resourcecultureMapper.insert(oDefResourceCulture);
				if (rst <= 0) {
					addError(safeSettings.getCulture(), SAFEMessageId.InsertCultureFail);
				}
				return rst;

			} else {
				addError(safeSettings.getCulture(), SAFEMessageId.WithoutResourceData);
				return -1;
			}
		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	public int updateResourceCulture(Syscomresourceculture oDefResourceCulture) throws Exception {
		try {
			if (oDefResourceCulture.getResourceid() == null) {
				addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
				return -1;
			}
			if (StringUtils.isBlank(oDefResourceCulture.getCulture())) {
				oDefResourceCulture.setCulture(safeSettings.getCulture());
			}

			int rst = resourcecultureMapper.updateByPrimaryKeySelective(oDefResourceCulture);
			if (rst <= 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
			}
			return rst;

		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	public int updateResourceCulture(String resourceNo, String culture, String resourceName, String remark)
			throws Exception {
		try {
			if (StringUtils.isBlank(resourceNo)) {
				// 資源代碼不得空白
				addError(safeSettings.getCulture(), SAFEMessageId.EmptyResourceNo);
				return -1;
			}
			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}
			Integer resourceid = resourceMapper.queryResourceIdByNo(resourceNo);
			if (resourceid != null) {
				Syscomresourceculture oDefResourceCulture = new Syscomresourceculture();
				oDefResourceCulture.setResourceid(resourceid);
				oDefResourceCulture.setCulture(culture);
				oDefResourceCulture.setRemark(remark);
				oDefResourceCulture.setResourcename(resourceName);

				int rst = resourcecultureMapper.updateByPrimaryKey(oDefResourceCulture);
				if (rst <= 0) {
					addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
				}
				return rst;

			} else {
				addError(safeSettings.getCulture(), SAFEMessageId.WithoutResourceData);
				return -1;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public int removeResourceCulture(Integer resourceId, String culture) throws Exception {
		try {
			if (resourceId == null) {
				// 資源序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return -1;
			}
			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			Syscomresourceculture syscomresourceculture = new Syscomresourceculture();
			syscomresourceculture.setResourceid(resourceId);
			syscomresourceculture.setCulture(culture);
			int rst = resourcecultureMapper.deleteByPrimaryKey(syscomresourceculture);
			if (rst <= 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.DeleteNoRecord);
			}
			return rst;
		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	public int removeAllResourceCultures(Integer resourceId) throws Exception {
		try {
			if (resourceId == null) {
				// 資源序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return -1;
			}
			int rst = resourcecultureMapper.deleteAllByResourceId(resourceId);
			if (rst <= 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.DeleteNoRecord);
			}
			return rst;
		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	public Syscomresourceculture getResourceCultureById(Integer resourceId,String culture) throws Exception {
		try {
			if (resourceId == null) {
				// 資源序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return null;
			}
			if (StringUtils.isBlank(culture)) {
				culture = safeSettings.getCulture();
			}

			Syscomresourceculture oDefResourceCulture = resourcecultureMapper.selectByPrimaryKey(resourceId,culture);
			if (oDefResourceCulture != null) {
				return oDefResourceCulture;
			} else {
				addError(safeSettings.getCulture(), SAFEMessageId.QueryNoRecord);
				return null;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public List<Syscomresourceculture> getResourceCulturesById(Integer resourceId) throws Exception {
		try {
			if (resourceId == null) {
				// 資源序號未傳入
				addError(safeSettings.getCulture(), SAFEMessageId.LostResourceId);
				return null;
			}
			
			return resourcecultureMapper.queryAllByResourceId(resourceId);
			
		} catch (Exception e) {
			throw e;
		}

	}

}
