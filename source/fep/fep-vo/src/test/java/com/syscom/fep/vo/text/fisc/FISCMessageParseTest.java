package com.syscom.fep.vo.text.fisc;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.ext.mapper.BitmapdefExtMapper;
import com.syscom.fep.mybatis.ext.mapper.DataattrExtMapper;
import com.syscom.fep.mybatis.model.Bitmapdef;
import com.syscom.fep.mybatis.model.Dataattr;
import com.syscom.fep.vo.VoBaseTest;
import com.syscom.fep.vo.text.fisc.FISC_INBK.DefIC_DATA;
import com.syscom.fep.vo.text.fisc.FISC_INBK.DefOB_DATA;
import com.syscom.fep.vo.text.fisc.FISC_INBK.DefORI_DATA;
import org.springframework.test.context.ActiveProfiles;

/**
 * 用來測試送出的FISC電文format是否正確
 * 
 * @author Richard
 *
 */
// @ActiveProfiles({"integration","mybatis","taipei"})
public class FISCMessageParseTest extends VoBaseTest {
	private static final LogHelper logger = LogHelperFactory.getUnitTestLogger();

	@Autowired
	private BitmapdefExtMapper bitmapdefExtMapper;
	@Autowired
	private DataattrExtMapper dataattrExtMapper;

	// cache data
	private HashMap<String, Bitmapdef> bitmapdefMap = new HashMap<String, Bitmapdef>();
	private static HashMap<String, List<Dataattr>> dataAttrList = new HashMap<String, List<Dataattr>>();

	@Test
	public void testParsePcode02002130() {
		String fiscMessage =
				"000000303230303231333035363032353733393530303030303830373030303031303039303831353036343130303030E5905ED024008004000008012B303030303030303034393930303033393435303030303131303039303738303735333535363330AA7822AA";
		this.testParse(FISC_INBK.class, fiscMessage);
	}

	@Test
	public void testParsePcode02002573() {
		String fiscMessage =
				"00000030323030323537333030303430383739353030303030383037303030303130303931383134303833393030303050572F1D24002000000018012B30303030303031343832373030393030313030303132303138303332383135323234343030313238303138303031363632363638303730303032323232FFFFFFFF";
		this.testParse(FISC_INBK.class, fiscMessage);
	}

	@Test
	public void testParsePcode257400() {
		String fiscMessage =
				"000000303230303235373435353036333138343237303030303830373030303031303039333031313131353230303030984B4A6F2400A000000018012B3030303030303134383237303039303031303030313030323032313039333031303236323030303132383031383030313636323636343237353530363330341EE226E7";
		this.testParse(FISC_INBK.class, fiscMessage);
	}

	@Test
	public void testParsePcode213000() {
		String fiscMessage =
				"00000030323030323133303030303431343739353030303030383037303030303130303933303131333533303030303050572F1D24008004000008012B303030303030313030303030303630333232303030303030303030303038303738353838393738D030C46D";
		this.testParse(FISC_INBK.class, fiscMessage);
	}

	@Test
	public void testParsePcode255600() {
		String fiscMessage =
				"000000303230303235353635353036353233383037303030303934333030303031303130303431373038303130303030984B4A6F24002011100010012B30303030303030303934353030393530303030303132303231313030343134353734393635394131303038333139353031303030303330303431353632303138303832392B303030303030303039353130303030303036303030303030303030303239383238343035333137303030304F757453616C6531383038323931363431303920323431313634313039303031393433353530363530323130313030342020202020202020202020202020202020202020303030313030303030303030303030333030303030303030333059202020202020202020202020202020202020202020202020202020202020202020303031323630313830303033373031378C88E117";
		this.testParse(FISC_INBK.class, fiscMessage);
	}

	@Test
	public void testParse(){
		String fiscMessage =
				"000000303531303532303230303834373534393530303030303031313030303031323032303831313338303230303031DDAD05635F802800000000002B30323530303030303030302B30323938373639393830363230313030303030303030342B303030303133373435313638303030303030302D30303030303030303030303030302D30303030303030303030303030302D3030303030303030303030303030";
		this.testParse(FISC_INBK.class, fiscMessage);
	}

