<#--
修正記錄：
2023/05/14，使用FreeMarker摸板引擎，ATM big class
2023/06/19，新增産出MFT電文
-->
<#-- 處理包路徑、引入、宣告 -->
package ${package};

import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import com.syscom.fep.frmcommon.annotation.Field;

public class ${className}{
    <#-- 處理屬性宣告 -->
    <#list xlsxMap?keys as key>
    <#if "N" == xlsxMap[key].notAscii?upper_case>
    @Field(encryption = "N")
    <#else>
    @Field(encryption = "")
    </#if>
    <#if "Y" == xlsxMap[key].type?upper_case>
    private BigDecimal ${key} = new BigDecimal(0);
    <#else>
    private String ${key} = StringUtils.EMPTY;
    </#if>
    </#list>
    <#-- 處理getter、setter -->
    <#list xlsxMap?keys as key>
    <#if "Y" == xlsxMap[key].type?upper_case>
    public BigDecimal get${xlsxMap[key].getsetName}(){
        return ${key};
    }
    public void set${xlsxMap[key].getsetName}(BigDecimal value){
        this.${key} = value;
    }
    <#else>
    public String get${xlsxMap[key].getsetName}(){
        return ${key};
    }
    public void set${xlsxMap[key].getsetName}(String value){
        this.${key} = value;
    }
    </#if>
    </#list>
}
