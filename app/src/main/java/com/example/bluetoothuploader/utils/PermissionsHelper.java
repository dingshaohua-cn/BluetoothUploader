package com.example.bluetoothuploader.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;


public class PermissionsHelper {
    private static final String TAG = "dsh";
    private final static int PermissionsCode_All = 0;
    static Map<String, String> map = new HashMap<String, String>() {{
        put("android.permission.ACCESS_FINE_LOCATION", "位置");
        put("android.permission.BLUETOOTH_SCAN", "蓝牙");
    }};

    public static String getPermissionZh(String key) {
        return map.get(key);
    }


    public static void requestPermissions(Activity active){
        // 动态获取权限
        String[] permissions = {Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        ActivityCompat.requestPermissions(active, permissions, PermissionsCode_All);
    }

    /**
     * 动态权限回调事件
     *
     * @param requestCode  权限申请组id 自定义的
     * @param permissions  申请权限组内容 是个数组
     * @param grantResults 存储着权限授权结果 也是个数组
     * @deprecated grantResults和permissions一一对应， 被拒绝则为-1，被同意则为0
     */
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode) {
            case PermissionsCode_All:
                String str = "";
                for (int i = 0; i < permissions.length; i++) {
                    String permissionName = getPermissionZh(permissions[i]); // 权限名
                    int permissionResult = grantResults[i]; // 权限结果
                    String permissionStatus = permissionResult == PackageManager.PERMISSION_GRANTED ? "开启  " : "关闭  ";
                    str += permissionName + ": " + permissionStatus;
                }
//                Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