	private <T extends FISCHeader> void testParse(Class<T> fiscClass, String fiscMessage) {
		// 定義FiscMessage, 並且把電文塞進去
		T fiscHeader = null;
		try {
			fiscHeader = fiscClass.getDeclaredConstructor(String.class).newInstance(fiscMessage);
		} catch (Exception e) {
			logger.exceptionMsg(e, e.getMessage());
			Assertions.assertFalse(true, e.getMessage());
		}
		// 解讀
		if(fiscHeader != null)
		fiscHeader.parseFISCMsg();
		// 讀取BITMAP定義
		Bitmapdef oBitMap = getBitmapData(fiscHeader.getMessageType() + fiscHeader.getProcessingCode());
		Assertions.assertFalse(
				oBitMap.getBitmapdefBitmapList().indexOf(fiscHeader.getBitMapConfiguration()) < 0,
				StringUtils.join("BitMapConfiguration不正確,fiscHeader.BitMapConfiguration=[", fiscHeader.getBitMapConfiguration(), "],",
						"BitMap.BITMAPDEF_BITMAP_LIST=[", oBitMap.getBitmapdefBitmapList(), "]"));
		// 讀取DATAATTR定義
		List<Dataattr> dvAttr = getDataAttributeDataByType(oBitMap.getBitmapdefType().toString());
		String apdata = fiscHeader.getAPData();
		int k = 0;
		try {
			// 將財金Bitmap Hex轉2進位
			char[] bitMaps = StringUtil.convertFromAnyBaseString(fiscHeader.getBitMapConfiguration(), 16, 2, 64).toCharArray();
			for (int i = 0; i < bitMaps.length; i++) {
				if (bitMaps[i] == '1') {
					int tmplen = 0;
					String tmpHex = StringUtils.EMPTY;
					String tmpAscii = StringUtils.EMPTY;
					String dataType = dvAttr.get(i).getDataattrDatatype();
					// 變動長度欄位
					if ("C".equalsIgnoreCase(dataType) || "B".equalsIgnoreCase(dataType)) {
						// 變動長度從電文取前2個BYTE為此欄位長度L(2)+DATA
						tmplen = Integer.parseInt(StringUtil.convertFromAnyBaseString(apdata.substring(k, k + 4), 16, 10, 0)) * 2;
						tmpHex = apdata.substring(k + 4, k + 4 + tmplen - 4);
						logger.debug("tmp:", tmpHex, " length:" + tmplen);
					} else {
						// 固定長度直接抓
						tmplen = dvAttr.get(i).getDataattrHexLen().intValue();
						tmpHex = apdata.substring(k, k + tmplen);
					}
					// 轉ASCII判斷
					if ("Y".equalsIgnoreCase(dvAttr.get(i).getDataattrTransferflag())) {
						tmpAscii = StringUtil.fromHex(tmpHex);
					}
					k = k + tmplen;
					// 晶片卡檢核TAC長度要大於等於10,小於等於130
					if ("25".equals(fiscHeader.getProcessingCode().substring(0, 2))) {
						if (i == 56) {
							Assertions.assertFalse(
									tmplen < 10 || tmplen > 130,
									StringUtils.join("ChkBitmap(TAC) 第[", i, "]位訊息格式錯誤,TAC長度錯誤!"));
						}
					}
					// 型別轉換及檢查及將檢核過的欄位值塞入Property中()
					switch (dataType) {
						case "2": // 數字(兩位小數)
							Assertions.assertFalse(
									!PolyfillUtil.isNumeric(tmpAscii),
									StringUtils.join("ChkBitmap 第[", i, "]位訊息格式錯誤, tmpAscii = [", tmpAscii, "], tmpHex = [", tmpHex, "]!"));
							fiscHeader.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
							break;
						case "9": // 數字
							Assertions.assertFalse(
									!PolyfillUtil.isNumeric(tmpAscii),
									StringUtils.join("ChkBitmap 第[", i, "]位訊息格式錯誤, tmpAscii = [", tmpAscii, "], tmpHex = [", tmpHex, "]!"));
							fiscHeader.setGetPropertyValue(i, tmpAscii);
							break;
						case "M": // Money
							Assertions.assertFalse(
									!PolyfillUtil.isNumeric(tmpAscii) || "+-".indexOf(tmpAscii.substring(0, 1)) < 0,
									StringUtils.join("ChkBitmap 第[", i, "]位訊息格式錯誤, tmpAscii = [", tmpAscii, "], tmpHex = [", tmpHex, "]!"));
							fiscHeader.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
							break;
						case "A": // 日期
							Assertions.assertFalse(
									CalendarUtil.adStringToADDate(tmpAscii) == null,
									StringUtils.join("ChkBitmap 第[", i, "]位訊息格式錯誤, tmpAscii = [", tmpAscii, "], tmpHex = [", tmpHex, "]!"));
							fiscHeader.setGetPropertyValue(i, tmpAscii);
							break;
						case "D": // 民國日期
							Assertions.assertFalse(
									CalendarUtil.rocStringToADDate("0" + tmpAscii) == null,
									StringUtils.join("ChkBitmap 第[", i, "]位訊息格式錯誤, tmpAscii = [", tmpAscii, "], tmpHex = [", tmpHex, "]!"));
							fiscHeader.setGetPropertyValue(i, tmpAscii);
							break;
						default: // 一般文字欄位
							if ("Y".equalsIgnoreCase(dvAttr.get(i).getDataattrTransferflag())) {
								if (DbHelper.toBoolean(dvAttr.get(i).getDataattrEncoding())) {
									// 轉CNS11643
									// 要求轉碼的元件順便補空白
									fiscHeader.setGetPropertyValue(i, tmpHex);
								} else {
									fiscHeader.setGetPropertyValue(i, tmpAscii);
								}
							} else {
								fiscHeader.setGetPropertyValue(i, tmpHex);
							}
							break;
					}
				}
			}
			if (fiscHeader instanceof FISC_INBK) {
				checkOB_DATA((FISC_INBK) fiscHeader);
				checkIC_DATA((FISC_INBK) fiscHeader);
				checkORI_DATA((FISC_INBK) fiscHeader);
			}
		} catch (Exception e) {
			logger.exceptionMsg(e, e.getMessage());
			Assertions.assertFalse(true, e.getMessage());
		} finally {
			logger.debug("[", fiscHeader.getClass().getSimpleName(), "]FISC Data : ", fiscHeader.toJSON());
		}
	}

