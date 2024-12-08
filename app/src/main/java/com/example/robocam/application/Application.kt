package com.example.robocam.application

import android.app.Application

class MyApplication :Application(){

    var instance: MyApplication?=null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}