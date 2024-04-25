<#--
修正記錄：
2023/05/14，使用FreeMarker摸板引擎，ATM
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.atm.${xlsxFile.cellMessageType};

import java.math.BigDecimal;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMTextBase;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.frmcommon.annotation.Field;

public class ${xlsxFile.className} extends ATMTextBase {
<#-- 處理屬性宣告 -->
<#list xlsxFile.xlsxList as xlsx>
	@Field(length = ${xlsx.lg})
	<#if "Y" == xlsx.type?upper_case>
	private BigDecimal ${xlsx.name};
	<#elseif "E" == xlsx.type?upper_case>
	<#-- 固定放分隔符號 -->
	private String ${xlsx.name} = ";#";
	<#elseif "S" == xlsx.type?upper_case>
	<#-- 固定放分隔符號 -->
	private String ${xlsx.name} = ";/";
	<#else>
	private String ${xlsx.name} = StringUtils.EMPTY;
	</#if>
	
</#list>
<#-- 電文總長度 -->
	private static final int _TotalLength = ${xlsxFile.totalSize};

<#-- 處理getter、setter -->
<#list xlsxFile.xlsxList as xlsx>
  <#if "Y" == xlsx.type?upper_case>
	public BigDecimal get${xlsx.getsetName}(){
		return ${xlsx.name};
	}
	public void set${xlsx.getsetName}(BigDecimal ${xlsx.name}){
		this.${xlsx.name} = ${xlsx.name};
	}
    <#else>
	public String get${xlsx.getsetName}(){
		return ${xlsx.name};
	}
	
	public void set${xlsx.getsetName}(String ${xlsx.name}){
		this.${xlsx.name} = ${xlsx.name};
	}
    </#if>
    
</#list>
<#-- 處理覆寫方法 -->
	@Override
	public int getTotalLength() {
		return _TotalLength;
	}

	@Override
	public ATMGeneral parseFlatfile(String flatfile) throws Exception {
		return null;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) {
		return null;
	}

	public String makeMessage() {
	<#list xlsxFile.xlsxList as xlsx>
	    <#if "Y" == xlsx.notAscii?upper_case>
	    this.set${xlsx.getsetName}(StringUtils.rightPad(this.get${xlsx.getsetName}(),${xlsx.lg}, "40"));
	    <#else>
	    <#if "Y" == xlsx.type?upper_case>
	    <#-- 左補空白 -->
	    this.set${xlsx.getsetName}(StringUtils.leftPad(this.get${xlsx.getsetName}(),${xlsx.lg},StringUtils.SPACE));
	    <#elseif "#" == xlsx.type?upper_case>
	    <#-- 該欄位內容將只取 # 前面的字串(含#) -->
		this.set${xlsx.getsetName}(this.get${xlsx.getsetName}().substring(0,this.get${xlsx.getsetName}().indexOf('#')+1));
	    <#elseif "L" == xlsx.type?upper_case>
	    <#-- 左補空白、內容如為空則固定寫 $0 -->
	    if(this.get${xlsx.getsetName}().equals(StringUtils.EMPTY)){
	    	this.set${xlsx.getsetName}(StringUtils.leftPad("$0",${xlsx.lg},StringUtils.SPACE));
	    }else{
			this.set${xlsx.getsetName}(StringUtils.leftPad(this.get${xlsx.getsetName}(),${xlsx.lg},StringUtils.SPACE));
	    }    
	    <#elseif "T" == xlsx.type?upper_case>    
		this.set${xlsx.getsetName}(StringUtils.stripEnd(this.getACTDATA(), StringUtils.SPACE));
	    <#else>
		this.set${xlsx.getsetName}(StringUtils.rightPad(this.get${xlsx.getsetName}(),${xlsx.lg},StringUtils.SPACE));
	    </#if>
	    </#if>
	</#list>

		return <#list xlsxFile.xlsxList as xlsx><#if "Y" == xlsx.notAscii?upper_case>this.get${xlsx.getsetName}()<#if xlsx_has_next> +</#if>
   			   <#else>CodeGenUtil.asciiToEbcdicDefaultEmpty(this.get${xlsx.getsetName}(),${xlsx.lg})<#if xlsx_has_next> +</#if></#if>
		      </#list>;
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
	}
}
