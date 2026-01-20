package com.example.habeshapool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_selection);

        Button twoPlayersButton = findViewById(R.id.button_two_players);
        Button threePlayersButton = findViewById(R.id.button_three_players);
        Button fourPlayersButton = findViewById(R.id.button_four_players);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numPlayers;

                if (v.getId() == R.id.button_two_players) {
                    numPlayers = 2;
                } else if (v.getId() == R.id.button_three_players) {
                    numPlayers = 3;
                } else {
                    numPlayers = 4;
                }

                Intent intent = new Intent(PlayerSelectionActivity.this, GameActivity.class);
                intent.putExtra("numPlayers", numPlayers);
                startActivity(intent);
            }
        };

        twoPlayersButton.setOnClickListener(listener);
        threePlayersButton.setOnClickListener(listener);
        fourPlayersButton.setOnClickListener(listener);
    }
}