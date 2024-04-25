package com.syscom.fep.suipConnect;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StackTracePointCut(caller = SvrConst.SVR_SUIP_CONNECT)
public class FepSuipConnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(FepSuipConnectApplication.class, args);
	}

}
