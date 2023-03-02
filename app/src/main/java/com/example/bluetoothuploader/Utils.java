package com.example.bluetoothuploader;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    static Map<String, String> map = new HashMap<String, String>() {{
        put("android.permission.ACCESS_FINE_LOCATION", "位置");
        put("android.permission.BLUETOOTH_SCAN", "蓝牙");
    }};

    static String getPermissionZh(String key) {
        return map.get(key);
    }
}