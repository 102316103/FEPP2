package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.MsgkbMapper;
import com.syscom.fep.mybatis.model.Msgkb;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface MsgkbExtMapper extends MsgkbMapper {
    List<Msgkb> getDataTableByPrimaryKey(Msgkb msgkb);

    /**
     * ZK ADD 2021-12-09
     * */
    List<Msgkb> getMSGKB(@Param("errcode") String errcode, @Param("exSubCode") String exSubCode);
}
