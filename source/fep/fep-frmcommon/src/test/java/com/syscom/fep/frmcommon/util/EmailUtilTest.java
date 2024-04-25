package com.syscom.fep.frmcommon.util;

import org.junit.jupiter.api.Test;

public class EmailUtilTest {
	@Test
	public void sendSimpleEmail() throws Exception {
		EmailUtil.sendSimpleEmail(
				"smtp.qq.com",
				25,
				"fep2022",
				"guugsmckizmadhfj",
				"fep2022@qq.com",
				"myfifa2005@qq.com,richard_yu@email.lingan.com.cn;annie_bai@email.lingan.com.cn",
				null,
				"這是一封測試郵件(High)",
				"這是一封測試郵件(High)",
				EmailUtil.MailPriority.High);
		EmailUtil.sendSimpleEmail(
				"smtp.qq.com",
				25,
				"fep2022",
				"guugsmckizmadhfj",
				"fep2022@qq.com",
				"myfifa2005@qq.com,richard_yu@email.lingan.com.cn;annie_bai@email.lingan.com.cn",
				null,
				"這是一封測試郵件(Normal)",
				"這是一封測試郵件(Normal)",
				EmailUtil.MailPriority.Normal);
		EmailUtil.sendSimpleEmail(
				"smtp.qq.com",
				25,
				"fep2022",
				"guugsmckizmadhfj",
				"fep2022@qq.com",
				"myfifa2005@qq.com,richard_yu@email.lingan.com.cn;annie_bai@email.lingan.com.cn",
				null,
				"這是一封測試郵件(Lowest)",
				"這是一封測試郵件(Lowest)",
				EmailUtil.MailPriority.Lowest);
	}
	@Test
	public void sendHtmlEmail() throws Exception {
		String html = "<!DOCTYPE HTML>\n" +
				"<html>\n" +
				"<head>\n" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
				"<style>\n" +
				".AlignLeft { text-align: left; }\n" +
				".AlignCenter { text-align: center; }\n" +
				".AlignRight { text-align: right; }\n" +
				"body { font-family: sans-serif; font-size: 11pt; }\n" +
				"img.AutoScale { max-width: 100%; max-height: 100%; }\n" +
				"td { vertical-align: top; padding-left: 4px; padding-right: 4px; }\n" +
				"\n" +
				"tr.SectionGap td { font-size: 4px; border-left: none; border-top: none; border-bottom: 1px solid Black; border-right: 1px solid Black; }\n" +
				"tr.SectionAll td { border-left: none; border-top: none; border-bottom: 1px solid Black; border-right: 1px solid Black; }\n" +
				"tr.SectionBegin td { border-left: none; border-top: none; border-right: 1px solid Black; }\n" +
				"tr.SectionEnd td { border-left: none; border-top: none; border-bottom: 1px solid Black; border-right: 1px solid Black; }\n" +
				"tr.SectionMiddle td { border-left: none; border-top: none; border-right: 1px solid Black; }\n" +
				"tr.SubsectionAll td { border-left: none; border-top: none; border-bottom: 1px solid Gray; border-right: 1px solid Black; }\n" +
				"tr.SubsectionEnd td { border-left: none; border-top: none; border-bottom: 1px solid Gray; border-right: 1px solid Black; }\n" +
				"table.fc { border-top: 1px solid Black; border-left: 1px solid Black; width: 100%; font-family: monospace; font-size: 10pt; }\n" +
				"td.TextItemInsigMod { color: #000000; background-color: #EEEEFF; }\n" +
				"td.TextItemInsigOrphan { color: #000000; background-color: #FAEEFF; }\n" +
				"td.TextItemNum { color: #696969; background-color: #F0F0F0; }\n" +
				"td.TextItemSame { color: #000000; background-color: #FFFFFF; }\n" +
				"td.TextItemSigMod { color: #000000; background-color: #FFE3E3; }\n" +
				"td.TextItemSigOrphan { color: #000000; background-color: #F1E3FF; }\n" +
				".TextSegInsigDiff { color: #0000FF; }\n" +
				".TextSegReplacedDiff { color: #0000FF; font-style: italic; }\n" +
				".TextSegSigDiff { color: #FF0000; }\n" +
				"td.TextItemInsigAdd { color: #000000; background-color: #EEEEFF; }\n" +
				"td.TextItemInsigDel { color: #000000; background-color: #EEEEFF; text-decoration: line-through; }\n" +
				"td.TextItemSigAdd { color: #000000; background-color: #FFE3E3; }\n" +
				"td.TextItemSigDel { color: #000000; background-color: #FFE3E3; text-decoration: line-through; }\n" +
				"</style>\n" +
				"<title>TCBFEP_application-gateway-fisc.properties</title>\n" +
				"</head>\n" +
				"<body>\n" +
				"TCBFEP_application-gateway-fisc.properties<br>\n" +
				"已产生： 2023/08/11 15:24:52<br>\n" +
				"&nbsp; &nbsp;\n" +
				"<br>\n" +
				"模式：&nbsp; 全部 &nbsp;\n" +
				"<br>\n" +
				"左边旧版文件： C:\\Users\\Richard\\AppData\\Local\\Temp\\TortoiseGit\\application-gateway-fisc-5686522d.002.properties &nbsp;\n" +
				"<br>\n" +
				"右边新版文件： C:\\Users\\Richard\\AppData\\Local\\Temp\\TortoiseGit\\application-gateway-fisc-189cc299.000.properties &nbsp;\n" +
				"<br>\n" +
				"<table class=\"fc\" cellspacing=\"0\" cellpadding=\"0\">\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSame\">server.port=8301</td>\n" +
				"<td class=\"AlignCenter\">=</td>\n" +
				"<td class=\"TextItemSame\">server.port=8301</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.application.name=fep-gateway</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.application.name=fep-gateway</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">management.endpoints.prometheus.enabled=true</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">management.endpoints.prometheus.enabled=true</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">management.endpoints.web.exposure.include=*</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">management.endpoints.web.exposure.include=*</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">management.metrics.tags.application=${spring.fep.application.name}-fisc</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">management.metrics.tags.application=${spring.fep.application.name}-fisc</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">management.endpoint.health.show-details=always</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">management.endpoint.health.show-details=always</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">#restful controller for FISC Gateway</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">#restful controller for FISC Gateway</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.register.controller[0]=com.syscom.fep.gateway.netty.fisc.ctrl.FISCGatewayClientRestfulCtrl</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.register.controller[0]=com.syscom.fep.gateway.netty.fisc.ctrl.FISCGatewayClientRestfulCtrl</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSame\">#register Restful Controller recv message from FEP via restful</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">#register Restful Controller recv message from FEP via restful</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSigMod\">spring.register.controller[1]=com.syscom.fep.gateway.netty.fisc.<span class=\"TextSegSigDiff\">in</span>.FISCGateway<span class=\"TextSegSigDiff\">Clie</span>nt<span class=\"TextSegInsigDiff\">R</span>e<span class=\"TextSegSigDiff\">stfulTransmissionIn</span></td>\n" +
				"<td class=\"AlignCenter\">&lt;&gt;</td>\n" +
				"<td class=\"TextItemSigMod\">spring.register.controller[1]=com.syscom.fep.gateway.netty.fisc.<span class=\"TextSegSigDiff\">server</span>.FISCGateway<span class=\"TextSegSigDiff\">ServerCo</span>nt<span class=\"TextSegInsigDiff\">r</span><span class=\"TextSegSigDiff\">oll</span>e<span class=\"TextSegSigDiff\">r</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">#register</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Netty</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Bean</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">for</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">recv</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">message</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">from</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">FEP</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">via</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">socket</span></td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.register.bean[0]=com.syscom.fep.gateway.netty.fisc.in.FISCGatewayClientNettyServerTransmissionIn</span></td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionAll\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">=</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.netty.fisc.host=${spring.fep.hostip}</span></td>\n" +
				"<td class=\"AlignCenter\">&lt;&gt;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.netty.fisc.port=18091</span></td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.netty.fisc.reestablishConnectionInterval=3000</span></td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.netty.fisc.acceptIp=</span></td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemInsigMod\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSigMod\">#register</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\">#register<span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">FISC</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Gateway</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Sender</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[0].gtwClassName=com.syscom.fep.gateway.netty.fisc.sender.FISCGatewayClientSender</td>\n" +
				"<td class=\"AlignCenter\">=</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[0].gtwClassName=com.syscom.fep.gateway.netty.fisc.sender.FISCGatewayClientSender</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[0].hdrClassName=com.syscom.fep.gateway.netty.fisc.sender.FISCGatewayClientSenderChannelInboundHandlerAdapter</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[0].hdrClassName=com.syscom.fep.gateway.netty.fisc.sender.FISCGatewayClientSenderChannelInboundHandlerAdapter</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[0].cnfClassName=com.syscom.fep.gateway.netty.fisc.sender.FISCGatewayClientSenderConfiguration</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[0].cnfClassName=com.syscom.fep.gateway.netty.fisc.sender.FISCGatewayClientSenderConfiguration</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[0].prcClassName=com.syscom.fep.gateway.netty.fisc.sender.FISCGatewayClientSenderProcessRequest</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[0].prcClassName=com.syscom.fep.gateway.netty.fisc.sender.FISCGatewayClientSenderProcessRequest</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionAll\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">-+</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">#register</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">FISC</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Gateway</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Receiver</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[1].gtwClassName=com.syscom.fep.gateway.netty.fisc.receiver.FISCGatewayClientReceiver</td>\n" +
				"<td class=\"AlignCenter\">=</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[1].gtwClassName=com.syscom.fep.gateway.netty.fisc.receiver.FISCGatewayClientReceiver</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[1].hdrClassName=com.syscom.fep.gateway.netty.fisc.receiver.FISCGatewayClientReceiverChannelInboundHandlerAdapter</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[1].hdrClassName=com.syscom.fep.gateway.netty.fisc.receiver.FISCGatewayClientReceiverChannelInboundHandlerAdapter</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[1].cnfClassName=com.syscom.fep.gateway.netty.fisc.receiver.FISCGatewayClientReceiverConfiguration</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[1].cnfClassName=com.syscom.fep.gateway.netty.fisc.receiver.FISCGatewayClientReceiverConfiguration</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[1].prcClassName=com.syscom.fep.gateway.netty.fisc.receiver.FISCGatewayClientReceiverProcessRequest</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.register[1].prcClassName=com.syscom.fep.gateway.netty.fisc.receiver.FISCGatewayClientReceiverProcessRequest</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">-+</td>\n" +
				"<td class=\"TextItemInsigMod\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">#register</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">FISC</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Gateway</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Server</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.register[2].gtwClassName=com.syscom.fep.gateway.netty.fisc.server.FISCGatewayServer</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.register[2].hdrClassName=com.syscom.fep.gateway.netty.fisc.server.FISCGatewayServerChannelInboundHandlerAdapter</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.register[2].mgrClassName=com.syscom.fep.gateway.netty.fisc.server.FISCGatewayServerProcessRequestManager</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.register[2].cnfClassName=com.syscom.fep.gateway.netty.fisc.server.FISCGatewayServerConfiguration</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.register[2].ipfClassName=com.syscom.fep.gateway.netty.fisc.server.FISCGatewayServerRuleIpFilter</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">=</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">#FISC Gateway Sender</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">#FISC Gateway Sender</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.host=172.25.164.162</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.host=172.25.164.162</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.port=3003</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.port=3003</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.reestablishConnectionInterval=10000</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.reestablishConnectionInterval=10000</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.encoding=ascii</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.encoding=ascii</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.clientId=B889A01I</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.clientId=B889A01I</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.checkCode=12345678</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.checkCode=12345678</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.tcpKeepIdle=120</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.tcpKeepIdle=120</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.tcpKeepInterval=10</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.sender.tcpKeepInterval=10</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">#FISC Gateway Receiver</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">#FISC Gateway Receiver</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.host=172.25.164.162</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.host=172.25.164.162</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.port=3004</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.port=3004</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.reestablishConnectionInterval=10000</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.reestablishConnectionInterval=10000</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.disConnectInterval=30000</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.disConnectInterval=30000</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.encoding=ascii</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.encoding=ascii</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.clientId=B889A01I</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.clientId=B889A01I</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.checkCode=12345678</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.checkCode=12345678</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.tcpKeepIdle=120</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.tcpKeepIdle=120</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.tcpKeepInterval=10</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.transmission.fisc.receiver.tcpKeepInterval=10</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">-+</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">#FISC</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Gateway</span><span class=\"TextSegInsigDiff\"> </span><span class=\"TextSegSigDiff\">Server</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.transmission.fisc.server.host=${spring.fep.hostip}</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSigMod\"><span class=\"TextSegSigDiff\">spring.fep.gateway.transmission.fisc.server.port=18091</span></td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemInsigMod\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionBegin\">\n" +
				"<td class=\"TextItemSame\">#register Scheduler Job</td>\n" +
				"<td class=\"AlignCenter\">=</td>\n" +
				"<td class=\"TextItemSame\">#register Scheduler Job</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.register[0].className=com.syscom.fep.scheduler.job.impl.AppMonitorJob</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.register[0].className=com.syscom.fep.scheduler.job.impl.AppMonitorJob</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.register[0].configClassName=com.syscom.fep.scheduler.job.impl.AppMonitorJobConfig</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.register[0].configClassName=com.syscom.fep.scheduler.job.impl.AppMonitorJobConfig</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.register[1].className=com.syscom.fep.gateway.job.fisc.FISCGatewayClientAppMonitorJob</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.register[1].className=com.syscom.fep.gateway.job.fisc.FISCGatewayClientAppMonitorJob</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.register[1].configClassName=com.syscom.fep.gateway.job.fisc.FISCGatewayClientAppMonitorJobConfig</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.register[1].configClassName=com.syscom.fep.gateway.job.fisc.FISCGatewayClientAppMonitorJobConfig</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">#AppMonitorJob</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">#AppMonitorJob</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.cronExpression=0 0/1 * * * ?</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.cronExpression=0 0/1 * * * ?</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.serviceName=${management.metrics.tags.application}</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.serviceName=${management.metrics.tags.application}</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.serviceHostIp=${spring.fep.hostip}</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.serviceHostIp=${spring.fep.hostip}</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.serviceHostName=${spring.fep.hostname}</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.serviceHostName=${spring.fep.hostname}</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.serviceUrl=http://${spring.fep.hostip}:${server.port}</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.serviceUrl=http://${spring.fep.hostip}:${server.port}</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.monitorUrl=http://${spring.fep.hostip}:8201/api/mon/SendMessage</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.scheduler.job.app-monitor.monitorUrl=http://${spring.fep.hostip}:8201/api/mon/SendMessage</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">&nbsp;</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">#FISC Gateway Job</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">#FISC Gateway Job</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionMiddle\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.job.fisc.app-monitor.cronExpression=0 0/1 * * * ?</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.job.fisc.app-monitor.cronExpression=0 0/1 * * * ?</td>\n" +
				"</tr>\n" +
				"<tr class=\"SectionEnd\">\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.job.fisc.app-monitor.monitorUrl=http://${spring.fep.hostip}:8201/recv/net/client</td>\n" +
				"<td class=\"AlignCenter\">&nbsp;</td>\n" +
				"<td class=\"TextItemSame\">spring.fep.gateway.job.fisc.app-monitor.monitorUrl=http://${spring.fep.hostip}:8201/recv/net/client</td>\n" +
				"</tr>\n" +
				"</table>\n" +
				"<br>\n" +
				"</body>\n" +
				"</html>\n";
		EmailUtil.sendHtmlEmail(
				"smtp.qq.com",
				25,
				"fep2022",
				"guugsmckizmadhfj",
				"fep2022@qq.com",
				"myfifa2005@qq.com,richard_yu@email.lingan.com.cn;annie_bai@email.lingan.com.cn",
				null,
				"這是一封測試郵件(High)",
				html,
				EmailUtil.MailPriority.High);
		EmailUtil.sendHtmlEmail(
				"smtp.qq.com",
				25,
				"fep2022",
				"guugsmckizmadhfj",
				"fep2022@qq.com",
				"myfifa2005@qq.com,richard_yu@email.lingan.com.cn;annie_bai@email.lingan.com.cn",
				null,
				"這是一封測試郵件(Normal)",
				html,
				EmailUtil.MailPriority.Normal);
		EmailUtil.sendHtmlEmail(
				"smtp.qq.com",
				25,
				"fep2022",
				"guugsmckizmadhfj",
				"fep2022@qq.com",
				"myfifa2005@qq.com,richard_yu@email.lingan.com.cn;annie_bai@email.lingan.com.cn",
				null,
				"這是一封測試郵件(Lowest)",
				html,
				EmailUtil.MailPriority.Lowest);
	}
}
