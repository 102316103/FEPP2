package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Cbact;
import org.apache.ibatis.annotations.Param;

public interface CbactMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CBACT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Cbact record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CBACT
     *
     * @mbg.generated
     */
    int insert(Cbact record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CBACT
     *
     * @mbg.generated
     */
    int insertSelective(Cbact record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CBACT
     *
     * @mbg.generated
     */
    Cbact selectByPrimaryKey(@Param("cbactType") String cbactType, @Param("cbactBkno") String cbactBkno, @Param("cbactActno") String cbactActno, @Param("cbactActnoLen") Short cbactActnoLen);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CBACT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Cbact record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CBACT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Cbact record);
}