package com.example.mobilagv_v1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

@SuppressLint("AppCompatCustomView")
public class MapRobotDraw extends ImageView {

    private Paint paint;
    private boolean isInitView;
    private Rect rect;
    private Rect rectMap;
    private int heigth, width = 0;

    public Bitmap Map; // Haberleşmeden
    public double RobotOriginX, RobotOriginY;  // Haberleşmeden
    public double RobotWidth, RobotHeigth; // barkoddan
    Random r;
    private byte[] byteMap;
    private boolean MapInit = false;

    public boolean isMapInit() {
        return MapInit;
    }

    public void setMapInit(boolean mapInit) {
        MapInit = mapInit;
    }


    public MapRobotDraw(Context context) {
        super(context);
    }

    public MapRobotDraw(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MapRobotDraw(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView(){
        r = new Random();
        heigth = getHeight();
        width = getWidth();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        isInitView = true;
        w = 0;
        double mapscale;
        rect = new Rect();

        rect.left = width/2;
        rect.top = heigth/2;
        rect.right = rect.left + 100;
        rect.bottom = rect.top + 200;

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        byteMap = new byte[Variables.MapWidth * Variables.MapHeigth];

        System.arraycopy(Variables.byteMap, 0,byteMap,0,Variables.MapWidth * Variables.MapHeigth);
        //Create bitmap with width, height, and 4 bytes color (RGBA)
        /*
        for(int i = 0; i<Variables.MapWidth; i++){
            for(int j = 0 ; j<Variables.MapHeigth; j++){
                System.out.print(" " + byteMap[j + i * Variables.MapWidth] + " ");
            }

            System.out.println("");
        }*/
        /*
        Bitmap bmp = Bitmap.createBitmap(Variables.MapWidth, Variables.MapHeigth, Bitmap.Config.RGB_565);
        ByteBuffer buffer = ByteBuffer.wrap(byteMap);
        buffer.rewind();
        bmp.copyPixelsFromBuffer(buffer);
        */

        int[] intArray = new int[205*332];
        for(int i = 0; i<205; i++){
            for(int j = 0; j<332; j++){
                intArray[i+j*205] = 205;
            }
        }




        //Map = bmp;
        //rectMap = new Rect(0,0,width,heigth);

        Log.d("MyApp","width = " + width + "  heigth = " + heigth);
        Log.d("MyApp","Map width = " + Map.getWidth() + " Map heigth = " + Map.getHeight());

        double originalWidth = Map.getWidth();
        double originalHeigth= Map.getHeight();


        mapscale = (double)width/originalWidth > (double)heigth/originalHeigth ? (double)heigth/originalHeigth : (double)width/originalWidth;
        Log.d("MyApp"," width/originalWidth = " + width/originalWidth + "  heigth/originalHeigth = " + heigth/originalHeigth);
        double scaledWidth = originalWidth * mapscale;
        double scaledHeigth = originalHeigth * mapscale;

        double offsetX = width - scaledWidth;
        double offsetY = heigth - scaledHeigth;
        Log.d("MyApp"," mapscale = " + mapscale);
        rectMap = new Rect((int)Math.round(offsetX/2),0,(int)Math.round(scaledWidth + offsetX/2),(int)Math.round(scaledHeigth));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Map = Bitmap.createBitmap(205, 332, Bitmap.Config.ARGB_8888);
        Map.copyPixelsFromBuffer(IntBuffer.wrap(Variables.intMap));
        this.setImageBitmap(Map);

        if(Variables.isInitMap){
            if(!isInitView){
                initView();
            }

            //canvas.drawColor(Color.RED);
            //canvas.drawBitmap(Map,null,rectMap,null);
            //this.setImageBitmap(Map);
            //canvas.drawBitmap(Map,0,0,null);
            //canvas.drawColor(getResources().getDrawable(R.drawable.cds_map));
            //paint.reset();

            //canvas.rotate(0,width/2, heigth/2);

            //drawRect(canvas);
            //drawCenter(canvas);
            //drawNumbers(canvas);
            //drawHands(canvas);
            postInvalidateDelayed(10);
            //postInvalidate();
        }

    }



    int acc = 5 ;
    int w=0;
    private void drawRect(Canvas canvas){
        paint.reset();
        paint.setColor(getResources().getColor(android.R.color.holo_blue_bright));
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);

        paint.setAntiAlias(true);
        //canvas.drawRect(10,50,30,100,paint);
        //w += acc;
        //canvas.drawCircle(w,heigth/2,20+padding-10,paint);

        moveRect(rect);
        canvas.drawRect(rect,paint);
    }

    private void moveRect(Rect rec){

        int direction = (int)Math.round(r.nextDouble() * 4);

        if(direction == 0){
            rect.top+=acc;
            rect.bottom+=acc;
        }else if(direction == 1){
            rect.top-=acc;
            rect.bottom-=acc;
        }else if(direction == 2){
            rect.right+=acc;
            rect.left+=acc;
        }else if(direction == 3){
            rect.right-=acc;
            rect.left-=acc;
        }
    }
}
