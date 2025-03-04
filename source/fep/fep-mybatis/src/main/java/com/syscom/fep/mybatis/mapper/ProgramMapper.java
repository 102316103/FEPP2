package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Program;
import org.apache.ibatis.annotations.Param;

public interface ProgramMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PROGRAM
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Program record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PROGRAM
     *
     * @mbg.generated
     */
    int insert(Program record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PROGRAM
     *
     * @mbg.generated
     */
    int insertSelective(Program record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PROGRAM
     *
     * @mbg.generated
     */
    Program selectByPrimaryKey(@Param("programId") String programId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PROGRAM
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Program record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PROGRAM
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Program record);
}