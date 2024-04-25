package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Atmbox;
import org.apache.ibatis.annotations.Param;

public interface AtmboxMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOX
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Atmbox record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOX
     *
     * @mbg.generated
     */
    int insert(Atmbox record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOX
     *
     * @mbg.generated
     */
    int insertSelective(Atmbox record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOX
     *
     * @mbg.generated
     */
    Atmbox selectByPrimaryKey(@Param("atmboxId") Integer atmboxId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOX
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Atmbox record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOX
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Atmbox record);
}