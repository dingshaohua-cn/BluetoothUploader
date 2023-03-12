package com.example.bluetoothuploader;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.leancloud.LeanCloud;

import com.example.bluetoothuploader.utils.AnimationHelper;
import com.example.bluetoothuploader.utils.Connection;
import com.example.bluetoothuploader.utils.ConnectionAdapter;
import com.example.bluetoothuploader.utils.NotificationHelper;
import com.example.bluetoothuploader.utils.PermissionsHelper;
import com.example.bluetoothuploader.utils.SpHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.leancloud.LCObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "dsh";
    private Activity activity;
    // 表单元素的Dom节点
    EditText appIdIpt;
    EditText appKeyIpt;
    EditText appApiIpt;
    EditText macIpt;
    // 蓝牙对象（下面使用Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，而新API帮我们解析成ScanRecord类）
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

    // list视图适配器
    private ConnectionAdapter connectionAdapter;
    private List<Connection> connectionList = new ArrayList<>();

    private TextView btnSaveDom;

    // 构建leanCloud对象
    private LCObject leanCloud = new LCObject("bluetooth");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionsHelper.requestPermissions(this);
        activity = this;
        // 初始化表单元素的Dom节点
        appIdIpt = this.findViewById(R.id.appId);
        appKeyIpt = this.findViewById(R.id.appKey);
        appApiIpt = this.findViewById(R.id.appApi);
        macIpt = this.findViewById(R.id.mac);

        // 初始化本地存储
        SpHelper.initInstance(this);
        // 蓝牙扫描按钮事件
        View rightActionDom = this.findViewById(R.id.rightAction);
        rightActionDom.setOnClickListener(rightActionDomListener);
        // 启动按钮点击事件
        btnSaveDom = this.findViewById(R.id.btnSave);
        btnSaveDom.setOnClickListener(startBtnListener);

        // 一些基本初始化
        init();
    }

    /**
     * 一些基本初始化
     */

    protected void init() {
        // 软件重启 需要还原上次的输入回填
        String mac = SpHelper.getProp("mac");
        String appId = SpHelper.getProp("appId");
        String appKey = SpHelper.getProp("appKey");
        String appApi = SpHelper.getProp("appApi");
        macIpt.setText(mac);
        if(appId.equals("")){
            appIdIpt.setText("0XxTqBqbDxa7F3tXQvLJGKsR-gzGzoHsz");
        }else{
            appIdIpt.setText(appId);
        }
        if(appKey.equals("")){
            appKeyIpt.setText("96avIapLREM9OBqhttD72a3g");
        }else{
            appKeyIpt.setText(appKey);
        }
        if(appApi.equals("")){
            appApiIpt.setText("https://0xxtqbqb.lc-cn-n1-shared.com");
        }else{
            appApiIpt.setText(appApi);
        }



        // 初始化扫描到的蓝牙列表视图
        connectionAdapter = new ConnectionAdapter(activity, R.layout.son_layout, connectionList);
        ListView listView = activity.findViewById(R.id.list_view);
        listView.setAdapter(connectionAdapter);
        listView.setOnItemClickListener(listListener);

    }

    /**
     * 列表点击事件
     */
    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Connection connect = connectionList.get(position);
            EditText macDom = activity.findViewById(R.id.mac);
            macDom.setText(connect.getAdds());
        }
    };

    /**
     * 蓝牙扫描按钮事件
     */
    View.OnClickListener rightActionDomListener = new View.OnClickListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View view) {
            // 蓝牙扫描状态 节点dom（未扫描、扫描中）
            TextView statusDom = activity.findViewById(R.id.status);
            if (statusDom.getText().equals("开始扫描")) {
                // 开始扫描
                statusDom.setText("停止扫描");
                ImageView imageDom = activity.findViewById(R.id.refresh);
                imageDom.startAnimation(AnimationHelper.getRefreshAnm());
                bluetoothLeScanner.startScan(scanCallback);
            } else {
                statusDom.setText("开始扫描");
                AnimationHelper.stopRefreshAnm();
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }
    };

    /**
     * 启动按钮点击事件
     */
    View.OnClickListener startBtnListener = new View.OnClickListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View view) {

            if (btnSaveDom.getText().equals("停止")) {
                btnSaveDom.setText("开始");
                bluetoothLeScanner.stopScan(scan1Callback);
                return;
            }

            String macIptStr = macIpt.getText().toString();
            String appIdIptStr = appIdIpt.getText().toString();
            String appKeyIptStr = appKeyIpt.getText().toString();
            String appApiIptStr = appApiIpt.getText().toString();

            // 存储到本地存储中
            SharedPreferences.Editor editor = SpHelper.getSpEditor();
            editor.putString("mac", macIptStr);
            editor.putString("appId", appIdIptStr);
            editor.putString("appKey", appKeyIptStr);
            editor.putString("appApi", appApiIptStr);
            editor.commit();

            // 启动监听
            Boolean isServerEmpty = SpHelper.isPropsEmpty(new String[]{"appId", "appKey", "appApi"});
            Log.i(TAG, "onClick: " + isServerEmpty);
            if (SpHelper.isPropEmpty("mac")) {
                Toast.makeText(activity, "请检查蓝牙监听目标配置", Toast.LENGTH_SHORT).show();
            } else if (isServerEmpty) {
                Toast.makeText(activity, "请检查上报服务器配置", Toast.LENGTH_SHORT).show();
            } else {
                btnSaveDom.setText("停止");
                // 初始化LeanCloud
                LeanCloud.initialize(activity, appIdIptStr, appKeyIptStr, appApiIptStr);
                bluetoothLeScanner.startScan(scan1Callback);
            }
        }
    };

    /**
     * 动态权限回调事件
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult: ");
        PermissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 配置蓝牙mac：蓝牙扫描回调事件
     */
    ScanCallback scanCallback = new ScanCallback() {
        @Override
        @SuppressLint("MissingPermission")
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice(); // 获取BLE设备信息
            Connection connection = new Connection(device.getName() == null ? "匿名" : device.getName(), device.getAddress());

            Log.i(TAG, "onScanResult: "+"哈哈哈");

            // 判断是否存在 存在就不再录入并更新
            Boolean have = false;
            for (Connection item : connectionList) {
                if (item.getAdds().equals(device.getAddress())) {
                    have = true;
                }
            }
            if (!have) {
                connectionList.add(connection);
                connectionAdapter.notifyDataSetChanged(); // 异步更新数据
            }
        }
    };

    /**
     * 点击开始：蓝牙扫描回调事件
     */
    ScanCallback scan1Callback = new ScanCallback() {
        @Override
        @SuppressLint("MissingPermission")
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice(); // 获取BLE设备信息

            if (SpHelper.equals("mac", device.getAddress())) {
                saveData(device);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void saveData(BluetoothDevice dev) {
        Log.i(TAG, "saveData11111: ");
        // 为属性赋值
        leanCloud.put("name", dev.getName());
        leanCloud.put("mac", dev.getAddress());

        // 将对象保存到云端
        leanCloud.saveInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
            }

            public void onComplete() {
            }

            public void onNext(LCObject todo) {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss"); //设置时间格式
                String createDate = formatter.format(new Date());   //格式转换
                // 成功保存之后，执行其他逻辑
                Log.i(TAG, "保存成功。objectId：" + todo.getObjectId());
                NotificationHelper.addNotification(activity, 0, "上报数据", createDate);
            }

            public void onError(Throwable throwable) {
                // 异常处理
                Log.i(TAG, "保存失败。objectId：" + throwable.getMessage());
            }
        });
    }
}