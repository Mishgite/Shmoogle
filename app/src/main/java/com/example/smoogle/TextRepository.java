package com.example.smoogle;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;

public class TextRepository {
    private TextRecordDao dao;

    public TextRepository(Context context) {
        AppDatabase db = Room.databaseBuilder(context,
                AppDatabase.class, "text-db").build();
        dao = db.textRecordDao();
    }

    public void saveRecord(TextRecord record) {
        new Thread(() -> dao.insert(record)).start();
    }

    public void updateRecord(TextRecord record) {
        new Thread(() -> dao.update(record)).start();
    }

    public void deleteRecord(TextRecord record) {
        new Thread(() -> dao.delete(record)).start();
    }
    public LiveData<List<TextRecord>> getAllRecords() {
        return dao.getAllRecords();
    }
}