package com.example.surdo.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Command {
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getPath() {
        return path;
    }

    public void setPath(Integer path) {
        this.path = path;
    }

    public Command(String word, Integer path) {
        this.word = word;
        this.path = path;
    }

    public String word;
    public Integer path;

    @PrimaryKey(autoGenerate = true)
    public long id;
}
