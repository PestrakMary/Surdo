package com.example.surdo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import androidx.room.Room;

import com.example.surdo.db.AppDatabase;

public class MainActivity extends AppCompatActivity {
    private AppDatabase database = null;
    private LibFragment libFragment = null;
    private RecognizeFragment recognizeFragment = null;

    public AppDatabase getDatabase() {
        return database;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "library")
                .allowMainThreadQueries()
                .build();
        database.initializeDB();

        FragmentManager fm = getSupportFragmentManager();
        recognizeFragment = (RecognizeFragment) fm.findFragmentById(R.id.fragmentContainer);
        libFragment = (LibFragment) fm.findFragmentById(R.id.fragmentContainer);
        if (recognizeFragment == null) {
            recognizeFragment = new RecognizeFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, recognizeFragment)
                    .commit();
        }
    }

    public LibFragment getLibFragment() {
        if (libFragment == null) {
            libFragment = new LibFragment();
        }
        return libFragment;
    }

    public RecognizeFragment getRecognizeFragment() {
        if (recognizeFragment == null) {
            recognizeFragment = new RecognizeFragment();
        }
        return recognizeFragment;
    }
}