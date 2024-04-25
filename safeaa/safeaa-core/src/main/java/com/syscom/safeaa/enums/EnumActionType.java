package com.syscom.safeaa.enums;

import java.util.Arrays;

public enum EnumActionType {

	Add(0), // 新增
	Modify(1), // 修改
	Delete(2), // 刪除
	Query(3), // 查詢
	Confirm(4), // 確認
	Execute(5), // 執行
	Cancel(6), // 取消
	Clear(7), // 清除
	Save(8), // 儲存
	Copy(9), // 複製
	Quit(10), // 離開
	Close(11), // 結束
	Print(12), // 列印
	PrintAll(13), // 全部列印
	SelectOne(14), // 選取
	SelectAll(15), // 全選
	UNSelectOne(16), // 取消選擇
	UNSelectAll(17), // 取消全選
	Navigation(18); // 包含回上頁,上一頁,下一頁...
	
    private int value;

    private EnumActionType(int value) {
        this.value = value;
    }	
    
    public int getValue() {
        return value;
    }

    public static EnumActionType fromValue(final int value) {
        for (EnumActionType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }   
    
    public static EnumActionType getEnumActionByValue(final int value) {
    	return Arrays.stream(EnumActionType.values())
    			.filter(e->e.getValue()==value).findFirst()
    			.orElseThrow(()-> new IllegalArgumentException(String.format("Invalid value = [%s]!!!", value)));
    }
}
