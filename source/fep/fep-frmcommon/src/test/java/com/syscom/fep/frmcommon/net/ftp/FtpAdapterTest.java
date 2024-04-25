package com.syscom.fep.frmcommon.net.ftp;

import com.syscom.fep.frmcommon.FrmcommonBaseTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FtpAdapterTest extends FrmcommonBaseTest {
	@Autowired
	private FtpAdapterFactory factory;
	private FtpProperties ftpProperties;
	private FtpProperties sftpProperties;

	@BeforeEach
	public void setUp() {
		 ftpProperties = new FtpProperties();
		 ftpProperties.setHost("localhost");
		 ftpProperties.setPort(3721);
		 ftpProperties.setPassword("1234");
		 ftpProperties.setProtocol(FtpProtocol.FTP);
		 ftpProperties.setUsername("richard");

		sftpProperties = new FtpProperties();
		sftpProperties.setHost("192.168.30.29");
		sftpProperties.setPort(22);
		sftpProperties.setPassword("syscom");
		sftpProperties.setProtocol(FtpProtocol.SFTP);
		sftpProperties.setUsername("sftpuser");
		// sftpProperties.setSftpRootDirectory("data");
	}

	@Test
	public void testDownload() {
		testDownload(ftpProperties);
		testDownload(sftpProperties);
	}

	@Test
	public void testGetFileList() {
		testGetFileList(ftpProperties);
		testGetFileList(sftpProperties);
	}

	@Test
	public void testCreateRemotePath() {
		testCreateRemotePath(ftpProperties);
		testCreateRemotePath(sftpProperties);
	}

	@Test
	public void testUpload() {
		testUpload(ftpProperties);
		testUpload(sftpProperties);
	}

	@Test
	public void testIsRemotePathExist() {
		testIsRemotePathExist(ftpProperties);
		testIsRemotePathExist(sftpProperties);
	}
	
	@Test
	public void testDelete() {
		testDelete(ftpProperties);
		testDelete(sftpProperties);
	}

	private void testDownload(FtpProperties ftpProperties) {
		if (ftpProperties == null)
			return;
		FtpAdapter ftp = factory.createFtpAdapter(ftpProperties);
		UnitTestLogger.info(ftp.download("C:/Users/Richard/Desktop/ftp/README.txt", "README.txt"));
		UnitTestLogger.info(ftp.download("C:/Users/Richard/Desktop/ftp/download/0.txt", "/download/download-0.txt"));
		UnitTestLogger.info(ftp.download("C:/Users/Richard/Desktop/ftp/download/1/1.txt", "/download/1/download-1.txt"));
		UnitTestLogger.info(ftp.download("C:/Users/Richard/Desktop/ftp/download/2/2.txt", "ftp://localhost:3721/download/2/download-2.txt"));
	}

	private void testGetFileList(FtpProperties ftpProperties) {
		if (ftpProperties == null)
			return;
		FtpAdapter ftp = factory.createFtpAdapter(ftpProperties);
		UnitTestLogger.info(StringUtils.join(ftp.getFileList("/upload", "upload"), ","));
		UnitTestLogger.info(StringUtils.join(ftp.getFileList("ftp://localhost:3721/upload", "*"), ","));
		UnitTestLogger.info(StringUtils.join(ftp.getFileList("ftp://localhost:3721/upload", "*", true), ","));
	}

	private void testCreateRemotePath(FtpProperties ftpProperties) {
		if (ftpProperties == null)
			return;
		FtpAdapter ftp = factory.createFtpAdapter(ftpProperties);
		UnitTestLogger.info(ftp.createRemotePath("ftp://localhost:3721/create", "0"));
		UnitTestLogger.info(ftp.createRemotePath("/create/0/", "01"));
		UnitTestLogger.info(ftp.createRemotePath("/create/", "0/02"));
		UnitTestLogger.info(ftp.createRemotePath("/create/1", null));
	}

	private void testUpload(FtpProperties ftpProperties) {
		if (ftpProperties == null)
			return;
		FtpAdapter ftp = factory.createFtpAdapter(ftpProperties);
		UnitTestLogger.info(ftp.upload("C:/Users/Richard/Desktop/ftp/upload/0.txt", "/upload/upload-0.txt"));
		UnitTestLogger.info(ftp.upload("C:/Users/Richard/Desktop/ftp/upload/1/1.txt", "/upload/1/upload-1.txt"));
		UnitTestLogger.info(ftp.upload("C:/Users/Richard/Desktop/ftp/upload/2/2.txt", "ftp://localhost:3721/upload/2/upload-2.txt"));

		UnitTestLogger.info(ftp.upload("C:/Users/Richard/Desktop/ftp/upload/0.txt", "/upload/", true));
		UnitTestLogger.info(ftp.upload("C:/Users/Richard/Desktop/ftp/upload/1/1.txt", "/upload/1/", true));
		UnitTestLogger.info(ftp.upload("C:/Users/Richard/Desktop/ftp/upload/2/2.txt", "ftp://localhost:3721/upload/2/", true));
	}

	private void testIsRemotePathExist(FtpProperties ftpProperties) {
		if (ftpProperties == null)
			return;
		FtpAdapter ftp = factory.createFtpAdapter(ftpProperties);
		UnitTestLogger.info(ftp.isRemotePathExist("ftp://localhost:3721/create", "0"));
		UnitTestLogger.info(ftp.isRemotePathExist("/create/0/", "01"));
		UnitTestLogger.info(ftp.isRemotePathExist("/create/", "0/02"));
		UnitTestLogger.info(ftp.isRemotePathExist("/create/1", null));
		UnitTestLogger.info(ftp.isRemotePathExist("/create/3", null));
	}
	
	private void testDelete(FtpProperties ftpProperties) {
		if (ftpProperties == null)
			return;
		FtpAdapter ftp = factory.createFtpAdapter(ftpProperties);
		UnitTestLogger.info(ftp.delete("/data/upload/upload-0.txt"));
		UnitTestLogger.info(ftp.delete("/data/upload/1/upload-1.txt"));
		UnitTestLogger.info(ftp.delete("ftp://localhost:3721/data/upload/2/upload-2.txt"));
		UnitTestLogger.info(ftp.delete("/data/upload/0.txt"));
		UnitTestLogger.info(ftp.delete("/data/upload/1/1.txt"));
		UnitTestLogger.info(ftp.delete("ftp://localhost:3721/data/upload/2/2.txt"));
	}
}
