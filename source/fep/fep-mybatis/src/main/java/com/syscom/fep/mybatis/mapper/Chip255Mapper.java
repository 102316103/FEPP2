package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Chip255;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface Chip255Mapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP255
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Chip255 record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP255
     *
     * @mbg.generated
     */
    int insert(Chip255 record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP255
     *
     * @mbg.generated
     */
    int insertSelective(Chip255 record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP255
     *
     * @mbg.generated
     */
    Chip255 selectByPrimaryKey(@Param("chip255Tbsdy") String chip255Tbsdy, @Param("chip255Stan") String chip255Stan);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP255
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Chip255 record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP255
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Chip255 record);
}