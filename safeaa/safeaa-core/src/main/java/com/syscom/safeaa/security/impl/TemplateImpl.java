package com.syscom.safeaa.security.impl;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.constant.CommonConstants;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.extmapper.SyscomtemplateExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomtemplate;
import com.syscom.safeaa.security.Serial;
import com.syscom.safeaa.security.Template;
import com.syscom.safeaa.utils.DbHelper;
import com.syscom.safeaa.utils.SyscomConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class TemplateImpl extends ApplicationBase implements Template {

    private int mCurrentUserId;

    @Autowired
    private SyscomConfig safeSettings;

    @Autowired
    private Serial serialService;

    @Autowired
    private SyscomtemplateExtMapper syscomtemplateExtMapper;

    /**
     * Create one new SyscomTemplate record.
     * 建立一筆管理原則範本資料
     *
     * @param syscomtemplate
     * @return
     * @throws SafeaaException
     */
    @Override
    public int createTemplate(Syscomtemplate syscomtemplate) throws SafeaaException {
        if (StringUtils.isBlank(syscomtemplate.getTemplateno())){
            // 管理原則範本代碼不得空白
            addError(safeSettings.getCulture(), SAFEMessageId.EmptyTemplateNo);
            return -1;
        }
        try {
            int nextId = serialService.getNextId(CommonConstants.SerialName).intValue();
            syscomtemplate.setUpdateUser(mCurrentUserId);
            if (syscomtemplate.getTemplateid() == 0L){
                // 若未傳入管理原則範本序號則自取序號
                syscomtemplate.setTemplateid(nextId);
            }
            int mResult = syscomtemplateExtMapper.insertSelective(syscomtemplate);
            if (mResult < 0){
                addError(safeSettings.getCulture(), SAFEMessageId.InsertMasterFail);
            }
            return mResult;
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * Modify one SyscomTemplate record.
     * 修改一筆管理原則範本資料
     *
     * @param syscomtemplate
     * @return
     * @throws SafeaaException
     */
    @Override
    public int updateTemplate(Syscomtemplate syscomtemplate) throws SafeaaException {
        try{
            if (DbHelper.toBoolean(syscomtemplate.getTemplateid().shortValue())){
                syscomtemplate.setUpdateuserid(mCurrentUserId);
                syscomtemplate.setUpdatetime(new Date());
                int mResult = syscomtemplateExtMapper.updateByPrimaryKeySelective(syscomtemplate);
                if (mResult <= 0){
                    addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
                }
                return mResult;
            }
            else{
                if (!DbHelper.toBoolean(syscomtemplate.getTemplateid().shortValue())){
                    List<Syscomtemplate> list = syscomtemplateExtMapper.queryTemplateIdByNo(syscomtemplate.getTemplateno());
                    if (list.size() > 0){
                        syscomtemplate.setUpdateuserid(mCurrentUserId);
                        syscomtemplate.setUpdatetime(new Date());
                        int mResult = syscomtemplateExtMapper.updateByPrimaryKeySelective(syscomtemplate);
                        if (mResult <= 0){
                            addError(safeSettings.getCulture(), SAFEMessageId.UpdateNoRecord);
                        }
                        return mResult;
                    }
                    else{
                        // 無此管理原則範本資料
                        addError(safeSettings.getCulture(), SAFEMessageId.WithoutTemplateData);
                        return -1;
                    }
                }
                else{
                    // 管理原則範本代碼不得空白
                    addError(safeSettings.getCulture(), SAFEMessageId.EmptyTemplateNo);
                    return -1;
                }
            }
        }catch (Exception e){
            throw e;
        }
    }
}
