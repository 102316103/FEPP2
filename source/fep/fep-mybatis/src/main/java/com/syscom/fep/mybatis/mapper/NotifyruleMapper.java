package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Notifyrule;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface NotifyruleMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYRULE
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Notifyrule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYRULE
     *
     * @mbg.generated
     */
    int insert(Notifyrule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYRULE
     *
     * @mbg.generated
     */
    int insertSelective(Notifyrule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYRULE
     *
     * @mbg.generated
     */
    Notifyrule selectByPrimaryKey(@Param("ruleId") Long ruleId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYRULE
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Notifyrule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYRULE
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Notifyrule record);
}