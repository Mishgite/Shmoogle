package com.example.smoogle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordsActivity extends AppCompatActivity {
    private TextRepository repository;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        repository = new TextRepository(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadRecords();
    }

    private void loadRecords() {
        repository.getAllRecords().observe(this, records -> {
            RecordAdapter adapter = new RecordAdapter(records);
            recyclerView.setAdapter(adapter);
        });
    }

    class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {
        private List<TextRecord> records;

        RecordAdapter(List<TextRecord> records) {
            this.records = records;
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_record, parent, false);
            return new RecordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
            TextRecord record = records.get(position);
            holder.bind(record);
        }

        @Override
        public int getItemCount() {
            return records.size();
        }
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private Button btnAppend, btnReplace, btnDelete;

        RecordViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.record_text);
            btnAppend = itemView.findViewById(R.id.btn_append);
            btnReplace = itemView.findViewById(R.id.btn_replace);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(TextRecord record) {
            textView.setText(record.content);

            btnAppend.setOnClickListener(v -> {
                String newText = textView.getText().toString();
                record.content += "\n" + newText;
                repository.updateRecord(record);
            });

            btnReplace.setOnClickListener(v -> {
                String newText = textView.getText().toString();
                record.content = newText;
                repository.updateRecord(record);
            });

            btnDelete.setOnClickListener(v ->
                    repository.deleteRecord(record));
        }
    }
}