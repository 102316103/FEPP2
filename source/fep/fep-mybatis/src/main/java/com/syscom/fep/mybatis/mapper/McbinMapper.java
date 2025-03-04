package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Mcbin;
import org.apache.ibatis.annotations.Param;

public interface McbinMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MCBIN
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Mcbin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MCBIN
     *
     * @mbg.generated
     */
    int insert(Mcbin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MCBIN
     *
     * @mbg.generated
     */
    int insertSelective(Mcbin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MCBIN
     *
     * @mbg.generated
     */
    Mcbin selectByPrimaryKey(@Param("mcbinFromBin") String mcbinFromBin, @Param("mcbinToBin") String mcbinToBin);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MCBIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Mcbin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table MCBIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Mcbin record);
}