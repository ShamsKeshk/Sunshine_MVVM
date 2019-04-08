package com.example.android.sunshine.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.print.PrinterId;
import android.support.annotation.Nullable;

import com.example.android.sunshine.AppExecutor;
import com.example.android.sunshine.data.database.ListWeatherEntry;
import com.example.android.sunshine.data.database.WeatherDao;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.data.network.WeatherNetworkDataSource;
import com.example.android.sunshine.utilities.SunshineDateUtils;

import java.util.Date;
import java.util.List;

public class SunshineRepository {

    private static final String LOG_TAG = SunshineRepository.class.getSimpleName();

    private static SunshineRepository sunshineRepository;
    private static final Object LOCK = new Object();

    private final AppExecutor appExecutor;
    private final WeatherNetworkDataSource weatherNetworkDataSource;
    private final WeatherDao weatherDao;

    private SunshineRepository(final WeatherDao weatherDao,
                               WeatherNetworkDataSource weatherNetworkDataSource,
                               final AppExecutor appExecutor){
        this.weatherDao = weatherDao;
        this.weatherNetworkDataSource = weatherNetworkDataSource;
        this.appExecutor = appExecutor;

        LiveData<WeatherEntry[]> networkData =
                weatherNetworkDataSource.getCurrentWeatherForecasts();
        networkData.observeForever(new Observer<WeatherEntry[]>() {
            @Override
            public void onChanged(@Nullable final WeatherEntry[] weatherEntries) {
                appExecutor.getDiskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        deleteOldData();

                        weatherDao.bulkInsert(weatherEntries);
                    }
                });
            }
        });

    }


    public synchronized static SunshineRepository getInstance(WeatherDao weatherDao,
                                                 WeatherNetworkDataSource weatherNetworkDataSource,
                                                 AppExecutor appExecutor){
        if (sunshineRepository == null){
            synchronized (LOCK){
                sunshineRepository = new SunshineRepository(weatherDao,weatherNetworkDataSource,appExecutor);
            }
        }
        return sunshineRepository ;
    }

    public LiveData<List<ListWeatherEntry>> getCurrentWeatherForecasts() {
        weatherNetworkDataSource.initialize();
        Date today = SunshineDateUtils.getNormalizedUtcDateForToday();
        return weatherDao.getCurrentWeatherForecasts(today);
    }

    public LiveData<WeatherEntry> getWeatherByDate(Date date) {
        weatherNetworkDataSource.initialize();
        return weatherDao.getWeatherByDate(date);
    }

    /**
     * Deletes old weather data because we don't need to keep multiple days' data
     */
    private void deleteOldData() {
        Date today = SunshineDateUtils.getNormalizedUtcDateForToday();
        weatherDao.deleteOldWeather(today);
    }


}
