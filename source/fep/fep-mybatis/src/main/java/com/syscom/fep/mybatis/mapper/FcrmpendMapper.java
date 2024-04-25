package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Fcrmpend;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface FcrmpendMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Fcrmpend record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    int insert(Fcrmpend record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    int insertSelective(Fcrmpend record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    Fcrmpend selectByPrimaryKey(@Param("fcrmpendDate") String fcrmpendDate, @Param("fcrmpendTime") String fcrmpendTime, @Param("fcrmpendStan") String fcrmpendStan);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Fcrmpend record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Fcrmpend record);
}