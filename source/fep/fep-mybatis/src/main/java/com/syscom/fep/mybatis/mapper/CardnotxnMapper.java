package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Cardnotxn;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface CardnotxnMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDNOTXN
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Cardnotxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDNOTXN
     *
     * @mbg.generated
     */
    int insert(Cardnotxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDNOTXN
     *
     * @mbg.generated
     */
    int insertSelective(Cardnotxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDNOTXN
     *
     * @mbg.generated
     */
    Cardnotxn selectByPrimaryKey(@Param("txDate") String txDate, @Param("ejfno") Integer ejfno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDNOTXN
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Cardnotxn record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDNOTXN
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Cardnotxn record);
}