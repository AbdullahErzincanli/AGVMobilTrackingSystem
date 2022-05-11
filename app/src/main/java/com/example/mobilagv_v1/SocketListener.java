package com.example.mobilagv_v1;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketListener implements Runnable {

    private static Socket socket;
    private static BufferedWriter output;
    private static BufferedReader input;
    private static boolean isConnected = false;
    private static Thread socketThread;
    private static boolean isStartThread = false;
    private static SocketListener listener = new SocketListener();
    private static String SERVER_IP;
    private static int SERVER_PORT;
    private String TAG = getClass().getName();

    public static boolean isConnected() {
        return isConnected;
    }

    public static boolean isStartThread() {
        return isStartThread;
    }

    public static String getServerIp() {
        return SERVER_IP;
    }

    public static void setServerIp(String serverIp) {
        SERVER_IP = serverIp;
    }

    public static int getServerPort() {
        return SERVER_PORT;
    }

    public static void setServerPort(int serverPort) {
        SERVER_PORT = serverPort;
    }

    public static void startThread(){
        if(isConnected != true){

                socketThread = new Thread(listener);
                socketThread.start();

        }
    }

    public static void stopThread(){
        if(isConnected == true){
            //isConnected = false;
            isStartThread = false;
        }
    }

    @Override
    public void run() {
        long tStart, tEnd, tDelta = 0;
        String message;
        Exception error = null;

        try {
            Log.d("socketThread" , "dene1");
            socket = new Socket(SERVER_IP, SERVER_PORT);

            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isStartThread = true;
            isConnected = true;
            Log.d("socketThread" , "dene2");
            while (isStartThread ){

                if(isConnected){
                    message = input.readLine();
                        if (message != null) {
                            dataReader(message);
                        }
                }


                /*
                tStart = System.currentTimeMillis();
                while(tDelta<10000){
                    tEnd = System.currentTimeMillis();
                    tDelta = tEnd - tStart;
                }
                //Log.d("thread tDelta = ", "tDelta = " + tDelta);
                tDelta = 0;
                */
            }

        } catch (UnknownHostException ex) {
            Log.e(TAG, "doInBackground(): " + ex.toString());
            error = !isStartThread ? null : ex;
        } catch (IOException ex) {
            Log.d(TAG, "doInBackground(): " + ex.toString());
            error = !isStartThread ? null : ex;
        } catch (Exception ex) {
            Log.e(TAG, "doInBackground(): " + ex.toString());
            error = !isStartThread ? null : ex;
        } finally {
            try {
                socket.close();
                output.close();
                input.close();
            } catch (Exception ex) {}
        }
    }

    public void write(final String data) {
        try {
            Log.d(TAG, "writ(): data = " + data);
            output.write(data + "\n");
            output.flush();
        } catch (IOException ex) {
            Log.e(TAG, "write(): " + ex.toString());
        } catch (NullPointerException ex) {
            Log.e(TAG, "write(): " + ex.toString());
        }
    }

    public void disconnect() {
        try {
            Log.d(TAG, "Closing the socket connection.");

            isConnected = false;
            isStartThread = false;
            if(socket != null) {
                socket.close();
            }
            if(output != null & input != null) {
                output.close();
                input.close();
            }
        } catch (IOException ex) {
            Log.e(TAG, "disconnect(): " + ex.toString());
        }
    }

    private void dataReader(String msg){
        Log.d("datareader", "hadi bakalım : ");
    }

}
