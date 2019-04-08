/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.sunshine.ui.weather_detail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.sunshine.R;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.databinding.ActivityDetailBinding;
import com.example.android.sunshine.ui.setting.SettingsActivity;
import com.example.android.sunshine.utilities.InjectorUtils;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    /*
     * In this Activity, you can share the selected day's forecast. No social sharing is complete
     * without using a hashtag. #BeTogetherNotTheSame
     */
    public static final String EXTRA_WEATHER_ID_WHEN_NOTIFICATION_CLICKED = "weather_details_notification_id";
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    /*
     * This ID will be used to identify the Loader responsible for loading the weather details
     * for a particular day. In some cases, one Activity can deal with many Loaders. However, in
     * our case, there is only one. We will still use this ID to initialize the loader and create
     * the loader for best practice. Please note that 353 was chosen arbitrarily. You can use
     * whatever number you like, so long as it is unique and consistent.
     */

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private String mForecastSummary;

    /* The URI that is used to access the chosen day's weather details */
    private long mWeatherDate;


    /*
     * This field is used for data binding. Normally, we would have to call findViewById many
     * times to get references to the Views in this Activity. With data binding however, we only
     * need to call DataBindingUtil.setContentView and pass in a Context and a layout, as we do
     * in onCreate of this class. Then, we can access all of the Views in our layout
     * programmatically without cluttering up the code with findViewById.
     */
    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mWeatherDate = getIntent().getLongExtra(EXTRA_WEATHER_ID_WHEN_NOTIFICATION_CLICKED,0);
        if (mWeatherDate <= 0 ) throw new NullPointerException("URI for DetailActivity cannot be null");

        /* This connects our Activity into the loader lifecycle. */
        Date date = new Date(mWeatherDate);

        DetailActivityViewModel detailActivityViewModel =
                ViewModelProviders.of(this,InjectorUtils.provideDetailViewModelFactory(getApplicationContext(),
                        date)).get(DetailActivityViewModel.class);

        detailActivityViewModel.getWeather().observe(this, new Observer<WeatherEntry>() {
            @Override
            public void onChanged(@Nullable WeatherEntry weatherEntry) {
                bindDataToViews(weatherEntry);
            }
        });

    }

    private void bindDataToViews(WeatherEntry weatherEntry) {
        boolean cursorHasValidData = false;
        if (weatherEntry == null) return;


            /****************
             * Weather Icon *
             ****************/
            /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
            int weatherId = weatherEntry.getWeatherIconId();
            /* Use our utility method to determine the resource ID for the proper art */
            int weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

            /* Set the resource ID on the icon to display the art */
            mDetailBinding.primaryInfo.weatherIcon.setImageResource(weatherImageId);

            /****************
             * Weather Date *
             ****************/
            /*
             * Read the date from the cursor. It is important to note that the date from the cursor
             * is the same date from the weather SQL table. The date that is stored is a GMT
             * representation at midnight of the date when the weather information was loaded for.
             *
             * When displaying this date, one must add the GMT offset (in milliseconds) to acquire
             * the date representation for the local date in local time.
             * SunshineDateUtils#getFriendlyDateString takes care of this for us.
             */
            long localDateMidnightGmt = weatherEntry.getDate().getTime();
            String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);

            mDetailBinding.primaryInfo.date.setText(dateText);

            /***********************
             * Weather Description *
             ***********************/
            /* Use the weatherId to obtain the proper description */
            String description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);

            /* Create the accessibility (a11y) String from the weather description */
            String descriptionA11y = getString(R.string.a11y_forecast, description);

            /* Set the text and content description (for accessibility purposes) */
            mDetailBinding.primaryInfo.weatherDescription.setText(description);
            mDetailBinding.primaryInfo.weatherDescription.setContentDescription(descriptionA11y);

            /* Set the content description on the weather image (for accessibility purposes) */
            mDetailBinding.primaryInfo.weatherIcon.setContentDescription(descriptionA11y);

            /**************************
             * High (max) temperature *
             **************************/
            /* Read high temperature from the cursor (in degrees celsius) */
            double highInCelsius = weatherEntry.getMax();
            /*
             * If the user's preference for weather is fahrenheit, formatTemperature will convert
             * the temperature. This method will also append either °C or °F to the temperature
             * String.
             */
            String highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius);

            /* Create the accessibility (a11y) String from the weather description */
            String highA11y = getString(R.string.a11y_high_temp, highString);

            /* Set the text and content description (for accessibility purposes) */
            mDetailBinding.primaryInfo.highTemperature.setText(highString);
            mDetailBinding.primaryInfo.highTemperature.setContentDescription(highA11y);

            /*************************
             * Low (min) temperature *
             *************************/
            /* Read low temperature from the cursor (in degrees celsius) */
            double lowInCelsius = weatherEntry.getMin();
            /*
             * If the user's preference for weather is fahrenheit, formatTemperature will convert
             * the temperature. This method will also append either °C or °F to the temperature
             * String.
             */
            String lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius);

            String lowA11y = getString(R.string.a11y_low_temp, lowString);

            /* Set the text and content description (for accessibility purposes) */
            mDetailBinding.primaryInfo.lowTemperature.setText(lowString);
            mDetailBinding.primaryInfo.lowTemperature.setContentDescription(lowA11y);

            /************
             * Humidity *
             ************/
            /* Read humidity from the cursor */
            double humidity = weatherEntry.getHumidity();
            String humidityString = getString(R.string.format_humidity, humidity);

            String humidityA11y = getString(R.string.a11y_humidity, humidityString);

            /* Set the text and content description (for accessibility purposes) */
            mDetailBinding.extraDetails.humidity.setText(humidityString);
            mDetailBinding.extraDetails.humidity.setContentDescription(humidityA11y);

            mDetailBinding.extraDetails.humidityLabel.setContentDescription(humidityA11y);

            /****************************
             * Wind speed and direction *
             ****************************/
            /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
            double windSpeed = weatherEntry.getWind();
            double windDirection = weatherEntry.getDegrees();
            String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);

            String windA11y = getString(R.string.a11y_wind, windString);

            /* Set the text and content description (for accessibility purposes) */
            mDetailBinding.extraDetails.windMeasurement.setText(windString);
            mDetailBinding.extraDetails.windMeasurement.setContentDescription(windA11y);

            mDetailBinding.extraDetails.windLabel.setContentDescription(windA11y);

            /************
             * Pressure *
             ************/
            /* Read pressure from the cursor */
            double pressure = weatherEntry.getPressure();

            /*
             * Format the pressure text using string resources. The reason we directly access
             * resources using getString rather than using a method from SunshineWeatherUtils as
             * we have for other data displayed in this Activity is because there is no
             * additional logic that needs to be considered in order to properly display the
             * pressure.
             */
            String pressureString = getString(R.string.format_pressure, pressure);

            String pressureA11y = getString(R.string.a11y_pressure, pressureString);

            /* Set the text and content description (for accessibility purposes) */
            mDetailBinding.extraDetails.pressure.setText(pressureString);
            mDetailBinding.extraDetails.pressure.setContentDescription(pressureA11y);

            mDetailBinding.extraDetails.pressureLabel.setContentDescription(pressureA11y);

            /* Store the forecast summary String in our forecast summary field to share later */
            mForecastSummary = String.format("%s - %s - %s/%s",
                    dateText, description, highString, lowString);
    }

        /**
         * This is where we inflate and set up the menu for this Activity.
         *
         * @param menu The options menu in which you place your items.
         *
         * @return You must return true for the menu to be displayed;
         *         if you return false it will not be shown.
         *
         * @see android.app.Activity#onPrepareOptionsMenu(Menu)
         * @see #onOptionsItemSelected
         */
        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
            MenuInflater inflater = getMenuInflater();
            /* Use the inflater's inflate method to inflate our menu layout to this menu */
            inflater.inflate(R.menu.detail, menu);
            /* Return true so that the menu is displayed in the Toolbar */
            return true;
        }

        /**
         * Callback invoked when a menu item was selected from this Activity's menu. Android will
         * automatically handle clicks on the "up" button for us so long as we have specified
         * DetailActivity's parent Activity in the AndroidManifest.
         *
         * @param item The menu item that was selected by the user
         *
         * @return true if you handle the menu click here, false otherwise
         */
        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            /* Get the ID of the clicked item */
            int id = item.getItemId();

            /* Settings menu item clicked */
            if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            /* Share menu item clicked */
            if (id == R.id.action_share) {
                Intent shareIntent = createShareForecastIntent();
                startActivity(shareIntent);
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        /**
         * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
         * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
         * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
         *
         * @return the Intent to use to share our weather forecast
         */
        private Intent createShareForecastIntent () {
            Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                    .getIntent();
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            return shareIntent;
        }

}