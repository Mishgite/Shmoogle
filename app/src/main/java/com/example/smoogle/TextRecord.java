package com.example.smoogle;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "text_records")
public class TextRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    public TextRecord(String content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
}