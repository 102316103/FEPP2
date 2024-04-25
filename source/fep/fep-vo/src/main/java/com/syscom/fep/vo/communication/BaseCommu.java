package com.syscom.fep.vo.communication;

import com.syscom.fep.base.enums.FEPReturnCode;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.io.Serializable;

public abstract class BaseCommu implements Serializable {
    /**
     * 用於區分物件, 在字串轉物件的時候會用到
     */
    private String classname = this.getClass().getName();
    /**
     * 用於設定Response時的結果, 預設是true
     */
    private FEPReturnCode code = FEPReturnCode.Normal;
    /**
     * 錯誤訊息
     */
    private String errmsg;
    /**
     * 壓縮後的Hex字串
     */
    private String compressed;
    /**
     * 是否需要壓縮
     */
    @XStreamOmitField
    private transient boolean compress;

    public String getClassname() {
        return classname;
    }

    public FEPReturnCode getCode() {
        return code;
    }

    public void setCode(FEPReturnCode code) {
        this.code = code;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getCompressed() {
        return compressed;
    }

    public void setCompressed(String compressed) {
        this.compressed = compressed;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }
}