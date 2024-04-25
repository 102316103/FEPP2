package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.BatchMapper;
import com.syscom.fep.mybatis.model.Batch;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Resource
public interface BatchExtMapper extends BatchMapper {

    /**
     * zk add
     */
    Batch getSingleBATCHByDef(String batchName);

    /**
     * xy add
     *
     * @param batchName
     * @return
     */
    List<Batch> queryBatchByName(String batchName);

    /**
     * wj add
     *
     * @param batchName
     * @return
     */
    List<HashMap<String, Object>> getAllBatch(@Param("batchName") String batchName, @Param("subsysList") List<String> subsysList);

    /**
     * zk add
     *
     * @param batchBatchid
     * @return
     */
    List<HashMap<String, Object>> getBatchFirstTaskById(Integer batchBatchid);

    /**
     * 2022-02-09 Richard add
     *
     * @param batchBatchid
     * @param jobsJobid
     * @param jobsSeq
     * @param jobtaskStepid
     * @return
     */
    List<Map<String, Object>> getBatchContext(@Param("batchBatchid") int batchBatchid, @Param("jobsJobid") int jobsJobid, @Param("jobsSeq") int jobsSeq, @Param("jobtaskStepid") int jobtaskStepid);

    /**
     * 2022-03-14 Richard add
     *
     * @param batchName
     * @param batchSubsys
     * @return
     */
    List<Batch> queryScheduledBatchByNameAndSubsys(@Param("batchName") String batchName, @Param("batchSubsys") List<String> batchSubsys);

    /**
     * 取得 Batch All
     */
    List<Batch> getBatchAll();

    List<Batch> getAllBatchByLastRunTime(@Param("batchName") String batchName, @Param("sqlSortExpression") String sqlSortExpression);
}