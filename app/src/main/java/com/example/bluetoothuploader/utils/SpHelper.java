package com.example.bluetoothuploader.utils;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class SpHelper {
    private static final String TAG = "dsh";
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
     * 获取SharedPreferences对象
     * @return
     */
    public static SharedPreferences getSharedPreferences(){
        return sp;
    }

    /**
     * 初始化本地存储
     *
     * @return
     */
    public static SharedPreferences.Editor getSpEditor(){
        return sp.edit();
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
        for(String element: props){
            if(getProp(element).equals("")){
                return true;
            }
        }
        return false;
    }

    public static Boolean equals(String prop, String val){
        return getProp(prop).equals(val);
    }
}
