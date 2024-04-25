<#--
修正記錄：
2023/06/20，新增MFT電文CodeGen
-->
<#-- 處理包路徑、引入、宣告 -->
package com.syscom.fep.vo.text.mft.${xlsxFile.cellMessageType};

import com.syscom.fep.vo.text.mft.MFTGeneral;
import com.syscom.fep.vo.text.mft.MFTGeneralRequest;
import com.syscom.fep.vo.text.mft.MFTTextBase;
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
       return this.parseFlatfile(this.getClass(), flatfile);
    }

    @Override
    public String makeMessageFromGeneral(MFTGeneral general) {
       return null;
    }

    public String makeMessage() {
       return null;
    }

    @Override
    public void toGeneral(MFTGeneral general) throws Exception {
        MFTGeneralRequest request = general.getRequest();
        <#list xlsxFile.xlsxList as xlsx>
        <#-- 直接賦值，不轉換 -->
        request.set${xlsx.getsetName}(this.get${xlsx.getsetName}());
        </#list>
    }
}
