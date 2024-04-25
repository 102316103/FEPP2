package com.syscom.fep.notify.util;

import java.util.Set;

public class NotifyExpressionParse {

    public static void expressionParse(Set vars, String sign, String template) {
        String temp = template;
        int endPoint = temp.lastIndexOf(sign);
        int startPoint = 0;
        if (endPoint < 0) return;     // 當沒有相關的變數時
        do {
            temp = temp.substring(0, endPoint);
            startPoint = temp.lastIndexOf(sign);
            String v = template.substring(startPoint, endPoint + 2);
            vars.add(v);
            try {
                temp = template.substring(0, startPoint);
                endPoint = temp.lastIndexOf(sign);
            }catch (Exception e){
                e.printStackTrace();
            }
        } while (endPoint > 0);
    }
}
