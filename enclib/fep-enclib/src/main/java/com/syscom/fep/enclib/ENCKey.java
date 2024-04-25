package com.syscom.fep.enclib;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.enc.ext.mapper.EnckeyExtMapper;
import com.syscom.fep.mybatis.enc.model.Enckey;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.vo.ENCLogData;

public class ENCKey {
	private static final String ProgramName = StringUtils.join(ENCKey.class.getSimpleName(), ".");
	private ENCLogData logData;
	// KeyIdentity長度一把KEY應為Key_qty(n1)+Key_type(x2)+Key_id_1(x20)=23,
	// 2把KEY應為Key_qty(n1)+Key_type1(x2)+Key_id_1(x20)+Key_type2(x2)+Key_id_1(x20)=45,
	// 3把KEY應為Key_qty(n1)+Key_type1(x2)+Key_id_1(x20)+Key_type2(x2)+Key_id_2(x20)+Key_type3(x2)+Key_id_3(x20)=67,
	public static final int SingleKeyLength = 23;
	public static final int DoubleKeyLength = 45;
	public static final int TribleKeyLength = 67;

	private ENCKeyData[] keys;
	private int keyQty;

	private EnckeyExtMapper enckeyMapper = SpringBeanFactoryUtil.getBean(EnckeyExtMapper.class);

	public ENCKey(String keyIdentity, ENCLogData log) {
		this.logData = log;
		this.keyQty = Integer.parseInt(keyIdentity.substring(0, 1));
		this.keys = new ENCKeyData[this.keyQty];
		int offset = 0;
		for (int i = 0; i < this.keyQty; i++) {
			this.keys[i] = new ENCKeyData();
			if (i > 0) { // 第2把KEY的KEYTYPE如果為空白則等於第一把KEY的Keytype
				this.keys[i].KeyType = keyIdentity.substring(offset + 1, offset + 1 + 2).trim();
				if (StringUtils.isBlank(this.keys[i].KeyType)) {
					this.keys[i].KeyType = this.keys[0].KeyType;
				}
			} else {
				this.keys[i].KeyType = keyIdentity.substring(offset + 1, offset + 1 + 2).trim();
			}
			this.keys[i].KeyKind = keyIdentity.substring(offset + 3, offset + 3 + 4).trim();
			this.keys[i].KeyFunction = keyIdentity.substring(offset + 7, offset + 7 + 6).trim();
			this.keys[i].KeySubCode = keyIdentity.substring(offset + 13, offset + 13 + 8).trim();
			this.keys[i].KeyVersion = StringUtils.leftPad(keyIdentity.substring(offset + 21, offset + 21 + 2).trim(), 2,
					'0');
			switch (this.keys[i].KeyType) {
				case "S1":
					this.keys[i].KeyLength = 16;
					break;
				case "T2":
					this.keys[i].KeyLength = 32;
					break;
				case "T3":
					this.keys[i].KeyLength = 48;
					break;
			}
			offset += 22;
		}
	}

