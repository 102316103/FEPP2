package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Whoisactive;
import javax.annotation.Resource;

@Resource
public interface WhoisactiveMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WHOISACTIVE
     *
     * @mbg.generated
     */
    int insert(Whoisactive record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WHOISACTIVE
     *
     * @mbg.generated
     */
    int insertSelective(Whoisactive record);
}