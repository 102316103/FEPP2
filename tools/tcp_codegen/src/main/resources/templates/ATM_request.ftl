<#--
修正記錄：
2023/04/19，不轉ASCII為Y時，不轉碼直接賦值
2023/05/14，使用FreeMarker摸板引擎，ATM
2023/07/17，修正，欄位設定為數字的處理
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.atm.${xlsxFile.cellMessageType};

import java.math.BigDecimal;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
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
       return this.parseFlatfile(this.getClass(), flatfile);
    }

    @Override
    public String makeMessageFromGeneral(ATMGeneral general) {
       return null;
    }

    public String makeMessage() {
       return null;
    }

    @Override
    public void toGeneral(ATMGeneral general) throws Exception {
        ATMGeneralRequest request = general.getRequest();
        <#list xlsxFile.xlsxList as xlsx>
        <#if "Y" == xlsx.notAscii?upper_case>
        <#-- 直接賦值，不轉換 -->
        request.set${xlsx.getsetName}(this.get${xlsx.getsetName}());
        <#elseif "N" == xlsx.notAscii?upper_case>
        <#-- 使用StringUtil.fromHex()轉換 -->
        request.set${xlsx.getsetName}(StringUtil.fromHex(this.get${xlsx.getsetName}()));
        <#else>
        <#if "Y" == xlsx.type?upper_case>
        <#-- 數字 -->
        request.set${xlsx.getsetName}(this.get${xlsx.getsetName}());
        <#else>
        <#-- 字串 -->
        <#-- 使用EbcdicConverter.fromHex()轉換 -->
        request.set${xlsx.getsetName}(EbcdicConverter.fromHex(CCSID.English,this.get${xlsx.getsetName}()));
        </#if>
        </#if>
        </#list>
    }
}
