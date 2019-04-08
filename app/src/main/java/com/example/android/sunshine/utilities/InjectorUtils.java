package com.example.android.sunshine.utilities;

import android.content.Context;

import com.example.android.sunshine.AppExecutor;
import com.example.android.sunshine.data.SunshineRepository;
import com.example.android.sunshine.data.database.SunshineDatabase;
import com.example.android.sunshine.data.database.WeatherDao;
import com.example.android.sunshine.data.network.WeatherNetworkDataSource;
import com.example.android.sunshine.ui.weather_detail.DetailViewModelFactory;
import com.example.android.sunshine.ui.weather_list.MainViewModelFactory;

import java.util.Date;

public class InjectorUtils {



    public static SunshineRepository provideRepository(Context context){
        WeatherDao weatherDao = provideSunshineDatabase(context).weatherDao();
        return SunshineRepository.getInstance(weatherDao,provideNetworkDataSource(context),provideAppExecutor());
    }

    public static SunshineDatabase provideSunshineDatabase(Context context){
        return SunshineDatabase.getInstance(context);
    }

    public static WeatherNetworkDataSource provideNetworkDataSource(Context context){
        return WeatherNetworkDataSource.getInstance(context,provideAppExecutor());
    }

    public static DetailViewModelFactory provideDetailViewModelFactory(Context context, Date date) {
        SunshineRepository repository = provideRepository(context.getApplicationContext());
        return new DetailViewModelFactory(repository, date);
    }

    public static MainViewModelFactory provideMainActivityViewModelFactory(Context context) {
        SunshineRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }


    private static AppExecutor provideAppExecutor(){
        return AppExecutor.getInstance();
    }
}