	private Bitmapdef getBitmapData(String msgTypePcode) {
		// Fly 2016/11/25 避免多執行續同時對BitmapData做操作，增加LOCK的範圍
		synchronized (bitmapdefMap) {
			if (bitmapdefMap.size() == 0) {
				this.loadBitmapData();
			}
			return bitmapdefMap.get(msgTypePcode);
		}
	}

	private void loadBitmapData() {
		List<Bitmapdef> bitmapdefsList = bitmapdefExtMapper.queryAllData(StringUtils.EMPTY);
		synchronized (bitmapdefMap) {
			bitmapdefMap.clear();
			for (Bitmapdef bitmapdef : bitmapdefsList) {
				bitmapdefMap.put(StringUtils.join(bitmapdef.getBitmapdefMsgtype(), bitmapdef.getBitmapdefPcode()), bitmapdef);
			}
		}
	}

	private List<Dataattr> getDataAttributeDataByType(String attrType) {
		synchronized (dataAttrList) {
			if (dataAttrList.size() == 0) {
				List<Dataattr> dtDataAttr = dataattrExtMapper.queryAllData("");
				for (int i = 1; i <= 7; i++) {
					short temp = (short) i;
					List<Dataattr> filteredAndSortedList = dtDataAttr.stream().filter(t -> t.getDataattrType() == temp).sorted(new Comparator<Dataattr>() {
						@Override
						public int compare(Dataattr o1, Dataattr o2) {
							return o1.getDataattrBitoffset().compareTo(o2.getDataattrBitoffset());
						}
					}).collect(Collectors.toList());
					dataAttrList.put(String.valueOf(i), filteredAndSortedList);
				}
			}
		}
		return dataAttrList.get(attrType);
	}

	private void checkOB_DATA(FISC_INBK fiscINBK) {
		fiscINBK.setOBDATA(new DefOB_DATA());
		if (fiscINBK.getOriData().length() != 195) {
			logger.info("檢查跨境電子支付平台作業資料(bitmap36)總長度不足195位");
			return;
		}
		fiscINBK.getOBDATA().setMerchantId(fiscINBK.getOriData().substring(0, 15));
		fiscINBK.getOBDATA().setClosingDate(fiscINBK.getOriData().substring(15, 23));
		fiscINBK.getOBDATA().setTotTwdAmt(fiscINBK.getOriData().substring(23, 37));
		fiscINBK.getOBDATA().setTwnFee(fiscINBK.getOriData().substring(37, 44));
		fiscINBK.getOBDATA().setSetAmt(fiscINBK.getOriData().substring(44, 56));
		fiscINBK.getOBDATA().setSetCur(fiscINBK.getOriData().substring(56, 59));
		fiscINBK.getOBDATA().setSetExrate(fiscINBK.getOriData().substring(59, 67));
		fiscINBK.getOBDATA().setOrderNo(fiscINBK.getOriData().substring(67, 87));
		fiscINBK.getOBDATA().setRRN(fiscINBK.getOriData().substring(87, 99));
		fiscINBK.getOBDATA().setOriStan(fiscINBK.getOriData().substring(99, 109));
		fiscINBK.getOBDATA().setOriBusinessDate(fiscINBK.getOriData().substring(109, 115));
		fiscINBK.getOBDATA().setOriOrderNo(fiscINBK.getOriData().substring(115, 135));
		fiscINBK.getOBDATA().setTotTwdFee(fiscINBK.getOriData().substring(135, 142));
		fiscINBK.getOBDATA().setTotForAmt(fiscINBK.getOriData().substring(142, 154));
		fiscINBK.getOBDATA().setTotForFee(fiscINBK.getOriData().substring(154, 161));
		fiscINBK.getOBDATA().setYESFG(fiscINBK.getOriData().substring(161, 162));
	}

