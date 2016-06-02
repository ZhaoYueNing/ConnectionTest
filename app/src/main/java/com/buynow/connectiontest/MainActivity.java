package com.buynow.connectiontest;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String XML_FILE_NAME = "data.xml";
    public static final int CHANGE_PORT = 6;
    public static final int DISPLAY_DATA = 8;

    private EditText et_hostname;
    private EditText et_extremity;
    private EditText et_content;
    private EditText et_listener;
    private TextView tv_acceptContent;

    private SharedPreferences mPreferences;
    private ExecutorService mExecutorService;

    private Handler handle = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISPLAY_DATA:
                    String data = (String) msg.obj;
                    String str = tv_acceptContent.getText().toString();
                    tv_acceptContent.setText(str+"\n"+data  );
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init
        et_extremity = (EditText) findViewById(R.id.et_extremity);
        et_hostname = (EditText) findViewById(R.id.et_hostname);
        et_content = (EditText) findViewById(R.id.et_content);
        et_listener = (EditText)findViewById(R.id.et_listenerExtremity);
        tv_acceptContent = (TextView) findViewById(R.id.tv_acceptContent);


        mPreferences = getSharedPreferences(XML_FILE_NAME,0);
        String hostname = mPreferences.getString(getString(R.string.hostname), "");
        String extremity = mPreferences.getString(getString(R.string.extremity),"");
        String content = mPreferences.getString(getString(R.string.content), "");
        String listener = mPreferences.getString(getString(R.string.listener), "8080");
        et_extremity.setText(extremity);
        et_hostname.setText(hostname);
        et_content.setText(content);
        et_listener.setText(listener);
        mExecutorService = Executors.newCachedThreadPool();

        Log.d("TAGMY", getPsdnIp());


        new Thread(new Runnable() {
            @Override
            public void run() {
                Handler myHandle = new Handler();

                int extremity = 8080;
                try {
                    ServerSocket serverSocket = new ServerSocket(extremity);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        InputStream in= socket.getInputStream();

                        int i = -1;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while ((i = in.read()) != -1) {
                            baos.write(i);
                        }
                        String content = baos.toString();
                        socket.close();
                        baos.close();
                        in.close();
                        Message msg = Message.obtain();
                        msg.what = MainActivity.DISPLAY_DATA;
                        msg.obj = content;
                        myHandle.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        accept();

    }

    public void send(View v) {
        final String hostname = et_hostname.getText().toString();
        final String extremity = et_extremity.getText().toString();
        final String content = et_content.getText().toString();

        //send
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Client：Connecting");
                    //IP地址和端口号（对应服务端），我这的IP是本地路由器的IP地址
                    Socket socket = new Socket(hostname, Integer.parseInt(extremity));

                    //发送给服务端的消息
                    String message = content;
                    try {
                        System.out.println("Client Sending: '" + message + "'");

                        //第二个参数为True则为自动flush
                        PrintWriter out = new PrintWriter(
                                new BufferedWriter(new OutputStreamWriter(
                                        socket.getOutputStream())), true);
                        out.println(message);
//                      out.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        //关闭Socket
                        socket.close();
                        System.out.println("Client:Socket closed");
                    }
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    //监听端口 接受 数据
    private void accept() {
        String ip = et_hostname.getText().toString();
        String ex = et_extremity.getText().toString();
        new AcceptDataThread(ip, 8080,handle).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //save data
        String hostname = et_hostname.getText().toString();
        String extremity = et_extremity.getText().toString();
        String content = et_content.getText().toString();
        String listener = et_listener.getText().toString();

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(getString(R.string.extremity), extremity);
        editor.putString(getString(R.string.hostname), hostname);
        editor.putString(getString(R.string.content),content);
        editor.putString(getString(R.string.listener), listener);
        editor.commit();
    }
    public static String getPsdnIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        //if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }
}
