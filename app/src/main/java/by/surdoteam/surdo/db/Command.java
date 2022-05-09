package by.surdoteam.surdo.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Command {
    private String word;
    private String path;
    private boolean primary;

    @PrimaryKey(autoGenerate = true)
    private long id;

    public Command(String word, String path) {
        this.word = word;
        this.path = path;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
