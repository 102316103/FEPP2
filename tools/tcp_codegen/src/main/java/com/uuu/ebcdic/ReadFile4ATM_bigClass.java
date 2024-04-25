package com.uuu.ebcdic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.uuu.bean.FileType;
import com.uuu.bean.Xlsx;
import com.uuu.bean.XlsxFile;
import com.uuu.common.CommonValue;
import com.uuu.common.ReadFileUtils;
import com.uuu.common.ReadSingleFile;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class ReadFile4ATM_bigClass {
	public void doWrite(String folderName) {
		// 大class上行電文
		Map<String, Xlsx> mapRequest = new HashMap<>();
		// 大class下行電文
		Map<String, Xlsx> mapResponse = new HashMap<>();
		// codegen配置檔列表
		List<File> fileList = ReadFileUtils.findAllFilesInFolder(new File(folderName));

		// 電文屬性資料
		List<XlsxFile> xlsxFileList = new ArrayList<>();
		for (File excelFile : fileList) {
			xlsxFileList.add(ReadFileUtils.getXlsxFile(excelFile));
		}

		// 區分上、下行電文
		xlsxFileList.forEach(xlsxFile -> {
			String messageType = xlsxFile.getCellMessageType();
			xlsxFile.getXlsxList().stream().forEach(xlsx -> {
				// 上行電文
				if (StringUtils.equalsIgnoreCase("request", messageType)) {
					mapRequest.put(xlsx.getName(), xlsx);
				}
				// 下行電文
				else if (StringUtils.equalsIgnoreCase("response", messageType)) {
					mapResponse.put(xlsx.getName(), xlsx);
				}
				// 非預期情況
				else {
					throw new RuntimeException("Request/Response欄位輸入非預期");
				}
			});
		});

		try {
			// ! 建立request java檔案
			String requestFileName = "ATMGeneralRequest";
			this.writeRequestJavaFile(requestFileName, mapRequest);

			// ! 建立response java檔案
			String responseFileName = "ATMGeneralResponse";
			this.writeRequestJavaFile(responseFileName, mapResponse);
		} catch (IOException | TemplateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 建立java檔案
	 *
	 * @param fileName 産出檔名
	 * @param xlsxList 電文屬性資料列表
	 * @throws ParseException
	 * @throws MalformedTemplateNameException
	 * @throws TemplateNotFoundException
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void writeRequestJavaFile(String fileName, Map<String, Xlsx> attrbuteMap) //
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException {
		Configuration cfg = ReadSingleFile.getFreeMarkerConfig();
		Template template;
		String tFileName = "ATM_bigclass.ftl";
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
		data.put("package", "com.syscom.fep.vo.text.atm"); // 定義java檔所在包
		data.put("className", fileName);
		data.put("xlsxMap", attrbuteMap);

		// 處理模板並輸出結果
		FileWriter writer = ReadFileUtils.getFileWriterByFileType(FileType.ATM_bigClass, fileName);
		template.process(data, writer);
	}
}
