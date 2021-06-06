package by.surdoteam.surdo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.room.Room;

import com.google.android.material.navigation.NavigationView;

import by.surdoteam.surdo.db.AppDatabase;
import by.surdoteam.surdo.fragments.AboutFragment;
import by.surdoteam.surdo.fragments.LibFragment;
import by.surdoteam.surdo.fragments.RecognizeFragment;
import by.surdoteam.surdo.fragments.SettingsFragment;

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
    private SettingsFragment settingsFragment = null;
    private AboutFragment aboutFragment = null;
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

        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.
                setNavigationItemSelectedListener
                        (menuItem -> {
                            int id = menuItem.getItemId();
                            if (id == R.id.recognizer_settings) {
                                getSupportFragmentManager().
                                        beginTransaction().
                                        replace(R.id.fragmentContainer,
                                                getRecognizeFragment())
                                        .addToBackStack(null)
                                        .commit();
                            } else if (id == R.id.library_settings) {
                                getSupportFragmentManager().
                                        beginTransaction().
                                        replace(R.id.fragmentContainer,
                                                getLibFragment())
                                        .addToBackStack(null)
                                        .commit();
                            } else if (id == R.id.action_settings) {
                                getSupportFragmentManager().
                                        beginTransaction().
                                        replace(R.id.fragmentContainer,
                                                getSettingsFragment())
                                        .addToBackStack(null)
                                        .commit();
                            } else if (id == R.id.about_settings) {
                                getSupportFragmentManager().
                                        beginTransaction().
                                        replace(R.id.fragmentContainer,
                                                getAboutFragment())
                                        .addToBackStack(null)
                                        .commit();
                            } else {
                                return false;
                            }
                            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

                            drawer.closeDrawer(GravityCompat.START, true);
                            return true;
                        });

        readLibrary();

        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "library")
                .allowMainThreadQueries()
                .build();
        database.initializeDB(library);

        FragmentManager fm = getSupportFragmentManager();
        recognizeFragment = (RecognizeFragment) fm.findFragmentById(R.id.fragmentContainer);
        if (recognizeFragment == null) {
            recognizeFragment = new RecognizeFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, recognizeFragment)
                    .addToBackStack(null)
                    .commit();
        }
        libFragment = (LibFragment) fm.findFragmentById(R.id.fragmentContainer);
        settingsFragment = (SettingsFragment) fm.findFragmentById(R.id.fragmentContainer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.recognizer_settings:
                getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.fragmentContainer,
                                getRecognizeFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.library_settings:
                getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.fragmentContainer,
                                getLibFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.action_settings:
                getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.fragmentContainer,
                                getSettingsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.about_settings:
                getSupportFragmentManager().
                        beginTransaction().
                        replace(R.id.fragmentContainer,
                                getAboutFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    public AboutFragment getAboutFragment() {
        if (aboutFragment == null) {
            aboutFragment = new AboutFragment();
        }
        return aboutFragment;
    }

    public SettingsFragment getSettingsFragment() {
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }
        return settingsFragment;
    }
}