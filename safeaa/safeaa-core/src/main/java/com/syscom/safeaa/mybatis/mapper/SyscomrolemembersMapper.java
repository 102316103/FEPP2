package com.syscom.safeaa.mybatis.mapper;

import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import org.apache.ibatis.annotations.Param;

public interface SyscomrolemembersMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEMEMBERS
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Syscomrolemembers record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEMEMBERS
     *
     * @mbg.generated
     */
    int insert(Syscomrolemembers record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEMEMBERS
     *
     * @mbg.generated
     */
    int insertSelective(Syscomrolemembers record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEMEMBERS
     *
     * @mbg.generated
     */
    Syscomrolemembers selectByPrimaryKey(@Param("roleid") Integer roleid, @Param("childid") Integer childid, @Param("childtype") String childtype);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEMEMBERS
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Syscomrolemembers record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEMEMBERS
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Syscomrolemembers record);
}