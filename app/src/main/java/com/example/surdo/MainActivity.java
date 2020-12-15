package com.example.surdo;



import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {
    Map<String, Integer> mHashMap = new HashMap<>();
    List<String> dictionary = new ArrayList<>();
    List<Integer> video = new ArrayList<>();
    private Button buttonRecognize;
    private Button buttonLib;
    private Button buttonSettings;
    private FrameLayout fragmentContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //work with database
        mHashMap.put("поворот направо", R.raw.turn_right);
        mHashMap.put("поворот налево", R.raw.turn_left);
        mHashMap.put("тормоз", R.raw.brake);
        mHashMap.put("газ", R.raw.gas);
        mHashMap.put("автомобиль", R.raw.car);

        dictionary.addAll(mHashMap.keySet());
        video.addAll(mHashMap.values());

        buttonRecognize = (Button) findViewById(R.id.buttonHome);
        buttonLib = (Button) findViewById(R.id.buttonLib);
        buttonSettings = findViewById(R.id.buttonSettings);
        fragmentContainer = (FrameLayout) findViewById(R.id.fragmentContainer);
        buttonRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("text", (ArrayList<String>) dictionary);
                bundle.putIntegerArrayList("video", (ArrayList<Integer>) video);
                bundle.putString("packageName", getPackageName());
                FragmentManager fm = getSupportFragmentManager();

                Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
                if (fragment == null) {
                    fragment = new RecognizeFragment();
                    fragment.setArguments(bundle);
                    fm.beginTransaction()
                            .add(R.id.fragmentContainer, fragment)
                            .commit();
                }
            }
        });
        buttonLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("text", (ArrayList<String>) dictionary);
                bundle.putIntegerArrayList("video", (ArrayList<Integer>) video);
                bundle.putString("packageName", getPackageName());
                FragmentManager fm = getSupportFragmentManager();

                Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
                if (fragment == null) {
                    fragment = new LibFragment();
                    fragment.setArguments(bundle);
                    fm.beginTransaction()
                            .add(R.id.fragmentContainer, fragment)
                            .commit();
                }

            }
        });
    }
}