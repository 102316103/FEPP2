package com.syscom.safeaa.mybatis.mapper;

import com.syscom.safeaa.mybatis.model.Syscomuserstatus;
import org.apache.ibatis.annotations.Param;

public interface SyscomuserstatusMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMUSERSTATUS
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Syscomuserstatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMUSERSTATUS
     *
     * @mbg.generated
     */
    int insert(Syscomuserstatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMUSERSTATUS
     *
     * @mbg.generated
     */
    int insertSelective(Syscomuserstatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMUSERSTATUS
     *
     * @mbg.generated
     */
    Syscomuserstatus selectByPrimaryKey(@Param("userid") Integer userid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMUSERSTATUS
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Syscomuserstatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMUSERSTATUS
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Syscomuserstatus record);
}