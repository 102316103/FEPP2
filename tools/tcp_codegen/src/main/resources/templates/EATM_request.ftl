<#--
修正記錄：
2023/04/19，parseMessage()中，新增不轉ASCII判斷，需要與ATM同步處理
	getNotAscii()=="Y"，不轉碼直接賦值
	getNotAscii()=="N"，使用StringUtil.fromHex()
2023/06/11，使用FreeMarker摸板引擎，EATM
2023/06/22，EATM新增數字處理，主要調用WebatmXmlBase方法處理
	- 處理格式：
		1. S9(11)V99，表示有+-符號、小數前長度為11、無小數點、小數後長度為2
		2. S9(14).99，表示有+-符號、小數前長度為14、有小數點、小數後長度為2
	- 處理分工：
		1. parseMessage()，僅處理EBCDIC轉ASCII邏輯，xml物件存String
		2. getter，處理ASCII轉BigDecimal
		3. setter，處理BigDecimal轉ASCII
2023/06/22，新增轉換工具類CodeGenUtil，便於日後維護
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.webatm;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.CodeGenUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("SOAP-ENV:Envelope")
public class ${xlsxFile.className} extends WebatmXmlBase {
    <#-- 處理屬性宣告 -->
    @XStreamAsAttribute
    @XStreamAlias("xmlns:SOAP-ENV")
    private String xmlnsSoapEnv;
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("SOAP-ENV:Header")
    private ${xlsxFile.className}_Header header;
    @XStreamAlias("SOAP-ENV:Body")
    private ${xlsxFile.className}_Body body;

    <#-- 處理getter、setter -->
    public String getXmlnsSoapEnv() {
        return xmlnsSoapEnv;
    }
    public void setXmlnsSoapEnv(String xmlnsSoapEnv) {
        this.xmlnsSoapEnv = xmlnsSoapEnv;
    }
    public String getXmlnsXsi() {
        return xmlnsXsi;
    }
    public void setXmlnsXsi(String xmlnsXsi) {
        this.xmlnsXsi = xmlnsXsi;
    }
    public ${xlsxFile.className}_Header getHeader() {
        return header;
    }
    public void setHeader(${xlsxFile.className}_Header header) {
        this.header = header;
    }
    public ${xlsxFile.className}_Body getBody() {
        return body;
    }
    public void setBody(${xlsxFile.className}_Body body) {
        this.body = body;
    }
    <#-- 處理SOAP-ENV:Header，子類 -->
    @XStreamAlias("SOAP-ENV:Header")
    public static class ${xlsxFile.className}_Header {
    }
    <#-- 處理SOAP-ENV:Body，子類 -->
    @XStreamAlias("SOAP-ENV:Body")
    public static class ${xlsxFile.className}_Body {
        @XStreamAlias("esb:MsgRq")
        private ${xlsxFile.className}_Body_EsbMsgRq rq;
        public ${xlsxFile.className}_Body_EsbMsgRq getRq() {
            return rq;
        }
        public void setRq(${xlsxFile.className}_Body_EsbMsgRq rq) {
            this.rq = rq;
        }
    }
    <#-- 處理esb:MsgRq，子類 -->
    @XStreamAlias("esb:MsgRq")
    public static class ${xlsxFile.className}_Body_EsbMsgRq {
        @XStreamAlias("Header")
        private ${xlsxFile.className}_Body_MsgRq_Header header;
        @XStreamAlias("SvcRq")
        private ${xlsxFile.className}_Body_MsgRq_SvcRq svcRq;
        public ${xlsxFile.className}_Body_MsgRq_Header getHeader() {
            return header;
        }
        public void setHeader(${xlsxFile.className}_Body_MsgRq_Header header) {
            this.header = header;
        }
        public ${xlsxFile.className}_Body_MsgRq_SvcRq getSvcRq() {
            return svcRq;
        }
        public void setSvcRq(${xlsxFile.className}_Body_MsgRq_SvcRq svcRq) {
            this.svcRq = svcRq;
        }
    }

    <#-- 處理HEADER，子類 -->
    @XStreamAlias("Header")
    public static class ${xlsxFile.className}_Body_MsgRq_Header {
        <#-- 定制屬性 -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if "HEADER" == xlsx.headerBody?upper_case>
        private String ${xlsx.name};
        </#if>
        </#list>

        <#-- 定制屬性，getter、setter -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if "HEADER" == xlsx.headerBody?upper_case>
        public String get${xlsx.getsetName}(){
            return ${xlsx.name};
        }
        public void set${xlsx.getsetName}(String value) {
            this.${xlsx.name} = value;
        }
        </#if>
        </#list>
    }

    <#-- 處理SvcRq，子類 -->
    @XStreamAlias("SvcRq")
    public static class ${xlsxFile.className}_Body_MsgRq_SvcRq {
        <#-- 定制屬性 -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if "BODY" == xlsx.headerBody?upper_case>
        <#if "E" == xlsx.type?upper_case>
        <#-- 固定放結束符號 -->
        private String ${xlsx.name} = ";#";
        <#elseif "S" == xlsx.type?upper_case>
        <#-- 固定放分隔符號 -->
        private String ${xlsx.name} =  ";/";
        <#else>
        private String ${xlsx.name} = StringUtils.EMPTY;
        </#if>
        </#if>
        </#list>

        <#-- 定制屬性，getter、setter -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if "BODY" == xlsx.headerBody?upper_case>
        <#if "Y" == xlsx.type?upper_case>
        <#-- 數值型態 -->
        public BigDecimal get${xlsx.getsetName}(){
            return CodeGenUtil.asciiToBigDecimal(this.${xlsx.name}, ${xlsx.hasPoint}, ${xlsx.floatLen});
        }
        public void set${xlsx.getsetName}(BigDecimal value) {
            this.${xlsx.name} = CodeGenUtil.bigDecimalToAscii(value, ${xlsx.numberLen}, ${xlsx.hasPoint}, ${xlsx.floatLen}, ${xlsx.hasSign});
        }
        <#else>
        <#-- 字串型態 -->
        public String get${xlsx.getsetName}(){
            return this.${xlsx.name};
        }
        public void set${xlsx.getsetName}(String value) {
            this.${xlsx.name} = value;
        }
        </#if>
        </#if>
        </#list>
    }

    <#-- 處理覆寫方法，request -->
    @Override
    public void parseMessage(String data) throws Exception {
    	RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq = this.getBody().getRq().getSvcRq();
        <#list xlsxFile.xlsxList as xlsx>
        <#-- 屬性內容=BODY 或 屬性為字串 不處理 -->
        <#-- Type='N'這筆的內容是4731完整電文，不做解析，是解析的來源資料 -->
        <#if "BODY" == xlsx.headerBody?upper_case && "N" != xlsx.type?upper_case>
        <#if "Y" == xlsx.notAscii?upper_case>
        <#-- 不需轉ASCII -->
        svcRq.${xlsx.name} = data.substring(${(xlsx.start*2)?string('#')}, ${(xlsx.end*2)?string('#')});
        <#-- 與 ATM CodeGen 同步 -->
        <#elseif "N" == xlsx.notAscii?upper_case>
        svcRq.${xlsx.name} = StringUtil.fromHex(data.substring(${(xlsx.start*2)?string('#')}, ${(xlsx.end*2)?string('#')}));
        <#else>
        svcRq.${xlsx.name} = CodeGenUtil.ebcdicToAsciiDefaultEmpty(data.substring(${(xlsx.start*2)?string('#')}, ${(xlsx.end*2)?string('#')}));
        </#if>
        </#if>
        </#list>
    }
}
