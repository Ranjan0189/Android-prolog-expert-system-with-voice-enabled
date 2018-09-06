package com.example.root.myapplication90;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity{

    ListPreference m_updateList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


}