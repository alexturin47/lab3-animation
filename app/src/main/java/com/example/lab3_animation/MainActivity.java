package com.example.lab3_animation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    float x;
    float y;
    Point point;
    List<Point> points;
    Boolean runFlag = false;
    char chars[] = {'Т', 'Ю', 'Р', 'И', 'Н'};
    CharArray mychars;
    public static int HEIGTH_DEVICE;
    public static int WIDTH_DEVICE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrawView drawView = new DrawView(this);

        drawView.setOnTouchListener(this);
        setContentView(drawView);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);
        HEIGTH_DEVICE = metricsB.heightPixels;
        WIDTH_DEVICE = metricsB.widthPixels;

        mychars = new CharArray(chars);
        points = new ArrayList<>();
        runFlag = true;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        x = event.getX();
        y = event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            // добавляем в массив объект с начальными координатами x,y
            Point p = new Point(x,y,mychars.getChar());
            points.add(p);
            PointThread pth = PointThread.createAndStart(p);
        }
        return false;
    }


    class DrawView extends SurfaceView implements SurfaceHolder.Callback{

        private DrawThread drawThread;

        public DrawView(Context context) {
            super(context);
            getHolder().addCallback(this);
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            drawThread = new DrawThread(getHolder(), getResources());
            drawThread.setRunFlag(true);
            drawThread.start();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            boolean retry = true;
            drawThread.setRunFlag(false);
            while (retry){
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
             //       e.printStackTrace();
                }
            }
        }
    }

    class DrawThread extends Thread{
        private boolean runFlag = false;
        private SurfaceHolder surfaceHolder;
        private Paint paint;

        public DrawThread(SurfaceHolder surfaceHolder, Resources resources) {
            this.surfaceHolder = surfaceHolder;
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(120);
            paint.setFakeBoldText(true);
            paint.setColor(Color.BLUE);
        }

        public void setRunFlag(boolean flag){
            this.runFlag = flag;
        }

        @Override
        public void run(){
            Canvas canvas;
            while(runFlag){
                canvas = null;
                try{
                    canvas = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder){
                        canvas.drawColor(Color.WHITE);
                        try{
                            for(Point pt: points){
                                synchronized (pt){
                                    canvas.drawText(""+pt.ch, pt.x, pt.y, paint);
                                }
                            }
                        } catch (ConcurrentModificationException e){

                        }

                    }
                }
                finally {
                    if(canvas != null){
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }




    class CharArray{
        private char[] arr;
        int index;

        public CharArray(char[] arr) {
            this.arr = arr;
            this.index = 0;
        }

        public char getChar(){
            if(index == arr.length) {
                index = 1;
                return arr[0];
            }
            return arr[index++];
        }
    }
}

class Point {
    float x;
    float y;
    char ch;
    boolean run;
    boolean down;

    public Point(float x, float y, char ch) {
        this.x = x;
        this.y = y;
        this.ch = ch;
        this.run = true;
        this.down = true;
    }


}

class PointThread implements Runnable{
    Point point;
    Thread th;

    public PointThread(Point point) {
        this.point = point;
        th = new Thread(this) ;
    }


    public static PointThread createAndStart(Point point){
        PointThread pthr = new PointThread(point);
        pthr.th.start();
        return pthr;
    }


    @Override
    public void run() {

        float x0 = point.x;
        float y0 = point.y;
        float vy = 0;
        float vx = 25f;
        float g = 9.8f;
        float t = 0;
        float v=0;

        while(point.run){
            if (point.y <= MainActivity.HEIGTH_DEVICE - 250) {
                point.y = y0 + vy * t + g * t * (t / 2);
                point.x = x0 + vx * t;
                v = vy + g * t;
                vy = v;
                t += 0.2f;
            } else {
                vy = -(v+g*t);
                vx = vx*0.85f;
                t=0;
                x0 = point.x;
                y0 = MainActivity.HEIGTH_DEVICE-250;
                point.y = y0;
            }

            if(vx < 10 || point.x > MainActivity.WIDTH_DEVICE) point.run = false;

            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}