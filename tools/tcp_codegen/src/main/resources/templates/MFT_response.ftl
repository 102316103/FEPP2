<#--
修正記錄：
2023/06/20，新增MFT電文CodeGen
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.mft.${xlsxFile.cellMessageType};

import com.syscom.fep.vo.text.mft.MFTGeneral;
import com.syscom.fep.vo.text.mft.MFTTextBase;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.frmcommon.annotation.Field;

public class ${xlsxFile.className} extends MFTTextBase {
    <#-- 處理屬性宣告 -->
    <#list xlsxFile.xlsxList as xlsx>
    @Field(length =${xlsx.lg})
    private String ${xlsx.name} = StringUtils.EMPTY;

    </#list>
    <#-- 電文總長度 -->
    private static final int _TotalLength = ${xlsxFile.totalSize};

    <#-- 處理getter、setter -->
    <#list xlsxFile.xlsxList as xlsx>
    public String get${xlsx.getsetName}(){
        return ${xlsx.name};
    }
    public void set${xlsx.getsetName}(String value) {
        this.${xlsx.name} = value;
    }
    </#list>

    <#-- 處理覆寫方法 -->
    @Override
    public int getTotalLength() {
       return _TotalLength;
    }

    @Override
    public MFTGeneral parseFlatfile(String flatfile) throws Exception {
       return null;
    }

    @Override
    public String makeMessageFromGeneral(MFTGeneral general) {
       return null;
    }

    public String makeMessage() {
        <#list xlsxFile.xlsxList as xlsx>
        this.set${xlsx.getsetName}(StringUtils.rightPad(this.get${xlsx.getsetName}(),${xlsx.lg},StringUtils.SPACE));
        </#list>
        return <#list xlsxFile.xlsxList as xlsx>get${xlsx.getsetName}()<#if xlsx_has_next>+</#if></#list>;
    }

    @Override
    public void toGeneral(MFTGeneral general) throws Exception {
    }
}
