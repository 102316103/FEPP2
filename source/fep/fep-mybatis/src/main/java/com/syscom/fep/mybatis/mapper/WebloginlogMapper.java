package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Webloginlog;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface WebloginlogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Webloginlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    int insert(Webloginlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    int insertSelective(Webloginlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    Webloginlog selectByPrimaryKey(@Param("logno") Integer logno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Webloginlog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Webloginlog record);
}