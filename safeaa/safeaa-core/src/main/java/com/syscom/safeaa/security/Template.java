package com.syscom.safeaa.security;

import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.mybatis.model.Syscomtemplate;

public interface Template {
    /**
     * Create one new SyscomTemplate record.
     * 建立一筆管理原則範本資料
     * @param syscomtemplate
     * @return
     * @throws SafeaaException
     */
    public int createTemplate(Syscomtemplate syscomtemplate) throws SafeaaException;

    /**
     * Modify one SyscomTemplate record.
     * 修改一筆管理原則範本資料
     * @param syscomtemplate
     * @return
     * @throws SafeaaException
     */
    public int updateTemplate(Syscomtemplate syscomtemplate) throws SafeaaException;
}
