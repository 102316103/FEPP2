package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Webaudit;
import org.apache.ibatis.annotations.Param;

public interface WebauditMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDIT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Webaudit record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDIT
     *
     * @mbg.generated
     */
    int insert(Webaudit record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDIT
     *
     * @mbg.generated
     */
    int insertSelective(Webaudit record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDIT
     *
     * @mbg.generated
     */
    Webaudit selectByPrimaryKey(@Param("auditno") Long auditno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDIT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Webaudit record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDIT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeyWithBLOBs(Webaudit record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDIT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Webaudit record);
}