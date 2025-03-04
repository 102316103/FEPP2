package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Retain;
import java.util.Date;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface RetainMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RETAIN
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Retain record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RETAIN
     *
     * @mbg.generated
     */
    int insert(Retain record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RETAIN
     *
     * @mbg.generated
     */
    int insertSelective(Retain record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RETAIN
     *
     * @mbg.generated
     */
    Retain selectByPrimaryKey(@Param("retainTime") Date retainTime, @Param("retainAtmno") String retainAtmno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RETAIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Retain record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RETAIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Retain record);
}