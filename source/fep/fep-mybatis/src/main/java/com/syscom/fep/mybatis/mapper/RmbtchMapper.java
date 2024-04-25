package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Rmbtch;
import org.apache.ibatis.annotations.Param;

public interface RmbtchMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCH
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Rmbtch record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCH
     *
     * @mbg.generated
     */
    int insert(Rmbtch record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCH
     *
     * @mbg.generated
     */
    int insertSelective(Rmbtch record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCH
     *
     * @mbg.generated
     */
    Rmbtch selectByPrimaryKey(@Param("rmbtchSenderBank") String rmbtchSenderBank, @Param("rmbtchRemdate") String rmbtchRemdate, @Param("rmbtchTimes") String rmbtchTimes, @Param("rmbtchFepno") String rmbtchFepno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCH
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Rmbtch record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCH
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Rmbtch record);
}