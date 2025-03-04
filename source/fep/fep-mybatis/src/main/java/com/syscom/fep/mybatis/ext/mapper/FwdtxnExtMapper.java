package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.ext.model.FwdtxnExt;
import com.syscom.fep.mybatis.mapper.FwdtxnMapper;
import com.syscom.fep.mybatis.model.Fwdrst;
import com.syscom.fep.mybatis.model.Fwdtxn;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Resource
public interface FwdtxnExtMapper extends FwdtxnMapper {

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDTXN
     *
     * @mbg.generated
     *
     * zk add
     */
    List<HashMap<String,Object>> getFwdtxn(Fwdtxn fwdtxn);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDTXN
     *
     * @mbg.generated
     *
     * zk add
     */
    HashMap<String,Object> getFailTimes(Fwdtxn fwdtxn);

    /**
     * zk add
     */
    int lockFWDTXN(Fwdtxn record);

    /**
     * xy add
     * @param fwdrstTxDate
     * @param selectValue
     * @param fwdtxnTxId
     * @param channel
     * @param fwdtxnTroutActno
     * @param fwdtxnTrinBkno
     * @param fwdtxnTrinActno
     * @param fwdtxnTxAmt
     * @param sysFail
     * @return
     */
    List<HashMap<String,Object>>  getFWDTXNByTSBDYFISC(@Param("fwdrstTxDate") String fwdrstTxDate,
                                                @Param("selectValue")String selectValue,
                                                @Param("fwdtxnTxId")String fwdtxnTxId,
                                                @Param("channel") String channel,
                                                @Param("fwdtxnTroutActno") String fwdtxnTroutActno,
                                                @Param("fwdtxnTrinBkno") String fwdtxnTrinBkno,
                                                @Param("fwdtxnTrinActno") String fwdtxnTrinActno,
                                                @Param("fwdtxnTxAmt") String fwdtxnTxAmt,
                                                @Param("sysFail") Short sysFail
    );

    List<FwdtxnExt> getSummary(@Param("fwdrstTxDate") String fwdrstTxDate,
                                               @Param("selectValue")String selectValue,
                                               @Param("fwdtxnTxId")String fwdtxnTxId,
                                               @Param("channel") String channel,
                                               @Param("fwdtxnTroutActno") String fwdtxnTroutActno,
                                               @Param("fwdtxnTrinBkno") String fwdtxnTrinBkno,
                                               @Param("fwdtxnTrinActno") String fwdtxnTrinActno,
                                               @Param("fwdtxnTxAmt") String fwdtxnTxAmt,
                                               @Param("sysFail") Short sysFail
    );


}