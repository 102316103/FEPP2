package com.syscom.safeaa.base;


import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.enums.EMSData;
import com.syscom.safeaa.enums.ErrorPriority;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.log.LogHelper;
import com.syscom.safeaa.log.LogHelperFactory;
import com.syscom.safeaa.mybatis.extmapper.SyscombasemessageExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscombasemessagecultureExtMapper;
import com.syscom.safeaa.mybatis.model.Syscombasemessage;

import java.util.ArrayList;
import java.util.List;
import com.syscom.safeaa.mybatis.model.Syscombasemessageculture;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.model.Syscomuserstatus;
import com.syscom.safeaa.utils.DbHelper;
import com.syscom.safeaa.utils.SafeaaSpringBeanFactoryUtil;
//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
//import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/** 
* Basic class of SAFE. All application level components must inherits this class.<br/>
* @author David Tai
* @author chenyang
* @version: 1.0.0.0.
* Declaration: Copyright 2008 SYSCOM Computer Engineering Corporation. All rights reserved.<br/>
* Modify History<br/>
* 1. Modifier: David Tai.<br/>
*    Date    : 2013-10-15.<br/>
*    Reason  : From SAFEBase change to APBase<br/>
* 
* <li>SAFE的基礎類別。所有應用層的SAFE元件必須繼承此類別
*  
*/
public abstract class ApplicationBase{
    private String DataAccessPolicyName = "Data Access";
    private boolean mAutoDispose = true;
//    Private mErrorMessages As New System.Collections.ObjectModel.Collection(Of ApplicationMessage)
    List<ApplicationMessage> mErrorMessages = new ArrayList<>();
    protected static final LogHelper _log = LogHelperFactory.getEMSLogger();

    protected boolean mExplicitTx = false; //
    
    protected Integer currentUserId;

    protected Syscomuser currentUser;

    protected Syscomuserstatus mDefUserStatus;
    
    @Autowired
    SyscombasemessageExtMapper syscombasemessageMapper;
    
    @Autowired
    SyscombasemessagecultureExtMapper syscombasemessagecultureExtMapper;

	public Integer getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(Integer currentUserId) {
		this.currentUserId = currentUserId;
	}

