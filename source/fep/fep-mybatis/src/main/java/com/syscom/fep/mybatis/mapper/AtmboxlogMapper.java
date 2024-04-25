package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Atmboxlog;
import org.apache.ibatis.annotations.Param;

public interface AtmboxlogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOXLOG
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Atmboxlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOXLOG
     *
     * @mbg.generated
     */
    int insert(Atmboxlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOXLOG
     *
     * @mbg.generated
     */
    int insertSelective(Atmboxlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOXLOG
     *
     * @mbg.generated
     */
    Atmboxlog selectByPrimaryKey(@Param("atmboxlogTxDate") String atmboxlogTxDate, @Param("atmboxlogEjfno") Integer atmboxlogEjfno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOXLOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Atmboxlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMBOXLOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Atmboxlog record);
}