package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Ictltxn;
import org.apache.ibatis.annotations.Param;

public interface IctltxnMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ICTLTXN
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Ictltxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ICTLTXN
     *
     * @mbg.generated
     */
    int insert(Ictltxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ICTLTXN
     *
     * @mbg.generated
     */
    int insertSelective(Ictltxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ICTLTXN
     *
     * @mbg.generated
     */
    Ictltxn selectByPrimaryKey(@Param("ictltxnTxDate") String ictltxnTxDate, @Param("ictltxnEjfno") Integer ictltxnEjfno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ICTLTXN
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Ictltxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ICTLTXN
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Ictltxn record);
}