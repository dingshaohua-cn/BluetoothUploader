package com.example.bluetoothuploader;


import static com.example.bluetoothuploader.utils.DicHelper.getPermissionZh;
import static com.example.bluetoothuploader.utils.ScreenMode.fullScreenMode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.bluetoothuploader.databinding.ActivityMainBinding;

import cn.leancloud.LeanCloud;

public class MainActivity extends AppCompatActivity {
    private final int PermissionsCode_BlueScan = 0;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();  //隐藏nav的标题栏（导航栏模板自带的）
        fullScreenMode(this); // 隐藏状态栏

        // 动态获取权限
        String[] permissions = {Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        ActivityCompat.requestPermissions(this, permissions, PermissionsCode_BlueScan);


        // LeanCloud：提供 this、App ID、绑定的自定义 API 域名作为参数
        // LeanCloud.initializeSecurely(this, "0XxTqBqbDxa7F3tXQvLJGKsR-gzGzoHsz", "https://0xxtqbqb.lc-cn-n1-shared.com");
        LeanCloud.initialize(this, "0XxTqBqbDxa7F3tXQvLJGKsR-gzGzoHsz","96avIapLREM9OBqhttD72a3g", "https://0xxtqbqb.lc-cn-n1-shared.com");




        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_home)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }


    /**
     * 动态权限回调事件
     *
     * @param requestCode  权限申请组id 自定义的
     * @param permissions  申请权限组内容 是个数组
     * @param grantResults 存储着权限授权结果 也是个数组
     * @deprecated grantResults和permissions一一对应， 被拒绝则为-1，被同意则为0
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionsCode_BlueScan:
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