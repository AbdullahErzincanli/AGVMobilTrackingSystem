package com.example.mobilagv_v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

/**
 * The AsyncConnection class is an AsyncTask that can be used to open a socket connection with a server and to write/read data asynchronously.
 *
 * The socket connection is initiated in the background thread of the AsyncTask which will stay alive reading data in a while loop
 * until disconnect() method is called from outside or the connection has been lost.
 *
 * When the socket reads data it sends it to the ConnectionHandler via didReceiveData() method in the same thread of AsyncTask.
 * To write data to the server call write() method from outside thread. As the input and output streams are separate there will be no problem with synchronisation.
 *
 * A useful tip: if you wish to avoid connection timeout to happen while the application is inactive try to write some meaningless data periodically as a heartbeat.
 * A useful tip: if you wish to keep the connection alivdidConnecte for longer that the activity  life cycle than consider using services.
 *
 * Created by StarWheel on 10/08/13.
 *
 */
public class AsyncConnection extends android.os.AsyncTask<Void, String, Exception> {
    private String url;
    private int port;
    private int timeout;
    private ConnectionHandler connectionHandler;

    private BufferedReader in;
    private BufferedWriter out;
    private Socket socket;
    private boolean interrupted = false;

    private String TAG = getClass().getName();

    public AsyncConnection(String url, int port, int timeout, ConnectionHandler connectionHandler) {
        this.url = url;
        this.port = port;
        this.timeout = timeout;
        this.connectionHandler = connectionHandler;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Exception result) {
        super.onPostExecute(result);
        Log.d(TAG, "Finished communication with the socket. Result = " + result);
        //TODO If needed move the didDisconnect(error); method call here to implement it on UI thread.
    }

    @Override
    protected Exception doInBackground(Void... params) {
        Exception error = null;

        try {
            Log.d(TAG, "Opening socket connection.");
            socket = new Socket();
            socket.connect(new InetSocketAddress(url, port), timeout);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            connectionHandler.didConnect();

            while(!interrupted) {
                String line = in.readLine();
                //Log.d(TAG, "Received:" + line);
                connectionHandler.didReceiveData(line);
            }
        } catch (UnknownHostException ex) {
            Log.e(TAG, "doInBackground(): " + ex.toString());
            error = interrupted ? null : ex;
        } catch (IOException ex) {
            Log.d(TAG, "doInBackground(): " + ex.toString());
            error = interrupted ? null : ex;
        } catch (Exception ex) {
            Log.e(TAG, "doInBackground(): " + ex.toString());
            error = interrupted ? null : ex;
        } finally {
            try {
                socket.close();
                out.close();
                in.close();
            } catch (Exception ex) {}
        }

        connectionHandler.didDisconnect(error);
        return error;
    }

    public void write(final String data) {
        try {
            Log.d(TAG, "writ(): data = " + data);
            out.write(data + "\n");
            out.flush();
        } catch (IOException ex) {
            Log.e(TAG, "write(): " + ex.toString());
        } catch (NullPointerException ex) {
            Log.e(TAG, "write(): " + ex.toString());
        }
    }

    public void disconnect() {
        try {
            Log.d(TAG, "Closing the socket connection.");

            interrupted = true;
            if(socket != null) {
                socket.close();
            }
            if(out != null & in != null) {
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            Log.e(TAG, "disconnect(): " + ex.toString());
        }
    }
}