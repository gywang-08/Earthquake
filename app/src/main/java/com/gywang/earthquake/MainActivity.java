package com.gywang.earthquake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    static final private int MENU_PREFERENCES = Menu.FIRST + 1;
    static final private int MENU_UPDATE = Menu.FIRST + 2;

    private static final int SHOW_PREFERENCES = 1;

    private int minimumMagnitude = 0;
    private boolean autoUpdateChecked = false;
    private int updateFreq = 0;

    public int getMinimumMagnitude() {
        return minimumMagnitude;
    }

    public void setMinimumMagnitude(int minimumMagnitude) {
        this.minimumMagnitude = minimumMagnitude;
    }

    public boolean isAutoUpdateChecked() {
        return autoUpdateChecked;
    }

    public void setAutoUpdateChecked(boolean autoUpdateChecked) {
        this.autoUpdateChecked = autoUpdateChecked;
    }

    public int getUpdateFreq() {
        return updateFreq;
    }

    public void setUpdateFreq(int updateFreq) {
        this.updateFreq = updateFreq;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateFromPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case MENU_PREFERENCES:{
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivityForResult(intent,SHOW_PREFERENCES);
                return true;
            }

        }
        return false;
    }

    private void updateFromPreferences(){
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int minMagIndex = prefs.getInt(PreferencesActivity.PREF_MIN_MAG_INDEX,0);
        if(minMagIndex < 0)
            minMagIndex = 0;
        int freqIndex = prefs.getInt(PreferencesActivity.PREF_UPDATE_FREQ_iNDEX,0);
        if(freqIndex < 0)
            freqIndex = 0;

        autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE,false);

        Resources r = getResources();

        String[] minMagValues = r.getStringArray(R.array.magnitude);
        String[] freqValues = r.getStringArray(R.array.update_freq_values);

        minimumMagnitude = Integer.parseInt(minMagValues[minMagIndex]);
        updateFreq = Integer.parseInt(freqValues[freqIndex]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SHOW_PREFERENCES){
            if(resultCode == Activity.RESULT_OK){
                updateFromPreferences();
                FragmentManager fm = getSupportFragmentManager();
                final EarthquakeListFragment earthquakeListFragment =
                        (EarthquakeListFragment)fm.findFragmentById(R.id.EarthquakeListFragment);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        earthquakeListFragment.refreshEarthquakes();
                    }
                });
                thread.start();
            }
        }
    }


}
