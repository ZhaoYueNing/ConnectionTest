package com.buynow.connectiontest;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Created by buynow on 16-5-31.
 */
public class AcceptDataThread extends Thread {

    private final String mIp;
    private final int mExtremity;
    private final Handler mHandler;

    public AcceptDataThread(String ip, int extremity,Handler uiHandle) {
        super("AcceptDataThread");
        mIp = ip;
        mExtremity = extremity;
        mHandler = uiHandle;
    }
    @Override
    public void run() {
        String ip = mIp;
        int extremity = mExtremity;
        try {
            ServerSocket serverSocket = new ServerSocket(extremity);
            while (true) {
                Log.d("TAGMY", mIp);
                Socket socket = serverSocket.accept();
                InputStream in= socket.getInputStream();
                Log.d("TAGMY", mIp);

                int i = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((i = in.read()) != -1) {
                    baos.write(i);
                }
                String content = baos.toString();
                Log.d("TAGMY", content + "     $$$$$$$$$$");
                socket.close();
                baos.close();
                in.close();
                Message msg = Message.obtain();
                msg.what = MainActivity.DISPLAY_DATA;
                msg.obj = content;
                mHandler.sendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
