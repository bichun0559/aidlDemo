package com.example.ipcdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ipcdemo.entity.Message;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_connect;
    private Button btn_disconnect;
    private Button btn_isConnected;

    private Button btn_sendMessage;
    private Button btn_register;
    private Button btn_unRegister;

    private IConnectionService connectionServiceProxy;
    private IMessageService messageServiceProxy;
    private IServiceManager serviceManagerProxy;

    private MessageReceiveListener messageReceiveListener = new MessageReceiveListener.Stub() {
        @Override
        public void onReceiveMessage(final Message message) throws RemoteException {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, message.getContent(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_connect = findViewById(R.id.btn_connect);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_isConnected = findViewById(R.id.btn_isConnected);

        btn_connect.setOnClickListener(this);
        btn_disconnect.setOnClickListener(this);
        btn_isConnected.setOnClickListener(this);

        btn_sendMessage = findViewById(R.id.btn_sendMessage);
        btn_register = findViewById(R.id.btn_register);
        btn_unRegister = findViewById(R.id.btn_unRegister);

        btn_sendMessage.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        btn_unRegister.setOnClickListener(this);


        Intent intent = new Intent(this, RemoteService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                try {
                    serviceManagerProxy = IServiceManager.Stub.asInterface(iBinder);
                    connectionServiceProxy = IConnectionService.Stub.asInterface(serviceManagerProxy.getService(IConnectionService.class.getSimpleName()));
                    messageServiceProxy = IMessageService.Stub.asInterface(serviceManagerProxy.getService(IMessageService.class.getSimpleName()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                try {
                    connectionServiceProxy.connect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_disconnect:
                try {
                    connectionServiceProxy.disConnect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_isConnected:
                boolean isConnected = false;
                try {
                    isConnected = connectionServiceProxy.isConnected();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, String.valueOf(isConnected), Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_sendMessage:
                Message message = new Message();
                message.setContent("message send from main");
                try {
                    messageServiceProxy.sendMessage(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_register:
                try {
                    messageServiceProxy.registerMessageReceiverListener(messageReceiveListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_unRegister:
                try {
                    messageServiceProxy.unRegisterMessageReceiverListener(messageReceiveListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
