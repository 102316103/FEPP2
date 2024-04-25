<#--
修正記錄：
2023/06/10，使用FreeMarker摸板引擎，VA
2023/06/11，外圍電文使用共用模版，HCE、NB、VA、VO，此模版暫不使用
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.nb;

import org.apache.commons.lang3.StringUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("soap:Envelope")
public class ${xlsxFile.className} {
    <#-- 處理屬性宣告 -->
    @XStreamAsAttribute
    @XStreamAlias("xmlns:soap")
    private String xmlnsSoap = "http://schemas.xmlsoap.org/soap/envelope/";
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    <#-- 處理getter、setter -->
    @XStreamAlias("soap:Header")
    private String header = StringUtils.EMPTY;
    @XStreamAlias("soap:Body")
    private ${xlsxFile.className}_Body body;

    public ${xlsxFile.className}_Body getBody() {
        return body;
    }
    public void setBody(${xlsxFile.className}_Body body) {
        this.body = body;
    }
    <#-- 處理soap:Body，子類 -->
    @XStreamAlias("soap:Body")
    public static class ${xlsxFile.className}_Body {
        @XStreamAlias("NS1:MsgRs")
        private ${xlsxFile.className}_Body_MsgRs rs;
        public ${xlsxFile.className}_Body_MsgRs getRs() {
            return rs;
        }
        public void setRs(${xlsxFile.className}_Body_MsgRs rs) {
            this.rs = rs;
        }
    }
    <#-- 處理esb:MsgRs，子類 -->
    @XStreamAlias("NS1:MsgRs")
    public static class ${xlsxFile.className}_Body_MsgRs {
        @XStreamAsAttribute
        @XStreamAlias("xmlns:NS1")
        private String xmlnsNs1="http://www.ibm.com.tw/esb";
        @XStreamAlias("Header")
        private ${xlsxFile.className}_Body_MsgRs_Header header;
        @XStreamAlias("SvcRs")
        private ${xlsxFile.className}_Body_MsgRs_SvcRs svcRs;
        public ${xlsxFile.className}_Body_MsgRs_Header getHeader() {
            return header;
        }
        public void setHeader(${xlsxFile.className}_Body_MsgRs_Header header) {
            this.header = header;
        }
        public ${xlsxFile.className}_Body_MsgRs_SvcRs getSvcRs() {
            return svcRs;
        }
        public void setSvcRs(${xlsxFile.className}_Body_MsgRs_SvcRs svcRs) {
            this.svcRs = svcRs;
        }
    }

    <#-- 處理HEADER，子類 -->
    @XStreamAlias("Header")
    public static class ${xlsxFile.className}_Body_MsgRs_Header {
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

    <#-- 處理SvcRs，子類 -->
    @XStreamAlias("SvcRs")
    public static class ${xlsxFile.className}_Body_MsgRs_SvcRs {
        @XStreamAlias("REPLYDATA")
        private ${xlsxFile.className}_Body_MsgRs_SvcRs_ReplyData replydata;
        public ${xlsxFile.className}_Body_MsgRs_SvcRs_ReplyData getReplyData() {
            return replydata;
        }
        public void setReplyData(${xlsxFile.className}_Body_MsgRs_SvcRs_ReplyData replydata) {
            this.replydata = replydata;
        }
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

    @XStreamAlias("REPLYDATA")
    public static class ${xlsxFile.className}_Body_MsgRs_SvcRs_ReplyData {
        <#-- 定制屬性 -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if "REPLYDATA" == xlsx.headerBody?upper_case>
        private String ${xlsx.name};
        </#if>
        </#list>

        <#-- 定制屬性，getter、setter -->
        <#list xlsxFile.xlsxList as xlsx>
        <#if "REPLYDATA" == xlsx.headerBody?upper_case>
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
