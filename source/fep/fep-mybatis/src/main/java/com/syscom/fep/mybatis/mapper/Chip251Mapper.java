package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Chip251;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface Chip251Mapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP251
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Chip251 record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP251
     *
     * @mbg.generated
     */
    int insert(Chip251 record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP251
     *
     * @mbg.generated
     */
    int insertSelective(Chip251 record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP251
     *
     * @mbg.generated
     */
    Chip251 selectByPrimaryKey(@Param("chip251Tbsdy") String chip251Tbsdy, @Param("chip251Stan") String chip251Stan);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP251
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Chip251 record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP251
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Chip251 record);
}