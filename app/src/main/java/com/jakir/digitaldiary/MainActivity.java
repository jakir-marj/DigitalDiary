package com.jakir.digitaldiary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jakir.digitaldiary.adapter.TaskAdapter;
import com.jakir.digitaldiary.model.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private final List<Note> taskList = new ArrayList<>();
    private TaskAdapter adapter;
    private TextView noTaskView;

    private final ActivityResultLauncher<Intent> addTaskLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                String title = data.getStringExtra("title");
                                String details = data.getStringExtra("details");
                                String date = data.getStringExtra("date");
                                String priority = data.getStringExtra("priority");

                                Note newNote = new Note(title, details, date, priority);
                                // Instead of adding locally, save to Firestore
                                saveNoteToFirestore(newNote);
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        noTaskView = findViewById(R.id.noTask);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTasks);

        //  Firestore listener
        adapter = new TaskAdapter(taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load notes from Firestore in real-time
        loadNotes();

        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            addTaskLauncher.launch(intent);
        });
    }

    private void saveNoteToFirestore(Note note) {
        db.collection("notes")
                .add(note)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show());
    }

    private void loadNotes() {
        db.collection("notes")
                .orderBy("createdAt", Query.Direction.DESCENDING) // Show newest notes first
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    taskList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Note note = doc.toObject(Note.class);
                            taskList.add(note);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateNoTaskView();
                });
    }

    private void updateNoTaskView() {
        if (taskList.isEmpty()) {
            noTaskView.setVisibility(View.VISIBLE);
        } else {
            noTaskView.setVisibility(View.GONE);
        }
    }
}