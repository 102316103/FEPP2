<#--
修正記錄：
2023/06/10，使用FreeMarker摸板引擎HCE
2023/06/11，外圍電文使用共用模版，HCE、NB、VA、VO，此模版暫不使用
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.hce;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("SOAP-ENV:Envelope")
public class ${xlsxFile.className} {
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
        private String ${xlsx.name};
        </#if>
        </#list>

        <#-- 定制屬性，getter、setter -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if "BODY" == xlsx.headerBody?upper_case>
        public String get${xlsx.getsetName}(){
            return ${xlsx.name};
        }
        public void set${xlsx.getsetName}(String value) {
            this.${xlsx.name} = value;
        }
        </#if>
        </#list>
    }
}
