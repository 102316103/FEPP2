package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Hotcard;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface HotcardMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTCARD
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Hotcard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTCARD
     *
     * @mbg.generated
     */
    int insert(Hotcard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTCARD
     *
     * @mbg.generated
     */
    int insertSelective(Hotcard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTCARD
     *
     * @mbg.generated
     */
    Hotcard selectByPrimaryKey(@Param("panNo") String panNo);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTCARD
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Hotcard record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTCARD
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Hotcard record);
}