package com.example.habeshapool;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class PlayerSelectionActivity extends AppCompatActivity {

    private static final String LANG_EN = "en";
    private static final String LANG_AM = "am";

    private String currentLanguage = LANG_EN; // always start in English

    private TextView titleText;
    private TextView languagePromptText;
    private TextView playersPromptText;

    private Button englishButton;
    private Button amharicButton;

    private Button twoPlayersButton;
    private Button threePlayersButton;
    private Button fourPlayersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ALWAYS default to English locale on app start
        applyLocale("en");

        setContentView(R.layout.activity_player_selection);

        titleText = findViewById(R.id.title_text);
        languagePromptText = findViewById(R.id.text_language_prompt);
        playersPromptText = findViewById(R.id.text_players_prompt);

        englishButton = findViewById(R.id.button_english);
        amharicButton = findViewById(R.id.button_amharic);

        twoPlayersButton = findViewById(R.id.button_two_players);
        threePlayersButton = findViewById(R.id.button_three_players);
        fourPlayersButton = findViewById(R.id.button_four_players);

        // Language button listeners (SESSION ONLY)
        englishButton.setOnClickListener(v -> {
            currentLanguage = LANG_EN;
            Context localized = updateLocaleResources("en");
            applyLanguageTexts(localized);
            updateLanguageButtonStyles();
        });

        amharicButton.setOnClickListener(v -> {
            currentLanguage = LANG_AM;
            Context localized = updateLocaleResources("am");
            applyLanguageTexts(localized);
            updateLanguageButtonStyles();
        });

        // Player count buttons
        View.OnClickListener listener = v -> {
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
            intent.putExtra("language", currentLanguage); // pass session language only
            startActivity(intent);
        };

        twoPlayersButton.setOnClickListener(listener);
        threePlayersButton.setOnClickListener(listener);
        fourPlayersButton.setOnClickListener(listener);

        // Apply initial UI
        applyLanguageTexts(this);
        updateLanguageButtonStyles();
    }

    private Context updateLocaleResources(String lang) {
        if (lang == null) lang = LANG_EN;
        Locale locale = LANG_AM.equals(lang) ? new Locale("am") : new Locale("en");
        Locale.setDefault(locale);

        Resources res = getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        return createConfigurationContext(config);
    }

    // --- Locale switching (SESSION ONLY, NO SAVING) ---
    private void applyLocale(String lang) {
        if (lang == null) lang = "en";

        Locale locale = lang.equals("am") ? new Locale("am") : new Locale("en");
        Locale.setDefault(locale);

        Resources res = getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    private void applyLanguageTexts(Context ctx) {
        if (ctx == null) ctx = this;

        titleText.setText(ctx.getString(R.string.welcome_title));
        languagePromptText.setText(ctx.getString(R.string.please_select_language));
        playersPromptText.setText(ctx.getString(R.string.please_select_players));

        englishButton.setText(ctx.getString(R.string.eng_short));
        amharicButton.setText(ctx.getString(R.string.amh_short));

        twoPlayersButton.setText("2");
        threePlayersButton.setText("3");
        fourPlayersButton.setText("4");
    }

    private void updateLanguageButtonStyles() {
        if (LANG_EN.equals(currentLanguage)) {
            englishButton.setAlpha(1.0f);
            amharicButton.setAlpha(0.5f);
        } else {
            englishButton.setAlpha(0.5f);
            amharicButton.setAlpha(1.0f);
        }
    }
}