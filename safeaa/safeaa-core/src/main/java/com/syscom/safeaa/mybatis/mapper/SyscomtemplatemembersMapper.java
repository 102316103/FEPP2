package com.syscom.safeaa.mybatis.mapper;

import com.syscom.safeaa.mybatis.model.Syscomtemplatemembers;
import org.apache.ibatis.annotations.Param;

public interface SyscomtemplatemembersMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMTEMPLATEMEMBERS
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Syscomtemplatemembers record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMTEMPLATEMEMBERS
     *
     * @mbg.generated
     */
    int insert(Syscomtemplatemembers record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMTEMPLATEMEMBERS
     *
     * @mbg.generated
     */
    int insertSelective(Syscomtemplatemembers record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMTEMPLATEMEMBERS
     *
     * @mbg.generated
     */
    Syscomtemplatemembers selectByPrimaryKey(@Param("templateid") Integer templateid, @Param("policyid") Integer policyid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMTEMPLATEMEMBERS
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Syscomtemplatemembers record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMTEMPLATEMEMBERS
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Syscomtemplatemembers record);
}