package com.example.bluetoothuploader;

import static com.example.bluetoothuploader.Utils.*;
import android.Manifest;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int PermissionsCode_BlueScan = 0;

    private static final String TAG = "dsh";

    private List<Connection> connectionList = new ArrayList<>();

    private ConnectionAdapter adapter;


    private TextView statusDom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化列表视图
        adapter = new ConnectionAdapter(MainActivity.this, R.layout.son_layout, connectionList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        // 动态获取权限
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        ActivityCompat.requestPermissions(MainActivity.this, permissions, PermissionsCode_BlueScan);


        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 开始扫描
                connectionList.clear();
                startDiscover();

                // 定义广播和处理广播消息
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//注册广播接收信号
                registerReceiver(bluetoothReceiver, intentFilter);//用BroadcastReceiver 来取得结果
            }
        });

        statusDom = findViewById(R.id.status);
    }

    /**
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
                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
                break;
        }
    }




    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Connection connection = new Connection(device.getName() == null ? "匿名" : device.getName(), device.getAddress());
                    connectionList.add(connection);
                    // 异步更新数据
                    adapter.notifyDataSetChanged();

                }
            }
        }
    };


    private void startDiscover() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "startDiscover: 经典扫描");
            statusDom.setText("扫描中...");
            bluetoothAdapter.startDiscovery();
        }

    }

}