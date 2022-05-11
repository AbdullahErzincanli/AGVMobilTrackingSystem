package com.example.mobilagv_v1;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilagv_v1.MODEL.QRAGV;
import com.example.mobilagv_v1.MODEL.QRGeoModel;
import com.example.mobilagv_v1.MODEL.QRURLModel;
import com.example.mobilagv_v1.MODEL.QRVCarModel;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView scannerView;
    private TextView txtResult;
    private Button btn_Connect;

    private Thread Thread1 = null;
    private static ConnectionHandler handler;
    private Timer timer;
    private ProgressDialog progressDialog;
    private TCPAsyncConnection tcpAsyncConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        scannerView = (ZXingScannerView) findViewById(R.id.zxscan);
        txtResult = (TextView)findViewById(R.id.txt_result);
        btn_Connect = (Button)findViewById(R.id.btn_Connect);
        /*
        PDialoginit();
        if(SocketListener.isConnected())
            Toast.makeText(getApplicationContext(),"Connection Successful!",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(),"Connection Fail!",Toast.LENGTH_SHORT).show();
        */
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(MainActivity.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this,"You must accept this permission",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();


        btn_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( CDSAGV!= null && ConnectionInfo == false && (!CDSAGV.getIP().equals(QRAGV.isIPEmpty)) && CDSAGV.getPort() != QRAGV.isPortEmpty ){



                    SERVER_IP = CDSAGV.getIP();
                    SERVER_PORT = CDSAGV.getPort();

                    Variables.AGVServerIP = SERVER_IP;
                    Variables.AGVServerPort = SERVER_PORT;
                    Variables.AGVWidth = CDSAGV.getWidth();
                    Variables.AGVHeigth = CDSAGV.getLength();
                    Variables.AGVOdomWidth = CDSAGV.getOdomWidth();
                    Variables.AGVOdomHeigth = CDSAGV.getOdomLength();
                    Variables.AGVName = CDSAGV.getName();
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    scannerView.stopCamera();
                    startActivity(intent);
                    /*
                    showPDialog();
                    Log.d("Conncetion" , "Bağlantıda");
                    SERVER_IP = CDSAGV.getIP();
                    SERVER_PORT = CDSAGV.getPort();

                    SocketListener.setServerIp(SERVER_IP);
                    SocketListener.setServerPort(SERVER_PORT);
                    SocketListener.startThread();

                    //Thread1 = new Thread(new Thread1());
                    //Thread1.start();


                    closePDialog();

                    //btnDisconnect.setEnabled(true);
                    //btnConnect.setEnabled(false);
                    */
                }
            }
        });
