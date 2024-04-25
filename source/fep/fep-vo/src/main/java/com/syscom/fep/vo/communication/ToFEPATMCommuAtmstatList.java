package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

/**
 * 收到ATMGW查詢Atmstat List請求電文
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPATMCommuAtmstatList extends BaseXmlCommu {
    @XStreamAlias("atmstatAtmnos")
    private List<String> atmstatAtmnoList;
    /**
     * 查詢by status
     */
    private int atmstatStatus = -1;

    public List<String> getAtmstatAtmnoList() {
        return atmstatAtmnoList;
    }

    public void setAtmstatAtmnoList(List<String> atmstatAtmnoList) {
        this.atmstatAtmnoList = atmstatAtmnoList;
    }

    public int getAtmstatStatus() {
        return atmstatStatus;
    }

    public void setAtmstatStatus(int atmstatStatus) {
        this.atmstatStatus = atmstatStatus;
    }

    /**
     * 預設電文按照壓縮處理
     *
     * @return
     */
    @Override
    public final boolean isCompress() {
        return true;
    }

    /**
     * 預設電文按照壓縮處理
     *
     * @param compress
     */
    @Override
    public final void setCompress(boolean compress) {
        super.setCompress(true);
    }
}
