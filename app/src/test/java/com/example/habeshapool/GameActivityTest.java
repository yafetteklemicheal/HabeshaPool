package com.example.habeshapool;

import static org.junit.Assert.*;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class GameActivityTest {

    @Test
    public void gameActivityClassIsPresent() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        assertNotNull("GameActivity should be present on the classpath", cls);
    }

    @Test
    public void gameActivityDefinesExpectedStringConstants() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Field prefs = cls.getDeclaredField("PREFS_NAME");
        Field keyLang = cls.getDeclaredField("KEY_LANGUAGE");
        Field langEn = cls.getDeclaredField("LANG_EN");
        Field langAm = cls.getDeclaredField("LANG_AM");

        assertTrue("PREFS_NAME should be static final", Modifier.isStatic(prefs.getModifiers()) && Modifier.isFinal(prefs.getModifiers()));
        assertTrue("KEY_LANGUAGE should be static final", Modifier.isStatic(keyLang.getModifiers()) && Modifier.isFinal(keyLang.getModifiers()));
        assertEquals("LANG_EN should be String", String.class, langEn.getType());
        assertEquals("LANG_AM should be String", String.class, langAm.getType());

        prefs.setAccessible(true);
        keyLang.setAccessible(true);
        langEn.setAccessible(true);
        langAm.setAccessible(true);

        assertEquals("PREFS_NAME value mismatch", "habesha_pool_prefs", prefs.get(null));
        assertEquals("KEY_LANGUAGE value mismatch", "preferred_language", keyLang.get(null));
        assertEquals("LANG_EN value mismatch", "en", langEn.get(null));
        assertEquals("LANG_AM value mismatch", "am", langAm.get(null));
    }

    @Test
    public void actionTypeEnumExistsAndHasExpectedConstants() throws Exception {
        Class<?> enumClass = Class.forName("com.example.habeshapool.GameActivity$ActionType");
        assertTrue("ActionType should be an enum", enumClass.isEnum());

        Object[] constants = enumClass.getEnumConstants();
        assertNotNull("ActionType enum constants should not be null", constants);
        assertEquals("ActionType should have exactly 3 constants", 3, constants.length);

        boolean hasNone = false, hasScore = false, hasFoul = false;
        for (Object c : constants) {
            String name = c.toString();
            if ("NONE".equals(name)) hasNone = true;
            if ("SCORE".equals(name)) hasScore = true;
            if ("FOUL".equals(name)) hasFoul = true;
        }
        assertTrue("ActionType must contain NONE", hasNone);
        assertTrue("ActionType must contain SCORE", hasScore);
        assertTrue("ActionType must contain FOUL", hasFoul);
    }

    @Test
    public void currentBallsFieldExistsAndIsArrayList() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Field f = cls.getDeclaredField("currentBalls");
        assertEquals("currentBalls should be java.util.ArrayList", ArrayList.class, f.getType());
    }

    @Test
    public void playersFieldExistsAndIsArrayList() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Field f = cls.getDeclaredField("players");
        assertEquals("players should be java.util.ArrayList", ArrayList.class, f.getType());
    }

    @Test
    public void lastRankingIndicesFieldExistsAndIsArrayList() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Field f = cls.getDeclaredField("lastRankingIndices");
        assertEquals("lastRankingIndices should be java.util.ArrayList", ArrayList.class, f.getType());
    }

    @Test
    public void aggregateScoreTextFieldExists() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Field f = cls.getDeclaredField("aggregateScoreText");
        assertNotNull("aggregateScoreText field should exist", f);
    }

    @Test
    public void uiArrayFieldsAreDeclaredAndAreArrays() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        String[] arrayFieldNames = new String[] {
                "playerNameEdits", "playerScoreTexts", "playerHistoryTexts",
                "scoreButtons", "foulButtons", "scratchButtons", "undoButtons",
                "playerSections", "shooterLabels", "rankEmojiTexts",
                "scoredHistoryLayouts", "statusIndicatorTexts"
        };
        for (String name : arrayFieldNames) {
            Field f = cls.getDeclaredField(name);
            assertTrue("Field " + name + " should be an array", f.getType().isArray());
        }
    }

    @Test
    public void uiArrayComponentTypesAreExpected() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Field nameEdits = cls.getDeclaredField("playerNameEdits");
        Field scoreTexts = cls.getDeclaredField("playerScoreTexts");
        Field historyTexts = cls.getDeclaredField("playerHistoryTexts");
        Field scoreButtons = cls.getDeclaredField("scoreButtons");

        assertEquals("playerNameEdits component type should be EditText",
                "android.widget.EditText", nameEdits.getType().getComponentType().getName());
        assertEquals("playerScoreTexts component type should be TextView",
                "android.widget.TextView", scoreTexts.getType().getComponentType().getName());
        assertEquals("playerHistoryTexts component type should be TextView",
                "android.widget.TextView", historyTexts.getType().getComponentType().getName());
        assertEquals("scoreButtons component type should be Button",
                "android.widget.Button", scoreButtons.getType().getComponentType().getName());
    }

    @Test
    public void nestedPlayerClassExistsAndHasFieldsAndMethods() throws Exception {
        Class<?> playerClass = Class.forName("com.example.habeshapool.GameActivity$Player");
        assertNotNull("Player nested class should exist", playerClass);

        Field name = playerClass.getDeclaredField("name");
        Field score = playerClass.getDeclaredField("score");
        Field history = playerClass.getDeclaredField("history");
        Field scoredBalls = playerClass.getDeclaredField("scoredBalls");

        assertEquals("Player.name should be String", String.class, name.getType());
        assertEquals("Player.score should be int", int.class, score.getType());
        assertEquals("Player.history should be String", String.class, history.getType());
        assertEquals("Player.scoredBalls should be ArrayList", ArrayList.class, scoredBalls.getType());

        Method addScore = playerClass.getDeclaredMethod("addScore", int.class);
        Method deductScore = playerClass.getDeclaredMethod("deductScore", int.class);
        assertEquals("addScore should return void", void.class, addScore.getReturnType());
        assertEquals("deductScore should return void", void.class, deductScore.getReturnType());
    }

    @Test
    public void getCurrentBallSumDeclaredAndReturnsInt() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("getCurrentBallSum");
        assertEquals("getCurrentBallSum should return int", int.class, m.getReturnType());
    }

    @Test
    public void createCurrentBallButtonsDeclaredAndReturnsVoid() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("createCurrentBallButtons");
        assertEquals("createCurrentBallButtons should return void", void.class, m.getReturnType());
    }

    @Test
    public void removeBallButtonDeclaredAndAcceptsView() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("removeBallButton", android.view.View.class);
        assertEquals("removeBallButton should return void", void.class, m.getReturnType());
    }

    @Test
    public void updatePlayerUIDeclaredAndAcceptsInt() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("updatePlayerUI", int.class);
        assertEquals("updatePlayerUI should return void", void.class, m.getReturnType());
    }

    @Test
    public void updatePlayerScoredHistoryUIDeclaredAndAcceptsInt() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("updatePlayerScoredHistoryUI", int.class);
        assertEquals("updatePlayerScoredHistoryUI should return void", void.class, m.getReturnType());
    }

    @Test
    public void dpToPxDeclaredAndReturnsInt() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("dpToPx", int.class);
        assertEquals("dpToPx should return int", int.class, m.getReturnType());
    }

    @Test
    public void getImageForBallDeclaredAndReturnsInt() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("getImageForBall", int.class);
        assertEquals("getImageForBall should return int", int.class, m.getReturnType());
    }

    @Test
    public void checkEndOfGameDeclaredAndIsNonPublicVoid() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("checkEndOfGame");
        assertEquals("checkEndOfGame should return void", void.class, m.getReturnType());
        assertFalse("checkEndOfGame should not be public", Modifier.isPublic(m.getModifiers()));
    }

    @Test
    public void handleRestartCurrentGameDeclaredAndReturnsVoid() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("handleRestartCurrentGame");
        assertEquals("handleRestartCurrentGame should return void", void.class, m.getReturnType());
    }

    @Test
    public void reorderShootersByLastRankingDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("reorderShootersByLastRanking");
        assertEquals("reorderShootersByLastRanking should return void", void.class, m.getReturnType());
    }

    @Test
    public void showConfirmDialogDeclaredWithStringAndRunnable() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("showConfirmDialog", String.class, Runnable.class);
        assertEquals("showConfirmDialog should return void", void.class, m.getReturnType());
    }

    @Test
    public void clearAllNameFocusAndHideKeyboardDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("clearAllNameFocusAndHideKeyboard");
        assertEquals("clearAllNameFocusAndHideKeyboard should return void", void.class, m.getReturnType());
    }

    @Test
    public void updateAggregateScoreDeclaredAndReturnsVoid() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("updateAggregateScore");
        assertEquals("updateAggregateScore should return void", void.class, m.getReturnType());
    }

    @Test
    public void performReturnToPlayerSelectionDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("performReturnToPlayerSelection");
        assertEquals("performReturnToPlayerSelection should return void", void.class, m.getReturnType());
    }

    @Test
    public void onBackPressedOverrideExists() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Method m = cls.getDeclaredMethod("onBackPressed");
        assertNotNull("onBackPressed should be declared", m);
    }

    @Test
    public void noPublicMutableFieldsInGameActivity() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.GameActivity");
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            assertFalse("Field " + f.getName() + " should not be public", Modifier.isPublic(f.getModifiers()));
        }
    }
}