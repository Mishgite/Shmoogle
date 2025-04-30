package com.example.smoogle;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TextRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TextRecordDao textRecordDao();
}