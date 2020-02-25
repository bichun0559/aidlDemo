package com.example.ipcdemo;

interface IConnectionService {

   oneway void connect();

   void disConnect();

   boolean isConnected();

}
