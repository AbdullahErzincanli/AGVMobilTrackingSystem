package com.example.mobilagv_v1;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class MapActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    TextView txtAGVName, txtBattery, txtVel, txtLidarError, txtMotorError;

    String SERVER_IP, AGVName;
    int SERVER_PORT;
    Socket socket;
    private PrintWriter output;
    private InputStream input;
    private boolean isConnected = false;
    private boolean isMapLoaded = false;
    Thread Thread1 = null;
    StickerViewImage mrDraw;
    Bitmap Map;
    Canvas canvas ;
    Paint paint;
    Rect RobotRect;
    double RobotWidth, RobotHeight, RobotOdomWidth, RobotOdomHeight;
    double MapResolution = 0.05;
    double RobotX = 0;
    double RobotY = 0;
    double RobotTheta = 0;
    double RobotLinVel = 0;
    double RobotRotVel = 0;
    short RobotLidarError = 0, RobotMotorError = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_map);

        txtAGVName = (TextView)findViewById(R.id.txtConnection);
        txtBattery = (TextView)findViewById(R.id.txtBattery);
        txtVel = (TextView)findViewById(R.id.txtVels);
        txtLidarError = (TextView)findViewById(R.id.txtErrorLidar);
        txtMotorError = (TextView)findViewById(R.id.txtErrorMotor);

        SERVER_IP = Variables.AGVServerIP;
        SERVER_PORT = Variables.AGVServerPort;
        AGVName = Variables.AGVName;
        PDialoginit();
        mrDraw = (StickerViewImage)findViewById(R.id.mrDraw);
        mrDraw.setScaleType(ImageView.ScaleType.MATRIX);

        RobotRect = new Rect();
        RobotWidth = Variables.AGVWidth /1000.0/MapResolution;
        RobotHeight = Variables.AGVHeigth /1000.0/MapResolution;
        RobotOdomWidth = Variables.AGVOdomWidth /1000.0/MapResolution;
        RobotOdomHeight = Variables.AGVOdomHeigth /1000.0/MapResolution;


        Thread1 = new Thread(new Thread1());
        Thread1.start();

        showPDialog();



        long tStart, tEnd, tDelta = 0;
        tStart = System.currentTimeMillis();
        while( (isMapLoaded == false) && (tDelta<5000)){
            tEnd = System.currentTimeMillis();
            tDelta = tEnd - tStart;
            //System.out.println("isMapLoaded = "  + isMapLoaded + "  tDelta = " + tDelta);
        }

        closePDialog();

        if(isConnected){
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                public void run() {
                    if(Map != null)

                        //DrawRobot((int)RobotX,(int)RobotY,(int)RobotTheta);
                        mrDraw.setImageBitmap(Map);
                    if(isConnected){
                        txtAGVName.setBackgroundColor(Color.GREEN);
                        txtAGVName.setTextColor(Color.BLACK);
                        txtAGVName.setText(AGVName + '\n' + "Connected");


                    }
                    else{
                        txtAGVName.setBackgroundColor(Color.RED);
                        txtAGVName.setText(AGVName + '\n' + "Disconnected");

                    }

                    if(RobotLidarError == 1){
                        txtLidarError.setBackgroundColor(Color.GREEN);
                        txtLidarError.setTextColor(Color.BLACK);
                        txtLidarError.setText("No Error Lidar");
                    }else{
                        txtLidarError.setBackgroundColor(Color.RED);
                        txtLidarError.setText("Error Lidar");
                    }

                    if(RobotMotorError == 1){
                        txtMotorError.setBackgroundColor(Color.GREEN);
                        txtMotorError.setTextColor(Color.BLACK);
                        txtMotorError.setText("No Error Motor");
                    }else{
                        txtMotorError.setBackgroundColor(Color.RED);
                        txtMotorError.setText("Error Motor");
                    }


                    txtBattery.setText("Battery:" + '\n' + "%100");
                    txtVel.setText("LinVel= " + RobotLinVel + "m/s" + '\n' + "RotVel= " + RobotRotVel + "rad/s");

                    handler.postDelayed(this, 100);  //for interval...
                }
            };
            handler.postDelayed(runnable, 1000); //for initial delay..
        }
        else{
            Toast.makeText(this,"Connection Failed!",Toast.LENGTH_SHORT).show();
           finish();
        }


    }




    private void PDialoginit(){
        this.progressDialog = new ProgressDialog(this);

    }

    private void showPDialog(){
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.pdialog_layout);
    }

    private void closePDialog(){
        progressDialog.dismiss();
    }

    class Thread1 implements Runnable {
        public void run() {

            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);

                output = new PrintWriter(socket.getOutputStream());
                input = socket.getInputStream();
                isConnected = true;
                output.write("MAP:" + "$");
                output.flush();


                new Thread(new Thread2()).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    if(isConnected){
                        byte[] data = new byte[2];
                        int count = input.read(data);
                        ByteBuffer wrapped = ByteBuffer.wrap(data); // big-endian by default
                        short w = wrapped.getShort(); // 1

                        count = input.read(data);
                        wrapped = ByteBuffer.wrap(data); // big-endian by default
                        short h = wrapped.getShort(); // 1

                        count = input.read(data);
                        wrapped = ByteBuffer.wrap(data); // big-endian by default
                        short scale = wrapped.getShort(); // 1



                        if(w > 0 && h >0 && w*h<16761136 ){

                            //System.out.println(" w =" + w + " h = " + h + "scale = " + MapResolution +" w*h = " + w*h);

                            data = new byte[w*h];
                            count = input.read(data);
                            //System.out.println(" COUNT =" + count);
                            if(count == w*h){
                                Variables.MapWidth = w;
                                Variables.MapHeigth = h;
                                Variables.byteMap = new byte[w*h];
                                Variables.intMap = new int[w*h];
                                //wrapped = ByteBuffer.wrap(data);

                                for(int i =0; i<data.length; i++){
                                    Variables.intMap[i] = (int)(~((data[i]<<16) + (data[i]<<8) + (data[i])));
                                    //System.out.println("  i = " + i + "  ");
                                    //System.out.println(Variables.intMap[i]);
                                }
/*
                                int[] intArray = new int[205*332];
                                for(int i = 0; i<205; i++){
                                    for(int j = 0; j<332; j++){
                                        intArray[i+j*205] = 205;
                                    }
                                }
        */                      MapResolution = ((double)scale )/ 100.0;
                                Map = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                                Map.copyPixelsFromBuffer(IntBuffer.wrap(Variables.intMap));
                                isMapLoaded = true;
                                output.write("POS:" + "$");
                                output.flush();
                                //mrDraw.setImageBitmap(Map);

                                //System.arraycopy(wrapped.getInt(),0,Variables.byteMap,0, count);

                                //Variables.Convert1D22DArray(w,h);
                                Variables.isInitMap = true;

                            }else{
                                output.write("MAP:" + "$");
                                output.flush();
                            }

                        }else if(w == 0 && h == 12){ //10
                            data = new byte[h];
                            count = input.read(data);

                            byte[] Xbyte = new byte[2];
                            byte[] Ybyte = new byte[2];
                            byte[] Tbyte = new byte[2];
                            byte[] LinVelbyte = new byte[2];
                            byte[] RotVelbyte = new byte[2];
                            byte[] LidarErrorbyte = new byte[2];
                            byte[] MotorErrorbyte = new byte[2];

                            System.arraycopy(data,0, Xbyte,0,2);
                            System.arraycopy(data,2, Ybyte,0,2);
                            System.arraycopy(data,4, Tbyte,0,2);
                            System.arraycopy(data,6, LinVelbyte,0,2);
                            System.arraycopy(data,8, RotVelbyte,0,2);
                            System.arraycopy(data,10, LidarErrorbyte,0,1);
                            System.arraycopy(data,11, MotorErrorbyte,0,1);

                            wrapped = ByteBuffer.wrap(Xbyte); // big-endian by default
                            short x = wrapped.getShort(); // 1
                            RobotX = (double)x / 100.0;

                            wrapped = ByteBuffer.wrap(Ybyte); // big-endian by default
                            short y = wrapped.getShort(); // 1
                            RobotY = (double)y / 100.0;

                            wrapped = ByteBuffer.wrap(Tbyte); // big-endian by default
                            short t = wrapped.getShort(); // 1
                            RobotTheta = (double)t / 100.0 * 180 / Math.PI;

                            wrapped = ByteBuffer.wrap(LinVelbyte); // big-endian by default
                            short LinVel = wrapped.getShort(); // 1
                            RobotLinVel = (double)LinVel / 100.0;

                            wrapped = ByteBuffer.wrap(RotVelbyte); // big-endian by default
                            short RotVel = wrapped.getShort(); // 1
                            RobotRotVel = (double)RotVel / 100.0;
                            //System.out.println(" RobotRotVel = " + RobotRotVel);

                            wrapped = ByteBuffer.wrap(LidarErrorbyte); // big-endian by default
                            short LidarError = wrapped.getShort(); // 1
                            RobotLidarError = (short)(LidarError>>8);
                            //System.out.println(" RobotLidarError = " + RobotLidarError);

                            wrapped = ByteBuffer.wrap(MotorErrorbyte); // big-endian by default
                            short MotorError = wrapped.getShort(); // 1
                            RobotMotorError = (short)(MotorError>>8);
                            //System.out.println(" RobotMotorError = " + RobotMotorError);


                            if(Map != null){



                                //Map = Bitmap.createBitmap(205, 332, Bitmap.Config.ARGB_8888);
                                Map.copyPixelsFromBuffer(IntBuffer.wrap(Variables.intMap));
                                //gray(Map);

                                canvas = new Canvas(Map);
                                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                Paint Lidarpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                Paint Wheelpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                //paint.setColor(Color.BLACK);
///
                                //ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                                //paint.setColorFilter(filter);
///

                                paint.setColor(getResources().getColor(android.R.color.holo_blue_bright));
                                paint.setStrokeWidth(10);
                                paint.setStyle(Paint.Style.FILL);

                                double WheelDist = 0.10 / MapResolution;
                                double WheelRadius = 0.25 / MapResolution;

                                Lidarpaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
                                Lidarpaint.setStrokeWidth(10);
                                Lidarpaint.setStyle(Paint.Style.FILL);

                                Wheelpaint.setColor(getResources().getColor(android.R.color.black));
                                Wheelpaint.setStrokeWidth(10);
                                Wheelpaint.setStyle(Paint.Style.FILL);

                                Rect WheelRect = new Rect();

                                RobotRect.top = (int)((RobotX) / MapResolution - RobotOdomHeight);
                                RobotRect.bottom = (int)(RobotRect.top + RobotHeight);

                                RobotRect.left = (int)((RobotY) / MapResolution - RobotOdomWidth - WheelDist);
                                RobotRect.right = (int)(RobotRect.left + RobotWidth + WheelDist);

                                WheelRect.top = (int)((RobotX) / MapResolution + WheelRadius / 2);
                                WheelRect.bottom = (int)((RobotX) / MapResolution - WheelRadius / 2);

                                WheelRect.left = (int)(RobotRect.left  - WheelDist);
                                WheelRect.right = (int)(RobotRect.right + WheelDist);

                                canvas.rotate((float)-RobotTheta, (float)((RobotY) / MapResolution), (float)((RobotX) / MapResolution));

                                canvas.drawRect(WheelRect,Wheelpaint);
                                canvas.drawRect(RobotRect,paint);
                                canvas.drawCircle((float)((RobotY) / MapResolution -  (0.10 / 2 / MapResolution)) ,  (float)((RobotX) / MapResolution + RobotHeight - RobotOdomHeight -  (0.10 / MapResolution) ) , (float)(0.10/ MapResolution), Lidarpaint );


                                //System.out.println("RobotTheta = " + (float)RobotTheta );
                                //System.out.println("RobotX = " + RobotX + "  RobotY = " + RobotY);
                                //System.out.println("RobotHeight = " + RobotHeight + "  RobotWidth = " + RobotWidth+ "  RobotOdomHeight = " + RobotOdomHeight + "  RobotOdomWidth = " + RobotOdomWidth);
                                //System.out.println(" T = " + RobotRect.top + " B = " + RobotRect.bottom + " L = " + RobotRect.left + " R = " + RobotRect.right );

                                //canvas.drawCircle((int)(RobotY /MapResolution), (int)(RobotX/MapResolution), 10, paint);

                                //System.out.println(" w = " + Map.getWidth() /2  + "  h = " + Map.getHeight()/2);
                                //mrDraw.setImageBitmap(Map);

                            }
                            else{
                                output.write("MAP:" + "$");
                                output.flush();
                            }

                        }
                        else{

                        }
                        /*
                        if (message != null) {
                            System.out.println("len = " + message.length());
                        } else {
                            Thread1 = new Thread(new Thread1());
                            Thread1.start();
                            return;
                        }

                         */
                    }else{
                        // bağlantı kopma durmu
                        isConnected = false;
                    }
                } catch (IOException e) {
                    try {

                        if(isConnected == true){
                            socket.close();
                            isConnected = false;

                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }
    }

}
