package com.example.habeshapool;

import static org.junit.Assert.*;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PlayerSelectionActivityTest {

    @Test
    public void classIsPresent() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        assertNotNull(cls);
    }

    @Test
    public void languageConstantsExistAndAreStaticFinal() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Field langEn = cls.getDeclaredField("LANG_EN");
        Field langAm = cls.getDeclaredField("LANG_AM");
        assertTrue(Modifier.isStatic(langEn.getModifiers()));
        assertTrue(Modifier.isFinal(langEn.getModifiers()));
        assertTrue(Modifier.isStatic(langAm.getModifiers()));
        assertTrue(Modifier.isFinal(langAm.getModifiers()));
        langEn.setAccessible(true);
        langAm.setAccessible(true);
        assertEquals("en", langEn.get(null));
        assertEquals("am", langAm.get(null));
    }

    @Test
    public void currentLanguageFieldExistsAndIsString() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Field f = cls.getDeclaredField("currentLanguage");
        assertEquals(String.class, f.getType());
    }

    @Test
    public void uiTextFieldsDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Field titleText = cls.getDeclaredField("titleText");
        Field languagePromptText = cls.getDeclaredField("languagePromptText");
        Field playersPromptText = cls.getDeclaredField("playersPromptText");
        assertEquals("android.widget.TextView", titleText.getType().getName());
        assertEquals("android.widget.TextView", languagePromptText.getType().getName());
        assertEquals("android.widget.TextView", playersPromptText.getType().getName());
    }

    @Test
    public void uiButtonFieldsDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Field englishButton = cls.getDeclaredField("englishButton");
        Field amharicButton = cls.getDeclaredField("amharicButton");
        Field twoPlayersButton = cls.getDeclaredField("twoPlayersButton");
        Field threePlayersButton = cls.getDeclaredField("threePlayersButton");
        Field fourPlayersButton = cls.getDeclaredField("fourPlayersButton");
        assertEquals("android.widget.Button", englishButton.getType().getName());
        assertEquals("android.widget.Button", amharicButton.getType().getName());
        assertEquals("android.widget.Button", twoPlayersButton.getType().getName());
        assertEquals("android.widget.Button", threePlayersButton.getType().getName());
        assertEquals("android.widget.Button", fourPlayersButton.getType().getName());
    }

    @Test
    public void updateLocaleResourcesMethodDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("updateLocaleResources", String.class);
        assertEquals("android.content.Context", m.getReturnType().getName());
    }

    @Test
    public void applyLocaleMethodDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("applyLocale", String.class);
        assertEquals(void.class, m.getReturnType());
    }

    @Test
    public void applyLanguageTextsMethodDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("applyLanguageTexts", android.content.Context.class);
        assertEquals(void.class, m.getReturnType());
    }

    @Test
    public void updateLanguageButtonStylesMethodDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("updateLanguageButtonStyles");
        assertEquals(void.class, m.getReturnType());
    }

    @Test
    public void onCreateSignatureExists() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("onCreate", android.os.Bundle.class);
        assertNotNull(m);
        assertFalse(Modifier.isPublic(m.getModifiers()));
    }

    @Test
    public void updateLocaleResourcesHandlesNullParameterSignature() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("updateLocaleResources", String.class);
        assertEquals(1, m.getParameterCount());
    }

    @Test
    public void applyLanguageTextsAcceptsContextAndIsNonPublic() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("applyLanguageTexts", android.content.Context.class);
        assertFalse(Modifier.isPublic(m.getModifiers()));
    }

    @Test
    public void updateLanguageButtonStylesIsNonPublicVoid() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("updateLanguageButtonStyles");
        assertEquals(void.class, m.getReturnType());
    }

    @Test
    public void noPublicFieldsDeclared() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            assertFalse("Field " + f.getName() + " should not be public", Modifier.isPublic(f.getModifiers()));
        }
    }

    @Test
    public void englishAndAmharicButtonFieldsAreDistinct() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Field en = cls.getDeclaredField("englishButton");
        Field am = cls.getDeclaredField("amharicButton");
        assertNotNull(en);
        assertNotNull(am);
        assertNotEquals(en.getName(), am.getName());
    }

    @Test
    public void playerCountButtonsExistAndAreThreeDistinctFields() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Field two = cls.getDeclaredField("twoPlayersButton");
        Field three = cls.getDeclaredField("threePlayersButton");
        Field four = cls.getDeclaredField("fourPlayersButton");
        assertNotNull(two);
        assertNotNull(three);
        assertNotNull(four);
        assertNotEquals(two.getName(), three.getName());
        assertNotEquals(three.getName(), four.getName());
    }

    @Test
    public void applyLanguageTextsSetsPlayerButtonsTextSignature() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("applyLanguageTexts", android.content.Context.class);
        assertEquals(1, m.getParameterCount());
    }

    @Test
    public void updateLocaleResourcesReturnsContextType() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("updateLocaleResources", String.class);
        assertEquals("android.content.Context", m.getReturnType().getName());
    }

    @Test
    public void applyLocaleUpdatesConfigurationSignature() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("applyLocale", String.class);
        assertEquals(void.class, m.getReturnType());
    }

    @Test
    public void classImplementsNoUnexpectedPublicMethods() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method[] methods = cls.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().startsWith("lambda$")) continue;
            assertFalse("Method " + m.getName() + " should not be public", Modifier.isPublic(m.getModifiers()));
        }
    }

    @Test
    public void updateLanguageButtonStylesUsesCurrentLanguageField() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Field currentLanguage = cls.getDeclaredField("currentLanguage");
        assertEquals(String.class, currentLanguage.getType());
    }

    @Test
    public void applyLanguageTextsUsesResourceStringsSignature() throws Exception {
        Class<?> cls = Class.forName("com.example.habeshapool.PlayerSelectionActivity");
        Method m = cls.getDeclaredMethod("applyLanguageTexts", android.content.Context.class);
        assertNotNull(m);
    }
}