package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Mowkpostaccode;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface MowkpostaccodeMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTACCODE
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Mowkpostaccode record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTACCODE
     *
     * @mbg.generated
     */
    int insert(Mowkpostaccode record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MOWKPOSTACCODE
     *
     * @mbg.generated
     */
    int insertSelective(Mowkpostaccode record);
}