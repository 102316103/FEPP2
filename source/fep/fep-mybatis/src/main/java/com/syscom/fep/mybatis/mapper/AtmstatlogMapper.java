package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Atmstatlog;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface AtmstatlogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMSTATLOG
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Atmstatlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMSTATLOG
     *
     * @mbg.generated
     */
    int insert(Atmstatlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMSTATLOG
     *
     * @mbg.generated
     */
    int insertSelective(Atmstatlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMSTATLOG
     *
     * @mbg.generated
     */
    Atmstatlog selectByPrimaryKey(@Param("atmstatlogNo") Integer atmstatlogNo);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMSTATLOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Atmstatlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMSTATLOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Atmstatlog record);
}