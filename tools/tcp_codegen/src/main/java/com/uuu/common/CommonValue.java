package com.uuu.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class CommonValue {
	public static String propertiesPath = "src\\main\\resources\\properties\\";
	public static String outputPath = "output\\%s\\%s.java";
	public static Template template;
	
	public static void setConfiguration(Configuration cfg, Class<?> clazz, String directoryName) throws IOException {
		if(com.uuu.Main.notJar) {
			cfg.setClassForTemplateLoading(clazz, "/" + directoryName);
		}else {
			cfg.setDirectoryForTemplateLoading(new File(directoryName));
		}
	}
	
	public static Template getTemplate(String fileName) throws IOException {
		String path = "templates/";
		if(!com.uuu.Main.notJar) {
			path = "resources/" + path;
		}
		//判斷是否已取得樣版
		if(template == null) {
			System.out.println("create template : " + fileName);
			ClassPathResource cpr = new ClassPathResource(path + fileName);
			InputStream is = cpr.getInputStream();
			java.io.Reader reader = new java.io.InputStreamReader(is);
			template = new Template(fileName,reader ,null);
		}else {
			//判斷已取得的樣版的名稱是否和要取得的樣版名稱相同
			if(!fileName.equals(template.getName())) {
				System.out.println("create new template : " + fileName);
				ClassPathResource cpr = new ClassPathResource(path + fileName);
				InputStream is = cpr.getInputStream();
				java.io.Reader reader = new java.io.InputStreamReader(is);
				template = new Template(fileName,reader ,null);
			}
		}
		
		return template;
	}
}
