package com.example.bluetoothuploader.ui.dashboard;



import static android.content.Context.MODE_PRIVATE;

import static com.example.bluetoothuploader.utils.ScreenMode.fullScreenMode;
import static com.example.bluetoothuploader.utils.ScreenMode.stopFullScreenMode;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.bluetoothuploader.R;
import com.example.bluetoothuploader.databinding.FragmentDashboardBinding;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;

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

    private SharedPreferences sp;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // 初始化扫描到的蓝牙列表视图
        adapter = new ConnectionAdapter(activity, R.layout.son_layout, connectionList);
        ListView listView = root.findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        macDom = root.findViewById(R.id.mac);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Connection connect = connectionList.get(position);
                macDom.setText(connect.getAdds());
            }

        });



        // 蓝牙扫描状态 节点dom（未扫描、扫描中）
        statusDom = root.findViewById(R.id.status);

        // 定义和注册广播（用于接受经典蓝牙的扫描）
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//注册广播接收信号
        activity.registerReceiver(bluetoothReceiver, intentFilter);//用BroadcastReceiver 来取得结果

        // 蓝牙扫描按钮dom
        imageDom = root.findViewById(R.id.refresh);
        View rightActionDom = root.findViewById(R.id.rightAction);

        rightActionDom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(statusDom.getText().equals("开始扫描")){
                    // 开始扫描
                    connectionList.clear();
                    doDiscover(true);
                } else {
                    doDiscover(false);
                }
            }
        });

        //本地存储： 获取SharedPreferences对象
        sp = activity.getSharedPreferences("SP", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        // 保存按钮点击
        View btnSaveDom = root.findViewById(R.id.btnSave);
        EditText appIdIpt = root.findViewById(R.id.appId);
        EditText appKeyIpt = root.findViewById(R.id.appKey);
        EditText appApiIpt = root.findViewById(R.id.appApi);
        EditText macIpt = root.findViewById(R.id.mac);
        EditText timerIpt = root.findViewById(R.id.timer);
        btnSaveDom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String appIdIptStr = appIdIpt.getText().toString();
                String appKeyIptStr = appKeyIpt.getText().toString();
                String appApiIptStr = appApiIpt.getText().toString();
                String timerIptStr = timerIpt.getText().toString();
                String macIptStr = macIpt.getText().toString();
                if(appIdIptStr.equals("")){
                    Toast.makeText(activity, "请输入appId", Toast.LENGTH_SHORT).show();
                } else if (appKeyIptStr.equals("")) {
                    Toast.makeText(activity, "请输入appKey", Toast.LENGTH_SHORT).show();
                } else if (appApiIptStr.equals("")) {
                    Toast.makeText(activity, "请输入appApi", Toast.LENGTH_SHORT).show();
                } else{
                    editor.putString("appId", appIdIptStr);
                    editor.putString("appKey", appKeyIptStr);
                    editor.putString("appApi", appApiIptStr);
                    // 这两个不是必填的
                    editor.putString("timer", timerIptStr);
                    editor.putString("mac", macIptStr);
                    editor.commit();
                    Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 软件重启 需要还原上次的输入
        String appId = sp.getString("appId", "");
        String appKey = sp.getString("appKey", "");
        String appApi = sp.getString("appApi", "");
        String mac = sp.getString("mac", "");
        String timer = sp.getString("timer", "");
        macIpt.setText(mac);
        appIdIpt.setText(appId);
        appKeyIpt.setText(appKey);
        appApiIpt.setText(appApi);
        timerIpt.setText(timer);




        return root;
    }

    private RotateAnimation goRotate() {
        RotateAnimation anm = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anm.setDuration(1000);//设置动画持续时间
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
                    statusDom.setText("开始扫描");
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
            if (status) {
                statusDom.setText("停止扫描");
                Log.i(TAG, "doDiscover: "+"开始动画");
                imageDom.startAnimation(animation);
                bluetoothAdapter.startDiscovery(); // 如果不调用cancelDiscovery主动停止扫描的话，最多扫描12s
            } else {
                statusDom.setText("开始扫描");
                bluetoothAdapter.cancelDiscovery();
                animation.cancel();
            }
            handler.postDelayed(runnable, 2000);
        }
    }
}