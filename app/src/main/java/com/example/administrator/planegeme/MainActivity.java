package com.example.administrator.planegeme;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private GameView mGameView=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        mGameView=new GameView(this);
        setContentView(mGameView);
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
}
