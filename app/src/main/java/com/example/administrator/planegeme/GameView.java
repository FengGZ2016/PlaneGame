package com.example.administrator.planegeme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private ArrayList<GameImage> mGameImageList=new ArrayList<>();
    private SurfaceHolder mSurfaceHolder;
    private Bitmap cacheBitmap;//二级缓存的照片
    private boolean state=false;

    private FeijiImage selectFeiji;//选中的飞机
    private ArrayList<GameImage> zidans = new ArrayList<>();


    public GameView(Context context) {
        super(context);
        //注册回调方法
        getHolder().addCallback(this);
        //事件注册
        this.setOnTouchListener(this);
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
        mGameImageList.add(new DijiImage(diren,baozha));
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
        int direnNum=0;
        int zidanNum=0;
        try {
            while (state){
                if (selectFeiji!=null){
                    if (zidanNum==5){
                        zidans.add(new Zidan(selectFeiji,zidan));
                        zidanNum=0;
                    }
                   zidanNum++;
                }

                Canvas cacheCanvas=new Canvas(cacheBitmap);//二级缓存的画布
                //克隆一份集合
                for (GameImage gameImage:(List<GameImage>)mGameImageList.clone()){
                    if (gameImage instanceof DijiImage){
                        //当绘画敌机时，调用一次收到攻击的方法
                        ((DijiImage) gameImage).shouDaoGongJi(zidans);
                    }
                    cacheCanvas.drawBitmap(gameImage.getBitmap(),gameImage.getX(),gameImage.getY(),cachePaint);
                }

                //画子弹
                //子弹也要克隆一份
               for (GameImage zidanImage: (List<GameImage>)zidans.clone()){
                   cacheCanvas.drawBitmap(zidanImage.getBitmap(),zidanImage.getX(),zidanImage.getY(),cachePaint);
               }

                if (direnNum==100){
                    direnNum=0;
                    //增加一台敌机
                    mGameImageList.add(new DijiImage(diren,baozha));
                }

                direnNum++;

                Canvas canvas= mSurfaceHolder.lockCanvas();
                canvas.drawBitmap(cacheBitmap,0,0,cachePaint);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                Thread.sleep(5);
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
             //按下
            if (event.getAction()==MotionEvent.ACTION_DOWN){
                //找到飞机照片
                for (GameImage image:mGameImageList){
                    if (image instanceof FeijiImage){
                        //判断是否选中了飞机
                        FeijiImage feiji = (FeijiImage) image;
                        if (feiji.getX() < event.getX()
                                && feiji.getY() < event.getY()
                                && feiji.getX() + feiji.getWidth() > event.getX()
                                && feiji.getY() + feiji.getHeight() > event.getY()) {
                            //选中了

                           selectFeiji = feiji;
                        } else {
                            //没有选中

                            selectFeiji = null;
                        }
                        break;
                    }
                }
                //移动
            }else if (event.getAction()==MotionEvent.ACTION_MOVE){
                    if (selectFeiji!=null){
                        //随着移动更新坐标
                        selectFeiji.setX((int) event.getX() - selectFeiji.getWidth()
                                / 2);
                        selectFeiji.setY((int) event.getY() - selectFeiji.getHeight()
                                / 2);
                    }
                //松开
            }else if (event.getAction()==MotionEvent.ACTION_UP){
                selectFeiji = null;
            }
        return true;
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
        private int width;//战机的宽
        private int height;//战机的高

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

            //得到战机的宽和高
            width=my.getWidth()/4;
            height=my.getHeight();

        }


        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
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


    /**
     * 敌机类
     * */
    private class DijiImage implements GameImage{

        private Bitmap diren=null;
        private int x;
        private int y;
        private List<Bitmap> bitmaps=new ArrayList<>();
        private int index=0;
        private int num=0;

        private int width;
        private int height;

        private List<Bitmap> baozhas=new ArrayList<>();

        public DijiImage(Bitmap diren,Bitmap baozha){
            this.diren=diren;
            //分解敌机的照片
            bitmaps.add(Bitmap.createBitmap(diren,0,0,diren.getWidth()/4,diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren,(diren.getWidth())/4*1,0,diren.getWidth()/4,diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren,(diren.getWidth())/4*2,0,diren.getWidth()/4,diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren,(diren.getWidth())/4*3,0,diren.getWidth()/4,diren.getHeight()));
            //分解爆炸的照片
            baozhas.add(Bitmap.createBitmap(baozha, 0, 0,
                    baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha,
                    (baozha.getWidth() / 4) * 1, 0, baozha.getWidth() / 4,
                    baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha,
                    (baozha.getWidth() / 4) * 2, 0, baozha.getWidth() / 4,
                    baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha,
                    (baozha.getWidth() / 4) * 3, 0, baozha.getWidth() / 4,
                    baozha.getHeight() / 2));

            baozhas.add(Bitmap.createBitmap(baozha, 0, baozha.getHeight() / 2,
                    baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha,
                    (baozha.getWidth() / 4) * 1, baozha.getHeight() / 2,
                    baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha,
                    (baozha.getWidth() / 4) * 2, baozha.getHeight() / 2,
                    baozha.getWidth() / 4, baozha.getHeight() / 2));
            baozhas.add(Bitmap.createBitmap(baozha,
                    (baozha.getWidth() / 4) * 3, baozha.getHeight() / 2,
                    baozha.getWidth() / 4, baozha.getHeight() / 2));
            //
            width=diren.getWidth()/4;
            height=diren.getHeight();

            y=-diren.getHeight();
            //用随机数来定义敌机的坐标
            Random ran=new Random();
            x=ran.nextInt(bitmapWidth-(diren.getWidth()/4));
        }


        public Bitmap getBitmap() {
            Bitmap bitmap=bitmaps.get(index);
            if (num==8){
                index++;
                if (index==baozhas.size()&&isKill){
                    //敌机爆炸后去掉爆炸的痕迹
                    mGameImageList.remove(this);
                }
                if (index==bitmaps.size()){
                    index=0;
                }
                num=0;
            }
            y+=5;
            num++;
            if (y>bitmapHeight){
                //敌机越界，清除敌机
                mGameImageList.remove(this);
            }
            return bitmap;
        }

        private boolean isKill=false;//敌机是否被杀掉
        /**
         * 敌机收到攻击
         * */
     public void shouDaoGongJi(ArrayList<GameImage> zidans){

         if (!isKill){
             for (GameImage zidan:zidans){
                 //判断是否被子弹打中
                 if (zidan.getX()>x&&zidan.getY()>y&&zidan.getX()<x+width&&zidan.getY()<y+height){
                     //敌机被击中
                     Log.d("shouDaoGongJi","击中了！！！！！");
                     //移除该子弹
                     zidans.remove(zidan);
                     isKill=true;
                     //敌机的照片变成爆炸的照片
                     bitmaps=baozhas;
                     break;
                 }
             }
         }

        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }

    /**
     * 子弹类
     * */
    private class Zidan implements GameImage{
        private FeijiImage feiji;
        private Bitmap zidanBitmap;
        private int x;
        private int y;

        public Zidan(FeijiImage feiji,Bitmap zidanBitmap){
            this.feiji=feiji;
            this.zidanBitmap=zidanBitmap;

            x = (feiji.getX() + feiji.getWidth() / 2) - 8;
            y = feiji.getY() - zidan.getHeight();
        }

        @Override
        public Bitmap getBitmap() {
            y -= 50;
           if (y <= -10) {
                zidans.remove(this);
           }
            return zidanBitmap;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }
    }
}
