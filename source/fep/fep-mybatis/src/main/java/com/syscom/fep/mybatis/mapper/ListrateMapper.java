package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Listrate;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface ListrateMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Listrate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    int insert(Listrate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    int insertSelective(Listrate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    Listrate selectByPrimaryKey(@Param("listrateZoneCode") String listrateZoneCode, @Param("listratePairname") String listratePairname);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Listrate record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Listrate record);
}