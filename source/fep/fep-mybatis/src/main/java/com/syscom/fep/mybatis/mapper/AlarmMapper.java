package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Alarm;
import org.apache.ibatis.annotations.Param;

public interface AlarmMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ALARM
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Alarm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ALARM
     *
     * @mbg.generated
     */
    int insert(Alarm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ALARM
     *
     * @mbg.generated
     */
    int insertSelective(Alarm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ALARM
     *
     * @mbg.generated
     */
    Alarm selectByPrimaryKey(@Param("alarmNo") String alarmNo);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ALARM
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Alarm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ALARM
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Alarm record);
}