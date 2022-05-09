package by.surdoteam.surdo.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Command.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CommandDao CommandDao();
}