package com.jakir.digitaldiary.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jakir.digitaldiary.AddTaskActivity;
import com.jakir.digitaldiary.R;
import com.jakir.digitaldiary.model.Note;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Note> taskList;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TaskAdapter(List<Note> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Note currentNote = taskList.get(position);
        holder.bind(currentNote);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddTaskActivity.class);
            intent.putExtra("note_id", currentNote.getId());
            intent.putExtra("title", currentNote.getTitle());
            intent.putExtra("details", currentNote.getDetails());
            intent.putExtra("date", currentNote.getDate());
            intent.putExtra("priority", currentNote.getPriority());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("notes").document(currentNote.getId()).delete()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, description, date;
        private final Chip priorityChip;
        private final ImageButton deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            description = itemView.findViewById(R.id.taskDescription);
            date = itemView.findViewById(R.id.taskDate);
            deleteButton = itemView.findViewById(R.id.button_delete);
            priorityChip = itemView.findViewById(R.id.priority_chip);
        }

        void bind(Note note) {
            title.setText(note.getTitle());
            description.setText(note.getDetails());
            date.setText(note.getDate());

            if (note.getPriority() != null) {
                priorityChip.setText(note.getPriority());
                switch (note.getPriority().toLowerCase()) {
                    case "high":
                        priorityChip.setChipBackgroundColorResource(R.color.priority_high);
                        break;
                    case "low":
                        priorityChip.setChipBackgroundColorResource(R.color.priority_low);
                        break;
                    default: // Medium
                        priorityChip.setChipBackgroundColorResource(R.color.priority_medium);
                        break;
                }
            }


            title.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }
}