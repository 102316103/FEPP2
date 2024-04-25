package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Prgstat;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface PrgstatMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Prgstat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    int insert(Prgstat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    int insertSelective(Prgstat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    Prgstat selectByPrimaryKey(@Param("prgstatProgramid") String prgstatProgramid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Prgstat record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Prgstat record);
}