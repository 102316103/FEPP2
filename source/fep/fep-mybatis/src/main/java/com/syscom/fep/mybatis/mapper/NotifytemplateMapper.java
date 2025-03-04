package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Notifytemplate;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface NotifytemplateMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYTEMPLATE
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Notifytemplate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYTEMPLATE
     *
     * @mbg.generated
     */
    int insert(Notifytemplate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYTEMPLATE
     *
     * @mbg.generated
     */
    int insertSelective(Notifytemplate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYTEMPLATE
     *
     * @mbg.generated
     */
    Notifytemplate selectByPrimaryKey(@Param("templateId") String templateId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYTEMPLATE
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Notifytemplate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table NOTIFYTEMPLATE
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Notifytemplate record);
}