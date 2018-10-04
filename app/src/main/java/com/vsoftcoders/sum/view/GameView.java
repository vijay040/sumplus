package com.vsoftcoders.sum.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.vsoftcoders.sum.R;
import com.vsoftcoders.sum.activity.GameOverActivity;
import com.vsoftcoders.sum.activity.GamePlayActivity;
import com.vsoftcoders.sum.model.GameObject;
import com.vsoftcoders.sum.util.MySoundPool;
import com.vsoftcoders.sum.util.VsoftApp;

/**
 * Created by Vijay on 10/10/2017.
 */

public class GameView extends View implements View.OnTouchListener {

    GameObject objList[] = new GameObject[25];
    int indexArr[] = new int[25];
    int arrX[] = new int[5];
    private static Random rd = new Random();
    int w;
    int h;
    int x, y;
    public static int startPoint = 63;
    int startX = 5;
    int objWidth;
    int initY = 15;
    Rect baseRect, rodRect;
    boolean isPressed = false;
    Paint p, pLine, pText;
    Point last;
    ArrayList<Point> pointList = new ArrayList<>();
    int startIndex;
    Context context;
    public static int mScore = 0;
    public static int mCoinCount = 0;
    public static int targetCount = 10;
    public  boolean isPaused = false;
    public static boolean isInited = false;
    Bitmap rod;


