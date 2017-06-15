package com.example.administrator.planegeme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
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
    private int jifen=0;//积分
    private int guanqia=1;//关卡
    private int next=50;//下一关分数

    //区分等级的二维数{等级，分数，速度，步伐}
    private int[][] erweiArray = { { 1, 50, 30, 6 }, { 2, 100, 30, 7},
            { 3, 200, 30, 8}, { 4, 300, 25, 9 }, { 5, 400, 25, 10 },
            { 6, 500, 25, 11 }, { 7, 600, 20, 12 }, { 8, 700, 20, 13},
            { 9, 800, 15, 14 }, { 10, 900, 10, 15 }, { 11, 1000, 10, 16},
            { 12, 1100, 10, 17 } };
    private int chudishu = 30; // 出敌机的速度
    private int dijiyidong = 5; // 敌机移动的速度

    private boolean stopState = false;
    private Thread mThread=null;

    //音效
    private SoundPool pool = null;
    private int sound_bomb = 0;
    private int sound_gameover = 0;
    private int sound_shot = 0;

    private boolean isOver=false;//敌机是否被杀掉

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
        mGameImageList.add(new DijiImage(diren));

        // 加载声音
        //SoundPool：声音池（同时播放数，声音类型，声音转换质量）

        pool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 0);

        sound_bomb = pool.load(getContext(), R.raw.bomb, 1);
        sound_gameover = pool.load(getContext(), R.raw.gameover, 1);
        sound_shot = pool.load(getContext(), R.raw.shot, 1);

    }

    /**
     * 音效类
     * */
    private class SoundPlay extends Thread {
        int i = 0;

        public SoundPlay(int i) {
            this.i = i;

        }

        //pool.play(资源id，左声道，右声道，优先级，是否循环，速率)
        public void run() {
            pool.play(i, 1, 1, 1, 0, 1);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder=holder;
        state=true;
        mThread=new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        state=false;
    }



    public void stop() {
        stopState = true;
    }

    public void start() {
        stopState = false;
        mThread.interrupt();// 起来
    }

    public void reCome(){
        invalidate();
        state=true;
        stopState = false;
        mThread.interrupt();// 起来

    }

    @Override
    public void run() {
        Paint cachePaint=new Paint();
        Paint paint=new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(50);
        paint.setDither(true);
        paint.setAntiAlias(true);
        int direnNum=0;
        int zidanNum=0;
        try {
            while (state){
               while (stopState){
                   try {
                       Thread.sleep(1000000);
                   }catch (Exception e){

                   }

               }


                if (selectFeiji!=null){
                    if (zidanNum==5){
                        new SoundPlay(sound_shot).start();
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
                    if (gameImage instanceof FeijiImage){
                        //当绘画战机时，调用一次战机被碰撞的方法
                        ((FeijiImage) gameImage).shouDaoPengZhuang(mGameImageList);
                    }
                    cacheCanvas.drawBitmap(gameImage.getBitmap(),gameImage.getX(),gameImage.getY(),cachePaint);
                }

                //画子弹
                //子弹也要克隆一份
               for (GameImage zidanImage: (List<GameImage>)zidans.clone()){
                   cacheCanvas.drawBitmap(zidanImage.getBitmap(),zidanImage.getX(),zidanImage.getY(),cachePaint);
               }
                cacheCanvas.drawText("分:" + jifen, 0, 50, paint);
                cacheCanvas.drawText("关:" + guanqia, 0, 100, paint);
                cacheCanvas.drawText("下:" + next, 0, 150, paint);

                if (erweiArray[guanqia - 1][1] <= jifen) {
                        //达到一定积分时，通关
                    chudishu = erweiArray[guanqia][2];
                    dijiyidong = erweiArray[guanqia][3];
                    jifen = erweiArray[guanqia - 1][1] - jifen;
                    next = erweiArray[guanqia][1];
                    guanqia = erweiArray[guanqia][0];

                }

                if (direnNum==chudishu){
                    direnNum=0;
                    //增加一台敌机
                    mGameImageList.add(new DijiImage(diren));
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
        private List<Bitmap> baozhas=getBaozhas();

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

        /**
         * 战机受到碰撞
         * */
        public void shouDaoPengZhuang(List<GameImage> gameImages){
            if (!isOver){
                for (GameImage gameImage:gameImages){
                    if (gameImage instanceof DijiImage){
                        //判断战机是否被敌机碰撞
                        if (gameImage.getX()>x&&gameImage.getY()>y&&gameImage.getX()<x+width&&gameImage.getY()<y+height){
                            Log.d("shouDaoPengZhuang","战机被碰撞了！！");
                           bitmaps=baozhas;
                            new SoundPlay(sound_bomb).start();
                            isOver=true;
                            break;
                        }
                    }
                }

            }else {
               //游戏结束，广播通知mainactivity
                Intent intent=new Intent("com.example.administrator.planegeme");
                getContext().sendBroadcast(intent);
                new SoundPlay(sound_gameover).start();
                stop();
            }
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
                if (index==baozhas.size()&&isOver){
                    //敌机爆炸后去掉爆炸的痕迹
                    mGameImageList.remove(this);
                }
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

        private List<Bitmap> baozhas=getBaozhas();

        public DijiImage(Bitmap diren){
            this.diren=diren;
            //分解敌机的照片
            bitmaps.add(Bitmap.createBitmap(diren,0,0,diren.getWidth()/4,diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren,(diren.getWidth())/4*1,0,diren.getWidth()/4,diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren,(diren.getWidth())/4*2,0,diren.getWidth()/4,diren.getHeight()));
            bitmaps.add(Bitmap.createBitmap(diren,(diren.getWidth())/4*3,0,diren.getWidth()/4,diren.getHeight()));
            //分解爆炸的照片
//            baozhas.add(Bitmap.createBitmap(baozha, 0, 0,
//                    baozha.getWidth() / 4, baozha.getHeight() / 2));
//            baozhas.add(Bitmap.createBitmap(baozha,
//                    (baozha.getWidth() / 4) * 1, 0, baozha.getWidth() / 4,
//                    baozha.getHeight() / 2));
//            baozhas.add(Bitmap.createBitmap(baozha,
//                    (baozha.getWidth() / 4) * 2, 0, baozha.getWidth() / 4,
//                    baozha.getHeight() / 2));
//            baozhas.add(Bitmap.createBitmap(baozha,
//                    (baozha.getWidth() / 4) * 3, 0, baozha.getWidth() / 4,
//                    baozha.getHeight() / 2));
//
//            baozhas.add(Bitmap.createBitmap(baozha, 0, baozha.getHeight() / 2,
//                    baozha.getWidth() / 4, baozha.getHeight() / 2));
//            baozhas.add(Bitmap.createBitmap(baozha,
//                    (baozha.getWidth() / 4) * 1, baozha.getHeight() / 2,
//                    baozha.getWidth() / 4, baozha.getHeight() / 2));
//            baozhas.add(Bitmap.createBitmap(baozha,
//                    (baozha.getWidth() / 4) * 2, baozha.getHeight() / 2,
//                    baozha.getWidth() / 4, baozha.getHeight() / 2));
//            baozhas.add(Bitmap.createBitmap(baozha,
//                    (baozha.getWidth() / 4) * 3, baozha.getHeight() / 2,
//                    baozha.getWidth() / 4, baozha.getHeight() / 2));
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
            y+=dijiyidong;
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
                     //打掉一个敌机加10分
                     jifen+=10;
                     new SoundPlay(sound_bomb).start();
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
     * 分解爆炸图片
     * */
    public List<Bitmap> getBaozhas(){
         List<Bitmap> baozhas=new ArrayList<>();
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

        return baozhas;
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
