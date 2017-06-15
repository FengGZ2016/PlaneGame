package com.example.administrator.planegeme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 作者：国富小哥
 * 日期：2017/6/15
 * Created by Administrator
 */

public class MyReceiver extends BroadcastReceiver{
    private MyListener mMyListener;

    public MyReceiver(MyListener mMyListener){
        this.mMyListener=mMyListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.example.administrator.planegeme")){
            mMyListener.showDialog();
        }

    }
}