    public Syscomuser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Syscomuser currentUser) {
        this.currentUser = currentUser;
    }

	public Syscomuserstatus getmDefUserStatus() {
		return mDefUserStatus;
	}

	public void setmDefUserStatus(Syscomuserstatus mDefUserStatus) {
		this.mDefUserStatus = mDefUserStatus;
	}

	public ApplicationBase(){}

    public static class ApplicationMessage{
        private String mMessageId = ""; // 應用系統訊息代號
        private ErrorPriority mErrorLevel = ErrorPriority.fromValue(0); //錯誤等級
        private boolean mMonitor; // 是否送事件監控
        private boolean mIsProtected; //是否保護訊息
        private String mMessageText = ""; // 訊息內容

        
        /**
         * MessageId of baseMessage.
         * <li>應用系統訊息代號
         * @return String  messageId
         */
        public String getmMessageId() {
            return mMessageId;
        }

        public void setmMessageId(String mMessageId) {
            this.mMessageId = mMessageId;
        }

        /**
         *  Level of error.
         *  
         *   0=Info=不影響交易結果且可忽略的錯誤或業務訊息。
         *   1=Warning=不影響交易結果但可經重新操作或可自動排除的錯誤或業務訊息。  
         *   2=Errors=需人為介入排除的嚴重錯誤或業務訊息。
         *   3=Fatal=Fatal Error or Exception
         * @return ErrorPriority errorLevel
         */
        public ErrorPriority getmErrorLevel() {
            return mErrorLevel;
        }

        public void setmErrorLevel(ErrorPriority mErrorLevel) {
            this.mErrorLevel = mErrorLevel;
        }


        /**
         * Mark of send EMS monitor system.
         * 
         * <li>是否送事件監控處理
         * @return boolean, true-sendEMS, false-not send
         */
        public boolean getmMonitor() {
            return mMonitor;
        }

        public void setmMonitor(boolean mMonitor) {
            this.mMonitor = mMonitor;
        }


        /**
         * 
         *  <li>
         * @return boolean
         */
        public boolean ismIsProtected() {
            return mIsProtected;
        }

        public void setmIsProtected(boolean mIsProtected) {
            this.mIsProtected = mIsProtected;
        }


        /**
         * Content of message.
         * True=Protected message, False=Not protected message.
         * <li>訊息內容 
         * @return message
         */ 
        public String getmMessageText() {
            return mMessageText;
        }

        public void setmMessageText(String mMessageText) {
            this.mMessageText = mMessageText;
        }
    }

    /**
     * The collection of all ApplicationMessage.
     * 
     * <li>錯誤訊息代碼集合
     * @return
     */
    public List<ApplicationMessage> getmErrorMessages() {
        return mErrorMessages;
    }

    public void setmErrorMessages(List<ApplicationMessage> mErrorMessages) {
        this.mErrorMessages = mErrorMessages;
    }


    /**
     * Get all error messageId.
     *
     * <li>取得ErrorMessagesId集合
     * @return List<String> ErrorMessagesId
     */
    public List<String> getAllErrorMessageId(){
        List<String> msgIds = new ArrayList<String>();
        if (this.mErrorMessages.size() > 0){
            for(int i = 0;i <= this.mErrorMessages.size();i++){
                msgIds.add(i,this.mErrorMessages.get(i).getmMessageId());
            }
        }
        return msgIds;
    }

    /**
     * Add error and description to collection ErrorMessages.
     *
     * <li>將錯誤加入ErrorMessages集合
     * @param culture  culture code
     * @param messageId  SAFE defined error message Id
     */
    protected void addError(String culture, SAFEMessageId messageId) throws SafeaaException{

    	ApplicationMessage safeMessage = new ApplicationMessage();
        String errorMessageId = StringUtils.leftPad(Integer.toString(messageId.getValue()),4,"0");
        
        try{
            Syscombasemessage syscombasemessage = new Syscombasemessage();
            Syscombasemessageculture syscombasemessageculture = new Syscombasemessageculture();

            syscombasemessage = syscombasemessageMapper.selectByPrimaryKey(errorMessageId);
            syscombasemessageculture = syscombasemessagecultureExtMapper.selectByMessageIdAndCulture(errorMessageId,culture);

            if(syscombasemessage==null || syscombasemessageculture==null){
                safeMessage.setmMessageId(errorMessageId);
                safeMessage.setmMonitor(true);
                safeMessage.setmMessageText(SAFEMessageId.NoSuchMessageId.name());
            }else{
                safeMessage.setmMessageId(errorMessageId);
                safeMessage.setmErrorLevel(ErrorPriority.valueOf(syscombasemessage.getErrorlevel()));
                safeMessage.setmMonitor(DbHelper.toBoolean(syscombasemessage.getMonitor().toString()));
                safeMessage.setmIsProtected(DbHelper.toBoolean(syscombasemessage.getIsprotected().toString()));
                safeMessage.setmMessageText(syscombasemessageculture.getMessagetext());
            }

            throw new SafeaaException(safeMessage.getmMessageText(), messageId,null );

        }catch (Exception ex){
            throw ex;
        }
    }

    protected void addErrors(List<ApplicationMessage> errors){
        this.mErrorMessages = errors;
    }

    private void sendEMS(EMSData errorData) {
        if(errorData.getProgramException() != null){
            _log.warn(errorData.getLevel().toString(),errorData.getErrorCode(),errorData.getProgramException().toString(),errorData.getApMessage());
        }
        else{
            _log.warn(errorData.getLevel().toString(),errorData.getErrorCode(),errorData.getErrDescription(),errorData.getApMessage());
        }
    }

    public boolean ismExplicitTx() {
		return mExplicitTx;
	}

	public void setmExplicitTx(boolean mExplicitTx) {
		this.mExplicitTx = mExplicitTx;
	}    

	
}