    public GameView(Context context) {
        super(context);
        this.context = context;
        initScreen();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setOnTouchListener(this);
        rod = VsoftApp.Context().getBitmapFromAssets("game/" + "rod.png"); //mPath+mId+".png"
        initScreen();

        android.os.Handler h = new android.os.Handler(new android.os.Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                return false;
            }
        });

        h.postAtTime(new Runnable() {
            @Override
            public void run() {
                initIndex();
            }
        }, 3000);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < objList.length; i++) {
            if (objList[i] != null)
                objList[i].draw(canvas);
        }

        if (isPressed) {
            if (pointList.size() > 1) {
                for (int i = 0; i < pointList.size() - 1; i++)
                    canvas.drawLine(pointList.get(i).x, pointList.get(i).y, pointList.get(i + 1).x, pointList.get(i + 1).y, pLine);
            }
            if (pointList.size() > 0)
                canvas.drawLine(pointList.get((pointList.size() - 1)).x, pointList.get((pointList.size() - 1)).y, last.x, last.y, pLine);
        }
        //canvas.drawRect(baseRect,p);
        VsoftApp.DrawBitmap(canvas, rod, rodRect);

        ((GamePlayActivity) context).txtScore.setText("Score:" + mScore);
        ((GamePlayActivity) context).txtCoin.setText("" + mCoinCount);

        //canvas.drawText(mScore + "", w / 2, scoreTextY, pText);
        //canvas.drawText(mCoinCount + "", w / 9, scoreTextY + scoreTextY / 6, pText);
        setPosition();
    }


    public void setPosition() {
        for (int i = 0; i < objList.length; i++) {

            for (int j = i; j < objList.length; ) {
                if (j > 19)
                    break;
                j += 5;
                if (objList[i] != null && objList[j] != null && objList[i].rePosition == false) {

                    if (objList[i].mRectCol.intersect(objList[j].mRectCol) && objList[j].isIntersected == false) {
                        //   Log.d("Intersect***", "*****i*** " + i + " *************j****** " + j);

                        objList[j].setRePosition(objList[i].y);
                        // objList[i].setRePosition(indexArr[j]);

                        objList[j].isIntersected = true;
                        objList[j].rePosition = false;

                        if (j + 5 < objList.length - 1 && objList[j + 5] != null && objList[j + 5].rePosition == true)
                            objList[j + 5].rePosition = false;

                        int size = Math.abs(i - j);
                        if (size > 5) {
                            int k = i + 5;
                            objList[k] = objList[j];
                            objList[j] = null;

                        }
                        break;
                    }
                } else if (objList[i] == null && i < 5) {
                    int p = i;
                    for (int k = i; k < objList.length; ) {
                        if (k > 19)
                            break;
                        k += 5;
                        if (objList[k] != null) {
                            objList[p] = objList[k];
                            objList[p].isIntersected = false;
                            objList[p].rePosition = true;
                            objList[k] = null;
                            // Log.e("replace***", "*****p*** " + p+ " *************k****** " + k);
                        }
                        p += 5;
                    }
                }

            }

            if (i < 5 && objList[i] != null && objList[i].isIntersected == false) {
                if (objList[i].mRectCol.intersect(baseRect)) {
                    //Log.e("Intersectin Base***", "*****p*** Intersectin Base**********k******");
                    objList[i].isIntersected = true;
                    objList[i].rePosition = false;
                    objList[i].y = baseRect.bottom - (objList[i].mWidth + GameObject.gap);
                }

            }
            int rnd = VsoftApp.Context().GetRandom(10);
            if (i > 19 && objList[i] == null) {
                switch (i) {
                    case 20:
                        objList[i] = new GameObject(arrX[0], initY, context);
                        if (rnd == 5)
                            objList[i].isCoin = true;
                        break;
                    case 21:
                        objList[i] = new GameObject(arrX[1], initY, context);
                        if (rnd == 5)
                            objList[i].isCoin = true;
                        break;
                    case 22:
                        objList[i] = new GameObject(arrX[2], initY, context);
                        if (rnd == 5)
                            objList[i].isCoin = true;
                        break;
                    case 23:
                        objList[i] = new GameObject(arrX[3], initY, context);
                        if (rnd == 5)
                            objList[i].isCoin = true;
                        break;
                    case 24:
                        objList[i] = new GameObject(arrX[4], initY, context);
                        if (rnd == 5)
                            objList[i].isCoin = true;
                        break;
                }
            }
        }

        invalidate();
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            int mX = (int) motionEvent.getX();// - objWidth / 2;
            int mY = (int) motionEvent.getY();//- objWidth / 2;

            for (int i = 0; i < objList.length; i++) {
                if (objList[i] != null) {
                    if (objList[i].mRect.contains(mX, mY)) {
                        // VsoftApp.mPlayer.playBoop();
                        MySoundPool.playSound(context, R.raw.boop3);
                        Point start = new Point();
                        start.x = objList[i].x + objWidth / 2;
                        start.y = objList[i].y + objWidth / 2;
                        startIndex = i;
                        pLine.setColor(Color.parseColor(objList[i].arrColor[objList[i].mId - 1]));
                        pointList.add(start);
                    }
                }
            }
            return true;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            //AudioPlayer.playGameOverSound();
            startCount();
            int mX = (int) motionEvent.getX() - objWidth / 2;
            int mY = (int) motionEvent.getY() - objWidth / 2;
            pointList.clear();
            isPressed = false;
            removeObj();
            return true;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            isPressed = true;
            int x = (int) motionEvent.getX();// - objWidth / 2;
            int y = (int) motionEvent.getY();//- objWidth / 2;
            last.x = x;
            last.y = y;
            for (int i = 0; i < objList.length; i++) {
                if (objList[i] != null && objList[i].mRect != null) {
                    if (objList[i].mRect.contains(x, y) && (i == startIndex + 5 || i == startIndex - 5 || i == startIndex + (5 + 1) || i == startIndex + (5 - 1) || i == startIndex - (5 + 1) || i == startIndex - (5 - 1) || i == startIndex - 1 || i == startIndex + 1)) {

                        if (objList[startIndex] == null || objList[i] == null) {
                            break;
                        }

                        if (objList[startIndex].mId == objList[i].mId) {

                            objList[startIndex].isRemoved = true;
                            startIndex = i;
                            objList[i].isRemoved = true;
                            if (objList[startIndex].isCoin)
                                MySoundPool.playSound(context, R.raw.coinsound);
                            else
                                MySoundPool.playSound(context, R.raw.boop3);
                            Point last = new Point();
                            last.x = objList[i].x + objWidth / 2;
                            last.y = objList[i].y + objWidth / 2;
                            pointList.add(last);
                            if(timer!=null)
                            timer.cancel();
                            ((GamePlayActivity) context).txtCounter.setVisibility(GONE);

                        }
                    }
                }
            }
            return true;
        }

        return false;
    }

    public void initScreen() {
        setOnTouchListener(this);
        last = new Point();
        p = new Paint();
        p.setColor(Color.RED);

        pLine = new Paint();
        pLine.setColor(Color.YELLOW);
        pLine.setStyle(Paint.Style.STROKE);
        pLine.setStyle(Paint.Style.FILL);
        pLine.setStrokeWidth(20);

        pText = new Paint();
        pText.setColor(Color.WHITE);
        pText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        pText.setTextAlign(Paint.Align.CENTER);
        int scaledSize = context.getResources().getDimensionPixelSize(R.dimen._20sdp);
        pText.setTextSize(scaledSize);

        //pLine.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.NORMAL));
        w = (int) VsoftApp.Context().GetWidthFromPixelPercentageIntoDP(100);
        h = (int) VsoftApp.Context().GetHeightFromPixelPercentageIntoDP(100);

        int startY = (int) VsoftApp.Context().GetHeightFromPixelPercentageIntoDP(startPoint);
        y = startPoint;
        for (int i = 0; i < 25; i++) {
            if (i == 0 || i == 5 || i == 10 || i == 15 | i == 20) {
                x = startX;
                if (i != 0)
                    y = objList[i - 1].mY - (GameObject.widthInPix);
            }

            if (i < 5)
                objList[i] = new GameObject(x, startPoint - 10, context);
            else
                objList[i] = new GameObject(x, y, context);

            if (i < 5)
                arrX[i] = x;

            x = objList[i].mX + GameObject.widthInPix + GameObject.gapInPix;
            indexArr[i] = objList[i].y;
        }
        objWidth = objList[0].mWidth;

        baseRect = new Rect(0, startY + objList[0].mWidth, w, (startY + objList[0].mWidth) + objList[0].mWidth);

        rodRect = new Rect(0, startY + objList[0].mWidth + objList[0].mWidth / 2 + objList[0].mWidth / 6, w, (startY + objList[0].mWidth) + objList[0].mWidth);
    }

    public void printArr() {
        for (int i = 0; i < objList.length; i++) {
            if (objList[i] != null) {
                Log.e("objList[i]" + i, "isIntersected " + objList[i].isIntersected);
            } else
                Log.e("objList[i]" + i, "null " + "********************");
        }
    }

    public void removeObj() {

        int coount = 0;
        int value = 0;
        for (int i = 0; i < objList.length; i++) {
            if ((objList[i] != null && objList[i].isRemoved) && isPressed == false) {
                value = objList[i].mId;
                coount++;
                if (objList[i].isCoin)
                    mCoinCount++;
                objList[i].isSelected = true;
                objList[i].destroy();
                objList[i] = null;
                MySoundPool.playSound(context, R.raw.erasesound);
                for (int k = i; k < objList.length; ) {
                    if (k > 19)
                        break;
                    k += 5;
                    if (objList[k] != null) {
                        objList[k].isIntersected = false;
                        objList[k].rePosition = true;
                        //  Log.e("Intersect***", "*****k*** " + k+ " *************k****** " + k);
                    }
                }
            }
        }
        mScore += (coount * value);

        if (mScore > 500 && mScore < 1000) {
            GameObject.last = 7;
            targetCount = 15;
        } else if (mScore > 1000 && mScore < 1500) {
            GameObject.last = 9;
            targetCount = 18;
        } else if (mScore > 1500 && mScore < 2000) {
            GameObject.last = 11;
            targetCount = 20;
        } else if (mScore > 2000 && mScore < 3000) {
            GameObject.last = 13;
            targetCount = 25;
        } else if (mScore > 3000 && mScore < 4000) {
            GameObject.last = 15;
            targetCount = 30;
        } else if (mScore > 4000) {
            GameObject.last = 20;
            targetCount = 60;
        }
    }

    public void initIndex() {
        for (int i = 0; i < 25; i++) {
            if (objList[i].isIntersected)
                indexArr[i] = objList[i].y;
        }

        isInited = true;
    }


    int count = 0;
    TimerTask counterTask;
   public static Timer timer;

    public void startCount() {
        if (count > 0 && pointList.size()==0 )
            return;

        if (timer != null) {
            timer.cancel();
        }
        if (pointList.size() > 1) {
            count = 0;
        }

        ((GamePlayActivity) context).txtCounter.setVisibility(VISIBLE);
        timer = new Timer();
        counterTask = new TimerTask() {
            @Override
            public void run() {
                ((GamePlayActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (count <= targetCount) {
                            if ((targetCount - count) < 10)
                                ((GamePlayActivity) context).txtCounter.setText("0" + (targetCount - count));
                            else if ((targetCount - count) < 10)
                                ((GamePlayActivity) context).txtCounter.setText("" + (targetCount - count));
                        }
                        if (count >= targetCount) {
                            count = 0;
                            Intent i = new Intent(((GamePlayActivity) context), GameOverActivity.class);
                            ((GamePlayActivity) context).startActivity(i);
                            timer.cancel();
                            ((GamePlayActivity) context).mInterstitialAd.show();
                            ((GamePlayActivity) context).finish();
                        }
                    }
                });
                if (isPaused == false)
                    count++;
            }
        };

        // timer.schedule(counterTask, 1000* targetCount,1000 );
        timer.scheduleAtFixedRate(counterTask, 1000, 1000);

    }

}
