package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Guard;
import org.apache.ibatis.annotations.Param;

public interface GuardMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GUARD
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Guard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GUARD
     *
     * @mbg.generated
     */
    int insert(Guard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GUARD
     *
     * @mbg.generated
     */
    int insertSelective(Guard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GUARD
     *
     * @mbg.generated
     */
    Guard selectByPrimaryKey(@Param("guardNo") String guardNo);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GUARD
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Guard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GUARD
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Guard record);
}