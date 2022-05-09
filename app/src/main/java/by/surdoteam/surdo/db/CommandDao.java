package by.surdoteam.surdo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CommandDao {

    @Query("SELECT * FROM command")
    List<Command> getAll();

    @Query("SELECT * FROM command WHERE `primary` = 1")
    List<Command> getPrimary();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Command command);

    @Update
    void update(Command command);

    @Delete
    void delete(Command command);

    @Query("DELETE FROM command")
    void deleteAll();

}