	/**
	 * 傳入索引值讀取KeyIdentity的第N把KEY
	 * 
	 * @param keyIndex 索引值,第一把為0,第二把為1...
	 * @param key
	 * @return
	 */
	public ENCRC getKey(int keyIndex, RefString key) {
		this.logData.setProgramName(StringUtils.join(ProgramName, "getKey"));
		ENCRC rc = ENCRC.ENCLibError;
		key.set(StringUtils.EMPTY);
		try {
			Enckey enckey = enckeyMapper.selectByPrimaryKey(this.keys[keyIndex].KeySubCode, this.keys[keyIndex].KeyType,
					this.keys[keyIndex].KeyKind, this.keys[keyIndex].KeyFunction);
			if (enckey != null) {
				if ("IMS".equals(this.keys[keyIndex].KeyFunction) &&
						"00".equals(this.keys[keyIndex].KeyVersion)) {
					// 改用pending key for ChangeKey第4道
					rc = ENCRC.Normal;
					key.set(enckey.getPendingkey());
				} else if ("01".equals(this.keys[keyIndex].KeyVersion)) {

					// 2013-10-03 Modify by Ruling for ENCLib整合FEP和MRM
					if ("CDK".equals(this.keys[keyIndex].KeyKind) || "TMK".equals(this.keys[keyIndex].KeyKind) ||
							"ICBK".equals(this.keys[keyIndex].KeyFunction)) {
						String beginDate = enckey.getBegindate();
						if (StringUtils.isNotBlank(beginDate) && FormatUtil
								.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)
								.compareTo(beginDate) >= 0) {
							ENCRC rc2 = this.updateKey(keyIndex, StringUtils.EMPTY, UpdateType.PendingToCurrent);
							if (rc2 == ENCRC.Normal) {
								rc = ENCRC.Normal;
								key.set(enckey.getPendingkey());
							} else {
								rc = rc2;
							}
						} else {
							rc = ENCRC.Normal;
							key.set(enckey.getCurkey());
						}
					} else {
						rc = ENCRC.Normal;
						key.set(enckey.getCurkey());
					}
				} else {
					rc = ENCRC.Normal;
					key.set(enckey.getPendingkey());
				}
			} else {
				rc = "RM".equals(this.keys[keyIndex].KeyFunction.substring(0, 2)) ? ENCRC.RMKeyFileReadError
						: ENCRC.ATMKeyFileReadError;
			}
		} catch (Exception e) {
			rc = ENCRC.ENCKeyIoError;
			this.logData.setRemark(e.getMessage());
			this.logData.setProgramException(e);
			ENCSendEMS.sendEMS(this.logData);
		}
		return rc;
	}

	/**
	 * 讀取KeyIdentity的第一把KEY
	 * 
	 * @param key
	 * @return
	 */
	public ENCRC getKey(RefString key) {
		return this.getKey(0, key);
	}

	/**
	 * 更新KeyIdentity的第N把KEY
	 * 
	 * @param keyIndex   索引值,第一把為0,第二把為1...
	 * @param newKey
	 * @param updateType
	 * @return
	 */
	public ENCRC updateKey(int keyIndex, String newKey, UpdateType updateType) {
		this.logData.setProgramName(StringUtils.join(ProgramName, "updateKey"));
		ENCRC rc = ENCRC.ENCLibError;
		try {
			int ret = enckeyMapper.updateKey(this.keys[keyIndex].KeySubCode, this.keys[keyIndex].KeyType,
					this.keys[keyIndex].KeyKind, this.keys[keyIndex].KeyFunction, updateType.getValue(), newKey);
			rc = ret > 0 ? ENCRC.Normal : ENCRC.UpdateKeyFileError;
		} catch (Exception e) {
			rc = ENCRC.ENCKeyIoError;
			this.logData.setRemark(e.getMessage());
			this.logData.setProgramException(e);
			ENCSendEMS.sendEMS(this.logData);
		}
		return rc;
	}

	public ENCRC updateOrInsertKey(String newKey) {
		this.logData.setProgramName(StringUtils.join(ProgramName, "updateOrInsertKey"));
		ENCRC rc = ENCRC.ENCLibError;
		int keyIndex = 0;
		try {
			int ret = enckeyMapper.updateKey(this.keys[keyIndex].KeySubCode, this.keys[keyIndex].KeyType,
					this.keys[keyIndex].KeyKind, this.keys[keyIndex].KeyFunction, UpdateType.Current.getValue(),
					newKey);
			if (ret <= 0) {
				Enckey data = new Enckey();
				data.setBankid(this.keys[keyIndex].KeySubCode);
				data.setKeytype(this.keys[keyIndex].KeyType);
				data.setKeykind(this.keys[keyIndex].KeyKind);
				data.setKeyfn(this.keys[keyIndex].KeyFunction);
				data.setCurkey(newKey);
				ret = enckeyMapper.insertSelective(data);
			}
			rc = ret > 0 ? ENCRC.Normal : ENCRC.UpdateKeyFileError;
		} catch (Exception e) {
			rc = ENCRC.ENCKeyIoError;
			this.logData.setRemark(e.getMessage());
			this.logData.setProgramException(e);
			ENCSendEMS.sendEMS(this.logData);
		}
		return rc;
	}

	/**
	 * 更新KeyIdentity的第一把KEY
	 * 
	 * @param newKey
	 * @param updateType
	 * @return
	 */
	public ENCRC updateKey(String newKey, UpdateType updateType) {
		return updateKey(0, newKey, updateType);
	}

	public ENCKeyData[] getKeys() {
		return keys;
	}

	public void setKeys(ENCKeyData[] keys) {
		this.keys = keys;
	}

	public int getKeyQty() {
		return keyQty;
	}

	public void setKeyQty(int keyQty) {
		this.keyQty = keyQty;
	}

	public class ENCKeyData {
		public String KeyType;
		public String KeyKind;
		public String KeyFunction;
		public String KeySubCode;
		public String KeyVersion;
		public int KeyLength;
	}

	public enum UpdateType {
		Pending(0),
		Current(1),
		Old(2),
		PendingToCurrent(3);

		private int value;

		private UpdateType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static UpdateType fromValue(int value) {
			for (UpdateType e : values()) {
				if (e.getValue() == value) {
					return e;
				}
			}
			throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
		}

		public static UpdateType parse(Object nameOrValue) {
			if (nameOrValue instanceof Number) {
				return fromValue(((Number) nameOrValue).intValue());
			} else if (nameOrValue instanceof String) {
				String nameOrValueStr = (String) nameOrValue;
				if (StringUtils.isNumeric(nameOrValueStr)) {
					return fromValue(Integer.parseInt(nameOrValueStr));
				}
				for (UpdateType e : values()) {
					if (e.name().equalsIgnoreCase(nameOrValueStr)) {
						return e;
					}
				}
			}
			throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
		}
	}
}
