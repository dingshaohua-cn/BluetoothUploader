package com.example.bluetoothuploader.ui.home;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bluetoothuploader.R;
import com.example.bluetoothuploader.databinding.FragmentHomeBinding;

import cn.leancloud.LCObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;


    private static int NOTIFICATION_ID = 1;

    private FragmentActivity activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = getActivity();


        View startBtnDom = root.findViewById(R.id.startBtn);
        startBtnDom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNotification();
            }
        });

        return root;
    }


    private void saveData() {
        // 构建对象
        LCObject todo = new LCObject("Todo");

        // 为属性赋值
        todo.put("title", "马拉松报名");
        todo.put("priority", 2);

        // 将对象保存到云端
        todo.saveInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
            }

            public void onNext(LCObject todo) {
                // 成功保存之后，执行其他逻辑
                Log.i("aaaaa", "保存成功。objectId：" + todo.getObjectId());
            }

            public void onError(Throwable throwable) {
                // 异常处理
            }

            public void onComplete() {
            }
        });
    }

    private void addNotification() {
        //1、NotificationManager
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        /** 2、Builder->Notification
         *  必要属性有三项
         *  小图标，通过 setSmallIcon() 方法设置
         *  标题，通过 setContentTitle() 方法设置
         *  内容，通过 setContentText() 方法设置*/
        Notification.Builder builder = new Notification.Builder(activity);
        builder.setContentText("通知内容")//设置通知内容
                .setContentTitle("通知标题")//设置通知标题
                .setSmallIcon(R.drawable.notification);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {  // 这里的判断是为了兼容更高版本的API (O // 26)
            NotificationChannel channel = new NotificationChannel("001", "my_channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            manager.createNotificationChannel(channel);
            builder.setChannelId("001");
        }

        Notification n = builder.build();
        //3、manager.notify()
        manager.notify(NOTIFICATION_ID, n);


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}