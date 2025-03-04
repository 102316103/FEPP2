package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Chip24x;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface Chip24xMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP24X
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Chip24x record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP24X
     *
     * @mbg.generated
     */
    int insert(Chip24x record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP24X
     *
     * @mbg.generated
     */
    int insertSelective(Chip24x record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP24X
     *
     * @mbg.generated
     */
    Chip24x selectByPrimaryKey(@Param("chip24xTbsdy") String chip24xTbsdy, @Param("chip24xStan") String chip24xStan);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP24X
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Chip24x record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHIP24X
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Chip24x record);
}