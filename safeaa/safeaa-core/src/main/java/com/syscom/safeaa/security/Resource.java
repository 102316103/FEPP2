package com.syscom.safeaa.security;

import java.util.List;

import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.core.BaseInterface;
import com.syscom.safeaa.mybatis.model.Syscomresource;
import com.syscom.safeaa.mybatis.vo.SyscomresourceAndCulture;
import com.syscom.safeaa.mybatis.model.Syscomresourceculture;

/**
 * 
 * @author syscom
 *
 */
public interface Resource extends BaseInterface{
	
	/**
	 * 新增Resource
	 * @param oDefResource
	 * @return
	 * @throws SafeaaException
	 */
	public int createResource(Syscomresource oDefResource) throws Exception;
	
	/**
	 * 修改Resource
	 * @param oDefResource
	 * @return
	 * @throws SafeaaException
	 */
	public int updateResource(Syscomresource oDefResource) throws Exception;
	
	/**
	 * 刪除Resource
	 * @param resourceId
	 * @return
	 * @throws SafeaaException
	 */
	public boolean deleteResource(Integer resourceId) throws SafeaaException;
	
	/**
	 * 查詢ResourceId
	 * @param resourceNo
	 * @return
	 * @throws SafeaaException
	 */
	public Integer getResourceIdByNo(String resourceNo) throws SafeaaException;
	
	/**
	 * 查詢ResourceId
	 * @param resourceId
	 * @return
	 * @throws Exception
	 */
	public Syscomresource getResourceById(Integer resourceId) throws Exception;
	
	/**
	 * 查詢ResourceId
	 * @param oDefResourc
	 * @param oDefResourceCulture
	 * @return
	 * @throws Exception
	 */
	public int getResourceById(Syscomresource oDefResourc,Syscomresourceculture oDefResourceCulture) throws Exception;

	/**
	 * 查詢SyscomresourceAndCulture
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	public List<SyscomresourceAndCulture> getAllResources(String culture) throws Exception;
	
	/**
	 * 查詢SyscomresourceAndCulture
	 * @param resourceId
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	public List<SyscomresourceAndCulture> getResourceDataById(Integer resourceId,String culture) throws Exception;
	
	/**
	 * 查詢SyscomresourceAndCulture
	 * @param resourceName
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	public List<SyscomresourceAndCulture> getResourceDataByName(String resourceName,String culture) throws Exception;
	
	/**
	 * 查詢SyscomresourceAndCulture
	 * @param resourceNo
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	public List<SyscomresourceAndCulture> getResourceDataByNo(String resourceNo,String culture) throws Exception;
	
	/**
	 * 新增ResourceCulture
	 * @param oDefResourceCulture
	 * @return
	 * @throws Exception
	 */
	public int addResourceCulture(Syscomresourceculture oDefResourceCulture) throws Exception;
	
	/**
	 * 新增ResourceCulture
	 * @param resourceNo
	 * @param culture
	 * @param resourceName
	 * @param remark
	 * @return
	 * @throws Exception
	 */
	public int addResourceCulture(String resourceNo,String culture,String resourceName,String remark) throws Exception;
	
	/**
	 * 修改ResourceCulture
	 * @param oDefResourceCulture
	 * @return
	 * @throws Exception
	 */
	public int updateResourceCulture(Syscomresourceculture oDefResourceCulture) throws Exception;
	
	/**
	 * 修改ResourceCulture
	 * @param resourceNo
	 * @param culture
	 * @param resourceName
	 * @param remark
	 * @return
	 * @throws Exception
	 */
	public int updateResourceCulture(String resourceNo,String culture,String resourceName,String remark) throws Exception;
	
	/**
	 * 刪除ResourceCulture
	 * @param resourceId
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	public int removeResourceCulture(Integer resourceId,String culture) throws Exception;
	
	/**
	 * 刪除ResourceCulture
	 * @param resourceId
	 * @return
	 * @throws Exception
	 */
	public int removeAllResourceCultures(Integer resourceId) throws Exception;

	/**
	 * 查詢ResourceCulture
	 * @param resourceId
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	public Syscomresourceculture getResourceCultureById(Integer resourceId,String culture) throws Exception;
	
	/**
	 * 查詢ResourceCulture
	 * @param resourceId
	 * @return
	 * @throws Exception
	 */
    public List<Syscomresourceculture> getResourceCulturesById(Integer resourceId) throws Exception;

}
