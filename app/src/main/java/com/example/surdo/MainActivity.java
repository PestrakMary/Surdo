package com.example.surdo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import androidx.room.Room;

import com.example.surdo.db.AppDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private AppDatabase database = null;
    private LibFragment libFragment = null;
    private RecognizeFragment recognizeFragment = null;
    private final Map<String, Integer> library = new TreeMap<>();

    public AppDatabase getDatabase() {
        return database;
    }

    public void readLibrary() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.lib);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String eachline = bufferedReader.readLine();
            while (eachline != null) {
                // `the words in the file are separated by space`, so to get each words
                String[] words = eachline.split(", ");
                System.out.println(words[0] + "   " + words[1]);
                library.put(words[0], this.getResources().getIdentifier(words[1],
                        "raw", this.getPackageName()));
                eachline = bufferedReader.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readLibrary();

        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "library")
                .allowMainThreadQueries()
                .build();
        database.initializeDB(library);

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