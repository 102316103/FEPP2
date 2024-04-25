package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Date;
import java.util.List;

/**
 * 回應ATMGW的查詢Atmstat List
 *
 * @author Richard
 */
@XStreamAlias("response")
public class ToATMCommuAtmstatList extends BaseXmlCommu {
    @XStreamAlias("atmstats")
    private List<ToATMCommuAtmstat> atmstatList;

    public List<ToATMCommuAtmstat> getAtmstatList() {
        return atmstatList;
    }

    public void setAtmstatList(List<ToATMCommuAtmstat> atmstatList) {
        this.atmstatList = atmstatList;
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

    @XStreamAlias("atmstat")
    public static class ToATMCommuAtmstat {
        private String atmstatAtmno;
        private int atmstatStatus;
        //20230504 Bruce 增加顯示最後連線及斷線時間
        private String atmstatLastClose;
        private String atmstatLastOpen;
        //20230919 Bruce 增加顯示gateway ip
        private String atmmstrAtmpIp;

        public String getAtmstatAtmno() {
            return atmstatAtmno;
        }

        public void setAtmstatAtmno(String atmstatAtmno) {
            this.atmstatAtmno = atmstatAtmno;
        }

        public int getAtmstatStatus() {
            return atmstatStatus;
        }

        public void setAtmstatStatus(int atmstatStatus) {
            this.atmstatStatus = atmstatStatus;
        }

		public String getAtmstatLastClose() {
			return atmstatLastClose;
		}

		public void setAtmstatLastClose(String atmstatLastClose) {
			this.atmstatLastClose = atmstatLastClose;
		}

		public String getAtmstatLastOpen() {
			return atmstatLastOpen;
		}

		public void setAtmstatLastOpen(String atmstatLastOpen) {
			this.atmstatLastOpen = atmstatLastOpen;
		}

		public String getAtmmstrAtmpIp() {
			return atmmstrAtmpIp;
		}

		public void setAtmmstrAtmpIp(String atmmstrAtmpIp) {
			this.atmmstrAtmpIp = atmmstrAtmpIp;
		}
    }
}
