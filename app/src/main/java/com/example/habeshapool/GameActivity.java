package com.example.habeshapool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class GameActivity extends AppCompatActivity {

    private enum ActionType { NONE, SCORE, FOUL }
    private ActionType pendingAction = ActionType.NONE;
    private int currentPlayerIndex = -1;

    // List of current balls in play (numbers 3 to 15)
    private ArrayList<Integer> currentBalls = new ArrayList<>();
    private GridLayout currentBallsLayout;

    // Data model for a player
    private class Player {
        String name;
        int score;
        String history;
        // List of scored ball numbers (only those scored)
        ArrayList<Integer> scoredBalls = new ArrayList<>();

        Player() {
            name = "";
            score = 0;
            history = "History:";
        }

        void addScore(int value) {
            score += value;
            history += " +" + value;
        }

        void deductScore(int value) {
            score -= value;
            history += " -" + value;
        }
    }

    private ArrayList<Player> players = new ArrayList<>();

    // UI elements for each player section (max 4)
    private EditText[] playerNameEdits = new EditText[4];
    private TextView[] playerScoreTexts = new TextView[4];
    private TextView[] playerHistoryTexts = new TextView[4];
    private Button[] scoreButtons = new Button[4];
    private Button[] foulButtons = new Button[4];
    private Button[] scratchButtons = new Button[4];
    private Button[] undoButtons = new Button[4];
    private View[] playerSections = new View[4];
    private Button[] editNameButtons = new Button[4];
    private Button[] doneNameButtons = new Button[4];
    // New array for the scored history container (LinearLayout for images)
    private LinearLayout[] scoredHistoryLayouts = new LinearLayout[4];

    private Button newGameButton;
    private int numPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Retrieve the number of players from the intent.
        numPlayers = getIntent().getIntExtra("numPlayers", 2);

        // Initialize players.
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player());
        }

        // Initialize current balls (from 3 to 15).
        for (int ball = 3; ball <= 15; ball++) {
            currentBalls.add(ball);
        }

        // Set up the GridLayout for current balls.
        currentBallsLayout = findViewById(R.id.current_balls_layout);
        createCurrentBallButtons();

        // Initialize player sections (4 total; hide extras).
        for (int i = 0; i < 4; i++) {
            int sectionId = getResources().getIdentifier("player_section_" + (i + 1), "id", getPackageName());
            playerSections[i] = findViewById(sectionId);
            if (i >= numPlayers) {
                playerSections[i].setVisibility(View.GONE);
            } else {
                int editId = getResources().getIdentifier("edit_player_name_" + (i + 1), "id", getPackageName());
                int scoreTextId = getResources().getIdentifier("text_player_score_" + (i + 1), "id", getPackageName());
                int historyTextId = getResources().getIdentifier("text_player_history_" + (i + 1), "id", getPackageName());
                int btnScoreId = getResources().getIdentifier("btn_score_" + (i + 1), "id", getPackageName());
                int btnFoulId = getResources().getIdentifier("btn_foul_" + (i + 1), "id", getPackageName());
                int btnScratchId = getResources().getIdentifier("btn_scratch_" + (i + 1), "id", getPackageName());
                int btnUndoId = getResources().getIdentifier("btn_undo_" + (i + 1), "id", getPackageName());
                int linearScoredHistoryId = getResources().getIdentifier("linear_scored_history_" + (i + 1), "id", getPackageName());

                // Get views.
                playerNameEdits[i] = findViewById(editId);
                playerScoreTexts[i] = findViewById(scoreTextId);
                playerHistoryTexts[i] = findViewById(historyTextId);
                scoreButtons[i] = findViewById(btnScoreId);
                foulButtons[i] = findViewById(btnFoulId);
                undoButtons[i] = findViewById(btnUndoId);
                scratchButtons[i] = findViewById(btnScratchId);

                scoredHistoryLayouts[i] = findViewById(linearScoredHistoryId);

                // Enable name editing without extra buttons (remove any previous disabling).
                playerNameEdits[i].setFocusable(true);
                playerNameEdits[i].setFocusableInTouchMode(true);

                // Attach a listener so that when the user taps "Done" on the keyboard,
                // the focus is cleared and the keyboard hides.
                final EditText nameField = playerNameEdits[i]; // Ensure it's effectively final
                nameField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            v.clearFocus();
                            hideKeyboard(v);
                            return true;
                        }
                        return false;
                    }
                });

                // Set initial score and history.
                playerScoreTexts[i].setText("Score: 0 (History: )");
                playerHistoryTexts[i].setText("");
                scoredHistoryLayouts[i].removeAllViews();

                final int playerIndex = i;
                // Score button listener:
                scoreButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pendingAction = ActionType.SCORE;
                        currentPlayerIndex = playerIndex;
                        Toast.makeText(GameActivity.this, "Tap a ball to score for Player " + (playerIndex + 1), Toast.LENGTH_SHORT).show();
                    }
                });
                // Foul button listener:
                foulButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pendingAction = ActionType.FOUL;
                        currentPlayerIndex = playerIndex;
                        Toast.makeText(GameActivity.this, "Tap a ball to record a foul for Player " + (playerIndex + 1), Toast.LENGTH_SHORT).show();
                    }
                });
                // Scratch button listener:
                scratchButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentBalls.isEmpty()) {
                            Toast.makeText(GameActivity.this, "No balls remaining.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int currentBall = currentBalls.get(0);  // smallest value ball
                        int deduction = (currentBall == 3) ? 4 : currentBall;
                        players.get(playerIndex).deductScore(deduction);
                        updatePlayerUI(playerIndex);
                        Toast.makeText(GameActivity.this, "Scratch recorded for Player " + (playerIndex + 1), Toast.LENGTH_SHORT).show();
                        checkEndOfGame();
                    }
                });

                undoButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Player currentPlayer = players.get(playerIndex);
                        // Ensure there is something in the history to undo.
                        // We assume each shot is recorded as " +6" or " -4" etc.
                        if (currentPlayer.history != null && currentPlayer.history.trim().length() > 0
                                && currentPlayer.history.contains(" ")) {
                            // Find the index of the last shot entry by locating the last space.
                            int lastSpaceIndex = currentPlayer.history.lastIndexOf(" ");
                            // Get the last shot entry; e.g. " +6" or " +10" (could be 3 or 4 characters)
                            String shotEntry = currentPlayer.history.substring(lastSpaceIndex);
                            int shotValue;
                            try {
                                shotValue = Integer.parseInt(shotEntry.trim());
                            } catch (NumberFormatException e) {
                                // In case of unexpected formatting, show error and exit.
                                Toast.makeText(GameActivity.this, "Error undoing shot!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Adjust the player's score:
                            // If shotValue was positive, subtract it; if negative, subtracting negative adds points.
                            currentPlayer.score -= shotValue;

                            // If the shot was a scoring shot (i.e. shotValue is positive), restore the ball.
                            if (shotValue > 0 && currentPlayer.scoredBalls != null && !currentPlayer.scoredBalls.isEmpty()) {
                                // Remove the last scored ball from the player's record.
                                int ballToRestore = currentPlayer.scoredBalls.remove(currentPlayer.scoredBalls.size() - 1);
                                // Return the ball back to the current balls (i.e., the table).
                                currentBalls.add(ballToRestore);
                                // Refresh the balls in play UI (assuming you have a method to recreate the ball buttons).
                                createCurrentBallButtons();
                            }

                            // Remove the last shot record from the player's history.
                            currentPlayer.history = currentPlayer.history.substring(0, lastSpaceIndex);

                            // Update this player's UI (score, history display, and any ball images that appear in their section).
                            updatePlayerUI(playerIndex);
                        } else {
                            Toast.makeText(GameActivity.this, "Nothing to undo!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        // Set up the "Start a New Game" button.
        newGameButton = findViewById(R.id.button_new_game);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, PlayerSelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Add a touch listener to the root view to clear focus (and hide keyboard) when tapping outside.
        findViewById(R.id.game_root).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    currentFocus.clearFocus();
                    hideKeyboard(currentFocus);
                }
                return false;
            }
        });

        // Restart Current Game Button
        Button restartGameButton = findViewById(R.id.button_restart_game);
        restartGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset all player scores to zero
                for (Player player : players) {
                    player.score = 0;
                    player.history = "History: "; // Clear full history
                    player.scoredBalls.clear(); // Clear scored balls history
                }

                // Reset balls in play (3 to 15)
                currentBalls.clear();
                for (int i = 3; i <= 15; i++) {
                    currentBalls.add(i);
                }

                // Refresh UI to reflect reset game state
                for (int i = 0; i < numPlayers; i++) {
                    updatePlayerUI(i);
                }
                createCurrentBallButtons(); // Recreate ball buttons in the GridLayout

                Toast.makeText(GameActivity.this, "Game restarted! Scores reset.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Create ball buttons dynamically using images in the GridLayout.
    private void createCurrentBallButtons() {
        Collections.sort(currentBalls);
        currentBallsLayout.removeAllViews();
        for (final int ball : currentBalls) {
            Button b = new Button(this);
            // Set the background image for the ball button.
            b.setBackgroundResource(getImageForBall(ball));
            b.setTag(ball);
            b.setText(""); // Remove any text.
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(45);  // Change to your preferred size
            params.height = dpToPx(45); // Change to your preferred size
            params.setMargins(dpToPx(3), dpToPx(5), dpToPx(5), dpToPx(3));
            b.setLayoutParams(params);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pendingAction == ActionType.NONE || currentPlayerIndex < 0) {
                        Toast.makeText(GameActivity.this, "Select an action (Score, Foul, or Scratch) for a player first.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int tappedBall = (int) v.getTag();
                    if (pendingAction == ActionType.SCORE) {
                        int pointValue = (tappedBall == 3) ? 6 : tappedBall;
                        players.get(currentPlayerIndex).addScore(pointValue);
                        // Also add this ball to the scored history.
                        players.get(currentPlayerIndex).scoredBalls.add(tappedBall);
                        updatePlayerUI(currentPlayerIndex);
                        removeBallButton(v);
                        Toast.makeText(GameActivity.this, "Player " + (currentPlayerIndex + 1) + " scored " + pointValue, Toast.LENGTH_SHORT).show();
                        checkEndOfGame();
                    } else if (pendingAction == ActionType.FOUL) {
                        int currentBall = currentBalls.get(0);  // smallest ball count as current
                        int deduction = (currentBall == 3) ? 4 : tappedBall;
                        players.get(currentPlayerIndex).deductScore(deduction);
                        updatePlayerUI(currentPlayerIndex);
                        Toast.makeText(GameActivity.this, "Foul recorded for Player " + (currentPlayerIndex + 1) + " (-" + deduction + ")", Toast.LENGTH_SHORT).show();
                    }
                    pendingAction = ActionType.NONE;
                    currentPlayerIndex = -1;
                }
            });
            currentBallsLayout.addView(b);
        }
    }

    // Helper method: Remove the tapped ball button and update the ball state.
    private void removeBallButton(View ballButton) {
        int ballNumber = (int) ballButton.getTag();
        currentBalls.remove(Integer.valueOf(ballNumber));
        currentBallsLayout.removeView(ballButton);
    }

    // Update player's complete history, score, and the scored-history display.
    private void updatePlayerUI(int playerIndex) {
        // Format score with history
        String scoreText = "Score: " + players.get(playerIndex).score;
        String historyText = players.get(playerIndex).history.isEmpty() ? "" : " (" + players.get(playerIndex).history + ")";

        // Update the combined score and history in a single TextView
        playerScoreTexts[playerIndex].setText(scoreText + historyText);

        // Update scored ball display (image-based history)
        updatePlayerScoredHistoryUI(playerIndex);
    }

    // Update the scored history LinearLayout by adding ImageViews for each scored ball.
    private void updatePlayerScoredHistoryUI(int playerIndex) {
        LinearLayout layout = scoredHistoryLayouts[playerIndex];
        layout.removeAllViews();
        for (int ballNumber : players.get(playerIndex).scoredBalls) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(getImageForBall(ballNumber));

            // Set size for ball images
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(30), dpToPx(30));
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)); // Adjust spacing
            iv.setLayoutParams(params);
            layout.addView(iv);
        }
    }

    // Helper method: Convert dp to pixels.
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Helper method: Return drawable resource id for a given ball number.
    private int getImageForBall(int ballNumber) {
        switch (ballNumber) {
            case 3:  return R.drawable.pool_ball_3;
            case 4:  return R.drawable.pool_ball_4;
            case 5:  return R.drawable.pool_ball_5;
            case 6:  return R.drawable.pool_ball_6;
            case 7:  return R.drawable.pool_ball_7;
            case 8:  return R.drawable.pool_ball_8;
            case 9:  return R.drawable.pool_ball_9;
            case 10: return R.drawable.pool_ball_10;
            case 11: return R.drawable.pool_ball_11;
            case 12: return R.drawable.pool_ball_12;
            case 13: return R.drawable.pool_ball_13;
            case 14: return R.drawable.pool_ball_14;
            case 15: return R.drawable.pool_ball_15;
            default: return R.drawable.default_ball;
        }
    }

    // Check if the game is over (all balls are pocketed) and display the winner.
    private void checkEndOfGame() {
        if (currentBalls.isEmpty()) {
            // Sort players by score (highest first)
            ArrayList<Player> sortedPlayers = new ArrayList<>(players);
            sortedPlayers.sort((p1, p2) -> Integer.compare(p2.score, p1.score));

            // Get the top player's name.
            Player first = sortedPlayers.get(0);
            int firstIndex = players.indexOf(first); // Find original index
            String firstName = playerNameEdits[firstIndex].getText().toString().trim();
            if (firstName.isEmpty()) {
                firstName = "Player " + (firstIndex + 1);
            }

            // If two players have equal top score, declare a tie.
            String titleMessage;
            if (sortedPlayers.size() > 1 && first.score == sortedPlayers.get(1).score) {
                titleMessage = "No winner, its a tie!\n\n";
            } else {
                titleMessage = "Winner: " + firstName + "\n\n";
            }

            // Build the game over message with rankings.
            StringBuilder resultMessage = new StringBuilder(titleMessage);
            for (int i = 0; i < sortedPlayers.size(); i++) {
                Player currentPlayer = sortedPlayers.get(i);
                int originalIndex = players.indexOf(currentPlayer);
                String playerName = playerNameEdits[originalIndex].getText().toString().trim();
                if (playerName.isEmpty()) {
                    playerName = "Player " + (originalIndex + 1);
                }
                resultMessage.append((i + 1))
                        .append(". ")
                        .append(playerName)
                        .append(" - Score: ")
                        .append(currentPlayer.score)
                        .append("\n");
            }

            // Show the game-over popup with accurate rankings & names.
            new AlertDialog.Builder(this)
                    .setTitle("Game Over")
                    .setMessage(resultMessage.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    // Hide the soft keyboard.
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}