	public void checkIC_DATA(FISC_INBK fiscINBK) {
		fiscINBK.setICDATA(new DefIC_DATA());
		if (fiscINBK.getIcData().length() != 70) {
			logger.info("總長度不足70位");
			return;
		}
		fiscINBK.getICDATA().setAcqNo(fiscINBK.getIcData().substring(0, 8));
		fiscINBK.getICDATA().setAcqCntry(fiscINBK.getIcData().substring(8, 11));
		fiscINBK.getICDATA().setTxCur(fiscINBK.getIcData().substring(11, 14));
		fiscINBK.getICDATA().setSetExrate(fiscINBK.getIcData().substring(14, 22));
		fiscINBK.getICDATA().setTwdFee(fiscINBK.getIcData().substring(22, 29));
		fiscINBK.getICDATA().setProcFee(fiscINBK.getIcData().substring(29, 36));
		fiscINBK.getICDATA().setSetAmt(fiscINBK.getIcData().substring(36, 50));
		fiscINBK.getICDATA().setTxAmt(fiscINBK.getIcData().substring(50, 64));
		fiscINBK.getICDATA().setIcStan(fiscINBK.getIcData().substring(64, 70));
	}

	public void checkORI_DATA(FISC_INBK fiscINBK) {
		fiscINBK.setORIDATA(new DefORI_DATA());
		if (fiscINBK.getOriData().length() != 195) {
			logger.info("總長度不足195位");
			return;
		}
		fiscINBK.getORIDATA().setOriMsgtype(fiscINBK.getOriData().substring(0, 4));
		fiscINBK.getORIDATA().setOriVisaStan(fiscINBK.getOriData().substring(4, 10));
		fiscINBK.getORIDATA().setOriTxDatetime(fiscINBK.getOriData().substring(10, 20));
		fiscINBK.getORIDATA().setOriAcq(fiscINBK.getOriData().substring(20, 31));
		fiscINBK.getORIDATA().setOriFwdInst(fiscINBK.getOriData().substring(31, 42));
		fiscINBK.getORIDATA().setTxAmt(fiscINBK.getOriData().substring(42, 54));
		fiscINBK.getORIDATA().setSetAmt(fiscINBK.getOriData().substring(54, 66));
		fiscINBK.getORIDATA().setTxCur(fiscINBK.getOriData().substring(66, 69));
		fiscINBK.getORIDATA().setSetCur(fiscINBK.getOriData().substring(69, 72));
		fiscINBK.getORIDATA().setSetExrate(fiscINBK.getOriData().substring(72, 80));
		fiscINBK.getORIDATA().setSetFee(fiscINBK.getOriData().substring(80, 89));
		fiscINBK.getORIDATA().setProcFee(fiscINBK.getOriData().substring(89, 98));
		fiscINBK.getORIDATA().setLocDatetime(fiscINBK.getOriData().substring(98, 108));
		fiscINBK.getORIDATA().setSetDateMmdd(fiscINBK.getOriData().substring(108, 112));
		fiscINBK.getORIDATA().setCovDateMmdd(fiscINBK.getOriData().substring(112, 116));
		fiscINBK.getORIDATA().setBilAmt(fiscINBK.getOriData().substring(116, 128));
		fiscINBK.getORIDATA().setBillRate(fiscINBK.getOriData().substring(128, 136));
		fiscINBK.getORIDATA().setTxRpamt(fiscINBK.getOriData().substring(136, 148));
		fiscINBK.getORIDATA().setSetRpamt(fiscINBK.getOriData().substring(148, 160));
		fiscINBK.getORIDATA().setICCR(fiscINBK.getOriData().substring(160, 168));
		fiscINBK.getORIDATA().setMCCR(fiscINBK.getOriData().substring(168, 180));
		fiscINBK.getORIDATA().setBilCur(fiscINBK.getOriData().substring(180, 183));
		fiscINBK.getORIDATA().setPosMode(fiscINBK.getOriData().substring(183, 187));
		fiscINBK.getORIDATA().setAcqCntry(fiscINBK.getOriData().substring(187, 190));
	}
}
