package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Hmt24atm;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface Hmt24atmMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HMT24ATM
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Hmt24atm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HMT24ATM
     *
     * @mbg.generated
     */
    int insert(Hmt24atm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HMT24ATM
     *
     * @mbg.generated
     */
    int insertSelective(Hmt24atm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HMT24ATM
     *
     * @mbg.generated
     */
    Hmt24atm selectByPrimaryKey(@Param("zone") String zone, @Param("ejfno") Integer ejfno);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HMT24ATM
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Hmt24atm record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HMT24ATM
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Hmt24atm record);
}