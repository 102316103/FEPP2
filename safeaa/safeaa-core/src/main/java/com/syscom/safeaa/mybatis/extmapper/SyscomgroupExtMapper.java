package com.syscom.safeaa.mybatis.extmapper;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.safeaa.mybatis.mapper.SyscomgroupMapper;
import com.syscom.safeaa.mybatis.vo.SyscomgroupAndCulture;
import com.syscom.safeaa.mybatis.vo.SyscomgroupInfoVo;

@Resource
public interface SyscomgroupExtMapper extends SyscomgroupMapper {
	/**
	 * Query function group data by groupNo
	 *
	 * <p>
	 * 以功能群組代碼查出功能群組序號
	 * </p>
	 * 
	 * @param groupNo 功能群組代碼
	 * @return 功能群組序號
	 */
	Integer queryGroupIdByNo(@Param("groupno") String groupno);

	/**
	 * <p>
	 * 以傳入語系查出所有功能群組資料(含語系資料)
	 * </p>
	 * <p>
	 * remark
	 * <li>查詢條件要注意常數字串的單引號
	 *
	 * @param group
	 * @return 有功能群組資料(含語系資料)
	 */
	List<SyscomgroupAndCulture> queryAllGroups(SyscomgroupAndCulture groupCulture);

	/**
	 * query the groups for the culture exclude groupno='groupno'
	 * 
	 * @param groupno
	 * @param culture
	 * @return
	 */
	List<SyscomgroupAndCulture> queryAllExcludeGroups(@Param("groupno") String groupno, @Param("culture") String culture);

	List<SyscomgroupInfoVo> getSyscomgroupInfoVoAll();

	Integer getMaxLocationno();

}