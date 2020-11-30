package com.example.hotspot.ui.settings;

/**
 * Base settings fragment, helps connect settings
 */

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.example.hotspot.R;
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}