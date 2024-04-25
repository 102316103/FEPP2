package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Rmnoctl;
import org.apache.ibatis.annotations.Param;

public interface RmnoctlMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMNOCTL
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Rmnoctl record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMNOCTL
     *
     * @mbg.generated
     */
    int insert(Rmnoctl record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMNOCTL
     *
     * @mbg.generated
     */
    int insertSelective(Rmnoctl record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMNOCTL
     *
     * @mbg.generated
     */
    Rmnoctl selectByPrimaryKey(@Param("rmnoctlBrno") String rmnoctlBrno, @Param("rmnoctlCategory") String rmnoctlCategory);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMNOCTL
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Rmnoctl record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMNOCTL
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Rmnoctl record);
}