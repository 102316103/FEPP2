<#--
修正記錄：
2023/05/20，使用FreeMarker摸板引擎，IMS
2023/06/01，新增makeMessage()方法
2023/06/12，makeMessage()中如果字串為null，默認值為StringUtils.EMPTY
2023/06/12，新增數字處理，主要調用IMSTextBase方法處理
	S9(11)V99，表示有+_符號、小數前長度為11、小數後長度為2
2023/06/12，IMS若"不需轉ASCII"為"Y"，makeMessage()中就不轉為EBCDIC
2023/06/22，新增轉換工具類CodeGenUtil，便於日後維護
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class ${xlsxFile.className} extends IMSTextBase {
<#-- 處理屬性宣告 -->
<#list xlsxFile.xlsxList as xlsx>
    @Field(length = ${xlsx.lg})
    <#if "Y" == xlsx.type?upper_case>
	private BigDecimal ${xlsx.name};
    <#else>
	private String ${xlsx.name} = StringUtils.EMPTY;
    </#if>

</#list>
<#-- 處理getter、setter -->
<#list xlsxFile.xlsxList as xlsx>
    <#if "Y" == xlsx.type?upper_case>
	public BigDecimal get${xlsx.getsetName}(){
        return this.${xlsx.name};
	}
	
	public void set${xlsx.getsetName}(BigDecimal ${xlsx.name}){
        this.${xlsx.name} = ${xlsx.name};
	}
	
    <#else>
	public String get${xlsx.getsetName}(){
        return this.${xlsx.name};
	}
	
	public void set${xlsx.getsetName}(String ${xlsx.name}){
        this.${xlsx.name} = ${xlsx.name};
	}
	
    </#if>
</#list>
<#-- 轉物件 -->
	public void parseCbsTele(String tita) throws ParseException{
<#list xlsxFile.xlsxList as xlsx>
	<#if "Y" == xlsx.notAscii?upper_case>
    <#-- 直接賦值，不轉換 -->
		this.set${xlsx.getsetName}(tita.substring(${(xlsx.start*2)?c}, ${(xlsx.end*2)?c}));
    <#else>
    <#if "Y" == xlsx.type?upper_case>
        this.set${xlsx.getsetName}(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(${(xlsx.start*2)?c}, ${(xlsx.end*2)?c}).trim()), ${xlsx.hasPoint}, ${xlsx.floatLen}));
    <#else>
        this.set${xlsx.getsetName}(EbcdicConverter.fromHex(CCSID.English,tita.substring(${(xlsx.start*2)?c}, ${(xlsx.end*2)?c})));
    </#if>
    </#if>
</#list>
	}

<#-- 轉EBCDIC -->
	public String makeMessage() {
		return "" 
<#list xlsxFile.xlsxList as xlsx>
    <#if "Y" == xlsx.notAscii?upper_case>
    <#-- 直接賦值，不轉換 -->
        	+ this.get${xlsx.getsetName}()  
    <#else>
        <#if "Y" == xlsx.type?upper_case>
        <#-- 數字 -->
            + CodeGenUtil.bigDecimalToEbcdic(this.get${xlsx.getsetName}(), ${xlsx.numberLen}, ${xlsx.hasPoint}, ${xlsx.floatLen}, ${xlsx.hasSign}) 
        <#else>
        <#-- 字串 -->
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.get${xlsx.getsetName}(), ${xlsx.lg}) 
        </#if>
    </#if>
</#list>
		;
	}

<#-- TEST -->
	public String makeMessageAscii() {
		return "" 
<#list xlsxFile.xlsxList as xlsx>
    <#if "Y" == xlsx.notAscii?upper_case>
    <#-- 直接賦值，不轉換 -->
        	+ this.get${xlsx.getsetName}()  
    <#else>
        <#if "Y" == xlsx.type?upper_case>
        <#-- 數字 -->
            + CodeGenUtil.bigDecimalToAsciiCBS(this.get${xlsx.getsetName}(), ${xlsx.numberLen}, ${xlsx.hasPoint}, ${xlsx.floatLen}, ${xlsx.hasSign})
        <#else>
        <#-- 字串 -->
			+ StringUtils.rightPad(StringUtils.defaultIfEmpty(this.get${xlsx.getsetName}(), StringUtils.EMPTY), ${xlsx.lg}," ")
        </#if>
    </#if>
</#list>
		;
	}
}
