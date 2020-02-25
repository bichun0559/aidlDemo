package com.example.ipcdemo;
import com.example.ipcdemo.entity.Message;
import com.example.ipcdemo.MessageReceiveListener;
//消息服务
interface IMessageService {

    void sendMessage(in Message message);

    void registerMessageReceiverListener(MessageReceiveListener messageReceiveListener);

    void unRegisterMessageReceiverListener(MessageReceiveListener messageReceiveListener);

}
