package com.example.android.sunshine.data.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.example.android.sunshine.AppExecutor;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.data.network.sync.SunshineSyncTask;
import com.example.android.sunshine.data.network.sync.SunshineSyncUtils;

public class WeatherNetworkDataSource {

    public static final String LOG_TAG = WeatherNetworkDataSource.class.getSimpleName();

    private static WeatherNetworkDataSource weatherNetworkDataSource;
    public static final Object LOCK = new Object();
    private final AppExecutor appExecutor;
    private final Context mContext;
    private final MutableLiveData<WeatherEntry[]> mDownloadedWeatherForecasts;


    private WeatherNetworkDataSource(Context context, AppExecutor appExecutor){
        this.appExecutor = appExecutor;
        this.mContext = context;
        this.mDownloadedWeatherForecasts = new MutableLiveData<>();
    }

    public static WeatherNetworkDataSource getInstance(Context context,AppExecutor appExecutor){
        if (weatherNetworkDataSource == null){
            synchronized (LOCK){
                weatherNetworkDataSource = new WeatherNetworkDataSource(context,appExecutor);
            }
        }

        return weatherNetworkDataSource;
    }

    public LiveData<WeatherEntry[]> getCurrentWeatherForecasts() {
        return mDownloadedWeatherForecasts;
    }

    void fetchWeather(){
        SunshineSyncTask.syncWeather(mContext,appExecutor,mDownloadedWeatherForecasts);
    }

    public void scheduleRecurringFetchWeatherSync(){
        SunshineSyncUtils.scheduleFirebaseJobDispatcherSync(mContext);
    }

    public void initialize(){
        SunshineSyncUtils.initialize(mContext,appExecutor);
    }

    public void startFetchWeatherService(){
        SunshineSyncUtils.startImmediateSync(mContext);
    }




}
