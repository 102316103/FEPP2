package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Sysconf;
import org.apache.ibatis.annotations.Param;

public interface SysconfMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCONF
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Sysconf record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCONF
     *
     * @mbg.generated
     */
    int insert(Sysconf record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCONF
     *
     * @mbg.generated
     */
    int insertSelective(Sysconf record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCONF
     *
     * @mbg.generated
     */
    Sysconf selectByPrimaryKey(@Param("sysconfSubsysno") Short sysconfSubsysno, @Param("sysconfName") String sysconfName);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCONF
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Sysconf record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCONF
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Sysconf record);
}