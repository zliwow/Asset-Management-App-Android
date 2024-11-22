package com.northeastern.finly;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TeamInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_info);

        // Set team member information
        TextView member1Name = findViewById(R.id.member1Name);
        TextView member1Email = findViewById(R.id.member1Email);
        TextView member2Name = findViewById(R.id.member2Name);
        TextView member2Email = findViewById(R.id.member2Email);
        TextView member3Name = findViewById(R.id.member3Name);
        TextView member3Email = findViewById(R.id.member3Email);

        // Update with your team's information
        member1Name.setText("Guyriano Charles");
        member1Email.setText("charles.g@northeastern.edu");
        member2Name.setText("Yumeng Zeng");
        member2Email.setText("zeng.yum@northeastern.edu");
        member3Name.setText("Andrew Li");
        member3Email.setText("li.zihao6@northeastern.edu");
    }
}