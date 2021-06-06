package by.surdoteam.surdo.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import by.surdoteam.surdo.R;


public class SettingsFragment extends PreferenceFragmentCompat {

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//        sharedPref.registerOnSharedPreferenceChangeListener((prefs, key) -> Toast.makeText(requireActivity().getApplicationContext(), R.string.restart_app, Toast.LENGTH_LONG).show());
//    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }
}
