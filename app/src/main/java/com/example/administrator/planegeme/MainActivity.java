package com.example.administrator.planegeme;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements MyListener{

    private GameView mGameView=null;
    private IntentFilter filter;
    private MyReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        mGameView=new GameView(this);
        setContentView(mGameView);
        initReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //注册广播接收者
    private void initReceiver() {
        filter=new IntentFilter();
        receiver=new MyReceiver(this);
        filter.addAction("com.example.administrator.planegeme");
        registerReceiver(receiver,filter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            //先暂停游戏
            mGameView.stop();
            //弹出对话框
            AlertDialog.Builder dialog=new AlertDialog.Builder(this);
            dialog.setTitle("你真的要退出游戏吗？");
            dialog.setNeutralButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            dialog.setNegativeButton("继续", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mGameView.start();
                }
            });
            dialog.create().show();
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }


    /**
     * 游戏结束的对话框
     * */
    @Override
    public void showDialog() {
            AlertDialog.Builder dialog=new AlertDialog.Builder(this);
            dialog.setTitle("游戏结束");
            dialog.setMessage("很遗憾，您输了！");
            dialog.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   mGameView.reCome();
                }
            });
            dialog.setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   finish();
                }
            });
            dialog.create().show();

    }
}
