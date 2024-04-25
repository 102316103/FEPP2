package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Rmin;
import org.apache.ibatis.annotations.Param;

public interface RminMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMIN
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Rmin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMIN
     *
     * @mbg.generated
     */
    int insert(Rmin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMIN
     *
     * @mbg.generated
     */
    int insertSelective(Rmin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMIN
     *
     * @mbg.generated
     */
    Rmin selectByPrimaryKey(@Param("rminTxdate") String rminTxdate, @Param("rminBrno") String rminBrno, @Param("rminFepno") String rminFepno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Rmin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Rmin record);
}