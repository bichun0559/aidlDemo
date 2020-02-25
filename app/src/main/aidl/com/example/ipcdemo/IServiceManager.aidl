package com.example.ipcdemo;

interface IServiceManager {
    IBinder getService(String ServiceName);
}
