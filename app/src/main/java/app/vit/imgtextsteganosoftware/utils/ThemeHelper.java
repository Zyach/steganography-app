package app.vit.imgtextsteganosoftware.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public final class ThemeHelper {

  private static final String PREF_THEME_MODE = "theme_mode";

  private ThemeHelper() {
  }

  public static void applySavedMode(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    int mode = prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    AppCompatDelegate.setDefaultNightMode(mode);
  }

  public static void setMode(Context context, int mode) {
    AppCompatDelegate.setDefaultNightMode(mode);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    prefs.edit().putInt(PREF_THEME_MODE, mode).apply();
  }

  public static int getMode(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
  }
}
