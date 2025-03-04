package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.Rmfiscout1Mapper;
import com.syscom.fep.mybatis.model.Rmfiscout1;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface Rmfiscout1ExtMapper extends Rmfiscout1Mapper {

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMFISCOUT1
     *
     * @mbg.generated
     */
    int updateRMFISCOUT1byFISCResNO(Rmfiscout1 record);

    List<HashMap<String, Object>> queryByPrimaryKey(Rmfiscout1 record);


    /**
     * xy add BtBatchDaily 調用
     */
    int updateRMFISCOUT1forBatchDaily();
}