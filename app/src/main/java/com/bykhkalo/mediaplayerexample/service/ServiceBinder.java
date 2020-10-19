package com.bykhkalo.mediaplayerexample.service;

import android.app.Service;
import android.os.Binder;

public class ServiceBinder extends Binder {

    private Service service;

    public ServiceBinder(Service service) {
        this.service = service;
    }

    public Service getService(){
        return service;
    }
}



