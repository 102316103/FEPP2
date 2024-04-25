package com.uuu.xml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.uuu.bean.HeaderType;
import com.uuu.bean.XlsxFile;
import com.uuu.common.CommonValue;
import com.uuu.common.ReadFileUtils;
import com.uuu.common.ReadSingleFile;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/*
 * 20230111	Ben
 * 針對VO電文(XML格式 VO=外圍語音 須放在 IVR package下 )
 * Request上行,Response下行
 * 注意：excel檔案第一欄內容須填寫 HEADER 或 BODY 或 PAYDATA 而 PAYDATA標千則須放空白
 * 		上行用PAYDATA內容為 1.汽燃料費、全國繳費 2.繳稅 (資料會二擇一放，所以會有一邊是空的)
 */
public class ReadFile4VO extends ReadSingleFile {
	@Override
	public void doWrite(XlsxFile xlsxFile) throws IOException {
		Configuration cfg = ReadSingleFile.getFreeMarkerConfig();
		Template template;
		String tFileName = (StringUtils.equalsIgnoreCase("request", xlsxFile.getCellMessageType())) ? 
				"xml_request.ftl" : "xml_response.ftl";
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
			data.put("package", "com.syscom.fep.vo.text.ivr"); // 定義java檔所在包
			data.put("extends", "VoXmlBase"); // 定義繼承父類
			if (StringUtils.equalsIgnoreCase("request", xlsxFile.getCellMessageType())) {
				data.put("headerTypeList", Arrays.asList( // 設置電文說明的父子類關系
						new HeaderType("HEADER", "Header"), //
						new HeaderType("BODY", "SvcRq").addSubHeaderList(new HeaderType("PAYDATA", "PAYDATA")), //
						new HeaderType("PAYDATA", "PAYDATA")));
			} else {
				data.put("headerTypeList", Arrays.asList( // 設置電文說明的父子類關系
						new HeaderType("HEADER", "Header"), //
						new HeaderType("BODY", "SvcRs")));
			}

			// 處理模板並輸出結果
			FileWriter writer = ReadFileUtils.getFileWriterByFileType(xlsxFile.getFileType(), xlsxFile.getClassName());
			template.process(data, writer);
		} catch (IOException | TemplateException e) {
			e.printStackTrace();
		}
	}
}
