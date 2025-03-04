package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Eventguard;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface EventguardMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTGUARD
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Eventguard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTGUARD
     *
     * @mbg.generated
     */
    int insert(Eventguard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTGUARD
     *
     * @mbg.generated
     */
    int insertSelective(Eventguard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTGUARD
     *
     * @mbg.generated
     */
    Eventguard selectByPrimaryKey(@Param("eventguardId") Integer eventguardId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTGUARD
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Eventguard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTGUARD
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Eventguard record);
}