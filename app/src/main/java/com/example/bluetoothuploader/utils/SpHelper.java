package com.example.bluetoothuploader.utils;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

public class SpHelper {

    private static SharedPreferences sp;

    /**
     * 初始化本地存储
     * @param activity
     * @return
     */
    public static SharedPreferences initInstance(Activity activity){
        sp = activity.getSharedPreferences("SP", MODE_PRIVATE);
        return sp;
    }

    /**
     * 根据属性获取值
     * @param prop
     * @return
     */
    public static String getProp(String prop){
        return sp.getString(prop, "");
    }

    /**
     * 判断属性是否为空
     * @param prop
     * @return
     */
    public static Boolean isPropEmpty(String prop){
        return getProp(prop).equals("");
    }

    /**
     * 判断属性是否都为空
     * @param props
     * @return
     */
    public static Boolean isPropsEmpty(String[] props){
        String str = "";
        for(String element: props){
            str+=element;
        }
        return str.equals("");
    }

    public static Boolean equals(String prop, String val){
        return getProp(prop).equals(val);
    }




}
