<#--
修正記錄：
2023/06/11，外圍電文使用共用模版，HCE、NB、VA、VO，此模版暫不使用
2023/06/22，HCE、NB、VA、VO新增數字處理，主要調用WebatmXmlBase方法處理
	- 處理格式：
		1. S9(11)V99，表示有+-符號、小數前長度為11、無小數點、小數後長度為2
		2. S9(14).99，表示有+-符號、小數前長度為14、有小數點、小數後長度為2
	- 處理分工：
		1. getter，處理ASCII轉BigDecimal
		2. setter，處理BigDecimal轉ASCII
-->
<#-- 處理包路徑、引入、宣告 -->
package ${package};

import java.math.BigDecimal;

import com.syscom.fep.vo.CodeGenUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("SOAP-ENV:Envelope")
<#if extends?? && extends != ""><#-- 不為空且不為空白字串 -->
public class ${xlsxFile.className} extends ${extends} {
<#else>
public class ${xlsxFile.className} {
</#if>
    <#-- 處理屬性宣告 -->
    @XStreamAsAttribute
    @XStreamAlias("xmlns:SOAP-ENV")
    private String xmlnsSoapEnv;
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    <#-- 處理getter、setter -->
    @XStreamAlias("SOAP-ENV:Header")
    private ${xlsxFile.className}_Header header;
    @XStreamAlias("SOAP-ENV:Body")
    private ${xlsxFile.className}_Body body;

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
    @XStreamAlias("SOAP-ENV:Body")
    public static class ${xlsxFile.className}_Body {
        @XStreamAlias("esb:MsgRq")
        private ${xlsxFile.className}_Body_MsgRq rq;
        public ${xlsxFile.className}_Body_MsgRq getRq() {
            return rq;
        }
        public void setRq(${xlsxFile.className}_Body_MsgRq rq) {
            this.rq = rq;
        }
    }
    <#-- 處理esb:MsgRq，子類 -->
    @XStreamAlias("esb:MsgRq")
    public static class ${xlsxFile.className}_Body_MsgRq {
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

    <#list headerTypeList as headerType>
    <#-- 處理電文說明，子類 -->
    @XStreamAlias("${headerType.xmlStr}")
    public static class ${xlsxFile.className}_Body_MsgRq_${headerType.xmlStr} {
        <#-- 子類電文說明 -->
        <#list headerType.subHeaderList as subHeader>
        @XStreamAlias("${subHeader.xmlStr}")
        private ${xlsxFile.className}_Body_MsgRq_${subHeader.compareStr} ${subHeader.compareStr};
        public ${xlsxFile.className}_Body_MsgRq_${subHeader.compareStr} get${subHeader.compareStr}() {
            return ${subHeader.compareStr};
        }
        public void set${subHeader.compareStr}(${xlsxFile.className}_Body_MsgRq_${subHeader.compareStr} ${subHeader.compareStr}) {
            this.${subHeader.compareStr} = ${subHeader.compareStr};
        }

        </#list>
        <#-- 定制屬性 -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if headerType.compareStr?upper_case == xlsx.headerBody?upper_case>
        private String ${xlsx.name};
        </#if>
        </#list>

        <#-- 定制屬性，getter、setter -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if headerType.compareStr?upper_case == xlsx.headerBody?upper_case>
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

    </#list>
}
