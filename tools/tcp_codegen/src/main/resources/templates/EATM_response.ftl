<#--
修正記錄：
2023/06/11，使用FreeMarker摸板引擎，EATM
2023/06/11，BODY除OUTDATA外參數新增以下Annotation：
	1. @XStreamOmitField
	2. @Field(length = 欄位長度)
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.webatm;
import com.syscom.fep.vo.CodeGenUtil;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.annotation.Field;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("soap:Envelope")
public class ${xlsxFile.className} extends WebatmXmlBase {
<#-- 處理屬性宣告 -->
@XStreamAsAttribute
@XStreamAlias("xmlns:soap")
private String xmlnsSoap = "http://schemas.xmlsoap.org/soap/envelope/";
@XStreamAsAttribute
@XStreamAlias("xmlns:xsi")
private String xmlnsXsi;
@XStreamAlias("soap:Header")
private String header = StringUtils.EMPTY;
@XStreamAlias("soap:Body")
private ${xlsxFile.className}_Body body;

<#-- 處理getter、setter -->
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
private ${xlsxFile.className}_Body_NS1MsgRs rs;
public ${xlsxFile.className}_Body_NS1MsgRs getRs() {
return rs;
}
public void setRs(${xlsxFile.className}_Body_NS1MsgRs rs) {
this.rs = rs;
}
}
<#-- 處理NS1:MsgRs，子類 -->
@XStreamAlias("NS1:MsgRs")
public static class ${xlsxFile.className}_Body_NS1MsgRs {
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
public void setSvcRq(${xlsxFile.className}_Body_MsgRs_SvcRs svcRs) {
this.svcRs = svcRs;
}
}

<#-- 處理Header，子類 -->
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
<#-- 定制屬性 -->
<#list xlsxFile.xlsxList as xlsx>
    <#if "BODY" == xlsx.headerBody?upper_case>
        <#if "OUTDATA" != xlsx.name?upper_case>
            @XStreamOmitField
            @Field(length = ${xlsx.lg})
        </#if>
        <#if "E" == xlsx.type?upper_case>
        <#-- 指派【結束符號 = ;#】 for ATM,EATM下行電文 -->
            private String ${xlsx.name} = ";#";
        <#elseif "S" == xlsx.type?upper_case>
        <#-- 指派【分隔符號 = ;/】 for ATM,EATM下行電文 -->
            private String ${xlsx.name} =  ";/";
        <#else>
            private String ${xlsx.name} = StringUtils.EMPTY;
        </#if>
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
<#-- 轉EBCDIC -->
public String makeMessage() {
return "" //
<#list xlsxFile.xlsxList as xlsx>
    <#if "BODY" == xlsx.headerBody?upper_case>
        <#if "OUTDATA" != xlsx.name?upper_case>
            <#if "Y" == xlsx.notAscii?upper_case>
            <#-- 直接賦值，不轉換 -->
                + (StringUtils.isNotBlank(this.get${xlsx.getsetName}()) ? this.get${xlsx.getsetName}() : CodeGenUtil.asciiToEbcdicDefaultEmpty(this.get${xlsx.getsetName}(), ${xlsx.lg}))  //
            <#else>
                <#if "Y" == xlsx.type?upper_case>
                <#-- 數字 -->
                    + CodeGenUtil.bigDecimalToEbcdic(this.get${xlsx.getsetName}(), ${xlsx.numberLen}, ${xlsx.hasPoint}, ${xlsx.floatLen}, ${xlsx.hasSign}) //
                <#else>
                <#-- 字串 -->
                    + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.get${xlsx.getsetName}(), ${xlsx.lg}) //
                </#if>
            </#if>
        </#if>
    </#if>
</#list>
;
}
}

<#-- 處理覆寫方法 -->
@Override
public void parseMessage(String data) throws Exception {
}


}