/*
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        //System.out.println("Linvel = " + fmt.format(LinVel) + "  RotVel = " + fmt.format(RotVel));


                        if(LinVelIncreaseControl == true)
                            LinVel = LinVel + MaxLinVel /20;
                        if(LinVelDecreaseControl == true)
                            LinVel = LinVel - MaxLinVel /20;

                        if(RotVelIncreaseControl == true)
                            RotVel = RotVel + MaxRotVel /20;
                        if(RotVelDecreaseControl == true)
                            RotVel = RotVel - MaxRotVel /20;

                        LinVel = Math.max(-MaxLinVel, LinVel);
                        LinVel = Math.min(MaxLinVel, LinVel);
                        RotVel = Math.max(-MaxRotVel, RotVel);
                        RotVel = Math.min(MaxRotVel, RotVel);

                        tvMessages.setText("Linvel = " + fmt.format(LinVel) + " m/s"  + "\nRotVel = " + fmt.format(RotVel) + " m/s");

                        if(isConnected){
                            output.write("L" + fmt.format(100*LinVel )+ "R" + fmt.format(100*RotVel)+ "$");
                            output.flush();
                            //System.out.println("gönderiyor");
                        }

                    }
                });
            }
        };

        timer = new Timer();

        timer.schedule(timerTask,2000,50);
*/

        //SocketListener.startThread();

    }

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }

    @Override
    public void handleResult(Result rawResult) {
        //txtResult.setText(rawResult.getText());

        processRawResult(rawResult.getText());
        Log.d("getText" , "  = " + rawResult.getText());

    }

    private void processRawResult(String text) {

        if(text.startsWith("BEGIN:"))
        {
            String[] tokens = text.split("\n");
            QRVCarModel qrvCarModel = new QRVCarModel();
            for(int i = 0; i<tokens.length; i++)
            {
                if(tokens[i].startsWith("BEGIN:"))
                {
                    qrvCarModel.setType(tokens[i].substring("BEGIN:".length()));
                }else if(tokens[i].startsWith("N:"))
                {
                    qrvCarModel.setName(tokens[i].substring("N:".length()));
                }else if(tokens[i].startsWith("ORG:"))
                {
                    qrvCarModel.setOrg(tokens[i].substring("ORG:".length()));
                }else if(tokens[i].startsWith("TEL:"))
                {
                    qrvCarModel.setTel(tokens[i].substring("TEL:".length()));
                }else if(tokens[i].startsWith("URL:"))
                {
                    qrvCarModel.setUrl(tokens[i].substring("URL:".length()));
                }else if(tokens[i].startsWith("EMAIL:"))
                {
                    qrvCarModel.setEmail(tokens[i].substring("EMAIL:".length()));
                }
                else if(tokens[i].startsWith("ADR:"))
                {
                    qrvCarModel.setAdress(tokens[i].substring("ADR:".length()));
                }else if(tokens[i].startsWith("NOTE:"))
                {
                    qrvCarModel.setNote(tokens[i].substring("NOTE:".length()));
                }else if(tokens[i].startsWith("SUMMARY:"))
                {
                    qrvCarModel.setSummary(tokens[i].substring("SUMMARY:".length()));
                }else if(tokens[i].startsWith("DTSTART:"))
                {
                    qrvCarModel.setDtstart(tokens[i].substring("DTSTART:".length()));
                }else if(tokens[i].startsWith("DTEND:"))
                {
                    qrvCarModel.setDtend(tokens[i].substring("DTEND:".length()));
                }

                txtResult.setText(qrvCarModel.getTel());
            }

        }else if(text.startsWith("http://") || text.startsWith("https://") || text.startsWith("www."))
        {
            QRURLModel qrurlModel = new QRURLModel(text);
            txtResult.setText(qrurlModel.getUrl());
        }else if(text.startsWith("geo:"))
        {
            QRGeoModel qrGeoModel = new QRGeoModel();
            String delims =  "[ , ?q=]+";
            String tokens[] = text.split(delims);

            for(int i = 0 ; i<tokens.length;i++){
                if(tokens[i].startsWith(" geo:")){
                    qrGeoModel.setLat(tokens[i].substring("geo:".length()));
                }
            }
            qrGeoModel.setLat(tokens[0].substring("geo:".length()));
            qrGeoModel.setLng(tokens[1]);
            qrGeoModel.setGeo_plave(tokens[2]);

            txtResult.setText(qrGeoModel.getLat() + "/" + qrGeoModel.getLng());
        }else if(text.startsWith("AGVCDS:"))
        {
            String[] tokens = text.split("//");
            CDSAGV = new QRAGV();

            for(int i = 0; i<tokens.length; i++)
            {

                if(tokens[i].startsWith("NAME:"))
                {
                    CDSAGV.setName(tokens[i].substring("NAME:".length()));
                }else if(tokens[i].startsWith("IP:"))
                {
                    CDSAGV.setIP(tokens[i].substring("IP:".length()));
                }else if(tokens[i].startsWith("LENGTH:"))
                {
                    CDSAGV.setRobotLength(tokens[i].substring("LENGTH:".length()));
                }else if(tokens[i].startsWith("WIDTH:"))
                {
                    CDSAGV.setRobotwidth(tokens[i].substring("WIDTH:".length()));
                }else if(tokens[i].startsWith("AGVCDS:"))
                {
                    tokens[i].substring("AGVCDS:".length());
                }else if(tokens[i].startsWith("PORT:"))
                {
                    CDSAGV.setPort(tokens[i].substring("PORT:".length()));
                }else if(tokens[i].startsWith("ODOML:"))
                {
                    CDSAGV.setOdomLength(tokens[i].substring("ODOML:".length()));
                }else if(tokens[i].startsWith("ODOMW:"))
                {
                    CDSAGV.setOdomWidth(tokens[i].substring("ODOMW:".length()));
                }


            }
            txtResult.setText(CDSAGV.getName());
        }else
        {
            txtResult.setText(text);
        }
        scannerView.resumeCameraPreview(MainActivity.this);
    }

    QRAGV CDSAGV;
    String SERVER_IP;
    int SERVER_PORT;
    Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private boolean isConnected = false;
    boolean ConnectionInfo = false;

    class Thread1 implements Runnable {
        public void run() {

            try {

                socket = new Socket(SERVER_IP, SERVER_PORT);

                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isConnected = true;
                        //tvConnection.setText("Connected");
                        ConnectionInfo = true;
                        //btnConnect.setText("DisConnect To Server");
                    }
                });
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
                        final String message = input.readLine();
                        if (message != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //tvMessages.append("server: " + message + "\n");
                                }
                            });
                        } else {
                            Thread1 = new Thread(new Thread1());
                            Thread1.start();
                            return;
                        }
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

}
