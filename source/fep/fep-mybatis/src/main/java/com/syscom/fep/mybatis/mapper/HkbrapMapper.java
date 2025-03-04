package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Hkbrap;
import org.apache.ibatis.annotations.Param;

public interface HkbrapMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKBRAP
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Hkbrap record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKBRAP
     *
     * @mbg.generated
     */
    int insert(Hkbrap record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKBRAP
     *
     * @mbg.generated
     */
    int insertSelective(Hkbrap record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKBRAP
     *
     * @mbg.generated
     */
    Hkbrap selectByPrimaryKey(@Param("hkbrapSeqno") Integer hkbrapSeqno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKBRAP
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Hkbrap record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKBRAP
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Hkbrap record);
}