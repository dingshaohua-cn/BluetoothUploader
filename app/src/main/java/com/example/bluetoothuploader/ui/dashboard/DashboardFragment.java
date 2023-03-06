package com.example.bluetoothuploader.ui.dashboard;

import static com.example.bluetoothuploader.ui.dashboard.Utils.getPermissionZh;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bluetoothuploader.MainActivity;
import com.example.bluetoothuploader.R;
import com.example.bluetoothuploader.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    private final int PermissionsCode_BlueScan = 0;

    private static final String TAG = "dsh";

    private List<Connection> connectionList = new ArrayList<>();

    private ConnectionAdapter adapter;

    BluetoothAdapter bluetoothAdapter;
    private TextView statusDom;

    private ImageView imageDom;
    private EditText macDom;

    private Activity activity;

    private View root;

    private RotateAnimation animation = goRotate();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        root = binding.getRoot();


        // 初始化扫描到的蓝牙列表视图
        adapter = new ConnectionAdapter(activity, R.layout.son_layout, connectionList);
        ListView listView = (ListView) root.findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        macDom = root.findViewById(R.id.macDom);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Connection connect = connectionList.get(position);
                macDom.setText(connect.getAdds());
            }

        });

        // 动态获取权限
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        ActivityCompat.requestPermissions(activity, permissions, PermissionsCode_BlueScan);

        // 蓝牙扫描状态 节点dom（未扫描、扫描中）
        statusDom = root.findViewById(R.id.status);

        // 定义和注册广播（用于接受经典蓝牙的扫描）
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//注册广播接收信号
        activity.registerReceiver(bluetoothReceiver, intentFilter);//用BroadcastReceiver 来取得结果

        // 蓝牙扫描按钮dom
        imageDom = (ImageView) root.findViewById(R.id.refresh);
        imageDom.setAnimation(animation);
        imageDom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: "+animation.hasStarted());
                if(animation.hasStarted()){
                    // 开始扫描
                    connectionList.clear();
                    doDiscover(true);
                }
//                else {
//                    doDiscover(false);
//                }
            }
        });

        return root;
    }

    private RotateAnimation goRotate() {
        RotateAnimation anm = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anm.setDuration(30000);//设置动画持续时间
        anm.setRepeatCount(Animation.INFINITE);
        anm.setInterpolator(new LinearInterpolator());
        return anm;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
                break;
        }
    }


    /**
     * 蓝牙扫描接收回调
     */
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

    // 定时器 用于监听蓝牙扫描结束状态
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                if (!bluetoothAdapter.isDiscovering()) {
                    statusDom.setText("未扫描");
                    animation.cancel();
                    handler.removeCallbacks(runnable);
                }
            }
            //要做的事情，这里再次调用此Runnable对象，以实现每1秒实现一次的定时器操作
            handler.postDelayed(this, 1000);
        }
    };


    /**
     * 蓝牙扫描操作
     *
     * @param status
     */
    private void doDiscover(Boolean status) {
        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();  // 获得蓝牙适配器对象
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "startDiscover: 经典扫描");
            statusDom.setText("扫描中...");
            if (status) {
                animation.startNow();
                bluetoothAdapter.startDiscovery(); // 如果不调用cancelDiscovery主动停止扫描的话，最多扫描12s
            } else {
                bluetoothAdapter.cancelDiscovery();
                animation.cancel();
            }
            handler.postDelayed(runnable, 2000);
        }
    }
}