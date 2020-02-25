package com.example.ipcdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.widget.Toast;

import com.example.ipcdemo.entity.Message;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RemoteService extends Service {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean isConnected = false;

    private RemoteCallbackList<MessageReceiveListener> messageReceiveListenerRemoteCallbackList = new RemoteCallbackList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;  //定时任务

    private ScheduledFuture scheduledFuture;

    IConnectionService connectionService = new IConnectionService.Stub() {
        @Override
        public void connect() throws RemoteException {
            try {
                Thread.sleep(5000);
                isConnected = true;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RemoteService.this, "connect", Toast.LENGTH_SHORT).show();
                    }
                });

                scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        int size = messageReceiveListenerRemoteCallbackList.beginBroadcast();
                        for (int i = 0; i < size; i++) {
                            Message message = new Message();
                            message.setContent("this message from remote");
                            try {
                                messageReceiveListenerRemoteCallbackList.getBroadcastItem(i).onReceiveMessage(message);  //将子进程收到的消息回调给主进程
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        messageReceiveListenerRemoteCallbackList.finishBroadcast();
                    }
                }, 5000, 5000, TimeUnit.MILLISECONDS); //在连接建立成功之后，通过定时任务模拟消息接收的过程，每隔5s收到一条消息
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disConnect() throws RemoteException {
            isConnected = false;
            scheduledFuture.cancel(true);  //断连之后，停止定时任务
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RemoteService.this, "disConnect", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public boolean isConnected() throws RemoteException {
            return isConnected;
        }
    };

    private IMessageService messageService = new IMessageService.Stub() {
        @Override
        public void sendMessage(final Message message) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RemoteService.this, message.getContent(), Toast.LENGTH_SHORT).show();
                }
            });

            if (isConnected) {
                message.setSendSuccess(true);
            } else {
                message.setSendSuccess(false);
            }
        }

        @Override
        public void registerMessageReceiverListener(MessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null) {
                messageReceiveListenerRemoteCallbackList.register(messageReceiveListener);
            }
        }

        @Override
        public void unRegisterMessageReceiverListener(MessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null) {
                messageReceiveListenerRemoteCallbackList.unregister(messageReceiveListener);
            }
        }
    };

    private IServiceManager serviceManager = new IServiceManager.Stub() {
        @Override
        public IBinder getService(String ServiceName) throws RemoteException {
            if (IConnectionService.class.getSimpleName().equals(ServiceName)) {
                return connectionService.asBinder();
            } else if (IMessageService.class.getSimpleName().equals(ServiceName)) {
                return messageService.asBinder();
            } else {
                return null;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return serviceManager.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1); //初始化定时任务
    }
}
