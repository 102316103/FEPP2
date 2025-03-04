package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Webauditdesc;
import org.apache.ibatis.annotations.Param;

public interface WebauditdescMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDITDESC
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Webauditdesc record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDITDESC
     *
     * @mbg.generated
     */
    int insert(Webauditdesc record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDITDESC
     *
     * @mbg.generated
     */
    int insertSelective(Webauditdesc record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDITDESC
     *
     * @mbg.generated
     */
    Webauditdesc selectByPrimaryKey(@Param("programid") String programid, @Param("controlid") String controlid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDITDESC
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Webauditdesc record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBAUDITDESC
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Webauditdesc record);
}