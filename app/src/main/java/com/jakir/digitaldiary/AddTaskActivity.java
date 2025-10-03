package com.jakir.digitaldiary;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jakir.digitaldiary.model.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText titleField, detailsField, dateField;
    private TextInputLayout titleLayout;
    private ChipGroup priorityChipGroup;
    private ExtendedFloatingActionButton saveFab;
    private FirebaseFirestore db;

    private boolean isEditMode = false;
    private String documentIdToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = FirebaseFirestore.getInstance();
        setupViews();
        setupToolbar();
        setupListeners();

        if (getIntent().hasExtra("note_id")) {
            isEditMode = true;
            documentIdToEdit = getIntent().getStringExtra("note_id");
            populateFieldsForEdit();
        }
    }

    private void setupViews() {
        titleLayout = findViewById(R.id.title_layout);
        titleField = findViewById(R.id.editTitle);
        detailsField = findViewById(R.id.editDetails);
        dateField = findViewById(R.id.editDate);
        priorityChipGroup = findViewById(R.id.priority_chip_group);
        saveFab = findViewById(R.id.btnSaveTask);
    }

    private void populateFieldsForEdit() {
        // Change UI text for edit mode
        MaterialToolbar topAppBar = findViewById(R.id.addTaskTopAppBar);
        topAppBar.setTitle("Edit Note");
        saveFab.setText("Update Note");

        String title = getIntent().getStringExtra("title");
        String details = getIntent().getStringExtra("details");
        String date = getIntent().getStringExtra("date");
        String priority = getIntent().getStringExtra("priority");

        titleField.setText(title);
        detailsField.setText(details);
        dateField.setText(date);

        if (priority != null) {
            for (int i = 0; i < priorityChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) priorityChipGroup.getChildAt(i);
                if (chip.getText().toString().equalsIgnoreCase(priority)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private void setupToolbar() {
        MaterialToolbar topAppBar = findViewById(R.id.addTaskTopAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        saveFab.setOnClickListener(v -> saveOrUpdateNote());
        dateField.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            TimeZone timeZone = TimeZone.getDefault();
            long offset = timeZone.getOffset(selection);
            Date date = new Date(selection + offset);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateField.setText(sdf.format(date));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private String getSelectedPriority() {
        int selectedChipId = priorityChipGroup.getCheckedChipId();
        if (selectedChipId == -1) {
            return "Medium";
        } else {
            Chip selectedChip = findViewById(selectedChipId);
            return selectedChip.getText().toString();
        }
    }

    private void saveOrUpdateNote() {
        String title = titleField.getText().toString().trim();
        String details = detailsField.getText().toString().trim();
        String date = dateField.getText().toString();
        String priority = getSelectedPriority();

        if (TextUtils.isEmpty(title)) {
            titleLayout.setError("Title cannot be empty");
            return;
        }

        // --- update or add ---
        if (isEditMode) {
            // We are editing, so perform an update
            db.collection("notes").document(documentIdToEdit)
                    .update("title", title, "details", details, "date", date, "priority", priority)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Note updated!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity after update
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show());
        } else {
            // We are adding a new note
            Note newNote = new Note(title, details, date, priority);
            db.collection("notes").add(newNote)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity after save
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show());
        }
    }
}