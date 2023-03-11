package com.example.bluetoothuploader.ui.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.example.bluetoothuploader.R;
import com.example.bluetoothuploader.databinding.FragmentHomeBinding;
import com.example.bluetoothuploader.utils.NotificationHelper;
import com.example.bluetoothuploader.utils.SpHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import cn.leancloud.LCObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private static final String TAG = "dsh";

    private FragmentActivity activity;

    // 构建对象
    private LCObject todo = new LCObject("bluetooth");

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    // 下面使用Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，而新API帮我们解析成ScanRecord类\
    final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

    private Boolean scanning = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onDestroyView: " + "哈哈onCreateView");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        activity = getActivity();

        //本地存储： 获取SharedPreferences对象
        SpHelper.initInstance(activity);

        // 点击启动事件
        TextView startBtnDom = root.findViewById(R.id.startBtn);
        startBtnDom.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("MissingPermission")
            public void onClick(View view) {
                if (scanning) {
                    scanning = false;
                    startBtnDom.setText("开始");

                    startBtnDom.setBackground(getContext().getDrawable(R.drawable.shape));
                    bluetoothLeScanner.stopScan(new ScanCallback() {

                    });
                } else {
                    scanning = true;
                    startBtnDom.setText("停止");
                    startBtnDom.setBackground(getContext().getDrawable(R.drawable.shape1));
                    Boolean isServerEmpty = SpHelper.isPropsEmpty(new String[]{"appId", "appKey", "appApi"});
                    if (SpHelper.isPropEmpty("mac")) {
                        Toast.makeText(activity, "请检查蓝牙监听目标配置", Toast.LENGTH_LONG).show();
                    } else if (isServerEmpty) {
                        Toast.makeText(activity, "请检查上报服务器配置", Toast.LENGTH_LONG).show();
                    } else {
                        bluetoothLeScanner.startScan(new ScanCallback() {
                            @Override
                            public void onScanResult(int callbackType, ScanResult result) {
                                super.onScanResult(callbackType, result);
                                BluetoothDevice dev = result.getDevice(); // 获取BLE设备信息
                                String address = dev.getAddress();
                                if (!address.equals(null)) {
                                    if (SpHelper.equals("mac", address)) {
                                        saveData(dev);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
        return root;
    }




    @SuppressLint("MissingPermission")
    private void saveData(BluetoothDevice dev) {
        // 为属性赋值
        todo.put("name", dev.getName());
        todo.put("mac", dev.getAddress());

        // 将对象保存到云端
        todo.saveInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onComplete() {}
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
//    @Override
//    public void onHiddenChanged(boolean hidd) {
//        if (!hidd && getActivity() != null) {
//            System.out.println("是否执行了这个方法");
//        }
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroyView: " + "哈哈 home程序被销毁了");
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: "+"哈哈 home视图被销毁了");
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        Log.e("测试","onSaveInstanceState super之前");
//        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }
}