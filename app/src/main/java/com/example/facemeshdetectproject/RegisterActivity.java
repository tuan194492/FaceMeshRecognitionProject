package com.example.facemeshdetectproject;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private Button btnSave;
    private String faceMeshPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_name);

        editTextName = findViewById(R.id.editTextName);
        btnSave = findViewById(R.id.btnSave);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String valueShow = bundle.getString("faceMeshPoints", "");
            faceMeshPoints = valueShow;
        }
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
//                getData();
            }
        });
    }

    private void saveData() {
        String name = editTextName.getText().toString();

        // Lưu dữ liệu vào SQLite
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveFaceData(name, faceMeshPoints);

        finish();
    }

}
