package com.uuu.string;

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

public class ReadFile4MFT extends ReadSingleFile {
	@Override
	public void doWrite(XlsxFile xlsxFile) {
		Configuration cfg = ReadSingleFile.getFreeMarkerConfig();
		Template template;
		String tFileName = (StringUtils.equalsIgnoreCase("request", xlsxFile.getCellMessageType())) ? 
				"MFT_request.ftl" : "MFT_response.ftl";
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
