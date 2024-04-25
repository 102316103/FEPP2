package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Cardmake;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface CardmakeMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKE
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Cardmake record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKE
     *
     * @mbg.generated
     */
    int insert(Cardmake record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKE
     *
     * @mbg.generated
     */
    int insertSelective(Cardmake record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKE
     *
     * @mbg.generated
     */
    Cardmake selectByPrimaryKey(@Param("txDate") String txDate, @Param("seqno") Integer seqno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKE
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Cardmake record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKE
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Cardmake record);
}