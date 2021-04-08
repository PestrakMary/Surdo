package by.surdoteam.surdo.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import by.surdoteam.surdo.R;


public class SettingsFragment extends Fragment {
    SeekBar seekBar;
    Spinner spinner;
    CheckBox checkBox;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        seekBar = view.findViewById(R.id.seekBarSettingsFragment);
        spinner = view.findViewById(R.id.spinnerSettingsFragment);
        checkBox = view.findViewById(R.id.logCheckBoxSettingsFragment);

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
