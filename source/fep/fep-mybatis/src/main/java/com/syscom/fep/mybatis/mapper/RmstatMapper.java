package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Rmstat;

public interface RmstatMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Rmstat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    int insert(Rmstat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    int insertSelective(Rmstat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    Rmstat selectByPrimaryKey(String rmstatHbkno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Rmstat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Rmstat record);
}