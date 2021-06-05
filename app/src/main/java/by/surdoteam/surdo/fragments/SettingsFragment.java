package by.surdoteam.surdo.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import by.surdoteam.surdo.R;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }
}
