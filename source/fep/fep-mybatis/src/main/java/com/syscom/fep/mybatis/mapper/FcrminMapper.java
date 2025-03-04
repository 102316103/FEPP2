package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Fcrmin;
import org.apache.ibatis.annotations.Param;

public interface FcrminMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMIN
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Fcrmin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMIN
     *
     * @mbg.generated
     */
    int insert(Fcrmin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMIN
     *
     * @mbg.generated
     */
    int insertSelective(Fcrmin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMIN
     *
     * @mbg.generated
     */
    Fcrmin selectByPrimaryKey(@Param("fcrminTxdate") String fcrminTxdate, @Param("fcrminBrno") String fcrminBrno, @Param("fcrminFepno") String fcrminFepno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Fcrmin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Fcrmin record);
}