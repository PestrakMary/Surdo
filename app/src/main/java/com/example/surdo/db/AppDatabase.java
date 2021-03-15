package com.example.surdo.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.surdo.R;

@Database(entities = {Command.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CommandDao CommandDao();

    public void initializeDB() {
        this.CommandDao().deleteAll();
        this.CommandDao().insert(new Command("поворот направо", R.raw.turn_right));
        this.CommandDao().insert(new Command("поворот налево", R.raw.turn_left));
        this.CommandDao().insert(new Command("тормоз", R.raw.brake));
        this.CommandDao().insert(new Command("газ", R.raw.gas));
        this.CommandDao().insert(new Command("автомобиль", R.raw.car));
    }
}