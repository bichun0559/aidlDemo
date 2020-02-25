package com.example.ipcdemo;
import com.example.ipcdemo.entity.Message;

interface MessageReceiveListener {

    void onReceiveMessage(in Message message);

}
