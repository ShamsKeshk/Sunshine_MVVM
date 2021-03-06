/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.data.network.sync;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.example.android.sunshine.AppExecutor;
import com.example.android.sunshine.data.database.SunshinePreferences;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.data.network.NetworkUtils;
import com.example.android.sunshine.data.network.OpenWeatherJsonUtils;
import com.example.android.sunshine.data.network.WeatherResponse;
import com.example.android.sunshine.utilities.NotificationUtils;

import java.net.URL;

public class SunshineSyncTask {

    /**
     * Performs the network request for updated weather, parses the JSON from that request, and
     * inserts the new weather information into our ContentProvider. Will notify the user that new
     * weather has been loaded if the user hasn't been notified of the weather within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */

    private static final String LOG_TAG = SunshineSyncTask.class.getSimpleName();

    public static void syncWeather(final Context context,
                                   AppExecutor executor,
                                   final MutableLiveData<WeatherEntry[]> mDownloadedWeatherForecasts) {

        executor.getNetworkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    /*
                     * The getUrl method will return the URL that we need to get the forecast JSON for the
                     * weather. It will decide whether to create a URL based off of the latitude and
                     * longitude or off of a simple location as a String.
                     */
                    URL weatherRequestUrl = NetworkUtils.getUrl(context);

                    /* Use the URL to retrieve the JSON */
                    String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

                    /* Parse the JSON into a list of weather values */
                    WeatherResponse weatherValues = OpenWeatherJsonUtils
                            .parse(jsonWeatherResponse);
                    Log.d(LOG_TAG, "JSON Parsing finished");



                    // As long as there are weather forecasts, update the LiveData storing the most recent
                    // weather forecasts. This will trigger observers of that LiveData, such as the
                    // SunshineRepository.




                    /*
                     * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
                     * would have returned null. We need to check for those cases here to prevent any
                     * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
                     * there isn't any to insert.
                     */
                    if (weatherValues != null && weatherValues.getWeatherForecast().length != 0) {
                        /* Get a handle on the ContentResolver to delete and insert data */
                        Log.d(LOG_TAG, "JSON not null and has " + weatherValues.getWeatherForecast().length
                                + " values");

                        Log.d(LOG_TAG, String.format("First value is %1.0f and %1.0f",
                                weatherValues.getWeatherForecast()[0].getMin(),
                                weatherValues.getWeatherForecast()[0].getMax()));

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mDownloadedWeatherForecasts.postValue(weatherValues.getWeatherForecast());

                        // If the code reaches this point, we have successfully performed our sync

                        /*
                         * Finally, after we insert data into the ContentProvider, determine whether or not
                         * we should notify the user that the weather has been refreshed.
                         */
                        boolean notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context);

                        /*
                         * If the last notification was shown was more than 1 day ago, we want to send
                         * another notification to the user that the weather has been updated. Remember,
                         * it's important that you shouldn't spam your users with notifications.
                         */
                        long timeSinceLastNotification = SunshinePreferences
                                .getEllapsedTimeSinceLastNotification(context);

                        boolean oneDayPassedSinceLastNotification = false;

                        if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                            oneDayPassedSinceLastNotification = true;
                        }

                        /*
                         * We only want to show the notification if the user wants them shown and we
                         * haven't shown a notification in the past day.
                         */
                        if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                            NotificationUtils.notifyUserOfNewWeather(context);
                        }

                        /* If the code reaches this point, we have successfully performed our sync */

                    }

                } catch (Exception e) {
                    /* Server probably invalid */
                    e.printStackTrace();
                }
            }
        });


    }
}