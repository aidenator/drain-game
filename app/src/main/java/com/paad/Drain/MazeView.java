package com.paad.Drain;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;

public class MazeView extends View {
    public float maxheight;//Screen dimensions...
    public float maxwidth;
    public Paint bar1;
    public Paint bar2;
    public Paint drain;
    public Paint tomato;
    public Paint textPaint;
    public Paint currTime;
    public Paint bestTime;
    private int textHeight;
    public float accelx = 0;//Acceleration in y direction
    public float accely = 0;
    public float vx = 0;//Velocity in x direction
    public float vy = 0;
    public float posx=50;//Current position of tomato in x axis
    public float posy=50;
    public float barwidth = 0;//How much gap between rectangle wall and edge of screen
    public float barstart = 0;//How far down the rectangle wall starts
    public int distance = 1000;//Current distance between drain and tomato
    public boolean inDrain = false;//True if we made it in the drain
    long curr = -1;//The current time
    long post = -1;
    long best = 999999999;
    long prevlong=0;
    int framecount = 0;

    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMazeView();
    }

    protected void initMazeView() {
        setFocusable(true);

        Resources r = this.getResources();

        bar1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        bar1.setColor(Color.GREEN);
        bar1.setStrokeWidth(1);
        bar1.setStyle(Paint.Style.FILL_AND_STROKE);

        bar2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        bar2.setColor(Color.MAGENTA);
        bar2.setStrokeWidth(1);
        bar2.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float textSize = 36;
        textPaint.setTextSize(textSize);
        textPaint.setColor(r.getColor(R.color.text_color));
        textHeight = (int)textPaint.measureText("yY");
        textPaint.setTextAlign(Paint.Align.CENTER);

        currTime = new Paint(Paint.ANTI_ALIAS_FLAG);
        textSize = 40;
        currTime.setTextSize(textSize);
        currTime.setColor(r.getColor(R.color.text_color));
        currTime.setTextAlign(Paint.Align.LEFT);

        bestTime = new Paint(Paint.ANTI_ALIAS_FLAG);
        textSize = 40;
        bestTime.setTextSize(textSize);
        bestTime.setColor(r.getColor(R.color.text_color));
        bestTime.setTextAlign(Paint.Align.LEFT);

        drain = new Paint(Paint.ANTI_ALIAS_FLAG);
        drain.setColor(Color.GRAY);
        drain.setStrokeWidth(5);
        drain.setStyle(Paint.Style.FILL_AND_STROKE);

        tomato = new Paint(Paint.ANTI_ALIAS_FLAG);
        tomato.setColor(Color.RED);
        tomato.setStrokeWidth(4);
        tomato.setStyle(Paint.Style.FILL_AND_STROKE);
        curr= System.currentTimeMillis();

        post(animator);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 100;
        int desiredHeight = 100;
        int width;
        int height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode= MeasureSpec.getMode(heightMeasureSpec);
        int heightSize= MeasureSpec.getSize(heightMeasureSpec);

        if(widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }
        else if(widthMode == MeasureSpec.AT_MOST){
            width = Math.min(desiredWidth, widthSize);
        }
        else{
            width = desiredWidth;
        }

        if(heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }
        else if(heightMode == MeasureSpec.AT_MOST){
            height = Math.min(desiredHeight, heightSize);
        }
        else{
            height = desiredHeight;
        }
        maxheight = height;
        maxwidth = width;
        barwidth = .7f*width;
        barstart = .7f*height;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int mMeasuredWidth = getMeasuredWidth();
        int mMeasuredHeight = getMeasuredHeight();

        int px = (int) maxwidth / 2;
        int py = (int) maxheight / 2 ;

        //drawRect (float left, float top, float right, float bottom, Paint paint)
        canvas.drawRect(0,.3f*mMeasuredHeight, .7f*mMeasuredWidth, .3f*mMeasuredHeight+20, bar1);
        canvas.drawRect(.3f*mMeasuredWidth,.7f*mMeasuredHeight, mMeasuredWidth, .7f*mMeasuredHeight+20, bar2);
        String dist = "Dist to drain: "+distance;
        canvas.drawText(dist, px, py, textPaint);
        canvas.drawText("Best: "+ ((best < 999999999)?best/1000f:"None") ,.65f*mMeasuredWidth,75,bestTime);
        long time = System.currentTimeMillis();
        long currentlong;
        framecount++;
        if(framecount == 2) {
            currentlong = time - curr;
            prevlong = currentlong;
            framecount = 0;
        }
        else
            currentlong = prevlong;
        float current = currentlong / (float)1000;

        canvas.drawText("Time: "+ Float.toString(current), .65f*mMeasuredWidth, 40, currTime);
        canvas.drawCircle(maxwidth-80, maxheight-80, 38, drain);

        if(inDrain) {
            tomato.setColor(Color.YELLOW);
            if(post - curr < best) best = post - curr;
            curr = System.currentTimeMillis();
            inDrain = false;
        }
        canvas.drawCircle(posx, posy, 30, tomato);
    }

    private Runnable animator = new Runnable() {
        @Override
        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            if(now % 200 == 0){//Log every once in awhile
                Log.i("Runnable", "x:"+ posx + ", y:"+posy+", vx:"+vx+", vy:"+vy+", ax:"+accelx+", ay:"+accely);
            }
            doPhysics();//Find proper coords for tomato
            postDelayed(this, 20);//Update every 20ms
            invalidate();
        }
    };

    //Does some fancy physics using acceleration and velocity
    public void doPhysics(){
        float tx, ty = 0;
        float beta = .25f;//Slow down acceleration
        float coef = .6f;//Coefficient of friction
        tx = beta * accelx;//Acceleration times a constant
        ty = beta * accely;
        float finalx = tx + vx;//Acceleration plus velocity
        float finaly = ty + vy;

        //Subtract off friction if it has some velocity. Else don't move if it would go negative.
        finalx = (Math.abs(finalx) - coef > 0) ? (finalx - coef) : 0;
        finaly = (Math.abs(finaly) - coef > 0) ? (finaly - coef) : 0;

        posx += finalx;//Actually update the coordinates
        posy += finaly;
        vx += accelx/6;//Update your velocity for the next call
        vy += accely/6;

        collision();//Do all the collision physics checking

    }

    //First check wall collisions then the rectangle walls
    public void collision(){
        float elasticity = -.3f;
        if(posx < 30) {//Collision check the walls, 30px is radius of tomato
            posx = 30;//Left wall
            vx *= elasticity;//Adjust velocity in opposite direction
        }
        if(posy < 30) {//Upper wall
            posy = 30;
            vy *= elasticity;
        }
        if(posx > maxwidth-30) {//Right wall
            posx = maxwidth-30;
            vx *= elasticity;
        }
        if(posy > maxheight-30) {//Bottom wall
            posy = maxheight-30;
            vy *= elasticity;
        }
        ////////Collision check rectangle blockades
        float bar1top = maxheight - barstart;// y = 100 / 400
        float bar1bottom = bar1top + 20;// y = 120 / 400
        float bar2top = barstart;// y = 300 / 400
        float bar2bottom = bar2top + 20;// y = 320 / 400
        float bar1end = barwidth;// x = 300 / 400
        float bar2end = maxwidth - barwidth;// x = 100 / 400

        if(Math.abs(posy - bar1top) < 30 && posx < bar1end){//Top of first bar
            posy = bar1top-31;
            vy *= elasticity;
        }
        if(Math.abs(posy - bar1bottom) < 30 && posx < bar1end){//Bottom of first bar
            posy = bar1bottom+31;
            vy *= elasticity;
        }

        if(Math.abs(posy - bar2top) < 30 && posx > bar2end){//Top of second bar
            posy = bar2top-31;
            vy *= elasticity;
        }
        if(Math.abs(posy - bar2bottom) < 30 && posx > bar2end){//Bottom of second bar
            posy = bar2bottom+31;
            vy *= elasticity;
        }

        if(Math.abs(posx - bar1end) < 30 && Math.abs(posy - bar1top + 10) < 20){
            posx = bar1end + 29;
            if(vx < 0)//Need this because if it rolls off platform it jumps to the edge.
                vx *= elasticity;
        }
        if(Math.abs(posx - bar2end) < 30 && Math.abs(posy - bar2top + 10) < 20){
            posx = bar2end -29;
            if(vx > 0)//Need this because if it rolls off platform it jumps to the edge.
                vx *= elasticity;
        }

        //Check if tomato is completely in drain
        //Calculate how far away from the drain the tomato is
        double distx = (maxwidth-80) - posx;
        double disty = (maxheight-80) - posy;
        distance = (int)Math.sqrt(distx * distx + disty * disty);
        distance -= 5;//Make it easier for the player...
        if(distance < 10){//10px is the difference between the two radii
            inDrain = true;
            post = System.currentTimeMillis();
            reset();
        }
    }

    public void reset(){
        posx = 50;
        posy = 50;
        vx = vy = 0;
    }

    public void setAccel(float x, float y){
        accelx = x;
        accely = y;
    }
}

