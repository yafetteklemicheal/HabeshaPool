package com.example.habeshapool;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView[] shooterLabels = new TextView[4];
    private TextView[] rankEmojiTexts = new TextView[4];
    private LinearLayout[] scoredHistoryLayouts = new LinearLayout[4];
    private TextView[] statusIndicatorTexts = new TextView[4]; // Lead / Out-of-contention indicators
    private TextView ballsSumTextView;

    private Button newGameButton;
    private int numPlayers;

    private ArrayList<Integer> lastRankingIndices = null;
    private boolean gameJustFinished = false;

    // Aggregate score display for 2-player games
    private TextView aggregateScoreText;

    // Early indicator tracking
    private int leadSecuredIndex = -1;
    private int outOfContentionIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ballsSumTextView = findViewById(R.id.text_balls_sum);

        numPlayers = getIntent().getIntExtra("numPlayers", 2);

        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player());
        }

        for (int ball = 3; ball <= 15; ball++) {
            currentBalls.add(ball);
        }

        currentBallsLayout = findViewById(R.id.current_balls_layout);
        createCurrentBallButtons();

        ballsSumTextView = findViewById(R.id.text_balls_sum);
        updateBallSumUI();

        aggregateScoreText = findViewById(R.id.text_aggregate_score);
        if (numPlayers == 2) {
            aggregateScoreText.setVisibility(View.VISIBLE);
        } else {
            aggregateScoreText.setVisibility(View.GONE);
        }

        for (int i = 0; i < 4; i++) {
            int sectionId = getResources().getIdentifier("player_section_" + (i + 1), "id", getPackageName());
            playerSections[i] = findViewById(sectionId);

            int shooterLabelId = getResources().getIdentifier("text_shooter_label_" + (i + 1), "id", getPackageName());
            shooterLabels[i] = findViewById(shooterLabelId);

            int rankEmojiId = getResources().getIdentifier("text_rank_emoji_" + (i + 1), "id", getPackageName());
            rankEmojiTexts[i] = findViewById(rankEmojiId);

            int statusId = getResources().getIdentifier("text_status_" + (i + 1), "id", getPackageName());
            statusIndicatorTexts[i] = findViewById(statusId);

            if (i >= numPlayers) {
                if (playerSections[i] != null) {
                    playerSections[i].setVisibility(View.GONE);
                }
            } else {
                if (shooterLabels[i] != null) shooterLabels[i].setVisibility(View.VISIBLE);
                if (rankEmojiTexts[i] != null) rankEmojiTexts[i].setText("");
                if (statusIndicatorTexts[i] != null) {
                    statusIndicatorTexts[i].setVisibility(View.GONE);
                }

                int editId = getResources().getIdentifier("edit_player_name_" + (i + 1), "id", getPackageName());
                int scoreTextId = getResources().getIdentifier("text_player_score_" + (i + 1), "id", getPackageName());
                int historyTextId = getResources().getIdentifier("text_player_history_" + (i + 1), "id", getPackageName());
                int btnScoreId = getResources().getIdentifier("btn_score_" + (i + 1), "id", getPackageName());
                int btnFoulId = getResources().getIdentifier("btn_foul_" + (i + 1), "id", getPackageName());
                int btnScratchId = getResources().getIdentifier("btn_scratch_" + (i + 1), "id", getPackageName());
                int btnUndoId = getResources().getIdentifier("btn_undo_" + (i + 1), "id", getPackageName());
                int linearScoredHistoryId = getResources().getIdentifier("linear_scored_history_" + (i + 1), "id", getPackageName());

                playerNameEdits[i] = findViewById(editId);
                playerScoreTexts[i] = findViewById(scoreTextId);
                playerHistoryTexts[i] = findViewById(historyTextId);
                scoreButtons[i] = findViewById(btnScoreId);
                foulButtons[i] = findViewById(btnFoulId);
                undoButtons[i] = findViewById(btnUndoId);
                scratchButtons[i] = findViewById(btnScratchId);
                scoredHistoryLayouts[i] = findViewById(linearScoredHistoryId);

                if (playerNameEdits[i] != null) {
                    playerNameEdits[i].setFocusable(true);
                    playerNameEdits[i].setFocusableInTouchMode(true);

                    final EditText nameField = playerNameEdits[i];
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
                }

                if (playerScoreTexts[i] != null) {
                    playerScoreTexts[i].setText("Score: 0 (History: )");
                }
                if (playerHistoryTexts[i] != null) {
                    playerHistoryTexts[i].setText("");
                }
                if (scoredHistoryLayouts[i] != null) {
                    scoredHistoryLayouts[i].removeAllViews();
                }

                final int playerIndex = i;

                if (scoreButtons[i] != null) {
                    scoreButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pendingAction = ActionType.SCORE;
                            currentPlayerIndex = playerIndex;
                            Toast.makeText(GameActivity.this, "Tap a ball to score for Player " + (playerIndex + 1), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if (foulButtons[i] != null) {
                    foulButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pendingAction = ActionType.FOUL;
                            currentPlayerIndex = playerIndex;
                            Toast.makeText(GameActivity.this, "Tap a ball to record a foul for Player " + (playerIndex + 1), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if (scratchButtons[i] != null) {
                    scratchButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (currentBalls.isEmpty()) {
                                Toast.makeText(GameActivity.this, "No balls remaining.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int currentBall = currentBalls.get(0);
                            int deduction = (currentBall == 3) ? 4 : currentBall;
                            players.get(playerIndex).deductScore(deduction);
                            updatePlayerUI(playerIndex);
                            updateRankEmojis();
                            updateAggregateScore();
                            updateEarlyIndicators();
                            Toast.makeText(GameActivity.this, "Scratch recorded for Player " + (playerIndex + 1), Toast.LENGTH_SHORT).show();
                            checkEndOfGame();
                        }
                    });
                }

                if (undoButtons[i] != null) {
                    undoButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Player currentPlayer = players.get(playerIndex);
                            if (currentPlayer.history != null && currentPlayer.history.trim().length() > 0
                                    && currentPlayer.history.contains(" ")) {
                                int lastSpaceIndex = currentPlayer.history.lastIndexOf(" ");
                                String shotEntry = currentPlayer.history.substring(lastSpaceIndex);
                                int shotValue;
                                try {
                                    shotValue = Integer.parseInt(shotEntry.trim());
                                } catch (NumberFormatException e) {
                                    Toast.makeText(GameActivity.this, "Error undoing shot!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                currentPlayer.score -= shotValue;

                                if (shotValue > 0 && currentPlayer.scoredBalls != null && !currentPlayer.scoredBalls.isEmpty()) {
                                    int ballToRestore = currentPlayer.scoredBalls.remove(currentPlayer.scoredBalls.size() - 1);
                                    currentBalls.add(ballToRestore);
                                    createCurrentBallButtons();
                                }

                                currentPlayer.history = currentPlayer.history.substring(0, lastSpaceIndex);
                                updatePlayerUI(playerIndex);
                                updateRankEmojis();
                                updateAggregateScore();
                                updateEarlyIndicators();
                            } else {
                                Toast.makeText(GameActivity.this, "Nothing to undo!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }

        newGameButton = findViewById(R.id.button_new_game);
        if (newGameButton != null) {
            newGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmDialog(
                            "Are you sure you want to start a new game? This will erase all progress.",
                            new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(GameActivity.this, PlayerSelectionActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                    );
                }
            });
        }

        View root = findViewById(R.id.game_root);
        if (root != null) {
            root.setOnTouchListener(new View.OnTouchListener() {
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
        }

        Button restartGameButton = findViewById(R.id.button_restart_game);
        if (restartGameButton != null) {
            restartGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmDialog(
                            "Are you sure you want to restart the current game? This will reset scores and history.",
                            new Runnable() {
                                @Override
                                public void run() {
                                    handleRestartCurrentGame();
                                }
                            }
                    );
                }
            });
        }

        updateRankEmojis();
        updateAggregateScore();
        updateEarlyIndicators();
    }

    private int getCurrentBallSum() {
        int sum = 0;
        for (int b : currentBalls) {
            sum += (b == 3 ? 6 : b);
        }
        return sum;
    }

    private void updateBallSumUI() {
        if (ballsSumTextView != null) {
            ballsSumTextView.setText("" + getCurrentBallSum());
        }
    }

    private void handleRestartCurrentGame() {
        if (gameJustFinished && lastRankingIndices != null && lastRankingIndices.size() == numPlayers) {
            boolean anyNamed = false;
            for (int i = 0; i < numPlayers; i++) {
                String name = playerNameEdits[i].getText().toString().trim();
                if (!name.isEmpty()) {
                    anyNamed = true;
                    break;
                }
            }

            if (anyNamed) {
                reorderShootersByLastRanking();
            }
        }

        gameJustFinished = false;

        for (Player player : players) {
            player.score = 0;
            player.history = "History:";
            player.scoredBalls.clear();
        }

        currentBalls.clear();
        for (int i = 3; i <= 15; i++) {
            currentBalls.add(i);
        }

        for (int i = 0; i < numPlayers; i++) {
            updatePlayerUI(i);
        }
        createCurrentBallButtons();
        updateBallSumUI();

        clearAllNameFocusAndHideKeyboard();

        updateRankEmojis();
        updateAggregateScore();
        clearEarlyIndicators();

        Toast.makeText(GameActivity.this, "Game restarted! Scores reset.", Toast.LENGTH_SHORT).show();
    }

    private void reorderShootersByLastRanking() {
        if (lastRankingIndices == null || lastRankingIndices.size() != numPlayers) {
            return;
        }

        String[] originalNames = new String[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            originalNames[i] = playerNameEdits[i].getText().toString().trim();
        }

        String[] newNamesByPosition = new String[numPlayers];

        for (int position = 0; position < numPlayers; position++) {
            int originalIndex = lastRankingIndices.get(position);
            String name = originalNames[originalIndex];
            newNamesByPosition[position] = name;
        }

        for (int i = 0; i < numPlayers; i++) {
            playerNameEdits[i].setText(newNamesByPosition[i]);
        }

        clearAllNameFocusAndHideKeyboard();
    }

    private void showConfirmDialog(String message, final Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                // LEFT button (negative) → shows "Yes"
                .setNegativeButton("Yes", (dialog, which) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                })
                // RIGHT button (positive) → shows "No"
                .setPositiveButton("No", null)
                .show();
    }

    private void clearAllNameFocusAndHideKeyboard() {
        View anyView = null;
        for (int i = 0; i < numPlayers; i++) {
            if (playerNameEdits[i] != null) {
                playerNameEdits[i].clearFocus();
                anyView = playerNameEdits[i];
            }
        }
        if (anyView != null) {
            hideKeyboard(anyView);
        }
    }

    private void createCurrentBallButtons() {
        Collections.sort(currentBalls);
        currentBallsLayout.removeAllViews();
        for (final int ball : currentBalls) {
            Button b = new Button(this);
            b.setBackgroundResource(getImageForBall(ball));
            b.setTag(ball);
            b.setText("");
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(45);
            params.height = dpToPx(45);
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
                        players.get(currentPlayerIndex).scoredBalls.add(tappedBall);
                        updatePlayerUI(currentPlayerIndex);
                        removeBallButton(v);
                        updateRankEmojis();
                        updateAggregateScore();
                        updateEarlyIndicators();
                        Toast.makeText(GameActivity.this, "Player " + (currentPlayerIndex + 1) + " scored " + pointValue, Toast.LENGTH_SHORT).show();
                        checkEndOfGame();
                    } else if (pendingAction == ActionType.FOUL) {
                        int currentBall = currentBalls.get(0);
                        int deduction = (currentBall == 3) ? 4 : tappedBall;
                        players.get(currentPlayerIndex).deductScore(deduction);
                        updatePlayerUI(currentPlayerIndex);
                        updateRankEmojis();
                        updateAggregateScore();
                        updateEarlyIndicators();
                        Toast.makeText(GameActivity.this, "Foul recorded for Player " + (currentPlayerIndex + 1) + " (-" + deduction + ")", Toast.LENGTH_SHORT).show();
                    }
                    pendingAction = ActionType.NONE;
                    currentPlayerIndex = -1;
                }
            });
            currentBallsLayout.addView(b);
        }
        updateBallSumUI();
    }

    private void removeBallButton(View ballButton) {
        int ballNumber = (int) ballButton.getTag();
        currentBalls.remove(Integer.valueOf(ballNumber));
        currentBallsLayout.removeView(ballButton);
        updateBallSumUI();
    }

    private void updatePlayerUI(int playerIndex) {
        String scoreText = "Score: " + players.get(playerIndex).score;
        String historyText = players.get(playerIndex).history.isEmpty() ? "" : " (" + players.get(playerIndex).history + ")";
        playerScoreTexts[playerIndex].setText(scoreText + historyText);
        updatePlayerScoredHistoryUI(playerIndex);
    }

    private void updatePlayerScoredHistoryUI(int playerIndex) {
        LinearLayout layout = scoredHistoryLayouts[playerIndex];
        layout.removeAllViews();
        for (int ballNumber : players.get(playerIndex).scoredBalls) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(getImageForBall(ballNumber));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(30), dpToPx(30));
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            iv.setLayoutParams(params);
            layout.addView(iv);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

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

    private void checkEndOfGame() {
        if (currentBalls.isEmpty()) {
            clearEarlyIndicators();

            ArrayList<Player> sortedPlayers = new ArrayList<>(players);
            sortedPlayers.sort((p1, p2) -> Integer.compare(p2.score, p1.score));

            lastRankingIndices = new ArrayList<>();
            for (Player p : sortedPlayers) {
                int originalIndex = players.indexOf(p);
                lastRankingIndices.add(originalIndex);
            }
            gameJustFinished = true;

            Player first = sortedPlayers.get(0);
            int firstIndex = players.indexOf(first);
            String firstName = playerNameEdits[firstIndex].getText().toString().trim();
            if (firstName.isEmpty()) {
                firstName = "Player " + (firstIndex + 1);
            }

            String titleMessage;
            if (sortedPlayers.size() > 1 && first.score == sortedPlayers.get(1).score) {
                titleMessage = "No winner, its a tie!\n\n";
            } else {
                titleMessage = "Winner: " + firstName + "\n\n";
            }

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

            new AlertDialog.Builder(this)
                    .setTitle("Game Over")
                    .setMessage(resultMessage.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // --- NEW: Aggregate score for 2-player games ---
    private void updateAggregateScore() {
        if (numPlayers != 2 || aggregateScoreText == null) {
            return;
        }

        int score1 = players.get(0).score;
        int score2 = players.get(1).score;

        if (score1 == score2) {
            aggregateScoreText.setText("Score tied\nAggregate score: 0");
            return;
        }

        int diff = Math.abs(score1 - score2);
        int leaderIndex = (score1 > score2) ? 0 : 1;

        String name1 = playerNameEdits[0].getText().toString().trim();
        String name2 = playerNameEdits[1].getText().toString().trim();

        if (name1.isEmpty()) name1 = "Player 1";
        if (name2.isEmpty()) name2 = "Player 2";

        String leaderName = (leaderIndex == 0) ? name1 : name2;

        String text = leaderName + " in the lead\nAggregate score: " + diff;
        aggregateScoreText.setText(text);
    }

    // --- NEW: Rank emojis based on current scores ---
    private void updateRankEmojis() {
        // Clear all first
        for (int i = 0; i < numPlayers; i++) {
            if (rankEmojiTexts[i] != null) {
                rankEmojiTexts[i].setText("");
            }
        }

        if (numPlayers < 2) return;

        int[] scores = new int[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            scores[i] = players.get(i).score;
        }

        // Check full tie → no emojis
        boolean allEqual = true;
        for (int i = 1; i < numPlayers; i++) {
            if (scores[i] != scores[0]) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) return;

        // Build list of indices sorted by score desc (stable)
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) indices.add(i);
        indices.sort((a, b) -> Integer.compare(scores[b], scores[a]));

        // Determine rank groups (1-based rank) with rank skipping on ties
        int[] rankByIndex = new int[numPlayers];
        int currentRank = 1;
        rankByIndex[indices.get(0)] = currentRank;

        for (int i = 1; i < indices.size(); i++) {
            int prevIdx = indices.get(i - 1);
            int curIdx = indices.get(i);

            if (scores[curIdx] == scores[prevIdx]) {
                // Same score → same rank
                rankByIndex[curIdx] = currentRank;
            } else {
                // Count how many players shared the previous score
                int tieCount = 0;
                for (int j = i - 1; j >= 0; j--) {
                    if (scores[indices.get(j)] == scores[prevIdx]) {
                        tieCount++;
                    } else {
                        break;
                    }
                }
                // Skip ranks equal to number of players in that previous tied group
                currentRank += tieCount;
                rankByIndex[curIdx] = currentRank;
            }
        }

        // Assign emojis based on final rank numbers
        for (int i = 0; i < numPlayers; i++) {
            int r = rankByIndex[i];
            String emoji;

            if (r == 1) emoji = "🥇";
            else if (r == 2) emoji = "🥈";
            else if (r == 3) emoji = "🥉";
            else emoji = "🗑";

            rankEmojiTexts[i].setText(emoji);
        }
    }

    // --- NEW: Early winner / loser indicators ---

    private void updateEarlyIndicators() {
        // Only active when 3 balls or less remain
        if (currentBalls.size() > 3 || numPlayers < 2) {
            clearEarlyIndicators();
            return;
        }

        int remainingSum = getCurrentBallSum();

        // Build scores and sorted indices (desc)
        int[] scores = new int[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            scores[i] = players.get(i).score;
        }

        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) indices.add(i);
        indices.sort((a, b) -> Integer.compare(scores[b], scores[a]));

        // LEAD SECURED
        int newLeadIndex = -1;
        if (numPlayers >= 2) {
            int leaderIdx = indices.get(0);
            int secondIdx = indices.get(1);
            int leaderScore = scores[leaderIdx];
            int secondScore = scores[secondIdx];

            // Must be a sole leader
            if (leaderScore != secondScore) {
                int leaderMinusRemaining = leaderScore - remainingSum;
                if (leaderMinusRemaining > secondScore) {
                    newLeadIndex = leaderIdx;
                }
            }
        }

        // OUT OF CONTENTION
        int newOutIndex = -1;
        if (numPlayers >= 2) {
            int lastIdx = indices.get(numPlayers - 1);
            int secondLastIdx = indices.get(numPlayers - 2);
            int lastScore = scores[lastIdx];
            int secondLastScore = scores[secondLastIdx];

            // Must be a sole last place
            if (lastScore != secondLastScore) {
                int lastPlusRemaining = lastScore + remainingSum;
                if (lastPlusRemaining < secondLastScore) {
                    newOutIndex = lastIdx;
                }
            }
        }

        // Apply changes with animations

        // Lead secured
        if (leadSecuredIndex != -1 && leadSecuredIndex != newLeadIndex) {
            hideStatusIndicator(leadSecuredIndex);
            leadSecuredIndex = -1;
        }
        if (newLeadIndex != -1) {
            leadSecuredIndex = newLeadIndex;
            showStatusIndicator(newLeadIndex, true);
        }

        // Out of contention
        if (outOfContentionIndex != -1 && outOfContentionIndex != newOutIndex) {
            hideStatusIndicator(outOfContentionIndex);
            outOfContentionIndex = -1;
        }
        if (newOutIndex != -1) {
            outOfContentionIndex = newOutIndex;
            showStatusIndicator(newOutIndex, false);
        }
    }

    private void clearEarlyIndicators() {
        if (leadSecuredIndex != -1) {
            hideStatusIndicator(leadSecuredIndex);
            leadSecuredIndex = -1;
        }
        if (outOfContentionIndex != -1) {
            hideStatusIndicator(outOfContentionIndex);
            outOfContentionIndex = -1;
        }
    }

    private void showStatusIndicator(int playerIndex, boolean isLead) {
        if (playerIndex < 0 || playerIndex >= statusIndicatorTexts.length) return;
        TextView tv = statusIndicatorTexts[playerIndex];
        if (tv == null) return;

        tv.setTextColor(Color.BLACK);
        if (isLead) {
            tv.setText("Lead Secured 🏆");
            tv.setBackgroundColor(Color.parseColor("#4CAF50")); // green-ish
        } else {
            tv.setText("Out of contention ❌");
            tv.setBackgroundColor(Color.parseColor("#F44336")); // red-ish
        }

        if (tv.getVisibility() != View.VISIBLE) {
            tv.setVisibility(View.VISIBLE);
            tv.startAnimation(createSlideInAnimation());
        }
    }

    private void hideStatusIndicator(final int playerIndex) {
        if (playerIndex < 0 || playerIndex >= statusIndicatorTexts.length) return;
        final TextView tv = statusIndicatorTexts[playerIndex];
        if (tv == null) return;
        if (tv.getVisibility() != View.VISIBLE) return;

        Animation slideOut = createSlideOutAnimation();
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                tv.setVisibility(View.GONE);
                tv.setText("");
                tv.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        tv.startAnimation(slideOut);
    }

    private Animation createSlideInAnimation() {
        int distance = dpToPx(80);
        TranslateAnimation anim = new TranslateAnimation(-distance, 0, 0, 0);
        anim.setDuration(200);
        anim.setFillAfter(true);
        return anim;
    }

    private Animation createSlideOutAnimation() {
        int distance = dpToPx(80);
        TranslateAnimation anim = new TranslateAnimation(0, -distance, 0, 0);
        anim.setDuration(200);
        anim.setFillAfter(true);
        return anim;
    }
}