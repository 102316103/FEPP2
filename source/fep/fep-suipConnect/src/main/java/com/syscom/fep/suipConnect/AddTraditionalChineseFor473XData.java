package com.syscom.fep.suipConnect;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AddTraditionalChineseFor473XData {
	
	static String telByDolly = "E:\\Dolly\\合庫壓測電文.txt";//dolly給的電文
	
	static String telBy473XData = "E:\\Dolly\\473XData.txt";//要加入中文的電文內容
	
	static String outPutTel = "E:\\Dolly\\";

	public static void main(String args[]) {
		String telDolly = "";
		String telDollySp = "";
		String tel473X = "";
		String tel473XSp = "";
		String out = "";
		String UTF_8_POM = "\uFEFF";
		List<String> outList = new ArrayList<String>();
		try {
			//dolly給的壓測電文(合庫壓測電文.txt)
			InputStream dolly = new FileInputStream(new File(telByDolly));
			InputStreamReader isr = new InputStreamReader(dolly, "UTF-8");
			//要加入中文的電文(473XData.txt)
			InputStream data473X = null;
			InputStreamReader isr473X = null;
			int i = 1;
			//產出新的電文
			try (Scanner obj = new Scanner(isr)) {//dolly給的壓測電文(合庫壓測電文.txt)
				DataOutputStream os = new DataOutputStream(new FileOutputStream(outPutTel+"tmp.txt"));
				while(obj.hasNextLine()){
					telDolly =  obj.nextLine().toString();	
					telDollySp = telDolly.split(",")[2];
					data473X = new FileInputStream(new File(telBy473XData));
					isr473X = new InputStreamReader(data473X, "UTF-8");
					try (Scanner o473X = new Scanner(isr473X)) {//要加入中文的電文(473XData.txt)
						while(o473X.hasNextLine()){
							tel473X = o473X.nextLine();
							System.out.println("tel473X:"+tel473X);
							System.out.println("tel473X.split(\",\").length:"+tel473X.split(",").length);
							if (tel473X.split(",").length == 1) {
								String[] line = tel473X.split("");
								for(String ii : line) {
									if(containsHanScript(ii)) {
										System.out.println("tel:"+tel473X+" 為中文");
										out = tel473X;
										break;
									}
								}
							}
							if(tel473X.split(",").length == 3) {
								tel473XSp = tel473X.split(",")[2];
								if(tel473XSp.equals(telDollySp)) {
									System.out.println("telDollySp:"+telDollySp);
									System.out.println("tel473XSp :"+tel473XSp);
									System.out.println("電文一樣"+i);
									i++;
									if(out.startsWith(UTF_8_POM)) {
										out = out.substring(1);
									}
//										if(tel1.startsWith(UTF_8_POM)) {
//											tel1 = tel1.substring(1);
//										}
									outList.add(out+"\n");
									outList.add(telDolly+"\n");
								}
							}
						}
					}
					
				}
				for (String outs : outList) {
					os.write(outs.getBytes());
				}
				if(os != null) {
					os.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//判斷是否有中文
	public static boolean containsHanScript(String s) {
	    for (int i = 0; i < s.length(); ) {
	        int codepoint = s.codePointAt(i);
	        i += Character.charCount(codepoint);
	        if (Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN) {
	            return true;
	        }
	    }
	    return false;
	}
}
