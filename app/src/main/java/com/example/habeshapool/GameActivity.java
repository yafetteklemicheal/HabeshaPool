package com.example.habeshapool;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "habesha_pool_prefs";
    private static final String KEY_LANGUAGE = "preferred_language";
    private static final String LANG_EN = "en";
    private static final String LANG_AM = "am";

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
            history = getString(R.string.history_label);
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

    private String currentLanguage = LANG_EN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String lang = getIntent().getStringExtra("language");
        if (lang == null) lang = "en";
        applyLocale(lang);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ballsSumTextView = findViewById(R.id.text_balls_sum);
        currentBallsLayout = findViewById(R.id.current_balls_layout);

        numPlayers = getIntent().getIntExtra("numPlayers", 2);

        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player());
        }

        for (int ball = 3; ball <= 15; ball++) {
            currentBalls.add(ball);
        }

        createCurrentBallButtons();
        updateBallSumUI();

        aggregateScoreText = findViewById(R.id.text_aggregate_score);
        if (aggregateScoreText != null) {
            if (numPlayers == 2) {
                aggregateScoreText.setVisibility(View.VISIBLE);
            } else {
                aggregateScoreText.setVisibility(View.GONE);
            }
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
                if (statusIndicatorTexts[i] != null) statusIndicatorTexts[i].setVisibility(View.GONE);

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
                    String initialScore = getString(R.string.score_label_plain, 0);
                    String initialHistory = getString(R.string.history_label);
                    playerScoreTexts[i].setText(initialScore + " (" + initialHistory + ")");
                }
                if (playerHistoryTexts[i] != null) {
                    playerHistoryTexts[i].setText("");
                }
                if (scoredHistoryLayouts[i] != null) {
                    scoredHistoryLayouts[i].removeAllViews();
                }

                final int playerIndex = i;

                if (scoreButtons[i] != null) {
                    scoreButtons[i].setText(getString(R.string.btn_score));
                    scoreButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (currentBalls.isEmpty()) {
                                Toast.makeText(GameActivity.this, getString(R.string.toast_no_balls_remaining), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pendingAction = ActionType.SCORE;
                            currentPlayerIndex = playerIndex;
                            String displayName = getPlayerDisplayName(playerIndex);
                            Toast.makeText(GameActivity.this, getString(R.string.toast_tap_ball_score, displayName), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if (foulButtons[i] != null) {
                    foulButtons[i].setText(getString(R.string.btn_foul));
                    foulButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (currentBalls.isEmpty()) {
                                Toast.makeText(GameActivity.this, getString(R.string.toast_no_balls_remaining), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pendingAction = ActionType.FOUL;
                            currentPlayerIndex = playerIndex;

                            String displayName = getPlayerDisplayName(playerIndex);
                            Toast.makeText(GameActivity.this, getString(R.string.toast_tap_ball_foul, displayName), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if (scratchButtons[i] != null) {
                    scratchButtons[i].setText(getString(R.string.btn_scratch));
                    scratchButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (currentBalls.isEmpty()) {
                                Toast.makeText(GameActivity.this, getString(R.string.toast_no_balls_remaining), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int currentBall = currentBalls.get(0);
                            int deduction = (currentBall == 3) ? 4 : currentBall;
                            players.get(playerIndex).deductScore(deduction);
                            updatePlayerUI(playerIndex);
                            updateRankEmojis();
                            updateAggregateScore();
                            updateEarlyIndicators();
                            checkEndOfGame();
                        }
                    });
                }

                if (undoButtons[i] != null) {
                    undoButtons[i].setText(getString(R.string.btn_undo));
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
                                    Toast.makeText(GameActivity.this, getString(R.string.toast_nothing_to_undo), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(GameActivity.this, getString(R.string.toast_nothing_to_undo), Toast.LENGTH_SHORT).show();
                                updateEarlyIndicators(); // ensure indicators recalculate even when undo does nothing
                            }
                        }
                    });
                }
            }
        }

        newGameButton = findViewById(R.id.button_new_game);
        if (newGameButton != null) {
            newGameButton.setText(getString(R.string.btn_new_game));
            newGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmDialog(getString(R.string.confirm_new_game), new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(GameActivity.this, PlayerSelectionActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
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
            restartGameButton.setText(getString(R.string.btn_restart_game));
            restartGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmDialog(getString(R.string.confirm_restart_game), new Runnable() {
                        @Override
                        public void run() {
                            handleRestartCurrentGame();
                        }
                    });
                }
            });
        }

        updateRankEmojis();
        updateAggregateScore();
        updateEarlyIndicators();
    }

    private String getPlayerDisplayName(int playerIndex) {
        String displayName = "";
        if (playerIndex >= 0 && playerIndex < playerNameEdits.length && playerNameEdits[playerIndex] != null) {
            displayName = playerNameEdits[playerIndex].getText().toString().trim();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = getString(R.string.player_default_name, playerIndex + 1);
        }
        return displayName;
    }

    private void applyLocale(String lang) {
        if (lang == null) lang = LANG_EN;
        Locale locale;
        if (LANG_AM.equals(lang)) {
            locale = new Locale("am");
        } else {
            locale = new Locale("en");
        }
        Locale.setDefault(locale);
        Resources res = getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
        getApplicationContext().createConfigurationContext(config);
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
            ballsSumTextView.setText(String.valueOf(getCurrentBallSum()));
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

        Toast.makeText(GameActivity.this, getString(R.string.toast_game_restarted), Toast.LENGTH_SHORT).show();
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
                .setNegativeButton(getString(R.string.yes), (dialog, which) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                })
                .setPositiveButton(getString(R.string.no), null)
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
        if (currentBallsLayout == null) return;
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
                        Toast.makeText(GameActivity.this, getString(R.string.toast_select_action_first), Toast.LENGTH_SHORT).show();
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
                        checkEndOfGame();
                    } else if (pendingAction == ActionType.FOUL) {
                        int currentBall = currentBalls.isEmpty() ? 0 : currentBalls.get(0);
                        int deduction = (currentBall == 3) ? 4 : tappedBall;
                        players.get(currentPlayerIndex).deductScore(deduction);
                        updatePlayerUI(currentPlayerIndex);
                        updateRankEmojis();
                        updateAggregateScore();
                        updateEarlyIndicators();
                    }
                    pendingAction = ActionType.NONE;
                    currentPlayerIndex = -1;
                }
            });
            currentBallsLayout.addView(b);
        }
        updateBallSumUI();
        updateEarlyIndicators(); // ensure indicators recalculate when balls change
    }

    private void removeBallButton(View ballButton) {
        if (ballButton == null) return;
        Object tag = ballButton.getTag();
        if (tag instanceof Integer) {
            int ballNumber = (Integer) tag;
            currentBalls.remove(Integer.valueOf(ballNumber));
        }
        if (currentBallsLayout != null) {
            currentBallsLayout.removeView(ballButton);
        }
        updateBallSumUI();
    }

    private void updatePlayerUI(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= players.size()) return;
        String scoreText = getString(R.string.score_label_plain, players.get(playerIndex).score);
        String historyText = players.get(playerIndex).history.isEmpty() ? "" : " (" + players.get(playerIndex).history + ")";
        if (playerScoreTexts[playerIndex] != null) {
            playerScoreTexts[playerIndex].setText(scoreText + historyText);
        }
        updatePlayerScoredHistoryUI(playerIndex);
    }

    private void updatePlayerScoredHistoryUI(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= scoredHistoryLayouts.length) return;
        LinearLayout layout = scoredHistoryLayouts[playerIndex];
        if (layout == null) return;
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
            String firstName = "";
            if (firstIndex >= 0 && playerNameEdits[firstIndex] != null) {
                firstName = playerNameEdits[firstIndex].getText().toString().trim();
            }
            if (firstName == null || firstName.isEmpty()) {
                firstName = getString(R.string.player_default_name, firstIndex + 1);
            }

            String titleMessage;
            if (sortedPlayers.size() > 1 && first.score == sortedPlayers.get(1).score) {
                titleMessage = getString(R.string.game_over_tie) + "\n\n";
            } else {
                titleMessage = getString(R.string.game_over_winner, firstName) + "\n\n";
            }

            StringBuilder resultMessage = new StringBuilder(titleMessage);
            for (int i = 0; i < sortedPlayers.size(); i++) {
                Player currentPlayer = sortedPlayers.get(i);
                int originalIndex = players.indexOf(currentPlayer);
                String playerName = "";
                if (originalIndex >= 0 && playerNameEdits[originalIndex] != null) {
                    playerName = playerNameEdits[originalIndex].getText().toString().trim();
                }
                if (playerName == null || playerName.isEmpty()) {
                    playerName = getString(R.string.player_default_name, originalIndex + 1);
                }
                resultMessage.append((i + 1))
                        .append(". ")
                        .append(playerName)
                        .append(" - ")
                        .append(getString(R.string.score_label_plain, currentPlayer.score))
                        .append("\n");
            }

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.game_over_title))
                    .setMessage(resultMessage.toString())
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // --- Aggregate score for 2-player games ---
    private void updateAggregateScore() {
        if (numPlayers != 2 || aggregateScoreText == null) {
            return;
        }

        int score1 = players.get(0).score;
        int score2 = players.get(1).score;

        if (score1 == score2) {
            aggregateScoreText.setText(getString(R.string.aggregate_tied));
            return;
        }

        int diff = Math.abs(score1 - score2);
        int leaderIndex = (score1 > score2) ? 0 : 1;

        String name1 = "";
        String name2 = "";
        if (playerNameEdits[0] != null) name1 = playerNameEdits[0].getText().toString().trim();
        if (playerNameEdits[1] != null) name2 = playerNameEdits[1].getText().toString().trim();

        if (name1 == null || name1.isEmpty()) name1 = getString(R.string.player_default_name, 1);
        if (name2 == null || name2.isEmpty()) name2 = getString(R.string.player_default_name, 2);

        String leaderName = (leaderIndex == 0) ? name1 : name2;

        String text = getString(R.string.aggregate_leader, leaderName, diff);
        aggregateScoreText.setText(text);
    }

    // --- Rank emojis based on current scores ---
    private void updateRankEmojis() {
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

        boolean allEqual = true;
        for (int i = 1; i < numPlayers; i++) {
            if (scores[i] != scores[0]) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) return;

        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) indices.add(i);
        indices.sort((a, b) -> Integer.compare(scores[b], scores[a]));

        int[] rankByIndex = new int[numPlayers];
        int currentRank = 1;
        rankByIndex[indices.get(0)] = currentRank;

        for (int i = 1; i < indices.size(); i++) {
            int prevIdx = indices.get(i - 1);
            int curIdx = indices.get(i);

            if (scores[curIdx] == scores[prevIdx]) {
                rankByIndex[curIdx] = currentRank;
            } else {
                int tieCount = 0;
                for (int j = i - 1; j >= 0; j--) {
                    if (scores[indices.get(j)] == scores[prevIdx]) {
                        tieCount++;
                    } else {
                        break;
                    }
                }
                currentRank += tieCount;
                rankByIndex[curIdx] = currentRank;
            }
        }

        for (int i = 0; i < numPlayers; i++) {
            int r = rankByIndex[i];
            String emoji;
            if (r == 1) emoji = "🥇";
            else if (r == 2) emoji = "🥈";
            else if (r == 3) emoji = "🥉";
            else emoji = "🗑";

            if (rankEmojiTexts[i] != null) rankEmojiTexts[i].setText(emoji);
        }
    }

    // --- Early winner / loser indicators ---
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

        // LEAD SECURED candidate (first vs second)
        int newLeadIndex = -1;
        if (numPlayers >= 2) {
            int leaderIdx = indices.get(0);
            int secondIdx = indices.get(1);
            int leaderScore = scores[leaderIdx];
            int secondScore = scores[secondIdx];

            // Must be a sole leader (no tie)
            if (leaderScore != secondScore) {
                int maxSecond = secondScore + remainingSum;
                if (maxSecond < leaderScore) {
                    newLeadIndex = leaderIdx;
                }
            }
        }

        // OUT OF CONTENTION candidate (last vs second-last)
        int newOutIndex = -1;
        if (numPlayers >= 2) {
            int lastIdx = indices.get(numPlayers - 1);
            int secondLastIdx = indices.get(numPlayers - 2);
            int lastScore = scores[lastIdx];
            int secondLastScore = scores[secondLastIdx];

            // Must be a sole last place (no tie)
            if (lastScore != secondLastScore) {
                int maxLast = lastScore + remainingSum;
                if (maxLast < secondLastScore) {
                    newOutIndex = lastIdx;
                }
            }
        }

        // --- Apply changes with animations using your show/hide helpers ---

        // Lead secured: hide old if changed
        if (leadSecuredIndex != -1 && leadSecuredIndex != newLeadIndex) {
            hideStatusIndicator(leadSecuredIndex);
            leadSecuredIndex = -1;
        }
        // Lead secured: show new
        if (newLeadIndex != -1 && leadSecuredIndex != newLeadIndex) {
            leadSecuredIndex = newLeadIndex;
            showStatusIndicator(newLeadIndex, true);
        }

        // Out of contention: hide old if changed
        if (outOfContentionIndex != -1 && outOfContentionIndex != newOutIndex) {
            hideStatusIndicator(outOfContentionIndex);
            outOfContentionIndex = -1;
        }
        // Out of contention: show new
        if (newOutIndex != -1 && outOfContentionIndex != newOutIndex) {
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

        // Cancel any running animation and ensure clean state
        tv.clearAnimation();
        tv.setTranslationX(0);

        tv.setTextColor(Color.BLACK);
        if (isLead) {
            tv.setText(getString(R.string.indicator_lead));
            tv.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            tv.setText(getString(R.string.indicator_out));
            tv.setBackgroundColor(Color.parseColor("#DC1704"));
        }

        if (tv.getVisibility() != View.VISIBLE) {
            tv.setVisibility(View.VISIBLE);
            Animation in = createSlideInAnimation();
            // ensure final state is visible and translation reset
            in.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) { }
                @Override public void onAnimationEnd(Animation animation) {
                    tv.clearAnimation();
                    tv.setTranslationX(0);
                }
                @Override public void onAnimationRepeat(Animation animation) { }
            });
            tv.startAnimation(in);
        }
    }

    private void hideStatusIndicator(final int playerIndex) {
        if (playerIndex < 0 || playerIndex >= statusIndicatorTexts.length) return;
        final TextView tv = statusIndicatorTexts[playerIndex];
        if (tv == null) return;
        if (tv.getVisibility() != View.VISIBLE) return;

        // Cancel any running animation first
        tv.clearAnimation();

        Animation slideOut = createSlideOutAnimation();
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Reset view state
                tv.clearAnimation();
                tv.setVisibility(View.GONE);
                tv.setText("");
                tv.setBackgroundColor(Color.TRANSPARENT);
                tv.setTranslationX(0);
            }

            @Override public void onAnimationRepeat(Animation animation) { }
        });
        tv.startAnimation(slideOut);
    }

    private Animation createSlideInAnimation() {
        int distance = dpToPx(80);
        TranslateAnimation anim = new TranslateAnimation(-distance, 0, 0, 0);
        anim.setDuration(200);
        anim.setFillAfter(false);
        return anim;
    }

    private Animation createSlideOutAnimation() {
        int distance = dpToPx(80);
        TranslateAnimation anim = new TranslateAnimation(0, -distance, 0, 0);
        anim.setDuration(200);
        anim.setFillAfter(false);
        return anim;
    }
}