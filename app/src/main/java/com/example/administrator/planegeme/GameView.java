package com.example.administrator.planegeme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/19.
 */

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback,View.OnTouchListener{
    private int bitmapWidth;
    private int bitmapHeight;
    private int height=0;
    private int width=0;
    private Bitmap my;//我
    private Bitmap baozha;//爆炸
    private Bitmap bg;//背景
    private Bitmap diren;//敌人
    private Bitmap zidan;//子弹
    private List<GameImage> mGameImageList=new ArrayList<>();
    private boolean state=false;
    private SurfaceHolder mSurfaceHolder;
    private Bitmap cacheBitmap;//二级缓存的照片

    public GameView(Context context) {
        super(context);
        //注册回调方法
        getHolder().addCallback(this);
        getWindomMetrics();
        init();

    }

    private void init() {
        //加载图片
        my= BitmapFactory.decodeResource(getResources(), R.drawable.my);
        baozha= BitmapFactory.decodeResource(getResources(),R.drawable.baozha);
        bg= BitmapFactory.decodeResource(getResources(),R.drawable.bg);
        diren= BitmapFactory.decodeResource(getResources(),R.drawable.diren);
        zidan= BitmapFactory.decodeResource(getResources(),R.drawable.zidan);
        //二级缓存图片
        cacheBitmap= Bitmap.createBitmap(bitmapWidth,bitmapHeight, Bitmap.Config.ARGB_8888);
        mGameImageList.add(new BgImage(bg));
        mGameImageList.add(new FeijiImage(my));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder=holder;
        state=true;
        new Thread(this).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        state=false;
    }

    @Override
    public void run() {
        Paint cachePaint=new Paint();
        try {
            while (state){
                Canvas cacheCanvas=new Canvas(cacheBitmap);//二级缓存的画布
                for (GameImage gameImage:mGameImageList){
                    cacheCanvas.drawBitmap(gameImage.getBitmap(),gameImage.getX(),gameImage.getY(),cachePaint);
                }



                Canvas canvas= mSurfaceHolder.lockCanvas();
                canvas.drawBitmap(cacheBitmap,0,0,cachePaint);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                Thread.sleep(10);
            }
        }catch (Exception e){

        }

    }

    private void getWindomMetrics() {
        //得到屏幕的分辨率
        DisplayMetrics metrics=getResources().getDisplayMetrics();
        bitmapWidth=metrics.widthPixels;
        bitmapHeight=metrics.heightPixels;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return false;
    }

    /**
     * 负责背景照片的处理
     * */
    private class BgImage implements GameImage{
        private Bitmap bgBitmap;
        private Bitmap newBitmap=null;
        private BgImage(Bitmap bgBitmap){
            this.bgBitmap=bgBitmap;
            newBitmap=Bitmap.createBitmap(bitmapWidth,bitmapHeight, Bitmap.Config.ARGB_8888);
        }


        @Override
        public Bitmap getBitmap() {
            Paint paint=new Paint();
            Canvas canvas=new Canvas(newBitmap);

            canvas.drawBitmap(bgBitmap,new Rect(0,0,bgBitmap.getWidth(),bgBitmap.getHeight()),new Rect(0,height,bitmapWidth,bitmapHeight+height),paint);
            canvas.drawBitmap(bgBitmap,new Rect(0,0,bgBitmap.getWidth(),bgBitmap.getHeight()),new Rect(0,-bitmapHeight+height,bitmapWidth,height),paint);
            height++;
            if (height==bitmapHeight){
                height=0;
            }
            return newBitmap;
        }

        @Override
        public int getX() {
            return 0;
        }

        @Override
        public int getY() {
            return 0;
        }
    }

    private interface GameImage{
        Bitmap getBitmap();
        int getX();
        int getY();
    }

    /**
     * 飞机类
     * */
    private class FeijiImage implements GameImage{
        private Bitmap my;
        private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        private int x;
        private int y;

        private int index=0;
        private int num=0;

        public FeijiImage(Bitmap my){
            this.my=my;
            //切割飞机
            bitmaps.add(Bitmap.createBitmap(my, 0, 0, my.getWidth() / 4,
                    my.getHeight()));
            bitmaps.add(Bitmap.createBitmap(my, (my.getWidth() / 4) * 1, 0,
                    my.getWidth() / 4, my.getHeight()));
            bitmaps.add(Bitmap.createBitmap(my, (my.getWidth() / 4) * 2, 0,
                    my.getWidth() / 4, my.getHeight()));
            bitmaps.add(Bitmap.createBitmap(my, (my.getWidth() / 4) * 3, 0,
                    my.getWidth() / 4, my.getHeight()));
            //飞机的坐标
            x = (bitmapWidth - my.getWidth() / 4) / 2;
            y = bitmapHeight - my.getHeight() - 30;

        }

        public Bitmap getBitmap() {
        Bitmap bitmap=bitmaps.get(index);
            if (num==7){
                index++;
                if (index==bitmaps.size()){
                    index=0;
                }
                num=0;
            }
            num++;
            return bitmap;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getX() {
            return x;
        }


        public int getY() {
            return y;
        }



    }
}
