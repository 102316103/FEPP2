package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 收到ATMGW查詢Atmmstr請求電文
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPATMCommuAtmmstr extends BaseXmlCommu {
    /**
     * 查詢by IP
     */
    private String atmIp;
    /**
     * 查詢by ATM NO
     */
    private String atmNo;

    public String getAtmIp() {
        return atmIp;
    }

    public void setAtmIp(String atmIp) {
        this.atmIp = atmIp;
    }

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }
}
