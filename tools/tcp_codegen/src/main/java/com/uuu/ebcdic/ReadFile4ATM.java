package com.uuu.ebcdic;

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
 * 注意事項：ATM CodeGen 上行方法toGeneral() 須同步於 EATM CodeGen parseMessage()
 * 20230207     下行response makeMessage方法中 增加Type=#功能。只取 # 前面的字串(含#)，後面全Trim掉
 * 20230131     上行request toGeneral()方法中 增加判斷 getNotAscii() = N 時則使用 StringUtil.fromHex()
 * 20230110		上行request toGeneral()方法中 原 this.toAscii() 改為 EbcdicConverter.fromHex(CCSID.English, );
 * 20221229     下行response 增加makeMessage   左補空白、內容如為空則固定寫 $0
 * 				 Type
 *               	E = ;#   【固定放結束符號】
 *                  S = ;/   【固定放分隔符號】
 *                  L = 	 【左補空白、內容如為空則固定寫 $0】
 *                  Y = 	 【左補空白】
 *                  # =      【該欄位內容將只取 # 前面的字串(含#)，後面的全部 TRIM掉 [#=結束符號，只認第1個#]】
 * 20221116	    增加makeMessageFromGeneral()
 */
public class ReadFile4ATM extends ReadSingleFile {
	@Override
	public void doWrite(XlsxFile xlsxFile) {
		Configuration cfg = ReadSingleFile.getFreeMarkerConfig();
		Template template;
		String tFileName = (StringUtils.equalsIgnoreCase("request", xlsxFile.getCellMessageType())) ? 
				"ATM_request.ftl" : "ATM_response.ftl";
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
