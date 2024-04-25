package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.MobrapMapper;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @author xingyun_yang
 * @create 2021/9/16
 */
@Resource
public interface MobrapExtMapper extends MobrapMapper {

    /**
     * xy add
     * UI019400 引用
     * @param stDate
     * @param pcode
     * @param apId
     * @param txType
     * @param brno
     * @param deptCode
     * @return
     */
    List<HashMap<String,Object>> getMOBRAPBySTDateForUI(@Param("stDate") String stDate,
                                                        @Param("pcode") String pcode,
                                                        @Param("apId") String apId,
                                                        @Param("txType") String txType,
                                                        @Param("brno") String brno,
                                                        @Param("deptCode") String deptCode,
                                                        @Param("brapCur") String brapCur);
}
