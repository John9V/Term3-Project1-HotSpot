package com.example.hotspot.ui.settings;

/**
 * Wilson what does this do?
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