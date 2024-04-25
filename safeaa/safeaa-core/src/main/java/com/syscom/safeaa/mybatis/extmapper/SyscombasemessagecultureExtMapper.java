package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscombasemessagecultureMapper;
import com.syscom.safeaa.mybatis.model.Syscombasemessageculture;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;

@Resource
public interface SyscombasemessagecultureExtMapper extends SyscombasemessagecultureMapper {
    /**
     *
     * @param errorMessageId
     * @param culture
     * @return
     */
    Syscombasemessageculture selectByMessageIdAndCulture(@Param("basemessageno") String errorMessageId,@Param("culture") String culture);
}