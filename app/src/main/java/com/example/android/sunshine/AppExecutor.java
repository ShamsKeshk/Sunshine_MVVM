package com.example.android.sunshine;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutor {

    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor mainThread;

    private static final Object LOCK = new Object();

    private static AppExecutor appExecutor;

    private AppExecutor(Executor diskIO,Executor networkIO,Executor mainThread){
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public static AppExecutor getInstance(){
        if (appExecutor == null){
            synchronized (LOCK){
                appExecutor = new AppExecutor(Executors.newSingleThreadExecutor(),
                        Executors.newFixedThreadPool(3),
                        new MainTHreadExector());
            }
        }
        return appExecutor;
    }

    public Executor getDiskIO() {
        return diskIO;
    }

    public Executor getNetworkIO() {
        return networkIO;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    public static class MainTHreadExector implements Executor {

        final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }
}
