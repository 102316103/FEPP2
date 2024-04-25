package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.model.Atmchannel;
import javax.annotation.Resource;
import org.apache.ibatis.annotations.Param;

@Resource
public interface AtmchannelMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCHANNEL
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Atmchannel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCHANNEL
     *
     * @mbg.generated
     */
    int insert(Atmchannel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCHANNEL
     *
     * @mbg.generated
     */
    int insertSelective(Atmchannel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCHANNEL
     *
     * @mbg.generated
     */
    Atmchannel selectByPrimaryKey(@Param("atmchannelNo") String atmchannelNo);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCHANNEL
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Atmchannel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMCHANNEL
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Atmchannel record);
}