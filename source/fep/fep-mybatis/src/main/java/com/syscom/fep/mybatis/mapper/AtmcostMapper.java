package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Atmcost;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface AtmcostMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCOST
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Atmcost record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCOST
     *
     * @mbg.generated
     */
    int insert(Atmcost record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCOST
     *
     * @mbg.generated
     */
    int insertSelective(Atmcost record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCOST
     *
     * @mbg.generated
     */
    Atmcost selectByPrimaryKey(@Param("atmcostTxMm") String atmcostTxMm, @Param("atmcostAtmno") String atmcostAtmno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCOST
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Atmcost record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCOST
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Atmcost record);
}