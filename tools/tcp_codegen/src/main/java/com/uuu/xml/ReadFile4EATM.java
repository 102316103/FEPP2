package com.uuu.xml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.uuu.bean.XlsxFile;
import com.uuu.common.CommonValue;
import com.uuu.common.ReadFileUtils;
import com.uuu.common.ReadSingleFile;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/*
 * 20230206	Ben
 * 針對EATM電文(XML格式)
 * Request上行,Response下行
 * 注意：excel檔案第一欄內容須填寫 HEADER 或 BODY 、 放4731完整電文的欄位 ATMDATA 其數值型態欄位內容須=N(表示不處理)
 * 		上行ATMDATA標千的內容為 4731完整電文，再透過 paseMessage(ATMDATA)切割資料入所有其它屬性之中
 * 			上行ATMDATA標千內容與ATM CodeGen邏輯相同才是。ex. 將Ebdic轉換成Ascii 編碼
 * 				Mac值是放在 PICCMACD 欄位
 *		上行request parseMessage()方法中 增加判斷 getNotAscii() = N 時則使用 StringUtil.fromHex()     與 ATM CodeGen 同步
 * 		下行則是我們指派值，不須切割資料，但有些欄位內容為固定(ex. 分隔符號=;/  結束符號=#/  )
 * 注意事項：ATM CodeGen 上行方法toGeneral() 須同步於 EATM CodeGen parseMessage()
 *
 */
public class ReadFile4EATM extends ReadSingleFile {
	@Override
	public void doWrite(XlsxFile xlsxFile) throws IOException {
		Configuration cfg = ReadSingleFile.getFreeMarkerConfig();
		Template template;
		String tFileName = (StringUtils.equalsIgnoreCase("request", xlsxFile.getCellMessageType())) ? 
				"EATM_request.ftl" : "EATM_response.ftl";
		try {
			if(com.uuu.Main.notJar) {
				// 設定模板所在的路徑
				CommonValue.setConfiguration(cfg, this.getClass(), "templates");
	
				// 載入模板
				template = cfg.getTemplate(tFileName);
			}else {
				template = CommonValue.getTemplate(tFileName);
			}

			// 建立模版變數
			Map<String, Object> data = new HashMap<>();
			data.put("xlsxFile", xlsxFile);

			// 處理模板並輸出結果
			FileWriter writer = ReadFileUtils.getFileWriterByFileType(xlsxFile.getFileType(), xlsxFile.getClassName());
			template.process(data, writer);
		} catch (IOException | TemplateException e) {
			e.printStackTrace();
		}
	}
}
