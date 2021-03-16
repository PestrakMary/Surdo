package com.example.surdo.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import java.util.Map;

@Database(entities = {Command.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CommandDao CommandDao();


    public void initializeDB(Map<String, Integer> library) {

        this.CommandDao().deleteAll();
        for(String key: library.keySet()){
            this.CommandDao().insert(new Command(key, library.get(key)));
        }
    }
}