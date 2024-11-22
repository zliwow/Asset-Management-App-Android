package com.northeastern.finly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button teamInfoButton = findViewById(R.id.teamInfoButton);
        Button numberExtractorButton = findViewById(R.id.numberExtractorButton);
        Button yumengButton = findViewById(R.id.yumengButton);
        Button guyButton = findViewById(R.id.guyButton);

        teamInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TeamInfoActivity.class);
            startActivity(intent);
        });

        numberExtractorButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NumberExtractorActivity.class);
            startActivity(intent);
        });

        yumengButton.setOnClickListener(v -> {
            // Placeholder for Yumeng's activity

        });

        guyButton.setOnClickListener(v -> {
            // Placeholder for Guy's activity

        });
    }
}