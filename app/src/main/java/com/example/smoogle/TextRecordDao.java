package com.example.smoogle;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TextRecordDao {
    @Insert
    void insert(TextRecord record);

    @Update
    void update(TextRecord record);

    @Delete
    void delete(TextRecord record);

    @Query("SELECT * FROM text_records ORDER BY timestamp DESC")
    LiveData<List<TextRecord>> getAllRecords();
}