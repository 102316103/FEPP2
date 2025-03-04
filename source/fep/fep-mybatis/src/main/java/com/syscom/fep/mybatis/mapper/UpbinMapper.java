package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Upbin;
import org.apache.ibatis.annotations.Param;

public interface UpbinMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UPBIN
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Upbin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UPBIN
     *
     * @mbg.generated
     */
    int insert(Upbin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UPBIN
     *
     * @mbg.generated
     */
    int insertSelective(Upbin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UPBIN
     *
     * @mbg.generated
     */
    Upbin selectByPrimaryKey(@Param("upbinBinLength") Short upbinBinLength, @Param("upbinBin") String upbinBin);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UPBIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Upbin record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UPBIN
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Upbin record);
}